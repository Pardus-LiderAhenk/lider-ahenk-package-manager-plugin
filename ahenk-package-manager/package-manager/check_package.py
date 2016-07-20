#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

from base.plugin.abstract_plugin import AbstractPlugin
from base.model.enum.ContentType import ContentType
import json
from base.system.system import System


class CheckPackage(AbstractPlugin):
    def __init__(self, data, context):
        super(AbstractPlugin, self).__init__()
        self.data = data
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()


    def handle_task(self):
        print('handle_task')
        try:
            package_name = str((self.data)['packageName'])
            package_version = str((self.data)['packageVersion'])
            uid = System.Ahenk.uid()
            a, result, b = self.execute('dpkg -s {} | grep Version'.format(package_name))
            data = result.split(':')
            if data[0] == 'Version': #Package is installed
                if package_version is None or len(package_version) == 0:
                    result = 'PACKAGE IS INSTALLED BUT WITH DIFFERENT VERSION - {}'.format(data[1])
                elif data[1] is not None and (package_version + '\n') in data[1]: #Package version is the same with wanted version
                    result = 'PACKAGE IS INSTALLED'
                else:
                    result = 'PACKAGE IS INSTALLED BUT WITH DIFFERENT VERSION - {}'.format(data[1])
            else: #Package is not installed
                result = 'PACKAGE IS NOT INSTALLED'
            res = {"uid": uid, "Result": result}
            self.logger.debug("[PACKAGE MANAGER] Result is: - {}".format(result))
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Paket Bilgileri başarıyla getirildi',
                                         data=json.dumps(res), content_type=ContentType.APPLICATION_JSON.value)
            self.logger.debug("[PACKAGE MANAGER] Package Info has sent")
        except Exception as e:
            self.logger.debug(str(e))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Paket Bilgilerini transferde beklenmedik hata!',
                                         content_type=ContentType.APPLICATION_JSON.value)


def handle_task(task, context):
    print('PackageManager Plugin Task')
    print('Task Data : {}'.format(str(task)))
    plugin = CheckPackage(task, context)
    plugin.handle_task()

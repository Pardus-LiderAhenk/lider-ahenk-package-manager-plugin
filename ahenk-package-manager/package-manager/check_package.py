#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

from base.plugin.abstract_plugin import AbstractPlugin
from base.model.enum.ContentType import ContentType
import subprocess
import json


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
            result = ''
            packageName = str((self.data)['packageName'])
            packageVersion = str((self.data)['packageVersion'])
            a, result, b = self.execute('dpkg -s {} | grep Version'.format(packageName))
            data = result.split(':')
            if data[0] == 'Version': #Package is installed
                if packageVersion is None or len(packageVersion) == 0:
                    result = 'PACKAGE IS INSTALLED BUT WITH DIFFERENT VERSION - {}'.format(data[1])
                elif data[1] is not None and (packageVersion + '\n') in data[1]: #Package version is the same with wanted version
                    result = 'PACKAGE IS INSTALLED'
                else:
                    result = 'PACKAGE IS INSTALLED BUT WITH DIFFERENT VERSION - {}'.format(data[1])
            else: #Package is not installed
                result = 'PACKAGE IS NOT INSTALLED'
            res = {"Result": result}
            self.logger.debug("[PACKAGE MANAGER] Result is: - {}".format(result))
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Package Manager Task - Getting Package Info Process completed successfully',
                                         data=json.dumps(res), content_type=ContentType.APPLICATION_JSON.value)
            self.logger.debug("[PACKAGE MANAGER] Package Info has sent")
        except Exception as e:
            self.logger.debug(str(e))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Error in Package Manager Task - Getting Package Info Process ',
                                         content_type=ContentType.APPLICATION_JSON.value)


def handle_task(task, context):
    print('PackageManager Plugin Task')
    print('Task Data : {}'.format(str(task)))
    plugin = CheckPackage(task, context)
    plugin.handle_task()

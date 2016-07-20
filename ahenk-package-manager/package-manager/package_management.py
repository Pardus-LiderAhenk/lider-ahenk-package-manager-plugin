#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

from base.plugin.abstract_plugin import AbstractPlugin
from base.model.enum.ContentType import ContentType
import json


class PackageManagement(AbstractPlugin):
    def __init__(self, data, context):
        super(AbstractPlugin, self).__init__()
        self.data = data
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

    def handle_task(self):
        print('handle_task')

        try:
            items = (self.data)['packageInfoList']
            result_message = ''
            tag = 'installed'
            for item in items:
                if item['tag'] == 'Kur' or item['tag'] == 'Install':
                    self.logger.debug("[PACKAGE MANAGER] Installing new package... {0}".format(item['packageName']))
                if item['tag'] == 'Kaldır' or item['tag'] == 'Uninstall':
                    tag = 'uninstalled'
                    self.logger.debug(
                        "[PACKAGE MANAGER] Removing process will be started... {0}".format(item['packageName']))
                command = '/bin/bash {0}package-manager/install_packages.sh {1} {2} {3}'.format(
                    self.Ahenk.plugins_path(), item['packageName'], item['version'], item['tag'])
                self.logger.debug(command)
                a, result, b = self.execute(command)
                self.logger.debug("[PACKAGE MANAGER] Result is : " + result)
                if a == 0:
                    result_message += 'Package is {0} - {1}={2}\r\n'.format(tag, item['packageName'], item['version'])
                else:
                    result_message += 'Package could not be {0} - {1}={2}\r\n'.format(tag, item['packageName'],
                                                                                           item['version'])
            data = {'Result': result_message}
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Paket Kur/Kaldır işlemleri başarıyla gerçekleştirildi',
                                         data=json.dumps(data),
                                         content_type=ContentType.APPLICATION_JSON.value)

        except Exception as e:
            self.logger.debug(str(e))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Paket Kur/Kaldır işlemleri gerçekleştirilirken beklenmedik hata!',
                                         content_type=ContentType.APPLICATION_JSON.value)


def handle_task(task, context):
    print('PackageManager Plugin Task')
    print('Task Data : {}'.format(str(task)))
    plugin = PackageManagement(task, context)
    plugin.handle_task()

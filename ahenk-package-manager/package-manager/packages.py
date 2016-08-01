#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

import json

from base.model.enum.ContentType import ContentType
from base.plugin.abstract_plugin import AbstractPlugin


class Packages(AbstractPlugin):
    def __init__(self, data, context):
        super(AbstractPlugin, self).__init__()
        self.data = data
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

    def handle_task(self):
        self.logger.debug('Handling Packages Task')
        try:
            resultMessage = 'Machine uid    :   {}\r\n'.format(self.Ahenk.uid())
            items = (self.data)['packageInfoList']
            a = 0
            for item in items:
                try:
                    if (str(item['tag']) == 'Yükle' or str(item['tag']) == 'Install') and item['source'] is not None:
                        try:
                            param = '/bin/bash {0}package-manager/add_repository_if_not_exists.sh "{1}"'.format(
                                self.Ahenk.plugins_path(), item['source'])
                            self.logger.debug(
                                "[PACKAGE MANAGER] Adding Repository if not exists... {0}".format(item['source']))
                            a, result, b = self.execute(param)
                            self.logger.debug("[PACKAGE MANAGER] Repository added")
                            resultMessage += 'Repository added - {}\r\n'.format(item['source'])
                        except Exception as e:
                            resultMessage += 'Repository could not be added - {}'.format(item['source'])

                    if a == 0 and (item['tag'] == 'Yükle' or item['tag'] == 'Install'):
                        self.logger.debug("[PACKAGE MANAGER] Installing new package... {0}".format(item['packageName']))
                        self.install_with_apt_get(item['packageName'], item['version'])
                        self.logger.debug("[PACKAGE MANAGER] Result is : " + result)
                        resultMessage += 'Package installed - {0}={1}\r\n'.format(item['packageName'], item['version'])
                    elif a == 0 and (item['tag'] == 'Kaldır' or item['tag'] == 'Uninstall'):
                        self.logger.debug("[PACKAGE MANAGER] Removing package... {0}".format(item['packageName']))
                        self.logger.debug(
                            "[PACKAGE MANAGER] sudo apt-get --yes --force-yes purge {0}={1}".format(item['packageName'],
                                                                                                    item['version']))
                        self.uninstall_package(item['packageName'], item['version'])
                        self.logger.debug("[PACKAGE MANAGER] Result is : " + result)
                        resultMessage += 'Package uninstalled - {0}={1}\r\n'.format(item['packageName'],
                                                                                    item['version'])
                except Exception as e:
                    if item['tag'] == 'Yükle' or item['tag'] == 'Install':
                        resultMessage += 'Package could not be installed - {0}={1}\r\n'.format(item['packageName'],
                                                                                               item['version'])
                    else:
                        resultMessage += 'Package could not be uninstalled - {0}={1}\r\n'.format(item['packageName'],
                                                                                                 item['version'])
            data = {'ResultMessage': resultMessage}

            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Paketler listelendi',
                                         data=json.dumps(data),
                                         content_type=ContentType.APPLICATION_JSON.value)
        except Exception as e:
            self.logger.debug(str(e))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Paketler listelenirken beklenmedik hata!',
                                         content_type=ContentType.APPLICATION_JSON.value)


def handle_task(task, context):
    plugin = Packages(task, context)
    plugin.handle_task()

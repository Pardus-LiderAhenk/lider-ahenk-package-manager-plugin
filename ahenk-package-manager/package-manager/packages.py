#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

from base.plugin.abstract_plugin import AbstractPlugin
from base.model.enum.ContentType import ContentType
import json


class Packages(AbstractPlugin):
    def __init__(self, data, context):
        super(AbstractPlugin, self).__init__()
        self.data = data
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

    def handle_task(self):
        print('Handling Packages Task')
        self.logger.debug('Handling Packages Task')
        try:
            resultMessage = ''
            items = (self.data)['packageInfoList']
            a = 0
            for item in items:
                try:
                    if item['source'] is not None:
                        try:
                            param = '/bin/bash {0}package-manager/add_repository_if_not_exists.sh "{1}"'.format(
                                self.Ahenk.plugins_path(), item['source'])
                            self.logger.debug("[PACKAGE MANAGER] Adding Repository if not exists... {0}".format(item['source']))
                            a, result, b = self.execute(param)
                            self.logger.debug("[PACKAGE MANAGER] Repository added")
                            resultMessage += 'Repository added - {}\r\n'.format(item['source'])
                        except Exception as e:
                            resultMessage += 'Repository could not be added - {}'.format(item['source'])
                    if a == 0 and (item['tag'] == 'Kur' or item['tag'] == 'Install'):
                        self.logger.debug("[PACKAGE MANAGER] Installing new package... {0}".format(item['packageName']))
                        self.logger.debug(
                            "[PACKAGE MANAGER] sudo apt-get --yes --force-yes install {0}={1}".format(item['packageName'],
                                                                                         item['version']))
                        command = "sudo apt-get --yes --force-yes install {0}={1}".format(item['packageName'],
                                                                                          item['version'])
                        a, result, b = self.execute(command)
                        self.logger.debug("[PACKAGE MANAGER] Result is : " + result)
                        resultMessage += 'Package installed - {0}={1}\r\n'.format(item['packageName'], item['version'])
                    elif a == 0 and (item['tag'] == 'Kaldır' or item['tag'] == 'Uninstall'):
                        self.logger.debug("[PACKAGE MANAGER] Removing package... {0}".format(item['packageName']))
                        self.logger.debug(
                            "[PACKAGE MANAGER] sudo apt-get --yes --force-yes purge {0}={1}".format(item['packageName'], item['version']))
                        command = "sudo apt-get --yes --force-yes purge {0}={1}".format(item['packageName'], item['version'])
                        a, result, b = self.execute(command)
                        self.logger.debug("[PACKAGE MANAGER] Result is : " + result)
                        resultMessage += 'Package uninstalled - {0}={1}\r\n'.format(item['packageName'],
                                                                                    item['version'])
                except Exception as e:
                    if item['tag'] == 'Kur' or item['tag'] == 'Install':
                        resultMessage += 'Package could not be installed - {0}={1}\r\n'.format(item['packageName'],
                                                                                               item['version'])
                    else:
                        resultMessage += 'Package could not be uninstalled - {0}={1}\r\n'.format(item['packageName'],
                                                                                                 item['version'])
            data = {'ResultMessage': resultMessage}
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Getting Packages Process completed successfully',
                                         data=json.dumps(data),
                                         content_type=ContentType.APPLICATION_JSON.value)
        except Exception as e:
            self.logger.debug(str(e))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Error in Packages Task - Getting Packages Process ',
                                         content_type=ContentType.APPLICATION_JSON.value)


def handle_task(task, context):
    print('PackageManager Plugin Task')
    print('Task Data : {}'.format(str(task)))
    plugin = Packages(task, context)
    plugin.handle_task()

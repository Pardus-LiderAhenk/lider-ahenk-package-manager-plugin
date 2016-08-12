#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

import json
import subprocess
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
            resultMessage = 'Dn    :   {}\r\n'.format(self.Ahenk.dn())
            items = (self.data)['packageInfoList']
            return_code = 0
            for item in items:
                try:
                    if (str(item['tag']) == 'Yükle' or str(item['tag']) == 'Install') and item['source'] is not None:
                        try:
                            param = '/bin/bash {0}package-manager/add_repository_if_not_exists.sh "{1}"'.format(
                                self.Ahenk.plugins_path(), item['source'])
                            self.logger.debug(
                                "[PACKAGE MANAGER] Adding Repository if not exists... {0}".format(item['source']))
                            process = subprocess.Popen(param, shell=True)
                            process.wait()
                            return_code = process.returncode
                            self.logger.debug("[PACKAGE MANAGER] Repository added, Result Code : {0} ".format(return_code))
                            resultMessage += 'Depo eklendi - {}\r\n'.format(item['source'])
                        except Exception as e:
                            resultMessage += 'Depo eklenemedi - {}'.format(item['source'])

                    if return_code == 0 and (item['tag'] == 'Yükle' or item['tag'] == 'Install'):
                        self.logger.debug("[PACKAGE MANAGER] Installing new package... {0}".format(item['packageName']))
                        result_code, p_result, p_err = self.install_with_apt_get(item['packageName'], item['version'])
                        if result_code == 0:
                            self.logger.debug("[PACKAGE MANAGER] Package installed : {0}={1}".format(item['packageName'], item['version']))
                            resultMessage += 'Paket yüklendi - {0}={1}\r\n'.format(item['packageName'], item['version'])
                        else:
                            self.logger.debug(
                                "[PACKAGE MANAGER] Package couldnt be installed : {0}={1}".format(item['packageName'],
                                                                                       item['version']))
                            resultMessage += 'Paket yüklenemedi - {0}={1}\r\n'.format(item['packageName'],
                                                                                               item['version'])
                    elif return_code == 0 and (item['tag'] == 'Kaldır' or item['tag'] == 'Uninstall'):
                        self.logger.debug("[PACKAGE MANAGER] Removing package... {0}".format(item['packageName']))
                        self.logger.debug(
                            "[PACKAGE MANAGER] sudo apt-get --yes --force-yes purge {0}={1}".format(item['packageName'],
                                                                                                    item['version']))
                        result_code, p_result, p_err = self.uninstall_package(item['packageName'], item['version'])
                        if result_code == 0:
                            self.logger.debug('[PACKAGE MANAGER] : Package uninstalled - {0}={1}\r\n'.format(item['packageName'], item['version']))
                            resultMessage += 'Paket kaldırıldı - {0}={1}\r\n'.format(item['packageName'],
                                                                                    item['version'])
                        else:
                            self.logger.debug(
                                '[PACKAGE MANAGER] : Package couldnt be uninstalled - {0}={1}\r\n'.format(item['packageName'],
                                                                                               item['version']))
                            resultMessage += 'Paket kaldırılamadı - {0}={1}\r\n'.format(item['packageName'],
                                                                                                 item['version'])

                except Exception as e:
                    if item['tag'] == 'Yükle' or item['tag'] == 'Install':
                        resultMessage += 'Paket yüklenemedi - {0}={1}\r\n'.format(item['packageName'],
                                                                                               item['version'])
                    else:
                        resultMessage += 'Paket kaldırılamadı - {0}={1}\r\n'.format(item['packageName'],
                                                                                                 item['version'])

                    self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                                 message=resultMessage.format(resultMessage))
            data = {'ResultMessage': resultMessage}

            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Paketler listelendi\r\n {}'.format(resultMessage),
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
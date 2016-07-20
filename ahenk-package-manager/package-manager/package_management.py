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
        try:
            items = self.data['packageInfoList']
            result_message = ''
            installed_packages = ''
            uninstalled_packages = ''
            failed_packages = ''

            for item in items:

                # Install package
                if item['tag'] == 'Install':
                    self.logger.debug("[PACKAGE MANAGER] Installing package: {0}".format(item['packageName']))
                    try:
                        self.install_with_apt_get(item['packageName'], item['version'])
                        self.logger.debug("[PACKAGE MANAGER] Installed package: {0}".format(item['packageName']))
                        installed_packages += ' ' + item['packageName']
                    except Exception as e:
                        self.logger.error(str(e))
                        failed_packages += ' ' + item['packageName']

                # Uninstall package
                if item['tag'] == 'Uninstall':
                    self.logger.debug("[PACKAGE MANAGER] Uninstalling package: {0}".format(item['packageName']))
                    try:
                        self.uninstall_package(item['packageName'], item['version'])
                        self.logger.debug("[PACKAGE MANAGER] Uninstalled package: {0}".format(item['packageName']))
                        uninstalled_packages += ' ' + item['packageName']
                    except Exception as e:
                        self.logger.error(str(e))
                        failed_packages += ' ' + item['packageName']

                # Result message
                if not installed_packages:
                    result_message += ' Kurulan paketler: (' + installed_packages + ' )'
                if not uninstalled_packages:
                    result_message += ' Kaldırılan paketler: (' + uninstalled_packages + ' )'
                if not failed_packages:
                    result_message += ' İşlem sırasında hata alan paketler: (' + failed_packages + ' )'
                    self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                                 message='Paket kur/kaldır işlemleri gerçekleştirilirken hata oluştu:' + str(e),
                                                 data=json.dumps({'Result': result_message}),
                                                 content_type=ContentType.APPLICATION_JSON.value)
                else:
                    self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                                 message='Paket kur/kaldır işlemleri başarıyla gerçekleştirildi',
                                                 data=json.dumps({'Result': result_message}),
                                                 content_type=ContentType.APPLICATION_JSON.value)

                # TODO return package list!

        except Exception as e:
            self.logger.error(str(e))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Paket kur/kaldır işlemleri gerçekleştirilirken hata oluştu:' + str(e),
                                         content_type=ContentType.APPLICATION_JSON.value)


def handle_task(task, context):
    plugin = PackageManagement(task, context)
    plugin.handle_task()

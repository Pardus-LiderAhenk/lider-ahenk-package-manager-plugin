#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

from base.plugin.abstract_plugin import AbstractPlugin
from base.model.enum.ContentType import ContentType
import subprocess
import json


class InstalledPackages(AbstractPlugin):
    def __init__(self, data, context):
        super(AbstractPlugin, self).__init__()
        self.data = data
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()


    def handle_task(self):
        print('handle_task')
        try:
            a, result, b = self.execute('/bin/bash {0}package-manager/installed_packages.sh {1}'.format(self.Ahenk.plugins_path(), self.Ahenk.plugins_path()))
            data = {}
            md5sum = self.get_md5_file(str(self.Ahenk.plugins_path() + "package-manager/installed_packages.txt"))
            self.logger.debug('[PACKAGE MANAGER] {0} renaming to {1}'.format(str(self.Ahenk.plugins_path() + 'package-manager/installed_packages.txt'), md5sum))
            self.rename_file('{0}{1}'.format(self.Ahenk.plugins_path(), 'package-manager/installed_packages.txt'), self.Ahenk.received_dir_path() + '/' + md5sum)
            self.logger.debug('[PACKAGE MANAGER] Renamed.')
            data['md5'] = md5sum
            json_data = json.dumps(data)
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Kurulu olan paketler başarıyla getirildi',
                                         data=json_data, content_type=self.get_content_type().TEXT_PLAIN.value)
            self.logger.debug('[PACKAGE MANAGER] Installed Packages task is handled successfully')
        except Exception as e:
            self.logger.debug(str(e))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Kurulu olan paketler getirilirken beklenmedik hata!',
                                         content_type=ContentType.APPLICATION_JSON.value)


def handle_task(task, context):
    print('PackageManager Plugin Task')
    print('Task Data : {}'.format(str(task)))
    plugin = InstalledPackages(task, context)
    plugin.handle_task()

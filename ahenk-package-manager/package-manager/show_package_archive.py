#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

import json

from base.model.enum.ContentType import ContentType
from base.plugin.abstract_plugin import AbstractPlugin


class ShowPackageArchive(AbstractPlugin):
    def __init__(self, data, context):
        super(AbstractPlugin, self).__init__()
        self.data = data
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

    def handle_task(self):
        try:
            package_name = str((self.data)['packageName'])
            self.logger.debug('[PACKAGE MANAGER] Package Installation History query is executing...')
            a, result, b = self.execute('grep "status installed {0}:" /var/log/dpkg*.log'.format(package_name))
            data = {}
            res = []
            message = ""
            self.logger.debug('[PACKAGE MANAGER] Package archive info is being parsed...')

            if result is not None:
                result_lines = result.splitlines()
                for line in result_lines:
                    result_array = line.split(' ')
                    obj = {"installationDate": '{0} {1}'.format(result_array[0], result_array[1]),
                           "version": result_array[5]}
                    res.append(obj)

            if a == 0 and len(res) > 0:
                data = {"Result": res}
                message = 'Paket arşivi başarıyla getirildi'
            elif a != 0:
                message = 'Paket bulunamadı'

            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message=message,
                                         data=json.dumps(data), content_type=ContentType.APPLICATION_JSON.value)
            self.logger.debug('[PACKAGE MANAGER] Getting Package Archive task is handled successfully')
        except Exception as e:
            self.logger.debug(str(e))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Paket arşivi getirilirken beklenmedik hata!',
                                         content_type=ContentType.APPLICATION_JSON.value)


def handle_task(task, context):
    plugin = ShowPackageArchive(task, context)
    plugin.handle_task()

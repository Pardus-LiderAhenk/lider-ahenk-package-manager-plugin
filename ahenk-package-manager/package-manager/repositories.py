#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

from base.plugin.abstract_plugin import AbstractPlugin
from base.model.enum.ContentType import ContentType
import json


class PackageSourcesList(AbstractPlugin):
    def __init__(self, data, context):
        super(AbstractPlugin, self).__init__()
        self.data = data
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

    def handle_task(self):
        error_message = ""
        try:
            result_code, p_out, p_err = self.execute('/bin/bash {}package-manager/sourcelist.sh'.format(self.Ahenk.plugins_path()))
            data = {}
            if result_code != 0:
                self.logger.error("[PACKAGE MANAGER] Error occurred while listing repositories: " + str(p_err))
                error_message += " Paket depoları okunurken hata oluştu: " + str(p_err)
            else:
                data['Result'] = p_out
                self.logger.debug("[PACKAGE MANAGER] Repositories are listed")

            if not error_message:
                self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                             message='Paket depoları başarıyla okundu.',
                                             data=json.dumps(data), content_type=ContentType.APPLICATION_JSON.value)
            else:
                self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                             message=error_message,
                                             content_type=ContentType.APPLICATION_JSON.value)

        except Exception as e:
            self.logger.debug(str(e))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message="Paket depoları okunurken hata oluştu: " + str(e),
                                         content_type=ContentType.APPLICATION_JSON.value)


def handle_task(task, context):
    plugin = PackageSourcesList(task, context)
    plugin.handle_task()

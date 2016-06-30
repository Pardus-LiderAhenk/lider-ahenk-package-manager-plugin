#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

from base.plugin.abstract_plugin import AbstractPlugin
from base.system.system import System
from base.model.enum.ContentType import ContentType
from time import sleep
import smtplib
import string


class PackageManagement(AbstractPlugin):
    def __init__(self, data, context):
        super(AbstractPlugin, self).__init__()
        self.data = data
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

    def handle_task(self):
        print('handle_task')
     #   self.context.create_response(code=self.message_code.TASK_PROCESSED.value, message='resource-usage-response',
     #                                data=data, content_type=ContentType.APPLICATION_JSON.value)


def handle_task(task, context):
    print('PackageManager Plugin Task')
    print('Task Data : {}'.format(str(task)))
    plugin = PackageManagement(task, context)
    plugin.handle_task()

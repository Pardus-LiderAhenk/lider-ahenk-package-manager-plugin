#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

from base.plugin.abstract_plugin import AbstractPlugin
from base.model.enum.ContentType import ContentType
import subprocess
from subprocess import call


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
            out_bytes = subprocess.check_output(['sh',
                                                 './plugins/package-manager/packages.sh'])
            result = out_bytes.decode(encoding='utf-8')
            print(result)
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Getting Packages Process completed successfully',
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

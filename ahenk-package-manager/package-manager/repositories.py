#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

from base.plugin.abstract_plugin import AbstractPlugin
from base.model.enum.ContentType import ContentType
import subprocess


class PackageSourcesList(AbstractPlugin):
    def __init__(self, data, context):
        super(AbstractPlugin, self).__init__()
        self.data = data
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

    def handle_task(self):
        print('handle_task')
        try:
            out_bytes = subprocess.check_output(['sh',
                                                 './plugins/package-manager/sourcelist.sh'])
            result = out_bytes.decode(encoding='utf-8')
            data = {'Result': result}
            self.logger.debug("Repositories are listed")
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Package Manager Task - Getting Repositories Process completed successfully',
                                         data=data, content_type=ContentType.APPLICATION_JSON.value)
        except Exception as e:
            self.logger.debug(str(e))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Error in Package Manager Task - Getting Repositories Process ',
                                         content_type=ContentType.APPLICATION_JSON.value)


def handle_task(task, context):
    print('PackageManager Plugin Task')
    print('Task Data : {}'.format(str(task)))
    plugin = PackageSourcesList(task, context)
    plugin.handle_task()

#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

from base.plugin.abstract_plugin import AbstractPlugin
from base.model.enum.ContentType import ContentType
import subprocess
import json


class PackageSources(AbstractPlugin):
    def __init__(self, data, context):
        super(AbstractPlugin, self).__init__()
        self.data = data
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

    def handle_task(self):
        print('handle_package_sources_task')
        added_items = (self.data)['addedItems']
        deleted_items = (self.data)['deletedItems']
        try:
            for item in added_items:
                subprocess.check_output(['sh', './plugins/package-manager/addedLists.sh', str(item)])
            self.logger.debug("Added Sources append to /etc/apt/soruces.list.d/ahenk.list file")
            for item in deleted_items:
                deleted_files_and_line_numbers = subprocess.check_output(['sh', './plugins/package-manager/deletedLists.sh', str(item)]).decode(encoding='utf-8')
                deleted_files_andL_line_numbers_array = deleted_files_and_line_numbers.split(':')
                print(deleted_files_andL_line_numbers_array)
                with open(deleted_files_andL_line_numbers_array[0], "r") as textobj:
                    lines = list(textobj)
                del lines[int(deleted_files_andL_line_numbers_array[1]) - 1]
                with open(deleted_files_andL_line_numbers_array[0], "w") as textobj:
                    for n in lines:
                        textobj.write(n)
            self.logger.debug("Deleted Sources deleted from interested file")
            out_bytes = subprocess.check_output(['sh',
                                                 './plugins/package-manager/sourcelist.sh'])
            result = out_bytes.decode(encoding='utf-8')
            data = {'Result': result}
            self.logger.debug("Repositories are listed")
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Package Manager Task - Editing Repositories Process completed successfully',
                                         data=data, content_type=ContentType.APPLICATION_JSON.value)
        except Exception as e:
            self.logger.debug(str(e))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Error in Package Manager Task - Editing Repositories Process ',
                                         content_type=ContentType.APPLICATION_JSON.value)


def handle_task(task, context):
    print('PackageManager Plugin Task')
    print('Task Data : {}'.format(str(task)))
    plugin = PackageSources(task, context)
    plugin.handle_task()

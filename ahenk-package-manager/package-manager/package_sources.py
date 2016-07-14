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
                param = '/bin/bash {0}package-manager/addedLists.sh "{1}"'.format(self.Ahenk.plugins_path(), str(item))
                self.execute(param)
            self.logger.debug("[PACKAGE MANAGER] Added Sources append to /etc/apt/sources.list.d/ahenk.list file")
            for item in deleted_items:

                param = '/bin/bash {0}package-manager/deletedLists.sh "{1}"'.format(self.Ahenk.plugins_path(), str(item))
                a, result, b = self.execute(param)
                deleted_files_and_line_numbers_array = result.split(':')
                print(deleted_files_and_line_numbers_array)
                with open(deleted_files_and_line_numbers_array[0], "r") as textobj:
                    lines = list(textobj)
                del lines[int(deleted_files_and_line_numbers_array[1]) - 1]
                with open(deleted_files_and_line_numbers_array[0], "w") as textobj:
                    for n in lines:
                        textobj.write(n)
            self.logger.debug("[PACKAGE MANAGER] Deleted Sources deleted from interested file")
            self.logger.debug("[PACKAGE MANAGER] Updated all repositories...")
            a, result, b = self.execute('sudo apt-get update')
            print(result)
            param = '/bin/bash {0}package-manager/sourcelist.sh'.format(self.Ahenk.plugins_path())
            a, result, b = self.execute(param)
            data = {'Result': result}
            self.logger.debug("[PACKAGE MANAGER] Repositories are listed")
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Package Manager Task - Editing Repositories Process completed successfully',
                                         data=json.dumps(data), content_type=ContentType.APPLICATION_JSON.value)
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

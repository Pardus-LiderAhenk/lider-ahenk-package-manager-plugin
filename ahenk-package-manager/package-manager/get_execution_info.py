#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

import json

from base.plugin.abstract_plugin import AbstractPlugin


class VersionInfoItem:
    def __init__(self, command_name, package_name, package_version):
        self.commandName = command_name
        self.packageName = package_name
        self.packageVersion = package_version


class CommandExecutionInfoItem:
    def __init__(self, command_name, user, process_time, start_date):
        self.commandName = command_name
        self.user = user
        self.processTime = process_time
        self.startDate = start_date


def encode_version_info_object(obj):
    if isinstance(obj, VersionInfoItem):
        return obj.__dict__
    return obj


def encode_command_execution_info_object(obj):
    if isinstance(obj, CommandExecutionInfoItem):
        return obj.__dict__
    return obj


class GetExecutionInfo(AbstractPlugin):
    def __init__(self, data, context):
        super(AbstractPlugin, self).__init__()
        self.data = data
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()
        self.command_execution_statistic_list = []
        self.version_list = []

    def check_output(self, result_code, result):
        try:
            list = []
            if result_code == 0:
                for line in result.splitlines():
                    output_columns = line.split()
                    command_name = output_columns[0]
                    user = output_columns[len(output_columns) - 8]
                    process_time = output_columns[len(output_columns) - 6]
                    start_date = output_columns[len(output_columns) - 4] + ' ' + output_columns[
                        len(output_columns) - 3] + ' ' + output_columns[len(output_columns) - 2] + ' ' + output_columns[
                                     len(output_columns) - 1]
                    list.append(CommandExecutionInfoItem(command_name, user, process_time, start_date))
            return list

        except Exception as e:
            self.logger.debug('[ PACKAGE MANAGER ]Error in check_output method {}'.format(str(e)))

    def handle_task(self):
        try:
            commands = str((self.data)['command'])
            user = str((self.data)['user'])
            is_strict_match = (self.data)['isStrictMatch']
            dn = str(self.Ahenk.dn())
            result_message = ''
            res = {}
            if dn is None:
                dn = " "
            if commands is not None and commands != 'None' and len(commands) > 0:
                for command in commands.split(' '):
                    result_code, result, p_err = self.execute('whereis {}'.format(command))
                    if result_code == 0:
                        result = result.split(':')[1]
                        result = result.split()[0]
                        result_code, result, p_err = self.execute('dpkg-query -S {}'.format(result))
                        if result_code == 0:  # Command exists
                            result = result.split(': ')[0]
                            result_code, p_result, p_err = self.execute('dpkg -s {} | grep Version'.format(result))
                            if result_code == 0:
                                self.version_list.append(VersionInfoItem(command, result, p_result.split(': ')[1]))
                            else:
                                result_message += 'Command\'s related package version could not be parsed(Deb : {}).'.format(
                                    result)
                                self.version_list.append(VersionInfoItem(command, result, '-'))
                        else:  # command not exists
                            result_message += 'Command\'s related package could not be found(Command : {})'.format(
                                result)
                            self.version_list.append(VersionInfoItem(command, '-', '-'))
                    else:  # command not exists
                        result_message += 'Command {} could not found'

            if commands != 'None' and len(commands) > 0:
                if is_strict_match is False:
                    comm = 'lastcomm --command {0}'.format(commands)
                    if user != 'None' and len(user) > 0:
                        comm += " --user {}".format(user)
                    result_code, result, error = self.execute(comm)
                    result_list = self.check_output(result_code, result)
                    if len(result_list) > 0:
                        self.command_execution_statistic_list.extend(result_list)
                else:
                    for command in commands.split():
                        comm = 'lastcomm --command {0} '.format(command)
                        if user != 'None' and len(user) > 0:
                            comm += " --user {}".format(user)
                        comm += " --strict-match"
                        result_code, result, error = self.execute(comm)
                        result_list = self.check_output(result_code, result)
                        if len(result_list) > 0:
                            self.command_execution_statistic_list.extend(result_list)
            elif user != 'None' and len(user) > 0:
                comm = 'lastcomm --user {0} '.format(user)
                if is_strict_match is True:
                    comm += ' --strict-match'
                result_code, result, error = self.execute(comm)
                result_list = self.check_output(result_code, result)
                if len(result_list) > 0:
                    self.command_execution_statistic_list.extend(result_list)

            res["dn"] = dn
            result_command_execution_info_list = json.dumps([ob.__dict__ for ob in self.command_execution_statistic_list])
            result_version_list = json.dumps([ob.__dict__ for ob in self.version_list])
            self.logger.debug('[ PACKAGE MANAGER ]' + 'Command Execution Info list: ' + str(result_command_execution_info_list))
            if self.command_execution_statistic_list is not None and len(self.command_execution_statistic_list) > 0:
                res["commandExecutionInfoList"] = result_command_execution_info_list
            if self.version_list is not None and len(self.version_list) > 0:
                res["versionList"] = result_version_list

            self.logger.debug("[PACKAGE MANAGER] Execution Info fetched succesfully. ")
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Uygulama çalıştırma bilgileri başarıyla sisteme geçirildi.',
                                         data=json.dumps(res), content_type=self.get_content_type().APPLICATION_JSON.value)
            self.logger.debug("[PACKAGE MANAGER] Execution Info has sent")
        except Exception as e:
            self.logger.debug(str(e))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Uygulama çalıştırma bilgilerini getirirken beklenmedik hata!')


def handle_task(task, context):
    plugin = GetExecutionInfo(task, context)
    plugin.handle_task()

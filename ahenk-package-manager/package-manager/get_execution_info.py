#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author: Cemre ALPSOY <cemre.alpsoy@agem.com.tr>

import json

from base.plugin.abstract_plugin import AbstractPlugin


class GetExecutionInfo(AbstractPlugin):
    def __init__(self, data, context):
        super(AbstractPlugin, self).__init__()
        self.data = data
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()
        self.command_execution_statistic_list = []
        self.version_list = []
        self.result_message = ''
        self.logger.debug('[PACKAGE MANAGER] Execution info initialized')

    def handle_task(self):

        self.logger.debug('[PACKAGE MANAGER] Task handling')
        try:
            commands = self.data['command']
            user = self.data['user']
            is_strict_match = self.data['isStrictMatch']
            dn = self.Ahenk.dn()
            res = {}
            if dn is None:
                self.logger.debug('[PACKAGE MANAGER] Dn not found')
                dn = " "
            res['dn'] = dn
            if commands:
                self.get_version_list(commands)
                if is_strict_match is False:
                    lastcomm_command = 'lastcomm --command {0}'.format(commands)
                    if user:
                        lastcomm_command += " --user {}".format(user)
                    result_code, result, error = self.execute(lastcomm_command)
                    result_list = self.check_output(result_code, result)
                    if result_list is None:
                        return
                    elif len(result_list) > 0:
                        self.command_execution_statistic_list.extend(result_list)
                else:
                    for command in commands.split():
                        lastcomm_command = 'lastcomm --command {0} '.format(command)
                        if user:
                            lastcomm_command += " --user {}".format(user)
                        lastcomm_command += " --strict-match"
                        result_code, result, error = self.execute(lastcomm_command)
                        result_list = self.check_output(result_code, result)
                        if result_list is None:
                            return
                        elif len(result_list) > 0:
                            self.command_execution_statistic_list.extend(result_list)
            elif user:
                lastcomm_command = 'lastcomm --user {0} '.format(user)
                if is_strict_match is True:
                    lastcomm_command += ' --strict-match'
                result_code, result, error = self.execute(lastcomm_command)
                result_list = self.check_output(result_code, result)
                if result_list is None:
                    return
                elif len(result_list) > 0:
                    self.command_execution_statistic_list.extend(result_list)
            elif user is None and commands is None:
                result_code, result, error = self.execute('lastcomm')
                result_list = self.check_output(result_code, result)
                if result_list is None:
                    return
                elif len(result_list) > 0:
                    self.command_execution_statistic_list.extend(result_list)
            result_command_execution_info_list = json.dumps(
                [ob.__dict__ for ob in self.command_execution_statistic_list])
            result_version_list = json.dumps([ob.__dict__ for ob in self.version_list])
            self.logger.debug(
                '[ PACKAGE MANAGER ]' + 'Command Execution Info list: ' + str(result_command_execution_info_list))
            if self.command_execution_statistic_list is not None and len(self.command_execution_statistic_list) > 0:
                res["commandExecutionInfoList"] = result_command_execution_info_list
            if self.version_list is not None and len(self.version_list) > 0:
                res["versionList"] = result_version_list

            self.logger.debug("[PACKAGE MANAGER] Execution Info fetched succesfully. ")
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Uygulama çalıştırma bilgileri başarıyla sisteme geçirildi.',
                                         data=json.dumps(res),
                                         content_type=self.get_content_type().APPLICATION_JSON.value)
            self.logger.debug("[PACKAGE MANAGER] Execution Info has sent")
        except Exception as e:
            self.logger.debug(str(e))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Uygulama çalıştırma bilgilerini getirirken beklenmedik hata!')

    def get_version_list(self, commands):
        for command in commands.split(' '):
            result_code, result, p_err = self.execute('whereis {0}'.format(command))
            if result_code == 0:
                result = result.split(':')[1]
                result = result.split()[0]
                result_code, result, p_err = self.execute('dpkg-query -S {0}'.format(result))
                if result_code == 0:  # Command exists
                    result = result.split(': ')[0]
                    result_code, p_result, p_err = self.execute('dpkg -s {0} | grep Version'.format(result))
                    if result_code == 0:
                        self.version_list.append(VersionInfoItem(command, result, p_result.split(': ')[1]))
                    else:
                        self.result_message += 'Command\'s related package version could not be parsed(Deb : {0}).'.format(
                            result)
                        self.version_list.append(VersionInfoItem(command, result, '-'))
                else:  # command not exists
                    self.result_message += 'Command\'s related package could not be found(Command : {0})'.format(
                        result)
                    self.version_list.append(VersionInfoItem(command, '-', '-'))
            else:  # command not exists
                self.result_message += 'Command {0} could not found'.format(command)


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
                self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                             message='Uygulama çalıştırma bilgilerini getirirken beklenmedik hata!')
                return list
            else:
                self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Uygulama çalıştırma bilgilerini getirirken beklenmedik hata!')
                return None

        except Exception as e:
            self.logger.debug('[ PACKAGE MANAGER ]Error in check_output method {}'.format(str(e)))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Uygulama çalıştırma bilgilerini getirirken beklenmedik hata!')


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


def handle_task(task, context):
    plugin = GetExecutionInfo(task, context)
    plugin.handle_task()

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
        self.temp_file_name = str(self.generate_uuid())
        self.file_path = '{0}{1}'.format(str(self.Ahenk.received_dir_path()), self.temp_file_name)

    def handle_task(self):

        self.logger.debug('[PACKAGE MANAGER] Task handling')
        try:
            commands = self.data['command']
            user = self.data['user']
            is_strict_match = self.data['isStrictMatch']
            res = {}
            if commands:
                self.get_version_list(commands)
                if is_strict_match is False:
                    lastcomm_command = 'lastcomm --command {0}'.format(commands)
                    if user:
                        lastcomm_command += " --user {}".format(user)
                    self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] {0} command will be executed'.format(lastcomm_command))
                    result_code, result, error = self.execute(lastcomm_command)
                    self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] {0} command is executed'.format(lastcomm_command))
                    result_list = self.check_output(result_code, result)
                    if result_list is None:
                        return
                    elif len(result_list) > 0:
                        self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] lastcomm result list will be added to main result list')
                        self.command_execution_statistic_list.extend(result_list)
                        self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] lastcomm result list is added to main result list ')
                    elif len(result_list) == 0:
                        self.logger.debug(
                            '[ PACKAGE MANAGER - GET_EXECUTION_INFO] {0} komutları içim herhangi bir sonuç bulunamadı'.format(
                                commands))
                else:
                    for command in commands.split():
                        lastcomm_command = 'lastcomm --command {0} '.format(command)
                        if user:
                            lastcomm_command += " --user {}".format(user)
                        lastcomm_command += " --strict-match"
                        self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] {0} command will be executed'.format(lastcomm_command))
                        result_code, result, error = self.execute(lastcomm_command)
                        self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] {0} command is executed'.format(lastcomm_command))
                        result_list = self.check_output(result_code, result)
                        if result_list is None:
                            return
                        elif len(result_list) > 0:
                            self.logger.debug(
                                '[ PACKAGE MANAGER - GET_EXECUTION_INFO] lastcomm result list will be added to main result list')
                            self.command_execution_statistic_list.extend(result_list)
                            self.logger.debug(
                                '[ PACKAGE MANAGER - GET_EXECUTION_INFO] lastcomm result list is added to main result list ')
                        elif len(result_list) == 0:
                            self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] {0} komutu içim herhangi bir sonuç bulunamadı'.format(command))
            elif user:
                lastcomm_command = 'lastcomm --user {0} '.format(user)
                if is_strict_match is True:
                    lastcomm_command += ' --strict-match'
                self.logger.debug(
                    '[ PACKAGE MANAGER - GET_EXECUTION_INFO] {0} command will be executed'.format(lastcomm_command))
                result_code, result, error = self.execute(lastcomm_command)
                self.logger.debug(
                    '[ PACKAGE MANAGER - GET_EXECUTION_INFO] {0} command is executed'.format(lastcomm_command))
                result_list = self.check_output(result_code, result)
                if result_list is None:
                    return
                elif len(result_list) > 0:
                    self.logger.debug(
                        '[ PACKAGE MANAGER - GET_EXECUTION_INFO] lastcomm result list will be added to main result list')
                    self.command_execution_statistic_list.extend(result_list)
                    self.logger.debug(
                        '[ PACKAGE MANAGER - GET_EXECUTION_INFO] lastcomm result list is added to main result list ')
                elif len(result_list) == 0:
                    self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] {0} kullanıcısı içim herhangi bir sonuç bulunamadı'.format(user))
            elif user is None and commands is None:
                result_code, result, error = self.execute('lastcomm')
                result_list = self.check_output(result_code, result)
                if result_list is None:
                    return
                elif len(result_list) > 0:
                    self.logger.debug(
                        '[ PACKAGE MANAGER - GET_EXECUTION_INFO] lastcomm result list will be added to main result list')
                    self.command_execution_statistic_list.extend(result_list)
                    self.logger.debug(
                        '[ PACKAGE MANAGER - GET_EXECUTION_INFO] lastcomm result list is added to main result list ')
                elif len(result_list) == 0:
                    self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] Herhangi bir sonuç bulunamadı')
            result_command_execution_info_list = json.dumps(
                [ob.__dict__ for ob in self.command_execution_statistic_list])
            result_version_list = json.dumps([ob.__dict__ for ob in self.version_list])
            self.logger.debug(
                '[ PACKAGE MANAGER ]' + 'Command Execution Info list: ' + str(result_command_execution_info_list))
            if self.command_execution_statistic_list is not None and len(self.command_execution_statistic_list) > 0:
                res["\"commandExecutionInfoList\""] = result_command_execution_info_list
            if self.version_list is not None and len(self.version_list) > 0:
                res["\"versionList\""] = result_version_list
            if len(self.command_execution_statistic_list) == 0:
                self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Herhangi bir kayıt bulunmamaktadır!')
            else:
                self.execute('echo {0} > {1}'.format(res, self.file_path))
                if self.is_exist(self.file_path):
                    data = {}
                    md5sum = self.get_md5_file(str(self.file_path))
                    self.logger.debug('[PACKAGE MANAGER] {0} renaming to {1}'.format(self.temp_file_name, md5sum))
                    self.rename_file(self.file_path, self.Ahenk.received_dir_path() + '/' + md5sum)
                    self.logger.debug('[PACKAGE MANAGER] Renamed.')
                    data['md5'] = md5sum
                    self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                             message='Uygulama çalıştırma bilgileri başarıyla sisteme geçirildi.',
                                             data=json.dumps(data),
                                             content_type=self.get_content_type().TEXT_PLAIN.value)
                    self.logger.debug("[PACKAGE MANAGER] Execution Info fetched succesfully. ")
                    self.logger.debug("[PACKAGE MANAGER] Execution Info has sent")
                else:
                    raise Exception('File not found on this path: {}'.format(self.file_path))

        except Exception as e:
            self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] Unexpected error in get_execution.py. Error message : {0}'.format(str(e)))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Uygulama çalıştırma bilgilerini getirirken beklenmedik hata!')

    def get_version_list(self, commands):
        for command in commands.split(' '):
            self.check_version_list(command)

    def check_version_list(self, command):
        is_exist = False
        for version in self.version_list:
            if version.commandName == command:
                is_exist = True
        if not is_exist:
            self.logger.debug(
                '[ PACKAGE MANAGER - GET_EXECUTION_INFO] Version searching is started for command : {0}'.format(
                    command))
            result_code, result, p_err = self.execute('whereis {0}'.format(command))
            self.logger.debug('result:{}'.format(result))
            if result_code == 0 and "komut yok" not in result and len(result.split(':')) >= 2:
                result = result.split(':')[1]
                if result.split() is None or len(result.split()) == 0:
                    self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] Command installed place is not found')
                    self.result_message += 'Command {0} could not found'.format(command)
                else:
                    self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] Command installed place is found')
                    result = result.split()[0]
                    result_code, result, p_err = self.execute('dpkg-query -S {0}'.format(result))
                    if result_code == 0:  # Command exists
                        self.logger.debug(
                            '[ PACKAGE MANAGER - GET_EXECUTION_INFO] Command related package name is found')
                        result = result.split(': ')[0]
                        result_code, p_result, p_err = self.execute('dpkg -s {0} | grep Version'.format(result))
                        if result_code == 0:
                            self.logger.debug(
                                '[ PACKAGE MANAGER - GET_EXECUTION_INFO] Command related package version is found')
                            self.version_list.append(VersionInfoItem(command, result, p_result.split(': ')[1]))
                        else:
                            self.logger.debug(
                                '[ PACKAGE MANAGER - GET_EXECUTION_INFO] Command related package version is not found')
                            self.result_message += 'Command\'s related package version could not be parsed(Deb : {0}).'.format(
                                result)
                            self.version_list.append(VersionInfoItem(command, result, '-'))
                    else:  # command not exists
                        self.logger.debug(
                            '[ PACKAGE MANAGER - GET_EXECUTION_INFO] Command related package name is not found')
                        self.result_message += 'Command\'s related package could not be found(Command : {0})'.format(
                            result)
                        self.version_list.append(VersionInfoItem(command, '-', '-'))
            else:  # command not exists
                self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] Command installed place is not found')
                self.result_message += 'Command {0} could not found'.format(command)

            self.logger.debug(
                '[ PACKAGE MANAGER - GET_EXECUTION_INFO] Version searching is finished for command : {}'.format(
                    command))


    def check_output(self, result_code, result):
        try:
            list = []
            if result_code == 0:
                self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] lastcomm execution has returned with no error')
                for line in result.splitlines():
                    self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] line parsing has done')
                    output_columns = line.split()
                    self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] Column parsing has done')
                    command_name = output_columns[0]
                    user = output_columns[len(output_columns) - 8]
                    process_time = output_columns[len(output_columns) - 6]
                    start_date = output_columns[len(output_columns) - 4] + ' ' + output_columns[
                        len(output_columns) - 3] + ' ' + output_columns[len(output_columns) - 2] + ' ' + output_columns[
                                     len(output_columns) - 1]
                    self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] CommandExecutionInfoItem attributes are ready for adding to result list')
                    list.append(CommandExecutionInfoItem(command_name, user, process_time, start_date))
                    self.check_version_list(command_name)
                    self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] CommandExecutionInfoItem is created and added to result list')
                return list
            else:
                self.logger.debug('[ PACKAGE MANAGER - GET_EXECUTION_INFO] lastcomm command has not return with a result')
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

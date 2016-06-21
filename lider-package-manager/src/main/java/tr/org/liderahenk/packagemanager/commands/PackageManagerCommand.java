package tr.org.liderahenk.packagemanager.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.lider.core.api.log.IOperationLogService;
import tr.org.liderahenk.lider.core.api.persistence.IPluginDbService;
import tr.org.liderahenk.lider.core.api.plugin.ICommand;
import tr.org.liderahenk.lider.core.api.plugin.IPluginInfo;
import tr.org.liderahenk.lider.core.api.rest.requests.ITaskRequest;
import tr.org.liderahenk.lider.core.api.service.ICommandContext;
import tr.org.liderahenk.lider.core.api.service.ICommandResult;
import tr.org.liderahenk.lider.core.api.service.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.service.enums.CommandResultStatus;

public class PackageManagerCommand implements ICommand {

	private Logger logger = LoggerFactory.getLogger(PackageManagerCommand.class);

	private ICommandResultFactory resultFactory;
	private IPluginInfo pluginInfo;
	private IOperationLogService logService;
	private IPluginDbService pluginDbService;

	@Override
	public ICommandResult execute(ICommandContext context) {

		ITaskRequest req = context.getRequest();
		Map<String, Object> parameterMap = req.getParameterMap();
		parameterMap.put("dummy-param", "dummy-param-value");

		logger.debug("Parameter map updated.");

		// Object entity = new Object();
		// pluginDbService.save(entity);
		logger.debug("Entity saved successfully.");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("dummy-param", "dummy-param-value");

		ICommandResult commandResult = resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this,
				resultMap);

		return commandResult;
	}

	@Override
	public ICommandResult validate(ICommandContext context) {
		return resultFactory.create(CommandResultStatus.OK, null, this, null);
	}

	@Override
	public String getCommandId() {
		return "PACKAGE_MANAGER";
	}

	@Override
	public Boolean executeOnAgent() {
		return true;
	}

	@Override
	public String getPluginName() {
		return pluginInfo.getPluginName();
	}

	@Override
	public String getPluginVersion() {
		return pluginInfo.getPluginVersion();
	}

	public void setResultFactory(ICommandResultFactory resultFactory) {
		this.resultFactory = resultFactory;
	}

	public void setPluginInfo(IPluginInfo pluginInfo) {
		this.pluginInfo = pluginInfo;
	}

	public void setLogService(IOperationLogService logService) {
		this.logService = logService;
	}

	public void setPluginDbService(IPluginDbService pluginDbService) {
		this.pluginDbService = pluginDbService;
	}

}

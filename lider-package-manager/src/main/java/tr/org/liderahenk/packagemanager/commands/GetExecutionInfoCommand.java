package tr.org.liderahenk.packagemanager.commands;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.lider.core.api.persistence.IPluginDbService;
import tr.org.liderahenk.lider.core.api.persistence.dao.IAgentDao;
import tr.org.liderahenk.lider.core.api.persistence.dao.ITaskDao;
import tr.org.liderahenk.lider.core.api.persistence.entities.ICommandExecutionResult;
import tr.org.liderahenk.lider.core.api.plugin.ICommand;
import tr.org.liderahenk.lider.core.api.plugin.IPluginInfo;
import tr.org.liderahenk.lider.core.api.plugin.ITaskAwareCommand;
import tr.org.liderahenk.lider.core.api.service.ICommandContext;
import tr.org.liderahenk.lider.core.api.service.ICommandResult;
import tr.org.liderahenk.lider.core.api.service.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.service.enums.CommandResultStatus;
import tr.org.liderahenk.packagemanager.entities.CommandExecutionStatistics;
import tr.org.liderahenk.packagemanager.entities.CommandPackageVersion;

public class GetExecutionInfoCommand implements ICommand, ITaskAwareCommand {

	private Logger logger = LoggerFactory.getLogger(GetExecutionInfoCommand.class);
	private ICommandResultFactory resultFactory;
	private IPluginInfo pluginInfo;
	private IPluginDbService pluginDbService;
	private IAgentDao agentDao;
	private ITaskDao taskDao;
	private EntityManager entityManager;

	@Override
	public ICommandResult execute(ICommandContext context) {

		return resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this);
	}

	@Override
	public ICommandResult validate(ICommandContext context) {
		return resultFactory.create(CommandResultStatus.OK, null, this, null);
	}

	@Override
	public String getCommandId() {
		return "GET_EXECUTION_INFO";
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

	public IPluginDbService getPluginDbService() {
		return pluginDbService;
	}

	public void setPluginDbService(IPluginDbService pluginDbService) {
		this.pluginDbService = pluginDbService;
	}

	public IAgentDao getAgentDao() {
		return agentDao;
	}

	public void setAgentDao(IAgentDao agentDao) {
		this.agentDao = agentDao;
	}

	public ITaskDao getTaskDao() {
		return taskDao;
	}

	public void setTaskDao(ITaskDao taskDao) {
		this.taskDao = taskDao;
	}

	@Override
	public void onTaskUpdate(ICommandExecutionResult result) {
		logger.info("HELLOOO");
		byte[] data = result.getResponseData();
		Long agentId = result.getAgentId();
		Long taskId = result.getCommandExecution().getCommand().getTask().getId();
		Long command_execution_id = result.getCommandExecution().getId();
		try {
			final Map<String, Object> responseData = new ObjectMapper().readValue(data, 0, data.length,
					new TypeReference<HashMap<String, Object>>() {
					});
			if (responseData != null && !responseData.isEmpty() && responseData.containsKey("commandExecutionInfoList")
					&& responseData.containsKey("versionList")) {
				final List<HashMap<String, Object>> value = new ObjectMapper().readValue(
						responseData.get("commandExecutionInfoList").toString(),
						new TypeReference<List<HashMap<String, Object>>>() {
						});
				final List<HashMap<String, Object>> versionInfoList = new ObjectMapper().readValue(
						responseData.get("versionList").toString(), new TypeReference<List<HashMap<String, Object>>>() {
						});
				for (HashMap<String, Object> map : versionInfoList) {
					CommandPackageVersion verInfo = new CommandPackageVersion();
					verInfo.setAgentId(agentId);
					verInfo.setTaskId(taskId);
					verInfo.setCreateDate(new Date());
					verInfo.setCommand(map.get("commandName").toString());
					verInfo.setPackageName(map.get("packageName").toString());
					verInfo.setPackageVersion(map.get("packageVersion").toString());

					pluginDbService.save(verInfo);
				}
				for (HashMap<String, Object> map : value) {
					try {

						CommandExecutionStatistics item = new CommandExecutionStatistics();
						item.setCommand(map.get("commandName").toString());
						item.setUser(map.get("user").toString());
						Float processTime =Float.parseFloat(map.get("processTime").toString());
						logger.info(processTime.toString());
						item.setProcessTime(processTime);
						DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
						String currentYearString = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
						item.setProcessStartDate((Date)formatter.parse(map.get("startDate").toString() + ":00 " + currentYearString));

						item.setAgentId(agentId);
						item.setTaskId(taskId);
						item.setIsActive("1");
						item.setCreateDate(new Date());
						item.setCommandExecutionId(command_execution_id);

						
						Query query = entityManager.createQuery(
								"UPDATE CommandExecutionStatistics ces SET ces.isActive ='0' WHERE ces.agentId = :agentId AND ces.command = :command AND ces.user = :user AND ces.taskId <> :taskId");
						query.setParameter("agentId", item.getAgentId());
						query.setParameter("taskId", item.getTaskId());
						query.setParameter("command", item.getCommand());
						query.setParameter("user", item.getUser());
						query.executeUpdate();
						pluginDbService.save(item);

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

}

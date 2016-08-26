package tr.org.liderahenk.packagemanager.model;

import java.io.Serializable;

public class CommandExecutionInfoItem{

	private static final long serialVersionUID = -7315132498427095052L;

	private String agentId;
	private String command;
	private String user;
	private String createDate;
	private Float processTime;
	private String startDate;

	public CommandExecutionInfoItem() {
		super();
	}

	public CommandExecutionInfoItem(String agentId, String command, String user, String createDate, Float processTime,
			String startDate) {
		super();
		this.agentId = agentId;
		this.command = command;
		this.user = user;
		this.createDate = createDate;
		this.processTime = processTime;
		this.startDate = startDate;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public Float getProcessTime() {
		return processTime;
	}

	public void setProcessTime(Float processTime) {
		this.processTime = processTime;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

}

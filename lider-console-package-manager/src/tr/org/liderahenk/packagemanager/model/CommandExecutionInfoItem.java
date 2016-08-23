package tr.org.liderahenk.packagemanager.model;

import java.io.Serializable;

public class CommandExecutionInfoItem implements Serializable {

	private static final long serialVersionUID = -7315132498427095052L;

	private String commandName;
	private String user;
	private Double processTime;
	private String startDate;

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public Double getProcessTime() {
		return processTime;
	}

	public void setProcessTime(Double processTime) {
		this.processTime = processTime;
	}

	@Override
	public String toString() {
		return "CommandExecutionInfoItem [commandName=" + commandName + ", user=" + user + ", processTime="
				+ getProcessTime() + ", startDate=" + startDate + "]";
	}

}

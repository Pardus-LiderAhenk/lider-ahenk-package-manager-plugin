package tr.org.liderahenk.packagemanager.dialogs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;
import tr.org.liderahenk.packagemanager.constants.PackageManagerConstants;
import tr.org.liderahenk.packagemanager.i18n.Messages;
import tr.org.liderahenk.packagemanager.model.CommandExecutionInfoItem;
import tr.org.liderahenk.packagemanager.model.PackageInfo;

public class GetExecutionInfoTaskDialog extends DefaultTaskDialog {

	private TableViewer tableViewer;
	private Label lblCommand;
	private Text txtCommand;
	private Label lblUser;
	private Text txtUser;
	private Button btnIsStrictMatch;

	private static final Logger logger = LoggerFactory.getLogger(GetExecutionInfoTaskDialog.class);

	public GetExecutionInfoTaskDialog(Shell parentShell, Set<String> dnSet) {
		super(parentShell, dnSet);
		subscribeEventHandler(eventHandler);
	}

	private EventHandler eventHandler = new EventHandler() {
		@Override
		public void handleEvent(final Event event) {
			Job job = new Job("TASK") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("PACKAGE_SOURCES", 100);
					try {
						final TaskStatusNotification taskStatus = (TaskStatusNotification) event
								.getProperty("org.eclipse.e4.data");
						Display.getDefault().asyncExec(new Runnable() {
							@SuppressWarnings({ "unchecked", "rawtypes" })
							@Override
							public void run() {
								try {
									// Agent DN
									final String dn = taskStatus.getCommandExecution().getDn();
									final byte[] data = TaskRestUtils.getResponseData(taskStatus.getResult().getId());

									final Map<String, Object> responseData = new ObjectMapper().readValue(data, 0,
											data.length, new TypeReference<HashMap<String, Object>>() {
									});
									if (responseData != null && !responseData.isEmpty()
											&& responseData.containsKey("commandExecutionInfoList")
											&& responseData.containsKey("versionList")) {
										Object object = responseData.get("commandExecutionInfoList");
										ArrayList<Object> list = (ArrayList<Object>) object;
										Object versionObject = responseData.get("versionList");
										ArrayList<Object> versionList = (ArrayList<Object>) versionObject;
										ArrayList<PackageInfo> commandPackageInfo = new ArrayList<>();
										for (Object oldMap : versionList) {
											Map<String, String> map = (Map) oldMap;
											PackageInfo packageInfo = new PackageInfo();
											packageInfo.setPackageName(map.get("packageName"));
											packageInfo.setVersion(map.get("packageVersion"));
											packageInfo.setTag(map.get("commandName"));
											commandPackageInfo.add(packageInfo);
										}
										ArrayList<CommandExecutionInfoItem> items = new ArrayList<>();
										for (Object oldMap : list) {
											Map<String, String> map = (Map) oldMap;
											CommandExecutionInfoItem item = new CommandExecutionInfoItem();
											item.setCommand(map.get("commandName").toString());
											item.setUser(map.get("user").toString());
											Float processTime = Float.parseFloat(map.get("processTime").toString());
											item.setProcessTime(processTime);
											String currentYearString = Integer
													.toString(Calendar.getInstance().get(Calendar.YEAR));
											item.setStartDate(map.get("startDate").toString() + ":00 " + currentYearString);
											item.setAgentId(dn);
											item.setProcessCount(1);
											organizeResultList(items, item, commandPackageInfo);
										}
										ArrayList<CommandExecutionInfoItem> listItems = (ArrayList<CommandExecutionInfoItem>) tableViewer
												.getInput();
										if (listItems == null) {
											listItems = new ArrayList<>();
										}
										listItems.addAll(items);
										tableViewer.setInput(listItems);
										tableViewer.refresh();
									}

								} catch (Exception e1) {
									e1.printStackTrace();
								}
							}
						});

					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						Notifier.error("", Messages.getString("UNEXPECTED_ERROR_TAKING_SCREENSHOT"));
					}

					monitor.worked(100);
					monitor.done();

					return Status.OK_STATUS;
				}
			};

			job.setUser(true);
			job.schedule();
		}
	};

	private void organizeResultList(ArrayList<CommandExecutionInfoItem> items, CommandExecutionInfoItem item,
			ArrayList<PackageInfo> commandPackageInfo) {
		boolean isExist = false;
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getAgentId().equals(item.getAgentId())
					&& items.get(i).getCommand().equals(item.getCommand())
					&& items.get(i).getUser().equals(item.getUser())) {
				items.get(i).setProcessTime(items.get(i).getProcessTime() + item.getProcessTime());
				items.get(i).setProcessCount(items.get(i).getProcessCount() + 1);
				setPackageInfo(items.get(i), commandPackageInfo);
				isExist = true;
			}
		}
		if (!isExist) {
			setPackageInfo(item, commandPackageInfo);
			item.setLastExecutionDate(item.getStartDate());
			items.add(item);
		}
	}

	private void setPackageInfo(CommandExecutionInfoItem item, ArrayList<PackageInfo> commandPackageInfo) {
		for (PackageInfo packageInfo : commandPackageInfo) {
			if (packageInfo.getTag().equals(item.getCommand())) {
				item.setPackageName(packageInfo.getPackageName());
				item.setPackageversion(packageInfo.getVersion());
			}
		}
	}

	@Override
	public String createTitle() {
		return Messages.getString("GetExecutionInfo");
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridData gData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gData.widthHint = 1650;
		composite.setLayoutData(gData);

		Composite infoComposite = new Composite(composite, SWT.NONE);
		infoComposite.setLayout(new GridLayout(2, false));
		infoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		lblUser = new Label(infoComposite, SWT.NONE);
		lblUser.setText(Messages.getString("USER"));

		txtUser = new Text(infoComposite, SWT.BORDER);
		txtUser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		lblCommand = new Label(infoComposite, SWT.NONE);
		lblCommand.setText(Messages.getString("COMMAND"));

		txtCommand = new Text(infoComposite, SWT.BORDER);
		txtCommand.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		btnIsStrictMatch = new Button(composite, SWT.CHECK);
		btnIsStrictMatch.setText(Messages.getString("IS_STRICT_MATCH"));

		Label commandUsageDescription = new Label(composite, SWT.NONE);
		commandUsageDescription.setText(Messages.getString("COMMAND_USAGE_DESCRIPTION"));

		createTableArea(composite);

		return null;
	}

	@Override
	public void validateBeforeExecution() throws ValidationException {
		if ((txtCommand == null || txtCommand.getText() == null || txtCommand.getText().isEmpty())
				&& (txtUser == null || txtUser.getText() == null || txtUser.getText().isEmpty())) {
			throw new ValidationException(Messages.getString("PLEASE_ENTER_AT_LEAST_COMMAND_OR_USER"));
		}
		if (btnIsStrictMatch.getSelection()
				&& ((txtCommand == null || txtCommand.getText() == null || txtCommand.getText().isEmpty())
						|| (txtUser == null || txtUser.getText() == null || txtUser.getText().isEmpty()))) {
			throw new ValidationException(Messages.getString("STRICT_MATCH_USAGE_WARNING"));
		}
	}

	@Override
	public Map<String, Object> getParameterMap() {
		Map<String, Object> taskData = new HashMap<String, Object>();
		taskData.put(PackageManagerConstants.CHECK_INFO_PARAMETERS.COMMAND,
				(txtCommand.getText() == null || txtCommand.getText().isEmpty()) ? null : txtCommand.getText());
		taskData.put(PackageManagerConstants.CHECK_INFO_PARAMETERS.USER,
				(txtUser.getText() == null || txtUser.getText().isEmpty()) ? null : txtUser.getText());
		taskData.put(PackageManagerConstants.CHECK_INFO_PARAMETERS.IS_STRICT_MATCH, btnIsStrictMatch.getSelection());
		tableViewer.setInput(null);
		tableViewer.refresh();
		return taskData;
	}

	@Override
	public String getCommandId() {
		return "GET_EXECUTION_INFO";
	}

	@Override
	public String getPluginName() {
		return PackageManagerConstants.PLUGIN_NAME;
	}

	@Override
	public String getPluginVersion() {
		return PackageManagerConstants.PLUGIN_VERSION;
	}

	private void createTableArea(Composite parent) {

		tableViewer = SWTResourceManager.createTableViewer(parent);
		createTableColumns();

		// Hook up listeners
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
			}
		});

		tableViewer.refresh();
	}

	private void createTableColumns() {

		// Status
		TableViewerColumn agentIdColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("DN"), 600);
		agentIdColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecutionInfoItem) {
					return ((CommandExecutionInfoItem) element).getAgentId() != null
							? ((CommandExecutionInfoItem) element).getAgentId() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Status
		TableViewerColumn commandColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("COMMAND"), 120);
		commandColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecutionInfoItem) {
					return ((CommandExecutionInfoItem) element).getCommand() != null
							? ((CommandExecutionInfoItem) element).getCommand() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Status
		TableViewerColumn userColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("USER"), 120);
		userColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecutionInfoItem) {
					return ((CommandExecutionInfoItem) element).getUser() != null
							? ((CommandExecutionInfoItem) element).getUser() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Status
		TableViewerColumn processTimeColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("TOTAL_PROCESS_TIME"), 160);
		processTimeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecutionInfoItem) {
					return ((CommandExecutionInfoItem) element).getProcessTime() != null
							? ((CommandExecutionInfoItem) element).getProcessTime().toString()
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Status
		TableViewerColumn processStartDateColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("PROCESS_COUNT"), 120);
		processStartDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecutionInfoItem) {
					return ((CommandExecutionInfoItem) element).getProcessCount() != null
							? ((CommandExecutionInfoItem) element).getProcessCount().toString()
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Status
		TableViewerColumn packageNameColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("PACKAGE_NAME"), 120);
		packageNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecutionInfoItem) {
					return ((CommandExecutionInfoItem) element).getPackageName() != null
							? ((CommandExecutionInfoItem) element).getPackageName() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Status
		TableViewerColumn packageVersionColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("PACKAGE_VERSION"), 120);
		packageVersionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecutionInfoItem) {
					return ((CommandExecutionInfoItem) element).getPackageversion() != null
							? ((CommandExecutionInfoItem) element).getPackageversion() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Status
		TableViewerColumn lastExecutionTimeColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("LAST_EXECUTION_TIME"), 200);
		lastExecutionTimeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecutionInfoItem) {
					return ((CommandExecutionInfoItem) element).getLastExecutionDate() != null
							? ((CommandExecutionInfoItem) element).getLastExecutionDate() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

	}

}

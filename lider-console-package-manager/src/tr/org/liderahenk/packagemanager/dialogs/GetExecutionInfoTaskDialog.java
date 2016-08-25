package tr.org.liderahenk.packagemanager.dialogs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;
import tr.org.liderahenk.packagemanager.constants.PackageManagerConstants;
import tr.org.liderahenk.packagemanager.i18n.Messages;

public class GetExecutionInfoTaskDialog extends DefaultTaskDialog {

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
						TaskStatusNotification taskStatus = (TaskStatusNotification) event
								.getProperty("org.eclipse.e4.data");
						byte[] data = taskStatus.getResult().getResponseData();
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								
							}
						});
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						Notifier.error("", Messages.getString("UNEXPECTED_ERROR_ACCESSING_EXECUTING_INFO"));
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

	@Override
	public String createTitle() {
		return Messages.getString("GetExecutionInfo");
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


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
		
		return null;
	}

	@Override
	public void validateBeforeExecution() throws ValidationException {
		if ((txtCommand == null || txtCommand.getText() == null || txtCommand.getText().isEmpty()) && (txtUser == null || txtUser.getText() == null || txtUser.getText().isEmpty()) ) {
			throw new ValidationException(Messages.getString("PLEASE_ENTER_AT_LEAST_COMMAND_OR_USER"));
		}
		if (btnIsStrictMatch.getSelection() && ((txtCommand == null || txtCommand.getText() == null || txtCommand.getText().isEmpty()) || (txtUser == null || txtUser.getText() == null || txtUser.getText().isEmpty()))){
			throw new ValidationException(Messages.getString("STRICT_MATCH_USAGE_WARNING"));
		}
	}

	@Override
	public Map<String, Object> getParameterMap() {
		Map<String, Object> taskData = new HashMap<String, Object>();
		taskData.put(PackageManagerConstants.CHECK_INFO_PARAMETERS.COMMAND, (txtCommand.getText() == null || txtCommand.getText().isEmpty()) ? null : txtCommand.getText());
		taskData.put(PackageManagerConstants.CHECK_INFO_PARAMETERS.USER, (txtUser.getText() == null || txtUser.getText().isEmpty()) ? null : txtUser.getText());
		taskData.put(PackageManagerConstants.CHECK_INFO_PARAMETERS.IS_STRICT_MATCH, btnIsStrictMatch.getSelection());
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

}

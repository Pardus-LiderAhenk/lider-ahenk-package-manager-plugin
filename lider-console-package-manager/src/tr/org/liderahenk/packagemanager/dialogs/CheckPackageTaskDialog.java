package tr.org.liderahenk.packagemanager.dialogs;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
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
import tr.org.liderahenk.packagemanager.model.PackageInfo;

public class CheckPackageTaskDialog extends DefaultTaskDialog {

	private ScrolledComposite sc;
	private CheckboxTableViewer viewer;
	String upperCase = "";
	private Composite packageComposite;
	private Label lblPackageName;
	private Text txtPackageName;
	private Label lblVersion;
	private Text txtVersion;
	private Text txtPackageInfo;

	private PackageInfo item;

	private IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);

	private static final Logger logger = LoggerFactory.getLogger(PackageSourcesTaskDialog.class);

	// TODO do not forget to change this constructor if SingleSelectionHandler
	// is used!
	public CheckPackageTaskDialog(Shell parentShell, Set<String> dnSet) {
		super(parentShell, dnSet);
		upperCase = getPluginName().toUpperCase(Locale.ENGLISH);
		eventBroker.subscribe(getPluginName().toUpperCase(Locale.ENGLISH), eventHandler);
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
						final Map<String, Object> responseData = new ObjectMapper().readValue(data, 0, data.length,
								new TypeReference<HashMap<String, Object>>() {
						});
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								if(responseData.containsKey("Result")){
									txtPackageInfo.setText(responseData.get("Result").toString());
									txtPackageInfo.pack();
								}
							}
						});
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						 Notifier.error("",Messages.getString("UNEXPECTED_ERROR_ACCESSING_PACKAGE_INFO"));
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
		return Messages.getString("CheckPackage");
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		packageComposite = new Composite(composite, SWT.NONE);
		packageComposite.setLayout(new GridLayout(2, false));
		packageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		lblPackageName = new Label(packageComposite, SWT.BOLD);
		lblPackageName.setText(Messages.getString("PACKAGE_NAME"));

		txtPackageName = new Text(packageComposite, SWT.BORDER);
		txtPackageName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		lblVersion = new Label(packageComposite, SWT.BOLD);
		lblVersion.setText(Messages.getString("VERSION"));

		txtVersion = new Text(packageComposite, SWT.BORDER);
		txtVersion.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		txtPackageInfo = new Text(composite, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
		txtPackageInfo.setLayoutData(new GridData( GridData.FILL_HORIZONTAL ));
		txtPackageInfo.setBounds(txtPackageInfo.getBounds().x+100, txtPackageInfo.getBounds().y, 500, txtPackageInfo.getBounds().height);
		txtPackageInfo.setEnabled(false);

		return null;
	}

	@Override
	public void validateBeforeExecution() throws ValidationException {
		if(txtPackageName == null || txtPackageName.getText() == null || txtPackageName.getText().isEmpty()){
			throw new ValidationException(Messages.getString("PLEASE_ENTER_AT_LEAST_PACKAGE_NAME"));
		}
	}

	@Override
	public Map<String, Object> getParameterMap() {
		Map<String, Object> taskData = new HashMap<String, Object>();
		taskData.put(PackageManagerConstants.PACKAGE_PARAMETERS.PACKAGE_NAME, txtPackageName.getText());
		taskData.put(PackageManagerConstants.PACKAGE_PARAMETERS.PACKAGE_VERSION, txtVersion.getText());
		return taskData;
	}

	@Override
	public String getCommandId() {
		return "CHECK_PACKAGE";
	}

	@Override
	public String getPluginName() {
		return PackageManagerConstants.PLUGIN_NAME;
	}

	@Override
	public String getPluginVersion() {
		return PackageManagerConstants.PLUGIN_VERSION;
	}

	public PackageInfo getItem() {
		return item;
	}

	public void setItem(PackageInfo item) {
		this.item = item;
	}

}

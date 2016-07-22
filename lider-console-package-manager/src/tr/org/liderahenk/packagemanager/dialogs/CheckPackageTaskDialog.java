package tr.org.liderahenk.packagemanager.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;
import tr.org.liderahenk.packagemanager.constants.PackageManagerConstants;
import tr.org.liderahenk.packagemanager.i18n.Messages;
import tr.org.liderahenk.packagemanager.model.PackageCheckItem;

public class CheckPackageTaskDialog extends DefaultTaskDialog {

	private ScrolledComposite sc;
	private CheckboxTableViewer viewer;
	private Composite packageComposite;
	private Label lblPackageName;
	private Text txtPackageName;
	private Label lblVersion;
	private Text txtVersion;

	private static final Logger logger = LoggerFactory.getLogger(CheckPackageTaskDialog.class);

	public CheckPackageTaskDialog(Shell parentShell, Set<String> dnSet) {
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
						final Map<String, Object> responseData = new ObjectMapper().readValue(data, 0, data.length,
								new TypeReference<HashMap<String, Object>>() {
						});
						Display.getDefault().asyncExec(new Runnable() {

							@SuppressWarnings("unchecked")
							@Override
							public void run() {
								if (responseData != null && responseData.containsKey("res")
										&& responseData.containsKey("uid")) {
									PackageCheckItem item = new PackageCheckItem(responseData.get("res").toString(),
											responseData.get("uid").toString());
									ArrayList<PackageCheckItem> listItems = (ArrayList<PackageCheckItem>) viewer
											.getInput();
									if (listItems == null) {
										listItems = new ArrayList<>();
									}
									listItems.add(item);
									viewer.setInput(listItems);
									redraw();
								}
							}
						});
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						Notifier.error("", Messages.getString("UNEXPECTED_ERROR_ACCESSING_PACKAGE_INFO"));
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

		sc = new ScrolledComposite(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gData = new GridData(SWT.FILL, SWT.FILL, true, true);
		sc.setLayoutData(gData);
		gData.widthHint = 1000;
		sc.setLayout(new GridLayout(1, false));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		final Composite composite = new Composite(sc, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		sc.setContent(composite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		composite.setBounds(composite.getBounds().x, composite.getBounds().y, 1000, composite.getBounds().height);

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

		viewer = SWTResourceManager.createCheckboxTableViewer(composite);

		return null;
	}

	protected void handleRemoveGroupButton(SelectionEvent e) {
		Button thisBtn = (Button) e.getSource();
		Composite parent = thisBtn.getParent();
		Control[] children = parent.getChildren();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				if (children[i].equals(thisBtn) && i - 1 > 0) {
					children[i - 1].dispose();
					children[i].dispose();
					redraw();
					break;
				}
			}
		}
	}

	private void redraw() {
		sc.layout(true, true);
		sc.setMinSize(sc.getContent().computeSize(800, SWT.DEFAULT));
	}

	private void createTableColumns() {

		String[] titles = { Messages.getString("PACKAGE_INFO"), Messages.getString("UID") };

		final TableViewerColumn selectAllColumn = SWTResourceManager.createTableViewerColumn(viewer, "", 30);
		selectAllColumn.getColumn().setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/check-cancel.png"));
		selectAllColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}
		});
		selectAllColumn.getColumn().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				/* If all list selected deselect all */
				if (viewer.getCheckedElements().length == viewer.getTable().getItemCount()) {
					viewer.setAllChecked(false);
					selectAllColumn.getColumn().setImage(SWTResourceManager
							.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/check-cancel.png"));

					redraw();
				} else {
					viewer.setAllChecked(true);
					selectAllColumn.getColumn().setImage(SWTResourceManager
							.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/check-done.png"));

					redraw();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		TableViewerColumn packageInfoColumn = SWTResourceManager.createTableViewerColumn(viewer, titles[0], 600);
		packageInfoColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PackageCheckItem) {
					return ((PackageCheckItem) element).getResult();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn uidColumn = SWTResourceManager.createTableViewerColumn(viewer, titles[1], 300);
		uidColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PackageCheckItem) {
					return ((PackageCheckItem) element).getUid();
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	private void disposeTableColumns() {
		Table table = viewer.getTable();
		while (table.getColumnCount() > 0) {
			table.getColumns()[0].dispose();
		}
	}

	private void emptyTable() {
		recreateTable();
		viewer.setInput(new ArrayList<PackageCheckItem>());
		redraw();
	}

	private void recreateTable() {

		viewer.getTable().setRedraw(false);
		viewer.getTable().setHeaderVisible(true);

		disposeTableColumns();
		createTableColumns();
		viewer.getTable().setRedraw(true);
	}

	@Override
	public void validateBeforeExecution() throws ValidationException {
		if (txtPackageName == null || txtPackageName.getText() == null || txtPackageName.getText().isEmpty()) {
			throw new ValidationException(Messages.getString("PLEASE_ENTER_AT_LEAST_PACKAGE_NAME"));
		}
	}

	@Override
	public Map<String, Object> getParameterMap() {
		Map<String, Object> taskData = new HashMap<String, Object>();
		taskData.put(PackageManagerConstants.PACKAGE_PARAMETERS.PACKAGE_NAME, txtPackageName.getText());
		taskData.put(PackageManagerConstants.PACKAGE_PARAMETERS.PACKAGE_VERSION, txtVersion.getText());
		emptyTable();
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

}

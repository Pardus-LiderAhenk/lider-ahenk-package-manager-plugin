package tr.org.liderahenk.packagemanager.dialogs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.rest.requests.TaskRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;
import tr.org.liderahenk.packagemanager.constants.PackageManagerConstants;
import tr.org.liderahenk.packagemanager.i18n.Messages;
import tr.org.liderahenk.packagemanager.model.PackageInfo;

/**
 * Task execution dialog for package-manager plugin.
 * 
 */
public class PackageManagementTaskDialog extends DefaultTaskDialog {

	private ScrolledComposite sc;
	String upperCase = "";
	private Button btnAdd;
	private Button btnDelete;
	private Button btnEdit;
	private Button btnCheckInstall;
	private Button btnCheckUnInstall;
	private CheckboxTableViewer viewer;

	private PackageInfo item;

	private IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);

	private static final Logger logger = LoggerFactory.getLogger(PackageManagementTaskDialog.class);

	// TODO do not forget to change this constructor if SingleSelectionHandler
	// is used!
	public PackageManagementTaskDialog(Shell parentShell, Set<String> dnSet) {
		super(parentShell, dnSet);
		upperCase = getPluginName().toUpperCase(Locale.ENGLISH);
		eventBroker.subscribe(getPluginName().toUpperCase(Locale.ENGLISH), eventHandler);
		getData(dnSet);
	}

	private void getData(Set<String> dnSet) {

		try {
			TaskRequest task = new TaskRequest(new ArrayList<String>(dnSet), DNType.AHENK, getPluginName(),
					getPluginVersion(), "INSTALLED_PACKAGES", null, null, new Date());
			TaskRestUtils.execute(task);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
		}
	}

	private EventHandler eventHandler = new EventHandler() {
		@Override
		public void handleEvent(final Event event) {
			Job job = new Job("TASK") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("PACKAGE_MANAGEMENT", 100);
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
								ArrayList<PackageInfo> items = new ArrayList<>();
								PackageInfo i1 = new PackageInfo();
								i1.setPackageName("ant");
								i1.setVersion("1.9.3-2build1");
								PackageInfo i2 = new PackageInfo();
								i2.setPackageName("skype");
								i2.setVersion("4.3.0.37-0ubuntu0.12.04.1");
								items.add(i1);
								items.add(i2);
								if (items != null){
									recreateTable();
									viewer.setInput(items);
									redraw();
								}else{
									emptyTable();
								}
							}
						});
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						Notifier.error("", Messages.getString("UNEXPECTED_ERROR_ACCESSING_PACKAGES"));
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
		return Messages.getString("PackageManager");
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {

		sc = new ScrolledComposite(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sc.setLayout(new GridLayout(1, false));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		Composite composite = new Composite(sc, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		sc.setContent(composite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		
		createButtons(composite);
		Composite installationComposite = new Composite(composite, SWT.NONE);
		installationComposite.setLayout(new GridLayout(2, false));
		
		btnCheckInstall = new Button(installationComposite, SWT.CHECK);
		btnCheckInstall.setText(Messages.getString("INSTALL"));
		btnCheckInstall.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnCheckUnInstall.setSelection(false);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		btnCheckInstall.setSelection(true);

		btnCheckUnInstall = new Button(installationComposite, SWT.CHECK);
		btnCheckUnInstall.setText(Messages.getString("UNINSTALL"));
		btnCheckUnInstall.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnCheckInstall.setSelection(false);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
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
		sc.setMinSize(sc.getContent().computeSize(600, SWT.DEFAULT));
	}



	private void createTableColumns() {

		String[] titles = { Messages.getString("PACKAGE_NAME"), Messages.getString("PACKAGE_VERSION")};

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

		TableViewerColumn packageNameColumn = SWTResourceManager.createTableViewerColumn(viewer, titles[0], 150);
		packageNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PackageInfo) {
					return ((PackageInfo) element).getPackageName();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn versionColumn = SWTResourceManager.createTableViewerColumn(viewer, titles[1], 150);
		versionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PackageInfo) {
					return ((PackageInfo) element).getVersion();
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
		viewer.setInput(new ArrayList<PackageInfo>());
		redraw();
	}

	private void recreateTable() {

		viewer.getTable().setRedraw(false);
		viewer.getTable().setHeaderVisible(true);

		disposeTableColumns();
		createTableColumns();
		viewer.getTable().setRedraw(true);
	}



	private void createButtons(final Composite parent) {
		final Composite tableButtonComposite = new Composite(parent, SWT.NONE);
		tableButtonComposite.setLayout(new GridLayout(3, false));

		btnAdd = new Button(tableButtonComposite, SWT.NONE);
		btnAdd.setText(Messages.getString("ADD"));
		btnAdd.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnAdd.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/add.png"));
		btnAdd.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PackageManagementItemDialog dialog = new PackageManagementItemDialog(
						Display.getDefault().getActiveShell(), viewer);
				dialog.create();
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnEdit = new Button(tableButtonComposite, SWT.NONE);
		btnEdit.setText(Messages.getString("EDIT"));
		btnEdit.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/edit.png"));
		btnEdit.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnEdit.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null == getItem()) {
					Notifier.warning(null, Messages.getString("PLEASE_SELECT_ITEM"));
					return;
				}
				PackageManagementItemDialog dialog = new PackageManagementItemDialog(tableButtonComposite.getShell(),
						getItem(), viewer);
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnDelete = new Button(tableButtonComposite, SWT.NONE);
		btnDelete.setText(Messages.getString("DELETE"));
		btnDelete.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/delete.png"));
		btnDelete.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnDelete.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null == getItem()) {
					Notifier.warning(null, Messages.getString("PLEASE_SELECT_ITEM"));
					return;
				}
				@SuppressWarnings("unchecked")
				List<PackageManagementItemDialog> items = (List<PackageManagementItemDialog>) viewer.getInput();
				items.remove(viewer.getTable().getSelectionIndex());
				viewer.setInput(items);
				viewer.refresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	@Override
	public void validateBeforeExecution() throws ValidationException {
		if (viewer.getInput() == null || ((List<PackageInfo>) viewer.getInput()).isEmpty() || viewer.getCheckedElements().length == 0) {
			throw new ValidationException(Messages.getString("ADD_ITEM"));
		}
	}

	@Override
	public Map<String, Object> getParameterMap() {
		Map<String, Object> taskData = new HashMap<String, Object>();
		Object[] checkedElements = viewer.getCheckedElements();
		for (Object packageInfo : checkedElements) {
			if(btnCheckInstall.getSelection())
				((PackageInfo)packageInfo).setTag(Messages.getString("INSTALL"));
			else
				((PackageInfo)packageInfo).setTag(Messages.getString("UNINSTALL"));
		}
		taskData.put(PackageManagerConstants.PACKAGES.PACKAGE_INFO_LIST, checkedElements);
		return taskData;
	}

	@Override
	public String getCommandId() {
		return "PACKAGE_MANAGEMENT";
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

package tr.org.liderahenk.packagemanager.dialogs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
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
import tr.org.liderahenk.packagemanager.model.PackageArchiveItem;
import tr.org.liderahenk.packagemanager.model.PackageInfo;

public class PackageArchiveTaskDialog extends DefaultTaskDialog {

	private ScrolledComposite sc;
	private CheckboxTableViewer viewer;
	String upperCase = "";
	private Composite packageComposite;
	private Label lblPackageName;
	private Text txtPackageName;
	private Set<String> dnSet = new HashSet<>();
	private Button btnList;

	private IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);

	private static final Logger logger = LoggerFactory.getLogger(PackageArchiveTaskDialog.class);

	// TODO do not forget to change this constructor if SingleSelectionHandler
	// is used!
	public PackageArchiveTaskDialog(Shell parentShell, String dn) {
		super(parentShell, dn);
		upperCase = getPluginName().toUpperCase(Locale.ENGLISH);
		eventBroker.subscribe(getPluginName().toUpperCase(Locale.ENGLISH), eventHandler);
		dnSet.add(dn);
//		getData(dnSet);
	}
	

	private void getData(Set<String> dnSet) {

		try {
			Map<String, Object> taskData = new HashMap<String, Object>();
			taskData.put(PackageManagerConstants.PACKAGE_PARAMETERS.PACKAGE_NAME, txtPackageName.getText());
			TaskRequest task = new TaskRequest(new ArrayList<String>(dnSet), DNType.AHENK, getPluginName(),
					getPluginVersion(), "SHOW_PACKAGE_ARCHIVE", taskData, null, new Date());
			TaskRestUtils.execute(task);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
		}
	}

@SuppressWarnings("unchecked")
public static <T extends List<?>> T cast(Object obj) {
    return (T) obj;
}
	private EventHandler eventHandler = new EventHandler() {
		@Override
		public void handleEvent(final Event event) {
			Job job = new Job("TASK") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("PACKAGE_ARCHIVE", 100);
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
								if(responseData != null && !responseData.isEmpty() && responseData.containsKey("Result")){
									Object itemms = responseData.get("Result");
									List<PackageArchiveItem> its = new ArrayList<>();
									List<LinkedHashMap<String, String>> list = cast(itemms);
									for (Map<String, String> item : list) {
										PackageArchiveItem it = new PackageArchiveItem(item.get("version"),item.get("installationDate"));
										its.add(it);
									}
									recreateTable();
									viewer.setInput(its);
									redraw();
								}else if(responseData != null && !responseData.isEmpty() && responseData.containsKey("ResultMessage")){
									Notifier.info(Messages.getString("INSTALL_FROM_ARCHIVE_TITLE"), responseData.get("ResultMessage").toString());
								}else{
									emptyTable();
								}
							}
						});
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						 Notifier.error("",Messages.getString("UNEXPECTED_ERROR_ACCESSING_PACKAGE_ARCHIVE"));
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
		return Messages.getString("PackageArchive");
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
		
		packageComposite = new Composite(composite, SWT.NONE);
		packageComposite.setLayout(new GridLayout(2, false));
		packageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		lblPackageName = new Label(packageComposite, SWT.BOLD);
		lblPackageName.setText(Messages.getString("PACKAGE_NAME"));

		txtPackageName = new Text(packageComposite, SWT.BORDER);
		txtPackageName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		new Label(packageComposite, SWT.BOLD);
		
		btnList = new Button(packageComposite, SWT.NONE);
		btnList.setText(Messages.getString("LIST_PACKAGES"));
		GridData btnGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		btnGridData.widthHint = 120;
		btnList.setLayoutData(btnGridData);
		btnList.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(txtPackageName == null || txtPackageName.getText() == null || txtPackageName.getText().isEmpty())
					Notifier.error("Paket Kurulumu Sonucu", Messages.getString("PLEASE_ENTER_AT_LEAST_PACKAGE_NAME"));
				else
					getData(dnSet);
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		viewer = SWTResourceManager.createCheckboxTableViewer(composite);
		
		viewer.getTable().addSelectionListener(new SelectionAdapter() {

			  @Override
			  public void widgetSelected(SelectionEvent e) {
			      int df = viewer.getTable().getSelectionIndex();

			      viewer.setAllChecked(false);
				  viewer.setChecked(viewer.getElementAt(df), !viewer.getChecked(viewer.getElementAt(df)));
			  }         
			});

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
		sc.setMinSize(sc.getContent().computeSize(1000, SWT.DEFAULT));
	}



	private void createTableColumns() {

		String[] titles = { Messages.getString("VERSION"), Messages.getString("INSTALLATION_DATE")};

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

		TableViewerColumn versionColumn = SWTResourceManager.createTableViewerColumn(viewer, titles[0], 500);
		versionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PackageArchiveItem) {
					return ((PackageArchiveItem) element).getVersion();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn installationDateColumn = SWTResourceManager.createTableViewerColumn(viewer, titles[1], 500);
		installationDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PackageArchiveItem) {
					return ((PackageArchiveItem) element).getInstallationDate();
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
		viewer.setInput(new ArrayList<PackageArchiveItem>());
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
	}

	@Override
	public Map<String, Object> getParameterMap() {
		Map<String, Object> taskData = new HashMap<String, Object>();
		Object[] checkedElements = viewer.getCheckedElements();
		for (Object checkedElement : checkedElements) {
			taskData.put(PackageManagerConstants.PACKAGE_PARAMETERS.PACKAGE_NAME, txtPackageName.getText());
			taskData.put(PackageManagerConstants.PACKAGE_PARAMETERS.PACKAGE_VERSION, ((PackageArchiveItem)checkedElement).getVersion());
		}
		
		return taskData;
	}

	@Override
	public String getCommandId() {
		return "PACKAGE_ARCHIVE";
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

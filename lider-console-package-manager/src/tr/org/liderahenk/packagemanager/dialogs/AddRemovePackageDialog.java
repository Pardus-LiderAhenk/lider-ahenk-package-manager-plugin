package tr.org.liderahenk.packagemanager.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;
import tr.org.liderahenk.packagemanager.constants.PackageManagerConstants;
import tr.org.liderahenk.packagemanager.i18n.Messages;
import tr.org.liderahenk.packagemanager.model.PackageInfo;
import tr.org.liderahenk.packagemanager.model.RepoSourcesListParser;

public class AddRemovePackageDialog extends DefaultTaskDialog {

	private ScrolledComposite sc;
	private Combo cmbDeb;
	private Text txtUrl;
	private Text txtComponents;
	private Text txtPackageName;
	private Text txtDescription;
	private Composite packageComposite;
	private Button btnList;
	private Button btnAddRep;
	private CheckboxTableViewer viewer;
	private Button btnCheckInstall;
	private Button btnCheckUnInstall;
	private PackageManagementLoadingDialog loadingDialog;
	private AddRemovePackageLoadingDialog addRemLoadingDialog;
	private Label lblUrl;
	private Label lblComponents;

	private final String[] debArray = new String[] { "deb", "deb-src" };
	private PackageInfo item;

	public AddRemovePackageDialog(Shell parentShell, Set<String> dnSet) {
		super(parentShell, dnSet);
		subscribeEventHandler(eventHandler);
	}

	private EventHandler eventHandler = new EventHandler() {
		@Override
		public void handleEvent(final Event event) {

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					addRemLoadingDialog = new AddRemovePackageLoadingDialog(Display.getDefault().getActiveShell());
					addRemLoadingDialog.open();
				}
			});

			Job job = new Job("TASK") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("PACKAGES", 100);
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
								if (responseData != null && responseData.containsKey("ResultMessage")) {
									if (viewer.getCheckedElements() != null) {
										viewer.setAllChecked(false);
										redraw();
									}
								}
							}
						});
					} catch (Exception e) {
						Notifier.error("", Messages.getString("UNEXPECTED_ERROR_WHILE_INSTALL_UNINSTALL_PACKAGE"));
					}
					monitor.worked(100);
					monitor.done();

					return Status.OK_STATUS;
				}
			};

			job.setUser(true);
			job.schedule();

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					addRemLoadingDialog.close();
				}
			});
		}
	};

	@Override
	public String createTitle() {
		return Messages.getString("AddRemovePackages");
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {

		sc = new ScrolledComposite(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = 1000;
		sc.setLayoutData(gridData);
		sc.setLayout(new GridLayout(1, false));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		Composite composite = new Composite(sc, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		sc.setContent(composite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);


		Composite packageLabelComposite = new Composite(composite, SWT.NONE);
		packageLabelComposite.setLayout(new GridLayout(3, false));
		packageLabelComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label tmpLbl =new Label(packageLabelComposite, SWT.NONE);
		GridData gdUrl = new GridData(SWT.CENTER, SWT.FILL, false, true);
		gdUrl.widthHint = 350;
		tmpLbl.setLayoutData(gdUrl);
		lblUrl = new Label(packageLabelComposite, SWT.NONE);
		lblUrl.setText(Messages.getString("URL"));
		lblUrl.setLayoutData(gdUrl);

		lblComponents = new Label(packageLabelComposite, SWT.NONE);
		lblComponents.setText(Messages.getString("COMPONENTS"));
		 GridData gdComponents = new GridData(SWT.LEFT, SWT.FILL, false,
		 true);
		 gdComponents.widthHint = 300;
		 lblComponents.setLayoutData(gdComponents);


		packageComposite = new Composite(composite, SWT.NONE);
		packageComposite.setLayout(new GridLayout(2, false));
		packageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		createPackageEntry(packageComposite);

		btnAddRep = new Button(packageComposite, SWT.NONE);
		btnAddRep.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/add.png"));
		btnAddRep.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAddGroupButton(e);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		createSearchingPart(composite);

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
		viewer.refresh();
	}

	protected void handleAddGroupButton(SelectionEvent e) {

		Composite parent = (Composite) ((Button) e.getSource()).getParent();

		createPackageEntry(parent);

		Button btnRemoveGroup = new Button(parent, SWT.NONE);
		btnRemoveGroup.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/remove.png"));
		btnRemoveGroup.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRemoveGroupButton(e);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		redraw();
	}

	private void createPackageEntry(Composite parent) {

		Group grpPackageEntry = new Group(parent, SWT.NONE);
		grpPackageEntry.setLayout(new GridLayout(3, false));
		grpPackageEntry.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		cmbDeb = new Combo(grpPackageEntry, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData cmbGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		cmbGridData.widthHint = 50;
		cmbDeb.setLayoutData(cmbGridData);
		for (int i = 0; i < debArray.length; i++) {
			String i18n = Messages.getString(debArray[i]);
			if (i18n != null && !i18n.isEmpty()) {
				cmbDeb.add(i18n);
				cmbDeb.setData(i + "", debArray[i]);
			}
		}
		cmbDeb.select(0);

		txtUrl = new Text(grpPackageEntry, SWT.BORDER);
		GridData txtGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		txtUrl.setMessage(Messages.getString("URL_EXAMPLE"));
		txtGridData.widthHint = 400;
		txtUrl.setLayoutData(txtGridData);

		txtComponents = new Text(grpPackageEntry, SWT.BORDER);
		GridData componentsGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		txtComponents.setMessage(Messages.getString("COMPONENT_EXAMPLE"));
		componentsGridData.widthHint = 300;
		txtComponents.setLayoutData(componentsGridData);
	}

	private void createTableColumns() {

		String[] titles = { Messages.getString("PACKAGE_NAME"), Messages.getString("VERSION"),
				Messages.getString("SIZE"), Messages.getString("DESCRIPTION") };

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
		packageNameColumn.getColumn().setAlignment(SWT.LEFT);
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
		versionColumn.getColumn().setAlignment(SWT.LEFT);
		versionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PackageInfo) {
					return ((PackageInfo) element).getVersion();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn sizeColumn = SWTResourceManager.createTableViewerColumn(viewer, titles[2], 150);
		sizeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PackageInfo) {
					return ((PackageInfo) element).getSize();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn descriptionColumn = SWTResourceManager.createTableViewerColumn(viewer, titles[3], 150);
		descriptionColumn.getColumn().setAlignment(SWT.LEFT);
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PackageInfo) {
					return ((PackageInfo) element).getDescription();
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	private void createSearchingPart(final Composite parent) {
		Composite tableButtonComposite = new Composite(parent, SWT.NONE);
		tableButtonComposite.setLayout(new GridLayout(3, false));

		txtPackageName = new Text(tableButtonComposite, SWT.BORDER);
		GridData txtGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		txtGridData.widthHint = 200;
		txtPackageName.setLayoutData(txtGridData);
		txtPackageName.setMessage(Messages.getString("PACKAGE_NAME"));

		txtDescription = new Text(tableButtonComposite, SWT.BORDER);
		txtDescription.setLayoutData(txtGridData);
		txtDescription.setMessage(Messages.getString("DESCRIPTION"));

		Composite packageSearchComposite = new Composite(tableButtonComposite, SWT.RIGHT);
		packageSearchComposite.setLayout(new GridLayout(1, false));

		btnList = new Button(packageSearchComposite, SWT.NONE);
		btnList.setText(Messages.getString("LIST_PACKAGES"));
		GridData btnGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		btnGridData.widthHint = 120;
		btnList.setLayoutData(btnGridData);
		btnList.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						loadingDialog = new PackageManagementLoadingDialog(Display.getDefault().getActiveShell());
						loadingDialog.open();
					}
				});

				String[] list = list();
				List<PackageInfo> resultSet = new ArrayList<PackageInfo>();
				for (int i = 0; i < list.length; i = i + 3) {
					List<PackageInfo> items = RepoSourcesListParser.parseURL(list[i + 1], list[i + 2].split(" ")[0],
							Arrays.copyOfRange(list[i + 2].split(" "), 1, list[i + 2].split(" ").length), "amd64",
							list[i]);
					if(items != null && !items.isEmpty())
						resultSet.addAll(items);
					else{
						Notifier.error("", "Depo ayrıştırılırken hata ile karşılaşıldı.Depo alanının doğruluğundan emin olunuz");
					}
					if (checkSearchingCriteria(resultSet) != null && checkSearchingCriteria(resultSet).size() > 0) {
						recreateTable();
						viewer.setInput(checkSearchingCriteria(resultSet));
						redraw();
					} else {
						emptyTable();
					}
				}

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						loadingDialog.close();
					}
				});
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private List<PackageInfo> checkSearchingCriteria(List<PackageInfo> resultSet) {
		List<PackageInfo> result = new ArrayList<>();
		boolean isProper;
		if (resultSet != null && resultSet.size() > 0) {
			for (PackageInfo packageInfo : resultSet) {
				isProper = false;
				if (txtPackageName.getText().isEmpty() && txtDescription.getText().isEmpty())
					isProper = true;
				if (txtPackageName.getText() != null && !txtPackageName.getText().isEmpty()
						&& packageInfo.getPackageName() != null && !packageInfo.getPackageName().isEmpty()
						&& packageInfo.getPackageName().contains(txtPackageName.getText()))
					isProper = true;
				if (txtDescription.getText() != null && !txtDescription.getText().isEmpty()
						&& packageInfo.getDescription() != null && !packageInfo.getDescription().isEmpty()
						&& packageInfo.getDescription().contains(txtDescription.getText()))
					isProper = true;
				if (isProper)
					result.add(packageInfo);
			}
		}
		return result;
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

	private String[] list() {

		ArrayList<String> returningAttributes = new ArrayList<String>();
		// Always add objectClass to returning attributes, to determine if an
		// entry belongs to a user or agent

		Control[] children = packageComposite.getChildren();
		if (children != null) {
			for (Control child : children) {
				if (child instanceof Group) {
					Control[] gChildren = ((Group) child).getChildren();
					if (gChildren != null && gChildren.length == 3) {
						returningAttributes.add(((Combo) gChildren[0]).getText());
						returningAttributes.add(((Text) gChildren[1]).getText());
						returningAttributes.add(((Text) gChildren[2]).getText());
					}
				}
			}
		}

		return returningAttributes.toArray(new String[] {});
	}

	@Override
	public void validateBeforeExecution() throws ValidationException {
		if (viewer.getCheckedElements().length == 0) {
			throw new ValidationException(Messages.getString("PLEASE_SELECT_AT_LEAST_AN_ITEM"));
		}
	}

	@Override
	public Map<String, Object> getParameterMap() {
		Map<String, Object> taskData = new HashMap<String, Object>();
		Object[] checkedElements = viewer.getCheckedElements();
		for (Object packageInfo : checkedElements) {
			if (btnCheckInstall.getSelection())
				((PackageInfo) packageInfo).setTag(Messages.getString("INSTALL"));
			else
				((PackageInfo) packageInfo).setTag(Messages.getString("UNINSTALL"));
		}
		taskData.put(PackageManagerConstants.PACKAGES.PACKAGE_INFO_LIST, checkedElements);
		return taskData;
	}

	@Override
	public String getCommandId() {
		return "PACKAGES";
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

	public Label getLblUrl() {
		return lblUrl;
	}

	public void setLblUrl(Label lblUrl) {
		this.lblUrl = lblUrl;
	}

	public Label getLblComponents() {
		return lblComponents;
	}

	public void setLblComponents(Label lblComponents) {
		this.lblComponents = lblComponents;
	}

}

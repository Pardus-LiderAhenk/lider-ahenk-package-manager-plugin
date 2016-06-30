package tr.org.liderahenk.packagemanager.dialogs;

import java.util.ArrayList;
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
import org.eclipse.jface.viewers.ArrayContentProvider;
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
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;
import tr.org.liderahenk.packagemanager.constants.PackageManagerConstants;
import tr.org.liderahenk.packagemanager.i18n.Messages;
import tr.org.liderahenk.packagemanager.model.PackageInfo;
import tr.org.liderahenk.packagemanager.model.PackageSourceItem;
import tr.org.liderahenk.packagemanager.model.RepoSourcesListParser;

public class AddRemovePackageDialog extends DefaultTaskDialog {

	private ScrolledComposite sc;
	String upperCase = "";
	private Combo cmbDeb;
	private Text txtUrl;
	private Text txtComponents;
	private Text txtPackageName;
	private Text txtDescription;
	private Composite tableComposite;
	private Composite packageComposite;
	private TableViewer tableViewer;
	private Button btnList;
	private Button btnDelete;
	private Button btnAddRep;

	private final String[] debArray = new String[] { "deb", "deb-src" };
	private PackageSourceItem item;

	private IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);

	private static final Logger logger = LoggerFactory.getLogger(PackageSourcesTaskDialog.class);

	// TODO do not forget to change this constructor if SingleSelectionHandler
	// is used!
	public AddRemovePackageDialog(Shell parentShell, Set<String> dnSet) {
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
							}
						});
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						// Notifier.error("",
						// Messages.getString("UNEXPECTED_ERROR_ACCESSING_PACKAGE_SOURCES"));
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
		return Messages.getString("AddRemovePackages");
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
		createTableArea(composite);

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

		txtUrl = new Text(grpPackageEntry, SWT.BORDER);
		GridData txtGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		txtUrl.setLayoutData(txtGridData);

		txtComponents = new Text(grpPackageEntry, SWT.BORDER);
		GridData componentsGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		txtComponents.setLayoutData(componentsGridData);
	}

	private void createTableArea(Composite parent) {
		tableComposite = new Composite(parent, SWT.BORDER);
		tableComposite.setLayout(new GridLayout(1, false));
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, true);
		gridData.widthHint = 600;
		tableComposite.setLayoutData(gridData);
		createTable(tableComposite);
	}

	private void createTable(final Composite parent) {
		tableViewer = new TableViewer(parent,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		// Create table columns
		createTableColumns();

		// Configure table layout
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.getVerticalBar().setEnabled(true);
		table.getVerticalBar().setVisible(true);
		tableViewer.setContentProvider(new ArrayContentProvider());

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 1;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = 250;
		gridData.horizontalAlignment = GridData.FILL;
		tableViewer.getControl().setLayoutData(gridData);

		// Hook up listeners
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				Object firstElement = selection.getFirstElement();
				firstElement = (PackageSourceItem) firstElement;
				if (firstElement instanceof PackageSourceItem) {
					setItem((PackageSourceItem) firstElement);
				}
				btnDelete.setEnabled(true);
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				PackageSourceItemDialog dialog = new PackageSourceItemDialog(parent.getShell(), getItem(), tableViewer);
				dialog.open();
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(false);
		column.setAlignment(SWT.LEFT);
		return viewerColumn;
	}

	private void createTableColumns() {

		String[] titles = { Messages.getString("PACKAGE_NAME"), Messages.getString("VERSION"),
				Messages.getString("SIZE"), Messages.getString("DESCRIPTION") };
		int[] bounds = { 150, 150 };

		TableViewerColumn packageNameColumn = createTableViewerColumn(titles[0], bounds[0]);
		packageNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PackageSourceItem) {
					return ((PackageSourceItem) element).getUrl();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn versionColumn = createTableViewerColumn(titles[1], bounds[0]);
		versionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PackageSourceItem) {
					return ((PackageSourceItem) element).getUrl();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn sizeColumn = createTableViewerColumn(titles[2], bounds[0]);
		sizeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PackageSourceItem) {
					return ((PackageSourceItem) element).getUrl();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn descriptionColumn = createTableViewerColumn(titles[3], bounds[0]);
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PackageSourceItem) {
					return ((PackageSourceItem) element).getUrl();
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

		txtDescription = new Text(tableButtonComposite, SWT.BORDER);
		txtDescription.setLayoutData(txtGridData);

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
				String[] list = list();
				for(int i =0 ; i<list.length ; i=i+3){
					System.out.println(list[i] + "   " + list[i+1]  + "   " + list[i+2]);
					Set<PackageInfo> parseURL = RepoSourcesListParser.parseURL(list[i], list[i+1], list[i+2].split(" "), "amd64");
					System.out.println(parseURL.size());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
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
						returningAttributes.add(((Combo)gChildren[0]).getText());
						returningAttributes.add(((Text)gChildren[1]).getText());
						returningAttributes.add(((Text)gChildren[2]).getText());
					}
				}
			}
		}

		return returningAttributes.toArray(new String[] {});
	}

	@Override
	public void validateBeforeExecution() throws ValidationException {
	}

	@Override
	public Map<String, Object> getParameterMap() {
		Map<String, Object> taskData = new HashMap<String, Object>();
		return taskData;
	}

	@Override
	public String getCommandId() {
		return "ADD_REMOVE_PACKAGES";
	}

	@Override
	public String getPluginName() {
		return PackageManagerConstants.PLUGIN_NAME;
	}

	@Override
	public String getPluginVersion() {
		return PackageManagerConstants.PLUGIN_VERSION;
	}

	public PackageSourceItem getItem() {
		return item;
	}

	public void setItem(PackageSourceItem item) {
		this.item = item;
	}

}

package tr.org.liderahenk.packagemanager.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import tr.org.liderahenk.liderconsole.core.dialogs.DefaultLiderTitleAreaDialog;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.packagemanager.i18n.Messages;
import tr.org.liderahenk.packagemanager.model.PackageManagementItem;
import tr.org.liderahenk.packagemanager.model.PackageSourceItem;

public class PackageManagementItemDialog extends DefaultLiderTitleAreaDialog {

	// Model
	private PackageManagementItem item;
	// Table
	private TableViewer tableViewer;
	// Widgets
	private Label lblPackageName;
	private Text txtPackageName;
	private Label lblPackageVersion;
	private Text txtPackageVersion;

	public PackageManagementItemDialog(Shell parentShell, TableViewer tableViewer) {
		super(parentShell);
		this.tableViewer = tableViewer;
	}

	public PackageManagementItemDialog(Shell parentShell, PackageManagementItem item, TableViewer tableViewer) {
		super(parentShell);
		this.item = item;
		this.tableViewer = tableViewer;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.getString("PACKAGE_MANAGEMENT_ITEM"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(1, false));
		GridData gData = new GridData(SWT.FILL, SWT.FILL, false, true);
		gData.widthHint = 520;
		composite.setLayoutData(gData);

		Composite mailComposite = new Composite(composite, SWT.NONE);
		mailComposite.setLayout(new GridLayout(2, false));

		lblPackageName = new Label(mailComposite, SWT.NONE);
		lblPackageName.setText(Messages.getString("PACKAGE_NAME"));

		txtPackageName = new Text(mailComposite, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.widthHint=400;
		txtPackageName.setLayoutData(gridData);
		
		if (item != null && item.getPackageName() != null) {
			txtPackageName.setText(item.getPackageName());
		}


		lblPackageVersion = new Label(mailComposite, SWT.NONE);
		lblPackageVersion.setText(Messages.getString("PACKAGE_VERSION"));

		txtPackageVersion = new Text(mailComposite, SWT.BORDER);
		gridData.widthHint=400;
		txtPackageVersion.setLayoutData(gridData);
		
		if (item != null && item.getPackageVersion() != null) {
			txtPackageVersion.setText(item.getPackageVersion());
		}

		return composite;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {

		setReturnCode(OK);

		if (txtPackageName.getText().isEmpty() || txtPackageVersion.getText().isEmpty()) {
			Notifier.error(null, Messages.getString("FILL_ALL_FIELDS"));
			return;
		}

		boolean editMode = true;
		if (item == null) {
			item = new PackageManagementItem();
			editMode = false;
		}
		// Set values
		item.setPackageName(txtPackageName.getText());
		item.setPackageVersion(txtPackageVersion.getText());

		// Get previous items...
		List<PackageManagementItem> items = (List<PackageManagementItem>) tableViewer.getInput();
		if (items == null) {
			items = new ArrayList<PackageManagementItem>();
		}

		if (editMode) {
			int index = tableViewer.getTable().getSelectionIndex();
			if (index > -1) {
				// Override previous item!
				items.set(index, item);
			}
		} else {
			// New item!
			items.add(item);
		}

		tableViewer.setInput(items);
		tableViewer.refresh();

		close();
	}

}

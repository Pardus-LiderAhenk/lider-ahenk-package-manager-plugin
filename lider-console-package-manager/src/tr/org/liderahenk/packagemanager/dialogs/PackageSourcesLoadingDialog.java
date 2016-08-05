package tr.org.liderahenk.packagemanager.dialogs;

import tr.org.liderahenk.packagemanager.i18n.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class PackageSourcesLoadingDialog extends Dialog {

	private Label message;
	private ProgressBar progBar;

	public PackageSourcesLoadingDialog(Shell parentShell) {
		super(parentShell);

		// Do not show close on the title bar and lock parent window.
		super.setShellStyle(SWT.TITLE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createContents(Composite parent) {

		// Disable ESC key in this dialog
		getShell().addListener(SWT.Traverse, new Listener() {
			@Override
			public void handleEvent(Event e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					e.doit = false;
				}
			}
		});

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label image = new Label(composite, SWT.NONE);
		image.setImage(new Image(Display.getCurrent(), this.getClass().getResourceAsStream("/icons/64/wait.png")));

		message = new Label(composite, SWT.WRAP);
		message.setText(Messages.getString("REPOSITORY_LOADING_MESSAGE"));
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		message.setLayoutData(gridData);

		progBar = new ProgressBar(composite, SWT.INDETERMINATE);
		GridData barGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		progBar.setLayoutData(barGridData);

		return composite;
	}
}

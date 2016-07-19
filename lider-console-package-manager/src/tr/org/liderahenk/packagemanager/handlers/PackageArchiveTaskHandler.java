package tr.org.liderahenk.packagemanager.handlers;

import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.handlers.SingleSelectionHandler;
import tr.org.liderahenk.packagemanager.dialogs.PackageArchiveTaskDialog;

public class PackageArchiveTaskHandler extends SingleSelectionHandler {

	private Logger logger = LoggerFactory.getLogger(PackageManagerTaskHandler.class);
	
	@Override
	public void executeWithDn(String dn) {
		PackageArchiveTaskDialog dialog = new PackageArchiveTaskDialog(Display.getDefault().getActiveShell(), dn);
		dialog.create();
		dialog.open();
	}

}

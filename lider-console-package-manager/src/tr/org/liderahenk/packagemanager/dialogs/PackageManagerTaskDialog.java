package tr.org.liderahenk.packagemanager.dialogs;

import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;

/**
 * Task execution dialog for package-manager plugin.
 * 
 */
public class PackageManagerTaskDialog extends DefaultTaskDialog {

	private static final Logger logger = LoggerFactory.getLogger(PackageManagerTaskDialog.class);

	// TODO do not forget to change this constructor if SingleSelectionHandler
	// is used!
	public PackageManagerTaskDialog(Shell parentShell, Set<String> dnSet) {
		super(parentShell, dnSet);
	}

	@Override
	public String createTitle() {
		// TODO dialog title
		return null;
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {
		// TODO create your task-related widgets here
		return null;
	}

	@Override
	public boolean validateBeforeExecution() {
		// TODO triggered before task execution
		return false;
	}

	@Override
	public Map<String, Object> getParameterMap() {
		// TODO custom parameter map
		return null;
	}

	@Override
	public String getCommandId() {
		// TODO command id which is used to match tasks with ICommand class in
		// the corresponding Lider plugin
		return null;
	}

	@Override
	public String getPluginName() {
		// TODO plugin name
		return null;
	}

	@Override
	public String getPluginVersion() {
		// TODO plugin version
		return null;
	}

}

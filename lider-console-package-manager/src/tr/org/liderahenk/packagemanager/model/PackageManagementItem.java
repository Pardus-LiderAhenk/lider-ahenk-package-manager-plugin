package tr.org.liderahenk.packagemanager.model;

import java.io.Serializable;

public class PackageManagementItem implements Serializable {

	private static final long serialVersionUID = 5340275124534084734L;

	private String packageName;
	
	private String packageVersion;
	
	public PackageManagementItem() {
		super();
	}

	public PackageManagementItem(String packageName, String packageVersion) {
		super();
		this.packageName = packageName;
		this.setPackageVersion(packageVersion);
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageVersion() {
		return packageVersion;
	}

	public void setPackageVersion(String packageVersion) {
		this.packageVersion = packageVersion;
	}
}

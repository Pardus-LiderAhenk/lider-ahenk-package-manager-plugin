package tr.org.liderahenk.packagemanager.model;

import java.io.Serializable;

public class PackageArchiveItem implements Serializable {

	private static final long serialVersionUID = 8410790905721952374L;
	private String version;
	private String installationDate;

	public PackageArchiveItem(String version, String installationDate) {
		super();
		this.version = version;
		this.installationDate = installationDate;
	}

	public PackageArchiveItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getInstallationDate() {
		return installationDate;
	}

	public void setInstallationDate(String installationDate) {
		this.installationDate = installationDate;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}

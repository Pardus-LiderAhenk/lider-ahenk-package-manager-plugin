package tr.org.liderahenk.packagemanager.model;

import java.io.Serializable;

public class PackageCheckItem implements Serializable{

	private static final long serialVersionUID = 5605455949309964919L;
	
	private String dn;
	private String result;

	public PackageCheckItem() {
		super();
	}

	public PackageCheckItem(String result, String uid) {
		super();
		this.result = result;
		this.dn = uid;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String uid) {
		this.dn = uid;
	}
	

}

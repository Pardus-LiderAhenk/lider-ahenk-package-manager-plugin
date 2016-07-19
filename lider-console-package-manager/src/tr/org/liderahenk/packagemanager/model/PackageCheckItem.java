package tr.org.liderahenk.packagemanager.model;

import java.io.Serializable;

public class PackageCheckItem implements Serializable{

	private static final long serialVersionUID = 5605455949309964919L;
	
	private String uid;
	private String result;

	public PackageCheckItem() {
		super();
	}

	public PackageCheckItem(String result, String uid) {
		super();
		this.result = result;
		this.uid = uid;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	

}

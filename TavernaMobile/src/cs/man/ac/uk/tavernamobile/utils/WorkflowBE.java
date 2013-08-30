package cs.man.ac.uk.tavernamobile.utils;

import java.util.List;

import android.graphics.Bitmap;

// Workflow Business Entity
public class WorkflowBE {
	
	private String Title;
	private String Version;
	private String Filename;
	private String UploaderName;
	private Bitmap Avator;
	private String Workflow_URI;
	private String RunID;
	private List<String> privileges;

	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public String getFilename() {
		return Filename;
	}
	public void setFilename(String filename) {
		Filename = filename;
	}
	public String getUploaderName() {
		return UploaderName;
	}
	public void setUploaderName(String uploaderName) {
		UploaderName = uploaderName;
	}
	public Bitmap getAvator() {
		return Avator;
	}
	public void setAvator(Bitmap avator) {
		Avator = avator;
	}
	public String getVersion() {
		return Version;
	}
	public void setVersion(String version) {
		this.Version = version;
	}
	public String getRunID() {
		return RunID;
	}
	public String setRunID(String runid) {
		return this.RunID = runid;
	}
	public String getWorkflow_URI() {
		return Workflow_URI;
	}
	public void setWorkflow_URI(String workflow_URI) {
		Workflow_URI = workflow_URI;
	}
	public List<String> getPrivileges() {
		return privileges;
	}
	public void setPrivileges(List<String> privileges) {
		this.privileges = privileges;
	}
}

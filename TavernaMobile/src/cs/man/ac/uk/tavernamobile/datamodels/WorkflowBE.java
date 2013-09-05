package cs.man.ac.uk.tavernamobile.datamodels;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

import android.graphics.Bitmap;

// Workflow Business Entity
public class WorkflowBE implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -638985876257294052L;
	private String Title;
	private String Version;
	private String Filename;
	private String UploaderName;
	private Bitmap Avator;
	private String Workflow_URI;
	private String RunID;
	private List<String> privileges;
	private String FirstLaunched;
	private String LastLaunched;

	private static ByteBuffer dst;
	private static byte[] bytesar;
	
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
	public String getFirstLaunched() {
		return FirstLaunched;
	}
	public void setFirstLaunched(String firstLaunched) {
		FirstLaunched = firstLaunched;
	}
	public String getLastLaunched() {
		return LastLaunched;
	}
	public void setLastLaunched(String lastLaunched) {
		LastLaunched = lastLaunched;
	}
	
	/*public Video (long newVideoId) {
	    this.videoId=newVideoId;
	}*/
	private void writeObject(ObjectOutputStream out) throws IOException{

	    out.writeObject(Title);
	    out.writeObject(Version);
	    out.writeObject(Filename);
	    out.writeObject(UploaderName);
	    out.writeObject(Workflow_URI);
	    out.writeObject(RunID);
	    out.writeObject(privileges);
	    out.writeObject(FirstLaunched);
	    out.writeObject(LastLaunched);
	    
	    out.writeInt(Avator.getRowBytes());
	    out.writeInt(Avator.getHeight());
	    out.writeInt(Avator.getWidth());

	    int bmSize = Avator.getRowBytes() * Avator.getHeight();
	    if(dst==null || bmSize > dst.capacity())
	        dst= ByteBuffer.allocate(bmSize);

	    out.writeInt(dst.capacity());

	    dst.position(0);

	    Avator.copyPixelsToBuffer(dst);
	    if(bytesar==null || bmSize > bytesar.length)
	        bytesar=new byte[bmSize];

	    dst.position(0);
	    dst.get(bytesar);
	    out.write(bytesar, 0, bytesar.length);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{

	    Title = (String) in.readObject();
	    Version = (String) in.readObject();
	    Filename = (String) in.readObject();
	    UploaderName = (String) in.readObject();
	    Workflow_URI = (String) in.readObject();
	    RunID = (String) in.readObject();
	    privileges = (List<String>) in.readObject();
	    FirstLaunched = (String) in.readObject();
	    LastLaunched = (String) in.readObject();

	    in.readInt();
	    int height=in.readInt();
	    int width=in.readInt();
	    int bmSize=in.readInt();
	    if(bytesar==null || bmSize > bytesar.length)
	        bytesar= new byte[bmSize];

	    int offset=0;

	    while(in.available()>0){
	        offset=offset + in.read(bytesar, offset, in.available());
	    }

	    if(dst==null || bmSize > dst.capacity())
	        dst= ByteBuffer.allocate(bmSize);
	    dst.position(0);
	    dst.put(bytesar);
	    dst.position(0);
	    Avator=Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	    Avator.copyPixelsFromBuffer(dst);
	    //in.close();
	}
}

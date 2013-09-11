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
	private static final long serialVersionUID = 5304169958030841817L;
	private String workflow_title;
	private String version;
	private String filePath;
	private String uploaderName;
	private Bitmap avatar;
	private String workflow_URI;
	private List<String> privileges;
	private String firstLaunched;
	private String lastLaunched;
	private List<String> savedInputsFilesPath;

	private static ByteBuffer dst;
	private static byte[] bytesar;
	
	public String getTitle() {
		return workflow_title;
	}
	public void setTitle(String title) {
		workflow_title = title;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String path) {
		filePath = path;
	}
	public String getUploaderName() {
		return uploaderName;
	}
	public void setUploaderName(String name) {
		uploaderName = name;
	}
	public Bitmap getAvatar() {
		return avatar;
	}
	public void setAvatar(Bitmap a) {
		avatar = a;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getWorkflow_URI() {
		return workflow_URI;
	}
	public void setWorkflow_URI(String uri) {
		workflow_URI = uri;
	}
	public List<String> getPrivileges() {
		return privileges;
	}
	public void setPrivileges(List<String> privileges) {
		this.privileges = privileges;
	}
	public String getFirstLaunched() {
		return firstLaunched;
	}
	public void setFirstLaunched(String first) {
		firstLaunched = first;
	}
	public String getLastLaunched() {
		return lastLaunched;
	}
	public void setLastLaunched(String last) {
		lastLaunched = last;
	}
	public List<String> getSavedInputsFilesPath() {
		return savedInputsFilesPath;
	}
	public void setSavedInputsFilesPath(List<String> savedInputsFilePath) {
		this.savedInputsFilesPath = savedInputsFilePath;
	}
	/*public Video (long newVideoId) {
	    this.videoId=newVideoId;
	}*/
	private void writeObject(ObjectOutputStream out) throws IOException{

	    out.writeObject(workflow_title);
	    out.writeObject(version);
	    out.writeObject(filePath);
	    out.writeObject(uploaderName);
	    out.writeObject(workflow_URI);
	    out.writeObject(privileges);
	    out.writeObject(firstLaunched);
	    out.writeObject(lastLaunched);
	    out.writeObject(savedInputsFilesPath);
	    
	    if(avatar != null){
	    	out.writeInt(avatar.getRowBytes());
		    out.writeInt(avatar.getHeight());
		    out.writeInt(avatar.getWidth());

		    int bmSize = avatar.getRowBytes() * avatar.getHeight();
		    if(dst==null || bmSize > dst.capacity()){
		        dst= ByteBuffer.allocate(bmSize);
		    }
		    out.writeInt(dst.capacity());
		    dst.position(0);

		    avatar.copyPixelsToBuffer(dst);
		    if(bytesar==null || bmSize > bytesar.length)
		        bytesar=new byte[bmSize];

		    dst.position(0);
		    dst.get(bytesar);
		    out.write(bytesar, 0, bytesar.length);
	    }   
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{

	    workflow_title = (String) in.readObject();
	    version = (String) in.readObject();
	    filePath = (String) in.readObject();
	    uploaderName = (String) in.readObject();
	    workflow_URI = (String) in.readObject();
	    privileges = (List<String>) in.readObject();
	    firstLaunched = (String) in.readObject();
	    lastLaunched = (String) in.readObject();
	    savedInputsFilesPath = (List<String>) in.readObject();
	    
	    if(avatar != null){
	    in.readInt();
	    int height=in.readInt();
	    int width=in.readInt();
	    int bmSize=in.readInt();
	    //if(height > 1 && width > 1 && bmSize > 1){
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
		    avatar=Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		    avatar.copyPixelsFromBuffer(dst);
		    //in.close();
	    //}
	    }
	}
}

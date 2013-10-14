package cs.man.ac.uk.tavernamobile.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.apache.http.client.CookieStore;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

import uk.org.taverna.server.client.Run;
import uk.org.taverna.server.client.Server;
import uk.org.taverna.server.client.connection.HttpBasicCredentials;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.datamodels.User;
import cs.man.ac.uk.tavernamobile.datamodels.Workflow;

/*@ReportsCrashes(formKey = "dFg0OHU2alo1VDZpYUFud3JwMzFsMnc6MQ", mode = ReportingInteractionMode.DIALOG,
// displayed as soon as the crash occurs, before collecting data which can take
// a few seconds
resToastText = R.string.crash_toast_text, resDialogText = R.string.crash_dialog_text,
// default is a warning sign
resDialogIcon = android.R.drawable.ic_dialog_info,
// default is your application name
resDialogTitle = R.string.crash_dialog_title,
// when defined, adds a user text field input with this text resource as a label
resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
// displays a Toast message when the user accepts to send a report.
resDialogOkToast = R.string.crash_dialog_ok_toast)*/
public class TavernaAndroid extends Application {

	/**** for testing purpose ****/
	// private static final String TavernaServerAddress =
	// "http://leela.cs.man.ac.uk:8080/taverna242";
	private static final String TavernaServerAddress = 
			"https://eric.rcs.manchester.ac.uk:8443/taverna-server-2";
	/**** for testing purpose ****/

	private Server server;
	private uk.org.taverna.server.client.connection.UserCredentials defaultUser;
	// Run and output that going through this app every time
	private Run workflowRunLaunched;
	private HashMap<String, String> outputs = new HashMap<String, String>();

	// physically cache workflowDetail data
	// private Workflow workflowCache;
	// private License license;
	// private User uploader;
	
	// image cache
	private static LruCache<String, Bitmap> mImageCache;
	// textual data cache
	private static HashMap<String, Object> mCache;

	private static User myEUserLoggedin;
	private static ArrayList<Workflow> myWorkflows = new ArrayList<Workflow>();
	private static ArrayList<Workflow> favouriteWorkflows = new ArrayList<Workflow>();
	private static CookieStore myExperimentSessionCookies;
	
	private static int notificationId;
	private static DropboxAPI<AndroidAuthSession> mApi;

	/**** for testing purpose ****/
	public TavernaAndroid() {
		try {
			URI serverURI = new URI(TavernaServerAddress);
			server = new Server(serverURI);
			defaultUser = new HttpBasicCredentials("taverna", "taverna");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**** for testing purpose ****/

	@Override
	public void onCreate() {
		//ACRA.init(this);
		super.onCreate();

		// Get max available VM memory,
		// since exceeding this amount will throw an OutOfMemory exception.
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 8;

		mImageCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes
				return bitmap.getByteCount() / 1024;
			}
		};

		mCache = new HashMap<String, Object>();
	}

	public Server getServer() {
		return server;
	}

	public HashMap<String, String> getOutputs() {
		return outputs;
	}

	public void setOutputs(HashMap<String, String> outputs) {
		this.outputs = outputs;
	}

	public uk.org.taverna.server.client.connection.UserCredentials getDefaultUser() {
		return defaultUser;
	}

	public synchronized Run getWorkflowRunLaunched() {
		return workflowRunLaunched;
	}

	public void setWorkflowRunLaunched(Run workflowRunLaunched) {
		this.workflowRunLaunched = workflowRunLaunched;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public void setDefaultUser(
			uk.org.taverna.server.client.connection.UserCredentials defaultUser) {
		this.defaultUser = defaultUser;
	}

	/*public Workflow getWorkflowCache() {
	 return workflowCache;
	 }
	
	 public void setWorkflowCache(Workflow workflowCache) {
	 this.workflowCache = workflowCache;
	 }
	
	 public License getLicense() {
	 return license;
	 }
	
	 public void setLicense(License license) {
	 this.license = license;
	 }
	
	 public User getUploader() {
	 return uploader;
	 }
	
	 public void setUploader(User uploader) {
	 this.uploader = uploader;
	 }*/

	public static DropboxAPI<AndroidAuthSession> getmApi() {
		return mApi;
	}
	public static void setmApi(DropboxAPI<AndroidAuthSession> mApi) {
		TavernaAndroid.mApi = mApi;
	}
	public static int getNotificationId() {
		return notificationId;
	}
	public static void setNotificationId(int id) {
		notificationId = id;
	}
	public static ArrayList<Workflow> getMyWorkflows() {
		return myWorkflows;
	}
	public static void setMyWorkflows(ArrayList<Workflow> myWorkflows) {
		TavernaAndroid.myWorkflows = myWorkflows;
	}
	public static ArrayList<Workflow> getFavouriteWorkflows() {
		return favouriteWorkflows;
	}
	public static void setFavouriteWorkflows(ArrayList<Workflow> favouriteWorkflows) {
		TavernaAndroid.favouriteWorkflows = favouriteWorkflows;
	}
	public static CookieStore getMyExperimentSessionCookies() {
		return myExperimentSessionCookies;
	}

	public static void setMyExperimentSessionCookies(
			CookieStore myExperimentSessionCookies) {
		TavernaAndroid.myExperimentSessionCookies = myExperimentSessionCookies;
	}

	public static User getMyEUserLoggedin() {
		return myEUserLoggedin;
	}

	public static void setMyEUserLoggedin(User myEUserLoggedin) {
		TavernaAndroid.myEUserLoggedin = myEUserLoggedin;
	}

	public static LruCache<String, Bitmap> getmMemoryCache() {
		return mImageCache;
	}

	public static void setmMemoryCache(LruCache<String, Bitmap> mMemoryCache) {
		TavernaAndroid.mImageCache = mMemoryCache;
	}

	public static HashMap<String, Object> getmTextCache() {
		return mCache;
	}

	public static void setmTextCache(HashMap<String, Object> mTextCache) {
		TavernaAndroid.mCache = mTextCache;
	}

	public boolean HasCookies(Context context) {
		if (myExperimentSessionCookies != null) {
			return true;
		}

		return false;
	}
}

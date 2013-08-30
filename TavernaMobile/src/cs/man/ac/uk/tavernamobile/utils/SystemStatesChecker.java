package cs.man.ac.uk.tavernamobile.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.widget.Toast;

public class SystemStatesChecker {
	
	private Context appContext;
	
	public SystemStatesChecker(Context context){
		appContext = context;
	}
	
	public boolean IsPhoneStorageReady() {
		boolean ready = true;
		String message = null;
		String extStoState = Environment.getExternalStorageState();
		if (extStoState.equals(Environment.MEDIA_BAD_REMOVAL)) {
			message = "Phone External Storage was removed before it was unmounted.\n"
					+ "Please check the external storage";
			ready = false;
		} else if (extStoState.equals(Environment.MEDIA_CHECKING)) {
			message = "Phone External Storage being disk-checked.\n"
					+ "Please check the external storage";
			ready = false;
		} else if (extStoState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			message = "Phone External Storage is read only.\n"
					+ "Please check the external storage";
			ready = false;
		} else if (extStoState.equals(Environment.MEDIA_NOFS)) {
			message = "Phone External Storage is blank or is using an unsupported filesystem,\n"
					+ "Please check the external storage";
			ready = false;
		} else if (extStoState.equals(Environment.MEDIA_REMOVED)) {
			message = "No External Storage found.\n"
					+ "Please check the external storage";
			ready = false;
		} else if (extStoState.equals(Environment.MEDIA_SHARED)) {
			message = "Phone External Storage media is unmounted and shared via USB mass storage.\n"
					+ "Please check the external storage";
			ready = false;
		} else if (extStoState.equals(Environment.MEDIA_UNMOUNTABLE)) {
			message = "Phone External Storage cannot be mounted.\n"
					+ "Please check the external storage";
			ready = false;
		} else if (extStoState.equals(Environment.MEDIA_UNMOUNTED)) {
			message = "Phone External Storage is not mounted.\n"
					+ "Please check the external storage";
			ready = false;
		}

		if (message != null) {
			Toast.makeText(appContext, message, Toast.LENGTH_LONG).show();
		}
		return ready;
	}

	public boolean isNetworkConnected() {
		if (!checkNetworkConnection()) {
			Toast.makeText(
					appContext,
					"No Internet access. Please check your network Connection.",
					Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	private boolean checkNetworkConnection() {
		ConnectivityManager cm = 
				(ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// No active network.
			return false;
		} else
			return true;
	}
}

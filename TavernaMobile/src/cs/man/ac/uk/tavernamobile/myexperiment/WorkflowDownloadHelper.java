package cs.man.ac.uk.tavernamobile.myexperiment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;

public class WorkflowDownloadHelper implements CallbackTask {

	private String downloadURL;
	private String fileName;
	private int file_size;
	private int downloadedSize;

	private int nofificationId;

	private CallbackTask downloadListner;

	private Activity currentActivity;

	public WorkflowDownloadHelper(Activity activity){
		currentActivity = activity;
		nofificationId = 0;
	}

	public void StartDownload(String url, CallbackTask listner){
		downloadURL = url;
		downloadListner = listner;
		fileName = extractFileName(downloadURL);
		StartDownloading(url, fileName);
	}

	// extract file name from download URL
	private String extractFileName(String downloadURL){
		String[] elementsList = downloadURL.split("/");
		return elementsList[elementsList.length - 1];
	}

	private void StartDownloading(String downloadURL, String fileName){
		nofificationId++;
		try {
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(currentActivity, this, "Downloading workflow file...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Object onTaskInProgress(Object... params) {
		String locationToSave = getFileSaveLocation();
		Object DownloadResult = this.DownloadWorkflow(downloadURL, fileName, locationToSave);

		return DownloadResult;
	}

	public Object onTaskComplete(Object... result) {
		String fileStoreLocation = null;
		String messageToShow = null;
		// check whether it is a file that is being returned
		if(result[0] instanceof File){
			fileStoreLocation = ((File) result[0]).getAbsolutePath();
			messageToShow = "File saved at " + fileStoreLocation;
			if(downloadListner != null){
				downloadListner.onTaskComplete(fileStoreLocation);
			}
		}
		// if it is not a file
		else if(fileStoreLocation == null){
			messageToShow = (String) result[0];
		}
		
		Toast toast = Toast.makeText(currentActivity, messageToShow, Toast.LENGTH_LONG);
		toast.show();

		return null;
	}

	// method to setup location to save downloaded workflow
	private String getFileSaveLocation() {

		String storeLocation = null;

		/**** store in SD card ****/
		File root = android.os.Environment.getExternalStorageDirectory();
		// if no SD card present store in the application directory
		if(root == null){
			try {
				PackageInfo packageInfo = currentActivity.getPackageManager()
						.getPackageInfo(currentActivity.getPackageName(), 0);
				String applicationDir = packageInfo.applicationInfo.dataDir;
				root = new File(applicationDir);
			} catch (NameNotFoundException e) {
				// TODO: "log" has to be removed in release version
				Log.e("AppPackage", "Error Package name not found ", e);
			}
		}

		File dir = new File (root.getAbsolutePath() + "/TavernaAndroid");
		if(dir.exists() == false) 
		{
			dir.mkdirs();
		}

		storeLocation = dir.getAbsolutePath();

		/**** or find application directory to store workflow downloaded ***/
		/*String applicationDir = null;
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			applicationDir = packageInfo.applicationInfo.dataDir;
		} catch (NameNotFoundException e) {
			// TODO: "log" has to be removed in release version
			Log.e("AppPackage", "Error Package name not found ", e);
		}

		if (applicationDir == null)
		{
			MessageHelper.showMessageDialog(this, 
					"Invalid Application Directory. Please contact developer for support.");
		}*/
		return storeLocation;
	}

	/*** download t2flow from MyExperiment ***/
	private Object DownloadWorkflow(String downloadURL, String fileName, String locationToSave)
	{
		if (downloadURL == null){
			throw new IllegalArgumentException("downloadURL");
		}
		if (fileName == null){
			throw new IllegalArgumentException("fileName");
		}

		File outputFile = null;

		try {
			URL url = new URL(downloadURL);		

			// log info for debugging
			long startTime = System.currentTimeMillis();
			// TODO: "log" has to be removed in release version
			Log.d("Download", "download begining");
			Log.d("Download", "download URL:" + url);
			Log.d("Download", "downloaded file name:" + fileName);

			/* Open a connection to that URL. */
			URLConnection connection = url.openConnection();

			file_size = connection.getContentLength() / 1024;
			System.out.println("file size : "+file_size);

			// start downloading notification
			startNotification();

			// Define InputStreams to read from the URLConnection.
			InputStream inputStream = connection.getInputStream();

			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

			// Read bytes to the Buffer until there is nothing more to read(-1).
			ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(1024);
			int current = 0;
			while ((current = bufferedInputStream.read()) != -1) 
			{
				byteArrayBuffer.append((byte) current);
				// set downloaded size in order to update notification
				downloadedSize = byteArrayBuffer.length() / 1024;
				//System.out.println("downloaded Size : "+downloadedSize);
			}
			
			inputStream.close();

			/* Convert the Bytes read to a String. 
			 * and store file in application directory*/
			outputFile = new File(locationToSave, fileName);
			FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
			fileOutputStream.write(byteArrayBuffer.toByteArray());
			fileOutputStream.flush();
			fileOutputStream.close();

			// log info for debugging
			// TODO: has to be removed in release version
			Log.d("Download", "download complete in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");

		} catch (IOException e) {
			// File not found exception - if the workflow is set to be not "downloadable"...
			// MessageHelper.showMessageDialog(currentActivity, e.getMessage());
			String message = "An error occurred during downloading, please try again.";
			if(e instanceof FileNotFoundException){
				message = "The workflow file (" + fileName + ") does not exists on the server";
			}
			return message;
		} catch (Exception e) {
			/*MessageHelper.showMessageDialog(currentActivity, 
			 * "An error occurred during downloading\nPlease try again.");*/
			return "An error occurred during downloading, please try again.";
		}

		return outputFile;
	}

	private void startNotification(){

		final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(currentActivity)
		.setSmallIcon(R.drawable.download)
		.setContentTitle(fileName)
		.setContentText("Download in progress");

		final NotificationManager mNotificationManager =
				(NotificationManager) currentActivity.getSystemService(Context.NOTIFICATION_SERVICE);

		new Thread(
				new Runnable() {
					public void run() {

						while (file_size != 0 && downloadedSize < file_size){

							mBuilder.setProgress(file_size, downloadedSize, false);
							mBuilder.setContentText(downloadedSize + "KB / " + file_size + "KB");
							// Displays the progress bar
							mNotificationManager.notify(nofificationId, mBuilder.build());

							try {
								// Sleep for 1 seconds before updating the state
								// of notification again
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO: "log" has to be removed in release version
								Log.d("sleep", "sleep failure");
							}
						}
						// When the download is finished, updates the notification
						mBuilder.setContentText("download complete")
						// Removes the progress bar
						.setProgress(0,0,false);
						mNotificationManager.notify(nofificationId, mBuilder.build());
					}
				}
				).start();
	}
}
package cs.man.ac.uk.tavernamobile.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import uk.org.taverna.server.client.InputPort;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import cs.man.ac.uk.tavernamobile.dataaccess.DataProviderConstants;
import cs.man.ac.uk.tavernamobile.dataaccess.DatabaseLoader;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowBE;
import cs.man.ac.uk.tavernamobile.io.InputsList;
import cs.man.ac.uk.tavernamobile.myexperiment.WorkflowDownloadHelper;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;

public class WorkflowLaunchHelper {

	// Activities that use functionalities in this helper class
	// are all fragment activity
	// TODO: temporary solution for contentResolver type 
	// mismatch in activities and fragments
	private FragmentActivity currentActivity;

	// Object to stored necessary workflow data
	private WorkflowBE workflowEntity;

	// code to remember the starting activity
	// in order to help with navigation
	private int Activity_Starter_Code;

	// Workflow-record-checking loader ID
	private static final int loaderId = 1;
	
	// try to reuse object...
	// utilities set up
	private SystemStatesChecker checker;
	private WorkflowRunManager manager;

	private boolean firstEntry;
	private int launchMode;
	private CallbackTask launchListener;

	public WorkflowLaunchHelper(FragmentActivity activity, int code) {
		currentActivity = activity;
		Activity_Starter_Code = code;
		checker = new SystemStatesChecker(currentActivity);
		manager = new WorkflowRunManager(currentActivity);
		launchListener = new RunCreationListener(); // default
		firstEntry = false;
	}
	
	public void registerLaunchListener(CallbackTask listener){
		launchListener = listener;
	}

	/**
	 * Launch the workflow. Check whether the workflow file 
	 * needs to be downloaded first
	 * 
	 * @param entity - workflow entity 
	 * @param mode - 0 = launch only. 1 = launch -> set saved input -> start Run
	 */
	public void launch(WorkflowBE entity, int mode) {
		// check whether phone storage is ready for saving output
		if(!checker.IsPhoneStorageReady()){
			return;
		}
		
		// set the workflow entity the instance is dealing with
		workflowEntity = entity;
		// set the launch mode
		launchMode = mode;
		if(launchMode != 0 && launchMode != 1){
			throw new IllegalArgumentException("launchMode can only be 0 or 1");
		}
		
		// preparing for checking workflow existence
		String[] projection = new String[] {
				DataProviderConstants.WorkflowTitle,
				DataProviderConstants.WorkflowUri,
				DataProviderConstants.Version,
				DataProviderConstants.WorkflowFilePath,
				DataProviderConstants.UploaderName };
		String selection = DataProviderConstants.WorkflowTitle + " = ? AND "
						 + DataProviderConstants.Version + " = ? AND "
						 + DataProviderConstants.UploaderName + " = ?";
		String[] selectionArgs = new String[] { workflowEntity.getTitle(), 
												workflowEntity.getVersion(), 
												workflowEntity.getUploaderName() };

		Bundle loaderArgs = new Bundle();
		loaderArgs.putStringArray("projection", projection);
		loaderArgs.putString("selection", selection);
		loaderArgs.putStringArray("selectionArgs", selectionArgs);
		loaderArgs.putString("tableURI", DataProviderConstants.WF_TABLE_CONTENTURI.toString());
		
		// initLoader() - If at the point of call the caller is in its started
		// state, and the requested loader already exists and has generated its data,
		// then callback onLoadFinished(Loader, D) will be called immediately
		// (inside of this function)

		// restartLoader() - Starts a new or restarts an existing Loader in this
		// manager, registers the callbacks to it, and (if the activity/fragment is
		// currently started) starts loading it. If a loader with the same id has previously been
		// started it will automatically be destroyed when the new loader completes its
		// work. The callback will be delivered before the old loader is destroyed
		this.currentActivity.getSupportLoaderManager().restartLoader(
				loaderId, loaderArgs,
				new DatabaseLoader(currentActivity,
						new WorkflowDataLoadingListener()));
	}

	// class to deal with results loaded by content loader.
	// To check whether the workflow has been ran before
	// whether the workflow file actually exist
	private class WorkflowDataLoadingListener implements CallbackTask {
		@Override
		public Object onTaskInProgress(Object... param) { return null;}

		@Override
		public Object onTaskComplete(Object... result) {
			if (result[0] instanceof Cursor) {
				Cursor existingWFRecord = (Cursor) result[0];

				// if the cursor was previously consumed
				/*if (existingWFRecord.isClosed()) {
					// do nothing - not to load, downloading etc.
					// Changes made to the data source can trigger
					// the reloading of data and hence execute the callback
					return null;
				}*/

				// if the workflow has a record in database
				// try to find the workflow file saved on the phone
				// in order to launch later
				if (existingWFRecord.moveToFirst()) {

					String downloadURL = existingWFRecord.getString(
							existingWFRecord.getColumnIndexOrThrow(
									DataProviderConstants.WorkflowUri));

					String workflowFilePath = existingWFRecord.getString(
							existingWFRecord.getColumnIndexOrThrow(
									DataProviderConstants.WorkflowFilePath));

					/*File workflowFile = new File(android.os.Environment
							.getExternalStorageDirectory().getPath()
							+ "/TavernaAndroid/" + workflowFilename);*/
					//File workflowFile = new File(workflowFilePath);

					if (new File(workflowFilePath).exists()) {
						workflowEntity.setFilePath(workflowFilePath);
						createWorkflowRun();
					}
					// else if it has record but the file doesn't exist
					else {
						checkAndDownload(downloadURL);
					}
				}
				// else if no record at all
				else {
					firstEntry = true;
					// prepare to download the workflow file
					String downloadURL = workflowEntity.getWorkflow_URI();
					// Check whether the workflow file
					// is actually a "t2flow" file or not
					String[] urlSegments = downloadURL.split("//");
					String path = urlSegments[1];
					String[] pathSegments = path.split("/");
					String fileName = pathSegments[pathSegments.length - 1];
					if (fileName.matches(".*\\.t2flow")) {
						checkAndDownload(downloadURL);
					} else {
						String message = "The workflow document is not a \".t2flow\" file, "
								+ "which is currently not supported";
						MessageHelper.showMessageDialog(currentActivity, null, message, null);
					}
				}

				existingWFRecord.close();
			}
			return null;
		}

		private void checkAndDownload(String downloadURL) {
			// flag for privileges checking
			boolean canDownload = false;
			List<String> privileges = workflowEntity.getPrivileges();
			for (String privilege : privileges) {
				if (privilege.equals("download")) {
					canDownload = true;
				}
			}

			if (canDownload) {
				downloadWorkflowFile(currentActivity, downloadURL);
			} else {
				String message = "You don't have the privilege to download this workflow.";
				MessageHelper.showMessageDialog(currentActivity, "Attention", message, null);
			}
		}
	}

	private void downloadWorkflowFile(final Activity currentActivity, final String downloadURL) {
		MessageHelper.showOptionsDialog(
				currentActivity,
				"The workflow file not found.\n"
				+ "Do you want to downloaded it from myExperiment.org?",
				"Attention", 
				new CallbackTask() {
					@Override
					public Object onTaskInProgress(Object... param) {
						WorkflowDownloadHelper downloadHelper = 
								new WorkflowDownloadHelper(currentActivity);
						try {
							downloadHelper.StartDownload(downloadURL, new DownloadListener());
						} catch (Exception e) {
							Toast.makeText(
								currentActivity,
								"Workflow download failed, please try again.\n" + e.getMessage(),
								Toast.LENGTH_LONG).show();
						}
						return null;
					}

					@Override
					public Object onTaskComplete(Object... result) { return null; }
				}, null);
	}

	private class DownloadListener implements CallbackTask {

		public Object onTaskInProgress(Object... param) {
			return null;
		}

		public Object onTaskComplete(Object... result) {
			String filePath = (String) result[0];

			if(filePath == null){
				return null;
			}
			workflowEntity.setFilePath(filePath);
			// if it is the first time the workflow
			// is downloaded record the workflow detail
			if(firstEntry){
				recordWorkflow();
			}else{
				// only update the saved workflow file path
				updateWorkflowFilePath(filePath);
			}
			// then create run for it
			createWorkflowRun();
			return null;
		}
	}

	private void recordWorkflow() {
		ContentValues valuesToInsert = new ContentValues();

		valuesToInsert.put(DataProviderConstants.WorkflowTitle, workflowEntity.getTitle());
		valuesToInsert.put(DataProviderConstants.Version, workflowEntity.getVersion());
		valuesToInsert.put(DataProviderConstants.UploaderName, workflowEntity.getUploaderName());
		valuesToInsert.put(DataProviderConstants.WorkflowFilePath, workflowEntity.getFilePath());
		byte[] avatarData = bitmapToByteArray(workflowEntity.getAvatar());
		valuesToInsert.put(DataProviderConstants.Avatar, avatarData);
		valuesToInsert.put(DataProviderConstants.WorkflowUri, workflowEntity.getWorkflow_URI());

		// insert via content provider
		this.currentActivity.getContentResolver()
				.insert(DataProviderConstants.WF_TABLE_CONTENTURI, valuesToInsert);
	}
	
	private void updateWorkflowFilePath(String workflowFilePath){
		ContentValues valuesToUpdate = new ContentValues();
		valuesToUpdate.put(DataProviderConstants.WorkflowFilePath, workflowFilePath);
		String selection = DataProviderConstants.WorkflowTitle + " = ? AND "+
						   DataProviderConstants.Version + " = ? AND " +
						   DataProviderConstants.UploaderName + " = ?";
		String[] selectionArgs = 
				new String[] { workflowEntity.getTitle(), 
							   workflowEntity.getVersion(), 
							   workflowEntity.getUploaderName()};
		this.currentActivity.getContentResolver().update(
				DataProviderConstants.WF_TABLE_CONTENTURI, valuesToUpdate, selection, selectionArgs);
	}

	private void createWorkflowRun() {
		try {
			switch(launchMode){
			case 0:
				manager.CreateRun(workflowEntity, launchListener);
				break;
			case 1:
				manager.StartRunWithSavedInput(workflowEntity, launchListener);
				break;
			default:
				break;
			}
			recordLaunchTime(firstEntry, workflowEntity);
			// set the flag to indicate that it is not the 
			// first time this workflow has been launched.
			// For recording the launch time
			if(firstEntry){
				firstEntry = false;
			}
		} catch (Exception e) {
			Toast.makeText(currentActivity,
					"Run Creation failed please try again.",
					Toast.LENGTH_LONG).show();
		}
	}

	// public due to used at multiple places
	public class RunCreationListener implements CallbackTask {

		public Object onTaskInProgress(Object... param) {
			return null;
		}

		public Object onTaskComplete(Object... result) {
			// test for exception message first
			if (result[0] instanceof String) {
				String exceptionMessage = (String) result[0];
				if (exceptionMessage != null) {
					MessageHelper.showMessageDialog(currentActivity, null, exceptionMessage, null);
				}
			} else {
				/*HashMap<String, Map<String, InputPort>> idAndInputPorts =
				(HashMap<String, Map<String, InputPort>>) result[0];

				Map<String, InputPort> inputPorts = null;
				String runID = null;

				Iterator<Entry<String, Map<String, InputPort>>> it = idAndInputPorts
						.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, Map<String, InputPort>> pair = it.next();
					runID = pair.getKey();
					inputPorts = pair.getValue();
				}*/
				
				Map<String, InputPort> inputPorts = (Map<String, InputPort>)result[0];
				if(inputPorts == null){
					return null;
				}
				// Navigate to input screen
				Intent goToInputList = new Intent(currentActivity, InputsList.class);
				Bundle extras = prepareInputs(inputPorts, workflowEntity);
				goToInputList.putExtras(extras);
				currentActivity.startActivity(goToInputList);
			}
			return null;
		}
	}
	
	private void recordLaunchTime(boolean firstTime, WorkflowBE workflowEntity) {		
		DateFormat df = DateFormat.getDateTimeInstance();
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		String gmtTime = df.format(new Date());
		
		ContentValues values = new ContentValues(); 
		if(firstTime){
			values.put(DataProviderConstants.FirstLaunch, gmtTime);
			values.put(DataProviderConstants.LastLaunch, gmtTime);
		}else{
			values.put(DataProviderConstants.LastLaunch, gmtTime);
		}
		String selection = 
				   DataProviderConstants.WorkflowTitle + " = ? AND "
				 + DataProviderConstants.Version + " = ? AND "
				 + DataProviderConstants.UploaderName + " = ?";
		String[] selectionArgs = new String[] {
				workflowEntity.getTitle(), 
				workflowEntity.getVersion(), 
				workflowEntity.getUploaderName()};
		
		currentActivity.getContentResolver().update(
				DataProviderConstants.WF_TABLE_CONTENTURI, 
				values, 
				selection, selectionArgs);
	}

	public Bundle prepareInputs(Map<String, InputPort> inputPorts, WorkflowBE workflowEntity) {
		// Navigate to input screen
		Bundle extras = new Bundle();
		if (inputPorts != null && !inputPorts.isEmpty()) {
			ArrayList<String> inputNames = extractInputName(inputPorts);
			extras.putStringArrayList("input_names", inputNames);
		}
		extras.putSerializable("workflowEntity", workflowEntity);
		extras.putInt("activity_starter", Activity_Starter_Code);

		return extras;
	}

	private ArrayList<String> extractInputName(Map<String, InputPort> inputPorts) {
		ArrayList<String> inputNames = new ArrayList<String>();
		Iterator<Entry<String, InputPort>> it = inputPorts.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, InputPort> pair = it.next();
			inputNames.add(pair.getKey());
		}
		return inputNames;
	}
	
	private byte[] bitmapToByteArray(Bitmap bitmapImage) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] bitmapdata = stream.toByteArray();

		return bitmapdata;
	}
}
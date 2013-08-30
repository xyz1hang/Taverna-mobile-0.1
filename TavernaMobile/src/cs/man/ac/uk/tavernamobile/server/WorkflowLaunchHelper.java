package cs.man.ac.uk.tavernamobile.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.org.taverna.server.client.InputPort;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import cs.man.ac.uk.tavernamobile.dataaccess.DataProviderConstants;
import cs.man.ac.uk.tavernamobile.dataaccess.DatabaseLoader;
import cs.man.ac.uk.tavernamobile.io.InputsList;
import cs.man.ac.uk.tavernamobile.myexperiment.WorkflowDownloadHelper;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;
import cs.man.ac.uk.tavernamobile.utils.WorkflowBE;
import cs.man.ac.uk.tavernamobile.utils.WorkflowFileLoader;

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
	
	// the ID of the row that the workflow record
	// was being inserted, which will be used to update
	// the Run reference table
	private String insertedRowID;
	
	// try to reuse object...
	// utilities set up
	private SystemStatesChecker checker;
	private WorkflowRunManager manager;

	public WorkflowLaunchHelper(FragmentActivity activity, WorkflowBE wfEntity,
			int code) {
		currentActivity = activity;
		workflowEntity = wfEntity;
		Activity_Starter_Code = code;
		checker = new SystemStatesChecker(currentActivity);
		manager = new WorkflowRunManager(currentActivity);
	}

	/**
	 * Launch the workflow. Check whether the workflow file 
	 * needs to be downloaded first
	 */
	public void launch() {

		// check whether phone storage is ready for saving output
		if(!checker.IsPhoneStorageReady()){
			return;
		}
		
		// preparing for checking workflow existence
		// by querying database and then launch the workflow
		// after the checking is finished
		String[] projection = new String[] {
				DataProviderConstants.WorkflowTitle,
				DataProviderConstants.WorkflowUri,
				DataProviderConstants.Version,
				DataProviderConstants.WorkflowFileName,
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
				loaderId,
				loaderArgs,
				new DatabaseLoader(currentActivity,
						new workflowDataLoadingListener()));
	}

	// class to deal with results loaded by content loader.
	// To check whether the workflow has been ran before
	private class workflowDataLoadingListener implements CallbackTask {

		@Override
		public Object onTaskInProgress(Object... param) {
			return null;
		}

		@Override
		public Object onTaskComplete(Object... result) {
			if (result[0] instanceof Cursor) {
				Cursor existingWFRecord = (Cursor) result[0];

				// if the cursor was previously consumed
				if (existingWFRecord.isClosed()) {
					// do nothing - not to load, downloading etc.
					// Changes made to the data source can trigger
					// the reloading of data and hence execute the callback
					return null;
				}

				// if the workflow has a record in database
				// try to find the workflow file saved on the phone
				if (existingWFRecord.moveToFirst()) {

					String downloadURL = existingWFRecord
							.getString(existingWFRecord
									.getColumnIndexOrThrow(DataProviderConstants.WorkflowUri));

					workflowEntity.setWorkflow_URI(downloadURL);

					String workflowFilename = existingWFRecord
							.getString(existingWFRecord
									.getColumnIndexOrThrow(DataProviderConstants.WorkflowFileName));

					File workflowFile = new File(android.os.Environment
							.getExternalStorageDirectory().getPath()
							+ "/TavernaAndroid/" + workflowFilename);

					if (workflowFile.exists()) {
						createRunWithFileData(workflowFile);
					}
					// else if it has record but the file doesn't exist
					else {
						checkAndDownload(downloadURL);
					}
				}// end of if record exists

				// else if no record at all
				else {
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
						String message = "The workflow document is not a \"t2flow\" file,\n"
								+ "which is currently not supported";
						MessageHelper.showMessageDialog(currentActivity,
								message);
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
				MessageHelper.showMessageDialog(currentActivity,
						message);
			}
		}
	}

	private void downloadWorkflowFile(final Activity currentActivity,
			final String downloadURL) {
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
								"Workflow download failed, please try again.",
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

			String[] elements = filePath.split("/");
			String fileName = elements[elements.length - 1];
			File workflowFile = new File(filePath);
			// save workflow detail in database
			recordWorkflow(fileName);
			// then create run for it
			createRunWithFileData(workflowFile);
			return null;
		}
	}

	private void recordWorkflow(String workflowFileName) {
		ContentValues valuesToInsert = new ContentValues();

		valuesToInsert.put(DataProviderConstants.WorkflowTitle, workflowEntity.getTitle());
		valuesToInsert.put(DataProviderConstants.Version, workflowEntity.getVersion());
		valuesToInsert.put(DataProviderConstants.WorkflowFileName, workflowFileName);
		valuesToInsert.put(DataProviderConstants.UploaderName, workflowEntity.getUploaderName());
		byte[] avatarData = bitmapToByteArray(workflowEntity.getAvator());
		valuesToInsert.put(DataProviderConstants.Avatar, avatarData);
		valuesToInsert.put(DataProviderConstants.WorkflowUri, workflowEntity.getWorkflow_URI());

		// insert via content provider
		Uri uri = this.currentActivity.getContentResolver()
				.insert(DataProviderConstants.WF_TABLE_CONTENTURI, valuesToInsert);
		insertedRowID = uri.getLastPathSegment();
	}

	private void createRunWithFileData(File workflowFile) {
		byte[] workflowData = null;
		try {
			workflowData = WorkflowFileLoader.getBytesFromFile(workflowFile);
		} catch (Exception e) {
			MessageHelper.showMessageDialog(currentActivity, e.getMessage());
		}

		// create run
		if (workflowData != null) {
			try {
				manager.CreateRun(workflowData, new RunCreationListener());
			} catch (Exception e) {
				Toast.makeText(currentActivity,
						"Run Creation failed please try again.",
						Toast.LENGTH_LONG).show();
			}
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
					MessageHelper.showMessageDialog(currentActivity,
							exceptionMessage);
				}
			} else {
				HashMap<String, Map<String, InputPort>> idAndInputPorts =
				(HashMap<String, Map<String, InputPort>>) result[0];

				Map<String, InputPort> inputPorts = null;
				String runID = null;

				Iterator<Entry<String, Map<String, InputPort>>> it = idAndInputPorts
						.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, Map<String, InputPort>> pair = it.next();
					runID = pair.getKey();
					inputPorts = pair.getValue();
				}

				// prepare data to be inserted into database tables
				ContentValues args = new ContentValues();
				args.put(DataProviderConstants.Run_Id, runID);
				
				if(insertedRowID != null){
					// insert run ID into the reference table
					int wfId = Integer.parseInt(insertedRowID);
					args.put(DataProviderConstants.WF_ID, wfId);
				}
				// if there was no insert i.e the workflow had been ran before
				// we find the ID of the workflow which this run
				// belongs to and insert it into the workflowID_runID table 
				// along with the run id
				else{
					String subQuery = "(select WF_ID from launchHistory"+
							  "where Workflow_Title=\""+workflowEntity.getTitle()+ "\" AND"+
							  "Version = \""+workflowEntity.getVersion()+"\" AND"+
							  "Uploader_Name = \""+workflowEntity.getUploaderName()+"\")";
					args.put(DataProviderConstants.WF_ID, subQuery);
				}
				
				currentActivity.getContentResolver().insert(
						DataProviderConstants.RUN_TABLE_CONTENTURI, args);

				prepareInputs(inputPorts);
			}

			return null;
		}
	}

	public void prepareInputs(Map<String, InputPort> inputPorts) {
		// Navigate to input screen
		Intent goToInputList = new Intent(currentActivity, InputsList.class);
		Bundle extras = new Bundle();

		if (!inputPorts.isEmpty()) {
			ArrayList<String> inputNames = extractInputName(inputPorts);
			extras.putStringArrayList("input_names", inputNames);
		}

		extras.putString("workflow_title", workflowEntity.getTitle());
		extras.putInt("activity_starter", Activity_Starter_Code);
		goToInputList.putExtras(extras);

		currentActivity.startActivity(goToInputList);
	}

	private ArrayList<String> extractInputName(Map<String, InputPort> inputPorts) {
		ArrayList<String> inputNames = new ArrayList<String>();
		Iterator<Entry<String, InputPort>> it = inputPorts.entrySet()
				.iterator();
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
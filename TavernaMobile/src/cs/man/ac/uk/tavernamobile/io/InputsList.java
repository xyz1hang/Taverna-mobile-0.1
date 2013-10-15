package cs.man.ac.uk.tavernamobile.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowBE;
import cs.man.ac.uk.tavernamobile.server.WorkflowRunManager;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

public class InputsList extends Activity{
	
	// for context access within this class
	private InputsList currentActivity;
	private WorkflowRunManager manager;

	// data used to display
	private ArrayList<Map<String, String>> listData = new ArrayList<Map<String, String>>();
	private ArrayList<String> inputNames;
	private WorkflowBE workflowEntity;
	
	public final static String EXTRA_FILE_PATH = "file_path";
	private final int REQUEST_PICK_FILE = 1;
	private final int REQUEST_PICK_DROPBOX_FILE = 2;
	private InputsListAdaptor resultListAdapter;
	
	// collection to store all user inputs.
	// this is data needed for the run
	public HashMap<String, Object> userInputs = new HashMap<String, Object>();
	// store the name of the input that user is 
	// currently selecting the input file for
	private String currentInputName;
	private int inputsListSelectedIndex;

	private int Activity_Starter_Code;
	private DropboxAPI<AndroidAuthSession> mApi;
	private SystemStatesChecker sysChecker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inputs);
		
		currentActivity = this;
		manager = new WorkflowRunManager(this);
		mApi = TavernaAndroid.getmApi();
		sysChecker = new SystemStatesChecker(currentActivity);
		
		// UI components
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setIcon(this.getResources().getDrawable(R.drawable.taverna_wheel_logo_medium));
		actionBar.setTitle("Supply Inputs");
		
		final Button runButton = (Button) findViewById(R.id.runButton);
		Button cancel = (Button) findViewById(R.id.cancelButton);
		TextView title = (TextView) findViewById(R.id.input_wfTitle);
		TextView number = (TextView) findViewById(R.id.input_wfNumber);
		ListView resultList = (ListView) findViewById(R.id.inputList);

		// get data
		if (savedInstanceState != null) {
			inputNames = savedInstanceState.getStringArrayList("inputNames");
			workflowEntity =  (WorkflowBE) savedInstanceState.getSerializable("workflowEntity");
			Activity_Starter_Code = savedInstanceState.getInt("activity_starter");
		}
		else{
			// get data passed in
			Bundle extras = getIntent().getExtras();
			workflowEntity = (WorkflowBE) extras.getSerializable("workflowEntity");
			inputNames = extras.getStringArrayList("input_names");
			Activity_Starter_Code = extras.getInt("activity_starter"); 
		}
		if(inputNames != null && inputNames.size() > 0){
			preparingListData(inputNames);	
		}

		// data setup
		title.setText(workflowEntity.getTitle());
		if (inputNames != null && inputNames.size() > 1){
			number.setText("This workflow has "+ inputNames.size() + " inputs : ");
		}
		else if(inputNames != null && inputNames.size() == 1){
			number.setText("This workflow has "+ inputNames.size() + " input : ");
		}else{
			number.setText("This workflow has no input ");
		}

		LayoutInflater myInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		resultListAdapter = new InputsListAdaptor(myInflater, listData);
		resultList.setAdapter(resultListAdapter);
		
		runButton.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(android.view.View v) {
				if (!(sysChecker.isNetworkConnected())){
					return;
				}

				if(inputNames != null && inputNames.size() > 0){
					// check inputs not null if there are any
					String unSetInputName = inputCheck(userInputs);
					if (unSetInputName != null){
						MessageHelper.showMessageDialog(currentActivity, 
								"Empty field", "Please set input for \"" + unSetInputName+ "\"", null);
						return;
					}
				}
				runButton.setEnabled(false);
				preprocessInputs();
			}
		});

		cancel.setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View v) {
				currentActivity.finish();
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putStringArrayList("inputNames", inputNames);
		savedInstanceState.putSerializable("workflowEntity", workflowEntity);
		savedInstanceState.putInt("activity_starter", Activity_Starter_Code);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		inputNames = savedInstanceState.getStringArrayList("inputNames");
		workflowEntity =  (WorkflowBE) savedInstanceState.getSerializable("workflowEntity");
		Activity_Starter_Code = savedInstanceState.getInt("activity_starter");
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void preprocessInputs(){
		if (!(sysChecker.isNetworkConnected())){
			return;
		}
		
		new BackgroundTaskHandler().StartBackgroundTask(currentActivity, 
			new CallbackTask(){
				@Override
				public Object onTaskInProgress(Object... param) {
					// get input files folder path
					String inputsSubPath = "/TavernaAndroid/Inputs/" 
							+ workflowEntity.getTitle().replace(" ", "") + "_" 
							+ workflowEntity.getVersion() + "_" 
							+ workflowEntity.getUploaderName().replace(" ", "") + "/";
					String locationToStore = getFileSaveLocation(inputsSubPath);
					// retrieve dropbox files
					Iterator<Entry<String, Object>> it = userInputs.entrySet().iterator();
					while(it.hasNext()){
						Map.Entry<String, Object> pair = it.next();
						if(pair.getValue() instanceof DropboxInputFile){
							FileOutputStream outputStream = null;
							DropboxInputFile dbfile = (DropboxInputFile) pair.getValue();
							try {
								String[] segments = dbfile.getPath().split("/");
								String fileName = segments[segments.length - 1];
							    File file = new File(locationToStore+"/"+fileName);
							    if(!file.exists()){
							    	file.createNewFile();
							    }
							    outputStream = new FileOutputStream(file);
							    mApi.getFile(dbfile.getPath(), null, outputStream, null);
							    // after this line the file should have actual content
							    userInputs.put(pair.getKey(), file);
							} catch (Exception e) {
								e.printStackTrace();
							    return "Error retrieving dropbox file";
							} finally {
							    if (outputStream != null) {
							        try {
							            outputStream.close();
							        } catch (IOException e) {
							        	// swallow irrelevant message
							        	e.printStackTrace();
							        }
							    }
							}
						}
					}
					return null;
				}
		
				@Override
				public Object onTaskComplete(Object... result) {
					if(result[0] instanceof String){
						Toast.makeText(currentActivity, (String)result[0], Toast.LENGTH_SHORT).show();
						return null;
					}
					startTheRun();
					return null;
				}
		}, "Loading dropbox files...");
	}
	
	private void startTheRun() {
		MessageHelper.showOptionsDialog(currentActivity, 
			"Do you want to monitor the run?", 
			"Run Monitoring", 
			// Positive button action
			new CallbackTask(){
				@Override
				public Object onTaskInProgress(Object... param) {
					// go to monitor and then start the run there
					Intent goToMonitor = new Intent(currentActivity, RunMonitorScreen.class);
					Bundle extras = new Bundle();
					extras.putSerializable("workflowEntity", workflowEntity);
					if(inputNames != null && inputNames.size() > 0){
						extras.putSerializable("userInputs", userInputs);
					}
					extras.putInt("activity_starter", Activity_Starter_Code);
					extras.putString("command", "RunWorkflow");
					goToMonitor.putExtras(extras);
					currentActivity.startActivity(goToMonitor);
					return null;
				}

				@Override
				public Object onTaskComplete(Object... result) { return null; }
			}, 
			// Negative button action
			new CallbackTask(){
				@Override
				public Object onTaskInProgress(Object... param) {
					// if user don't want to monitor
					// just start the run and the quit the activity
					manager.StartWorkflowRun(
						userInputs, 
						workflowEntity, 
						new CallbackTask(){
							@Override
							public Object onTaskInProgress(Object... param) { return null; }

							@Override
							public Object onTaskComplete(Object... result) {
								if(result == null || result.length < 1){
									return null;
								}
								// in case there are any message returned 
								// during the starting of workflow run
								if(result[0] instanceof String){
									final String message = (String) result[0];
									Toast.makeText(currentActivity, message, Toast.LENGTH_SHORT).show();
								}
								return null;
							}
						},
						false);
					
					currentActivity.finish();
					return null;
				}

				@Override
				public Object onTaskComplete(Object... result) {return null;}
			});
	}// end of startTheRun
	
	static class ViewHolder{
		TextView inputNameView;
		TextView fileName;
		EditText inputValue;
		ImageButton fileSelectButton;
		ImageButton dropBoxSelectButton;
	}
	
	private class InputsListAdaptor extends BaseAdapter {

		private LayoutInflater myInflater;
		private ArrayList<Map<String, String>> data;

		public InputsListAdaptor(LayoutInflater layoutInflater, ArrayList<Map<String, String>> listData){
			myInflater = layoutInflater;
			data = listData;
		}

		public int getCount() {
			return data.size();
		}

		public Object getItem(int position) {
			return data.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// index that help with matching input data
			// with correct input ports
			final int selectedInputIndex = position;
			// view holder
			ViewHolder viewHolder = null;
			if (convertView == null){
				convertView = myInflater.inflate(R.layout.inputs_single_row, null);
				viewHolder  = new ViewHolder();
				viewHolder.inputNameView = (TextView) convertView.findViewById(R.id.inputName);
				viewHolder.fileName = (TextView) convertView.findViewById(R.id.selectedFileName);
				viewHolder.inputValue = (EditText) convertView.findViewById(R.id.inputValueText);
				viewHolder.fileSelectButton = (ImageButton) convertView.findViewById(R.id.fileSelectButton);
				viewHolder.dropBoxSelectButton = (ImageButton) convertView.findViewById(R.id.dropboxFileSelButton);
				convertView.setTag(viewHolder);
			} else{
				viewHolder = (ViewHolder) convertView.getTag();
			}

			Map<String, String> nameFilePair = (Map<String, String>) getItem(position);
			// set input port name
			String inputName = nameFilePair.keySet().iterator().next();
			viewHolder.inputNameView.setText(inputName);
			// display select file name if not null
			String selfileName = nameFilePair.get(inputName);
			if (selfileName != null){
				viewHolder.fileName.setText(selfileName);
			}

			viewHolder.inputValue.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener(){
				public void onFocusChange(View v, boolean hasFocus){
					if (!hasFocus){
						final EditText inputText = (EditText) v;
						String textInput = inputText.getText().toString();
						userInputs.put(inputNames.get(selectedInputIndex), textInput);
					}
				}
			});
			
			viewHolder.fileSelectButton.setOnClickListener(new android.view.View.OnClickListener(){
				public void onClick(View v) {
					currentInputName = inputNames.get(selectedInputIndex);
					inputsListSelectedIndex = selectedInputIndex;
					Intent intent = new Intent(currentActivity, FilePickerActivity.class);
					intent.putExtra("inputPortName", currentInputName);
					startActivityForResult(intent, REQUEST_PICK_FILE);
				}
			});
			
			viewHolder.dropBoxSelectButton.setOnClickListener(new android.view.View.OnClickListener(){
				public void onClick(View v) {
					currentInputName = inputNames.get(selectedInputIndex);
					inputsListSelectedIndex = selectedInputIndex;
					Intent intent = new Intent(currentActivity, DropboxFilePickerActivity.class);
					intent.putExtra("inputPortName", currentInputName);
					startActivityForResult(intent, REQUEST_PICK_DROPBOX_FILE);
				}
			});

			return convertView;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode != RESULT_OK) {
			return;
		}
		
		boolean fileSelected = false;
		switch(requestCode) {
			case REQUEST_PICK_FILE:
				if(data.hasExtra(FilePickerActivity.FILE_PATH)) {
					// Get the file URI
					Uri fileUri = Uri.fromFile(
							new File(data.getStringExtra(FilePickerActivity.FILE_PATH)));
					java.net.URI fileURI = null;
					try {
						fileURI = new java.net.URI(fileUri.toString());
					} catch (URISyntaxException e) {
						// TODO: need to handle...
						e.printStackTrace();
					}
					File selectedFile = new File(fileURI);
					fileSelected = selectedFile == null ? false : true;
					// setup data to upload
					if(fileSelected){
						userInputs.put(currentInputName, selectedFile);
					}
				}
				break;
			case REQUEST_PICK_DROPBOX_FILE:
				if(data.hasExtra(FilePickerActivity.FILE_PATH)) {
					DropboxInputFile dbFile = new DropboxInputFile();
					dbFile.setPath(data.getStringExtra(FilePickerActivity.FILE_PATH));
					userInputs.put(currentInputName, dbFile);
					fileSelected = true;
				}
				break;
		}
		
		if(fileSelected){
			// update screen display
			String fileName = data.getStringExtra(FilePickerActivity.SELECTED_FILE_NAME);
			HashMap<String, String> newInputNameSelFilePair = new HashMap<String, String>();
			newInputNameSelFilePair.put(currentInputName, fileName);
			listData.set(inputsListSelectedIndex, newInputNameSelFilePair);
			resultListAdapter.notifyDataSetChanged();
		}
	}

	private void preparingListData(ArrayList<String> inputNames){
		for(String i : inputNames){
			HashMap<String, String> pair = new HashMap<String, String>();
			pair.put(i, null);
			// data neede for run
			userInputs.put(i, null);
			// data used to display
			listData.add(pair);
		}
	}

	private String inputCheck(Map<String, Object> userInputs){
		String unSetInputName = null;
		if (userInputs != null){
			Iterator<Entry<String, Object>> it = userInputs.entrySet().iterator();
			search:
				while(it.hasNext()){
					Map.Entry<String, Object> pair = it.next();
					if(pair.getValue() == null){
						unSetInputName = pair.getKey();
						break search;
					}
				}
		}

		return unSetInputName;
	}
	
	private class DropboxInputFile{
		private String path;

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}
	}
	
	private String getFileSaveLocation(String subPath) {

		String storeLocation = null;

		/**** store in SD card ****/
		File root = android.os.Environment.getExternalStorageDirectory();               

		//try to avoid folder name syntax error
		//by using simpler start time as folder name
		File dir = new File (root.getAbsolutePath() + subPath);
		if(dir.exists() == false) 
		{
			if(dir.mkdirs()){
				storeLocation = dir.getAbsolutePath();
				return storeLocation;
			}
			else{
				Toast.makeText(
						currentActivity,
						"Output can't be saved to external storage",
						Toast.LENGTH_LONG).show();
				return null;
			}
		}

		// if the directory does exist
		storeLocation = dir.getAbsolutePath();

		return storeLocation;
	}
}

package cs.man.ac.uk.tavernamobile.io;

import java.io.File;
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
import android.widget.ListView;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowBE;
import cs.man.ac.uk.tavernamobile.server.WorkflowRunManager;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;

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
	private InputsListAdaptor resultListAdapter;
	
	// collection to store all user inputs.
	// this is data needed for the run
	public HashMap<String, Object> userInputs = new HashMap<String, Object>();
	// store the name of the input that user is 
	// currently selecting the input file for
	private String currentInputName;
	private int inputsListSelectedIndex;

	private int Activity_Starter_Code;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inputs);
		
		manager = new WorkflowRunManager(this);
		
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

		currentActivity = this; // for access of this activity inside OnClickListner
		runButton.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(android.view.View v) {
				SystemStatesChecker sysChecker = new SystemStatesChecker(currentActivity);
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
				startTheRun();
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
									public Object onTaskInProgress(Object... param) {
										return null;
									}
	
									@Override
									public Object onTaskComplete(Object... result) {
										if(result == null || result.length < 1){
											return null;
										}
										// in case there are any message returned 
										// during the starting of workflow run
										if(result[0] instanceof String){
											final String message = (String) result[0];
											
											MessageHelper.showMessageDialog(
												currentActivity, null, 
												(String) result[0], new CallbackTask(){
													@Override
													public Object onTaskInProgress(Object... param) {
														if(message.equals("The Run has been successfully started.")){
															currentActivity.finish();
														}
														return null;
													}

													@Override
													public Object onTaskComplete(Object... result) { return null; }
												});
										}
										return null;
									}
								},
								false);
							return null;
						}
	
						@Override
						public Object onTaskComplete(Object... result) {return null;}
					});
			}// end of startTheRun
		});

		cancel.setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View v) {
				currentActivity.finish();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
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

	private class InputsListAdaptor extends BaseAdapter {

		private LayoutInflater myInflater;
		private ArrayList<Map<String, String>> data;

		private TextView inputNameView;

		public InputsListAdaptor(LayoutInflater layoutInflater, ArrayList<Map<String, String>> listData)
		{
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

			final int selectedInputIndex = position;
			if (convertView == null)
			{
				convertView = myInflater.inflate(R.layout.inputs_single_row, null);
			}

			inputNameView = (TextView) convertView.findViewById(R.id.inputName);
			TextView fileName = (TextView) convertView.findViewById(R.id.selectedFileName);

			Map<String, String> nameFilePair = (Map<String, String>) getItem(position);
			String inputName = nameFilePair.keySet().iterator().next();
			inputNameView.setText(inputName);
			String selfileName = nameFilePair.get(inputName);
			if (selfileName != null){
				fileName.setText(selfileName);
			}

			EditText inputValue = (EditText) convertView.findViewById(R.id.inputValueText);
			inputValue.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener()
			{
				public void onFocusChange(View v, boolean hasFocus)
				{
					if (!hasFocus){
						final EditText inputText = (EditText) v;
						String textInput = inputText.getText().toString();
						userInputs.put(inputNames.get(selectedInputIndex), textInput);
					}
				}
			});

			Button fileSelectButton = (Button) convertView.findViewById(R.id.fileSelectButton);
			fileSelectButton.setOnClickListener(new android.view.View.OnClickListener(){

				public void onClick(View v) {
					currentInputName = inputNames.get(selectedInputIndex);
					inputsListSelectedIndex = selectedInputIndex;
					Intent intent = new Intent(currentActivity, FilePickerActivity.class);
					intent.putExtra("inputPortName", currentInputName);
					startActivityForResult(intent, REQUEST_PICK_FILE);
				}
			});

			return convertView;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		File selectedFile = null;
		if(resultCode == RESULT_OK) {
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
					selectedFile = new File(fileURI);
				}
			}
		}
		if(selectedFile != null){
			// setup data to upload
			userInputs.put(currentInputName, selectedFile);
			// setup screen display
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
}

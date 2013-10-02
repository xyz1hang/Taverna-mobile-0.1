package cs.man.ac.uk.tavernamobile.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowBE;
import cs.man.ac.uk.tavernamobile.server.WorkflowLaunchHelper;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;

public class InputsHistoryActivity extends FragmentActivity {

	private static String INITIAL_DIRECTORY;
	private File mDirectory;
	private List<File> mFiles;
	private List<Boolean> checkboxesStates;
	private InputsHistoryListAdapter mAdapter;

	private FragmentActivity currentActivity;
	protected ActionMode mActionMode;
	private TextView defaultText;
	private ListView inputsList;
	private TextView wfHistoryStatistics;
	
	private List<File> selectedInputs;
	private WorkflowBE workflowEntity;
	
	private int Activity_Starter_Code;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_inputs_history);

		currentActivity = this;
		selectedInputs = new ArrayList<File>();
		
		this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		
		// UI components
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("Inputs History");
		actionBar.setIcon(this.getResources().getDrawable(R.drawable.taverna_wheel_logo_medium));

		// TODO: temp replacement of statistic
		wfHistoryStatistics = (TextView) this.findViewById(R.id.wfHistoryStatistics);
		TextView wfHistoryWfTitle = (TextView) this.findViewById(R.id.wfHistoryWfTitle);
		inputsList = (ListView) findViewById(R.id.savedInputsList);
		defaultText = (TextView) findViewById(R.id.workflow_frag_default_textview);

		workflowEntity = (WorkflowBE) getIntent().getSerializableExtra("workflowEntity");
		Activity_Starter_Code = (Integer) getIntent().getIntExtra("Activity_Starter_Code", 1);

		if (workflowEntity != null) {
			wfHistoryWfTitle.setText(workflowEntity.getTitle());
			// get directory path
			File root = android.os.Environment.getExternalStorageDirectory();
			String inputsSubPath = "/TavernaAndroid/Inputs/"
					+ workflowEntity.getTitle().replace(" ", "") + "_"
					+ workflowEntity.getVersion() + "_"
					+ workflowEntity.getUploaderName().replace(" ", "") + "/";
			INITIAL_DIRECTORY = root.getAbsolutePath() + inputsSubPath;
			mDirectory = new File(INITIAL_DIRECTORY);
			mFiles = new ArrayList<File>();
			checkboxesStates = new ArrayList<Boolean>();
			mAdapter = new InputsHistoryListAdapter(mFiles);
			inputsList.setAdapter(mAdapter);
		}
	}

	@Override
	protected void onResume() {
		resetCheckboxesStates();
		if(mActionMode != null){
			mActionMode.finish();
		}
		refreshFilesList();
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		finish();
		super.onBackPressed();
	}
	
	@Override
	protected void onPause(){
		resetCheckboxesStates();
		if(mActionMode != null){
			mActionMode.finish();
		}
		this.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		super.onPause();
	}
	
	@Override
	public void finish(){
		this.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		super.finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.input_history_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.input_history_relaunch:
			MessageHelper.showOptionsDialog(currentActivity,
					"Do you want to launch the workflow again ?", null, 
					new CallbackTask() {
						@Override
						public Object onTaskInProgress(Object... param) {
							SystemStatesChecker sysChecker = new SystemStatesChecker(currentActivity);
							if (!(sysChecker.isNetworkConnected())) {
								return true;
							}
							WorkflowLaunchHelper launchHelper = 
									new WorkflowLaunchHelper(currentActivity, Activity_Starter_Code);
							launchHelper.launch(workflowEntity, 0);
							return null;
						}

						@Override
						public Object onTaskComplete(Object... result) {
							return null;
						}
					},null);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class InputsHistoryListAdapter extends BaseAdapter {

		private List<File> fileName;

		public InputsHistoryListAdapter(List<File> data) {
			fileName = data;
		}

		@Override
		public int getCount() {
			return fileName.size();
		}

		@Override
		public Object getItem(int position) {
			return fileName.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) currentActivity
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(
						R.layout.main_inputs_history_singlerow, parent, false);
			}

			final File file = (File) getItem(position);

			TextView fileName = (TextView) convertView.findViewById(R.id.savedInputFileName);
			CheckBox inputHisCheckbox = (CheckBox) convertView.findViewById(R.id.savedInputCheckBox);
			inputHisCheckbox.setChecked(checkboxesStates.get(position));
			inputHisCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						int fileIndex = mFiles.indexOf(file);
						if (isChecked) {
							// set check box states in the state collection
							checkboxesStates.set(fileIndex, true);
							selectedInputs.add(file);
							if (mActionMode == null) {
								mActionMode = currentActivity.startActionMode(mActionModeCallback);
							}
						} else {
							checkboxesStates.set(fileIndex, false);
							selectedInputs.remove(file);
							if (selectedInputs.size() < 1) {
								mActionMode.finish();
								mActionMode = null;
							}
						}
					}
				});

			fileName.setSingleLine(true);
			fileName.setText(file.getName().split("\\.")[0]);
			
			convertView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					String message = null;
					try {
						message = readFile(file);
					} catch (IOException e) {
						e.printStackTrace();
						message = "Fail to read saved input.";
					}
					MessageHelper.showMessageDialog(currentActivity, "Input detail", message, null);	
				}
			});

			return convertView;
		}
		
		String readFile(File file) throws IOException {
			FileInputStream fis = new FileInputStream(file);
		    ObjectInputStream ois = new ObjectInputStream(fis);
		    HashMap<String, Object> savedInputs = null;
			try {
				savedInputs = (HashMap<String, Object>) ois.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return "Fail to read saved input";
			} finally {
				fis.close();
				ois.close();
		    }
		    
		    // setup(upload) input
		    Iterator<Entry<String, Object>> it = savedInputs.entrySet().iterator();
		    String messageToReturn = "";
			while(it.hasNext()){
				Entry<String, Object> pair = it.next();
				Object value = pair.getValue();
				if(value instanceof String){
					messageToReturn += pair.getKey() + " = " + value + "\n";
				}else if(value instanceof File){
					messageToReturn += pair.getKey() + " = " + ((File)value).getAbsolutePath() + "\n";
				}
				
			}
			return messageToReturn;
		}
	}

	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.input_history_action_mode_menu, menu);
			return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.input_history_launch:
				MessageHelper.showOptionsDialog(currentActivity,
					"Launch the workflow with selected inputs ?", "Alert", 
					new CallbackTask() {
						@Override
						public Object onTaskInProgress(Object... param) {
							// set input history files path of the workflow entity
							// which will be used to load previous inputs
							List<String> paths = new ArrayList<String>();
							for(File f : selectedInputs){
								paths.add(f.getAbsolutePath());
							}
							workflowEntity.setSavedInputsFilesPath(paths);
							
							WorkflowLaunchHelper launchHelper = 
									new WorkflowLaunchHelper(currentActivity, Activity_Starter_Code);
							launchHelper.registerLaunchListener(new CallbackTask(){
								@Override
								public Object onTaskInProgress(Object... param) { return null; }

								@Override
								public Object onTaskComplete(Object... result) {
									if(result != null && result.length > 0 && 
											result[0] instanceof String){
										MessageHelper.showMessageDialog(
											currentActivity, null, (String) result[0], 
											new CallbackTask(){
												@Override
												public Object onTaskInProgress(Object... param) {
													currentActivity.finish();
													return null;
												}
	
												@Override
												public Object onTaskComplete(Object... result) 
												{ return null; }
											});
									}
									return null;
								}
							});
							launchHelper.launch(workflowEntity, 1);
							// exit action mode
							mode.finish();
							return null;
						}

						@Override
						public Object onTaskComplete(Object... result) { return null; }
					}, null);
				
				return true;
			case R.id.input_history_delete:
				MessageHelper.showOptionsDialog(currentActivity,
						"Delete selected inputs ?", null, new CallbackTask() {

							@Override
							public Object onTaskInProgress(Object... param) {
								for(File f : selectedInputs){
									f.delete();
								}
								mode.finish();
								return null;
							}

							@Override
							public Object onTaskComplete(Object... result) {
								refreshFilesList();
								return null;
							}
						}, null);
				
				return true;
			default:
				return false;
			}
		}

		// Called when the user exits the action mode
		public void onDestroyActionMode(ActionMode mode) {
			resetCheckboxesStates();
		}
	};

	protected void refreshFilesList() {
		mFiles.clear();
		checkboxesStates.clear();
		// Set the file extension filter
		ExtensionFilenameFilter filter = new ExtensionFilenameFilter(new String[] { ".tai" });

		// Get the files in the directory
		File[] files = mDirectory.listFiles(filter);
		if (files != null && files.length > 0) {
			for (File f : files) {
				mFiles.add(f);
				// Initialize checkboxes state in the same time
				checkboxesStates.add(false);
			}
			
			Collections.sort(mFiles, new FileComparator());
			inputsList.setVisibility(0);
			defaultText.setVisibility(8);
			wfHistoryStatistics.setVisibility(0);
			wfHistoryStatistics.setText("Select from following previous inputs to launch the workflow again.");
		}else{
			defaultText.setVisibility(0);
			inputsList.setVisibility(8);
			wfHistoryStatistics.setVisibility(8);
		}
		mAdapter.notifyDataSetChanged();
	}

	private void resetCheckboxesStates() {
		for(int i = 0; i < checkboxesStates.size(); i++){
			checkboxesStates.set(i, false);
		}
		mAdapter.notifyDataSetChanged();
	}
	
	private class FileComparator implements Comparator<File> {
		public int compare(File f1, File f2) {
			String f1time = f1.getName();
			String f2time = f2.getName();
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
			Date f1date = null; 
			Date f2date = null;
			try {
				f1date = sdf.parse(f1time);
				f2date = sdf.parse(f2time);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (f1date.before(f2date)) {
				return 1;
			}
			if (f2date.before(f1date)){
				return -1;
			}
			// Sort the directories alphabetically
			return f1.getName().compareToIgnoreCase(f2.getName());
		}
	}

	private class ExtensionFilenameFilter implements FilenameFilter {
		private String[] mExtensions;

		public ExtensionFilenameFilter(String[] extensions) {
			super();
			mExtensions = extensions;
		}

		public boolean accept(File dir, String filename) {
			if (new File(dir, filename).isDirectory()) {
				// Accept all directory names
				return true;
			}
			if (mExtensions != null && mExtensions.length > 0) {
				for (int i = 0; i < mExtensions.length; i++) {
					if (filename.endsWith(mExtensions[i])) {
						// The filename ends with the extension
						return true;
					}
				}
				// The filename did not match any of the extensions
				return false;
			}
			// No extensions has been set. Accept all file extensions.
			return true;
		}
	}
}

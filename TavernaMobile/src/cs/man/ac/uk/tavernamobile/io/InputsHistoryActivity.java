package cs.man.ac.uk.tavernamobile.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import android.view.View.OnLongClickListener;
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

		inputsList = (ListView) findViewById(R.id.savedInputsList);
		defaultText = (TextView) findViewById(R.id.workflow_frag_default_textview);

		workflowEntity = (WorkflowBE) getIntent().getSerializableExtra("workflowEntity");
		Activity_Starter_Code = (Integer) getIntent().getIntExtra("Activity_Starter_Code", 1);

		if (workflowEntity != null) {
			
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
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
						} else {
							checkboxesStates.set(fileIndex, false);
							selectedInputs.remove(file);
						}
						
						// start the action mode when there are 
						// selected input file
						if (mActionMode == null) {
							mActionMode = currentActivity.startActionMode(mActionModeCallback);
						} else if (selectedInputs.size() < 1) {
							mActionMode.finish();
						}
					}
				});

			convertView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					mActionMode = currentActivity.startActionMode(mActionModeCallback);
					return true;
				}
			});

			fileName.setSingleLine(true);
			fileName.setText(file.getName());

			return convertView;
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
			mActionMode = null;
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
		}else{
			defaultText.setVisibility(0);
			inputsList.setVisibility(8);
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
			if (f1 == f2) {
				return 0;
			}
			if (f1.isDirectory() && f2.isFile()) {
				// Show directories above files
				return -1;
			}
			if (f1.isFile() && f2.isDirectory()) {
				// Show files below directories
				return 1;
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

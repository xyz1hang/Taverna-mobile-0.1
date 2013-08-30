package cs.man.ac.uk.tavernamobile.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.MainActivity;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.dataaccess.DataProviderConstants;
import cs.man.ac.uk.tavernamobile.dataaccess.DatabaseLoader;
import cs.man.ac.uk.tavernamobile.server.WorkflowLaunchHelper;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;
import cs.man.ac.uk.tavernamobile.utils.WorkflowBE;

public class WorkflowsFragment extends Fragment {

	private MainActivity parentActivity;
	private int Activity_Starter_Code;

	private String selectedTitle = null;
	private String selectedWfVersion = null;
	private String selectedWfUploaderName = null;

	private LinearLayout root;
	private ListView savedWfList;
	private TextView fragWfDesc;
	private TextView defaultTextView;

	private SavedWorkflowListAdapter mAdapter;

	// saved workflows list loader
	private static final int loaderId = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View wfFragmentsView = inflater.inflate(R.layout.main_workflows,
				container, false);

		fragWfDesc = (TextView) wfFragmentsView.findViewById(R.id.fragWfDesc);
		defaultTextView = (TextView) wfFragmentsView
				.findViewById(R.id.workflow_frag_default_textview);
		root = (LinearLayout) wfFragmentsView
				.findViewById(R.id.savedWorkflowListRoot);
		savedWfList = (ListView) wfFragmentsView
				.findViewById(R.id.savedWorkflowList);

		return wfFragmentsView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Activity_Starter_Code = 2;
		parentActivity = (MainActivity) getActivity();
	}

	@Override
	public void onStart() {
		super.onStart();
		prepareData();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	/*private class RunStateChecker implements CallbackTask {

		private String wftitle;
		private String runId;

		public RunStateChecker(String workflowTitle, String id) {
			wftitle = workflowTitle;
			runId = id;
		}

		public Object onTaskInProgress(Object... param) {
			return null;
		}

		public Object onTaskComplete(Object... result) {
			String runState = (String) result[0];
			// if running or finished go to monitor to view progress or output
			if (runState == "Running" || runState == "Finished") {
				// go to monitor
				Intent goToMonitor = new Intent(parentActivity,
						RunMonitorScreen.class);
				Bundle extras = new Bundle();
				extras.putString("workflow_title", wftitle);
				extras.putString("command", "MonitoringOnly");
				goToMonitor.putExtras(extras);
				parentActivity.startActivity(goToMonitor);
			} else if (runState == "Initialised") {
				WorkflowRunManager manager = new WorkflowRunManager(parentActivity);
				manager.getRunInputs(runId, new GoToInputs(wftitle));
			} else {
				showLaunchDialog("Do you want to launch this workflow ?");
			}

			return null;
		}
	}*/

	/*private class GoToInputs implements CallbackTask {

		private String wftitle;

		public GoToInputs(String workflowTitle) {
			wftitle = workflowTitle;
		}

		@Override
		public Object onTaskInProgress(Object... param) {
			return null;
		}

		@Override
		public Object onTaskComplete(Object... result) {
			Map<String, InputPort> inputPorts = (Map<String, InputPort>) result[0];
			WorkflowBE entity = new WorkflowBE();
			entity.setTitle(wftitle);
			WorkflowLaunchHelper launchHelper = new WorkflowLaunchHelper(
					parentActivity, entity, Activity_Starter_Code);
			launchHelper.prepareInputs(inputPorts);
			return null;
		}
	}*/

	private void showLaunchDialog(String message) {
		MessageHelper.showOptionsDialog(parentActivity, message, "Attention",
				new CallbackTask() {
					@Override
					public Object onTaskInProgress(Object... param) {
						// check Internet
						SystemStatesChecker sysChecker = new SystemStatesChecker(parentActivity);
						if (!sysChecker.isNetworkConnected()) {
							return null;
						}
						reRun();
						return null;
					}

					@Override
					public Object onTaskComplete(Object... result) { return null; }
				}, null);
	}

	private void reRun() {
		WorkflowBE workflowEntity = new WorkflowBE();
		workflowEntity.setTitle(selectedTitle);
		workflowEntity.setVersion(selectedWfVersion);
		workflowEntity.setUploaderName(selectedWfUploaderName);

		WorkflowLaunchHelper launchHelper = new WorkflowLaunchHelper(
				parentActivity, workflowEntity, Activity_Starter_Code);
		launchHelper.launch();
	}

	private void prepareData() {

		String[] projection = new String[] {
				DataProviderConstants.WorkflowTitle,
				DataProviderConstants.Version,
				DataProviderConstants.UploaderName,
				DataProviderConstants.Avatar};

		Bundle loaderArgs = new Bundle();
		loaderArgs.putStringArray("projection", projection);
		loaderArgs.putString("tableURI", DataProviderConstants.WF_TABLE_CONTENTURI.toString());

		// create CursorLoader
		getLoaderManager().restartLoader(
				loaderId,
				loaderArgs,
				new DatabaseLoader(parentActivity,
						new SavedWorkflowDataLoadingListener()));
	}

	// class to handle data loading results
	private class SavedWorkflowDataLoadingListener implements
			CallbackTask {

		@Override
		public Object onTaskInProgress(Object... param) {
			return null;
		}

		@Override
		public Object onTaskComplete(Object... result) {
			if (!(result[0] instanceof Cursor)) {
				return null;
			}

			final ArrayList<WorkflowBE> savedWorkflows = new ArrayList<WorkflowBE>();

			Cursor allRecords = (Cursor) result[0];

			// have to move the cursor to point to the
			// first row for every query after the first one (same content loader?)
			if (allRecords.moveToFirst()) {
				do {
					String title = allRecords
							.getString(allRecords
									.getColumnIndexOrThrow(DataProviderConstants.WorkflowTitle));
					String version = allRecords
							.getString(allRecords
									.getColumnIndexOrThrow(DataProviderConstants.Version));
					String username = allRecords
							.getString(allRecords
									.getColumnIndexOrThrow(DataProviderConstants.UploaderName));
					/*String wfuri = allRecords
							.getString(allRecords
									.getColumnIndexOrThrow(DataProviderConstants.WorkflowUri));*/
					byte[] avatorData = allRecords
							.getBlob(allRecords
									.getColumnIndexOrThrow(DataProviderConstants.Avatar));
					Bitmap avatorBitmap = BitmapFactory.decodeByteArray(
							avatorData, 0, avatorData.length);
					/*String runID = allRecords
							.getString(allRecords
									.getColumnIndexOrThrow(DataProviderConstants.Run_Id));*/

					WorkflowBE savedWorkflow = new WorkflowBE();
					savedWorkflow.setTitle(title);
					savedWorkflow.setUploaderName(username);
					savedWorkflow.setVersion(version);
					savedWorkflow.setAvator(avatorBitmap);
					//savedWorkflow.setWorkflow_URI(wfuri);
					//savedWorkflow.setRunID(runID);

					savedWorkflows.add(savedWorkflow);
				} while (allRecords.moveToNext());
			}

			// refresh the list
			if (savedWorkflows != null && savedWorkflows.size() > 0) {
				root.setVisibility(0);
				defaultTextView.setVisibility(8);
				int count = savedWorkflows.size();
				if (count > 1) {
					fragWfDesc.setText("There are " + count
							+ " saved workflows : ");
				} else {
					fragWfDesc.setText("There is " + count
							+ " saved workflow : ");
				}
				fragWfDesc.setPadding(5, 0, 0, 0);
				fragWfDesc.setTextSize(14);

				mAdapter = new SavedWorkflowListAdapter(parentActivity, savedWorkflows);
				savedWfList.setAdapter(mAdapter);
				mAdapter.notifyDataSetChanged();
			}

			savedWfList.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View arg1,
						int itemIndex, long arg3) {

					final WorkflowBE workflowEntity = savedWorkflows.get(itemIndex);
					selectedTitle = workflowEntity.getTitle();
					selectedWfVersion = workflowEntity.getVersion();
					selectedWfUploaderName = workflowEntity.getUploaderName();
					
					showLaunchDialog("Do you want to launch this workflow ?");
					/*MessageHelper.showOptionsDialog(parentActivity, "Do you want to launch this workflow ?", null, new CallbackTask(){

						@Override
						public Object onTaskInProgress(Object... param) {
							// check Internet
							SystemStatesChecker sysChecker = new SystemStatesChecker(parentActivity);
							if (!sysChecker.isNetworkConnected()) {
								return null;
							}

							WorkflowLaunchHelper launchHelper = new WorkflowLaunchHelper(
									parentActivity, workflowEntity, Activity_Starter_Code);
							launchHelper.launch();
							return null;
						}

						@Override
						public Object onTaskComplete(Object... result) {
							// TODO Auto-generated method stub
							return null;
						}
						
					}, null);*/
					

					// get launched workflows run ID
					// in order to retrieve its state
					// and then monitor it
					/*String runID = savedWorkflows.get(itemIndex).getRunID();
					if (runID != null) {
						WorkflowRunManager manager = new WorkflowRunManager(
								parentActivity, null);
						manager.checkRunStateWithID(runID, new RunStateChecker(
								selectedTitle, runID));
					} else {
						// A run of this workflow has been attempted
						// i.e it has been recorded
						// but the run creation was unsuccessful
						showLaunchDialog("There was a problem launching this workflow."
										+"\nDo you want to try again ?");
					}*/
				}
			});

			return null;
		}
	}

	private class SavedWorkflowListAdapter extends ArrayAdapter<WorkflowBE> {

		private List<WorkflowBE> mData;

		public SavedWorkflowListAdapter(Context context,
				List<WorkflowBE> objects) {
			super(context, R.layout.main_workflows_single_row,
					android.R.id.text1, objects);
			mData = objects;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public WorkflowBE getItem(int position) {
			return mData.get(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View row = null;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.main_workflows_single_row,
						parent, false);
			} else {
				row = convertView;
			}

			ImageView avatar = (ImageView) row
					.findViewById(R.id.savedAvatarImage);
			TextView title = (TextView) row
					.findViewById(R.id.savedWorkflowTitle);
			TextView userName = (TextView) row
					.findViewById(R.id.savedUploaderName);
			TextView version = (TextView) row.findViewById(R.id.savedWfVersion);

			WorkflowBE object = getItem(position);

			avatar.setImageBitmap(object.getAvator());
			title.setText(object.getTitle());
			userName.setText(object.getUploaderName());
			version.setText("version: " + object.getVersion());

			return row;
		}
	}
}

package cs.man.ac.uk.tavernamobile.fragments;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.datamodels.Workflow;
import cs.man.ac.uk.tavernamobile.myexperiment.WorkflowsLoader;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;
import cs.man.ac.uk.tavernamobile.utils.WorkflowsListAdapter;

public class MyWorkflowsBase extends Fragment {

	protected FragmentActivity parentActivity;
	protected View myWorkflowsMainView;
	protected TextView defaultText;
	protected PullToRefreshListView workflowList;
	protected WorkflowsListAdapter resultListAdapter;
	protected ArrayList<Workflow> workflows;
	protected HashMap<String, Object> mCache;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		myWorkflowsMainView = inflater.inflate(R.layout.my_workflows, container, false);
		
		defaultText = (TextView) myWorkflowsMainView.findViewById(R.id.myWorkflowDefaultText);
		
		TextView barText = (TextView) myWorkflowsMainView.findViewById(R.id.myWorkflowText);
		barText.setText("Pull to refresh");
		
		TextView noticeText = (TextView) myWorkflowsMainView.findViewById(R.id.myWorkflowNoticeText);
		noticeText.setText("Note: some items may not be visible to you, due to viewing permissions.");
		
		workflowList = (PullToRefreshListView) myWorkflowsMainView.findViewById(R.id.myWorkflowList);
		return myWorkflowsMainView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		parentActivity = getActivity();
		mCache = TavernaAndroid.getmTextCache();
	}
	
	protected void refreshTheList(final CallbackTask refreshListener) {
		// load user workflows and user's favourite workflows
		WorkflowsLoader wfListLoader = new WorkflowsLoader(parentActivity, new CallbackTask(){
			@Override
			public Object onTaskInProgress(Object... param) {
				return null;
			}

			@Override
			public Object onTaskComplete(Object... result) {
				String responseMessage = (String) result[0];
				if (responseMessage != null) {
					if(responseMessage.equals("no results")){
						defaultText.setText("No workflow data found");
					}else{
						defaultText.setText("Fail to load workflows data, please try again.");
					}
					return null;
				}
				if(workflows == null){
					workflows = new ArrayList<Workflow>();
					resultListAdapter = new WorkflowsListAdapter(parentActivity, workflows);
					workflowList.setAdapter(resultListAdapter);
				}
				if(refreshListener != null) refreshListener.onTaskComplete();
				workflowList.onRefreshComplete();
				return null;
			}
		});
		wfListLoader.LoadMyWorkflows(TavernaAndroid.getMyEUserLoggedin().getId());
	}
}
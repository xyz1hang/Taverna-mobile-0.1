package cs.man.ac.uk.tavernamobile.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

import cs.man.ac.uk.tavernamobile.datamodels.Workflow;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;
import cs.man.ac.uk.tavernamobile.utils.WorkflowsListAdapter;

public class MyWorkflowsFragment extends MyWorkflowsBase {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		setHasOptionsMenu(true);
		return myWorkflowsMainView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// try to get from cache first
		workflows = (ArrayList<Workflow>) mCache.get("myWorkflow");
		if(workflows != null){
			resultListAdapter = new WorkflowsListAdapter(parentActivity, workflows);
			workflowList.setAdapter(resultListAdapter);
		}
		
		workflowList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>(){
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
            	refreshTheList(new CallbackTask(){
					@Override
					public Object onTaskInProgress(Object... param) {
						return null;
					}

					@Override
					public Object onTaskComplete(Object... result) {
						// change the data set of the listView adapter of super class
		        		workflows.clear();
		        		workflows.addAll(TavernaAndroid.getMyWorkflows());
		        		// cache the workflow
		        		mCache.put("myWorkflow", workflows);
		        		resultListAdapter.notifyDataSetChanged();
						return null;
					}
            	});
            }
        });
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		// remove menu added by previous fragment
		for(int i = 1; i < menu.size(); i ++){
			menu.removeItem(menu.getItem(i).getItemId());
		}
	}
}
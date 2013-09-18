package cs.man.ac.uk.tavernamobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

public class FavouriteWorkflowsFragment extends MyWorkflowsBase {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return myWorkflowsMainView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
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
		        		workflows.addAll(TavernaAndroid.getFavouriteWorkflows());
		        		resultListAdapter.notifyDataSetChanged();
						return null;
					}
            	});
            }
        });
	}
	
	/*@Override
	protected void refreshTheList() {
		super.refreshTheList();
		ArrayList<Workflow> favouriteWorkflows = TavernaAndroid.getFavouriteWorkflows();
		if(favouriteWorkflows == null){
			// start fetching workflow data
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(parentActivity, new MyWorkflowsDetailsFetcher(), null);
		}else{
			WorkflowsListAdapter resultListAdapter = new WorkflowsListAdapter(parentActivity, favouriteWorkflows);
			workflowList.setAdapter(resultListAdapter);
			// hide progress bar
			loadingProBar.setVisibility(8);
			workflowList.setVisibility(0);
			workflowList.onRefreshComplete();
		}
	}
	
	private class MyWorkflowsDetailsFetcher implements CallbackTask{

		@Override
		public Object onTaskInProgress(Object... param) {
			ArrayList<Workflow> myWorkflows = TavernaAndroid.getFavouriteWorkflows();
			while(myWorkflows == null){
				if(needRetry || noResults){
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				myWorkflows = TavernaAndroid.getFavouriteWorkflows();
			}
			return myWorkflows;
		}

		@Override
		public Object onTaskComplete(Object... result) {
			ArrayList<Workflow> myWorkflows = (ArrayList<Workflow>) result[0];
			if(myWorkflows == null){
				if(noResults){
					defaultText.setText("No workflow data found");
				}
				else if(needRetry){
					defaultText.setText("Fail to load workflows data, please try again.");
				}
				return null;
			}
			
			WorkflowsListAdapter resultListAdapter = new WorkflowsListAdapter(parentActivity, myWorkflows);
			workflowList.setAdapter(resultListAdapter);
			// hide progress bar
			loadingProBar.setVisibility(8);
			workflowList.setVisibility(0);
			workflowList.onRefreshComplete();
			return null;
		}	
	}*/
}
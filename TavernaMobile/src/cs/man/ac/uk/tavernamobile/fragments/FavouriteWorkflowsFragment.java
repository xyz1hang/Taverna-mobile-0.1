package cs.man.ac.uk.tavernamobile.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.datamodels.Workflow;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;
import cs.man.ac.uk.tavernamobile.utils.WorkflowsListAdapter;

public class FavouriteWorkflowsFragment extends Fragment {

	private FragmentActivity parentActivity;
	private ListView workflowList;
	private ProgressBar loadingProBar;
	private TextView myWorkflowText;
	
	// utilities
	//private WorkflowsLoader wfListLoader;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View searchView = inflater.inflate(R.layout.my_workflows, container, false);
		workflowList = (ListView) searchView.findViewById(R.id.myWorkflowList);
		loadingProBar = (ProgressBar) searchView.findViewById(R.id.myWorkflowLoadingProgressBar);
		myWorkflowText = (TextView) searchView.findViewById(R.id.myWorkflowText);
		return searchView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		parentActivity = getActivity();
		myWorkflowText.setText("Note: some items may not be visible to you, due to viewing permissions.");
		ArrayList<Workflow> myWorkflows = TavernaAndroid.getFavouriteWorkflows();
		// hide progress bar
		loadingProBar.setVisibility(8);
		workflowList.setVisibility(0);
		
		WorkflowsListAdapter resultListAdapter = 
				new WorkflowsListAdapter(parentActivity, myWorkflows);
		workflowList.setAdapter(resultListAdapter);
	}
	
	/*private void refreshTheList() {
		workflowList.setVisibility(8);
		workflowList.removeFooterView(footerView);
		loadingProBar.setVisibility(0);
		// set up a loader for loading indexed workflows
		wfListLoader = new WorkflowsLoader(parentActivity, new WorkflowExpoLoadingListener());
		wfListLoader.LoadWorkflows(expoSortBy, order);
	}*/
}
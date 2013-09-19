package cs.man.ac.uk.tavernamobile.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.MainPanelActivity;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.datamodels.Workflow;
import cs.man.ac.uk.tavernamobile.myexperiment.WorkflowsLoader;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.ListViewOnScrollTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.WorkflowsListAdapter;

public class ExploreFragment extends Fragment {

	private FragmentActivity parentActivity;
	private View footerView;
	private ListView expoList;
	// private ProgressBar loadingProBar;
	private CheckBox reverseRadioButton;
	private Spinner sortCriteriaSpinner;
	private Menu refreshMenu;
	private TextView wfListDefaultTest;
	
	// utilities
	private WorkflowsLoader wfListLoader;
	private ListViewOnScrollTaskHandler onScrollTaskHandler;
	
	private String expoSortBy;
	private String order = "reverse";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View expoView = inflater.inflate(R.layout.main_explore, container, false);
		// loadingProBar = (ProgressBar) expoView.findViewById(R.id.wfListLoadingProgressBar);
		expoList = (ListView) expoView.findViewById(R.id.workflowExpoList);
		reverseRadioButton = (CheckBox) expoView.findViewById(R.id.wfListSortOrderRadioButton);
		sortCriteriaSpinner = (Spinner) expoView.findViewById(R.id.wfListSortSpinner);
		wfListDefaultTest = (TextView) expoView.findViewById(R.id.wfList_default_textview);
		setHasOptionsMenu(true);
		return expoView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		parentActivity = getActivity();
		
		reverseRadioButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					order = null;
					refreshTheList();
				}
				else{
					order = "reverse";
					refreshTheList();
				}
			}
		});
		
		//ImageButton searchButton = (ImageButton) parentActivity.findViewById(R.id.exploreSearchButton);
		/*searchButton.setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View v) {

				Intent goToSearchResultScreen = new Intent(parentActivity, SearchResultScreen.class);
				parentActivity.startActivity(goToSearchResultScreen);
				searchQuery = searchQueryText.getText().toString();
				if (searchQuery.isEmpty()) {
					MessageHelper
							.showMessageDialog(parentActivity,
									"Oops! You haven't told me what you would like to search !");
				} else {
					// start with default search sorting and order
					wfSearchLoader.DoSearch(searchQuery, null, null, true);
				}
			}
		});*/

		ArrayAdapter<CharSequence> adapter = 
				ArrayAdapter.createFromResource(
						parentActivity, 
						R.array.wfList_sort_criteria, 
						android.R.layout.simple_spinner_item);
		// Sets the layout resource to create the drop down views
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sortCriteriaSpinner.setAdapter(adapter);
		sortCriteriaSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1, int itemIndex, long arg3) {
				// creation time (created), update time (updated), 
				// title (title) and name (name)
				switch(itemIndex){
				case 0:
					expoSortBy = "created";
					refreshTheList();
					break;
				case 1:
					expoSortBy = "updated";
					refreshTheList();
					break;
				case 2:
					expoSortBy = "title";
					refreshTheList();
					break;
				case 3:
					expoSortBy = "name";
					refreshTheList();
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		refreshMenu = menu;
		// remove menu added by previous fragment
		for(int i = 1; i < menu.size(); i ++){
			menu.removeItem(menu.getItem(i).getItemId());
		}
		parentActivity.getMenuInflater().inflate(R.menu.expo_menu, menu);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int menuId = item.getItemId();
		switch(menuId){
			case R.id.refresh_expo:
				refreshTheList();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
		
	}
	
	private void setRefreshIconState(boolean refreshing){
		MenuItem refreshItem = refreshMenu.findItem(R.id.refresh_expo);
		View mRefreshIndeterminateProgressView = null;
		if (refreshing) {
            LayoutInflater inflater = (LayoutInflater)parentActivity.getSystemService(
                            Context.LAYOUT_INFLATER_SERVICE);
            mRefreshIndeterminateProgressView = inflater.inflate(
                    R.layout.actionbar_progress_icon, null);
        }
		if(refreshItem != null){
			refreshItem.setActionView(mRefreshIndeterminateProgressView);
		}
	}

	private void refreshTheList() {
		setRefreshIconState(true);
		expoList.setVisibility(8);
		wfListDefaultTest.setVisibility(8);
		expoList.removeFooterView(footerView);
		//loadingProBar.setVisibility(0);
		// set up a loader for loading indexed workflows
		wfListLoader = new WorkflowsLoader(parentActivity, new WorkflowExpoLoadingListener());
		wfListLoader.LoadWorkflows(expoSortBy, order);
	}
	
	// class that handle the initial workflow data loaded by index request
	private class WorkflowExpoLoadingListener implements CallbackTask{
		
		@Override
		public Object onTaskInProgress(Object... param) { return null; }

		@Override
		public Object onTaskComplete(Object... result) {
			if(result[0] instanceof String){
				if(((String)result[0]).equals("No connection")){
					if (refreshMenu != null){
						setRefreshIconState(false);
					}
				}
				else{
					MessageHelper.showMessageDialog(
							parentActivity, "Attention", (String)result[0], null);
				}
				wfListDefaultTest.setVisibility(0);
				//loadingProBar.setVisibility(8);
				return null;
			}
			
			final ArrayList<Workflow> workflows = (ArrayList<Workflow>) result[0];
			if(workflows == null){
				wfListDefaultTest.setText("No workflow data found, please try again");
				wfListDefaultTest.setVisibility(0);
				//loadingProBar.setVisibility(8);
				return null;
			}
			// hide progress bar
			//loadingProBar.setVisibility(8);
			wfListDefaultTest.setVisibility(8);
			expoList.setVisibility(0);
			
			footerView = ((LayoutInflater) parentActivity
								.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
										.inflate(R.layout.list_footer_loading, null, false);
			expoList.addFooterView(footerView);
			
			WorkflowsListAdapter resultListAdapter = 
					new WorkflowsListAdapter(parentActivity, workflows);
			expoList.setAdapter(resultListAdapter);
			
			expoList.post(new Runnable() {
			    public void run() {
			    	if(workflows.size() <= expoList.getLastVisiblePosition()){
			    		expoList.removeFooterView(footerView);
			    	}
			    }
			});
			
			onScrollTaskHandler = new ListViewOnScrollTaskHandler(
							(MainPanelActivity)parentActivity, expoList, new OnScrollLoadingTask());
			onScrollTaskHandler.setOnScrollLoading();
			
			// the initial loading is finished 
			// now change to the "auto-load-more"
			WorkflowExpoAutoLoader autoloader = new WorkflowExpoAutoLoader(resultListAdapter);
			wfListLoader.registerLoadingListener(autoloader);
			
			if (refreshMenu != null){
				setRefreshIconState(false);
			}

			return null;
		}
	}
	
	// class that handle more workflow data loaded when user scroll
	private class WorkflowExpoAutoLoader implements CallbackTask{
		
		private WorkflowsListAdapter listAdaptor;

		public WorkflowExpoAutoLoader(WorkflowsListAdapter adaptor){
			listAdaptor = adaptor;
		}

		public Object onTaskInProgress(Object... param) {
			return null;
		}

		// loading results will gets passed here
		public Object onTaskComplete(Object... result) {
			ArrayList<Workflow> newResults = (ArrayList<Workflow>) result[0];
			int previousDataSize = listAdaptor.getCount();
			listAdaptor.animationStartPosition = 
					previousDataSize > 0 ? previousDataSize - 1 : 0;
			if(newResults != null && newResults.size() > 0){
				listAdaptor.AppendData(newResults);
				listAdaptor.notifyDataSetChanged();
				// onScrollTask is complete and release the lock
				onScrollTaskHandler.taskInProgress = false;
			}
			else{
				expoList.removeFooterView(footerView);
				// no more results to load so
				// so disable the onScrollTask
				onScrollTaskHandler.disableTask = true;
				MessageHelper.showMessageDialog(
						parentActivity,
						"Attention", "No more matching workflow found", null);
			}
			return null;
		}
	}
	
	// task to do when scroll to the end
	private class OnScrollLoadingTask implements CallbackTask{
		@Override
		public Object onTaskInProgress(Object... param) {
			wfListLoader.LoadWorkflows(expoSortBy, order);
			return null;
		}

		@Override
		public Object onTaskComplete(Object... result) {
			return null;
		}
	}
}
package cs.man.ac.uk.tavernamobile.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.SearchResultScreen;
import cs.man.ac.uk.tavernamobile.datamodels.Workflow;
import cs.man.ac.uk.tavernamobile.myexperiment.WorkflowsLoader;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.ListViewOnScrollTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.WorkflowExpoListAdapter;

public class ExploreFragment extends Fragment implements CallbackTask {

	private Activity parentActivity;
	//private TextView myExperimentLoginText;
	private View footerView;
	private ListView expoList;
	private ProgressBar loadingProBar;
	
	// utilities
	// private WorkflowsLoader wfSearchLoader;
	private WorkflowsLoader wfExpoLoader;
	private ListViewOnScrollTaskHandler onScrollTaskHandler;
	
	private String searchQuery;
	private String expoSortBy;
	private String order = "reverse";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View searchView = inflater.inflate(R.layout.main_explore, container, false);
		return searchView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		parentActivity = getActivity();
		// set up a loader for search
		// wfSearchLoader = new WorkflowsLoader(parentActivity, this);
		
		// UI components
		loadingProBar = (ProgressBar)parentActivity.findViewById(R.id.wfExpoLoadingProgressBar);
		expoList = (ListView) parentActivity.findViewById(R.id.workflowExpoList);
		CheckBox reverseRadioButton = 
				(CheckBox) parentActivity.findViewById(R.id.wfExpoSortOrderRadioButton);
		reverseRadioButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
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
		
		ImageButton searchButton = (ImageButton) parentActivity.findViewById(R.id.exploreSearchButton);
		searchButton.setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View v) {

				Intent goToSearchResultScreen = new Intent(parentActivity, SearchResultScreen.class);
				parentActivity.startActivity(goToSearchResultScreen);
				/*searchQuery = searchQueryText.getText().toString();
				if (searchQuery.isEmpty()) {
					MessageHelper
							.showMessageDialog(parentActivity,
									"Oops! You haven't told me what you would like to search !");
				} else {
					// start with default search sorting and order
					wfSearchLoader.DoSearch(searchQuery, null, null, true);
				}*/
			}
		});
		
		Spinner sortCriteriaSpinner = (Spinner) parentActivity.findViewById(R.id.wfExpoSortSpinner);
		ArrayAdapter<CharSequence> adapter = 
				ArrayAdapter.createFromResource(
						parentActivity, 
						R.array.wfExpo_sort_criteria, 
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
		
		/*final EditText searchQueryText = (EditText) parentActivity.findViewById(R.id.searchQueryText);
		ImageButton searchButton = (ImageButton) parentActivity.findViewById(R.id.searchButton);
		searchButton.setOnClickListener(new android.view.View.OnClickListener() {
					public void onClick(android.view.View v) {

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
		
		/*myExperimentLoginText = (TextView) getActivity().findViewById(R.id.myExperimentLoginState);
		myExperimentLoginText.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						User user = TavernaAndroid.getMyEUserLoggedin();
						if (user != null) {
							MessageHelper.showOptionsDialog(parentActivity,
									"Do you wish to log out ?", 
									"Attention",
									new CallbackTask() {

										@Override
										public Object onTaskInProgress(
												Object... param) {
											// Clear user logged-in and cookie
											TavernaAndroid.setMyEUserLoggedin(null);
											TavernaAndroid.setMyExperimentSessionCookies(null);
											refreshLoginState();
											return null;
										}

										@Override
										public Object onTaskComplete(
												Object... result) {
											// TODO Auto-generated method stub
											return null;
										}
									}, null);
						}
						else{
							Intent gotoMyexperimentLogin = new Intent(
									parentActivity, MyExperimentLogin.class);
							parentActivity.startActivity(gotoMyexperimentLogin);
						}
					}
				});*/
	}

	/*@Override
	public void onStart() {
		refreshLoginState();
		super.onStart();
	}

	private void refreshLoginState() {
		User userLoggedin = TavernaAndroid.getMyEUserLoggedin();
		String userName = null;
		if (userLoggedin != null) {
			userName = userLoggedin.getName();
			Bitmap avatarBitmap = 
			 	TavernaAndroid.getmMemoryCache().get(userLoggedin.getAvatar().getResource());
			if(avatarBitmap != null){
				Drawable avatarDrawable = new BitmapDrawable(getResources(),
						Bitmap.createBitmap(avatarBitmap));
				Rect outRect = new Rect();
				myExperimentLoginText.getDrawingRect(outRect);
				// resize the Rect
				//outRect.inset(-10, 10);
				avatarDrawable.setBounds(outRect);
				myExperimentLoginText.setCompoundDrawables(avatarDrawable, null, null, null);
			}
			myExperimentLoginText.setText("Logged in as: "+ userName);
		}else{
			myExperimentLoginText.setText("Log in to myExperiment");
		}
	}*/
	
	private void refreshTheList() {
		expoList.setVisibility(8);
		expoList.removeFooterView(footerView);
		loadingProBar.setVisibility(0);
		// set up a loader for loading indexed workflows
		wfExpoLoader = new WorkflowsLoader(parentActivity, new WorkflowExpoLoadingListener());
		wfExpoLoader.LoadWorkflows(expoSortBy, order);
	}

	public Object onTaskInProgress(Object... param) {
		return null;
	}

	public Object onTaskComplete(Object... result) {
		// if there was an (error) message
		if (result[0] instanceof String) {
			String message = (String) result[0];
			MessageHelper.showMessageDialog(parentActivity, message);
		} else {
			ArrayList<Workflow> workflowResults = (ArrayList<Workflow>) result[0];

			// If no element has been added into the list 
			// i.e search return no results
			if (workflowResults == null || workflowResults.size() < 1) {
				String dialogMessage = null;
				String errorMessage = (String)result[0];
				if (errorMessage != null) {
					dialogMessage = errorMessage;
				} else {
					dialogMessage = "No workflow found for " + "\""
							+ searchQuery + "\"";
				}
				MessageHelper.showMessageDialog(parentActivity, dialogMessage);
			} else {
				Intent intent = new Intent(parentActivity, SearchResultScreen.class);
				Bundle extras = new Bundle();
				extras.putSerializable("wfSearch_Result_list", workflowResults);
				extras.putString("searchQuery", this.searchQuery);
				intent.putExtras(extras);
				parentActivity.startActivity(intent);
			}
		}

		return null;
	}
	
	// class that handle the initial workflow data loaded by index request
	private class WorkflowExpoLoadingListener implements CallbackTask{
		
		@Override
		public Object onTaskInProgress(Object... param) { return null; }

		@Override
		public Object onTaskComplete(Object... result) {
			if(result[0] instanceof String){
				MessageHelper.showMessageDialog(parentActivity, (String)result[0]);
				return null;
			}
			
			ArrayList<Workflow> workflows = (ArrayList<Workflow>) result[0];
			if(workflows == null){
				return null;
			}
			// hide progress bar
			loadingProBar.setVisibility(8);
			expoList.setVisibility(0);
			
			footerView = 
					((LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.list_footer_loading, null, false);
			expoList.addFooterView(footerView);
			
			WorkflowExpoListAdapter resultListAdapter = 
					new WorkflowExpoListAdapter(parentActivity, workflows);
			expoList.setAdapter(resultListAdapter);
			
			onScrollTaskHandler = new ListViewOnScrollTaskHandler(expoList, new OnScrollLoadingTask());
			onScrollTaskHandler.setOnScrollLoading();
			
			// the initial loading is finished 
			// now change to the "auto-load-more"
			WorkflowExpoAutoLoader autoloader = new WorkflowExpoAutoLoader(resultListAdapter);
			wfExpoLoader.registerLoadingListener(autoloader);

			return null;
		}
	}
	
	// class that handle more workflow data loaded when user scroll
	private class WorkflowExpoAutoLoader implements CallbackTask{
		
		private WorkflowExpoListAdapter listAdaptor;

		public WorkflowExpoAutoLoader(WorkflowExpoListAdapter adaptor){
			listAdaptor = adaptor;
		}

		public Object onTaskInProgress(Object... param) {
			return null;
		}

		// loading results will gets passed here
		public Object onTaskComplete(Object... result) {
			ArrayList<Workflow> newResults = (ArrayList<Workflow>) result[0];
			
			if(newResults != null && newResults.size() > 0){
				int previousDataSize = listAdaptor.getCount();
				listAdaptor.animationStartPosition = 
						previousDataSize > 0 ? previousDataSize - 1 : 0;

				listAdaptor.AppendData(newResults);
				listAdaptor.notifyDataSetChanged();
				
				// onScrollTask is complete and 
				// release the lock
				onScrollTaskHandler.taskInProgress = false;
			}
			else{
				expoList.removeFooterView(footerView);
				// no more results to load so
				// so disable the onScrollTask
				onScrollTaskHandler.disableTask = true;
				MessageHelper.showMessageDialog(
						parentActivity,
						"No more matching workflow found");
			}
			return null;
		}
	}
	
	// task to do when scroll to the end
	private class OnScrollLoadingTask implements CallbackTask{
		@Override
		public Object onTaskInProgress(Object... param) {return null;}

		@Override
		public Object onTaskComplete(Object... result) {
			wfExpoLoader.LoadWorkflows(expoSortBy, order);
			return null;
		}
	}
}
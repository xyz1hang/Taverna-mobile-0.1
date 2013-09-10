package cs.man.ac.uk.tavernamobile;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.datamodels.Workflow;
import cs.man.ac.uk.tavernamobile.myexperiment.HttpRequestHandler;
import cs.man.ac.uk.tavernamobile.myexperiment.WorkflowsLoader;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.ListViewOnScrollTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.SearchResultListAdapter;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;

public class SearchResultScreen extends Activity implements CallbackTask {

	private ArrayList<Workflow> workflowResults;
	private SearchResultListAdapter resultListAdapter;
	private ListView resultList;
	private View footerView;
	private ProgressBar loadingProBar;
	//private TextView searchQueryQuote;

	private String searchQuery;
	private Activity currentActivity;
	private String sortedBy;
	private String order = "reverse";

	// utilities
	protected static HttpRequestHandler requestHandler;
	private WorkflowsLoader search;
	private InitialSearchResultHandler iniLoadingListener;
	private AutoLoadMoreListener moreResultsLoadingListener;
	private ListViewOnScrollTaskHandler onScrollTaskHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result_screen);
		
		this.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);

		currentActivity = this;
		// utilities object setup
		requestHandler = new HttpRequestHandler(this);
		iniLoadingListener = new InitialSearchResultHandler();
		moreResultsLoadingListener = new AutoLoadMoreListener();
		// case 1:
		// when navigate to result screen the loading mode should be
		// "auto-load more result" by
		// search = new WorkflowsLoader(currentActivity, moreResultsLoadingListener);
		
		// case 2:
		// navigate to result screen to do the search
		search = new WorkflowsLoader(currentActivity, iniLoadingListener);

		// Initialize view components
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#D02E2E2E")));
		actionBar.setTitle("Search");
		
		loadingProBar = (ProgressBar) currentActivity.findViewById(R.id.wfSearchProgressBar);
		// footer view to append to the list
		footerView = ((LayoutInflater) currentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.list_footer_loading, null, false);
		
		//searchQueryQuote = (TextView) findViewById(R.id.searchQueryQuote);
		resultList = (ListView) findViewById(R.id.searchResultList);

		/** case 1: **/
		// get data passed in
		// workflowResults = (ArrayList<Workflow>) getIntent().getSerializableExtra("wfSearch_Result_list");
		//searchQuery = getIntent().getStringExtra("searchQuery");
		
		//searchQueryQuote.setText("Search results for : \"" + searchQuery + "\"");
		// list adapter
		/*resultListAdapter = new SearchResultListAdapter(this, workflowResults);
		resultList.addFooterView(footerView);
		resultList.setAdapter(resultListAdapter);*/
		
		onScrollTaskHandler = new ListViewOnScrollTaskHandler(resultList, new OnScrollLoadingTask());
		onScrollTaskHandler.setOnScrollLoading();
		
		// setup event reaction -
		// tap on one of the result going to detail
		final SearchResultScreen currentClass = this;
		resultList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View arg1, int itemIndex, long arg3) {
				// hide the soft keyboard if it is shown
				hideKeyboard();
				
				// check for Internet connection
				SystemStatesChecker checker = new SystemStatesChecker(currentClass);
				if (!checker.isNetworkConnected()){
					return;
				}

				// Begin to load workflow details
				String selectedwfUri = workflowResults.get(itemIndex).getUri()
						+"&elements=id,title,description,type,uploader,"
						+"created-at,preview,license-type,content-uri,privileges";
				BackgroundTaskHandler handler = new BackgroundTaskHandler();
				handler.StartBackgroundTask(currentActivity, currentClass, "Retrieving data...", selectedwfUri);
			}
		});
		
		Spinner sortCriteriaSpinner = (Spinner) currentActivity.findViewById(R.id.wfSearchSortSpinner);
		ArrayAdapter<CharSequence> adapter = 
				ArrayAdapter.createFromResource(
						currentActivity, 
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
					sortedBy = "created";
					refreshTheList();
					break;
				case 1:
					sortedBy = "updated";
					refreshTheList();
					break;
				case 2:
					sortedBy = "title";
					refreshTheList();
					break;
				case 3:
					sortedBy = "name";
					refreshTheList();
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		
		CheckBox reverseRadioButton = 
				(CheckBox) currentActivity.findViewById(R.id.wfSearchSortOrderRadioButton);
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

		/*secondSearchButton.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(android.view.View v) {
				// hide the soft keyboard if it is shown
				hideKeyboard();
				// check search query and do the search
				searchQuery = resultListQuery.getText().toString();
				if (searchQuery.isEmpty()){
					MessageHelper.showMessageDialog(currentContext, 
							"Oops! You haven't told me what you would like to search !");
				}
				else{
					loader.initialize();
					// reset page index
					search.searchResultsPageCount = 1;
					search.DoSearch(searchQuery, true);
				}				
			}
		});*/
	}
	
	private void refreshTheList() {
		if(searchQuery == null){
			return;
		}
		resultList.setVisibility(8);
		resultList.removeFooterView(footerView);
		loadingProBar.setVisibility(0);
		// set up a loader for loading indexed workflows
		search.registerLoadingListener(iniLoadingListener);
		// reset page index
		search.searchResultsPageCount = 1;
		search.DoSearch(searchQuery, sortedBy, order, false);
	}
	
	private class OnScrollLoadingTask implements CallbackTask{
		@Override
		public Object onTaskInProgress(Object... param) {return null;}

		@Override
		public Object onTaskComplete(Object... result) {
			search.DoSearch(searchQuery, sortedBy, order, false);
			return null;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_results_screen, menu);
		LinearLayout searchView = 
				(LinearLayout) menu.findItem(R.id.search_results_search).getActionView();
	    
		final EditText query = (EditText)searchView.getChildAt(0);
		query.requestFocus();
		query.setOnEditorActionListener(
		        new EditText.OnEditorActionListener() {
		            @Override
		            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		                	search(query);
		                    return true;
		                }
		                return false;
		            }
		        });
		ImageButton searchButton = (ImageButton) searchView.getChildAt(1); 
		
		searchButton.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(android.view.View v) {
				search(query);				
			}
		});
		return true; 
	}
	
	private void search(final EditText query) {
		// hide the soft keyboard if it is shown
		hideKeyboard();				
		// check search query and do the search
		searchQuery = query.getText().toString();
		if (searchQuery.isEmpty()){
			MessageHelper.showMessageDialog(currentActivity, 
					"Oops! You haven't told me what you would like to search !");
		}
		else{
			loadingProBar.setVisibility(0);
			// change to initial loading listener
			search.registerLoadingListener(iniLoadingListener);
			// searchQueryQuote.setText("Search results for : \"" + searchQuery + "\"");
			onScrollTaskHandler.initializeSearchState();
			// reset page index
			search.searchResultsPageCount = 1;
			search.DoSearch(searchQuery, sortedBy, order, true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.overridePendingTransition(0, R.anim.push_left_out);
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// on workflow details loading task in progress
	public Object onTaskInProgress(Object... param) {
		String selectedwfUri = (String) param[0];
		if(selectedwfUri == null){
			return "Invalid workflow content uri";
		}
		
		String exceptionMessage = null;
		Workflow selectWorkflow = null;
		try {
			selectWorkflow = (Workflow) requestHandler.Get(selectedwfUri, Workflow.class, null, null);
		} catch (Exception e) {
			exceptionMessage = e.getMessage();
		}

		return exceptionMessage != null ? exceptionMessage: selectWorkflow;
	}

	// on workflow details loading task complete
	public Object onTaskComplete(Object... result) {
		if(result[0] instanceof String){
			String exception = (String) result[0];
			if(exception != null){
				MessageHelper.showMessageDialog(currentActivity, exception);
			}
		}
		else{
			Workflow selectWorkflow = (Workflow) result[0];

			Intent intent = new Intent(currentActivity, WorkflowDetail.class);
			intent.putExtra("workflow_details", selectWorkflow);
			startActivity(intent);	
		}
		return null;
	}
	
	// method that hide the soft keyboard
	private void hideKeyboard(){
		InputMethodManager inputManager = (InputMethodManager)
				getSystemService(Context.INPUT_METHOD_SERVICE); 

		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	// class that handle initial workflow search result loading of NEW search
	private class InitialSearchResultHandler implements CallbackTask{
		
		public Object onTaskInProgress(Object... param) {
			return null;
		}

		public Object onTaskComplete(Object... result) {
			if(result[0] instanceof String){
				// exception message
				MessageHelper.showMessageDialog(currentActivity, (String)result[0]);
				loadingProBar.setVisibility(8);
				return null;
			}
			
			ArrayList<Workflow> newResults = (ArrayList<Workflow>) result[0];
			
			if(newResults!= null && newResults.size() > 0){
				// do animation on all of them since they are all
				// newly added items
				workflowResults = new ArrayList<Workflow>();
				workflowResults.addAll(newResults);
				resultListAdapter = new SearchResultListAdapter(currentActivity, workflowResults);
				resultListAdapter.animationStartPosition = 0;
				
				resultList.addFooterView(footerView);
				resultList.setAdapter(resultListAdapter);
			}
			else{
				resultList.removeFooterView(footerView);
				// tell loader no need to do the loading
				// since no more results found
				onScrollTaskHandler.disableTask = true;
				MessageHelper.showMessageDialog(
						currentActivity, 
						"No more matching workflow found");
			}

			// tell the loader that we are not in a search
			// therefore it is safe to execute a new search
			// when scroll to the end
			onScrollTaskHandler.taskInProgress = false;
			
			// hide loading progress bar
			// display results
			resultList.setVisibility(0);
			loadingProBar.setVisibility(8);
			
			// when initial loading complete
			// change the loading listener back to "auto-load-more"
			search.registerLoadingListener(moreResultsLoadingListener);
			return null;
		}
	}
	
	// class that handle more results loading
	private class AutoLoadMoreListener implements CallbackTask{

		@Override
		public Object onTaskInProgress(Object... param) {return null;}

		@Override
		public Object onTaskComplete(Object... result) {
			if(result[0] instanceof String){
				// exception message
				MessageHelper.showMessageDialog(currentActivity, (String)result[0]);
				return null;
			}
			
			ArrayList<Workflow> newResults = (ArrayList<Workflow>) result[0];
			
			if(newResults!= null && newResults.size() > 0){
				// only do animation on newly added items
				resultListAdapter.animationStartPosition = 
						workflowResults.size() > 0 ? workflowResults.size() - 1 : 0;
				workflowResults.addAll(newResults);
				resultListAdapter.notifyDataSetChanged();
			}
			else{
				resultList.removeFooterView(footerView);
				// tell loader no need to do the loading
				// since no more results found
				onScrollTaskHandler.disableTask = true;
				MessageHelper.showMessageDialog(
						currentActivity, 
						"No more matching workflow found");
			}

			onScrollTaskHandler.taskInProgress = false;
			return null;
		}
	}
	
	@Override
	public void finish(){
		this.overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
		super.finish();
	}
}

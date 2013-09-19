package cs.man.ac.uk.tavernamobile.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.MainPanelActivity;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.WorkflowDetail;
import cs.man.ac.uk.tavernamobile.datamodels.Workflow;
import cs.man.ac.uk.tavernamobile.myexperiment.HttpRequestHandler;
import cs.man.ac.uk.tavernamobile.myexperiment.WorkflowsLoader;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.ListViewOnScrollTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;
import cs.man.ac.uk.tavernamobile.utils.WorkflowsListAdapter;

public class SearchResultFragment extends Fragment implements CallbackTask {

	private FragmentActivity parentActivity;
	
	private ArrayList<Workflow> workflowResults;
	private WorkflowsListAdapter resultListAdapter;

	private View mainView;
	private ListView resultList;
	private View footerView;
	private ProgressBar loadingProBar;
	private TextView searchQueryQuote;
	// private LinearLayout searchResultsTopLayout;

	private String searchQuery;
	private String sortedBy;
	private String order = "reverse";

	// utilities
	protected static HttpRequestHandler requestHandler;
	private WorkflowsLoader search;
	private InitialSearchResultHandler iniLoadingListener;
	private AutoLoadMoreListener moreResultsLoadingListener;
	private ListViewOnScrollTaskHandler onScrollTaskHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.search_result_screen, container, false);
		loadingProBar = (ProgressBar) mainView.findViewById(R.id.wfSearchProgressBar);
		searchQueryQuote = (TextView) mainView.findViewById(R.id.searchQueryQuote);
		resultList = (ListView) mainView.findViewById(R.id.searchResultList);
		// searchResultsTopLayout = (LinearLayout) mainView.findViewById(R.id.searchResultsTopLayout);
		setHasOptionsMenu(true);
		return mainView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		parentActivity = getActivity();
		// utilities object setup
		requestHandler = new HttpRequestHandler(parentActivity);

		iniLoadingListener = new InitialSearchResultHandler();
		moreResultsLoadingListener = new AutoLoadMoreListener();
		search = new WorkflowsLoader(parentActivity, iniLoadingListener);

		// footer view to append to the list
		footerView = ((LayoutInflater) parentActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
						R.layout.list_footer_loading, null, false);

		onScrollTaskHandler = new ListViewOnScrollTaskHandler(
				(MainPanelActivity)parentActivity, resultList, new OnScrollLoadingTask());
		onScrollTaskHandler.setOnScrollLoading();

		// setup event reaction -
		// tap on one of the result going to detail
		final SearchResultFragment currentClass = this;
		resultList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View arg1,
					int itemIndex, long arg3) {
				// hide the soft keyboard if it is shown
				hideKeyboard();

				// check for Internet connection
				SystemStatesChecker checker = new SystemStatesChecker(parentActivity);
				if (!checker.isNetworkConnected()) {
					return;
				}

				// Begin to load workflow details
				String selectedwfUri = workflowResults.get(itemIndex).getUri()
						+ "&elements=id,title,description,type,uploader,"
						+ "created-at,preview,license-type,content-uri,privileges";
				BackgroundTaskHandler handler = new BackgroundTaskHandler();
				handler.StartBackgroundTask(parentActivity, currentClass, "Retrieving data...", selectedwfUri);
			}
		});

		/*Spinner sortCriteriaSpinner = (Spinner) currentActivity
				.findViewById(R.id.wfSearchSortSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				currentActivity, R.array.wfList_sort_criteria,
				android.R.layout.simple_spinner_item);
		// Sets the layout resource to create the drop down views
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sortCriteriaSpinner.setAdapter(adapter);
		sortCriteriaSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View arg1, int itemIndex, long arg3) {
						// creation time (created), update time (updated),
						// title (title) and name (name)
						switch (itemIndex) {
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
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});

		CheckBox reverseRadioButton = (CheckBox) currentActivity
				.findViewById(R.id.wfSearchSortOrderRadioButton);
		reverseRadioButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							order = null;
							refreshTheList();
						} else {
							order = "reverse";
							refreshTheList();
						}

					}
				});*/
	}

	/*private void refreshTheList() {
		if (searchQuery == null) {
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
	}*/

	/*@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// remove menu added by previous fragment
		for(int i = 1; i < menu.size(); i ++){
			menu.removeItem(menu.getItem(i).getItemId());
		}
		inflater.inflate(R.menu.search_results_screen, menu);
		LinearLayout searchView = (LinearLayout) menu.findItem(R.id.search_results_search).getActionView();

		final EditText query = (EditText) searchView.getChildAt(0);
		query.requestFocus();
		query.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					searchQuery = query.getText().toString();
					search(searchQuery);
					return true;
				}
				return false;
			}
		});
		ImageButton searchButton = (ImageButton) searchView.getChildAt(1);

		searchButton.setOnClickListener(new android.view.View.OnClickListener() {
				public void onClick(android.view.View v) {
					searchQuery = query.getText().toString();
					search(searchQuery);
				}
			});
	
		super.onCreateOptionsMenu(menu, inflater);
	}*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			((MainPanelActivity) parentActivity).getMenu().toggle();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// remove menu added by previous fragment
		for(int i = 1; i < menu.size(); i ++){
			menu.removeItem(menu.getItem(i).getItemId());
		}
		parentActivity.getMenuInflater().inflate(R.menu.search_results_screen, menu);
		LinearLayout searchView = (LinearLayout) menu.findItem(R.id.search_results_search).getActionView();

		final EditText query = (EditText) searchView.getChildAt(0);
		query.requestFocus();
		query.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					searchQuery = query.getText().toString();
					search(searchQuery);
					return true;
				}
				return false;
			}
		});
		ImageButton searchButton = (ImageButton) searchView.getChildAt(1);

		searchButton.setOnClickListener(new android.view.View.OnClickListener() {
				public void onClick(android.view.View v) {
					searchQuery = query.getText().toString();
					search(searchQuery);
				}
			});
		super.onPrepareOptionsMenu(menu);
	}

	private void search(String query) {
		// hide the soft keyboard if it is shown
		hideKeyboard();
		// check search query and do the search
		searchQuery = query;
		if (searchQuery.isEmpty()) {
			MessageHelper.showMessageDialog(
					parentActivity,
					"Alert",
					"Oops! You haven't told me what you would like to search !",
					null);
		} else {
			loadingProBar.setVisibility(0);
			// change to initial loading listener
			search.registerLoadingListener(iniLoadingListener);
			onScrollTaskHandler.initializeSearchState();
			// reset page index
			search.searchResultsPageCount = 1;
			search.DoSearch(query, sortedBy, order, true);
		}
	}

	// on workflow details loading task in progress
	public Object onTaskInProgress(Object... param) {
		String selectedwfUri = (String) param[0];
		if (selectedwfUri == null) {
			return "Invalid workflow content uri";
		}

		String exceptionMessage = null;
		Workflow selectWorkflow = null;
		try {
			selectWorkflow = (Workflow) requestHandler.Get(selectedwfUri,
					Workflow.class, null, null);
		} catch (Exception e) {
			exceptionMessage = e.getMessage();
		}

		return exceptionMessage != null ? exceptionMessage : selectWorkflow;
	}

	// on workflow details loading task complete
	public Object onTaskComplete(Object... result) {
		if (result[0] instanceof String) {
			String exception = (String) result[0];
			if (exception != null) {
				MessageHelper.showMessageDialog(parentActivity, null,
						exception, null);
			}
		} else {
			Workflow selectWorkflow = (Workflow) result[0];
			Intent intent = new Intent(parentActivity, WorkflowDetail.class);
			intent.putExtra("workflow_details", selectWorkflow);
			startActivity(intent);
		}
		return null;
	}

	// method that hide the soft keyboard
	private void hideKeyboard() {
		InputMethodManager inputManager = 
				(InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

		inputManager.hideSoftInputFromWindow(parentActivity.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	// class that handle initial workflow search result loading of NEW search
	private class InitialSearchResultHandler implements CallbackTask {

		public Object onTaskInProgress(Object... param) {
			return null;
		}

		public Object onTaskComplete(Object... result) {
			if (result[0] instanceof String) {
				// exception message
				MessageHelper.showMessageDialog(parentActivity, null,
						(String) result[0], null);
				loadingProBar.setVisibility(8);
				return null;
			}

			final ArrayList<Workflow> newResults = (ArrayList<Workflow>) result[0];

			if (newResults != null && newResults.size() > 0) {
				// do animation on all of them since they are all
				// newly added items
				workflowResults = new ArrayList<Workflow>();
				workflowResults.addAll(newResults);
				resultListAdapter = new WorkflowsListAdapter(parentActivity, workflowResults);
				resultList.addFooterView(footerView);
				resultList.setAdapter(resultListAdapter);

				resultList.post(new Runnable() {
					public void run() {
						if (newResults.size() <= resultList
								.getLastVisiblePosition()) {
							resultList.removeFooterView(footerView);
						}
						// At this point the list has already being rendered.
						// Set the animation start position to be the start of
						// the
						// next set of items, hence any layout changes made on
						// the
						// rendered list will not trigger animation
						// the list view being refreshed
						resultListAdapter.animationStartPosition = resultList
								.getLastVisiblePosition();
					}
				});
			} else {
				// inform loader no need to do the loading
				// since no more results found
				onScrollTaskHandler.disableTask = true;
				// inform the adapter not to add bottom loading view
				// resultListAdapter.noMoreData = true;
				MessageHelper.showMessageDialog(parentActivity, null,
						"No matching workflow found", null);
			}

			// inform the loader that we are not in a search
			// therefore it is safe to execute a new search
			// when scroll to the end
			onScrollTaskHandler.taskInProgress = false;

			// hide loading progress bar
			// display results
			resultList.setVisibility(0);
			loadingProBar.setVisibility(8);
			searchQueryQuote.setText("Search results for : \"" + searchQuery + "\"");

			// when initial loading complete
			// change the loading listener back to "auto-load-more"
			search.registerLoadingListener(moreResultsLoadingListener);
			return null;
		}
	}

	// class that handle more results loading
	private class AutoLoadMoreListener implements CallbackTask {

		@Override
		public Object onTaskInProgress(Object... param) {
			return null;
		}

		@Override
		public Object onTaskComplete(Object... result) {
			if (result[0] instanceof String) {
				// exception message
				MessageHelper.showMessageDialog(parentActivity, null,
						(String) result[0], null);
				return null;
			}

			ArrayList<Workflow> newResults = (ArrayList<Workflow>) result[0];
			// only do animation on newly added items
			resultListAdapter.animationStartPosition = workflowResults.size() > 0 ? workflowResults
					.size() - 1 : 0;
			if (newResults != null && newResults.size() > 0) {
				workflowResults.addAll(newResults);
				resultListAdapter.notifyDataSetChanged();
			} else {
				resultList.removeFooterView(footerView);
				// inform loader no need to do the loading
				// since no more results found
				onScrollTaskHandler.disableTask = true;
				// inform the adapter not to add bottom loading view
				// resultListAdapter.noMoreData = true;
				MessageHelper.showMessageDialog(parentActivity, null,
						"No more matching workflow found", null);
			}

			onScrollTaskHandler.taskInProgress = false;
			return null;
		}
	}

	private class OnScrollLoadingTask implements CallbackTask {
		@Override
		public Object onTaskInProgress(Object... param) {
			search.DoSearch(searchQuery, sortedBy, order, false);
			return null;
		}

		@Override
		public Object onTaskComplete(Object... result) {
			return null;
		}
	}
}

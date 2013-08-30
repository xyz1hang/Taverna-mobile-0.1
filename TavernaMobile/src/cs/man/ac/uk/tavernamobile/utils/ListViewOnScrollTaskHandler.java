package cs.man.ac.uk.tavernamobile.utils;

import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

public class ListViewOnScrollTaskHandler {
	
	// flag that help with preventing multiple
	// enter of the block that execute a search.
	// since every single change on the list trigger the
	// scroll event...
	public boolean taskInProgress = false;
	
	// flag that used to stop executing the task
	// depends on business logic e.g. no more results to load
	public boolean disableTask = false;
	
	// The onScroll gets called even when the user
	// not actually scrolling (consider a bug in the
	// framework) - hence need a flag to indicate actual
	// scrolling from user
	private boolean userScrolled = false;
	
	private SystemStatesChecker systemStatesChecker;
	
	private ListView theList;
	private CallbackTask loadingTask;
	
	public ListViewOnScrollTaskHandler(ListView list, CallbackTask task){
		theList = list;
		loadingTask = task;
		systemStatesChecker = new SystemStatesChecker(theList.getContext());
	}
	
	public void setOnScrollLoading(){
		theList.setOnScrollListener(new OnScrollListener(){
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				// ">=" - in case there are any padding
				boolean reachTheEnd = firstVisibleItem + visibleItemCount >= totalItemCount &&
						totalItemCount > visibleItemCount;

						// Check Network Connection
						if(!systemStatesChecker.isNetworkConnected()){
							return;
						}
						
						// if scroll reach the end of the list AND
						// in - if we are already in the process of doing
						// a new search... (since every change made to the list
						// triggers the invocation of onScroll())
						if(reachTheEnd &&!taskInProgress && userScrolled && !disableTask) {
							taskInProgress = true; // lock
							loadingTask.onTaskComplete(null);
							userScrolled = false;
						}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == 1){
					userScrolled = true;
				}	
			}
		});
	}
	
	// method to initialize variables related
	// to the search state
	public void initializeSearchState() {
		this.taskInProgress = false;
		// this.newSearch = true;
		this.disableTask = false;
		this.userScrolled = false;
	}
}

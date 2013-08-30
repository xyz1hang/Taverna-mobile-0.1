package cs.man.ac.uk.tavernamobile.myexperiment;

import java.util.ArrayList;
import java.util.List;

import uk.org.taverna.server.client.NetworkConnectionException;

import android.app.Activity;

import cs.man.ac.uk.tavernamobile.datamodels.Workflow;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowExpoResults;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowSearchResults;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;

public class WorkflowsLoader implements CallbackTask{
	
	private Activity CallingActivity;
	private static HttpRequestHandler requestHandler;
	private CallbackTask loadingListener;
	private SystemStatesChecker sysStatesChecker;
	
	public int searchResultsPageCount;

	public WorkflowsLoader(Activity activity, CallbackTask listener){
		CallingActivity = activity;
		requestHandler = new HttpRequestHandler(CallingActivity);
		sysStatesChecker = new SystemStatesChecker(CallingActivity);
		loadingListener = listener;
		searchResultsPageCount = 1;
	}
	
	public void registerLoadingListener(CallbackTask listener){
		loadingListener = listener;
	}
	
	public void DoSearch(String searchQuery, String sort, String order, boolean hasMessage){
		// check for Internet connection
		if (!(sysStatesChecker.isNetworkConnected())){
			return;
		}
		else{
			if(searchQuery == null){
				throw new IllegalArgumentException("\"searchQuery\" can not be null.");
			}
			// if there is no order/sort specified
			// use empty string
			order = order != null ? "&order="+order : "";
			sort = sort != null ? "&sort="+sort : "";
			
			// replace all spaces with "+"
			String finalString = searchQuery.replace(" ", "+");
			// start searching background task
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			String theQuery = "http://www.myexperiment.org/search.xml?query=" + finalString + sort + order
							+"&type=workflow&num=25&page=" + searchResultsPageCount + "&elements=title,uploader";
			if(hasMessage){
				handler.StartBackgroundTask(
						CallingActivity, 
						this, 
						"Searching for "+ "\"" + searchQuery+ "\""+ "...", 
						theQuery, WorkflowSearchResults.class);
			}else{
				handler.StartBackgroundTask(
						CallingActivity, 
						this, 
						null, 
						theQuery, WorkflowSearchResults.class);
			}
			
		}
	}
	
	// load indexed workflows
	// sort - creation time (created), update time (updated), 
	// 		  title (title) and name (name) 
	public void LoadWorkflows(String sort, String order){
		// check for Internet connection
		if (!sysStatesChecker.isNetworkConnected()){
			return;
		}
		else{
			// if there is no order/sort specified
			// use empty string
			order = order != null ? "&order="+order : "";
			sort = sort != null ? "&sort="+sort : "";
			
			String theQuery = "http://www.myexperiment.org/workflows.xml?"
							 +"&num=10&page=" + searchResultsPageCount + sort + order
							 +"&elements=title,content-uri,uploader,preview,privileges,license-type,"
							 +"created-at,updated-at,thumbnail-big,credits,ratings,type";
			// start searching background task
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(
					CallingActivity, 
					this, 
					null,
					theQuery, WorkflowExpoResults.class);
		}
	}

	public Object onTaskInProgress(Object... params) {
		String searchQuery = (String)params[0];
		Class<?> targetClass = (Class<?>) params[1];
		// when searching by using same instance i.e loading more results,
		// we load result from following pages
		searchResultsPageCount++;
		ArrayList<Workflow> workflows = null;
		List<Workflow> retrievedWorkflows = null;
		try{
			if(targetClass == WorkflowSearchResults.class){
				WorkflowSearchResults results = 
						(WorkflowSearchResults) requestHandler.Get(searchQuery, targetClass, null, null);

				retrievedWorkflows = results.getWorkflows();
			}
			else if(targetClass == WorkflowExpoResults.class){
				WorkflowExpoResults results = 
						(WorkflowExpoResults) requestHandler.Get(searchQuery, targetClass, null, null);
				
				retrievedWorkflows = results.getWorkflows();
			}
			
			if (retrievedWorkflows != null){
				workflows = new ArrayList<Workflow>(retrievedWorkflows);
			}
		} catch(NetworkConnectionException e){
			return e.getMessage();
		} catch(Exception e) {
			return e.getMessage();
		}
		
		return workflows;
	}

	public Object onTaskComplete(Object... result) {
		loadingListener.onTaskComplete(result);
		return null;
	}
}
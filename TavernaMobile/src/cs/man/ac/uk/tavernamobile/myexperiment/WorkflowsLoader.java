package cs.man.ac.uk.tavernamobile.myexperiment;

import java.util.ArrayList;
import java.util.List;

import uk.org.taverna.server.client.NetworkConnectionException;

import android.app.Activity;

import cs.man.ac.uk.tavernamobile.datamodels.ElementBase;
import cs.man.ac.uk.tavernamobile.datamodels.User;
import cs.man.ac.uk.tavernamobile.datamodels.UserFavourited;
import cs.man.ac.uk.tavernamobile.datamodels.Workflow;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowBrief;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowBriefCollection;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowCollection;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowSearchResults;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

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
			String uri = "http://www.myexperiment.org/search.xml?query=" + finalString + sort + order
							+"&type=workflow&num=15&page=" + searchResultsPageCount
							+"&elements=title,content-uri,uploader,preview,privileges,license-type,"
							+"created-at,updated-at,thumbnail-big,credits,ratings,type";
			if(hasMessage){
				handler.StartBackgroundTask(
						CallingActivity, 
						this, 
						"Searching for "+ "\"" + searchQuery+ "\""+ "...", 
						uri, WorkflowSearchResults.class);
			}else{
				handler.StartBackgroundTask(
						CallingActivity, 
						this, 
						null, 
						uri, WorkflowSearchResults.class);
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
			
			String uri = "http://www.myexperiment.org/workflows.xml?"
							 +"&num=10&page=" + searchResultsPageCount + sort + order
							 +"&elements=title,content-uri,uploader,preview,privileges,license-type,"
							 +"created-at,updated-at,thumbnail-big,credits,ratings,type";
			// start background loading task
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(
					CallingActivity, 
					this, 
					null,
					uri, WorkflowCollection.class);
		}
	}
	
	public void LoadMyWorkflows(String userID){
		// check for Internet connection
		if (!sysStatesChecker.isNetworkConnected()){
			return;
		}
		
		String uri = "http://www.myexperiment.org/user.xml?id="+userID+"&elements=workflows,favourited";
		// start background loading task
		BackgroundTaskHandler handler = new BackgroundTaskHandler();
		handler.StartBackgroundTask(
				CallingActivity, 
				new UserWorkflowsLoadingTask(),
				null,
				uri);
		
	}

	public Object onTaskInProgress(Object... params) {
		String searchUri = (String)params[0];
		Class<?> targetClass = (Class<?>) params[1];
		// when searching by using same instance i.e loading more results,
		// we load result from following pages
		searchResultsPageCount++;
		ArrayList<Workflow> workflows = null;
		List<Workflow> retrievedWorkflows = null;
		try{
			if(targetClass == WorkflowSearchResults.class){
				WorkflowSearchResults results = 
						(WorkflowSearchResults) requestHandler.Get(searchUri, targetClass, null, null);

				retrievedWorkflows = results.getWorkflows();
			}
			else if(targetClass == WorkflowCollection.class){
				WorkflowCollection results = 
						(WorkflowCollection) requestHandler.Get(searchUri, targetClass, null, null);
				
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
	
	/**
	 * Background Task loading user's workflow and their favorite workflow
	 * 
	 * @author hyde zhang
	 *
	 */
	private class UserWorkflowsLoadingTask implements CallbackTask{

		@Override
		public Object onTaskInProgress(Object... param) {
			String searchUri = (String)param[0];
			ArrayList<Workflow> myWorkflows = new ArrayList<Workflow>();
			ArrayList<Workflow> favouriteWorkflows = new ArrayList<Workflow>();
			
			try{
				User results = (User) requestHandler.Get(searchUri, User.class, null, null);
				WorkflowBriefCollection myWf = results.getWorkflows();
				if(myWf != null){
					for(WorkflowBrief wfb: myWf.getWorkflowBrief()){
						String uri = wfb.getUri() + "&elements=title,content-uri,uploader,preview,privileges,"
									+"license-type,created-at,updated-at,thumbnail-big,credits,ratings,type";
						Workflow workflow = (Workflow) requestHandler.Get(uri, Workflow.class, null, null);
						myWorkflows.add(workflow);
					}
				}
				
				UserFavourited favourite = results.getFavourited();
				if(favourite != null){
					List<ElementBase> favouritedWf = favourite.getFavouritedEntity();
					for(ElementBase wfb: favouritedWf){
						if(wfb instanceof WorkflowBrief){
							String uri = wfb.getUri() + "&elements=title,content-uri,uploader,preview,privileges,"
									+"license-type,created-at,updated-at,thumbnail-big,credits,ratings,type";
							Workflow workflow = (Workflow) requestHandler.Get(uri, Workflow.class, null, null);
							favouriteWorkflows.add(workflow);
						}
					}
				}				
			} catch(NetworkConnectionException e){
				return e.getMessage();
			} catch(Exception e) {
				return e.getMessage();
			}
			
			// save workflow details in the global context
			// alone with user detail
			TavernaAndroid.setMyWorkflows(myWorkflows);
			TavernaAndroid.setFavouriteWorkflows(favouriteWorkflows);
			
			return null;
		}

		@Override
		public Object onTaskComplete(Object... result) {
			loadingListener.onTaskComplete(result);
			return null;
		}
	}
}
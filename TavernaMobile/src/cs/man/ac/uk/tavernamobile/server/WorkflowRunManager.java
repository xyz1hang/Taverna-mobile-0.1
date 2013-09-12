package cs.man.ac.uk.tavernamobile.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.text.StrBuilder;

import uk.org.taverna.server.client.InputPort;
import uk.org.taverna.server.client.NetworkConnectionException;
import uk.org.taverna.server.client.OutputPort;
import uk.org.taverna.server.client.PortValue;
import uk.org.taverna.server.client.Run;
import uk.org.taverna.server.client.RunStatus;
import uk.org.taverna.server.client.Server;
import uk.org.taverna.server.client.connection.AccessForbiddenException;
import uk.org.taverna.server.client.connection.UserCredentials;
import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import cs.man.ac.uk.tavernamobile.dataaccess.DataProviderConstants;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowBE;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowRun;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;
import cs.man.ac.uk.tavernamobile.utils.WorkflowFileLoader;

public class WorkflowRunManager
{
	/*************** for simple test only *****************/
	//protected final static String TEST_WORKFLOW = "/mnt/sdcard/simple.t2flow";
	//private final static String TEST_IN_FILE = "/Test data/HelloWorld.txt";
	/*************** for simple test only *****************/

	// reference to the activity that requests
	// services from the manager
	private Activity currentActivity;
	private TavernaAndroid ta;

	private String runStartTime;
	private String runEndTime;
	private String runStatue;
	
	// controllers of various background tasks
	private BackgroundTaskHandler outputHandlingTaskHandler;
	private BackgroundTaskHandler runInitiationTaskHandler;
	private BackgroundTaskHandler runStatesPullingTaskHandler;

	// Background task progress listeners
	private CallbackTask creationListener;
	private CallbackTask runListener;
	private CallbackTask runStateCheckListener;
	private CallbackTask inputPortsRetrievalListener;
	private CallbackTask runListRetrieverListener;
	private CallbackTask runDeletionListener;
	private CallbackTask outputRetrievalListener;
	
	// Run states
	private String STATE_INITIALISED = "Initialised";
	private String STATE_RUNNING = "Running";
	private String STATE_FINISHED = "Finished";
	private String STATE_DELETED = "Deleted";
	private String STATE_UNDEFINED = "Undefined";
	
	// settings
	private SharedPreferences sharedPrefs;

	public WorkflowRunManager(Activity activity){
		currentActivity = activity;
		ta = (TavernaAndroid) activity.getApplication();
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(currentActivity);
	}

	public void getRuns(CallbackTask listener){
		runListRetrieverListener = listener;
		new RunListRetriever().Execute();
	}
	
	public String getRunStartTime() {
		return runStartTime;
	}

	public String getRunEndTime() {
		return runEndTime;
	}

	public String getRunStatue() {
		return runStatue;
	}
	
	/**
	 * Retrieve outputs of a particular run or the most recent run
	 * from the Taverna server
	 * 
	 * @param runID - ID of the run to get output from
	 * @param listener - the listener for handling inputs retrieved
	 */
	public void getRunInputs(String runId, CallbackTask listener){
		inputPortsRetrievalListener = listener;
		new InputPortsRetriever().Execute(runId);
	}
	
	/**
	 * Retrieve outputs of a particular run or the most recent run
	 * from the Taverna server
	 * 
	 * @param workflowTitle - String that is used to build outputs directory
	 * @param runID - ID of the run to get output from
	 * @param listener - the listener for output retrieval states (fail/success)
	 */
	// TODO: passing in workflow title String...
	public void getRunOutput(String workflowTitle, String runId, CallbackTask listener){
		outputRetrievalListener = listener;
		// start outputHandler thread gather result
		new OutputHanlder().Execute(workflowTitle, runId);
	}

	public void CreateRun(WorkflowBE workflowEntity, CallbackTask listener){
		creationListener = listener;
		new RunCreation().Execute(workflowEntity);
	}

	// for newly created workflow we need to
	// setup input and then run the workflow, all in one go
	// in order to minimise threads interference
	
	/**
	 * for newly created workflow we need to setup input 
	 * and then run the workflow, all in one go
	 * 
	 * @param inputs
	 * @param workflowEntity
	 * @param listener
	 * @param requireMonitor - flag to indicate whether monitor info is needed
	 * i.e whether the monitor is on
	 */
	public void StartWorkflowRun(Map<String, Object> inputs, 
			WorkflowBE workflowEntity, CallbackTask listener, boolean requireMonitor){
		runListener = listener;
		// start this thread to pull run statue first
		// otherwise it is difficult to update statue
		if(requireMonitor){
			new RunProgressListenerInvoker().Execute();
		}
		new RunInitiator().Execute(inputs, workflowEntity);
	}
	
	/**
	 * @param workflowData
	 * @param savedInputFileName
	 * @param listener
	 */
	public void StartRunWithSavedInput(WorkflowBE workflowEntity, CallbackTask listener){
		runListener = listener;
		new RunWithExistingInputs().Execute(workflowEntity);
	}

	// only monitoring
	public void StartMonitoring(CallbackTask listener){
		runListener = listener;
		new RunProgressListenerInvoker().Execute();
	}

	public void StopRun(String message, ArrayList<String> runIDs){
		// cancel all task
		if(outputHandlingTaskHandler != null){
			outputHandlingTaskHandler.CancelTask(true);
		}
		if(runInitiationTaskHandler != null){
			runInitiationTaskHandler.CancelTask(true);
		}
		if(runStatesPullingTaskHandler != null){
			runStatesPullingTaskHandler.CancelTask(true);
		}
		// delete the run
		DeleteRun(message, runIDs);
	}

	public void DeleteRun(String message,  ArrayList<String> runID){
		// delete the run of the most recent workflow
		// or a set of runs on the server
		if(runID != null){
			new RunCleaner().Execute(message, 1, runID);
		}
		else{
			new RunCleaner().Execute(message, 0);
		}
	}
	
	public void DeleteAllRun(CallbackTask Listener){
		this.runDeletionListener = Listener;
		new RunCleaner().Execute("Deleting all runs...", 2);
	}
	
	

	// Check Run State and invoke relevant reaction
	public void checkRunStateWithID(String runId, CallbackTask listener){
		runStateCheckListener = listener;
		new RunStateChecker().Execute(runId);
	}
	
	

	public String reportRunStartTime(){
		Run run = ta.getWorkflowRunLaunched();
		String exceptionMessage = null;
		if(run != null){
			try {
				runStartTime = run.getStartTime().split("\\+")[0].replace('T', ' ');
			} catch (NetworkConnectionException e) {
				exceptionMessage = e.getMessage();
			}
		}

		return exceptionMessage == null ? runStartTime : exceptionMessage;
	}

	public String reportRunFinishTime(){
		String runningTimeString = null;
		Run run = ta.getWorkflowRunLaunched();
		
		if(run != null){
			try {
				runEndTime = run.getFinishTime().split("\\+")[0].replace('T', ' ');
			} catch (NetworkConnectionException e1) {
				return "Connection problem reading data from server/"+
					   "Connection problem reading data from server";
			}
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
			try {
				Date start = dateFormat.parse(runStartTime);
				Date end = dateFormat.parse(runEndTime);
				long diff = end.getTime() - start.getTime();
				String duration = String.format(Locale.getDefault(), "%d min, %d sec", 
						TimeUnit.MILLISECONDS.toMinutes(diff),
						TimeUnit.MILLISECONDS.toSeconds(diff) - 
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff)));
				runningTimeString = duration;
			} catch (ParseException e) {
				// TODO: possible error when server change the date format it returns
				e.printStackTrace();
			}
		}

		return runEndTime + "/" + runningTimeString;
	}

	public String reportRunState(){
		Run run = ta.getWorkflowRunLaunched();
		if(run != null){
			RunStatus statu;
			try {
				statu = run.getStatus();
			} catch (NetworkConnectionException e) {
				return "Connection problem reading data from server";
			}
			runStatue = getRunState(statu);
		}
		return runStatue;
	}

	/*public String getRunStateWithID(String runID){
		Server server = ta.getServer();
		Run theRun = null;
		try {
			theRun = server.getRun(runID, ta.getDefaultUser());
		} catch (NetworkConnectionException e) {
			return "Connection problem reading data from server";
		}
		if(theRun != null){
			ta.setWorkflowRunLaunched(theRun);
			RunStatus statu;
			try {
				statu = theRun.getStatus();
			} catch (NetworkConnectionException e) {
				return "Connection problem reading data from server";
			}
			runStatue = getRunState(statu);
		}
		return runStatue;
	}*/

	private String getRunState(RunStatus statu) {
		switch (statu){
		case INITIALIZED:
			runStatue = STATE_INITIALISED;
			return runStatue;
		case RUNNING:
			runStatue = STATE_RUNNING;
			return runStatue;
		case FINISHED:
			runStatue = STATE_FINISHED;
			return runStatue;
		case DELETED:
			runStatue = STATE_DELETED;
			return runStatue;
		default:
			runStatue = STATE_UNDEFINED;
			return runStatue;
		}
	}
	
	private Object createWorkflowRun(WorkflowBE workflowEntity) {
		byte[] workflowData = null;
		try {
			workflowData = WorkflowFileLoader.getBytesFromFile(new File(workflowEntity.getFilePath()));
		} catch (Exception e) {
			MessageHelper.showMessageDialog(currentActivity, null, e.getMessage(), null);
		}
		
		Run runCreated = null;
		try{
			runCreated = Run.create(ta.getServer(), workflowData, ta.getDefaultUser());
			ta.setWorkflowRunLaunched(runCreated);
		} catch(AccessForbiddenException e){
			return "Access to the run of this workflow is forbidden";
		} catch (NetworkConnectionException e) {
			return e.getMessage();
		}
		// Insert Run ID of this workflow into database
		String runID = runCreated.getIdentifier();
		ContentValues args = new ContentValues();
		args.put(DataProviderConstants.Run_Id, runID);
		
		String subQuery = "SELECT WF_ID FROM launchHistory "+
				  "WHERE Workflow_Title = '"+workflowEntity.getTitle()+ "' AND "+
					    "Version = '"+workflowEntity.getVersion()+"' AND "+
					    "Uploader_Name = '"+workflowEntity.getUploaderName()+"'";
		args.put(DataProviderConstants.WF_ID, subQuery);
		
		/** INSERT **/
		currentActivity.getContentResolver().insert(
				DataProviderConstants.RUN_TABLE_CONTENTURI, args);
		
		return runCreated;
	}

	// Class that in charge of run creation
	private class RunCreation implements CallbackTask {
		private WorkflowBE workflowEntity;
		
		public void Execute(Object... params){
			if(!(params[0] instanceof WorkflowBE)){
				throw new IllegalArgumentException("Invalid Workflow Entity");
			}
			workflowEntity = (WorkflowBE) params[0];
			
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(currentActivity, this, "Creating run...", params);
		}

		public Object onTaskInProgress(Object... params) {
			Object result = createWorkflowRun(workflowEntity);
			if(!(result instanceof Run)){
				return (String) result;
			}
			
			Run runCreated = (Run) result;
			// get input ports, if available
			Map<String, InputPort> inputPorts = null;
			
			/*HashMap<String, Map<String, InputPort>> idAndinputs = 
					new HashMap<String, Map<String, InputPort>>();
			if (runCreated != null){
				try{
					inputPorts = runCreated.getInputPorts();
					String runId = runCreated.getIdentifier();
					idAndinputs.put(runId, inputPorts);
				} catch (Exception e){
					e.printStackTrace();
				}
			}*/
			
			try {
				inputPorts = runCreated.getInputPorts();
			} catch (NetworkConnectionException e) {
				return e.getMessage();
			}

			return inputPorts; //idAndinputs;
		}

		public Object onTaskComplete(Object... result) {
			if (creationListener != null){
				creationListener.onTaskComplete(result);
			}
			return null;
		}
	}
	
	/**
	 * Callback task to create workflow run, setup input ports with
	 * saved inputs value and start the run all in one go
	 * 
	 * @author Hyde
	 */
	public class RunWithExistingInputs implements CallbackTask {
		
		private Run runCreated = null;
		private WorkflowBE workflowEntity;
		private List<String> inputsFilesPath;
		
		public void Execute(Object... params){
			// parameters checking
			workflowEntity = (WorkflowBE) params[0];
			if(workflowEntity == null){
				throw new IllegalArgumentException("Invalid Workflow Entity");
			}
			inputsFilesPath = workflowEntity.getSavedInputsFilesPath();
			if(inputsFilesPath == null || inputsFilesPath.size() < 1){
				throw new IllegalArgumentException("inputsFilesPath can not be empty");
			}
			
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(currentActivity, this, "Launching...", params);
		}

		@Override
		public Object onTaskInProgress(Object... param) {
			// launch run for each inputs file
			for(int i = 0; i < inputsFilesPath.size(); i++){
				try{
					Object result = createWorkflowRun(workflowEntity);
					if(!(result instanceof Run)){
						return (String) result;
					}
					// create the run
					//runCreated = Run.create(ta.getServer(), workflowData, ta.getDefaultUser());
					runCreated = (Run) result;
					// read saved input object
					FileInputStream fis = new FileInputStream(inputsFilesPath.get(i));
				    ObjectInputStream ois = new ObjectInputStream(fis);
				    // String test = (String) ois.readObject();
				    HashMap<String, Object> savedInputs = (HashMap<String, Object>) ois.readObject();
				    if(savedInputs == null){
				    	fis.close();
				    	ois.close();
				    	return null;
				    }
				    
				    // setup(upload) input
				    Iterator<Entry<String, Object>> it = savedInputs.entrySet().iterator();
					while(it.hasNext()){
						Map.Entry<String, Object> pair = it.next();
						Object value = pair.getValue();
						Class<?> valueType = value.getClass();
						//String dataToWrite = null;
						// if input is file
						if (valueType.equals(File.class)){
								File inputfile = (File) value;
								// upload input to server
								InputPort inputPort = runCreated.getInputPort(pair.getKey());
								if(inputPort == null){
									// if one port does not exist the port name must
									// had been changed, hence an up to dated input data
									// need to be supplied by creating a new run
									fis.close();
									ois.close();
									return "Saved inputs data is out of date.\n"
											+"Please supply new data by creating new run";
								}
								inputPort.setFile(inputfile);
						}
						// if input is text
						else if (valueType.equals(String.class)){
								String inputString = (String) value;
								// upload input to server
								InputPort inputPort = runCreated.getInputPort(pair.getKey());
								if(inputPort == null){
									// if one port does not exist the port name must
									// had been changed, hence an up to dated input data
									// need to be supplied by creating a new run
									fis.close();
									ois.close();
									return "Saved inputs data is out of date.\n"
											+"Please supply new data by creating new run";
								}
								inputPort.setValue(inputString);
						}
					}// end of iterating over inputs
					// close ObjectInputStream
					ois.close();
					
					// start the run
					if (runCreated != null){
						runCreated.start();
						if (!runCreated.isRunning()){
							// make sure the run is started
							waitForWorkflowRunToStart(runCreated);
						}
						// set run started time and statue
						reportRunState();
						reportRunStartTime();
					}
				} catch (FileNotFoundException e) {
					return e.getMessage();
				} catch (NetworkConnectionException e) {
					return e.getMessage();
				} catch(AccessForbiddenException e){
					return "Access to the run of this workflow is forbidden";
				} catch(Exception e){
					return e.getMessage();
				}
			}
			
			return "Workflow have been Launched successfully";
		}

		@Override
		public Object onTaskComplete(Object... result) {
			if(runListener != null){
				runListener.onTaskComplete(result);
			}
			return null;
		}
	}

	/**
	 * Background Task to retrieve inputs of a particular run
	 * 
	 * @author Hyde Zhang
	 */
	private class InputPortsRetriever implements CallbackTask{

		public void Execute(Object... params){
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(currentActivity, this, "Please wait...", params);
		}

		public Object onTaskInProgress(Object... params) {
			String runId = (String) params[0];

			Server server = ta.getServer();
			Run theRun;
			try {
				theRun = server.getRun(runId, ta.getDefaultUser());
			} catch (NetworkConnectionException e1) {
				return "Connection problem reading data from server";
			}
			// get input ports, if available
			Map<String, InputPort> inputPorts = null;
			if (theRun != null){
				try{
					inputPorts = theRun.getInputPorts();
				} catch (Exception e){
					e.printStackTrace();
				}
			}

			Map<String, InputPort> idAndinputs = new HashMap<String, InputPort>();
			idAndinputs.putAll(inputPorts);

			return idAndinputs;
		}

		public Object onTaskComplete(Object... result) {
			if (inputPortsRetrievalListener != null){
				inputPortsRetrievalListener.onTaskComplete(result);
			}
			return null;
		}
	}

	/**
	 * Background Task to setup inputs and start the run
	 * 
	 * @author Hyde Zhang
	 */
	private class RunInitiator implements CallbackTask{

		private Run newlyCreatedRun = ta.getWorkflowRunLaunched();

		public void Execute(Object... params){
			runInitiationTaskHandler = new BackgroundTaskHandler();
			runInitiationTaskHandler.StartBackgroundTask(currentActivity, this, null, params);
		}

		public Object onTaskInProgress(Object... params) {
			if(params != null){
				// set (upload) inputs
				Map<String, Object> inputs = (HashMap<String, Object>) params[0];
				// check how many input port
				if (inputs != null && inputs.size() > 0){
					WorkflowBE workflowEntity = (WorkflowBE) params[1];
					// prepare to save inputs in the 
					// "Inputs/Title_verion_uploaderName" directory.
					// Saving the input while uploading them
					String inputsSubPath = "/TavernaAndroid/Inputs/" 
							+ workflowEntity.getTitle().replace(" ", "") + "_" 
							+ workflowEntity.getVersion() + "_" 
							+ workflowEntity.getUploaderName().replace(" ", "") + "/";
					String locationToStore = getFileSaveLocation(inputsSubPath);
					
					Iterator<Entry<String, Object>> it = inputs.entrySet().iterator();
					while(it.hasNext()){
						FileOutputStream stream = null;
						try {
							// save input by dateTime
							DateFormat df = DateFormat.getDateTimeInstance();
							df.setTimeZone(TimeZone.getTimeZone("GMT"));
							String gmtTime = df.format(new Date());
							stream = new FileOutputStream(locationToStore+"/"+gmtTime+".tai");
							
							Map.Entry<String, Object> pair = it.next();
							Object value = pair.getValue();
							Class<?> valueType = value.getClass();
							//String dataToWrite = null;
							// if input is file
							if (valueType.equals(File.class)){
									File inputfile = (File) value;
									// upload input to server
									newlyCreatedRun.getInputPort(pair.getKey()).setFile(inputfile);
									// store input locally
									//dataToWrite = pair.getKey()+">(File)"+inputfile.getAbsoluteFile().toString()+"\n";
							}
							// if input is text
							else if (valueType.equals(String.class)){
									String inputString = (String) value;
									// upload input to server
									newlyCreatedRun.getInputPort(pair.getKey()).setValue(inputString);
									// store input locally
									//dataToWrite = pair.getKey()+">(Text)"+inputString+"\n";
							}
							/*stream.write(dataToWrite.getBytes());
							stream.flush();
							stream.close();*/
							
							ObjectOutputStream oos = new ObjectOutputStream(stream);
							oos.writeObject(inputs);
							oos.close();
						} catch (FileNotFoundException e) {
							// if there were any error saving the input
							// swallow... the run starting process
							// shouldn't be interrupted
							e.printStackTrace();
						} catch (NetworkConnectionException e) {
							try {
								stream.close();
							} catch (IOException e1) {
								// Irrelevant message. swallow
								e1.printStackTrace();
							}
							// report network connection problem
							return e.getMessage();
						} catch(IOException e){
							// Irrelevant message. swallow
							e.printStackTrace();	
						}
					}// end of iterating over inputs
				}// end of if there are input
			}

			// start the run
			if (newlyCreatedRun != null){
				try {
					newlyCreatedRun.start();
					if (!newlyCreatedRun.isRunning()){
						// make sure the run is started
						waitForWorkflowRunToStart(newlyCreatedRun);
					}
					// set run started time and statue
					reportRunState();
					reportRunStartTime();
				} catch (Exception e) {
					// TODO : exception message handling
					return e.getMessage();
				}
			}
			else{
				// TODO: "log" has to be removed in release version
				Log.e("WorkflowRunError2", "Workflow Run needed.");
			}
			return "The Run has been successfully started.";
		}

		public Object onTaskComplete(Object... result) {
			if(runListener != null){
				// inform monitor to pull run statue
				// since run is now started
				runListener.onTaskInProgress(result);
			}
			return null;
		}
	}

	/**
	 * Background Task to retrieve run states
	 * 
	 * @author Hyde
	 */
	private class RunProgressListenerInvoker implements CallbackTask{
		
		private int pollInterval;
		
		public RunProgressListenerInvoker(){
			pollInterval = Integer.parseInt(sharedPrefs.getString("runStateRefreshFrequency", "500"));
		}

		public void Execute(){
			runStatesPullingTaskHandler = new BackgroundTaskHandler();
			runStatesPullingTaskHandler.StartBackgroundTask(currentActivity, this, null);
		}

		public Object onTaskInProgress(Object... param) {
			// it will set the statue of run 
			// which triggers the outputHandler to run
			reportRunState();
			reportRunStartTime();
			// Sleep for pre-set seconds in the 
			// background thread before poll again
			try {
				Thread.sleep(pollInterval);
			} catch (InterruptedException e) {
				// swallow
				e.printStackTrace();
			}
			return null;
		}

		public Object onTaskComplete(Object... result) {
			// inform monitor to update statues
			if(runListener != null){
				runListener.onTaskInProgress();
			}

			// pull state again
			if(!runStatue.equals("Finished")){
				this.Execute();
			}
			else{
				if(runListener != null){
					// inform monitor to get finished time
					// since run is now finished
					runListener.onTaskComplete();
				}
			}

			return null;
		}
	}

	/**
	 * Background Task to retrieve outputs
	 * 
	 * @author Hyde
	 */
	private class OutputHanlder implements CallbackTask{

		private HashMap<String, String> outputs = new HashMap<String, String>();

		//supported output type
		// TODO: more types support......
		public static final String PORT_ERROR_TYPE = "application/x-error";
		//public static final String PORT_LIST_TYPE = "application/x-list";
		public static final String PORT_TEXT_TYPE = "text/plain";
		public static final String PORT_IMAGE_TYPE = "image/png";

		private ArrayList<Object> outputPortsValue;
		private int portDepth;
		private String currentPortName;
		// string used to build the outputs directory path
		private String workflowTitle;

		public void Execute(Object... params){
			workflowTitle = (String) params[0];
			if(workflowTitle == null){
				throw new NullPointerException("\"workflowTitle\" can not be null");
			}
			outputHandlingTaskHandler = new BackgroundTaskHandler();
			outputHandlingTaskHandler.StartBackgroundTask(
					currentActivity, this, "Preparing outputs...", params);
		}

		public Object onTaskInProgress(Object... params) {
			String runId = (String) params[1];

			Run theRun = null;
			if(runId != null){
				try {
					Server server = ta.getServer();
					theRun = server.getRun(runId, ta.getDefaultUser());
				} catch (NetworkConnectionException e1) {
					return "Connection problem reading data from server";
				}
			}
			
			if(theRun == null){
				theRun = ta.getWorkflowRunLaunched();
			}
			
			try {
				if (theRun.isRunning()){
					waitForWorkflowRun(theRun);
				}
			} catch (NetworkConnectionException e) {
				return "Connection problem reading data from server";
			}

			outputPortsValue = new ArrayList<Object>();
			Map<String, OutputPort> OutputPorts;
			try {
				OutputPorts = theRun.getOutputPorts();
			} catch (NetworkConnectionException e) {
				return "Connection problem reading data from server";
			}
			Iterator<Entry<String, OutputPort>> it = OutputPorts.entrySet().iterator();
			// for one output ports
			while(it.hasNext()){
				Map.Entry<String, OutputPort> pair = it.next();
				// high level determined variables
				OutputPort outPort = pair.getValue();
				portDepth = outPort.getDepth();
				currentPortName = outPort.getName();

				// undetermined variable
				PortValue portValue = outPort.getValue();

				Object data = null;
				// no list, single value
				if(portDepth == 0){
					data = retrieveSingleDepthData(portValue);
					outputPortsValue.add(data);
				}
				else if (portDepth > 0){
					// this method should eventually return ArrayList<Object>
					// the return type declared in its signature is for 
					// recursively collect data
					data = retrieveMultiDepthData(portDepth, portValue);
					// add the collection
					outputPortsValue.add(data);
				}

				// string representation of "outputPortsValue"
				String textToDisplay = "";
				textToDisplay = constructString(textToDisplay, data, 0);

				outputs.put(pair.getKey(), textToDisplay);
			}

			// set global outputs (output of most recent run) holder
			ta.setOutputs(outputs);

			return outputs;
		}

		private String retrieveSingleDepthData(PortValue portValue){

			// at this point so it should be safe to get value
			// without throwing UnsupportedOperationException
			byte[] data;
			try {
				data = portValue.getData();
			} catch (NetworkConnectionException e1) {
				return "Connection problem reading data from server";
			}
			while(data == null){
				try {
					// wait for 1 second
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			long dataSize = portValue.getDataSize();
			// check types
			String contentType = portValue.getContentType();
			String outputsSubPath = "/TavernaAndroid/Outputs/" 
					+ runStartTime +"/" + workflowTitle +"/" + currentPortName + "/";
			
			if(contentType.equals(PORT_TEXT_TYPE)){				
				String locationToStore = getFileSaveLocation(outputsSubPath);
				try {
					FileOutputStream stream = new FileOutputStream(locationToStore+"/output.txt"); 
					stream.write(data);
					stream.flush();
					stream.close();
				}catch(IOException e){
					e.printStackTrace();	
				}
				// if text is too large to fit in the screen
				// store only
				if (dataSize > 4096){
					return "(Output saved in output folder)";
				}
				else{
					String dataString = new String(data);
					return dataString;
				}
			}
			else if(contentType.equals(PORT_IMAGE_TYPE)){
				// store image under the sub folder named
				// after port name 
				String locationToStore = getFileSaveLocation(outputsSubPath);
				try {
					FileOutputStream stream = new FileOutputStream(locationToStore+"/output.png"); 
					stream.write(data);
					stream.flush();
					stream.close();

					return "(Image saved in output folder)";
				}catch(IOException e){
					e.printStackTrace();	
				}
			}
			else if (contentType.equals(PORT_ERROR_TYPE)){
				// if error text is too large to fit in the screen
				// store it 
				if (dataSize > 4096){
					String locationToStore = getFileSaveLocation(outputsSubPath);
					try {
						FileOutputStream stream = new FileOutputStream(locationToStore+"/error.txt"); 
						stream.write(data);
						stream.flush();
						stream.close();

						return "(error message saved in output folder)";
					}catch(IOException e){
						e.printStackTrace();	
					}
				}
				else{
					String dataString = new String(data);
					return dataString;
				}
			}
			else{
				return "The output data type is currently not supported";
			}

			// should never return byte[]
			return null;
		}

		private Object retrieveMultiDepthData(int depth, PortValue portValue){

			// prepare a list to store all elements in current level
			ArrayList<Object> dataList = new ArrayList<Object>();

			// if it hasn't reached the bottom
			if(depth != 0){
				// for every single element
				int listSize = portValue.size();
				for(int i = 0; i < listSize; i++){
					// pick one (every one)
					PortValue subPortValue = portValue.get(i);
					// go in deeper of it
					depth--;
					// eventually what returned below should be 
					// single data in current level
					Object data = retrieveMultiDepthData(depth, subPortValue);
					dataList.add(data);
				}

				// when all elements of the list are collected
				// return this collection for upper level to add them in
				// upper level collection
				return dataList;
			}

			//reached bottom
			String data = retrieveSingleDepthData(portValue);

			// return the single data when it reached bottom
			// only recursive return should be done here
			return data;
		}

		private String constructString(String result, Object object, int indent){
			if (object instanceof ArrayList<?>){
				ArrayList<Object> arraydata = (ArrayList<Object>) object;
				for(Object elements : arraydata){
					result += constructString(result, elements, indent);
				}
				// add indent for one collection
				StrBuilder message = new StrBuilder();
				message.appendPadding(indent, ' ');
				message.appendln(result);
				result = message.toString();
			}
			else if(object instanceof String){

				result += (String)object + "\n";	
			}

			return result;
		}


		public Object onTaskComplete(Object... result) {
			outputRetrievalListener.onTaskComplete(result);
			// delete the run
			// the run has to be delete 
			// since it can't be restarted once it's finished
			new RunCleaner().Execute(null, 0);

			return null;
		}
	}

	// return state of the run which is retrieved by using Run ID
	private class RunStateChecker implements CallbackTask{

		public void Execute(Object... params){
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(currentActivity, this, "Checking Run State...", params);
		}

		public Object onTaskInProgress(Object... param) {
			String runID = (String) param[0];
			//String runState = getRunStateWithID(runID);
			Server server = ta.getServer();
			Run theRun = null;
			try {
				theRun = server.getRun(runID, ta.getDefaultUser());
			} catch (NetworkConnectionException e) {
				return "Connection problem reading data from server";
			}
			if(theRun != null){
				ta.setWorkflowRunLaunched(theRun);
				RunStatus statu;
				try {
					statu = theRun.getStatus();
				} catch (NetworkConnectionException e) {
					return "Connection problem reading data from server";
				}
				runStatue = getRunState(statu);
			}
			return runStatue;
			//return runState;
		}

		public Object onTaskComplete(Object... result) {
			runStateCheckListener.onTaskComplete(result);
			return null;
		}
	}

	/**
	 * Class to send delete request to the server
	 * 
	 * a "mode" parameter has to be passed into the Execute method
	 * 
	 * mode 0 - clean up the run (most recent) whose outputs has been retrieved
	 * mode 1 - delete a specific/set of run. A Run object has to be passed in
	 * 			in this mode
	 * mode 2 - delete all Runs of current Server user
	 * 
	 * @author Hyde Zhang
	 *
	 */
	private class RunCleaner implements CallbackTask{

		public void Execute(Object... params){
			String message = null;
			if(params[0] instanceof String){
				message = (String) params[0];
			}
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(currentActivity, this, message, params);
		}

		public Object onTaskInProgress(Object... params) {
			int mode = (Integer) params[1];

			switch (mode){
			case 0:
				Run run = ta.getWorkflowRunLaunched();
				if(run != null){
					try {
						boolean deleteOutcome = run.delete();
						// delete database record
						currentActivity.getContentResolver().delete(
								DataProviderConstants.RUN_TABLE_CONTENTURI, 
								DataProviderConstants.Run_Id + "= ?", 
								new String[] {run.getIdentifier()});
						
						return deleteOutcome;
						
					} catch (NetworkConnectionException e) {
						return e.getMessage();
					}
				}
				break;
			case 1:
				ArrayList<String> idsOfRunsToDelete = (ArrayList<String>) params[2];
				Run runToDelete = null;
				String state = null;
				if(idsOfRunsToDelete == null){
					throw new IllegalArgumentException(
							"Run ID has to be passed in as the third parameter,"
								+" when in deletion mode 1");
				}
				
				// flag indicated whether all delete has been successful
				boolean succeed = false;
				// Initialize the where args to be used in delete
				// while iterating over the array list
				String[] whereArgs = new String[idsOfRunsToDelete.size()];
				try {
					for(int i = 0; i<idsOfRunsToDelete.size(); i++){
						String id = idsOfRunsToDelete.get(i);
						runToDelete = ta.getServer().getRun(id, ta.getDefaultUser());
						if(runToDelete != null){
							state = getRunState(runToDelete.getStatus());
							
							//Run runToDelete = (Run) params[2];
							if(state != STATE_DELETED && idsOfRunsToDelete != null){
									succeed = runToDelete.delete();
							}
							// build the where args for database DELETE
							whereArgs[i] = id;
						}
					}
					// delete records from database
					String selection = null;
					if(whereArgs.length < 1){
						selection = DataProviderConstants.Run_Id + " IS NOT NULL";
						whereArgs = null;
					} else{
						selection = DataProviderConstants.Run_Id + "= ?";
					}
					currentActivity.getContentResolver().delete(
							DataProviderConstants.RUN_TABLE_CONTENTURI, 
							selection,
							whereArgs);
				}catch (NetworkConnectionException e) {
					succeed = false;
					return e.getMessage();
				}
				
				return succeed;
			case 2:
				try{
					ta.getServer().deleteAllRuns(ta.getDefaultUser());
					// delete all records from database
					currentActivity.getContentResolver().delete(
							DataProviderConstants.RUN_TABLE_CONTENTURI, 
							DataProviderConstants.WF_RUN_ID +" IS NOT NULL", null);
					return true;
				}
				catch(NetworkConnectionException e){
					return e.getMessage();
				}
			}
			return false;
		}

		public Object onTaskComplete(Object... result) {
			if(result[0] instanceof String){
				if(runDeletionListener != null){
					runDeletionListener.onTaskComplete(result);
				}
				return null;
			}
			boolean success = (Boolean) result[0];

			if(success){
				if(runDeletionListener != null){
					runDeletionListener.onTaskComplete(result);
				}
				// TODO: "log" has to be removed in release version
				Log.i("run deletion", "Run deleted successfully");
			}
			else if (!success){
				// TODO: "log" has to be removed in release version
				Log.i("run deletion", "Fail to delete the run");
			}

			return null;
		}
	}
	
	private class RunListRetriever implements CallbackTask{
		
		public void Execute(Object... params){
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(currentActivity, this, null, params);
		}

		@Override
		public Object onTaskInProgress(Object... param) {
			String exceptionMessage = null;
			
			Server server = ta.getServer();
			UserCredentials user = ta.getDefaultUser();
			HashMap<String, WorkflowRun> runIdsStates = null;
			// TODO: catch exception
			try{
				Collection<Run> runs = server.getRuns(user);
				// if no run found return the message
				if(runs.size() < 1){
					String message = "No runs found";
					return message; 
				}
				
				runIdsStates = new HashMap<String, WorkflowRun>();
				// return a <Run_Id, Run_state> map
				for(Run r : runs){
					String[] startTime = r.getStartTime().replace("T", " ").split("\\.");
					String[] endTime = r.getFinishTime().replace("T", " ").split("\\.");
					String state = getRunState(r.getStatus());
					
					WorkflowRun wfRun = new WorkflowRun();
					wfRun.setStartTime(startTime[0]);
					wfRun.setEndTime(endTime[0]);
					wfRun.setRunState(state);
					
					runIdsStates.put(r.getIdentifier(), wfRun);
				}
			}catch(NetworkConnectionException e){
				exceptionMessage = e.getMessage();
			}
			
			// if there was an exception return the exception message
			return exceptionMessage == null ? runIdsStates : exceptionMessage;
		}

		@Override
		public Object onTaskComplete(Object... result) {
			if(result[0] instanceof String){
				runListRetrieverListener.onTaskComplete((String)result[0]);
				return null;
			}
			HashMap<String, String> runIdsStates = (HashMap<String, String>) result[0];
			runListRetrieverListener.onTaskComplete(runIdsStates);
			return null;
		}
	}

	private void waitForWorkflowRunToStart(Run run) {
		try {
			while (run.getStatus() != RunStatus.RUNNING) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO: "log" has to be removed in release version
					Log.e("waiting for workflow run to start", e.getMessage());
				}
			}
		} catch (NetworkConnectionException e) {
			// ignore
			e.printStackTrace();
		}
	}

	private void waitForWorkflowRun(Run run) {
		try {
			while (run.getStatus() == RunStatus.RUNNING) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO: "log" has to be removed in release version
					Log.e("waiting for workflow run", e.getMessage());
				}
			}
		} catch (NetworkConnectionException e) {
			// ignore
			e.printStackTrace();
		}
	}
	
	private String getFileSaveLocation(String subPath) {

		String storeLocation = null;

		/**** store in SD card ****/
		File root = android.os.Environment.getExternalStorageDirectory();               

		//try to avoid folder name syntax error
		//by using simpler start time as folder name
		File dir = new File (root.getAbsolutePath() + subPath);
		if(dir.exists() == false) 
		{
			if(dir.mkdirs()){
				storeLocation = dir.getAbsolutePath();
				return storeLocation;
			}
			else{
				Toast.makeText(
						currentActivity,
						"Output can't be saved to external storage",
						Toast.LENGTH_LONG).show();
				return null;
			}
		}

		// if the directory does exist
		storeLocation = dir.getAbsolutePath();

		return storeLocation;
	}
}

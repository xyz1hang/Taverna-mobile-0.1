package cs.man.ac.uk.tavernamobile.io;

import java.util.HashMap;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowBE;
import cs.man.ac.uk.tavernamobile.server.WorkflowRunManager;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

public class RunMonitorScreen extends Activity implements CallbackTask {

	private TavernaAndroid ta;
	private WorkflowRunManager manager;

	private String startTime;
	private String state;
	private String endTime;
	private String runningTime;

	private TextView currentStateValue;
	private TextView startTimeValue;
	private TextView endTimeValue;
	private TextView runningTimeValue;
	private LinearLayout root;
	private ProgressBar pb;
	private Button cancelButton;
	private Button goToOutputButton;

	private Activity currentActivity;
	private HashMap<String, Object> userInputs;

	int notificationId = 0;
	private NotificationManager mNotificationManager;

	private int Activity_Starter_Code;

	private boolean running = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.run_monitor_screen);

		currentActivity = this;

		// get data passed in
		Bundle extras = getIntent().getExtras();
		final WorkflowBE workflowEntity = (WorkflowBE) extras.getSerializable("workflowEntity");
		userInputs = (HashMap<String, Object>) extras.getSerializable("userInputs");
		Activity_Starter_Code = extras.getInt("activity_starter");
		// command which indicate to start a new run or
		// monitoring an existing run
		String command = extras.getString("command");

		ta = (TavernaAndroid) getApplication();
		manager = new WorkflowRunManager(this);

		//UI components
		TextView workflowTitle = (TextView) findViewById(R.id.monitorWfTitle);
		TextView usernameValue = (TextView) findViewById(R.id.monitorUsernameValue);

		pb = (ProgressBar) findViewById(R.id.runProgressBar);

		currentStateValue = (TextView) findViewById(R.id.monitorCurrentStateValue);
		startTimeValue = (TextView) findViewById(R.id.monitorStartTimeValue);
		endTimeValue = (TextView) findViewById(R.id.monitorEndTimeValue);
		runningTimeValue = (TextView) findViewById(R.id.monitorRunningTimeValue);
		cancelButton = (Button) findViewById(R.id.cancelRunButton);
		goToOutputButton = (Button) findViewById(R.id.runMonitorOutputButton);

		root = (LinearLayout) findViewById(R.id.runEndStatLayout);

		//data setup
		workflowTitle.setText(workflowEntity.getTitle());
		usernameValue.setText(ta.getDefaultUser().getUsername());

		// TODO: start the run here ????
		if(command.equals("RunWorkflow")){
			manager.StartWorkflowRun(userInputs, workflowEntity, this);
		}
		else if (command.equals("MonitoringOnly")){
			manager.StartMonitoring(this);
		}

		// do notification
		running = true;
		startNotification(workflowEntity.getTitle());

		cancelButton.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(View v) {
				// no message and stop the most recent run
				manager.StopRun(null, null);
				running = false;
				state = "Stopped";
				mNotificationManager.cancelAll();//.cancel(notificationId);
				finish();
			}
		});

		goToOutputButton.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(View v) {
				running = false;
				mNotificationManager.cancelAll();//.cancel(notificationId);

				manager.getRunOutput(workflowEntity.getTitle(), null, new CallbackTask(){

					@Override
					public Object onTaskInProgress(Object... param) { return null; }

					@Override
					public Object onTaskComplete(Object... result) {
						
						if(result[0] instanceof String){
							MessageHelper.showMessageDialog(currentActivity, (String)result[0]);
							return null;
						}
						// TODO : prepare output tree view
						
						Intent goToOutput = new Intent(currentActivity, OutputsList.class);
						Bundle extras = new Bundle();
						extras.putSerializable("workflowEntity", workflowEntity);
						extras.putInt("activity_starter", Activity_Starter_Code);
						goToOutput.putExtras(extras);
						currentActivity.startActivity(goToOutput);
						return null;
					}
					
				});
			}
		});
	}

	public Object onTaskInProgress(Object... param) {
		state = manager.getRunStatue();
		currentStateValue.setText(state);

		startTime = manager.getRunStartTime();
		if (startTime != null && !startTime.equals("")){
			startTimeValue.setText(startTime);
		}
		return null;
	}

	public Object onTaskComplete(Object... result) {
		if(result.length > 0 && result[0] instanceof String){
			MessageHelper.showMessageDialog(currentActivity, (String)result[0]);
			return null;
		}
		running = false;
		new RunEndStatRetriever().Execute();
		return null;
	}

	private class RunEndStatRetriever implements CallbackTask{

		public void Execute(){
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(currentActivity, this, null);
		}

		public Object onTaskInProgress(Object... param) {
			String endTimeString = manager.reportRunFinishTime();
			endTime = endTimeString.split("/")[0];
			runningTime = endTimeString.split("/")[1];
			return null;
		}

		public Object onTaskComplete(Object... result) {
			endTimeValue.setText(endTime);
			runningTimeValue.setText(runningTime);
			pb.setVisibility(8);
			root.setVisibility(0);
			cancelButton.setVisibility(8);
			goToOutputButton.setVisibility(0);
			return null;
		}
	}

	private void startNotification(final String wftitle){
		notificationId++;

		// display notification
		final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(currentActivity)
		.setSmallIcon(R.drawable.running)
		.setContentTitle("Running \""+wftitle+"\"")
		.setContentText("The run is in progress..");

		mNotificationManager =
				(NotificationManager) currentActivity.getSystemService(Context.NOTIFICATION_SERVICE);

		// Start a lengthy operation in a background thread
		new Thread(
				new Runnable() {
					public void run() {
						while (running){

							mBuilder.setProgress(0, 0, true);
							// Displays the progress bar for the first time.
							mNotificationManager.notify(notificationId, mBuilder.build());

							// wait for 3 seconds before request the status again
							try {
								// Sleep for 3 seconds
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								// TODO: "log" has to be removed in release version
								Log.d("sleep", "sleep failure");
							}
						}
						if(state.equals("Stopped")){
							// When the run is stopped, updates the notification
							mBuilder.setContentTitle("\""+wftitle+"\"");
							mBuilder.setContentText("Run Stopped");
						}
						else if (state.equals("Finished")){
							// When the run is finished, updates the notification
							mBuilder.setContentTitle("\""+wftitle+"\"");
							mBuilder.setContentText("Run complete");
						}
						// Removes the progress bar
						mBuilder.setProgress(0,0,false);
						mNotificationManager.notify(notificationId, mBuilder.build());
					}	
				}
				).start();
	}
}
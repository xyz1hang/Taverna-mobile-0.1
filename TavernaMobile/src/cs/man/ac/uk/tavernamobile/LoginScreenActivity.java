package cs.man.ac.uk.tavernamobile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import uk.org.taverna.server.client.NetworkConnectionException;
import uk.org.taverna.server.client.Run;
import uk.org.taverna.server.client.Server;
import uk.org.taverna.server.client.connection.HttpBasicCredentials;
import uk.org.taverna.server.client.connection.UserCredentials;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;
import cs.man.ac.uk.tavernamobile.utils.WorkflowFileLoader;

public class LoginScreenActivity extends Activity implements CallbackTask {

	private String[] serverAddressList;
	private String serverAddress;
	private Server server;
	private UserCredentials defaultUser;

	private LoginScreenActivity currentActivity;
	private TavernaAndroid ta;

	private final static String TEST_WF_PATH = "/Test data/simple.t2flow";

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.login_taverna_server);

		ta = (TavernaAndroid) getApplication();
		currentActivity = this;

		// UI Components
		final EditText username = (EditText) findViewById(R.id.loginUsernameValue);
		final EditText password = (EditText) findViewById(R.id.loginPasswordValue);
		Spinner serverSpinner = (Spinner) findViewById(R.id.serverListSpinner);
		Button btnSubmit = (Button) findViewById(R.id.loginButton);

		// data setup
		serverAddressList = new String[]{"https://eric.rcs.manchester.ac.uk:8443/taverna-server-2" /*, 
										 "http://leela.cs.man.ac.uk:8080/taverna242"*/};

		List<String> list = new ArrayList<String>();
		list.add("Eric");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		serverSpinner.setAdapter(dataAdapter);
		serverSpinner.setOnItemSelectedListener(new SpinnerItemSelectedListener());

		btnSubmit.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(View v) {
				// check for Internet connection
				SystemStatesChecker checker = new SystemStatesChecker(currentActivity);
				if (!(checker.isNetworkConnected())){
					return;
				}
				
				String usernamevalue = username.getText().toString().trim();
				String passwordvalue = password.getText().toString().trim();

				if(usernamevalue == null || usernamevalue.equals("")){
					MessageHelper.showMessageDialog(currentActivity, "Please type in username");
				}
				else if(passwordvalue == null || passwordvalue.equals("")){
					MessageHelper.showMessageDialog(currentActivity, "Please type in password");
				}
				else{
					try {
						URI serverURI = new URI(serverAddress);
						server = new Server(serverURI);
						defaultUser = new HttpBasicCredentials(usernamevalue, passwordvalue);
					} catch (Exception e) {
						MessageHelper.showMessageDialog(currentActivity, e.getMessage());
					}

					BackgroundTaskHandler handler = new BackgroundTaskHandler();
					handler.StartBackgroundTask(currentActivity, currentActivity, "Authenticating...");
				}
			}
		});
	}

	private class SpinnerItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			serverAddress = serverAddressList[pos];

			Toast.makeText(parent.getContext(), 
					"Selected Server address : " + serverAddress,
					Toast.LENGTH_LONG).show();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			Toast.makeText(parent.getContext(), 
					"Please selected a server",
					Toast.LENGTH_SHORT).show();
		}
	}

	public Object onTaskInProgress(Object... param) {
		String exceptionMessage = null;
		//TODO: no other way to authenticate server user at the moment
		//Update: no need to (internal users...)
		
		Run run = null;
		try{
			byte[] test = new WorkflowFileLoader().loadResource(TEST_WF_PATH);
			run = Run.create(server, test, defaultUser);
		} catch (Exception e){
			// TODO: connection problem =\= invalid user name or password
			exceptionMessage = "Invalid Username or Password."; // \nDetail : " + e.getMessage();
		} finally{
			if(run != null){
				try {
					run.delete();
				} catch (NetworkConnectionException e) {
					MessageHelper.showMessageDialog(this, e.getMessage());
				}
			}
		}

		return exceptionMessage;
	}

	public Object onTaskComplete(Object... result) {
		String exceptionMessage = (String) result[0];
		if (exceptionMessage != null){
			MessageHelper.showMessageDialog(currentActivity, exceptionMessage);
		}
		else{
			ta.setServer(server);
			ta.setDefaultUser(defaultUser);

			Intent mainIntent = new Intent(this, MainActivity.class);
			LoginScreenActivity.this.startActivity(mainIntent);
			LoginScreenActivity.this.finish();
		}
		return null;
	}
}
package cs.man.ac.uk.tavernamobile;

import uk.org.taverna.server.client.NetworkConnectionException;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import cs.man.ac.uk.tavernamobile.datamodels.MyExperimentSession;
import cs.man.ac.uk.tavernamobile.datamodels.User;
import cs.man.ac.uk.tavernamobile.myexperiment.HttpRequestHandler;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.ImageRetriever;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

public class MyExperimentLogin extends Activity implements
		CallbackTask {

	private MyExperimentLogin currentActivity;
	private static HttpRequestHandler requestHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_myexperiment);

		currentActivity = this;
		requestHandler = new HttpRequestHandler(this);
		
		this.overridePendingTransition(R.anim.push_left_in, 0);

		// UI components
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		final EditText username = (EditText) findViewById(R.id.myeloginUsernameValue);
		final EditText password = (EditText) findViewById(R.id.myeloginPasswordValue);
		Button btnSubmit = (Button) findViewById(R.id.myeloginButton);

		btnSubmit.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(View v) {
				// check for Internet connection
				SystemStatesChecker checker = new SystemStatesChecker(currentActivity);
				if (!checker.isNetworkConnected()) {
					return;
				}

				String usernamevalue = username.getText().toString().trim();
				String passwordvalue = password.getText().toString().trim();

				if (usernamevalue == null || usernamevalue.equals("")) {
					MessageHelper.showMessageDialog(currentActivity,
							"Please type in username");
				} else if (passwordvalue == null || passwordvalue.equals("")) {
					MessageHelper.showMessageDialog(currentActivity,
							"Please type in password");
				} else {
					BackgroundTaskHandler handler = new BackgroundTaskHandler();
					handler.StartBackgroundTask(currentActivity, currentActivity,
							"Authenticating...", usernamevalue, passwordvalue);
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Object onTaskInProgress(Object... param) {
		String username = (String) param[0];
		String password = (String) param[1];
		
		// Check the correctness of username and password first
		String whoAmI = "http://www.myexperiment.org/whoami.xml";
		User currentUser = null;
		String responseMessage = null;
		try {
			currentUser = (User) requestHandler.Get(whoAmI, User.class,
					username, password);
			
			// load and cache the user avatar
			// so that it can be retrieved from cache
			// when needed rather than over network request
			String avatarURI = currentUser.getAvatar().getResource();
			Bitmap avatar = new ImageRetriever().retrieveAvatarImage(avatarURI);
			LruCache<String, Bitmap> imageCache = TavernaAndroid.getmMemoryCache();
			imageCache.put(avatarURI, avatar);
			TavernaAndroid.setmMemoryCache(imageCache);
			
			// store the name of the user in "Application"
			// the Get will throw exception if the login 
			// details are not correct
			TavernaAndroid.setMyEUserLoggedin(currentUser);

			// then try to get cookies
			String location = "http://www.myexperiment.org/session";
			// construct an object which will be serialized to
			// the required XML document for sending username
			// and password to myExperiment
			MyExperimentSession loginMessage = new MyExperimentSession();
			loginMessage.setUsername(username);
			loginMessage.setPassword(password);

			// POST request will set global cookieStore
			requestHandler.Post(location, loginMessage, "application/xml");
			
		}catch(NetworkConnectionException e){
			responseMessage = e.getMessage();
		}catch(Exception e) {
			responseMessage = e.getMessage();
		}finally{
			if (responseMessage.equals("Unauthorized request")) {
				responseMessage = "Invalid username or passowrd";
			}
		}

		return responseMessage;
	}

	@Override
	public Object onTaskComplete(Object... result) {
		String responseMessage = (String) result[0];
		if (responseMessage != null) {
			MessageHelper.showMessageDialog(currentActivity, responseMessage);
		} else {
			// TavernaAndroid.getMyEUserLoggedin();
			// store the cookie retrieved and quit the activity
			Intent goBackToMain = new Intent(currentActivity, MainActivity.class);
			goBackToMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(goBackToMain);
		}
		return null;
	}
}

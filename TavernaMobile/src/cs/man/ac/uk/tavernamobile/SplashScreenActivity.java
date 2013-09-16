package cs.man.ac.uk.tavernamobile;

import uk.org.taverna.server.client.NetworkConnectionException;
import cs.man.ac.uk.tavernamobile.datamodels.MyExperimentSession;
import cs.man.ac.uk.tavernamobile.datamodels.User;
import cs.man.ac.uk.tavernamobile.myexperiment.HttpRequestHandler;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.ImageRetriever;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.LruCache;

public class SplashScreenActivity extends Activity implements CallbackTask {

	private final int SPLASH_DISPLAY_LENGHT = 1500;
	private final String INVALID_USERNAME_MESSAGE = "Saved username or passowrd is invalid, "
													+"please login to myExperiment again.";
	private Activity thisActivity;
	private SplashScreenActivity thisClass;

	private SharedPreferences loginPreferences;
	private static HttpRequestHandler requestHandler;
	private String username, passwords;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.splash_screen);
		
		thisActivity = this;
		thisClass = this;
		requestHandler = new HttpRequestHandler(thisActivity);
		
		loginPreferences = getSharedPreferences("loginPreference", MODE_PRIVATE);
		if (loginPreferences == null){
			navigateToActivity(MainActivity.class);
			return;
		}
		
		// retrieving saved login details
		boolean usernameSaved = loginPreferences.getBoolean("usernameSaved",false);
		if (usernameSaved) {
			username = loginPreferences.getString("username", "");
			boolean passwordSaved = loginPreferences.getBoolean("passwordSaved", false);
			if (passwordSaved) {
				passwords = loginPreferences.getString("password", "");
			}
		}
		
		// username and password both not null
		// try to automatically login
		if (username != null && passwords != null) {
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(thisActivity, thisClass, null);
		}
		// else if passwords is null
		// let user type in passwords
		else if (username != null && passwords == null){
			navigateToActivity(MyExperimentLogin.class);
		} else{
			navigateToActivity(MainActivity.class);
		}
	}

	@Override
	public Object onTaskInProgress(Object... param) {
		// Check the correctness of username and password first
		String whoAmI = "http://www.myexperiment.org/whoami.xml";
		Object response = null;
		String responseMessage = null;
		try {
			response = requestHandler.Get(whoAmI, User.class, username, passwords);

			if (response instanceof User) {
				User currentUser = (User) response;
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
				loginMessage.setPassword(passwords);

				// POST request will set global cookieStore
				requestHandler.Post(location, loginMessage, "application/xml");
			} else if (response instanceof String) {
				responseMessage = (String) response;
				if (responseMessage.equals("Unauthorized request")) {
					// rewrite message
					responseMessage = INVALID_USERNAME_MESSAGE;
				}
			}
		} catch (NetworkConnectionException e) {
			responseMessage = e.getMessage();
		} catch (Exception e) {
			responseMessage = e.getMessage();
		}

		return responseMessage;
	}

	@Override
	public Object onTaskComplete(Object... result) {
		String responseMessage = (String) result[0];
		if (responseMessage != null) {
			if(responseMessage.equals(INVALID_USERNAME_MESSAGE)){
				MessageHelper.showOptionsDialog(thisActivity, 
					INVALID_USERNAME_MESSAGE, 
					"Login Details Changed", 
					new CallbackTask(){
						@Override
						public Object onTaskInProgress(Object... param) {
							Intent mainIntent = 
									new Intent(SplashScreenActivity.this, MyExperimentLogin.class);
							thisActivity.startActivity(mainIntent);
							thisActivity.overridePendingTransition(
									R.anim.push_left_in, R.anim.push_left_out);
							thisActivity.finish();
							return null;
						}

						@Override
						public Object onTaskComplete(Object... result) { return null; }
					}, 
					new CallbackTask(){
						@Override
						public Object onTaskInProgress(Object... param) {
							navigateToActivity(MainActivity.class);
							return null;
						}
						@Override
						public Object onTaskComplete(Object... result) { return null; }
					});
			}
			MessageHelper.showMessageDialog(this, null, responseMessage, null);
		} else {
			navigateToActivity(MainActivity.class);
		}
		return null;
	}

	private void navigateToActivity(final Class<?> targetActivity) {
		/*
		 * New Handler to start the the Main activity and close this
		 * Splash-Screen after 1.5 seconds.
		 */
		new Handler().postDelayed(new Runnable() {
			public void run() {
				Intent intent = new Intent(SplashScreenActivity.this, targetActivity);
				thisActivity.startActivity(intent);
				thisActivity.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				thisActivity.finish();
			}
		}, SPLASH_DISPLAY_LENGHT);
	}
}
package cs.man.ac.uk.tavernamobile;

import uk.org.taverna.server.client.NetworkConnectionException;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import cs.man.ac.uk.tavernamobile.datamodels.MyExperimentSession;
import cs.man.ac.uk.tavernamobile.datamodels.User;
import cs.man.ac.uk.tavernamobile.myexperiment.HttpRequestHandler;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.ImageRetriever;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

public class MyExperimentLogin extends Activity implements CallbackTask {

	// references
	private Activity currentActivity;
	private MyExperimentLogin currentClass;
	
	// utilities
	private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
	private static HttpRequestHandler requestHandler;
	
	// UI elements
	private EditText username, password;
	private Button btnSubmit, btnBack;
	private CheckBox saveUserNameCb, savePasswordCb;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_myexperiment);

		currentActivity = this;
		currentClass = this;
		requestHandler = new HttpRequestHandler(currentActivity);
		
		this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		
		// UI components
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		username = (EditText) findViewById(R.id.myeloginUsernameValue);
		password = (EditText) findViewById(R.id.myeloginPasswordValue);
		btnSubmit = (Button) findViewById(R.id.myeloginButton);
		btnBack = (Button) findViewById(R.id.myeloginBackButton);
		saveUserNameCb = (CheckBox) findViewById(R.id.saveUsernameCheckbox);
		savePasswordCb = (CheckBox) findViewById(R.id.savePasswordsCheckbox);
		
		loginPreferences = getSharedPreferences("loginPreference", MODE_PRIVATE);
		if (loginPreferences != null){
			boolean usernameSaved = loginPreferences.getBoolean("usernameSaved", false);
	        if (usernameSaved) {
	        	// retrieve saved username
	        	username.setText(loginPreferences.getString("username", ""));
	        	boolean passwordSaved = loginPreferences.getBoolean("passwordSaved", false);
	        	if(passwordSaved){
	        		password.setText(loginPreferences.getString("password", ""));
	        	}
	        	// restore check box states
	        	saveUserNameCb.setChecked(usernameSaved);
	        	savePasswordCb.setChecked(passwordSaved);
	        }
		}
		
		username.setOnKeyListener(new OnKeyListener() {

		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		          // If the event is a key-down event on the "enter" button
		          if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
		               (keyCode == KeyEvent.KEYCODE_ENTER)){
		        	  username.clearFocus();
		        	  password.requestFocus();
		              return true;
		          }
		          return false;
		    }
		});
		
		password.setSingleLine();
		password.setOnKeyListener(new OnKeyListener() {

		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		          // If the event is a key-down event on the "enter" button
		          if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
		               (keyCode == KeyEvent.KEYCODE_ENTER)){
		        	  password.clearFocus();
		        	  btnSubmit.requestFocus();
		        	  //hideKeyboard();
		              return true;
		          }
		          return false;
		    }
		});
		
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
					MessageHelper.showMessageDialog(
							currentActivity, null, "Please type in username", null);
				} else if (passwordvalue == null || passwordvalue.equals("")) {
					MessageHelper.showMessageDialog(
							currentActivity, null, "Please type in password", null);
				} else {
					BackgroundTaskHandler handler = new BackgroundTaskHandler();
					handler.StartBackgroundTask(currentActivity,
							currentClass, "Authenticating...",
							usernamevalue, passwordvalue);
				}
			}
		});
		
		btnBack.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});
		
		username.requestFocus();
		
		saveUserNameCb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton checkbutton, boolean isChecked) {
				if(isChecked){
					savePasswordCb.setEnabled(true);
				}else{
					savePasswordCb.setEnabled(false);
				}
			}
		});
	}// end of onCreate

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent goBackToMain = new Intent(this, MainActivity.class);
			goBackToMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(goBackToMain);
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
		Object response = null;
		String responseMessage = null;
		try {
			response = requestHandler.Get(whoAmI, User.class, username, password);
			
			if(response instanceof User){
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
				
				// prepare to save username and password
				loginPreferences = getSharedPreferences("loginPreference", MODE_PRIVATE);
				// edit will save to "loginPreference"
		        loginPrefsEditor = loginPreferences.edit();
		        
		        if(saveUserNameCb.isChecked()){
		        	loginPrefsEditor.putBoolean("usernameSaved", true);
	                loginPrefsEditor.putString("username", username);
	                if (savePasswordCb.isChecked()) {
	                	loginPrefsEditor.putBoolean("passwordSaved", true);
	                	loginPrefsEditor.putString("password", password);
	                }
	                loginPrefsEditor.commit();
	            } else {
	                loginPrefsEditor.clear();
	                loginPrefsEditor.commit();
	            }

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
			}else if (response instanceof String){
				responseMessage = (String) response;
				if (responseMessage.equals("Unauthorized request")) {
					// rewrite message
					responseMessage = "Invalid username or passowrd";
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
			MessageHelper.showMessageDialog(this, null, responseMessage, null);
		} else {
			/*menu.setSlidingEnabled(true);
			FragmentTransaction ft =
					parentActivity.getSupportFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
			ft.remove(currentClass).commit();*/
			
			/*FragmentTransaction ft =
					parentActivity.getSupportFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
			Fragment newFragment = new MainFragment();
			ft.addToBackStack("mainFragment");
			ft.replace(R.id.main_panel_root, newFragment).commit();*/
			
			Intent goBackToMain = new Intent(this, MainActivity.class);
			goBackToMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(goBackToMain);
		}
		return null;
	}
	
	/*// method that hide the soft keyboard
	private void hideKeyboard(){
		InputMethodManager inputManager = (InputMethodManager)
				currentActivity.getSystemService(Context.INPUT_METHOD_SERVICE); 

		inputManager.hideSoftInputFromWindow(currentActivity.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}*/

	@Override
	public void finish() {
		this.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		super.finish();
	}
}

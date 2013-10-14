package cs.man.ac.uk.tavernamobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

import cs.man.ac.uk.tavernamobile.datamodels.User;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

public class SlidingMenuFragment extends Fragment {
	
	private FragmentActivity parentActivity;
	private TextView myExperimentLoginText;
	private View menuView;
	private LinearLayout listRoot;
	private LayoutInflater layoutInflater;
	private Typeface font;
	
	private int previouslySelectedFragIndex;
	private String previousSelectedFragTag;
	
    final static private String APP_KEY = "d3ce89fwxc41yep";
    final static private String APP_SECRET = "2ff9zujejcgu6sr";
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;

    // You don't need to change these, leave them alone.
    final static private String ACCOUNT_PREFS_NAME = "dropBoxPreference";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    final static private String LOGIN_FLAG = "LOGGEDIN";
	
	DropboxAPI<AndroidAuthSession> mApi;
	
	private String[] dataSourceNames;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		menuView = inflater.inflate(R.layout.sliding_list, null);
		listRoot = (LinearLayout) menuView.findViewById(R.id.slidingMenuRoot);
		//menuScroll = (ScrollView) menuView.findViewById(R.id.slidingMenuScroll);
		myExperimentLoginText = (TextView) menuView.findViewById(R.id.myExperimentLoginState);
		refreshLoginState();
		return menuView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		parentActivity = this.getActivity();
		layoutInflater = ((LayoutInflater) parentActivity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		font = Typeface.createFromAsset(parentActivity.getAssets(), "Roboto-Light.ttf");
		myExperimentLoginText.setTypeface(font);
		myExperimentLoginText.setTextSize(20);
		myExperimentLoginText.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					User user = TavernaAndroid.getMyEUserLoggedin();
					if (user != null) {
						MessageHelper.showOptionsDialog(parentActivity,
							"Do you wish to log out ?", 
							"Attention",
							new CallbackTask() {
								@Override
								public Object onTaskInProgress(
										Object... param) {
									// Clear user logged-in and cookie
									TavernaAndroid.setMyEUserLoggedin(null);
									TavernaAndroid.setMyExperimentSessionCookies(null);
									TavernaAndroid.setMyWorkflows(null);
									TavernaAndroid.setFavouriteWorkflows(null);
									clearLoginPreference();
									parentActivity.recreate();
									//refreshLoginState();
									return null;
								}
	
								@Override
								public Object onTaskComplete(Object... result) {
									return null;
								}
							}, null);
					}else{
						((MainPanelActivity) parentActivity).getMenu().toggle();
						Intent gotoMyexperimentLogin = new Intent(parentActivity, MyExperimentLogin.class);
						parentActivity.startActivity(gotoMyexperimentLogin);
					}
				}
			});
		
		// create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        TavernaAndroid.setmApi(mApi);
        if(dropboxLoggedIn()){
        	getDropboxAccountInfo();
        }
	}
	
	@Override
	public void onResume() {
        super.onResume();
        // refresh login state when coming back to the fragment
        refreshLoginState();
        // if already loggedin we don't need to continue
        if(dropboxLoggedIn()){
        	// refresh Menu
            refreshMenus();
        	return;
        }
        // The next part must be inserted in the onResume() method of the
        // fragment from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        AndroidAuthSession session = mApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();
                // Store it locally in the app for later use
                TokenPair tokens = session.getAccessTokenPair();
                storeKeys(tokens.key, tokens.secret);
                // set the preference flag
                setLoggedin(true);
                getDropboxAccountInfo();
            } catch (IllegalStateException e) {
            	Toast.makeText(parentActivity, 
            			"Couldn't authenticate with Dropbox:", 
            			Toast.LENGTH_LONG).show();
            }
        }// end of if authentication successful
    }

	private void getDropboxAccountInfo() {
		// try to get account information to display
		new BackgroundTaskHandler().StartBackgroundTask(parentActivity, new CallbackTask(){
			@Override
			public Object onTaskInProgress(Object... param) {
				try {
					Account dropboxAccount = mApi.accountInfo();
					if(dataSourceNames != null){
						// at this stage the dataSourceNames array shouldn't be null
						dataSourceNames[0] = dropboxAccount.displayName;	
					}
				} catch (DropboxException e) {
					// Irrelevant message
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public Object onTaskComplete(Object... result) {
				// refresh Menu
                refreshMenus();
				return null;
			}
			
		}, null);
	}

	private void refreshMenus() {
		listRoot.removeAllViews();
		// Navigation Menu
		final User userloggedIn = TavernaAndroid.getMyEUserLoggedin();
		ListView navigationMenuList = null;
		String[] navigationMenuNames = null;
		int[] navigationMenuIcons =  null;
		if(userloggedIn != null){
			navigationMenuNames = new String[] {"Workflow Run Control", "Explore Workflows", "My Workflows"};
			navigationMenuIcons = new int[] {R.drawable.gear_icon, R.drawable.myexperiment_logo_small, R.drawable.bookmark_icon};
		}else{
			navigationMenuNames = new String[] {"Workflow Run Control", "Explore Workflows"};
			navigationMenuIcons = new int[] {R.drawable.gear_icon, R.drawable.myexperiment_logo_small};
		}
		navigationMenuList = setupList("Navigation", navigationMenuNames, navigationMenuIcons);
		
		// DataSourc Menu
		if(dataSourceNames == null){
			dataSourceNames = new String[] {"Dropbox", "Google Drive"};
		}
		int[] dataSourceIcons = new int[] {R.drawable.dropbox_icon, R.drawable.google_drive_icon};
		ListView dataSourceList = setupList("Data Source", dataSourceNames, dataSourceIcons);
		
		// Other Menu
		String[] otherMenuNames = null;
		int[] otherMenuIcons = null;
		if(userloggedIn == null){
			otherMenuNames = new String[] {"Settings"};
			otherMenuIcons = new int[] {R.drawable.settings_icon_dark};
		}
		else{
			otherMenuNames = new String[] {"Settings", "Sign Out"};
			otherMenuIcons = new int[] {R.drawable.settings_icon_dark, R.drawable.sign_out_icon};
		}
		ListView settingList = setupList("Others", otherMenuNames, otherMenuIcons);
		
		// list item click event setting
		if(navigationMenuList != null){
			navigationMenuList.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> theListView, View parentView, int itemIndex, long arg3) {
					if(previouslySelectedFragIndex == itemIndex){
						((MainPanelActivity) parentActivity).getMenu().toggle();
					} else{
						previouslySelectedFragIndex = itemIndex;
						if(itemIndex == 1){
								beginFragmentTransaction(new int[] {2, 3}, "RunsControlFragment");
						} else if(itemIndex == 2){
								beginFragmentTransaction(new int[] {0, 1}, "WorkflowsFragment");
						} else if(itemIndex == 3){
								beginFragmentTransaction(new int[] {4, 5}, "MyWorkflowsFragment");
						}
					}
				}
			});
		}
		
		dataSourceList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> theListView, View parentView, 
					int itemIndex, long arg3) {
				// TODO: setup dropbox and google drive
				if(itemIndex == 1){
					// log out if logged in, or vice versa
	                if (dropboxLoggedIn()) {
	                	MessageHelper.showOptionsDialog(parentActivity,
	    						"Do you wish to unlink with current dropbox account ?", 
	    						"Attention",
	    						new CallbackTask() {
	    							@Override
	    							public Object onTaskInProgress(Object... param) {
	    								logOutDropbox();
	    								return null;
	    							}

	    							@Override
	    							public Object onTaskComplete(Object... result) { return null; }
	    						}, null);
	                } else {
	                    // Start the remote authentication
	                    mApi.getSession().startAuthentication(parentActivity);
	                    
	                }// end of else
				}
				else if(itemIndex == 2){
				}
			}
		});
		
		settingList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> theListView, View parentView, 
					int itemIndex, long arg3) {
				if(itemIndex == 1){
					((MainPanelActivity) parentActivity).getMenu().toggle();
					Intent goToSetting = new Intent(parentActivity, SettingsActivity.class);
					parentActivity.startActivity(goToSetting);
				}
				else if(itemIndex == 2){
					MessageHelper.showOptionsDialog(parentActivity,
						"Do you wish to log out ?", 
						"Attention",
						new CallbackTask() {
							@Override
							public Object onTaskInProgress(
									Object... param) {
								// Clear user logged-in and cookie
								TavernaAndroid.setMyEUserLoggedin(null);
								TavernaAndroid.setMyExperimentSessionCookies(null);
								TavernaAndroid.setMyWorkflows(null);
								TavernaAndroid.setFavouriteWorkflows(null);
								parentActivity.recreate();
								//refreshLoginState();
								//clearLoginPreference();
								//refreshMenus();
								return null;
							}

							@Override
							public Object onTaskComplete(Object... result) {
								return null;
							}
						}, null);
				}// end of else if
			}
		});
	}
	
	/**
	 * method to populate one menu list
	 * 
	 * @param menuList
	 * @param listTitle
	 * @param menuNames
	 * @param menuIcons
	 */
	private ListView setupList(String listTitle, String[] menuNames, int[] menuIcons) {
		SimpleAdapter menuAdapter = new SimpleAdapter(getActivity());
		for(int i = 0; i < menuNames.length; i++){
			listObject menuObject = new listObject();
			menuObject.setText(menuNames[i]);
			menuObject.setResID(menuIcons[i]);
			menuAdapter.add(menuObject);
		}
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		View headerview = layoutInflater.inflate(R.layout.sliding_menu_list_header, null);
		TextView listHeaderName = (TextView) headerview.findViewById(R.id.sliding_menu_list_name);
		listHeaderName.setTypeface(font);
		listHeaderName.setText(listTitle);
		ListView menuList = new ListView(parentActivity);
		menuList.setLayoutParams(params);
		
		int sizeInPx = 10;
		float scale = getResources().getDisplayMetrics().density;
		int sizeInDp = (int) (sizeInPx*scale + 0.5f);
		menuList.setPadding(sizeInDp, 0, sizeInDp, 0);
		//menuList.setPadding(R.dimen.list_padding, 0, R.dimen.list_padding, 0);
		
		menuList.addHeaderView(headerview);
		menuList.setAdapter(menuAdapter);
		listRoot.addView(menuList, params);
		return menuList;
	}
	
	private void refreshLoginState() {
		User userLoggedin = TavernaAndroid.getMyEUserLoggedin();
		String userName = null;
		if (userLoggedin != null) {
			userName = userLoggedin.getName();
			Bitmap avatarBitmap = 
			 	TavernaAndroid.getmMemoryCache().get(userLoggedin.getAvatar().getResource());
			if(avatarBitmap != null){
				// TODO : fixed scaled to 80 x 80
				Drawable avatarDrawable = new BitmapDrawable(getResources(),
						Bitmap.createScaledBitmap(avatarBitmap, 80, 80, false));
				/*Rect outRect = new Rect();
				myExperimentLoginText.getDrawingRect(outRect);
				// resize the Rect
				//outRect.inset(-10, 10);
				avatarDrawable.setBounds(outRect);
				myExperimentLoginText.setCompoundDrawables(avatarDrawable, null, null, null);*/
				myExperimentLoginText.setCompoundDrawablesWithIntrinsicBounds(
						avatarDrawable, null, null, null);
			}
			myExperimentLoginText.setText("Logged in as:\n"+ userName);
		}else{
			Drawable defaultDrawable = getResources().getDrawable(R.drawable.myexperiment_logo_small);
			myExperimentLoginText.setCompoundDrawablesWithIntrinsicBounds(
					defaultDrawable, null, null, null);
			myExperimentLoginText.setText("Log in to myExperiment");
			myExperimentLoginText.setTextSize(15);
		}
	}
	
	private void clearLoginPreference(){
		SharedPreferences loginPreferences = 
				parentActivity.getSharedPreferences("loginPreference", Context.MODE_PRIVATE);
		if (loginPreferences != null){
			SharedPreferences.Editor loginPrefsEditor = loginPreferences.edit();
			boolean usernameSaved = loginPreferences.getBoolean("usernameSaved", false);
	        if (usernameSaved) {
	        	loginPrefsEditor = loginPreferences.edit();
	        	loginPrefsEditor.putBoolean("usernameSaved", false);
	        	loginPrefsEditor.putString("username", "");
	        	boolean passwordSaved = loginPreferences.getBoolean("passwordSaved", false);
	        	if(passwordSaved){
	        		loginPrefsEditor.putBoolean("passwordSaved", false);
	        		loginPrefsEditor.putString("password", "");
	        	}
	        	loginPrefsEditor.commit();
	        }
		}
	}
	
	/**
	 * @param fragmentsToInt - integer representation of fragments
	 *			0 - ExploreFragment, 1 - SearchResultFragment, 2 - RunsFragments
	 *  		3 - LaunchHistoryFragments, 4 - MyWorkflowFragment, 5 - FavouriteWorkflowFragment
	 *  
	 * @param backStackTag
	 */
	private void beginFragmentTransaction(int[] fragmentsToInt, String backStackTag){
		FragmentManager fm = parentActivity.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		// ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
        //boolean successfullypopped = fm.popBackStackImmediate(backStackTag, 1);
        //if(!successfullypopped){
        	Fragment newFragment = new FragmentsContainer();
    		Bundle args = new Bundle();
    		args.putIntArray("fragmentsToInstantiate", fragmentsToInt);
    		newFragment.setArguments(args);
    		ft.addToBackStack(backStackTag);
    		if(previousSelectedFragTag == null){
    			fm.beginTransaction().hide(fm.findFragmentByTag("StarterFragments")).commit();
    		}else{
    			fm.beginTransaction().hide(fm.findFragmentByTag(previousSelectedFragTag)).commit();
    		}
    		previousSelectedFragTag = backStackTag;
    		ft.replace(R.id.main_panel_root, newFragment, backStackTag).commit();
        //}
        // smooth transaction
		new Handler().postDelayed(new Runnable() {
			public void run() {
				// close the menu
				((MainPanelActivity) parentActivity).getMenu().toggle();
			}},500);
	}

	private class SimpleAdapter extends ArrayAdapter<listObject> {

		public SimpleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.sliding_row, null);
			}
			
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).getText());
			title.setTypeface(font);
			Drawable drawable = getResources().getDrawable(getItem(position).getResID());
			title.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

			return convertView;
		}
	}
	
	private class listObject{
		private String text;
		private int resID;
		
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public int getResID() {
			return resID;
		}
		public void setResID(int resID) {
			this.resID = resID;
		}
	}
	
	/**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     *
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private String[] getKeys() {
        SharedPreferences prefs = parentActivity.getSharedPreferences(ACCOUNT_PREFS_NAME, Context.MODE_PRIVATE);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void storeKeys(String key, String secret) {
        // Save the access key for later
    	SharedPreferences prefs = parentActivity
    			.getSharedPreferences(ACCOUNT_PREFS_NAME, Context.MODE_PRIVATE);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    private void clearKeys() {
    	SharedPreferences prefs = parentActivity
    			.getSharedPreferences(ACCOUNT_PREFS_NAME, Context.MODE_PRIVATE);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
    
    private void setLoggedin(boolean loggedIn){
    	SharedPreferences prefs = parentActivity
    			.getSharedPreferences(ACCOUNT_PREFS_NAME, Context.MODE_PRIVATE);
        Editor edit = prefs.edit();
    	edit.putBoolean(LOGIN_FLAG, loggedIn);
    	edit.commit();
    }
    
    private boolean dropboxLoggedIn(){
    	SharedPreferences prefs = parentActivity
    			.getSharedPreferences(ACCOUNT_PREFS_NAME, Context.MODE_PRIVATE);
    	boolean loggedIn = prefs.getBoolean(LOGIN_FLAG, false);
    	return loggedIn;
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
    
    private void logOutDropbox() {
        // Remove credentials from the session
        mApi.getSession().unlink();
        // Clear our stored keys
        clearKeys();
        
        if(dataSourceNames != null){
			dataSourceNames[0] = "Dropbox";
			refreshMenus();
		}
    }
}

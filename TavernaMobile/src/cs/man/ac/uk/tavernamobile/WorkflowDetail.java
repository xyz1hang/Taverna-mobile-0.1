package cs.man.ac.uk.tavernamobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.datamodels.License;
import cs.man.ac.uk.tavernamobile.datamodels.Privilege;
import cs.man.ac.uk.tavernamobile.datamodels.User;
import cs.man.ac.uk.tavernamobile.datamodels.Workflow;
import cs.man.ac.uk.tavernamobile.fragments.DetailsDescriptionFragment;
import cs.man.ac.uk.tavernamobile.fragments.DetailsLicenseFragment;
import cs.man.ac.uk.tavernamobile.fragments.DetailsPreviewFragment;
import cs.man.ac.uk.tavernamobile.myexperiment.HttpRequestHandler;
import cs.man.ac.uk.tavernamobile.server.WorkflowLaunchHelper;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.ImageRetriever;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;
import cs.man.ac.uk.tavernamobile.utils.WorkflowBE;

public class WorkflowDetail extends FragmentActivity implements
		CallbackTask {

	MyPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	// Workflow data for sub-fragments to access
	public Workflow workflow;
	public License license;
	public String licenseUri;
	private User uploader;

	// UI components and data of current screen
	// private ImageView avatar;
	private TextView myExperimentLoginText;
	private TextView title;
	private TextView userName;
	private Bitmap avatarBitmap;
	private android.support.v4.util.LruCache<String, Bitmap> imageCache;
	private HashMap<String, Object> mCache;

	// class variables
	private FragmentActivity currentActivity;
	// variable to keep track of previous activity
	// in order to navigate back to starting activity
	private int Activity_Starter_Code;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.workflow_detail);

		Activity_Starter_Code = 1;

		imageCache = TavernaAndroid.getmMemoryCache();
		mCache = TavernaAndroid.getmTextCache();
		currentActivity = this; // for the access of current activity in
								// OnClickListener

		// UI components
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		// avatar = (ImageView) findViewById(R.id.avatarImage);
		title = (TextView) findViewById(R.id.workflowTitle);
		TextView version = (TextView) findViewById(R.id.workflowVersion);
		userName = (TextView) findViewById(R.id.uploaderName);
		final Button launch = (Button) findViewById(R.id.workflowlaunchButton);

		/**
		 * data loading
		 **/
		// try to get data passed and then load other data e.g. license etc.
		workflow = (Workflow) getIntent().getSerializableExtra("workflow_details");

		// If no data passed in - activity restored etc.
		// get data from memory if the activity was in back stack
		if (workflow == null) {
			workflow = (Workflow) mCache.get("workflow");
			license = (License) mCache.get("license");
			uploader = (User) mCache.get("uploader");
		}
		// if it is not in Cache
		else if (workflow == null) {
			// try to get data from savedInstanceState if the activity
			// was killed due to low memory etc.
			if (savedInstanceState != null) {
				// try to get data from saved instance state
				if (workflow == null) {
					workflow = (Workflow) savedInstanceState.getSerializable("workflow");
				}
				if (license == null) {
					license = (License) savedInstanceState.getSerializable("license");
				}
				if (uploader == null) {
					uploader = (User) savedInstanceState.getSerializable("uploader");
				}
			} 
		}else if(workflow == null){
				// if data can't even be loaded from 
				// saved instance state, inform user 
				// to try start the activity again, 
				// rather than crash the application
				MessageHelper.showMessageDialog(
						currentActivity,
						"No workflow data found,"
						+ "please try again.\n(The message will be dismissed in 4 seconds)");
				
				new Handler().postDelayed(
					new Runnable() {
						public void run() {
							currentActivity.finish();
						}
					}, 
					4000);
				return;
		}

		/** "workflow" should never be null at this point **/
		title.setText(workflow.getTitle());
		version.setText("Version "+workflow.getVersion());
		// any of the following is null we retrieve data from the server
		if (license == null || uploader == null || avatarBitmap == null) {
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(this, this, "Loading workflow data...");
		}else{
			// load avatar image from cache
			avatarBitmap = imageCache.get(uploader.getAvatar().getResource());
		}

		launch.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(View v) {
				SystemStatesChecker sysChecker = new SystemStatesChecker(currentActivity);
				if (!(sysChecker.isNetworkConnected())) {
					return;
				}

				WorkflowBE workflowEntity = new WorkflowBE();
				workflowEntity.setTitle(workflow.getTitle());
				workflowEntity.setVersion(workflow.getVersion());
				workflowEntity.setWorkflow_URI(workflow.getContent_uri());
				workflowEntity.setUploaderName(workflow.getUploader().getValue());
				workflowEntity.setAvator(Bitmap.createScaledBitmap(avatarBitmap, 100, 100, true));

				List<String> privilegesStrings = new ArrayList<String>();
				List<Privilege> privileges = workflow.getPrivileges();
				for (Privilege privilege : privileges) {
					privilegesStrings.add(privilege.getType());
				}
				workflowEntity.setPrivileges(privilegesStrings);

				WorkflowLaunchHelper launchHelper = new WorkflowLaunchHelper(
						currentActivity, workflowEntity, Activity_Starter_Code);
				launchHelper.launch();
			}
		});

		// Set up fragments
		DetailsPreviewFragment previewFragment = new DetailsPreviewFragment();
		DetailsDescriptionFragment descriptionFragment = new DetailsDescriptionFragment();
		DetailsLicenseFragment licenseFragment = new DetailsLicenseFragment();

		mSectionsPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
		mSectionsPagerAdapter.addFragment(previewFragment);
		mSectionsPagerAdapter.addFragment(descriptionFragment);
		mSectionsPagerAdapter.addFragment(licenseFragment);

		mViewPager = (ViewPager) findViewById(R.id.workflowDetailsViewPager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setCurrentItem(0);
		
		myExperimentLoginText = (TextView) findViewById(R.id.wfdMyExperimentLoginState);
		myExperimentLoginText.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					User user = TavernaAndroid.getMyEUserLoggedin();
					if (user != null) {
						MessageHelper.showOptionsDialog(currentActivity,
								"Do you wish to log out ?", 
								"Attention",
								new CallbackTask() {
									@Override
									public Object onTaskInProgress(Object... param) {
										// Clear user logged-in and cookie
										TavernaAndroid.setMyEUserLoggedin(null);
										TavernaAndroid.setMyExperimentSessionCookies(null);
										refreshLoginState();
										return null;
									}

									@Override
									public Object onTaskComplete(Object... result) {return null;}
								}, null);
					}else{
						Intent gotoMyexperimentLogin = new Intent(
								currentActivity, MyExperimentLogin.class);
						currentActivity.startActivity(gotoMyexperimentLogin);
					}
				}
			});
	}

	@Override
	public void onStart() {
		refreshLoginState();
		super.onStart();
	}

	private void refreshLoginState() {
		User userLoggedin = TavernaAndroid.getMyEUserLoggedin();
		String userName = null;
		if (userLoggedin != null) {
			userName = userLoggedin.getName();
			/*Bitmap avatarBitmap = TavernaAndroid.getmMemoryCache().get(userLoggedin.getAvatar().getResource());
			if(avatarBitmap != null){
				Drawable avatarDrawable = new BitmapDrawable(getResources(),
						Bitmap.createBitmap(avatarBitmap));
				Rect outRect = new Rect();
				myExperimentLoginText.getDrawingRect(outRect);
				// resize the Rect
				//outRect.inset(-10, 10);
				avatarDrawable.setBounds(outRect);
				myExperimentLoginText.setCompoundDrawables(avatarDrawable, null, null, null);
			}*/
			myExperimentLoginText.setText("Logged in as: "+ userName);
		}else{
			myExperimentLoginText.setText("Log in to myExperiment");
		}
	}

	// load workflow data in background
	public Object onTaskInProgress(Object... params) {
		String exceptionMessage = null;
		HttpRequestHandler requestHandler = new HttpRequestHandler(currentActivity);
		String userprofileUri = workflow.getUploader().getUri();
		licenseUri = workflow.getLicense_type().getUri();

		try {
			uploader = (User) requestHandler.Get(userprofileUri, User.class, null, null);
			avatarBitmap = new ImageRetriever().retrieveAvatarImage(uploader.getAvatar().getResource());

			// cache avatar image - use avatar image URI as key
			/*String imageCacheKey = uploader.getAvatar().getUri();// "workflowPreview";
			if (imageCacheKey != null && imageCache.get(imageCacheKey) == null) {
				imageCache.put(imageCacheKey, avatarBitmap);
			}*/

		} catch (Exception e) {
			exceptionMessage = "There was an error loading workflow details.\n"+e.getMessage();
		}
		return exceptionMessage != null ? exceptionMessage : null;
	}

	public Object onTaskComplete(Object... result) {
		if (result[0] instanceof String) {
			String exception = (String) result[0];
			if (exception != null) {
				MessageHelper.showMessageDialog(currentActivity, exception);
			}
		} else {
			// Scale it to 125 x 125
			Drawable avatarDrawable = new BitmapDrawable(getResources(),
					Bitmap.createScaledBitmap(avatarBitmap, 125, 125, true));
			Rect outRect = new Rect();
			userName.getDrawingRect(outRect);
			// resize the Rect
			outRect.inset(-10, 10);
			avatarDrawable.setBounds(outRect);
			userName.setCompoundDrawables(null, avatarDrawable, null, null);
			userName.setText(uploader.getName());
		}
		return null;
	}

	@Override
	protected void onPause() {
		// save data before activity going back to the back-stack
		// override the previous one.
		// It is better to retrieve data from myExperiment when
		// the activity is launched for the first time.
		// loading from cache is only for activity restoring
		mCache.put("workflow", workflow);
		mCache.put("license", license);
		mCache.put("uploader", uploader);
		TavernaAndroid.setmTextCache(mCache);
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putSerializable("workflow", workflow);
		savedInstanceState.putSerializable("license", license);
		savedInstanceState.putSerializable("uploader", uploader);
	}

	 @Override
	 public void onRestoreInstanceState(Bundle savedInstanceState) {
		 super.onRestoreInstanceState(savedInstanceState);
		 // Restore UI state from the savedInstanceState.
		 // This bundle has also been passed to onCreate.
		 workflow = (Workflow) savedInstanceState.getSerializable("workflow");
		 license = (License) savedInstanceState.getSerializable("license");
		 uploader = (User) savedInstanceState.getSerializable("uploader");
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

	public class MyPagerAdapter extends FragmentPagerAdapter {
		private final ArrayList<Fragment> mFragments = new ArrayList<Fragment>();

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public void addFragment(Fragment fragment) {
			mFragments.add(fragment);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Fragment getItem(int i) {
			return mFragments.get(i);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale locale = getApplicationContext().getResources()
					.getConfiguration().locale;
			switch (position) {
			case 0:
				return getString(R.string.workflow_detail_title_section1)
						.toUpperCase(locale);
			case 1:
				return getString(R.string.workflow_detail_title_section2)
						.toUpperCase(locale);
			case 2:
				return getString(R.string.workflow_detail_title_section3)
						.toUpperCase(locale);
			}
			return null;
		}
	}
}

package cs.man.ac.uk.tavernamobile;

import java.util.ArrayList;
import java.util.Locale;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import cs.man.ac.uk.tavernamobile.fragments.ExploreFragment;
import cs.man.ac.uk.tavernamobile.fragments.RunsFragment;
import cs.man.ac.uk.tavernamobile.fragments.WorkflowsFragment;

public class MainActivity extends FragmentActivity {
	
	private boolean backHit;
	//private BaseAdapter mSpinnerAdapter;
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
	 * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
	 * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
	 * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	// root layout for child fragments to access
	private static ViewGroup parentContainer;
	private SlidingMenu slidingMenu;

	public SlidingMenu getMenu() {
		return slidingMenu;
	}

	public static ViewGroup getParentContainer() {
		return parentContainer;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_panel);
		
		parentContainer = (ViewGroup) findViewById(R.id.main_panel_root);
		
		// configure the SlidingMenu
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.sliding_menu);
        
        this.overridePendingTransition(R.anim.push_left_out, 0);
		
		// UI components
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#D02E2E2E")));
		actionBar.setDisplayShowTitleEnabled(true);
		//mSpinnerAdapter = new ActionBarAdapter();
		
		// Section adapter that will return a fragment for every sections
	    mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
				
		ExploreFragment searchFragment = new ExploreFragment();
		WorkflowsFragment wfFragment = new WorkflowsFragment();
		RunsFragment runsFragment = new RunsFragment();
		
		mSectionsPagerAdapter.addFragment(searchFragment);
		mSectionsPagerAdapter.addFragment(wfFragment);
		mSectionsPagerAdapter.addFragment(runsFragment);
		
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.mainViewPager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(1);
	    mViewPager.setCurrentItem(0);
		
		/*FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
		Fragment newFragment = new MainFragment();
		ft.addToBackStack("mainFragment");
		ft.replace(R.id.main_panel_root, newFragment).commit();*/

	    backHit = false;
	}
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_panel_menu, menu);
		
		Spinner loginStateView = (Spinner) menu.findItem(R.id.main_panel_login_menu).getActionView();
		loginStateView.setAdapter(mSpinnerAdapter);
		loginStateView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1, int itemIndex, long arg3) {
				switch(itemIndex){
			    case 0:
			    	User user = TavernaAndroid.getMyEUserLoggedin();
			    	if(user != null){
			    		Intent gotoMyexperimentLogin = new Intent(
								currentActivity, MyExperimentLogin.class);
				    	currentActivity.startActivity(gotoMyexperimentLogin);
			    	}
			    	break;
			    case 1:
			    	MessageHelper.showOptionsDialog(
			    			currentActivity,
							"Do you wish to log out ?", 
							"Attention",
							new CallbackTask() {
								@Override
								public Object onTaskInProgress(Object... param) {
									// Clear user logged-in and cookie
									TavernaAndroid.setMyEUserLoggedin(null);
									TavernaAndroid.setMyExperimentSessionCookies(null);
									return null;
								}

								@Override
								public Object onTaskComplete(Object... result) { return null; }
								
							}, null);
			    	break;
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		return true;
	}*/
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_panel_menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.main_panel_setting_menu:
				Intent goToSetting = new Intent(this, SettingsActivity.class);
				startActivity(goToSetting);
				break;
		    /*case R.id.main_panel_login_menu:
		    	User user = TavernaAndroid.getMyEUserLoggedin();
		    	if(user != null){
		    		Intent gotoMyexperimentLogin = new Intent(
							currentActivity, MyExperimentLogin.class);
			    	currentActivity.startActivity(gotoMyexperimentLogin);
		    	}
		    	break;*/
		    default:
		    	break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		//mSpinnerAdapter.notifyDataSetChanged();
		slidingMenu.setSlidingEnabled(true);
		super.onStart();
	}
	
	public void onBackPressed() 
	{
		if(backHit){
			backHit = false;
			// put activity in background
			this.moveTaskToBack(true);
			// finish();
		}
		Toast.makeText(this, "Press Back button one more time to quit", Toast.LENGTH_SHORT).show();
		backHit = true;
	    return;
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
	 * sections of the app.
	 */
	private class SectionsPagerAdapter extends FragmentPagerAdapter {

		private final ArrayList<Fragment> mFragments = new ArrayList<Fragment>();

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		public void addFragment(Fragment fragment) {
	        mFragments.add(fragment);
	        notifyDataSetChanged();
	    }

		@Override
		public Fragment getItem(int i) {
			return mFragments.get(i);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale locale = getApplicationContext().getResources().getConfiguration().locale;
			switch (position) {
				case 0: return getString(R.string.title_section1).toUpperCase(locale);
				case 1: return getString(R.string.title_section2).toUpperCase(locale);
				case 2: return getString(R.string.title_section3).toUpperCase(locale);
			}
			return null;
		}
	}
	
	// action bar spinner data adapter
	/*private class ActionBarAdapter extends BaseAdapter {
		// just a place holder
		private String[] data = {" "};
		private User user;

		@Override
		public int getCount() {
			// try to get the previously stored user
			// if there was a user loggedin then the
			// spinner has 1 items(log out), 
			// otherwise 0 (only display user info)
			user = TavernaAndroid.getMyEUserLoggedin();
			return user != null ? 2 : 1;
		}

		@Override
		public Object getItem(int index) {
			return data[index];
		}

		@Override
		public long getItemId(int index) {
			return index;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.login_actionbar_single_item, null);
			}
			TextView loginTextView = (TextView) convertView.findViewById(R.id.loggedInUserName);

			String theText = "";
			if(user != null){
				switch(position){
				case 0:
					theText = user.getName();
					Bitmap avatarBitmap = TavernaAndroid.getmMemoryCache().get(user.getAvatar().getResource());
					if(avatarBitmap != null){
						Drawable avatarDrawable = new BitmapDrawable(getResources(),
								Bitmap.createBitmap(avatarBitmap));
						Rect outRect = new Rect();
						loginTextView.getDrawingRect(outRect);
						// resize the Rect
						//outRect.inset(-10, 10);
						avatarDrawable.setBounds(outRect);
						loginTextView.setCompoundDrawables(avatarDrawable, null, null, null);
					}
					break;
				case 1:
					// clear the drawable
					loginTextView.setCompoundDrawables(null, null, null, null);
					theText = "Log out";
					break;
				}
			}else{
				// clear the drawable
				loginTextView.setCompoundDrawables(null, null, null, null);
				theText = "Log in to myExperiment";
			}
			
			loginTextView.setText(theText);

			return convertView;
		}
	}*/
}

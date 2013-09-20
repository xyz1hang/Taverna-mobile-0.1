package cs.man.ac.uk.tavernamobile;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import cs.man.ac.uk.tavernamobile.datamodels.User;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

public class MainPanelActivity extends FragmentActivity {
	
	private boolean backHit;
	private SlidingMenu slidingMenu;
	private LinearLayout poweredByLayout;

	public SlidingMenu getMenu() {
		return slidingMenu;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_panel_frame);
		
		// configure the SlidingMenu
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMenu(R.layout.sliding_menu);
        
        // this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		
		// UI components
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#D02E2E2E")));
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setIcon(this.getResources().getDrawable(R.drawable.taverna_wheel_logo_medium));
		
		poweredByLayout = (LinearLayout) findViewById(R.id.poweredByLayout);
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		// ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
		Fragment newFragment = new FragmentsContainer();
		Bundle args = new Bundle();
		// integer representation of fragments
		User user = TavernaAndroid.getMyEUserLoggedin();
		int[] fragmentsToInstantiate = null;
		if(user != null){
			fragmentsToInstantiate = new int[] {4, 5};
		}else{
			fragmentsToInstantiate = new int[] {0, 1};
		}
		args.putIntArray("fragmentsToInstantiate", fragmentsToInstantiate);
		newFragment.setArguments(args);
		ft.addToBackStack("StarterFragments");
		ft.replace(R.id.main_panel_root, newFragment, "StarterFragments").commit();

	    backHit = false;
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// remove menu added by previous fragment
		for(int i = 1; i < menu.size(); i ++){
			menu.removeItem(menu.getItem(i).getItemId());
		}
		getMenuInflater().inflate(R.menu.main_panel_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				slidingMenu.toggle();
				return true;
			case R.id.main_panel_setting_menu:
				Intent goToSetting = new Intent(this, SettingsActivity.class);
				startActivity(goToSetting);
				return true;
		    default:
		    	return super.onOptionsItemSelected(item);
		}
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
	
	@Override
	protected void onPause(){
		super.onPause();
		this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}
	
	public void demoPoweredBy(){
		AnimatorSet set = 
				(AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.fade_in_out);
		set.setTarget(poweredByLayout);
		set.start();
		/*new Handler().postDelayed(new Runnable() {
			public void run() {
				poweredByLayout.setVisibility(8);
			}
		},6000);*/
	}

	public void hidePoweredBy(){
		AnimatorSet set = 
				(AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.fade_out);
		set.setTarget(poweredByLayout);
		set.start();
		/*new Handler().postDelayed(new Runnable() {
			public void run() {
				poweredByLayout.setVisibility(8);
			}
		},3000);*/
	}
	
	public void showPoweredBy(){
		AnimatorSet set = 
				(AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.fade_in);
		set.setTarget(poweredByLayout);
		set.start();
	}
}

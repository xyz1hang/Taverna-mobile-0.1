package cs.man.ac.uk.tavernamobile;

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
import android.view.ViewGroup;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainPanelActivity extends FragmentActivity {
	
	private boolean backHit;

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
		setContentView(R.layout.main_panel_frame);
		
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
        
        // this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		
		// UI components
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#D02E2E2E")));
		actionBar.setDisplayShowTitleEnabled(true);
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
		Fragment newFragment = new FragmentsContainer();
		Bundle args = new Bundle();
		// integer representation of fragments
		int[] fragmentsToInstantiate = new int[] {0, 1};
		args.putIntArray("fragmentsToInstantiate", fragmentsToInstantiate);
		newFragment.setArguments(args);
		ft.addToBackStack("StarterFragments");
		ft.replace(R.id.main_panel_root, newFragment).commit();

	    backHit = false;
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_panel_menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				slidingMenu.toggle();
				break;
			case R.id.main_panel_setting_menu:
				Intent goToSetting = new Intent(this, SettingsActivity.class);
				startActivity(goToSetting);
				break;
		    default:
		    	break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
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
	
	@Override
	protected void onPause(){
		super.onPause();
		this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}
}

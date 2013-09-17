package cs.man.ac.uk.tavernamobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import cs.man.ac.uk.tavernamobile.datamodels.User;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

public class SlidingMenuFragment extends Fragment {
	
	private FragmentActivity parentActivity;
	private TextView myExperimentLoginText;
	private View menuView;
	private LinearLayout listRoot;
	
	// Utilities.
	// try to reuse object
	private LayoutInflater layoutInflater;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		menuView = inflater.inflate(R.layout.sliding_list, null);
		listRoot = (LinearLayout) menuView.findViewById(R.id.slidingMenuListsRoot);
		myExperimentLoginText = (TextView) menuView.findViewById(R.id.myExperimentLoginState);
		refreshLoginState();
		return menuView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		parentActivity = this.getActivity();
		layoutInflater = ((LayoutInflater) parentActivity
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		refreshMenus();
		
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
									refreshLoginState();
									clearLoginPreference();
									return null;
								}
	
								@Override
								public Object onTaskComplete(Object... result) {
									return null;
								}
							}, null);
					}else{
						((MainPanelActivity) parentActivity).getMenu().toggle();
						((MainPanelActivity) parentActivity).getMenu().setSlidingEnabled(false);
						Intent gotoMyexperimentLogin = new Intent(parentActivity, MyExperimentLogin.class);
						parentActivity.startActivity(gotoMyexperimentLogin);
					}
				}
			});
	}

	@Override
	public void onStart() {
		refreshLoginState();
		super.onStart();
	}

	private void refreshMenus() {
		// Navigation Menu
		final User userloggedIn = TavernaAndroid.getMyEUserLoggedin();
		ListView navigationMenuList = null;
		String[] navigationMenuNames = null;
		int[] navigationMenuIcons =  null;
		if(userloggedIn != null){
			navigationMenuNames = new String[] {"My Workflows", "Workflow Run Control", "Explore Workflows", };
			navigationMenuIcons = new int[] {R.drawable.bookmark_icon, R.drawable.gear_icon, R.drawable.myexperiment_logo_small};
		}else{
			navigationMenuNames = new String[] {"Workflow Run Control", "Explore Workflows"};
			navigationMenuIcons = new int[] {R.drawable.gear_icon, R.drawable.myexperiment_logo_small};
		}
		navigationMenuList = setupList("Navigation", navigationMenuNames, navigationMenuIcons);
		
		// DataSourc Menu
		String[] dataSourceNames = new String[] {"Dropbox", "Google Drive"};
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
					if(userloggedIn != null){
						if(itemIndex == 1){
							/** integer representation of fragments
							 *	0 - ExploreFragment, 1 - SearchResultFragment, 2 - RunsFragments
							 *  3 - LaunchHistoryFragments, 4 - MyWorkflowFragment, 5 - FavouriteWorkflowFragment
							 */
							// beginFragmentTransaction(new int[] {4, 5}, "MyWorkflowsFragment")
						}
						else if(itemIndex == 2){
							beginFragmentTransaction(new int[] {2, 3}, "RunsControlFragment");
						}
						else if(itemIndex == 3){
							beginFragmentTransaction(new int[] {0, 1}, "WorkflowsFragment");
						}
					}
					else{
						if(itemIndex == 1){
							beginFragmentTransaction(new int[] {2, 3}, "RunsControlFragment");
						}
						else if(itemIndex == 2){
							beginFragmentTransaction(new int[] {0, 1}, "WorkflowsFragment");
						}
					}
				}
			});
		}
		
		dataSourceList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> theListView, View parentView, 
					int itemIndex, long arg3) {
				if(itemIndex == 1){
					((MainPanelActivity) parentActivity).getMenu().toggle();
					Intent goToSetting = new Intent(parentActivity, SettingsActivity.class);
					parentActivity.startActivity(goToSetting);
				}
				else if(itemIndex == 2){
					((MainPanelActivity) parentActivity).getMenu().toggle();
					Intent goToSetting = new Intent(parentActivity, SettingsActivity.class);
					parentActivity.startActivity(goToSetting);
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
								parentActivity.recreate();
								/*refreshLoginState();
								clearLoginPreference();
								refreshMenus();*/
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
		
		View headerview = layoutInflater.inflate(R.layout.sliding_menu_list_header, null);
		TextView listHeaderName = (TextView) headerview.findViewById(R.id.sliding_menu_list_name);
		listHeaderName.setText(listTitle);
		ListView menuList = new ListView(parentActivity);
		menuList.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		int sizeInPx = 10;
		float scale = getResources().getDisplayMetrics().density;
		int sizeInDp = (int) (sizeInPx*scale + 0.5f);
		menuList.setPadding(sizeInDp, 0, sizeInDp, 0);
		//menuList.setPadding(R.dimen.list_padding, 0, R.dimen.list_padding, 0);
		
		menuList.addHeaderView(headerview);
		menuList.setAdapter(menuAdapter);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
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
			myExperimentLoginText.setText("Logged in as: "+ userName);
		}else{
			Drawable defaultDrawable = getResources().getDrawable(R.drawable.myexperiment_logo_small);
			myExperimentLoginText.setCompoundDrawablesWithIntrinsicBounds(
					defaultDrawable, null, null, null);
			myExperimentLoginText.setText("Log in to myExperiment");
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
	
	private void beginFragmentTransaction(int[] fragmentsToInt, String backStackTag){
		FragmentTransaction ft =
				parentActivity.getSupportFragmentManager().beginTransaction();
		// ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
		Fragment newFragment = new FragmentsContainer();
		Bundle args = new Bundle();
		args.putIntArray("fragmentsToInstantiate", fragmentsToInt);
		newFragment.setArguments(args);
		ft.addToBackStack(backStackTag);
		ft.replace(R.id.main_panel_root, newFragment).commit();
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
}

package cs.man.ac.uk.tavernamobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
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
	private ListView list, settingList;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		menuView = inflater.inflate(R.layout.sliding_list, null);
		myExperimentLoginText = (TextView) menuView.findViewById(R.id.myExperimentLoginState);
		list = (ListView) menuView.findViewById(R.id.tobe_added_list);
		settingList = (ListView) menuView.findViewById(R.id.sliding_menu_setting_list);
		refreshLoginState();
		return menuView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		parentActivity = this.getActivity();
		
		SimpleAdapter tobeAdapter = new SimpleAdapter(getActivity());
		for (int i = 0; i < 5; i++) {
			listObject obj = new listObject();
			obj.setText("to be added...");
			obj.setResID(R.drawable.ic_action_overflow);
			tobeAdapter.add(obj);
		}
		setupList(list, tobeAdapter, "Data sources");
		
		SimpleAdapter settingAdapter = new SimpleAdapter(getActivity());
		listObject obj1 = new listObject();
		obj1.setText("Setting");
		obj1.setResID(R.drawable.ic_action_overflow);
		listObject obj2 = new listObject();
		obj2.setText("CopyRight Info");
		obj2.setResID(R.drawable.ic_action_search);
		listObject[] listObjects = new listObject[]{obj1, obj2};
		settingAdapter.addAll(listObjects);
		setupList(settingList, settingAdapter, "Other");
		
		settingList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> theListView, View parentView, 
					int itemIndex, long arg3) {
				if(itemIndex == 1){
					((MainActivity) parentActivity).getMenu().toggle();
					Intent goToSetting = new Intent(parentActivity, SettingsActivity.class);
					parentActivity.startActivity(goToSetting);
					
				}
			}
			
		});
		
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
										return null;
									}
	
									@Override
									public Object onTaskComplete(Object... result) {
										return null;
									}
								}, null);
					}else{
						/*FragmentTransaction ft =
								parentActivity.getSupportFragmentManager().beginTransaction();
						ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
						Fragment newFragment = new MyExperimentLogin();
						ft.addToBackStack("myExpLogin");
						ft.replace(R.id.main_panel_root, newFragment).commit();
						// close the menu
						((MainActivity) parentActivity).getMenu().toggle();*/
						
						((MainActivity) parentActivity).getMenu().toggle();
						((MainActivity) parentActivity).getMenu().setSlidingEnabled(false);
						Intent gotoMyexperimentLogin = new Intent(
								parentActivity, MyExperimentLogin.class);
						parentActivity.startActivity(gotoMyexperimentLogin);
					}
				}
			});
	}

	private void setupList(ListView list, SimpleAdapter tobeAdapter, String listTitle) {
		View headerview = 
				((LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.sliding_menu_list_header, null);
		TextView listHeaderName = (TextView)headerview.findViewById(R.id.sliding_menu_list_name);
		listHeaderName.setText(listTitle);
		list.addHeaderView(headerview);
		list.setAdapter(tobeAdapter);
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

	private class SimpleAdapter extends ArrayAdapter<listObject> {

		public SimpleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.sliding_row, null);
			}
			/*ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);*/
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

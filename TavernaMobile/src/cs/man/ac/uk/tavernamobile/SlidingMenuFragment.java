package cs.man.ac.uk.tavernamobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.datamodels.User;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

public class SlidingMenuFragment extends Fragment {
	
	private Activity parentActivity;
	private TextView myExperimentLoginText;
	private View menuView;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		menuView = inflater.inflate(R.layout.sliding_list, null);
		myExperimentLoginText = (TextView) menuView.findViewById(R.id.myExperimentLoginState);
		return menuView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		parentActivity = this.getActivity();
		refreshLoginState();
		
		/*SampleAdapter adapter = new SampleAdapter(getActivity());
		for (int i = 0; i < 10; i++) {
			adapter.add(new SampleItem("to be added...", android.R.drawable.ic_menu_search));
		}
		setListAdapter(adapter);*/
		
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
										public Object onTaskComplete(
												Object... result) {
											// TODO Auto-generated method stub
											return null;
										}
									}, null);
						}
						else{
							Intent gotoMyexperimentLogin = new Intent(
									parentActivity, MyExperimentLogin.class);
							parentActivity.startActivity(gotoMyexperimentLogin);
						}
					}
				});
	}
	
	private void refreshLoginState() {
		User userLoggedin = TavernaAndroid.getMyEUserLoggedin();
		String userName = null;
		if (userLoggedin != null) {
			userName = userLoggedin.getName();
			/*Bitmap avatarBitmap = 
			 	TavernaAndroid.getmMemoryCache().get(userLoggedin.getAvatar().getResource());
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

	private class SampleItem {
		public String tag;
		public int iconRes;
		public SampleItem(String tag, int iconRes) {
			this.tag = tag; 
			this.iconRes = iconRes;
		}
	}

	public class SampleAdapter extends ArrayAdapter<SampleItem> {

		public SampleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.sliding_row, null);
			}
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);

			return convertView;
		}

	}
}

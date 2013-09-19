package cs.man.ac.uk.tavernamobile.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.WorkflowDetail;
import cs.man.ac.uk.tavernamobile.datamodels.CreditGroup;
import cs.man.ac.uk.tavernamobile.datamodels.CreditUser;
import cs.man.ac.uk.tavernamobile.datamodels.ElementBase;
import cs.man.ac.uk.tavernamobile.datamodels.Privilege;
import cs.man.ac.uk.tavernamobile.datamodels.Rating;
import cs.man.ac.uk.tavernamobile.datamodels.User;
import cs.man.ac.uk.tavernamobile.datamodels.Workflow;
import cs.man.ac.uk.tavernamobile.myexperiment.HttpRequestHandler;
import cs.man.ac.uk.tavernamobile.myexperiment.WorkflowDownloadHelper;

public class WorkflowsListAdapter extends BaseAdapter {
	
	private LayoutInflater myInflater;
	private ArrayList<Workflow> data;
	private Activity mContext;
	
	private android.support.v4.util.LruCache<String, Bitmap> imageCache;
	private TextView thumbnailNotAvailableView;
	
	public int animationStartPosition;
	
	public WorkflowsListAdapter(Activity context, ArrayList<Workflow> listData)
	{
		data = listData;
		mContext = context;
		myInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageCache = TavernaAndroid.getmMemoryCache();
	}
	
	// method to change the data source 
	// so that when getView gets called again
	// it can display new data
	public void ChangeDataSource(ArrayList<Workflow> listData){
		data = listData;
	}
	
	public void AppendData(ArrayList<Workflow> listData){
		data.addAll(listData);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Workflow getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO: prevent the view being recycled which will display the 
		// TODO: view holder pattern
		// wrong avatar image (temporary solution)
		//if (convertView == null)
		//{
			convertView = myInflater.inflate(R.layout.workflowslist_single_row, null);
		//}
		
		// UI elements
		TextView uploaderNameView = (TextView) convertView.findViewById(R.id.wfListUploaderName);
		TextView titleView = (TextView) convertView.findViewById(R.id.wfListTitleVersion);
		TextView createdView = (TextView) convertView.findViewById(R.id.wfListCreated);
		TextView updatedView = (TextView) convertView.findViewById(R.id.wfListUpdateText);
		ImageView thumbnailView = (ImageView) convertView.findViewById(R.id.wfListThumbnail);
		TextView typeView = (TextView) convertView.findViewById(R.id.wfListTypeText);
		Button viewButton = (Button) convertView.findViewById(R.id.wfList_view_button);
		Button downloadButton = (Button) convertView.findViewById(R.id.wfList_download_button);
		TextView creditValue = (TextView) convertView.findViewById(R.id.wfListCreditText);
		LinearLayout creditLayout = (LinearLayout) convertView.findViewById(R.id.wfListSingleRowCreditLayout);
		TextView ratingValue = (TextView) convertView.findViewById(R.id.wfListRatingText);
		
		thumbnailNotAvailableView = 
				(TextView) convertView.findViewById(R.id.thumbnail_not_available_text);
		
		// get the data
		final Workflow expo = getItem(position);
		
		// set up view button listener
		viewButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(mContext, WorkflowDetail.class);
				intent.putExtra("workflow_details", expo);
				mContext.startActivity(intent);
			}
		});

		// set up download button listener
		downloadButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				boolean canDownload = false;
				List<Privilege> privileges = expo.getPrivileges();
				for (Privilege privilege : privileges) {
					if (privilege.getType().equals("download")) {
						canDownload = true;
					}
				}

				if (canDownload) {
					WorkflowDownloadHelper downloadHelper = new WorkflowDownloadHelper(mContext);
					try {
						// TODO: What to do after workflow has been downloaded from expo
						// null listener
						downloadHelper.StartDownload(expo.getContent_uri(), null);
					} catch (Exception e) {
						Toast.makeText(
							mContext,
							"Workflow download failed, please try again.",
							Toast.LENGTH_LONG).show();
					}
				} else {
					String message = "You don't have the privilege to download this workflow.";
					MessageHelper.showMessageDialog(mContext, "Attention", message, null);
				}
			}
		});
		// loading uploader avatar
		String uploaderUri = expo.getUploader().getUri();
		// load from memory cache
		Bitmap avatarBitmap = imageCache.get(uploaderUri);
		if(avatarBitmap != null){
			Drawable avatarDrawable = new BitmapDrawable(mContext.getResources(),
					Bitmap.createBitmap(avatarBitmap));
			uploaderNameView.setCompoundDrawablesWithIntrinsicBounds(null, avatarDrawable, null, null);
		}
		// else load from myExperiment
		else{
			// place the default image before loading finish
			Drawable defaultAvatar = mContext.getResources().getDrawable(R.drawable.default_avatar_img);
			uploaderNameView.setCompoundDrawablesWithIntrinsicBounds(null, defaultAvatar, null, null);
			
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(
					mContext, 
					new UploaderDetailLoader(uploaderUri, uploaderNameView), 
					null);
		}
		
		// load workflow thumbnail image
		String thumbnailUri = expo.getThumbnail();
		// load from memory cache
		Bitmap wfBitmap = imageCache.get(thumbnailUri);
		if(wfBitmap != null){
			thumbnailView.setImageBitmap(wfBitmap);
		}
		else{
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(
					mContext, 
					new UploaderDetailLoader(thumbnailUri, thumbnailView), 
					null);
		}
		
		uploaderNameView.setText(expo.getUploader().getValue());
		titleView.setText(expo.getTitle()+" (v"+expo.getVersion()+")");
		createdView.setText(expo.getCreated_at());
		updatedView.setText(expo.getUpdated_at());
		typeView.setText(expo.getType().getValue());
		
		List<ElementBase> credits = expo.getCredits().getCreditEntity();
		if(credits != null && credits.size() > 0){
			for(ElementBase c : credits){
				TextView creditView = new TextView(mContext);
				String creditText = "";
				int iconResID = 0;
				if (c instanceof CreditUser){
					creditText = ((CreditUser)c).getValue();
					iconResID = R.drawable.user_icon;
				}else if(c instanceof CreditGroup){
					creditText = ((CreditGroup)c).getValue();
					iconResID = R.drawable.group_icon;
				}
				
				creditView.setCompoundDrawablesWithIntrinsicBounds(iconResID, 0, 0, 0);
				//creditView.setCompoundDrawablePadding(3);
				creditView.setText(creditText);
				LayoutParams params = 
						new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				creditLayout.addView(creditView, params);
			}
		}else{
			creditValue.setVisibility(0);
			creditValue.setText("not available");
		}

		List<Rating> ratings = expo.getRatings();
		String averageString = null;
		double average = 0.0;
		int numOfRatings = 0;
		if(ratings != null && ratings.size() > 0){
			int sum = 0;
			for(Rating r : ratings){
				sum += Integer.parseInt(r.getValue());
			}
			average = sum / ratings.size();
			numOfRatings = ratings.size();
		}
		averageString = String.format(Locale.getDefault(), "%.1f", average);
		ratingValue.setText(averageString + " / 5 ("+numOfRatings+" ratings)");
		
		//String thumbnailUri = expo.getThumbnail();
		//String desctiption = expo.getDescription();
		/*String descriptionData = "<html>"+
									"<body>"+
										"<img width=\"100\" src=\""+ thumbnailUri + "\"/>"+
									"</body>"+
								 "</html>";*/
		/*String descriptionData = "<html>"+
									"<body>"+
										"<table border=\"0\">"+
										"<tr>"+
										"<td><img width=\"100\" src=\""+ thumbnailUri + "\"/></td>"+
										"<td valign=\"top\">" + desctiption + "</td>"+
										"</tr>"+
										"</table>"+
									"</body>"+
								 "</html>";*/
		//descriptionView.loadData(descriptionData, "text/html", "UTF-8");
		
		if(position > animationStartPosition){
			// setup animation
			DisplayMetrics metrics = new DisplayMetrics();
			mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			Animation animation = new TranslateAnimation(0, 0, metrics.heightPixels, 0);
			//Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.push_up_in);
			animation.setDuration(650);
			convertView.startAnimation(animation);
		}
		
		return convertView;
	}
	
	// class to execute the avatar image loading in background
	private class UploaderDetailLoader implements CallbackTask{
		private String resourceURI;
		//private TextView avatarImage;
		//private ImageView thumbnailImage;
		private View imageHolder;
		
		public UploaderDetailLoader(String uri, View holder){
			resourceURI = uri;
			imageHolder = holder;
		}

		@Override
		public Object onTaskInProgress(Object... param) {
			HttpRequestHandler requestHandler = new HttpRequestHandler(null);
			
			if(imageHolder instanceof TextView){
			
				User uploader = null;
				Bitmap avatar = null;
				try {
					 uploader = (User) requestHandler.Get(resourceURI, User.class, null, null);
					 String imageURI = uploader.getAvatar().getResource();
					 avatar = new ImageRetriever().retrieveImage(imageURI);
					 
					 // cache avatar with uploader URI
					 // imageCacheKey = uploader.getUri();
				} catch (Exception e) {
					// swallow - otherwise every view of the list
					// will have to report an exception
					// in the case of networkConnectionException
					// user can just retry loading
					e.printStackTrace();
				}
				return avatar;
			}
			else if(imageHolder instanceof ImageView){
				Bitmap wfImage = new ImageRetriever().retrieveImage(resourceURI);
				return wfImage;
			}
			
			return null;
		}

		@Override
		public Object onTaskComplete(Object... result) {			
			if(imageHolder instanceof TextView){
				if(!(result[0] instanceof Bitmap)){
					return null;
				}
				Bitmap bitmapImage = (Bitmap) result[0];
				TextView holder = (TextView) imageHolder;
				Drawable avatarDrawable = new BitmapDrawable(mContext.getResources(), bitmapImage);
				holder.setCompoundDrawablesWithIntrinsicBounds(null, avatarDrawable, null, null);
			}
			else if(imageHolder instanceof ImageView){
				if (result[0] instanceof Bitmap){
					thumbnailNotAvailableView.setVisibility(8);
					Bitmap bitmapImage = (Bitmap) result[0];
					ImageView holder = (ImageView) imageHolder;
					holder.setVisibility(0);
					holder.setImageBitmap(bitmapImage);
				}else{
					imageHolder.setVisibility(8);
					thumbnailNotAvailableView.setVisibility(0);
				}
			}
			
			// cache the avatar in memory
			// TODO: cache in external storage in the future
			/*if (imageCacheKey != null && imageCache.get(imageCacheKey) == null) {
				imageCache.put(imageCacheKey, avatarBitmap);
			}*/
			return null;
		}		
	}
}
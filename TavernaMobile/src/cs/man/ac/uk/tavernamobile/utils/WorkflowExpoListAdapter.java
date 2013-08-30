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
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.WorkflowDetail;
import cs.man.ac.uk.tavernamobile.datamodels.Credit;
import cs.man.ac.uk.tavernamobile.datamodels.Rating;
import cs.man.ac.uk.tavernamobile.datamodels.User;
import cs.man.ac.uk.tavernamobile.datamodels.Workflow;
import cs.man.ac.uk.tavernamobile.myexperiment.HttpRequestHandler;
import cs.man.ac.uk.tavernamobile.myexperiment.WorkflowDownloadHelper;

public class WorkflowExpoListAdapter extends BaseAdapter {
	
	private LayoutInflater myInflater;
	private ArrayList<Workflow> data;
	private Activity mContext;
	
	private android.support.v4.util.LruCache<String, Bitmap> imageCache;
	
	public int animationStartPosition;
	
	public WorkflowExpoListAdapter(Activity context, ArrayList<Workflow> listData)
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
		// wrong avatar image (temporary solution)
		//if (convertView == null)
		//{
			convertView = myInflater.inflate(R.layout.workflowsexpo_single_row, null);
		//}
		
		// UI elements
		TextView uploaderNameView = (TextView) convertView.findViewById(R.id.wfExpoUploaderName);
		TextView titleView = (TextView) convertView.findViewById(R.id.wfExpoTitleVersion);
		TextView createdView = (TextView) convertView.findViewById(R.id.wfExpoCreated);
		TextView updatedView = (TextView) convertView.findViewById(R.id.wfExpoUpdateText);
		TextView creditView = (TextView) convertView.findViewById(R.id.wfExpoCreditText);
		ImageView thumbnailView = (ImageView) convertView.findViewById(R.id.wfExpoThumbnail);
		TextView typeView = (TextView) convertView.findViewById(R.id.wfExpoTypeText);
		TextView ratingView = (TextView) convertView.findViewById(R.id.wfExpoRatingText);
		Button viewButton = (Button) convertView.findViewById(R.id.wfExpo_view_button);
		Button downloadButton = (Button) convertView.findViewById(R.id.wfExpo_download_button);
		
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
				WorkflowDownloadHelper downloadHelper = 
						new WorkflowDownloadHelper(mContext);
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
		
		List<Credit> credits = expo.getCredits();
		if(credits != null && credits.size() > 0){
			String creditText = "";
			for(Credit c : credits){
				creditText += c.getValue() + " ";
			}
			creditView.setText(creditText);
		}

		List<Rating> ratings = expo.getRatings();
		if(ratings != null && ratings.size() > 0){
			double average = 0.0; 
			int sum = 0;
			for(Rating r : ratings){
				sum += Integer.parseInt(r.getValue());
			}
			average = sum / ratings.size();
			String average2 = String.format(Locale.getDefault(), "%.1f", average);
			ratingView.setText(average2 + " / 5");
		}
		
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
					 avatar = new ImageRetriever().retrieveAvatarImage(imageURI);
					 
					 // cache avatar with uploader URI
					 // imageCacheKey = uploader.getUri();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return avatar;
			}
			else if(imageHolder instanceof ImageView){
				Bitmap wfImage = new ImageRetriever().retrieveAvatarImage(resourceURI);
				// cache the wfThumbnail by its URI
				// imageCacheKey = resourceURI;
				return wfImage;
			}
			
			return null;
		}

		@Override
		public Object onTaskComplete(Object... result) {
			if(!(result[0] instanceof Bitmap)){
				return null;
			}

			Bitmap avatarBitmap = (Bitmap) result[0];
			
			if(imageHolder instanceof TextView){
				TextView holder = (TextView) imageHolder;
				Drawable avatarDrawable = new BitmapDrawable(mContext.getResources(), avatarBitmap);
				holder.setCompoundDrawablesWithIntrinsicBounds(null, avatarDrawable, null, null);
			}
			else if(imageHolder instanceof ImageView){
				ImageView holder = (ImageView) imageHolder;
				holder.setImageBitmap(avatarBitmap);
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
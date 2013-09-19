package cs.man.ac.uk.tavernamobile.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.ImageRetriever;
import cs.man.ac.uk.tavernamobile.utils.ShowImage;

public class DetailsPreviewFragment extends DetailsFragmentsBase implements CallbackTask {

	private ImageView preview;
	private TextView previewNotAvailableView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.workflow_detail_preview, container, false);
		previewNotAvailableView = 
				(TextView) contentView.findViewById(R.id.wfDetail_preview_not_available_text);
		return contentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		preview = (ImageView) parentActivity.findViewById(R.id.workflowPreview);
		TextView createdAt = (TextView) parentActivity.findViewById(R.id.created_at);

		String createdAtString = workflow.getCreated_at();
		if(createdAtString != null){
			createdAt.setText("Created at: " + workflow.getCreated_at());
			createdAt.setTextSize(15);
		}

		loadImage();
		preview.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(View v) {
				Intent showImageIntent = new Intent(parentActivity, ShowImage.class);
				showImageIntent.putExtra("imageURI" , workflow.getPreview());
				parentActivity.startActivity(showImageIntent);

			}});
	}

	private void loadImage() {
		String imageCacheKey = workflow.getPreview();
		Bitmap imageBitmap = getBitmapFromMemCache(imageCacheKey);
		if(imageBitmap != null){
			// Scale it to 200 x 200
			Drawable finalImage = 
					new BitmapDrawable(getResources(), Bitmap.createBitmap(imageBitmap));
			setupImage(preview, finalImage);
		}
		else{
			BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(preview.getContext(), this, "Loading workflow preview...");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	/** network task **/
	public Object onTaskInProgress(Object... params) {
		Bitmap imageBitmap = new ImageRetriever().retrieveImage(workflow.getPreview());
		// cache image - use image URI as key
		// String imageCacheKey = workflow.getPreview();//"workflowPreview";
		// addBitmapToMemoryCache(imageCacheKey, imageBitmap);
		// Scale it to 200 x 200
		Drawable finalImage = null;
		if(imageBitmap != null){
			finalImage = new BitmapDrawable(
					getResources(), Bitmap.createScaledBitmap(imageBitmap, 200, 200, true));
		}

		return finalImage;
	}

	public Object onTaskComplete(Object... result) {
		if(!(result[0] instanceof Drawable)){
			preview.setVisibility(8);
			previewNotAvailableView.setVisibility(0);
			return null;
		}
		Drawable image = (Drawable) result[0];
		setupImage(preview, image);
		previewNotAvailableView.setVisibility(8);
		

		return null;
	}
	/** end of network task **/
}

package cs.man.ac.uk.tavernamobile.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import cs.man.ac.uk.tavernamobile.WorkflowDetail;
import cs.man.ac.uk.tavernamobile.datamodels.Workflow;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

public class DetailsFragmentsBase extends Fragment {

	protected WorkflowDetail parentActivity;
	protected Workflow workflow;
	private LruCache<String, Bitmap> mMemoryCache;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);

		parentActivity = (WorkflowDetail) getActivity();
		mMemoryCache = TavernaAndroid.getmMemoryCache();

		if (savedInstanceState != null) {
			workflow = (Workflow) savedInstanceState.getSerializable("workflow");
		} else {
			workflow = parentActivity.workflow;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("workflow", workflow);
	}

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	protected void setupImage(ImageView target, Drawable image) {
		if (image != null) {
			target.setImageDrawable(image);
		}
	}
}

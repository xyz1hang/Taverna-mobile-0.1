package cs.man.ac.uk.tavernamobile.utils;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.datamodels.Workflow;

public class SearchResultListAdapter extends BaseAdapter {
	
	private LayoutInflater myInflater;
	private ArrayList<Workflow> data;
	private Activity mContext;
	
	public int animationStartPosition;
	
	public SearchResultListAdapter(Activity context, ArrayList<Workflow> listData)
	{
		data = listData;
		mContext = context;
		myInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return data.size();
	}

	public Workflow getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null)
		{
			convertView = myInflater.inflate(R.layout.search_result_single_row, null);
		}
		
		TextView workflowTitle = (TextView) convertView.findViewById(R.id.wfTitle);
		TextView workflowVersion = (TextView) convertView.findViewById(R.id.wfVersion);
		TextView workflowUploader = (TextView) convertView.findViewById(R.id.wfUploader);
		
		Workflow wfData = getItem(position);
		
		workflowUploader.setText(wfData.getUploader().getValue());
		workflowTitle.setText(wfData.getTitle());
		workflowVersion.setText("v"+wfData.getVersion());
		
		if(position > animationStartPosition){
			// setup animation
			DisplayMetrics metrics = new DisplayMetrics();
			mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			Animation animation = new TranslateAnimation(metrics.widthPixels / 2, 0,
					0, 0);
			//Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.push_left_in);
			animation.setDuration(650);
			convertView.startAnimation(animation);
		}

		return convertView;
	}
}

package cs.man.ac.uk.tavernamobile.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.datamodels.OutputValue;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowBE;
import cs.man.ac.uk.tavernamobile.server.WorkflowRunManager;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.ShowImage;
import cs.man.ac.uk.tavernamobile.utils.TextViewer;

public class OutputsTree extends FragmentActivity{

	private static OutputsTree currentActivity;
	
	// variable holding all outputs data
	private static HashMap<String, OutputValue> allOutputs;
	
	private static PagerTitleStrip pagerTitleStrip;
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
	 * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
	 * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
	 * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	OutputTreesPagerAdapter mfragmentStatePagerAdapter;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	static ProgressBar loadingProgressBar;
	
	// common font
	static Typeface font;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.outputs_tree);
		currentActivity = this;
		
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#D02E2E2E")));
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setIcon(this.getResources().getDrawable(R.drawable.taverna_wheel_logo_medium));
		actionBar.setTitle("Outputs");
		TextView topBarText = (TextView) findViewById(R.id.outputsTopBar);
		TextView outputsTopNoticeText = (TextView) findViewById(R.id.outputsTopNoticeText);
		pagerTitleStrip = (PagerTitleStrip) findViewById(R.id.outputTree_pager_title_strip);
		font = Typeface.createFromAsset(this.getAssets(), "RobotoCondensed-Light.ttf");
	    for (int counter = 0 ; counter< pagerTitleStrip.getChildCount(); counter++) {

	        if (pagerTitleStrip.getChildAt(counter) instanceof TextView) {
	            ((TextView)pagerTitleStrip.getChildAt(counter)).setTypeface(font);
	            ((TextView)pagerTitleStrip.getChildAt(counter)).setTextSize(25);
	        }
	    }
	    loadingProgressBar = (ProgressBar) findViewById(R.id.outputTreeLoadingProgressBar);
	    
		// get data passed in
		Bundle extras = getIntent().getExtras();
		WorkflowBE workflowEntity = (WorkflowBE) extras.getSerializable("workflowEntity");
		
		topBarText.setText(workflowEntity.getTitle());
		outputsTopNoticeText.setText("Swipe to browser outputs from different output ports.");
		// begin retrieving output
		WorkflowRunManager manager = new WorkflowRunManager(this);
		manager.getRunOutput(workflowEntity.getTitle(), null, 
			new CallbackTask(){
				@Override
				public Object onTaskInProgress(Object... param) { return null; }
	
				@Override
				public Object onTaskComplete(Object... result) {
					if(result[0] instanceof String){
						MessageHelper.showMessageDialog(
								currentActivity, null, 
								(String)result[0], null);
						return null;
					}
					allOutputs = (HashMap<String, OutputValue>)result[0];
					if(allOutputs == null){
						MessageHelper.showMessageDialog(
								currentActivity, null, 
								"There was an error processing outputs", null);
						return null;
					}
					
					// when data is ready, prepare to setup views
					// fragment titles
					ArrayList<String> portNames = new ArrayList<String>();
					// set of fragment instance with arguments
					ArrayList<OutputTreeFragment> subFragments = new ArrayList<OutputTreeFragment>();
					Iterator<Entry<String, OutputValue>> it = allOutputs.entrySet().iterator();
					while(it.hasNext()){
						String portName = it.next().getKey();
						portNames.add(portName);
						OutputTreeFragment newFragment = newInstance(portName);
						subFragments.add(newFragment);
					}
					
					mfragmentStatePagerAdapter = new OutputTreesPagerAdapter(
							getSupportFragmentManager(), portNames);
					for(Fragment f : subFragments){
						mfragmentStatePagerAdapter.addFragment(f);
					}

					mViewPager = (ViewPager) currentActivity.findViewById(R.id.outputTreeViewPager);
					mViewPager.setAdapter(mfragmentStatePagerAdapter);
					mViewPager.setOffscreenPageLimit(2);
				    mViewPager.setCurrentItem(0);
				    
				    mfragmentStatePagerAdapter.notifyDataSetChanged();
				    mViewPager.setVisibility(0);
				    
					return null;
				}	
			});

		this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		    default:
		    	return super.onOptionsItemSelected(item);
		}
	}
	
	private static OutputTreeFragment newInstance(String portName) 
	{
		OutputTreeFragment myFragment = new OutputTreeFragment();
		Bundle args = new Bundle();
		// index which tells the Fragment which 
		// port in the allOutputs collection it should build tree view for
	    args.putString("portName", portName);
	    // Put any other arguments
	    myFragment.setArguments(args);
	    return myFragment;
	}

	public static class OutputTreeFragment extends Fragment {

		private String portName;
		private LinearLayout treeRoot;
		
		public OutputTreeFragment(){}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			super.onCreateView(inflater, container, savedInstanceState);
			View treeMainView = inflater.inflate(R.layout.output_tree_single, container, false);
			treeRoot = (LinearLayout) treeMainView.findViewById(R.id.output_Tree_Root);
			return treeMainView;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			Bundle extra = this.getArguments();
			portName = extra.getString("portName");
			final OutputValue onePortValues = allOutputs.get(portName);
			int listItemCount = onePortValues.hasListValue() ? onePortValues.getListValue().size() : 
						(onePortValues.hasFileValue() || onePortValues.hasStringValue()) ? 1 : 0;
			buildTree(onePortValues, treeRoot, listItemCount);
			
			// begin building tree in background
			/*BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(currentActivity, this, null);*/
		}

		/*@Override
		public Object onTaskInProgress(Object... param) {
			final OutputValue onePortValues = allOutputs.get(portName);
			buildTree(onePortValues, treeRoot);
			return null;
		}

		@Override
		public Object onTaskComplete(Object... result) {
			return null;
		}*/

		/**
		 * Method to construct Tree View of the output programmatically 
		 * 
		 * @param onePortValues - the actual output values (Text/Image)
		 * @param root - Root layout that the whole output list (layout) will attach to
		 * @param listItemCount - counter for display the number of value in current depth level
		 */
		private void buildTree(final OutputValue onePortValues, LinearLayout root, int listItemCount){
			// create layout parameter first
			final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,      
					LinearLayout.LayoutParams.WRAP_CONTENT);
			// add a TextView to the upper level to indicate 
			// that the output value is a list and add this view to root 
			// without margin
			TextView listText = new TextView(currentActivity);
			listText.setLayoutParams(params);
			if(listItemCount > 1){
				listText.setText("List with "+listItemCount+" values");
			}else{
				listText.setText("List with "+listItemCount+" value");
			}
			listText.setPadding(pxToDp(5), pxToDp(5), pxToDp(5), pxToDp(5));
			listText.setTypeface(font);
			listText.setTextSize(20);
			root.addView(listText, params);
			// set margin now hence following child view (list) will have indent
			params.setMargins(pxToDp(20), pxToDp(5), 0, 0);
			// create sub root layout to add values from list in deeper level
			LinearLayout subRoot = new LinearLayout(currentActivity);
			subRoot.setLayoutParams(params);
			subRoot.setOrientation(LinearLayout.VERTICAL);
			// add value according to value type
			if(onePortValues.hasStringValue()){
				setUpleafView(onePortValues, "Text Value", params, subRoot, 
					new OnClickListener(){
						@Override
						public void onClick(View arg0) {
							Intent goToShowText = new Intent(currentActivity, TextViewer.class);
							Bundle extra = new Bundle();
							extra.putString("textToShow", onePortValues.getStringValue());
							extra.putString("textTitle", portName);
							goToShowText.putExtras(extra);
							currentActivity.startActivity(goToShowText);
						}
					});
			}else if(onePortValues.hasFileValue()){
				setUpleafView(onePortValues, "Image Value", params, subRoot, 
					new OnClickListener(){
						@Override
						public void onClick(View arg0) {
							Intent goToShowImage = new Intent(currentActivity, ShowImage.class);
							Bundle extra = new Bundle();
							extra.putString("imgFilePath", onePortValues.getFileValue());
							extra.putString("imageTitle", portName);
							goToShowImage.putExtras(extra);
							currentActivity.startActivity(goToShowImage);
						}
					});
			}else if(onePortValues.hasListValue()){
				for(OutputValue opVal : onePortValues.getListValue()){
					listItemCount = opVal.hasListValue() ? opVal.getListValue().size() : 
						(opVal.hasFileValue() || opVal.hasStringValue()) ? 1 : 0;
					buildTree(opVal, subRoot, listItemCount);
				}
			}
			root.addView(subRoot, params);
		}

		private void setUpleafView(final OutputValue onePortValues, String textToDisplay,
				final LinearLayout.LayoutParams params, LinearLayout subRoot, 
				OnClickListener listener) {
			TextView text = new TextView(currentActivity);
			text.setLayoutParams(params);
			text.setText(textToDisplay);
			text.setTypeface(font);
			text.setTextSize(20);
			text.setPadding(pxToDp(5), pxToDp(5), pxToDp(5), pxToDp(5));
			text.setBackgroundResource(R.drawable.list_selector);
			text.setOnClickListener(listener);
			subRoot.addView(text, params);
		}
		
		private static int pxToDp(int sizeInPx){
			float scale = currentActivity.getResources().getDisplayMetrics().density;
			return (int) (sizeInPx*scale + 0.5f);
		}
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
	 * sections of the app.
	 */
	private class OutputTreesPagerAdapter extends FragmentStatePagerAdapter {

		private final ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
		private List<String> fragmentTitles;

		public OutputTreesPagerAdapter(FragmentManager fm, List<String> titles) {
			super(fm);
			fragmentTitles = titles;
		}
		
		public void addFragment(Fragment fragment) {
	        mFragments.add(fragment);
	    }

		@Override
		public Fragment getItem(int i) {
			return mFragments.get(i);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale locale = currentActivity.getResources().getConfiguration().locale;
			return fragmentTitles.get(position).toUpperCase(locale);
		}
	}
}

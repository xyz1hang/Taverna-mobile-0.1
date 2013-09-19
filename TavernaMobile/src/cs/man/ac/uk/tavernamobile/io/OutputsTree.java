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
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.MainPanelActivity;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.WorkflowDetail;
import cs.man.ac.uk.tavernamobile.datamodels.OutputValue;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowBE;
import cs.man.ac.uk.tavernamobile.server.WorkflowRunManager;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.ShowImage;
import cs.man.ac.uk.tavernamobile.utils.TextViewer;

public class OutputsTree extends FragmentActivity{

	private static OutputsTree currentActivity;
	
	private int Activity_Starter_Code;
	// variable holding all outputs data
	private static HashMap<String, OutputValue> allOutputs;
	
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
		// get data passed in
		Bundle extras = getIntent().getExtras();
		WorkflowBE workflowEntity = (WorkflowBE) extras.getSerializable("workflowEntity");
		Activity_Starter_Code = extras.getInt("activity_starter");
		
		topBarText.setText(workflowEntity.getTitle());
		outputsTopNoticeText.setText("Output Ports:");
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
				    
					return null;
				}	
			});

		this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				returnToStarterActivity();
				return true;
		    default:
		    	return super.onOptionsItemSelected(item);
		}
	}
	
	// back to the activity that started the run.
	// go back to where it came from.
	private void returnToStarterActivity(){
		switch(Activity_Starter_Code){
		case 1:
			Intent goBackToMain = new Intent(currentActivity, WorkflowDetail.class);
			goBackToMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(goBackToMain);
			break;
		case 2:
			Intent goBackWfDetail = new Intent(currentActivity, MainPanelActivity.class);
			//goBackWfDetail.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(goBackWfDetail);
			break;
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

	public static class OutputTreeFragment extends Fragment implements CallbackTask {

		private String portName;
		private LinearLayout treeRoot;
		
		public OutputTreeFragment(){}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			super.onCreateView(inflater, container, savedInstanceState);
			View treeMainView = inflater.inflate(R.layout.output_tree_single, container, false);
			treeRoot = (LinearLayout) treeMainView.findViewById(R.id.output_Tree_Root);
			setHasOptionsMenu(true);
			return treeMainView;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			Bundle extra = this.getArguments();
			portName = extra.getString("portName");
			final OutputValue onePortValues = allOutputs.get(portName);
			buildTree(onePortValues, treeRoot);
			// begin building tree
			/*BackgroundTaskHandler handler = new BackgroundTaskHandler();
			handler.StartBackgroundTask(currentActivity, this, null);*/
		}

		@Override
		public Object onTaskInProgress(Object... param) {
			final OutputValue onePortValues = allOutputs.get(portName);
			buildTree(onePortValues, treeRoot);
			return null;
		}

		@Override
		public Object onTaskComplete(Object... result) {
			return null;
		}

		private void buildTree(final OutputValue onePortValues, LinearLayout root){
			// Root layout of the list value which
			// the text view of leaf value will attach to
			// margin = indent
			LinearLayout subRoot = new LinearLayout(currentActivity);
			final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,      
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(pxToDp(20), pxToDp(20), 0, 0);
			subRoot.setLayoutParams(params);
			if(onePortValues.hasStringValue()){
				TextView text = new TextView(currentActivity);
				text.setLayoutParams(params);
				text.setText("Text Value");
				text.setPadding(pxToDp(5), pxToDp(5), pxToDp(5), pxToDp(5));
				/*text.setBackground(currentActivity.getResources()
						.getDrawable(R.drawable.sliding_header_login));*/
				text.setOnClickListener(new OnClickListener(){
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
				LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,      
						LinearLayout.LayoutParams.WRAP_CONTENT);
				subRoot.addView(text, textViewParams);
			}else if(onePortValues.hasFileValue()){
				TextView text = new TextView(currentActivity);
				text.setLayoutParams(params);
				text.setText("Image Value");
				text.setPadding(pxToDp(5), pxToDp(5), pxToDp(5), pxToDp(5));
				/*text.setBackground(currentActivity.getResources()
						.getDrawable(R.drawable.sliding_header_login));*/
				text.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						Intent goToShowText = new Intent(currentActivity, ShowImage.class);
						Bundle extra = new Bundle();
						extra.putString("imgFilePath", onePortValues.getFileValue());
						extra.putString("imageTitle", portName);
						goToShowText.putExtras(extra);
						currentActivity.startActivity(goToShowText);
					}
				});
				LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,      
						LinearLayout.LayoutParams.WRAP_CONTENT);
				subRoot.addView(text, textViewParams);
			}else if(onePortValues.hasListValue()){
				for(OutputValue opVal : onePortValues.getListValue()){
					buildTree(opVal, subRoot);
				}
			}
			root.addView(subRoot, params);
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

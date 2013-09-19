package cs.man.ac.uk.tavernamobile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cs.man.ac.uk.tavernamobile.fragments.ExploreFragment;
import cs.man.ac.uk.tavernamobile.fragments.FavouriteWorkflowsFragment;
import cs.man.ac.uk.tavernamobile.fragments.LaunchHistoryFragment;
import cs.man.ac.uk.tavernamobile.fragments.MyWorkflowsFragment;
import cs.man.ac.uk.tavernamobile.fragments.RunsFragment;
import cs.man.ac.uk.tavernamobile.fragments.SearchResultFragment;

public class FragmentsContainer extends Fragment {
	
	private FragmentActivity parentActivity;
	private View mainView;
	private List<Fragment> subFragments;
	private List<String> fragmentTitles;
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
	 * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
	 * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
	 * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	WorkflowsPagerAdapter mfragmentStatePagerAdapter;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainView = inflater.inflate(R.layout.main_panel_content, null);
		
		return mainView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		subFragments = new ArrayList<Fragment>();
		fragmentTitles = new ArrayList<String>();
		parentActivity = getActivity();
		
		Bundle args = this.getArguments();
		int[] fragmentsToInstantiate = args.getIntArray("fragmentsToInstantiate");
		for(int i = 0; i < fragmentsToInstantiate.length; i++){
			switch(fragmentsToInstantiate[i]){
				case 0:
					subFragments.add(new ExploreFragment());
					fragmentTitles.add("Explore");
					break;
				case 1:
					subFragments.add(new SearchResultFragment());
					fragmentTitles.add("Search");
					break;
				case 2:
					subFragments.add(new RunsFragment());
					fragmentTitles.add("Runs");
					break;
				case 3:
					subFragments.add(new LaunchHistoryFragment());
					fragmentTitles.add("History");
					break;
				case 4:
					subFragments.add(new MyWorkflowsFragment());
					fragmentTitles.add("My Workflow");
					break;
				case 5:
					subFragments.add(new FavouriteWorkflowsFragment());
					fragmentTitles.add("Favourite");
					break;
			}
		}
		
		mfragmentStatePagerAdapter = 
				new WorkflowsPagerAdapter(parentActivity.getSupportFragmentManager(), fragmentTitles);
		for(Fragment f : subFragments){
			mfragmentStatePagerAdapter.addFragment(f);
		}
		
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) mainView.findViewById(R.id.mainViewPager);
		mViewPager.setAdapter(mfragmentStatePagerAdapter);
		mViewPager.setOffscreenPageLimit(1);
	    mViewPager.setCurrentItem(0);
	    
	    mfragmentStatePagerAdapter.notifyDataSetChanged();
	    
	    new Handler().postDelayed(new Runnable() {
			public void run() {
				((MainPanelActivity)parentActivity).demoPoweredBy();
			}
	    },2000);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
	 * sections of the app.
	 */
	private class WorkflowsPagerAdapter extends FragmentStatePagerAdapter {

		private final ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
		private List<String> fragmentTitles;

		public WorkflowsPagerAdapter(FragmentManager fm, List<String> titles) {
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
			Locale locale = parentActivity.getResources().getConfiguration().locale;
			return fragmentTitles.get(position).toUpperCase(locale);
		}
	}
	
}

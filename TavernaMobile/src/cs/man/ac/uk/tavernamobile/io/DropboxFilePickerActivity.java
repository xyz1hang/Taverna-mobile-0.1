package cs.man.ac.uk.tavernamobile.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

public class DropboxFilePickerActivity extends ListActivity implements CallbackTask{
	
	public final static String FILE_PATH = "file_path";
	public final static String SELECTED_FILE_NAME = "selectedFileName";
	
	protected File mDirectory;
	protected ArrayList<Entry> mFiles;
	protected FilePickerListAdapter mAdapter;
	private Entry currentEntry;

	private DropboxAPI<AndroidAuthSession> mApi;
	private BackgroundTaskHandler handler;
	private SystemStatesChecker sysChecker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// get data passed in
		String inputPortName = (String) getIntent().getStringExtra("inputPortName");
		// set up title
		if(inputPortName == null){
			inputPortName = "input port";
		}
		// UI components
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("Select Input file for \""+inputPortName+"\"");
		actionBar.setIcon(this.getResources().getDrawable(R.drawable.taverna_wheel_logo_medium));
		
		// Set the view to be shown if the list is empty
		LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View emptyView = inflator.inflate(R.layout.file_picker_empty_view, null);
		((ViewGroup)getListView().getParent()).addView(emptyView);
		getListView().setEmptyView(emptyView);
		
		mApi = TavernaAndroid.getmApi();
		handler = new BackgroundTaskHandler();
		mFiles = new ArrayList<Entry>();
		sysChecker = new SystemStatesChecker(this);
		refreshFilesList("/");
	}
	
	@Override
	protected void onResume() {
		if (!(sysChecker.isNetworkConnected())){
			return;
		}
		
		String path = currentEntry == null ? "/" : currentEntry.path;
		refreshFilesList(path);
		super.onResume();
	}
	
	@Override
	public void onBackPressed() {
		finish();
		super.onBackPressed();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Entry selectedFile = (Entry)l.getItemAtPosition(position);
		if(selectedFile.isDeleted){
			Toast.makeText(this, "The file has been deleted on dropbox", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(selectedFile.isDir) {
			// Update the files list
			refreshFilesList(selectedFile.path);
		} else {
			// Set result
			Intent extra = new Intent();
			extra.putExtra(FILE_PATH, selectedFile.path);
			extra.putExtra(SELECTED_FILE_NAME, selectedFile.fileName());
			setResult(RESULT_OK, extra);
			// close the activity
			finish();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (!(sysChecker.isNetworkConnected())){
				return true;
			}
			if(currentEntry != null) {
				refreshFilesList(currentEntry.parentPath());
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private class FilePickerListAdapter extends ArrayAdapter<Entry> {
		
		private List<Entry> mObjects;
		
		public FilePickerListAdapter(Context context, List<Entry> objects) {
			super(context, R.layout.file_picker_list_item, android.R.id.text1, objects);
			mObjects = objects;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(convertView == null) { 
				LayoutInflater inflater = 
						(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.file_picker_list_item, parent, false);
			}
			Entry object = mObjects.get(position);

			ImageView imageView = (ImageView)convertView.findViewById(R.id.file_picker_image);
			TextView textView = (TextView)convertView.findViewById(R.id.file_picker_text);
			textView.setSingleLine(true);
			textView.setText(object.fileName());
			
			if(object.isDir) {
				// Show the folder icon
				imageView.setImageResource(R.drawable.folder_icon);
			} else {
				// Show the file icon
				imageView.setImageResource(R.drawable.file_icon);
			}
			
			return convertView;
		}
	}
	
	protected void refreshFilesList(String initialPath) {
		if (!(sysChecker.isNetworkConnected())){
			return;
		}
		
		mFiles.clear();
		handler.StartBackgroundTask(this, this, "Loading Dropbox files...", initialPath);
	}

	@Override
	public Object onTaskInProgress(Object... param) {
		if (!(sysChecker.isNetworkConnected())){
			return null;
		}
		
		String initialPath = (String) param[0];
		try {
			currentEntry = mApi.metadata(initialPath, 0, null, true, null);
		} catch (DropboxException e) {
			return "Can not access dropbox files";
		}
		
		mFiles = new ArrayList<Entry>();
		for (Entry e : currentEntry.contents) {
		    if (!e.isDeleted) {
		    	mFiles.add(e);
		    }
		}
		return null;
	}

	@Override
	public Object onTaskComplete(Object... result) {
		if(result[0] instanceof String){
			Toast.makeText(this, (String)result[0], Toast.LENGTH_LONG).show();
			finish();
			return null;
		}
		
		mAdapter = new FilePickerListAdapter(this, mFiles);
		setListAdapter(mAdapter);
		return null;
	}
}

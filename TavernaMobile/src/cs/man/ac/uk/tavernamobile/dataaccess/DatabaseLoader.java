package cs.man.ac.uk.tavernamobile.dataaccess;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;

public class DatabaseLoader implements LoaderManager.LoaderCallbacks<Cursor> {

	private Activity currentActivity;
	private CallbackTask loadListener;

	public DatabaseLoader(Activity context, CallbackTask listener) {
		currentActivity = context;
		loadListener = listener;
	}

	@Override
	public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = (String[]) args.get("projection");
		String selection = (String) args.get("selection");
		String[] selectionArgs = (String[]) args.get("selectionArgs");
		String tableUriString = (String) args.get("tableURI");
		Uri tableUri = Uri.parse(tableUriString);
		
		CursorLoader loader = new CursorLoader(currentActivity,
				tableUri, 
				projection, selection,
				selectionArgs, null);
		return loader;
	}

	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader,
			Cursor cursor) {
		if(this.loadListener != null){
			this.loadListener.onTaskComplete(cursor);
		}
	}

	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
		loader = null;
	}
}
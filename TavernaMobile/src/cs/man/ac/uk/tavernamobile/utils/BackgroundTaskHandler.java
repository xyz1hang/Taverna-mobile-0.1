package cs.man.ac.uk.tavernamobile.utils;

import uk.org.taverna.server.client.NetworkConnectionException;
import android.content.Context;
import android.widget.Toast;

public class BackgroundTaskHandler {
	
	private BackgroundTask mBackgroundTask;

	public void StartBackgroundTask(Context context,
			CallbackTask task, String message, Object... params) {
		
		mBackgroundTask = new BackgroundTask(message, context, task);
		mBackgroundTask.execute(params);
	}
	
	public void CancelTask(boolean mayInterrupt){
		mBackgroundTask.cancel(mayInterrupt);
	}
	
	public class BackgroundTask extends android.os.AsyncTask<Object, Void, Object> {
		private Context currentContext;
		private android.app.ProgressDialog dialog;
		private CallbackTask taskListener;
		private String dialogMessage;

		public BackgroundTask(String message, Context context,
				CallbackTask listener) {
			currentContext = context;
			dialog = new android.app.ProgressDialog(currentContext);
			dialogMessage = message;
			taskListener = listener;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (dialogMessage != null) {
				dialog.setMessage(dialogMessage);
				dialog.setCancelable(false);
				dialog.show();
			}
		}

		@Override
		protected Object doInBackground(Object... params) {
			Object result = null;
			if (taskListener != null) {
				try{
					result = taskListener.onTaskInProgress(params);
				} catch (NetworkConnectionException e){
					Toast.makeText(currentContext, e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			dialog.dismiss();

			if (taskListener != null) {
				taskListener.onTaskComplete(result);
			}
		}
	}
}

package cs.man.ac.uk.tavernamobile.utils;

import android.content.Context;

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
	
	public class BackgroundTask extends
			android.os.AsyncTask<Object, Void, Object> {
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
			if (taskListener != null) {
				Object results = null;
				try {
					results = taskListener.onTaskInProgress(params);
				} catch (Exception e) {
					// TODO: consume quietly or throw ?
					e.printStackTrace();
				}

				return results;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			dialog.dismiss();

			if (taskListener != null) {
				try {
					taskListener.onTaskComplete(result);
				} catch (Exception e) {
					// TODO: consume quietly or throw ?
					e.printStackTrace();
				}
			}
		}
	}
}

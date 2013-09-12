package cs.man.ac.uk.tavernamobile.fragments;

import uk.org.taverna.server.client.NetworkConnectionException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.datamodels.License;
import cs.man.ac.uk.tavernamobile.myexperiment.HttpRequestHandler;
import cs.man.ac.uk.tavernamobile.utils.CallbackTask;
import cs.man.ac.uk.tavernamobile.utils.BackgroundTaskHandler;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;

public class DetailsLicenseFragment extends DetailsFragmentsBase implements CallbackTask {

	private TextView title;
	private WebView web;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.workflow_detail_license, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// load data in background
		BackgroundTaskHandler handler = new BackgroundTaskHandler();
		handler.StartBackgroundTask(parentActivity, this, null);

		title = (TextView) getActivity().findViewById(R.id.licenseTitle);
		web = (WebView) getActivity().findViewById(R.id.licenseDetailWebView);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public Object onTaskInProgress(Object... param) {
		String exceptionMessage = null;
		String licenseUri = workflow.getLicense_type().getUri();
		HttpRequestHandler requestHandler = new HttpRequestHandler(parentActivity);
		License license = null;
		try {
			license = (License) requestHandler.Get(licenseUri, License.class, null, null);
		} catch(NetworkConnectionException e){
			exceptionMessage = e.getMessage();
		} catch(Exception e) {
			exceptionMessage = e.getMessage();
		}
		return exceptionMessage != null ? exceptionMessage : license;
	}

	public Object onTaskComplete(Object... result) {
		if(result[0] instanceof String){
			String exception = (String) result[0];
			if(exception != null){
				MessageHelper.showMessageDialog(parentActivity, null, exception, null);
			}
		}
		else{
			License license = (License) result[0];
			title.setText(license.getTitle());

			web.loadData(license.getDescription(), "text/html", "UTF-8");
			web.getSettings();
			web.setBackgroundColor(0);
		}
		return null;
	}
}

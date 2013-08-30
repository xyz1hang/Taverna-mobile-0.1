package cs.man.ac.uk.tavernamobile.fragments;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.datamodels.Privilege;

public class DetailsDescriptionFragment extends DetailsFragmentsBase {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.workflow_detail_description,
				container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		WebView web = (WebView) getActivity().findViewById(R.id.workflowDescHolder);
		String description = parentActivity.workflow.getDescription();
		if (description == null) {
			description = "No description available";
		}
		web.loadData(description, "text/html", "UTF-8");
		web.getSettings();
		web.setBackgroundColor(0);

		TextView privilegeTextView = (TextView) getActivity().findViewById(
				R.id.wfPermissionValue);
		
		List<Privilege> privileges = parentActivity.workflow.getPrivileges();
		String privilegeString = privileges.get(0).getType();
		for(int i = 1; i < privileges.size(); i++){
			Privilege p = privileges.get(i);
			if(p != null){
				privilegeString += " " + p.getType();
			}
		}

		privilegeTextView.setText(privilegeString);
	}
}

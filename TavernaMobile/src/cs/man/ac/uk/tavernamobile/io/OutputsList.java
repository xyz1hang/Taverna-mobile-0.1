package cs.man.ac.uk.tavernamobile.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.MainPanelActivity;
import cs.man.ac.uk.tavernamobile.WorkflowDetail;
import cs.man.ac.uk.tavernamobile.datamodels.WorkflowBE;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

public class OutputsList extends Activity{

	private OutputsList currentActivity;
	private OutputListAdaptor resultListAdapter;
	private ArrayList<HashMap<String, String>> listData;
	private TavernaAndroid ta;
	
	private int Activity_Starter_Code;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.outputs);
		
		ta = (TavernaAndroid) getApplication();

		// UI components
		Button returnButton = (Button) findViewById(R.id.returnButton);
		TextView title = (TextView) findViewById(R.id.output_wfTitle);
		TextView text = (TextView) findViewById(R.id.output_text);
		ListView resultList = (ListView) findViewById(R.id.outputList);

		// get data passed in
		Bundle extras = getIntent().getExtras();
		WorkflowBE workflowEntity = (WorkflowBE) extras.getSerializable("workflowEntity");
		Activity_Starter_Code = extras.getInt("activity_starter");
		
		// get the global outputs
		HashMap<String, String> outputs = ta.getOutputs();
		// data setup
		prepareListData(outputs);
		title.setText(workflowEntity.getTitle());
		
		if (outputs.size() > 1){
			text.setText("This workflow has "+ outputs.size() + " outputs : ");
		}
		else{
			text.setText("This workflow has "+ outputs.size() + " output : ");
		}
		LayoutInflater myInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		resultListAdapter = new OutputListAdaptor(myInflater, listData);
		resultList.setAdapter(resultListAdapter);

		currentActivity = this; // for access of this activity inside OnClickListner

		returnButton.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(android.view.View v) {
				returnToStarterActivity();
				currentActivity.finish();
			}
		});
	}
	
	private void prepareListData(HashMap<String, String> outputs){
		listData = new ArrayList<HashMap<String, String>>();
		Iterator<Entry<String, String>> it = outputs.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String> pair = it.next();
			String outputName = pair.getKey();
			String outputValue = pair.getValue();
			HashMap<String, String> oneOutput = new HashMap<String, String>();
			oneOutput.put(outputName, outputValue);
			listData.add(oneOutput);
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

	private class OutputListAdaptor extends BaseAdapter {

		private LayoutInflater myInflater;
		private ArrayList<HashMap<String, String>> data;

		private TextView outputNameView;

		public OutputListAdaptor(LayoutInflater layoutInflater, ArrayList<HashMap<String, String>> listData)
		{
			myInflater = layoutInflater;
			data = listData;
		}

		public int getCount() {
			return data.size();
		}

		public Object getItem(int position) {
			return data.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null)
			{
				convertView = myInflater.inflate(R.layout.outputs_single_row, null);
			}

			outputNameView = (TextView) convertView.findViewById(R.id.outputName);
			TextView outputValueView = (TextView) convertView.findViewById(R.id.outputValue);

			Map<String, String> nameValuePair = (Map<String, String>) getItem(position);
			
			String outputName = nameValuePair.keySet().iterator().next();
			outputNameView.setText(outputName);
			String outputValue = nameValuePair.get(outputName);
			if (outputValue != null){
				outputValueView.setText(outputValue);
			}

			return convertView;
		}	
	}
}

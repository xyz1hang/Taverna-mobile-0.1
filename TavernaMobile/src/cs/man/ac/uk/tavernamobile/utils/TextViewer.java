package cs.man.ac.uk.tavernamobile.utils;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.R;

public class TextViewer extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_text);
		
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#D02E2E2E")));
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setIcon(this.getResources().getDrawable(R.drawable.taverna_wheel_logo_medium));

		String textToShow = getIntent().getStringExtra("textToShow");
		String textTitle = getIntent().getStringExtra("textTitle");
		TextView text = (TextView) findViewById(R.id.textView);
		text.setText(textToShow);
		actionBar.setTitle(textTitle);
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
}

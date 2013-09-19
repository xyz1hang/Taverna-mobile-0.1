package cs.man.ac.uk.tavernamobile.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import cs.man.ac.uk.tavernamobile.R;

public class ShowImage extends Activity {

	private String imageURI;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_image);
		
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#D02E2E2E")));
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setIcon(this.getResources().getDrawable(R.drawable.taverna_wheel_logo_medium));

		WebView imageWeb = (WebView) findViewById(R.id.imageWebView);
		imageURI = getIntent().getStringExtra("imageURI"); 
		if(imageURI == null){
			String imgFilePath = getIntent().getStringExtra("imgFilePath");
			String imageTitle = getIntent().getStringExtra("imageTitle");
			actionBar.setTitle(imageTitle);
			String imagePath = "file://"+ imgFilePath;
			imageURI = "<html><head></head><body><img src=\""+ imagePath + "\"></body></html>";
			imageWeb.loadDataWithBaseURL("", imageURI, "text/html","utf-8", "");  
		}else{
			imageWeb.loadUrl(imageURI);
		}
		/*else{
			String imgFilePath = getIntent().getStringExtra("imgFilePath");
			String imageTitle = getIntent().getStringExtra("imageTitle");
			actionBar.setTitle(imageTitle);
			
			String imagePath = "file://"+ imgFilePath;
			imageURI = "<html><head></head><body><img src=\""+ imagePath + "\"></body></html>";
			File imgFile = new File(imgFilePath);
			if(imgFile.exists()){
			    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			    myImage.setImageBitmap(myBitmap);
			    imageWeb.setVisibility(8);
			    myImage.setVisibility(0);
			}
		}*/
		imageWeb.getSettings().setBuiltInZoomControls(true);
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

	protected Bitmap retrieveImage(String uri) {

		Drawable image = null;
		InputStream is = null;
		String inputurl = uri;
		try {
			URL url = new URL(inputurl);
			URLConnection connection = url.openConnection();
			connection.setUseCaches(true);
			Object content = connection.getContent();		        
			is = (InputStream) content;
			image = Drawable.createFromStream(is,"src");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
		return bitmap;
	}
}

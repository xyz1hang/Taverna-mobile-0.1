package cs.man.ac.uk.tavernamobile.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.webkit.WebView;
import cs.man.ac.uk.tavernamobile.R;

public class ShowImage extends Activity {

	private String imageURI;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_image);

		imageURI = getIntent().getStringExtra("imageURI"); 
		WebView imageWeb = (WebView) findViewById(R.id.imageWebView);
		imageWeb.loadUrl(imageURI);
		imageWeb.getSettings().setBuiltInZoomControls(true);
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

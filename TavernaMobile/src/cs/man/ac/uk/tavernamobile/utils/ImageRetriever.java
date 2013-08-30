package cs.man.ac.uk.tavernamobile.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageRetriever {
	
	private android.support.v4.util.LruCache<String, Bitmap> imageCache;
	
	public ImageRetriever(){
		imageCache = TavernaAndroid.getmMemoryCache();
	}

	public Bitmap retrieveAvatarImage(String imageUri) {

		Drawable image = null;
		InputStream is = null;
		String inputurl = imageUri;
		try {
			URL url = new URL(inputurl);
			Object content = url.getContent();
			is = (InputStream) content;
			image = Drawable.createFromStream(is, "src");
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
		// cache the image
		imageCache.put(imageUri, bitmap);
		TavernaAndroid.setmMemoryCache(imageCache);
		
		return bitmap;
	}
}

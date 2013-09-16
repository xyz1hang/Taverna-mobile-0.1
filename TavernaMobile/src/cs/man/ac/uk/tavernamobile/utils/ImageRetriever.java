package cs.man.ac.uk.tavernamobile.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import uk.org.taverna.server.client.NetworkConnectionException;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageRetriever {
	
	private android.support.v4.util.LruCache<String, Bitmap> imageCache;
	
	public ImageRetriever(){
		imageCache = TavernaAndroid.getmMemoryCache();
	}

	public Bitmap retrieveAvatarImage(String imageUri) throws NetworkConnectionException {

		Bitmap bitmap = null;
		InputStream is = null;
		try {
			URL url = new URL(imageUri);
			Object content = url.getContent();
			is = (InputStream) content;
			Drawable image = Drawable.createFromStream(is, "src");
			
			bitmap = ((BitmapDrawable) image).getBitmap();
			// cache the image
			imageCache.put(imageUri, bitmap);
			TavernaAndroid.setmMemoryCache(imageCache);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch(FileNotFoundException e){
			// swallow - image not available notice should be shown
		} catch (IOException e) {
			throw new NetworkConnectionException();
		} finally{
			try {
				is.close();
			} catch (IOException e) {
				// swallow - irrelevant exception message
				e.printStackTrace();
			}
		}
		
		return bitmap;
	}
}

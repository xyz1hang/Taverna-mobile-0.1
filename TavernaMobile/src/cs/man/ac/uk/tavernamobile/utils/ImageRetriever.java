package cs.man.ac.uk.tavernamobile.utils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.output.ByteArrayOutputStream;

import uk.org.taverna.server.client.NetworkConnectionException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageRetriever {
	
	private android.support.v4.util.LruCache<String, Bitmap> imageCache;
	
	public ImageRetriever(){
		imageCache = TavernaAndroid.getmMemoryCache();
	}

	public Bitmap retrieveAvatarImage(String imageUri) throws NetworkConnectionException {

		if(imageUri == null){
			return null;
		}
		
		Bitmap bitmap = null;
		InputStream is = null;
		try {
			URL url = new URL(imageUri);
			Object content = url.getContent();
			is = (InputStream) content;
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] buffer = new byte[10240];
			int len;
			try {
				while ((len = is.read(buffer)) > -1 ) {
					baos.write(buffer, 0, len);
				}
				baos.flush();
			} catch (IOException e2) {
				e2.printStackTrace();
			} 

			// make a copy of the input stream
			InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
			InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
			
			BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(is1, null, o);

	        final int REQUIRED_SIZE = 100;

	        int width_tmp = o.outWidth, height_tmp = o.outHeight;
	        int scale = 1;
	        while (!(width_tmp / 2 < REQUIRED_SIZE && height_tmp / 2 < REQUIRED_SIZE)) {
	            width_tmp /= 2;
	            height_tmp /= 2;
	            scale *= 2;
	        }

	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        bitmap = BitmapFactory.decodeStream(is2, null, o2);
			
			if(bitmap != null){
				imageCache.put(imageUri, bitmap);
				TavernaAndroid.setmMemoryCache(imageCache);
			}
			
			/*Drawable image = Drawable.createFromStream(is, "src");
			if(image != null){
				bitmap = ((BitmapDrawable) image).getBitmap();
				// cache the image
				imageCache.put(imageUri, bitmap);
				TavernaAndroid.setmMemoryCache(imageCache);
			}*/
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch(FileNotFoundException e){
			// swallow - image not available notice should be shown
		} catch (IOException e) {
			throw new NetworkConnectionException();
		} catch (Exception e){
			// swallow - any other exception happened here should never reflect to UI
			e.printStackTrace();
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

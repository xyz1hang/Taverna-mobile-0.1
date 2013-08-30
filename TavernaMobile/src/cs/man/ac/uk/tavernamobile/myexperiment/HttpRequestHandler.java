package cs.man.ac.uk.tavernamobile.myexperiment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import local.org.apache.http.client.utils.HttpClientUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import uk.org.taverna.server.client.NetworkConnectionException;

import android.app.Activity;
import android.util.Log;
import cs.man.ac.uk.tavernamobile.utils.TavernaAndroid;

public class HttpRequestHandler {

	// private Object mLock = new Object();
	private CookieStore mCookie = null;

	public HttpRequestHandler(Activity activity) {
		if (activity != null) {
			mCookie = TavernaAndroid.getMyExperimentSessionCookies();
		}
	}

	private byte[] serialize(Object data) {
		Serializer serializer = new Persister();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			serializer.write(data, os);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return os.toByteArray();
	}

	public Object Post(String uri, Object data, String contentType) throws NetworkConnectionException {
		Object serverResponse = null;

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(uri);

		byte[] messageContent = this.serialize(data);
		InputStreamEntity entity = new InputStreamEntity(
				new ByteArrayInputStream(messageContent), messageContent.length);
		entity.setContentType(contentType);
		httpPost.setEntity(entity);

		mCookie = TavernaAndroid.getMyExperimentSessionCookies();
		if (mCookie != null) {
			((AbstractHttpClient) httpClient).setCookieStore(mCookie);
		}

		HttpResponse postResponse = null;
		try {
			postResponse = httpClient.execute(httpPost);

			serverResponse = postResponse.getEntity().getContent();

			// reset the cookie with every new request
			CookieStore mCookie = ((AbstractHttpClient) httpClient).getCookieStore();
			TavernaAndroid.setMyExperimentSessionCookies(mCookie);

		} catch (IOException e){
			throw new NetworkConnectionException(e.getMessage(), e);
		} catch (Exception e) {
			throw new NetworkConnectionException(e.getMessage(), e);
		} finally {
			HttpClientUtils.closeQuietly(postResponse);
			HttpClientUtils.closeQuietly(httpClient);
		}

		return serverResponse;
	}

	// method to deserialise XML data to target class type
	private <T> Object deSerialize(Class<T> classType, String data) {

		Serializer serializer = new Persister();

		Object result = null;
		try {
			result = serializer.read(classType, data, false);
		} catch (Exception e) {
			// TODO: log has be removed in the release version
			Log.e("HTTP request / serialization error", e.getMessage());
		}

		return result;
	}

	// handle HTTP requests.
	public <T> Object Get(String uri, Class<T> classType, String username,
			String password) throws NetworkConnectionException {
		Object targetClass = null;

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(uri);
		((AbstractHttpClient) httpClient).setCookieStore(mCookie);

		if (username != null & password != null) {
			httpGet.addHeader(BasicScheme.authenticate(
					new UsernamePasswordCredentials(username, password),
					"UTF-8", false));
		}

		HttpResponse getResponse = null;
		try {
			getResponse = httpClient.execute(httpGet);

			if (isSuccess(getResponse, HttpURLConnection.HTTP_OK)) {
				HttpEntity entity = getResponse.getEntity();
				if (entity != null) {
					String responceString = EntityUtils.toString(entity);
					targetClass = this.deSerialize(classType, responceString);
				}
			} else {
				getError(getResponse, new URI(uri));
			}
		} catch (IOException e){
			throw new NetworkConnectionException(e.getMessage(), e);
		} catch (Exception e) {
			throw new NetworkConnectionException(e.getMessage(), e);
		} finally {
			HttpClientUtils.closeQuietly(getResponse);
			HttpClientUtils.closeQuietly(httpClient);
		}

		return targetClass;
	}

	private void getErrorResponse(HttpResponse response, HttpEntity entity,
			URI requestURI) throws Exception {
		int status = response.getStatusLine().getStatusCode();

		if (entity != null) {
			try {
				EntityUtils.toString(entity);
			} catch (IOException e) {
				// Ignore.
			}
		}

		switch (status) {
		case HttpURLConnection.HTTP_BAD_REQUEST:
			throw new Exception("Bad request");
		case HttpURLConnection.HTTP_UNAVAILABLE:
			throw new Exception("Server error - Service unavaiable");
		case HttpURLConnection.HTTP_UNAUTHORIZED:
			throw new Exception("Unauthorized request");
		case HttpURLConnection.HTTP_BAD_GATEWAY:
			throw new Exception("Server error - Bad gateway");
		default:
			throw new Exception("unknow server error");
		}
	}

	private void getError(HttpResponse response, URI requestURI)
			throws Exception {
		getErrorResponse(response, response.getEntity(), requestURI);
	}

	private boolean isSuccess(HttpResponse response, int success) {
		return response.getStatusLine().getStatusCode() == success;
	}
}

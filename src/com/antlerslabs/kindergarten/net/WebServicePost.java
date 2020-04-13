package com.antlerslabs.kindergarten.net;

import com.antlerslabs.kindergarten.parser.Parser;
import com.antlerslabs.kindergarten.parser.ParserFactory;

import java.util.List;
import java.util.Enumeration;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import android.graphics.Bitmap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.content.ByteArrayBody;

class WebServicePost extends WebServiceRequest {
	private Parser mParser;
	
	@Override
	public <T> T getForObject(Class<T> cls) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost();
		
		MultipartEntity multipartEntity = new MultipartEntity();
		Enumeration<String> keys = mPostValues.keys();
		
		try {
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				
				if(mPostValues.get(key).getClass().isAssignableFrom(File.class)) {
					multipartEntity.addPart(key, new FileBody((File) mPostValues.get(key)));
				} else if(mPostValues.get(key).getClass().isAssignableFrom(Bitmap.class)) {
					Bitmap bitmap = (Bitmap) mPostValues.get(key);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
					multipartEntity.addPart(key, new ByteArrayBody(baos.toByteArray(), "image/png", "image.png"));
				} else {
					multipartEntity.addPart(key, new StringBody(mPostValues.get(key).toString(), Charset.forName("UTF-8")));
				}
			}
			
			post.setURI(mApiUri);
			post.setEntity(multipartEntity);
			
			HttpResponse response = client.execute(post);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				switch(mResultFormat) {
					case XML:
						mParser = ParserFactory.createParser(DataFormat.XML);
						break;
					case JSON:
						mParser = ParserFactory.createParser(DataFormat.JSON);
						break;
				}
				
				mApiUri = null;
				mPostValues = null;
				mResultFormat = null;
				
				mParser.parse(response.getEntity().getContent());
				
				return mParser.getForObject(cls);
			}
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch(ClientProtocolException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(InstantiationException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public <T> List<T> getForObjects(Class<T> cls) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost();
		
		MultipartEntity multipartEntity = new MultipartEntity();
		Enumeration<String> keys = mPostValues.keys();
		
		try {
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				
				if(mPostValues.get(key).getClass().isAssignableFrom(File.class)) {
					multipartEntity.addPart(key, new FileBody((File) mPostValues.get(key)));
				} else {
					multipartEntity.addPart(key, new StringBody(mPostValues.get(key).toString(), Charset.forName("UTF-8")));
				}
			}
			
			post.setURI(mApiUri);
			post.setEntity(multipartEntity);
			
			HttpResponse response = client.execute(post);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				switch(mResultFormat) {
					case XML:
						mParser = ParserFactory.createParser(DataFormat.XML);
						break;
					case JSON:
						mParser = ParserFactory.createParser(DataFormat.JSON);
						break;
				}
			}
			
			mApiUri = null;
			mPostValues = null;
			mResultFormat = null;
			
			mParser.parse(response.getEntity().getContent());
			
			return mParser.getForObjects(cls);
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch(ClientProtocolException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String getRawResponse() {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost();
		
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		Enumeration<String> keys = mPostValues.keys();
		
		try {
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				
				if(mPostValues.get(key).getClass().isAssignableFrom(File.class)) {
					multipartEntity.addPart(key, new FileBody((File) mPostValues.get(key), "image/jpeg"));
				} else if(mPostValues.get(key).getClass().isAssignableFrom(Bitmap.class)) {
					Bitmap bitmap = (Bitmap) mPostValues.get(key);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
					multipartEntity.addPart(key, new ByteArrayBody(baos.toByteArray(), "image/jpeg", "image.jpg"));
				} else {
					multipartEntity.addPart(key, new StringBody(mPostValues.get(key).toString(), Charset.forName("UTF-8")));
				}
			}
			
			post.setURI(mApiUri);
			post.setEntity(multipartEntity);
			
			HttpResponse response = client.execute(post);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream is = response.getEntity().getContent();
				StringBuffer sb = new StringBuffer();
				byte[] buffer = new byte[1024];
				int len = 0;
				
				while((len = is.read(buffer)) != -1) {
					sb.append(new String(buffer, 0, len));
				}
				
				return sb.toString();
			}
			
			mApiUri = null;
			mPostValues.clear();
			mResultFormat = null;
			
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch(ClientProtocolException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

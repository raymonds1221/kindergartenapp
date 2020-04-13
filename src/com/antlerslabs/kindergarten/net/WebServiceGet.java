package com.antlerslabs.kindergarten.net;

import com.antlerslabs.kindergarten.parser.Parser;
import com.antlerslabs.kindergarten.parser.ParserFactory;

import java.util.List;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

class WebServiceGet extends WebServiceRequest {
	private Parser mParser;
	
	@Override
	public <T> T getForObject(Class<T> cls) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet();
		
		try {
			get.setURI(mApiUri);
			HttpResponse response = client.execute(get);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				switch(mResultFormat) {
					case XML:
						mParser = ParserFactory.createParser(DataFormat.XML);
						break;
					case JSON:
						mParser = ParserFactory.createParser(DataFormat.JSON);
						break;
				}
				
				mParser.parse(response.getEntity().getContent());
				
				return mParser.getForObject(cls);
			}
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
		HttpGet get = new HttpGet();
		
		try {
			get.setURI(mApiUri);
			HttpResponse response = client.execute(get);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				switch(mResultFormat) {
					case XML:
						mParser = ParserFactory.createParser(DataFormat.XML);
						break;
					case JSON:
						mParser = ParserFactory.createParser(DataFormat.JSON);
						break;
				}
				
				mParser.parse(response.getEntity().getContent());
				
				return mParser.getForObjects(cls);
			}
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
		HttpGet get = new HttpGet();
		
		try {
			get.setURI(mApiUri);
			HttpResponse response = client.execute(get);
			InputStream is = response.getEntity().getContent();
			StringBuffer sb = new StringBuffer();
			byte[] buffer = new byte[1024];
			int len = 0;
			
			while((len = is.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, len));
			}
			
			return sb.toString();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

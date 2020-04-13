package com.antlerslabs.kindergarten.parser;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;

public abstract class Parser {
	public abstract void parse(InputStream data);
	public abstract <T> T getForObject(Class<T> cls) throws InstantiationException;
	public abstract <T> List<T> getForObjects(Class<T> cls) throws InstantiationException;
	public abstract Object getSpecificValue(String key);
	public abstract <T> List<T> getSpecificXPathList(String xpath, Class<T> cls);
	
	protected String streamToString(InputStream data) {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(data));
			StringBuffer values = new StringBuffer();
			String tempStr = "";
			
			while((tempStr = reader.readLine()) != null) {
				values.append(tempStr);
			}
			
			return values.toString();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public InputStream stringToStream(String url) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet();
		
		try {
			get.setURI(new URI(url));
			HttpResponse response = client.execute(get);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return response.getEntity().getContent();
			}
		} catch(URISyntaxException e) {
			e.printStackTrace();
		} catch(ClientProtocolException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}

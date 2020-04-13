package com.antlerslabs.kindergarten.helpers;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class Network {
	private static Network mNetwork;
	private Context mContext;

	private Network(Context context) {
		mContext = context;
	}
	
	public static final Network getInstance(Context context) {
		if(mNetwork == null)
			mNetwork = new Network(context);
		return mNetwork;
	}
	
	public boolean isAvailable() {
		ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		
		if(networkInfo != null && networkInfo.isConnected())
			return true;
		return false;
	}
	
	public InputStream downloadOnlineContent(String url) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		
		try {
			HttpResponse response = client.execute(get);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return response.getEntity().getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}

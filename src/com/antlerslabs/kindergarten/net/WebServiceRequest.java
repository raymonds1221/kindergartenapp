package com.antlerslabs.kindergarten.net;

import com.antlerslabs.kindergarten.annotation.Parameter;
import com.antlerslabs.kindergarten.pojo.BaseObject;

import java.lang.reflect.Field;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class WebServiceRequest {
	protected URI mApiUri;
	private CharSequence mApiName = "";
	protected DataFormat mResultFormat = DataFormat.JSON;
	private final List<CharSequence> mParameters;
	protected Hashtable<String, Object> mPostValues = new Hashtable<String, Object>();
	public static WebServiceRequest GET;
	public static WebServiceRequest POST;
	private WebServiceRequest mRequestMethod;
	
	static {
		GET = new WebServiceGet();
		POST = new WebServicePost();
	}
	
	WebServiceRequest() {
		mParameters = new ArrayList<CharSequence>();
		mPostValues = new Hashtable<String, Object>();
	}
	
	public static final WebServiceRequest newInstance() {
		return new WebServiceRequest();
	}
	
	public synchronized WebServiceRequest setApiUri(URI apiUri) {
		this.mParameters.clear();
		this.mPostValues.clear();
		this.mApiUri = apiUri;
		return this;
	}
	
	public synchronized WebServiceRequest setApiUri(String apiUri) {
		try {
			this.mParameters.clear();
			this.mPostValues.clear();
			this.mApiUri = new URI(apiUri);
		} catch(URISyntaxException e) {
			e.printStackTrace();
		}
		
		return this;
	}
	
	public synchronized WebServiceRequest setApiName(CharSequence apiName) {
		this.mApiName = apiName;
		return this;
	}
	
	public synchronized WebServiceRequest setResultFormat(DataFormat resultFormat) {
		this.mResultFormat = resultFormat;
		mParameters.add(resultFormat.toString());
		return this;
	}
	
	public synchronized WebServiceRequest addParameter(CharSequence param) {
		mParameters.add(param);
		return this;
	}
	
	public synchronized WebServiceRequest putPostValues(String key, Object value) {
		if(value != null)
			mPostValues.put(key, value);
		return this;
	}
	
	public synchronized WebServiceRequest putPostValues(BaseObject baseObject) {
		Field[] fields = baseObject.getClass().getDeclaredFields();
		
		for(Field field: fields) {
			field.setAccessible(true);
			Annotation aParameter = field.getAnnotation(Parameter.class);
			String key = "";
			
			if(aParameter != null && aParameter instanceof Parameter) {
				Parameter parameter = (Parameter) aParameter;
				key = parameter.name();
				
				try {
					if(field.getType().isPrimitive()) {
						if(field.getType().isAssignableFrom(int.class)) {
							mPostValues.put(key, field.getInt(baseObject));
						} else if(field.getType().isAssignableFrom(boolean.class)) {
							if(field.getBoolean(baseObject)) {
								mPostValues.put(key, "1");
							} else {
								mPostValues.put(key, "0");
							}
						}
					} else {
						if(field.get(baseObject) != null)
							mPostValues.put(key, field.get(baseObject));
					}
				} catch(IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return this;
	}
	
	public synchronized WebServiceRequest setRequestMethod(WebServiceRequest requestMethod) {
		this.mRequestMethod = requestMethod;
		return this;
	}
	
	public synchronized <T> T getForObject(Class<T> cls) {
		String parameters = "";
		Iterator<CharSequence> iterator = mParameters.iterator();
		
		while(iterator.hasNext()) {
			parameters += "/" + iterator.next();
		}
		
		try {
			mApiUri = new URI(mApiUri.toString() + mApiName + parameters);
			mRequestMethod.mApiUri = mApiUri;
			mRequestMethod.mPostValues = mPostValues;
			mRequestMethod.mResultFormat = mResultFormat;
			mApiUri = null;
			mResultFormat = null;
			//mPostValues.clear();
			mParameters.clear();
		} catch(URISyntaxException e) {
			e.printStackTrace();
		}
		
		return mRequestMethod.getForObject(cls);
	}
	
	public synchronized <T> List<T> getForObjects(Class<T> cls) {
		String parameters = "";
		Iterator<CharSequence> iterator = mParameters.iterator();
		
		while(iterator.hasNext()) {
			parameters += "/" + iterator.next();
		}
		
		try {
			mApiUri = new URI(mApiUri.toString() + mApiName + parameters);
			mRequestMethod.mApiUri = mApiUri;
			mRequestMethod.mPostValues = mPostValues;
			mRequestMethod.mResultFormat = mResultFormat;
			mApiUri = null;
			mResultFormat = null;
			mPostValues.clear();
			mParameters.clear();
		} catch(URISyntaxException e) {
			e.printStackTrace();
		}
		
		return mRequestMethod.getForObjects(cls);
	}
	
	public String getRawResponse() {
		String parameters = "";
		Iterator<CharSequence> iterator = mParameters.iterator();
		
		while(iterator.hasNext()) {
			parameters += "/" + iterator.next();
		}
		
		try {
			mApiUri = new URI(mApiUri.toString() + mApiName + parameters);
			mRequestMethod.mApiUri = mApiUri;
			mRequestMethod.mPostValues = mPostValues;
			mRequestMethod.mResultFormat = mResultFormat;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return mRequestMethod.getRawResponse();
	}
	
	public static enum DataFormat {
		XML("xml"), JSON("json");
		
		private String format;
		
		private DataFormat(String format) {
			this.format = format;
		}
		
		@Override
		public String toString() {
			return format;
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}

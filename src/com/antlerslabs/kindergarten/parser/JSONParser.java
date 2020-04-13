package com.antlerslabs.kindergarten.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.antlerslabs.kindergarten.annotation.ApiMethod;
import com.antlerslabs.kindergarten.annotation.Parameter;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

class JSONParser extends Parser {
	private static JSONParser mJSONParser;
	private JSONObject mJSONObject;
	
	private JSONParser() {}
	
	public static final JSONParser newInstance() {
		if(mJSONParser == null)
			mJSONParser = new JSONParser();
		return mJSONParser;
	}
	
	public static final JSONParser buildParser(String data) {
		if(mJSONParser == null)
			mJSONParser = new JSONParser();
		try {
			mJSONParser.mJSONObject = new JSONObject(data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mJSONParser;
	}
	
	@Override
	public void parse(InputStream data) {
		try {
			mJSONObject = new JSONObject(streamToString(data));
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public <T> T getForObject(Class<T> cls) throws InstantiationException {
		Annotation aApiMethod = cls.getAnnotation(ApiMethod.class);
		
		if(aApiMethod != null && aApiMethod instanceof ApiMethod) {
			ApiMethod apiMethod = (ApiMethod) aApiMethod;
			
			try {
				Object oRoot = mJSONObject.get(apiMethod.rootName());
				
				if(!oRoot.getClass().isAssignableFrom(JSONObject.class)) {
					throw new InstantiationException("Object must be of type JSONObject ");
				}
				
				JSONObject joRoot = (JSONObject) oRoot;
				T t = cls.newInstance();
				Field[] fields = cls.getDeclaredFields();
				
				for(Field field: fields) {
					field.setAccessible(true);
					
					Annotation aParameter = field.getAnnotation(Parameter.class);
					
					if(aParameter != null && aParameter instanceof Parameter) {
						Parameter parameter = (Parameter) aParameter;
						
						if(field.getType().isPrimitive()) {
							if(field.getType().isAssignableFrom(int.class)) {
								field.setInt(t, joRoot.getInt(parameter.name()));
							} else if(field.getType().isAssignableFrom(double.class)) {
								field.setDouble(t, joRoot.getDouble(parameter.name()));
							}
						} else {
							if(field.getType().isAssignableFrom(InputStream.class) && parameter.download()) {
								field.set(t, stringToStream(joRoot.getString(parameter.name())));
							} else if(field.getType().isAssignableFrom(Date.class)) {
								DateFormat dateFormat = new SimpleDateFormat(parameter.format());
								field.set(t, dateFormat.parse(joRoot.getString(parameter.name())));
							} else {
								field.set(t, joRoot.getString(parameter.name()));
							}
						}
					}
				}
				
				return t;
			} catch(JSONException e) {
				e.printStackTrace();
			} catch(IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
		}
		
		return null;
	}

	@Override
	public <T> List<T> getForObjects(Class<T> cls) throws InstantiationException {
		Annotation aApiMethod = cls.getAnnotation(ApiMethod.class);
		List<T> values = new ArrayList<T>();
		
		if(aApiMethod != null && aApiMethod instanceof ApiMethod) {
			ApiMethod apiMethod = (ApiMethod) aApiMethod;
			
			try {
				Object oRoot = mJSONObject.get(apiMethod.rootName());
				
				if(!oRoot.getClass().isAssignableFrom(JSONArray.class)) {
					throw new InstantiationException("Object must be of type JSONArray");
				}
				
				JSONArray jaRoot = (JSONArray) oRoot;
				Field[] fields = cls.getDeclaredFields();
				
				for(int i=0;i<jaRoot.length();i++) {
					JSONObject joItem = jaRoot.getJSONObject(i);
					T t = cls.newInstance();
					
					for(Field field: fields) {
						field.setAccessible(true);
						
						Annotation aParameter = field.getAnnotation(Parameter.class);
						
						if(aParameter != null && aParameter instanceof Parameter) {
							Parameter parameter = (Parameter) aParameter;
							
							if(field.getType().isPrimitive()) {
								if(field.getType().isAssignableFrom(int.class)) {
									field.setInt(t, joItem.getInt(parameter.name()));
								} else if(field.getType().isAssignableFrom(double.class)) {
									field.setDouble(t, joItem.getDouble(parameter.name()));
								}
							} else {
								if(field.getType().isAssignableFrom(InputStream.class) && parameter.download()) {
									field.set(t, stringToStream(joItem.getString(parameter.name())));
								} else if(field.getType().isAssignableFrom(Date.class)) {
									DateFormat dateFormat = new SimpleDateFormat(parameter.format());
									field.set(t, dateFormat.parseObject(joItem.getString(parameter.name())));
								} else {
									field.set(t, joItem.getString(parameter.name()));
								}
							}
						}
					}
					
					values.add(t);
				}
			} catch(JSONException e) {
				e.printStackTrace();
			} catch(IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		return values;
	}
	
	@Override
	public Object getSpecificValue(String key) {
		try {
			return mJSONObject.get(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public <T> List<T> getSpecificXPathList(String xpath, Class<T> cls) {
		return null;
	}
}

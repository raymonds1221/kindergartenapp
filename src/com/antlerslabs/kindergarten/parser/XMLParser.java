package com.antlerslabs.kindergarten.parser;

import com.antlerslabs.kindergarten.annotation.ApiMethod;
import com.antlerslabs.kindergarten.annotation.Parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Date;
import java.util.List;
import java.util.ArrayList;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

class XMLParser extends Parser {
	private static XMLParser mXMLParser;
	private InputSource mInputSource;
	private Document mDocument;
	
	public static final XMLParser newInstance() {
		if(mXMLParser == null)
			mXMLParser = new XMLParser();
		return mXMLParser;
	}
	
	public static final XMLParser buildParser(String data) {
		if(mXMLParser == null)
			mXMLParser = new XMLParser();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			mXMLParser.mInputSource = new InputSource(new StringReader(data));
			mXMLParser.mInputSource.setEncoding("UTF-8");
			
			mXMLParser.mDocument = db.parse(mXMLParser.mInputSource); 
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return mXMLParser;
	}
	
	@Override
	public void parse(InputStream data) {
		mInputSource = new InputSource(data);
		mInputSource.setEncoding("utf-8");
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			mDocument = db.parse(mInputSource);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public <T> T getForObject(Class<T> cls) throws InstantiationException {
		DomXMLParser<T> domXMLParser = new DomXMLParser<T>(cls);
		domXMLParser.parse(mDocument);
		return domXMLParser.getForObject();
	}

	@Override
	public <T> List<T> getForObjects(Class<T> cls) throws InstantiationException {
		DomXMLParser<T> domXMLParser = new DomXMLParser<T>(cls);
		domXMLParser.parse(mDocument);
		return domXMLParser.getForObjects();
	}
	
	private class DomXMLParser<T> {
		private Class<T> cls;
		private List<T> values;
		
		public DomXMLParser(Class<T> cls) {
			this.cls = cls;
			values = new ArrayList<T>();
		}
		
		public T getForObject() {
			if(values.size() > 0)
				return values.get(0);
			return null;
		}
		
		public List<T> getForObjects() {
			return values;
		}
		
		public synchronized void parse(Document document) {
			Annotation aApiMethod = cls.getAnnotation(ApiMethod.class);
			
			if(aApiMethod != null && aApiMethod instanceof ApiMethod) {
				ApiMethod apiMethod = (ApiMethod) aApiMethod;
				
				if(document.getDocumentElement() != null && document.getDocumentElement().getNodeName().equals(apiMethod.rootName())) {
					XPathFactory xpf = XPathFactory.newInstance();
					XPath xpath = xpf.newXPath();
					
					//NodeList items = document.getElementsByTagName(apiMethod.subName());
					NodeList items = null;
					try {
						items = (NodeList) xpath.evaluate(String.format("/%s/%s", apiMethod.rootName(), apiMethod.subName()), document, XPathConstants.NODESET);
					} catch (XPathExpressionException e1) {
						e1.printStackTrace();
					}
					
					if(apiMethod.hasChildNodes() && items.getLength() > 0)
						items = items.item(0).getChildNodes();
					
					for(int i=0;i<items.getLength();i++) {
						Node item = items.item(i);
						
						try {
							T value = cls.newInstance();
							Field[] fields = cls.getDeclaredFields();
							
							NodeList params = item.getChildNodes();
							
							for(int j=0;j<params.getLength();j++) {
								Node param = params.item(j);
								
								for(Field field: fields) {
									field.setAccessible(true);
									Annotation aParameter = field.getAnnotation(Parameter.class);
									
									if(aParameter != null && aParameter instanceof Parameter) {
										Parameter parameter = (Parameter) aParameter;
										
										if(param.getNodeName().equalsIgnoreCase(parameter.name())) {
											if(field.getType().isPrimitive()) {
												if(field.getType().isAssignableFrom(int.class)) {
													field.setInt(value, Integer.parseInt(param.getFirstChild().getNodeValue()));
												} else if(field.getType().isAssignableFrom(double.class)) {
													field.setDouble(value, Double.parseDouble(param.getFirstChild().getNodeValue()));
												}
											} else {
												if(field.getType().isAssignableFrom(InputStream.class) && parameter.download()) {
													field.set(value, stringToStream(param.getFirstChild().getNodeValue()));
												} else if(field.getType().isAssignableFrom(Date.class)) {
													DateFormat dateFormat = new SimpleDateFormat(parameter.format());
													field.set(value, dateFormat.parse(param.getFirstChild().getNodeValue()));
												} else {
													field.set(value, param.getTextContent());
												}
											}
											break;
										}
									}
								}
							}
							
							values.add(value);
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (DOMException e) {
							e.printStackTrace();
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	@Override
	public Object getSpecificValue(String key) {
		NodeList nl = mDocument.getElementsByTagName(key);
		
		if(nl != null && nl.getLength() > 0)
			return nl.item(0).getFirstChild().getNodeValue();
		
		return null;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public <T> List<T> getSpecificXPathList(String xpath, Class<T> cls) {
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xp = xpf.newXPath();
		List<T> values = new ArrayList<T>();
		
		try {
			NodeList nl = (NodeList) xp.evaluate(xpath, mDocument, XPathConstants.NODESET);
			Field[] fields = cls.getDeclaredFields();
			
			for(int i=0;i<nl.getLength();i++) {
				T value = cls.newInstance();
				
				NodeList params = nl.item(i).getChildNodes();
				
				for(int j=0;j<params.getLength();j++) {
					Node param = params.item(j);
					
					for(Field field: fields) {
						field.setAccessible(true);
						Annotation aParameter = field.getAnnotation(Parameter.class);
						
						if(aParameter != null && aParameter instanceof Parameter) {
							Parameter parameter = (Parameter) aParameter;
							
							if(param.getNodeName().equalsIgnoreCase(parameter.name())) {
								if(field.getType().isPrimitive()) {
									if(field.getType().isAssignableFrom(int.class)) {
										field.setInt(value, Integer.parseInt(param.getFirstChild().getNodeValue()));
									} else if(field.getType().isAssignableFrom(double.class)) {
										field.setDouble(value, Double.parseDouble(param.getFirstChild().getNodeValue()));
									}
								} else {
									if(field.getType().isAssignableFrom(InputStream.class) && parameter.download()) {
										field.set(value, stringToStream(param.getFirstChild().getNodeValue()));
									} else if(field.getType().isAssignableFrom(Date.class)) {
										DateFormat dateFormat = new SimpleDateFormat(parameter.format());
										field.set(value, dateFormat.parse(param.getFirstChild().getNodeValue()));
									} else {
										field.set(value, param.getTextContent());
									}
								}
								break;
							}
						}
					}
				}
				
				values.add(value);
			}
			
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return values;
	}
}

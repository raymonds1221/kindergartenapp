package com.antlerslabs.kindergarten.parser;

import com.antlerslabs.kindergarten.net.WebServiceRequest.DataFormat;

public class ParserFactory {

	public static Parser createParser(DataFormat format) {
		Parser parser = null;
		
		switch(format) {
			case XML:
				parser = XMLParser.newInstance();
				break;
			case JSON:
				parser = JSONParser.newInstance();
				break;
		}
		
		return parser;
	}
	
	public static Parser buildParser(String data, DataFormat format) {
		Parser parser = null;
		
		switch(format) {
			case XML:
				parser = XMLParser.buildParser(data);
				break;
			case JSON:
				parser = JSONParser.buildParser(data);
				break;
		}
		
		return parser;
	}
}

package com.akrog.tolomet.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlParser {
	public static XmlElement load( Reader rd ) throws IOException {
		BufferedReader br = new BufferedReader(rd);
		return next(br);
	}
	
	private static XmlElement next( BufferedReader br ) throws IOException {				
		String begin;		
		while( (begin=readLine(br)) != null )
			if( !begin.startsWith("<?") )
				break;
		if( begin == null || begin.startsWith("</") )
			return null;
		
		Matcher matcher = patternName.matcher(begin);
		if( !matcher.find() )
			return null;		
		XmlElement result = new XmlElement();		
		result.setName(matcher.group(1));
		
		matcher = patternAttr.matcher(begin);
		while( matcher.find() )
			result.getAttributes().put(matcher.group(1), matcher.group(2));
		
		if( !begin.endsWith("/>") ) {
			XmlElement subElement;
			while( (subElement=next(br)) != null )
				result.getSubElements().add(subElement);
		}
		
		return result;
	}
	
	private static String readLine( BufferedReader br ) throws IOException {
		String line = br.readLine();
		if( line == null )
			return null;
		return line.replaceAll("^[ \\t\\n]*", "").replaceAll("[ \\t\\n]*$", "");
	}
	
	private final static Pattern patternName = Pattern.compile("^<(\\w+)");
	private final static Pattern patternAttr = Pattern.compile("(\\w+)=\"(.+?)\"");
}

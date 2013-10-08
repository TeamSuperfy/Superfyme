package de.enterprise.lokaServer.tools;

import java.io.IOException;
import java.io.StringWriter;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class JSONConverter {

	public static String toJSON(Object object) {

		StringWriter sw = new StringWriter();
		JsonFactory fac = new JsonFactory();
		JsonGenerator gen;
		ObjectMapper mapper = new ObjectMapper();

		try {
			
			gen = fac.createJsonGenerator(sw);
			mapper.writeValue(gen, object);
			
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sw.toString();
	}
	
	public static String toJsonArray(Object[] objects) {
		
		StringWriter sw = new StringWriter();
		JsonFactory fac = new JsonFactory();
		JsonGenerator gen;
		ObjectMapper mapper = new ObjectMapper();

		try {
			
			gen = fac.createJsonGenerator(sw);
			mapper.writeValue(gen, objects);
			
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sw.toString();
	}

	public static Object fromJSON(String jsonString, String _class) {
		ObjectMapper mapper = new ObjectMapper();
		Object object = null;

		try {

			Class<?> mClass = Class.forName(_class);
			object = mapper.readValue(jsonString, mClass);

		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return object;
	}
	
	public static Object fromJSONArray(String jsonString, String _class) {
		ObjectMapper mapper = new ObjectMapper();
		Object[] details = null;

		try {

			Class<?> mClass = Class.forName(_class);
			details = (Object[]) mapper.readValue(jsonString, mClass);



		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return details;
	}

}
package de.enterprise.lokaServer.tools;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import de.enterprise.lokaServer.pojos.PostPojo;
import de.enterprise.lokaServer.pojos.PostsArrayPojo;

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
	
	public static PostPojo getTest(){
		return null;//(Post) fromJSON(\\"{\\\"loc\\\":{\\\"latitude\\\":10.0,\\\"longitude\\\":10.0},\\\"message\\\":{\\\"date\\\":1333306150846,\\\"picURL\\\":null,\\\"text\\\":\\\"Hallo, test!\\\",\\\"user\\\":{\\\"name\\\":\\\"asdf\\\",\\\"id\\\":1}}}\\", Post.class.getName());
	}

	public static void main(String[] args) {
//		String json = \"{\\"dblLoc\\":{\\"bottomRight\\":{\\"latitude\\":6.0,\\"longitude\\":2.0},\\"upLeft\\":{\\"latitude\\":2.0,\\"longitude\\":6.0}},\\"filter\\":null,\\"query\\":null,\\"userID\\":1}\";
		String dblLoc = "[{\"loc\":{\"longitude\":11464926,\"latitude\":48191472},\"id\":3,\"message\":{\"user\":{\"id\":1,\"name\":\"alex\"},\"picURL\":\"file:/D:/serverImages/image860.0179987367071.jpg\",\"text\":\"fffffuuuuuucccccjkkkk\",\"date\":1334264437369}}]";
		//Post u = (Post) JSONConverter.fromJSON(json, Post.class.getName());
		//RequestPostContents contents = (RequestPostContents) JSONConverter.fromJSON(json, RequestPostContents.class.getName());
		//DoubleLocPojodou = (DoubleLoc) JSONConverter.fromJSON(dblLoc, DoubleLoc.class.getName());
//		Location loc = (Location) JSONConverter.fromJSON(dblLoc, Location.class.getName());
		PostPojo[] postsArr = (PostPojo[]) JSONConverter.fromJSONArray(dblLoc, PostPojo[].class.getName());
		System.out.println(postsArr);
	}

}
package de.enterprise.lokaServer.events;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;

import org.apache.catalina.connector.ResponseFacade;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;

import de.enterprise.lokaServer.LokaServer;

public abstract class ServerEvent{
	
	public String CMD;
	protected LokaServer lokaServer;
	
	protected abstract void execute(CommandEvent cmdEvt) throws Exception;
	
	protected void sendResponse(AsyncContext ctx, String content) throws IOException{
		if (CMD.equals("RTI")) {
			System.out.println(new Date(System.currentTimeMillis())+" sending tiny image to user");
		} else if (CMD.equals("RFI")) {
			System.out.println(new Date(System.currentTimeMillis())+" sending big image to user");
		} else if (CMD.equals("RTHI")) {
			System.out.println(new Date(System.currentTimeMillis())+" sending thumbnail image to user");
		} else {
			System.out.println(new Date(System.currentTimeMillis())+" sending message " + content + " to user");
		}
		

		ServletResponse httpResponse = ctx.getResponse();
		ResponseFacade rf = (ResponseFacade) httpResponse;
		rf.addHeader("Access-Control-Allow-Origin", "*");
		rf.addHeader("Access-Control-Allow-Methods", "POST");
		rf.addHeader("content-type", "text/plain");
		httpResponse.setCharacterEncoding("UTF-8");
		if(content != null){
			httpResponse.getWriter().println(CMD + "#" + content);
		}else{
			rf.setStatus(201);
		}
		ctx.complete();
	}
	
	protected void sendResponseMultipart(AsyncContext ctx, String content, byte[] data) throws IOException{
		System.out.println(new Date(System.currentTimeMillis())+" sending tiny image to user");
		System.out.println("string size: " + (content.getBytes().length));
		System.out.println("package size: " + (content.getBytes().length + data.length));

		ServletResponse httpResponse = ctx.getResponse();
		ResponseFacade rf = (ResponseFacade) httpResponse;
		rf.addHeader("Access-Control-Allow-Origin", "*");
		rf.addHeader("Access-Control-Allow-Methods", "POST");
		httpResponse.setCharacterEncoding("UTF-8");
		httpResponse.setContentType("multipart/mixed");
		
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "SEPERATOR_STRING",
				Charset.forName("UTF-8"));
		entity.addPart("json", new StringBody(CMD + "#" + content, "text/plain", Charset.forName("UTF-8")));
		entity.addPart("image", new ByteArrayBody(data, "image/jpeg", "file"));
		
		httpResponse.setContentLength((int) entity.getContentLength());
		
		entity.writeTo(httpResponse.getOutputStream());
		ctx.complete();
	}
	
	public void setLokaServer(LokaServer lokaServer){
		this.lokaServer = lokaServer;
	}
	
	public LokaServer getLokaServer(){
		return lokaServer;
	}
	
}

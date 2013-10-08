package de.enterprise.lokaServer.events.execution;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;
import de.enterprise.lokaServer.tools.JSONConverter;

public class CreateGroupEvent extends ServerEvent {

	public CreateGroupEvent(){
		CMD = "CG";
	}
	
	private String processImage(Part imagePart) {
		try {
			BufferedImage buff = ImageIO.read(imagePart.getInputStream());
			String path = "/tmp";
			int number = (int) (Math.random() * 100);
			File imgFile = new File(path + "/" + "image" + System.currentTimeMillis() + "-" + number + ".jpg");
			ImageIO.write(buff, "jpg", imgFile);
			return imgFile.toURI().toString();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt) throws Exception {
		String json = "";
		HttpServletRequest req = (HttpServletRequest)cmdEvt.getResponse().getRequest();
		Part jsonPart = null;
		Part imagePart = null;
		if(ServletFileUpload.isMultipartContent((HttpServletRequest) req)){
			jsonPart = req.getPart("json");
			imagePart = req.getPart("file");
			json = new BufferedReader(new InputStreamReader(jsonPart.getInputStream(), "UTF-8")).readLine();
		}else{
			json = cmdEvt.getCmd()[1];
		}
		GroupPojo gp = (GroupPojo) JSONConverter.fromJSON(json, GroupPojo.class.getName());
		if(imagePart != null){
			String url = processImage(imagePart);
			gp.setPic_id(lokaServer.getDBQuery().insertNewPicture(url));
		}
		int groupID = lokaServer.getDBQuery().insertNewGroup(gp);
		UserPojo user = lokaServer.getDBQuery().getUser(gp.getMembers()[0]);
		user.setGroups_available(0);
		int[] groups = user.getGroups();
		if(groups == null){
			groups = new int[]{groupID};
		}
		else{
			groups = CollectionStuff.appendToArray(groups, new int[]{groupID});
		}
		user.setGroups(groups);
		
		lokaServer.getDBQuery().updateUser(user);
		sendResponse(cmdEvt.getResponse(), String.valueOf(groupID));
	}

}

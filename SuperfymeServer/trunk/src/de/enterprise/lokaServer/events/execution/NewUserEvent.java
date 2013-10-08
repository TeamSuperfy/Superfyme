package de.enterprise.lokaServer.events.execution;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.UserPojo;

public class NewUserEvent extends ServerEvent {
	
	private static final int CODE_OK = 0, CODE_ALREADY_IN_USE = -1;

	public NewUserEvent() {
		CMD = "NU";
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
	protected void execute(CommandEvent evt)  throws Exception {
		String json = "";
		HttpServletRequest req = (HttpServletRequest)evt.getResponse().getRequest();
		Part jsonPart = req.getPart("json");
		Part imagePart = req.getPart("file");
		json = new BufferedReader(new InputStreamReader(jsonPart.getInputStream(), "UTF-8")).readLine();
		JSONObject obj = JSONObject.fromObject(json);
		int userID = obj.getInt("UID");
		String username = obj.getString("UN");
		UserPojo user = lokaServer.getDBQuery().getUser(username);
		int picID = -1;
		int errorCode;
		if(user == null){
			errorCode = CODE_OK;
		}else{
			errorCode = CODE_ALREADY_IN_USE;
		}
		
		if(errorCode == CODE_OK){
			if(imagePart != null){
				String picUrl = processImage(imagePart);
				picID = lokaServer.getDBQuery().insertNewPicture(picUrl);
			}
			
			user = lokaServer.getDBQuery().getUser(userID);
			user.setUsername(username);
			user.setPicID(picID);
			user.setGroups_available(1);
			
			lokaServer.getDBQuery().updateUser(user);
		}
		
		sendResponse(evt.getResponse(), ""+errorCode);
	}

}

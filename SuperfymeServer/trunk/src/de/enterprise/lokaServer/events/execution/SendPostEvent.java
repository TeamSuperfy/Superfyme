package de.enterprise.lokaServer.events.execution;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import de.enterprise.lokaServer.db.DBQuery;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.DoubleLocPojo;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.LocationPojo;
import de.enterprise.lokaServer.pojos.PostMapPojo;
import de.enterprise.lokaServer.pojos.PostNewPojo;
import de.enterprise.lokaServer.tools.GeoTools;
import de.enterprise.lokaServer.tools.JSONConverter;

public class SendPostEvent extends ServerEvent {

	public SendPostEvent() {
		CMD = "SP";
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
		PostNewPojo post = (PostNewPojo) JSONConverter.fromJSON(json, PostNewPojo.class.getName());
		String picUrl = processImage(imagePart);
		int picID = lokaServer.getDBQuery().insertNewPicture(picUrl);
		lokaServer.getDBQuery().insertPost(post, picID);
		if(post.getGroupID() > 0){
			GroupPojo group = lokaServer.getDBQuery().getGroup(post.getGroupID());
			group.setPost_count(group.getPost_count() + 1);
			lokaServer.getDBQuery().updateGroup(group);
		}
		
		
		deleteOldestPostInRadius(post);
		sendResponse(evt.getResponse(), null);

	}

	private void deleteOldestPostInRadius(PostNewPojo post) {
		//build radius coords
		//
		LocationPojo[] coords = GeoTools.getRectOfCoord(post.getLocation(), DBQuery.POST_RECTANGLE_SIZE);
		
		//get posts in 20m radius
		//
		DoubleLocPojo doubleLoc = new DoubleLocPojo();
		doubleLoc.setUpLeft(coords[0]);
		doubleLoc.setBottomRight(coords[1]);
		ArrayList<PostMapPojo> posts = lokaServer.getDBQuery().getPosts(doubleLoc, post.getGroupID());
		
		//if posts.size() > 10
		//delete oldest of them
		//
		PostMapPojo oldest = null;
		long oldestDate = Long.MAX_VALUE;
		if(posts.size() > 10){
			for (PostMapPojo postMapPojo : posts) {
				if(postMapPojo.getLast_action() < oldestDate){
					oldest = postMapPojo;
					oldestDate = postMapPojo.getLast_action();
				}
			}
			if(oldest != null)
				lokaServer.getDBQuery().deletePost(oldest.getPostID());
		}
	}

}

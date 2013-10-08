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

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.CommentNewPojo;
import de.enterprise.lokaServer.pojos.NotificationPojo;
import de.enterprise.lokaServer.pojos.PostListPojo;
import de.enterprise.lokaServer.tools.JSONConverter;
import de.enterprise.lokaServer.tools.Rating;
import de.enterprise.lokaServer.tools.SimpleTools;

public class AddCommentEvent extends ServerEvent{
	
	public AddCommentEvent(){
		CMD = "AC";
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
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = "";
		HttpServletRequest req = (HttpServletRequest)evt.getResponse().getRequest();
		Part jsonPart = null;
		Part imagePart = null;
		if(ServletFileUpload.isMultipartContent((HttpServletRequest) req)){
			jsonPart = req.getPart("json");
			imagePart = req.getPart("file");
			json = new BufferedReader(new InputStreamReader(jsonPart.getInputStream(), "UTF-8")).readLine();
		}else{
			json = evt.getCmd()[1];
		}
		//get new comment
		CommentNewPojo cnp = (CommentNewPojo)JSONConverter.fromJSON(json, CommentNewPojo.class.getName());
		PostListPojo pp = lokaServer.getDBQuery().getSinglePost(cnp.getPostID());
		
		pp.setCommentCount(pp.getCommentCount() + 1);
		pp.setLast_action(System.currentTimeMillis());
		
		if(pp.getLastCommentUID() != cnp.getUserID()){
			Rating.calculateRatingIndex(pp, 0, 1);
		}
		
		pp.setLastCommentUID(cnp.getUserID());
		
		int picID = -1;
		
		if(imagePart != null){
			String picUrl = processImage(imagePart);
			picID = lokaServer.getDBQuery().insertNewPicture(picUrl);
		}
		
		lokaServer.getDBQuery().updatePost(pp);
		lokaServer.getDBQuery().insertNewComment(cnp, picID);
		
		if(cnp.getUserID() != pp.getUserID()){
			NotificationPojo np = new NotificationPojo();
			np.setUser_id(pp.getUserID());
			np.setExcerpt(SimpleTools.generateExcerpt(pp.getText()));
			np.setOn_post_id(pp.getPostID());
			np.setComment_id(lokaServer.getDBQuery().getLastAutoIncrementID());
			np.setExcerpt_comment(SimpleTools.generateExcerpt(cnp.getText()));
			np.setDate(System.currentTimeMillis());
			np.setRead(false);
			if(picID > 0){
				np.setPic_id(picID);
			}
			else{
				np.setPic_id(pp.getPicID());
			}
			
			lokaServer.getDBQuery().insertNotification(np);
			
			

			int numbNews = lokaServer.getDBQuery().getNumberOfNews(pp.getUserID());
			JSONObject response = new JSONObject();
			response.put("Count", numbNews);
			
			//GC2D
		}
		
		sendResponse(evt.getResponse(), null);

	}

}

package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.CommentPojo;
import de.enterprise.lokaServer.pojos.NotificationPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;
import de.enterprise.lokaServer.tools.SimpleTools;

public class RateCommentEvent extends ServerEvent {

	public RateCommentEvent() {
		CMD = "RAC";
	}

	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int commentID = obj.getInt("CID");
		int userID = obj.getInt("UID");
		boolean goodRated = obj.getBoolean("R");

		// load comment
		CommentPojo cp = lokaServer.getDBQuery().getSingleComment(commentID);
		
		// apply rating
		if (goodRated) {
			cp.setGoodRating(cp.getGoodRating() + 1);
			cp.setRatedGoodBy(CollectionStuff.appendToArray(cp.getRatedGoodBy(), new int[]{userID}));
		} else {
			cp.setBadRating(cp.getBadRating() + 1);
			cp.setRatedBadBy(CollectionStuff.appendToArray(cp.getRatedBadBy(), new int[]{userID}));
		}

		lokaServer.getDBQuery().updateComment(cp);
		
		if(userID != cp.getUserID()){
			NotificationPojo np = new NotificationPojo();
			np.setUser_id(cp.getUserID());
			np.setOn_post_id(cp.getPostID());
			np.setOn_comment_id(cp.getCommentID());
			np.setRating(goodRated?1:-1);
			np.setExcerpt(SimpleTools.generateExcerpt(cp.getText()));
			np.setDate(System.currentTimeMillis());
			np.setRead(false);
			if(cp.getPicID() > 0){
				np.setPic_id(cp.getPicID());
			}
	
			lokaServer.getDBQuery().insertNotification(np);
			

			int numbNews = lokaServer.getDBQuery().getNumberOfNews(cp.getUserID());
			JSONObject response = new JSONObject();
			response.put("Count", numbNews);
			
			//GC2D
			sendResponse(evt.getResponse(), null);
		}
	}

}

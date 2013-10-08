package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.NotificationPojo;
import de.enterprise.lokaServer.pojos.PostListPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;
import de.enterprise.lokaServer.tools.Rating;
import de.enterprise.lokaServer.tools.SimpleTools;

public class RatePostEvent extends ServerEvent{

	public RatePostEvent(){
		CMD = "RAP";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int postID = obj.getInt("PID");
		int userID = obj.getInt("UID");
		boolean goodRated = obj.getBoolean("R");
		
		//load post
		PostListPojo pp = lokaServer.getDBQuery().getSinglePost(postID);
		
		//apply rating
		if (goodRated) {
			pp.setGoodRating(pp.getGoodRating() + 1);
			pp.setRatedGoodBy(CollectionStuff.appendToArray(pp.getRatedGoodBy(), new int[]{userID}));
		} else {
			pp.setBadRating(pp.getBadRating() + 1);
			pp.setRatedBadBy(CollectionStuff.appendToArray(pp.getRatedBadBy(), new int[]{userID}));
		}
		
		lokaServer.getDBQuery().updatePost(pp);
		
		if(userID != pp.getUserID()){
			NotificationPojo np = new NotificationPojo();
			np.setUser_id(pp.getUserID());
			np.setOn_post_id(pp.getPostID());
			np.setRating(goodRated?1:-1);
			np.setExcerpt(SimpleTools.generateExcerpt(pp.getText()));
			np.setDate(System.currentTimeMillis());
			np.setRead(false);
			np.setPic_id(pp.getPicID());
			
			lokaServer.getDBQuery().insertNotification(np);
			

			int numbNews = lokaServer.getDBQuery().getNumberOfNews(pp.getUserID());
			JSONObject response = new JSONObject();
			response.put("Count", numbNews);
			
			//GC2D
			sendResponse(evt.getResponse(), null);
		}
	}

}

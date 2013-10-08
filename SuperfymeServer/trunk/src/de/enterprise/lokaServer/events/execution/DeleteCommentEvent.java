package de.enterprise.lokaServer.events.execution;

import java.io.File;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.CommentPojo;
import de.enterprise.lokaServer.pojos.PostListPojo;

public class DeleteCommentEvent extends ServerEvent {

	public DeleteCommentEvent(){
		CMD = "DC";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int commentID = obj.getInt("CID");
		
		CommentPojo cp = lokaServer.getDBQuery().getSingleComment(commentID);
		PostListPojo pp = lokaServer.getDBQuery().getSinglePost(cp.getPostID());
		pp.setCommentCount(pp.getCommentCount() - 1);
		
		lokaServer.getDBQuery().deleteComment(commentID);
		lokaServer.getDBQuery().updatePost(pp);
		
		if(cp.getPicID() > 0){
			String url = lokaServer.getDBQuery().getPicUrl(cp.getPicID());
			(new File(url)).delete();
			lokaServer.getDBQuery().deletePicture(cp.getPicID());	
		}
		sendResponse(evt.getResponse(), null);

	}

}

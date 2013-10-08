package de.enterprise.lokaServer.events.execution;

import java.util.ArrayList;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.CommentPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class RequestCommentsEvent extends ServerEvent{

	public RequestCommentsEvent(){
		CMD = "RC";
	}
	
	@Override
	protected void execute(CommandEvent evt) throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int postID = obj.getInt("PID");
		
		//load comments from DB
		ArrayList<CommentPojo> comments = lokaServer.getDBQuery().getCommentsFromPost(postID);
		
		//send comments to user
		sendResponse(evt.getResponse(), JSONConverter.toJSON(comments.toArray()));

	}

}

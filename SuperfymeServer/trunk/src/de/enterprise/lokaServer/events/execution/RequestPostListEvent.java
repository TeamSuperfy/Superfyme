package de.enterprise.lokaServer.events.execution;

import java.util.ArrayList;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.PostListPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class RequestPostListEvent extends ServerEvent{

	public RequestPostListEvent(){
		CMD = "RPL";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int groupID = obj.getInt("gID");
		ArrayList<PostListPojo> posts = this.lokaServer.getDBQuery().getPostsOfGroup(groupID);
		
		//send posts
		sendResponse(evt.getResponse(), JSONConverter.toJsonArray(posts.toArray()));
		
	}

}

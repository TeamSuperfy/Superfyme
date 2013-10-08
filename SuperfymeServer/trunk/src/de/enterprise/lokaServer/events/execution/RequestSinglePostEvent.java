package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.PostListPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class RequestSinglePostEvent extends ServerEvent{

	public RequestSinglePostEvent(){
		CMD = "RSP";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int postID = obj.getInt("PID");
		PostListPojo post = lokaServer.getDBQuery().getSinglePost(postID);
		
		sendResponse(evt.getResponse(), JSONConverter.toJSON(post));
	}

}

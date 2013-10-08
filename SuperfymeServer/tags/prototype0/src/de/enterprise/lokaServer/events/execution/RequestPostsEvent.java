package de.enterprise.lokaServer.events.execution;

import java.util.ArrayList;

import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.PostPojo;
import de.enterprise.lokaServer.pojos.RequestPostContentsPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class RequestPostsEvent extends ServerEvent{

	public RequestPostsEvent(){
		CMD = "RP";
	}
	
	public void execute(String json){
		RequestPostContentsPojo contents = (RequestPostContentsPojo) JSONConverter.fromJSON(json, RequestPostContentsPojo.class.getName());
		ArrayList<PostPojo> postsInView = this.lokaServer.getDBQuery().getPostPojos(contents.getDblLoc());
		int userID = contents.getUserID();
		lokaServer.getServerSocket().sendToUser(userID, "RP", JSONConverter.toJsonArray(postsInView.toArray()));
	}

	@Override
	protected void execute(String json, String extra) {
	}
	
}

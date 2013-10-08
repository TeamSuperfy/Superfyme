package de.enterprise.lokaServer.events.execution;

import java.util.ArrayList;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class RequestMessagesEvent extends ServerEvent {

	public RequestMessagesEvent(){
		CMD = "RM";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt)  throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int userID = obj.getInt("UID");
		int otherUID = obj.getInt("OUID");
		int postID = obj.getInt("PID");
		int commentID = obj.getInt("CID");
		
		ArrayList<MessagePojo> myMessages = lokaServer.getDBQuery().getMessagesWithUsers(userID, otherUID, postID, commentID);
		sendResponse(cmdEvt.getResponse(), JSONConverter.toJsonArray(myMessages.toArray()));
	}

}

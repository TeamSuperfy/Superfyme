package de.enterprise.lokaServer.events.execution;

import java.util.ArrayList;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.NotificationPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class RequestNotificationsEvent extends ServerEvent{

	public RequestNotificationsEvent(){
		CMD = "RN";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int userID = obj.getInt("UID");
		ArrayList<NotificationPojo> notifications = lokaServer.getDBQuery().getNotificationsForUser(userID);
		
		sendResponse(evt.getResponse(), JSONConverter.toJSON(notifications.toArray()));
	}

}

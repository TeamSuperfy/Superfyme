package de.enterprise.lokaServer.events.execution;

import java.util.ArrayList;

import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class RequestUserPojosEvent extends ServerEvent{
	
	public RequestUserPojosEvent(){
		CMD = "RUP";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		Integer[] userIDs = (Integer[]) JSONConverter.fromJSONArray(json, Integer[].class.getName());
		
		ArrayList<UserPojo> users = lokaServer.getDBQuery().getUsers(userIDs);
		
		sendResponse(evt.getResponse(), JSONConverter.toJsonArray(users.toArray()));
	}

}

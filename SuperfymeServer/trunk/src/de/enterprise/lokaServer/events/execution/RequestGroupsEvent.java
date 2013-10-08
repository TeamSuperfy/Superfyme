package de.enterprise.lokaServer.events.execution;

import java.util.ArrayList;

import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class RequestGroupsEvent extends ServerEvent {

	public RequestGroupsEvent(){
		CMD = "RGR";
	}
	
	@Override
	protected void execute(CommandEvent evt) throws Exception {
		String json = evt.getCmd()[1];
		Integer[] groupIDs = (Integer[]) JSONConverter.fromJSONArray(json, Integer[].class.getName());
		ArrayList<GroupPojo> groups = lokaServer.getDBQuery().getGroups(groupIDs);
		
		sendResponse(evt.getResponse(), JSONConverter.toJSON(groups.toArray()));
	}

}

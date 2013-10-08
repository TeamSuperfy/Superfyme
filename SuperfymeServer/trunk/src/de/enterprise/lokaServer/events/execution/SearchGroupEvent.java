package de.enterprise.lokaServer.events.execution;

import java.util.ArrayList;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class SearchGroupEvent extends ServerEvent{

	public SearchGroupEvent(){
		CMD = "SGR";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt)  throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		String name = obj.getString("name");
		String place = obj.getString("place");
		boolean isPublic = obj.getBoolean("public");
		
		ArrayList<GroupPojo> groups = lokaServer.getDBQuery().searchGroups(name, place, isPublic);
		sendResponse(cmdEvt.getResponse(), JSONConverter.toJsonArray(groups.toArray()));
	}

}

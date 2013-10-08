package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;

public class CheckGroupNameAvailableEvent extends ServerEvent{

	public CheckGroupNameAvailableEvent(){
		CMD = "CGN";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt) throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		String groupName = obj.getString("name");
		String groupPlace = obj.getString("place");
		
		boolean available = lokaServer.getDBQuery().checkGroupAvailable(groupName, groupPlace);
		JSONObject resp = new JSONObject();
		resp.put("valid", available?1:0);
		
		sendResponse(cmdEvt.getResponse(), resp.toString());
	}

}

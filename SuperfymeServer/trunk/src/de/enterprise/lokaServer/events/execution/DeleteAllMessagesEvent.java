package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;

public class DeleteAllMessagesEvent extends ServerEvent {

	public DeleteAllMessagesEvent(){
		CMD = "DAM";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt)  throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int userID = obj.getInt("UID");
		
		lokaServer.getDBQuery().deleteAllMessagesForUser(userID);
		sendResponse(cmdEvt.getResponse(), null);
	}

}

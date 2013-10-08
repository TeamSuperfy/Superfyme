package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;

public class ReadMessageEvent extends ServerEvent {

	public ReadMessageEvent(){
		CMD = "RME";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int messID = obj.getInt("MID");
		
		lokaServer.getDBQuery().setMessageRead(messID);
		sendResponse(evt.getResponse(), null);
	}

}

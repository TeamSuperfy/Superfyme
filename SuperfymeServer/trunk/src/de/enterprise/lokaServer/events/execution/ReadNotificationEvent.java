package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;

public class ReadNotificationEvent extends ServerEvent {

	public ReadNotificationEvent(){
		CMD = "RNO";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int notifID = obj.getInt("NID");
		
		lokaServer.getDBQuery().setNotificationRead(notifID);
		sendResponse(evt.getResponse(), null);
	}

}

package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;

public class SendFeedbackEvent extends ServerEvent {

	public SendFeedbackEvent(){
		CMD = "SF";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt) throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int userID = obj.getInt("UID");
		String feedback = obj.getString("F");
		String time = obj.getString("T");
		String android = obj.getString("A");
		String version = obj.getString("V");
		String phone = obj.getString("P");
		
		lokaServer.getDBQuery().insertFeedback(userID, feedback, time, android, version, phone);
		sendResponse(cmdEvt.getResponse(), null);
	}

}

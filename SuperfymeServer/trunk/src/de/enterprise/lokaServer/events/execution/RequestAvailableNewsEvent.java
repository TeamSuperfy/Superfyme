package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;

public class RequestAvailableNewsEvent extends ServerEvent {

	public RequestAvailableNewsEvent(){
		CMD = "RAN";
	}
	
	@Override
	protected void execute(CommandEvent evt) throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int userID = obj.getInt("UID");
		int numbNews = lokaServer.getDBQuery().getNumberOfNews(userID);
		JSONObject response = new JSONObject();
		response.put("Count", numbNews);
		
		sendResponse(evt.getResponse(), response.toString());
	}

}

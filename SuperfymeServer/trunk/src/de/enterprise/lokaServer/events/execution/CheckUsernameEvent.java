package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;

public class CheckUsernameEvent extends ServerEvent{

	public CheckUsernameEvent(){
		CMD = "CUN";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt) throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		String username = obj.getString("name");
		
		boolean existent = lokaServer.getDBQuery().checkUsernameExistent(username);
		JSONObject resp = new JSONObject();
		resp.put("existent", existent?1:0);
		
		sendResponse(cmdEvt.getResponse(), resp.toString());
	}

}

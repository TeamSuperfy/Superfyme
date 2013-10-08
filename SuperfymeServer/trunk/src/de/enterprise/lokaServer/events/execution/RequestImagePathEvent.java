package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;

public class RequestImagePathEvent extends ServerEvent{

	public RequestImagePathEvent(){
		CMD = "RIP";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt) throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int picID = obj.getInt("PicID");
		
		String imagePath = lokaServer.getDBQuery().getPicUrl(picID);
		JSONObject resp = new JSONObject();
		resp.put("path", imagePath);
		sendResponse(cmdEvt.getResponse(), resp.toString());
	}

	
}

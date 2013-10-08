package de.enterprise.lokaServer.events.execution;

import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class SendMessageEvent extends ServerEvent {

	public SendMessageEvent(){
		CMD = "SM";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		MessagePojo mp = (MessagePojo) JSONConverter.fromJSON(json, MessagePojo.class.getName());
		mp.setDate(System.currentTimeMillis());
		int messID = lokaServer.getDBQuery().insertNewMessage(mp);
		mp.setId(messID);
		sendResponse(evt.getResponse(), null);
		
		//GC2D
		
//		int numbNews = lokaServer.getDBQuery().getNumberOfNews(mp.getTo());
//		JSONObject response = new JSONObject();
//		response.put("Count", numbNews);
		//GC2D
	}

}

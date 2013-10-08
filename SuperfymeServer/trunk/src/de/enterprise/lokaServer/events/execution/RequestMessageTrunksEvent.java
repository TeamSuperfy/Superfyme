package de.enterprise.lokaServer.events.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class RequestMessageTrunksEvent extends ServerEvent{

	public RequestMessageTrunksEvent(){
		CMD = "RMT";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt)  throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int userID = obj.getInt("UID");
		
		Map<Integer, MessagePojo> map = new HashMap<Integer, MessagePojo>();
		ArrayList<MessagePojo> myMessages = lokaServer.getDBQuery().getMessagesWithUser(userID);
		for (MessagePojo messagePojo : myMessages) {
			int other;
			if(messagePojo.getTo() != userID)
				other = messagePojo.getTo();
			else
				other = messagePojo.getFrom();
			int uniqueKey = Integer.parseInt(other + "" + messagePojo.getPostID() + "" + messagePojo.getCommentID());
			if(map.containsKey(uniqueKey)){
				MessagePojo mess = map.get(uniqueKey);
				if(mess.getDate() < messagePojo.getDate()){
					map.put(uniqueKey, messagePojo);
				}
			}else{
				map.put(uniqueKey, messagePojo);
			}
		}
		
		Collection<MessagePojo> trunks = map.values();
		sendResponse(cmdEvt.getResponse(), JSONConverter.toJsonArray(trunks.toArray()));
	}

}

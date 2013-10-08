package de.enterprise.lokaServer.events.execution;

import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.tools.JSONConverter;

public class DeleteMessagesEvent extends ServerEvent {

	public DeleteMessagesEvent(){
		CMD = "DME";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt)  throws Exception {
		String json = cmdEvt.getCmd()[1];
		Integer[] messIDs = (Integer[]) JSONConverter.fromJSONArray(json, Integer[].class.getName());
		lokaServer.getDBQuery().deleteMessages(messIDs);
		sendResponse(cmdEvt.getResponse(), null);
	}

}

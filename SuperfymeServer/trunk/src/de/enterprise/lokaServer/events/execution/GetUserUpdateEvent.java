package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class GetUserUpdateEvent extends ServerEvent {

	public GetUserUpdateEvent(){
		CMD = "GUU";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt) throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		
		int userID = obj.getInt("UID");
		
		UserPojo user = lokaServer.getDBQuery().getUser(userID);
		
		if(user == null){
			userID = lokaServer.getDBQuery().insertNewUser();
			user = new UserPojo();
			user.setId(userID);
		}
		
		if(user.isBanned()){
			if(System.currentTimeMillis() > user.getBannedUntil()){
				user.setBanned(false);
				user.setBannedUntil(0);
				
				MessagePojo message = new MessagePojo();
				message.setDate(System.currentTimeMillis());
				message.setFrom(-1);
				message.setPostID(-1);
				message.setText("Your ban-time is now over. I hope that you still enjoy SuperfyMe and respect each other.");
				message.setTo(userID);
				
				lokaServer.getDBQuery().insertNewMessage(message);
				
				lokaServer.getDBQuery().updateUser(user);
			}
		}
		
		sendResponse(cmdEvt.getResponse(), JSONConverter.toJSON(user));
	}

}

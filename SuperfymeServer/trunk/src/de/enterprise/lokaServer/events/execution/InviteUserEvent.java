package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;

public class InviteUserEvent extends ServerEvent {

	private static final int CODE_NOT_FOUND = -1, CODE_ALREADY_MEMBER = -2, CODE_ALREADY_INVITED = -3;
	
	public InviteUserEvent(){
		CMD = "IU";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt)  throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int groupID = obj.getInt("GID");
		String username = obj.getString("UN");
		
		UserPojo user = lokaServer.getDBQuery().getUser(username);
		GroupPojo group = lokaServer.getDBQuery().getGroup(groupID);
		int errorCode;
		if(user != null){
			if(CollectionStuff.arrayContains(user.getGroups(), groupID)){
				errorCode = CODE_ALREADY_MEMBER;
			}else if(CollectionStuff.arrayContains(group.getInvited_users(), user.getId())){
				errorCode = CODE_ALREADY_INVITED;
			}else{
				errorCode = user.getId();
			}
		}else{
			errorCode = CODE_NOT_FOUND;
		}
		if(errorCode > 0){
			group.setInvited_users(CollectionStuff.appendToArray(group.getInvited_users(), new int[]{user.getId()}));
			lokaServer.getDBQuery().updateGroup(group);
			
			MessagePojo msg = new MessagePojo();
			msg.setFrom(-1);
			msg.setTo(user.getId());
			msg.setText("You've got a group invitation!");
			msg.setInfo(""+groupID);
			msg.setRead(false);
			msg.setDate(System.currentTimeMillis());
			lokaServer.getDBQuery().insertNewMessage(msg);
		}

		sendResponse(cmdEvt.getResponse(), ""+errorCode);
	}

}

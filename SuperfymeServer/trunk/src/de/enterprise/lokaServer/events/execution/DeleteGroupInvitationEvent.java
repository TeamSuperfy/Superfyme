package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;

public class DeleteGroupInvitationEvent extends ServerEvent {

	private static final int CODE_OK = 0, CODE_NOT_FOUND = -1, CODE_MEMBER_NOT_INVITED = -2;
	
	public DeleteGroupInvitationEvent(){
		CMD = "DGI";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt)  throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int groupID = obj.getInt("GID");
		int userID = obj.getInt("UID");
		
		UserPojo user = lokaServer.getDBQuery().getUser(userID);
		GroupPojo group = lokaServer.getDBQuery().getGroup(groupID);
		int errorCode;
		if(user != null){
			if(!CollectionStuff.arrayContains(group.getInvited_users(), userID)){
				errorCode = CODE_MEMBER_NOT_INVITED;
			}else{
				errorCode = CODE_OK;
			}
		}else{
			errorCode = CODE_NOT_FOUND;
		}
		
		if(errorCode == CODE_OK){
			group.setInvited_users(CollectionStuff.integerToIntArray(
					CollectionStuff.removeIntFromArray(group.getInvited_users(), userID)));
			lokaServer.getDBQuery().updateGroup(group);
		}

		sendResponse(cmdEvt.getResponse(), ""+errorCode);
	}

}

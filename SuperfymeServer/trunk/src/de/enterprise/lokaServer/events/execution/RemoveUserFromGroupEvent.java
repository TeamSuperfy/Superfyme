package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;

public class RemoveUserFromGroupEvent extends ServerEvent{

	public RemoveUserFromGroupEvent(){
		CMD = "RUFG";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt)  throws Exception {
		String msg = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(msg);
		
		int userID = obj.getInt("UID");
		int groupID = obj.getInt("GID");
		
		GroupPojo group = lokaServer.getDBQuery().getGroup(groupID);
		group.setMembers(CollectionStuff.integerToIntArray(CollectionStuff.removeIntFromArray(group.getMembers(), userID)));
		lokaServer.getDBQuery().updateGroup(group);
		
		UserPojo user = lokaServer.getDBQuery().getUser(userID);
		if(user.getGroups() != null){
			user.setGroups(CollectionStuff.integerToIntArray(CollectionStuff.removeIntFromArray(user.getGroups(), groupID)));
			lokaServer.getDBQuery().updateUser(user);
		}
		
		sendResponse(cmdEvt.getResponse(), null);
	}

}

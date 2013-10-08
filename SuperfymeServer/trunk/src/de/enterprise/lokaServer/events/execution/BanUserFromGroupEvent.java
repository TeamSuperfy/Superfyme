package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;

public class BanUserFromGroupEvent extends ServerEvent {

	public BanUserFromGroupEvent(){
		CMD = "BUG";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt)  throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int groupID = obj.getInt("GID");
		int userID = obj.getInt("UID");
		
		GroupPojo group = lokaServer.getDBQuery().getGroup(groupID);
		UserPojo user = lokaServer.getDBQuery().getUser(userID);
		
		user.setGroups(CollectionStuff.integerToIntArray(CollectionStuff.removeIntFromArray(user.getGroups(), groupID)));
		
		group.setMembers(CollectionStuff.integerToIntArray(CollectionStuff.removeIntFromArray(group.getMembers(), userID)));
		group.setBanned_users(CollectionStuff.appendToArray(group.getBanned_users(), new int[]{userID}));
		group.setMember_count(group.getMember_count() - 1);
		
		lokaServer.getDBQuery().updateGroup(group);
		lokaServer.getDBQuery().updateUser(user);
		
		//GC2D
		sendResponse(cmdEvt.getResponse(), null);
	}

}

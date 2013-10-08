package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;

public class LeaveGroupEvent extends ServerEvent {

	public LeaveGroupEvent(){
		CMD = "LG";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt)  throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int userID = obj.getInt("UID");
		int groupID = obj.getInt("GID");
		
		UserPojo user = lokaServer.getDBQuery().getUser(userID);
		GroupPojo gp = lokaServer.getDBQuery().getGroup(groupID);
		
		int[] groups = user.getGroups();
		user.setGroups(CollectionStuff.integerToIntArray(CollectionStuff.removeIntFromArray(groups, groupID)));
		
		int[] users = gp.getMembers();
		gp.setMembers(CollectionStuff.integerToIntArray(CollectionStuff.removeIntFromArray(users, userID)));
		
		gp.setMember_count(gp.getMember_count() - 1);
		
		lokaServer.getDBQuery().updateGroup(gp);
		lokaServer.getDBQuery().updateUser(user);
		sendResponse(cmdEvt.getResponse(), null);
	}

}

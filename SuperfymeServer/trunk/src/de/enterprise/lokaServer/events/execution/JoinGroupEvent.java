package de.enterprise.lokaServer.events.execution;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;

public class JoinGroupEvent extends ServerEvent {

	public JoinGroupEvent(){
		CMD = "JG";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt)  throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int userID = obj.getInt("UID");
		int groupID = obj.getInt("gID");
		
		UserPojo user = lokaServer.getDBQuery().getUser(userID);
		GroupPojo group = lokaServer.getDBQuery().getGroup(groupID);
		
		int[] userGroups = user.getGroups();
		boolean notInGroup = true;
		if(userGroups == null){
			userGroups = new int[]{groupID};
		}else{
			if(!CollectionStuff.arrayContains(userGroups, groupID)){
				userGroups = CollectionStuff.appendToArray(userGroups, new int[]{groupID});
			}else{
				notInGroup = false;
			}
		}
		
		if(notInGroup){
			user.setGroups(userGroups);
			
			lokaServer.getDBQuery().updateUser(user);
			
			int[] groupUsers = group.getMembers();
			groupUsers = CollectionStuff.appendToArray(groupUsers, new int[]{userID});
			group.setMembers(groupUsers);
			group.setMember_count(group.getMember_count() + 1);
			
			if(CollectionStuff.arrayContains(group.getInvited_users(), userID)){
				group.setInvited_users(CollectionStuff.integerToIntArray(
						CollectionStuff.removeIntFromArray(group.getInvited_users(), userID)));
			}
			
			lokaServer.getDBQuery().updateGroup(group);
		}
		

		sendResponse(cmdEvt.getResponse(), null);
	}

}

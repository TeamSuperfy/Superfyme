package de.enterprise.lokaServer.events.execution;

import java.io.File;
import java.util.ArrayList;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;

public class DeleteGroupEvent extends ServerEvent{

	public DeleteGroupEvent(){
		CMD = "DG";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt)  throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int groupID = obj.getInt("GID");
		GroupPojo gp = lokaServer.getDBQuery().getGroup(groupID);
		int ownerID = gp.getCreator_id();
		
		ArrayList<UserPojo> users = lokaServer.getDBQuery().getUsers(CollectionStuff.intToIntegerArray(gp.getMembers()));
		for (UserPojo userPojo : users) {
			if(userPojo.getId() == ownerID){
				userPojo.setGroups_available(1);
			}
			int[] groups = userPojo.getGroups();
			userPojo.setGroups(CollectionStuff.integerToIntArray(CollectionStuff.removeIntFromArray(groups, groupID)));
			lokaServer.getDBQuery().updateUser(userPojo);
		}
		
		String url = lokaServer.getDBQuery().getPicUrl(gp.getPic_id());
		(new File(url)).delete();
		lokaServer.getDBQuery().deletePicture(gp.getPic_id());
		
		lokaServer.getDBQuery().deleteGroup(groupID);
		sendResponse(cmdEvt.getResponse(), null);
	}

}

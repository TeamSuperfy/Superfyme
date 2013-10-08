package de.enterprise.lokaServer.events.execution;

import java.io.File;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.PostListPojo;

public class DeletePostEvent extends ServerEvent {

	public DeletePostEvent(){
		CMD = "DP";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int postID = obj.getInt("PID");
		int groupID;
		
		PostListPojo pp = lokaServer.getDBQuery().getSinglePost(postID);
		groupID = pp.getGroup_id();
		lokaServer.getDBQuery().deletePost(postID);
		
		String url = lokaServer.getDBQuery().getPicUrl(pp.getPicID());
		(new File(url)).delete();
		lokaServer.getDBQuery().deletePicture(pp.getPicID());
		
		if(groupID > 0){
			GroupPojo group = lokaServer.getDBQuery().getGroup(groupID);
			group.setPost_count(group.getPost_count() - 1);
			lokaServer.getDBQuery().updateGroup(group);
		}
		sendResponse(evt.getResponse(), null);
	}

}

package de.enterprise.lokaServer.events.execution;

import java.util.ArrayList;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.DoubleLocPojo;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class RequestGroupsMapEvent extends ServerEvent{
	
	public RequestGroupsMapEvent(){
		CMD = "RGM";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		String dbl = null;
		if(obj.has("L")){
			dbl = obj.getString("L");
		}
		
		DoubleLocPojo dblLoc = null;
		if(dbl != null){
			dblLoc = (DoubleLocPojo)JSONConverter.fromJSON(dbl, DoubleLocPojo.class.getName());
		}
		
		ArrayList<GroupPojo> groupsInView = this.lokaServer.getDBQuery().getGroups(dblLoc);
		
		sendResponse(evt.getResponse(), JSONConverter.toJsonArray(groupsInView.toArray()));
	}

}

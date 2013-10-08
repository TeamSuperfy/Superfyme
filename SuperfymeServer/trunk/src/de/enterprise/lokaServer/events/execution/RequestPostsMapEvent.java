package de.enterprise.lokaServer.events.execution;

import java.util.ArrayList;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.DoubleLocPojo;
import de.enterprise.lokaServer.pojos.LocationPojo;
import de.enterprise.lokaServer.pojos.PostMapPojo;
import de.enterprise.lokaServer.tools.GeoTools;
import de.enterprise.lokaServer.tools.JSONConverter;

public class RequestPostsMapEvent extends ServerEvent{

	public static final int REQUEST_POSTS_RADIUS = 8000;
	
	public RequestPostsMapEvent(){
		CMD = "RPM";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		String dbl = null;
		if(obj.has("L")){
			dbl = obj.getString("L");
		}
		String pos = null;
		if(obj.has("P")){
			pos = obj.getString("P");
		}
		
		int groupID = obj.getInt("gID");
		String searchWord = "";
		if(obj.containsKey("Que")){
			searchWord = obj.getString("Que");
		}
		
		DoubleLocPojo dblLoc = null;
		LocationPojo loc = null;
		if(dbl != null){
			dblLoc = (DoubleLocPojo)JSONConverter.fromJSON(dbl, DoubleLocPojo.class.getName());
		}else if(pos != null){
			loc = (LocationPojo)JSONConverter.fromJSON(pos, LocationPojo.class.getName());
		}
		
		if(loc != null){
			LocationPojo[] locs = GeoTools.getRectOfCoord(loc, REQUEST_POSTS_RADIUS);
			dblLoc = new DoubleLocPojo(locs[0], locs[1]);
		}
		
		ArrayList<PostMapPojo> postsInView = this.lokaServer.getDBQuery().getPosts(dblLoc, groupID, searchWord);
		
		sendResponse(evt.getResponse(), JSONConverter.toJsonArray(postsInView.toArray()));
	}

}

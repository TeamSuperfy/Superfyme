package de.enterprise.lokaServer.events.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONArray;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.CommentPojo;
import de.enterprise.lokaServer.tools.JSONConverter;
import de.enterprise.lokaServer.tools.SimpleTools;

public class RequestLatestCommentsEvent extends ServerEvent{

	public RequestLatestCommentsEvent(){
		CMD = "RLC";
	}
	
	@Override
	protected void execute(CommandEvent cmdEvt)  throws Exception {
		String json = cmdEvt.getCmd()[1];
		JSONArray arr = JSONArray.fromObject(json);
		
		List<CommentPojo> comments = new ArrayList<CommentPojo>();

		
		for (int i = 0; i < arr.size(); i++) {
			int postID = arr.getInt(i);
			List<CommentPojo> postComments = new ArrayList<CommentPojo>();
			postComments = lokaServer.getDBQuery().getCommentsFromPost(postID);
			if(postComments.size() > 0){
				Collections.sort(postComments, new SimpleTools.CommentComparator());
				comments.add(postComments.get(0));
			}
		}
			
		
		sendResponse(cmdEvt.getResponse(), JSONConverter.toJsonArray(comments.toArray()));
	}

}

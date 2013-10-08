package de.enterprise.lokaServer.events.execution;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;
import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.CommentPojo;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.pojos.PostListPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;

public class ReportUserEvent extends ServerEvent{

	public static final int MAX_REPORTS_WITHOUT_BAN = 10;
	
	public ReportUserEvent(){
		CMD = "RU";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		JSONObject obj = JSONObject.fromObject(json);
		int userID = obj.getInt("UID");
		int reportedUserID = obj.getInt("RUID");
		int postID = obj.getInt("PID");
		int commentID = obj.getInt("CID");

		UserPojo user = lokaServer.getDBQuery().getUser(userID);
		UserPojo reportedUser = lokaServer.getDBQuery().getUser(reportedUserID);
		
		if(reportedUser.getReportedCount() + 1 >= MAX_REPORTS_WITHOUT_BAN){
			long bannedUntil = System.currentTimeMillis() + 1000 * 60  * 60 * 24 * 2;
			reportedUser.setReportedCount(0);
			reportedUser.setBannedCount(reportedUser.getBannedCount() + 1);
			reportedUser.setBanned(true);
			reportedUser.setBannedUntil(bannedUntil);
			
			MessagePojo message = new MessagePojo();
			message.setDate(System.currentTimeMillis());
			message.setFrom(-1);
			message.setPostID(postID);
			message.setText("Dear user, you are now banned until " + new Date(bannedUntil) +
					", because you was reported more than 10 times for offensive images or text.");
			message.setTo(reportedUserID);
			
			lokaServer.getDBQuery().insertNewMessage(message);
			
		}else{
			reportedUser.setReportedCount(reportedUser.getReportedCount() + 1);
		}
		
		if(postID > 0){
			reportedUser.setReportedPosts(CollectionStuff.appendToArray(reportedUser.getReportedPosts(), new int[]{postID}));
			user.setReportedOtherPosts(CollectionStuff.appendToArray(user.getReportedOtherPosts(), new int[]{postID}));
		}
		else{
			reportedUser.setReportedComments(CollectionStuff.appendToArray(reportedUser.getReportedComments(), new int[]{commentID}));
			user.setReportedOtherComments(CollectionStuff.appendToArray(user.getReportedOtherComments(), new int[]{commentID}));
		}
		
		Map<Integer, Integer> reportedPostsMap = new HashMap<Integer, Integer>();
		List<Integer> postsToDelete = new ArrayList<Integer>();
		Map<Integer, Integer> reportedCommentsMap = new HashMap<Integer, Integer>();
		List<Integer> commentsToDelete = new ArrayList<Integer>();
		int[] reportedPosts = reportedUser.getReportedPosts();
		if(reportedPosts != null){
			for (int i : reportedPosts) {
				if(reportedPostsMap.containsKey(i)){
					reportedPostsMap.put(i, reportedPostsMap.get(i) + 1);
				}else{
					reportedPostsMap.put(i, 1);
				}
			}
			for (Entry<Integer, Integer> e : reportedPostsMap.entrySet()) {
				if(e.getValue() >= 10){
					postsToDelete.add(e.getKey());
				}
			}
		}
		
		int[] reportedComments = reportedUser.getReportedComments();
		if(reportedComments != null){
			for (int i : reportedComments) {
				if(reportedCommentsMap.containsKey(i)){
					reportedCommentsMap.put(i, reportedCommentsMap.get(i) + 1);
				}else{
					reportedCommentsMap.put(i, 1);
				}
			}
			for (Entry<Integer, Integer> e : reportedCommentsMap.entrySet()) {
				if(e.getValue() >= 10){
					commentsToDelete.add(e.getKey());
				}
			}
		}
		
		for (int i : postsToDelete) {
			lokaServer.getDBQuery().deletePost(i);
		}
		for (int i : commentsToDelete) {
			CommentPojo cp = lokaServer.getDBQuery().getSingleComment(i);
			PostListPojo pp = lokaServer.getDBQuery().getSinglePost(cp.getPostID());
			pp.setCommentCount(pp.getCommentCount() - 1);
			lokaServer.getDBQuery().updatePost(pp);
			lokaServer.getDBQuery().deleteComment(i);
		}
		
		lokaServer.getDBQuery().updateUser(user);
		lokaServer.getDBQuery().updateUser(reportedUser);
		sendResponse(evt.getResponse(), null);
	}

}

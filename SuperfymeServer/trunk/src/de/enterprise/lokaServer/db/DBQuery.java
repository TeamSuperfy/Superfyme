package de.enterprise.lokaServer.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import de.enterprise.lokaServer.db.SQLInfo.CommentSQL;
import de.enterprise.lokaServer.db.SQLInfo.FeedbackSQL;
import de.enterprise.lokaServer.db.SQLInfo.GroupSQL;
import de.enterprise.lokaServer.db.SQLInfo.MessageSQL;
import de.enterprise.lokaServer.db.SQLInfo.NotificationSQL;
import de.enterprise.lokaServer.db.SQLInfo.PictureSQL;
import de.enterprise.lokaServer.db.SQLInfo.PostSQL;
import de.enterprise.lokaServer.db.SQLInfo.UserSQL;
import de.enterprise.lokaServer.pojos.CommentNewPojo;
import de.enterprise.lokaServer.pojos.CommentPojo;
import de.enterprise.lokaServer.pojos.DoubleLocPojo;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.LocationPojo;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.pojos.NotificationPojo;
import de.enterprise.lokaServer.pojos.PostListPojo;
import de.enterprise.lokaServer.pojos.PostMapPojo;
import de.enterprise.lokaServer.pojos.PostNewPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;
import de.enterprise.lokaServer.tools.JSONConverter;

public class DBQuery {
	public static final int POST_RECTANGLE_SIZE = 100;
	
	private DBConnection dbConnection;
	

	public DBQuery() {
		dbConnection = new DBConnection();
	}

	public void close() {
		//nothing?
	}

	public int insertNewPicture(String picUrl){
		executeSQL("INSERT INTO "+PictureSQL.TABLE_NAME+
				" ("+PictureSQL.FIELD_ID+", "+PictureSQL.FIELD_PIC_URL+")" +
						" VALUES" +
						" (DEFAULT, '"+picUrl+"')");
		return getLastAutoIncrementID();
	}
	
	public void insertPost(PostNewPojo post, int picID) {
		LocationPojo loc = post.getLocation();
		String sql = "INSERT INTO " + PostSQL.TABLE_NAME + " ("
				
				+ PostSQL.FIELD_ID + ", "
				+ PostSQL.FIELD_LAT + ", "
				+ PostSQL.FIELD_LON + ", "
				+ PostSQL.FIELD_USER_ID + ", "
				+ PostSQL.FIELD_DATE + ", "
				+ PostSQL.FIELD_PIC_ID + ", "
				+ PostSQL.FIELD_TEXT + ", "
				+ PostSQL.FIELD_GOODRATING + ", "
				+ PostSQL.FIELD_BADRATING + ", "
				+ PostSQL.FIELD_OTHER_RATING_INDEX + ", "
				+ PostSQL.FIELD_COMMENT_COUNT + ", "
				+ PostSQL.FIELD_GROUP_ID + ", "
				+ PostSQL.FIELD_LAST_ACTION + ", "
				+ PostSQL.FIELD_ATTENTION_POINTS + ", "
				+ PostSQL.FIELD_TEARING_DOWN + ", "
				+ PostSQL.FIELD_CURRENT_ATTENTION_POINTS + ", "
				+ PostSQL.FIELD_IS_ANONYMOUS + ", "
				+ PostSQL.FIELD_CATEGORY + ", "
				+ PostSQL.FIELD_LAST_COMMENT_UID + ")"
				
				+ " VALUES (" 
				
				+ "DEFAULT, '"
				+ loc.getLatitude() + "', '"
				+ loc.getLongitude() + "', '" 
				+ post.getUserID() + "', '" 
				+ post.getDate() + "', '" 
				+ picID + "', '"
				+ post.getText() + "', " 
				+ "DEFAULT" + ", " 
				+ "DEFAULT" + ", " 
				+ "DEFAULT" + ", " 
				+ 0 + ", " 
				+ post.getGroupID() + ", " 
				+ System.currentTimeMillis() + ", " 
				+ "DEFAULT" + ", " 
				+ false + ", " 
				+ "DEFAULT" + ", " 
				+ String.valueOf(post.isAnonymous()?1:0) + ", "
				+ post.getCategory() + ", "
				+ "DEFAULT" + ")";

		executeSQL(sql);
	}
	
	public void updatePost(PostListPojo post){
		String sql = "UPDATE "+ PostSQL.TABLE_NAME + " "
				
				+"SET "
				
				+ PostSQL.FIELD_GOODRATING + " = '" + post.getGoodRating() + "', "
				+ PostSQL.FIELD_BADRATING + " = '" + post.getBadRating() + "', "
				+ PostSQL.FIELD_OTHER_RATING_INDEX + " = '" + post.getOtherRating() + "', "
				+ PostSQL.FIELD_COMMENT_COUNT + " = '" + post.getCommentCount() + "', "
				+ PostSQL.FIELD_LAST_ACTION + " = '" + post.getLast_action() + "', "
				+ PostSQL.FIELD_RATED_BAD_BY + " = '" + JSONConverter.toJSON(post.getRatedBadBy()) + "', "
				+ PostSQL.FIELD_RATED_GOOD_BY + " = '" + JSONConverter.toJSON(post.getRatedGoodBy()) + "', "
				+ PostSQL.FIELD_ATTENTION_POINTS + " = '" + post.getAttentionPoints() + "', "
				+ PostSQL.FIELD_CURRENT_ATTENTION_POINTS + " = '" + post.getCurrent_attentionPoints() + "', "
				+ PostSQL.FIELD_TEARING_DOWN + " = '" + String.valueOf(post.isTearingDown()?1:0) + "', "
				+ PostSQL.FIELD_LAST_COMMENT_UID + " = '" + post.getLastCommentUID() + "'"
				
				+ " WHERE " + PostSQL.FIELD_ID + " = '" + post.getPostID() + "'";
		
		executeSQL(sql);
	}
	
	public void updateComment(CommentPojo comment){
		String sql = "UPDATE "+ CommentSQL.TABLE_NAME + " "
				
				+"SET "

				+ CommentSQL.FIELD_GOODRATING + " = '" + comment.getGoodRating() + "', "
				+ CommentSQL.FIELD_BADRATING + " = '" + comment.getBadRating() + "', "
				+ CommentSQL.FIELD_RATED_BAD_BY + " = '" + JSONConverter.toJSON(comment.getRatedBadBy()) + "', "
				+ CommentSQL.FIELD_RATED_GOOD_BY + " = '" + JSONConverter.toJSON(comment.getRatedGoodBy()) + "'"
				
				+ " WHERE " + CommentSQL.FIELD_ID + " = '" + comment.getCommentID() + "'";
		
		executeSQL(sql);
	}
	
	public void deletePost(int postID){
		String sql = "DELETE FROM " + PostSQL.TABLE_NAME +" WHERE " + PostSQL.FIELD_ID + " = '" + postID + "'";
		executeSQL(sql);
	}
	
	public void deleteComment(int commentID){
		String sql = "DELETE FROM " + CommentSQL.TABLE_NAME +" WHERE " + CommentSQL.FIELD_ID + " = '" + commentID + "'";
		executeSQL(sql);
	}

	/**
	 * inserts new user into db and returns the id of him
	 * 
	 * @param userName
	 * @return new id of user
	 */
	public int insertNewUser() {
		String sql = "INSERT INTO `"+UserSQL.TABLE_NAME+"` (`"
				
				+ UserSQL.FIELD_ID + "`, `"
				+ UserSQL.FIELD_GROUPS + "`, `"
				+ UserSQL.FIELD_REPORTED_COUNT + "`, `"
				+ UserSQL.FIELD_REPORTED_POSTS + "`, `"
				+ UserSQL.FIELD_REPORTED_COMMENTS + "`, `"
				+ UserSQL.FIELD_REPORTED_OTHER_POSTS + "`, `"
				+ UserSQL.FIELD_REPORTED_OTHER_COMMENTS + "`, `"
				+ UserSQL.FIELD_BANNED_COUNT + "`, `"
				+ UserSQL.FIELD_IS_BANNED + "`, `"
				+ UserSQL.FIELD_BANNED_UNTIL + "`, `"
				+ UserSQL.FIELD_GROUPS_AVAILABLE + "`)"
				
				+ " VALUES ("
				+ "DEFAULT, "
				+ "DEFAULT, "
				+ "DEFAULT, "
				+ "DEFAULT, "
				+ "DEFAULT, "
				+ "DEFAULT, "
				+ "DEFAULT, "
				+ "DEFAULT, "
				+ "DEFAULT, "
				+ "DEFAULT, "
				+ "DEFAULT)";
		
		executeSQL(sql);
		
		return getLastAutoIncrementID();
	}
	
	public void insertNewComment(CommentNewPojo cnp, int picID) {
			String sql = "INSERT INTO " + CommentSQL.TABLE_NAME + " ("
					
					+ CommentSQL.FIELD_ID + ", "
					+ CommentSQL.FIELD_POST_ID + ", "
					+ CommentSQL.FIELD_USER_ID + ", "
					+ CommentSQL.FIELD_PIC_ID + ", "
					+ CommentSQL.FIELD_LAT + ", "
					+ CommentSQL.FIELD_LON + ", "
					+ CommentSQL.FIELD_DATE + ", "
					+ CommentSQL.FIELD_GOODRATING + ", "
					+ CommentSQL.FIELD_BADRATING + ", "
					+ CommentSQL.FIELD_TEXT + ", "
					+ CommentSQL.FIELD_IS_ANONYMOUS + ")"
					
					+ " VALUES (" 
					
					+ "DEFAULT, '"
					+ cnp.getPostID() + "', '"
					+ cnp.getUserID() + "', " 
					+ (picID>0?"'"+String.valueOf(picID)+"'":"DEFAULT") + ", '" 
					+ cnp.getLocation().getLatitude() + "', '" 
					+ cnp.getLocation().getLongitude() + "', '"
					+ cnp.getDate() + "', " 
					+ "DEFAULT" + ", " 
					+ "DEFAULT" + ", '" 
					+ cnp.getText() + "', '" 
					+ String.valueOf(cnp.isAnonymous()?1:0) + "')";

			executeSQL(sql);
	}
	
	public int getLastAutoIncrementID(){
		int autoIncKeyFromFunc = -1;
		ResultSet rs = executeSQLQuery("SELECT LAST_INSERT_ID()");

		try {
			if (rs.next()) {
				autoIncKeyFromFunc = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			closeResult(rs);
			rs = null;
		}
		

		
		return autoIncKeyFromFunc;
	}
	
	public ArrayList<PostListPojo> getPosts(Integer[] pIDs) {
		StringBuffer sb = new StringBuffer();
		String sql = "SELECT * FROM " + PostSQL.TABLE_NAME
				+ " LEFT JOIN " + UserSQL.TABLE_NAME
				+ " ON " + PostSQL.TABLE_NAME + "." + PostSQL.FIELD_USER_ID + "=" + UserSQL.TABLE_NAME + "." + UserSQL.FIELD_ID
				+ " WHERE "
				+ PostSQL.FIELD_ID +" IN ('";
		sb.append(sql);
		for (int i = 0; i < pIDs.length-1; i++) {
			sb.append(pIDs[i] +"', '");
		}
		sb.append(pIDs[pIDs.length-1] +"')");

		ResultSet result = executeSQLQuery(sb.toString());
		ArrayList<PostListPojo> posts = new ArrayList<PostListPojo>();
		
		if (result != null) {
			
			try {
				while (result.next()) {
					posts.add(getPostFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				posts = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}


		
		return posts;

	}

	
	public ArrayList<PostMapPojo> getPosts(DoubleLocPojo dblLoc, int groupID){
		return getPosts(dblLoc, groupID, "");
	}
	
	public ArrayList<PostMapPojo> getPosts(DoubleLocPojo dblLoc, int groupID, String searchWord) {
		int[] upLeftCoords = new int[] { dblLoc.getUpLeft().getLatitude(),
				dblLoc.getUpLeft().getLongitude() };
		int[] bottomRightCoords = new int[] {
				dblLoc.getBottomRight().getLatitude(),
				dblLoc.getBottomRight().getLongitude() };
		/**
		 * [0 1 2 3 ] [left, up, right, bottom]
		 */
		int[] boundingBox = new int[] { upLeftCoords[1], bottomRightCoords[0],
				bottomRightCoords[1], upLeftCoords[0] };

		String sql = "SELECT * FROM " + PostSQL.TABLE_NAME + " WHERE "
				+ PostSQL.FIELD_LAT + " < " + boundingBox[3] + " AND "
				+ PostSQL.FIELD_LAT + " > " + boundingBox[1] + " AND "
				+ PostSQL.FIELD_LON + " < " + boundingBox[2] + " AND "
				+ PostSQL.FIELD_LON + " > " + boundingBox[0]
	
		 		+ " AND " + PostSQL.FIELD_GROUP_ID + " = " + groupID;
		
		if(searchWord.length() > 0){
			sql += " AND " + PostSQL.FIELD_TEXT + " LIKE '%" + searchWord + "%'";
		}

		ResultSet result = executeSQLQuery(sql);

		ArrayList<PostMapPojo> posts = null;
		if (result != null) {
			posts = new ArrayList<PostMapPojo>();
			try {
				while (result.next()) {
					posts.add(getPostMapFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				closeResult(result);
				result = null;
			}
		}
		

		return posts;
	}
	
	public ArrayList<GroupPojo> getGroups(DoubleLocPojo dblLoc) {
		int[] upLeftCoords = new int[] { dblLoc.getUpLeft().getLatitude(),
				dblLoc.getUpLeft().getLongitude() };
		int[] bottomRightCoords = new int[] {
				dblLoc.getBottomRight().getLatitude(),
				dblLoc.getBottomRight().getLongitude() };
		/**
		 * [0 1 2 3 ] [left, up, right, bottom]
		 */
		int[] boundingBox = new int[] { upLeftCoords[1], bottomRightCoords[0],
				bottomRightCoords[1], upLeftCoords[0] };

		String sql = "SELECT * FROM " + GroupSQL.TABLE_NAME + " WHERE "
				+ GroupSQL.FIELD_LAT + " < " + boundingBox[3] + " AND "
				+ GroupSQL.FIELD_LAT + " > " + boundingBox[1] + " AND "
				+ GroupSQL.FIELD_LON + " < " + boundingBox[2] + " AND "
				+ GroupSQL.FIELD_LON + " > " + boundingBox[0]
						
			    + " AND " + GroupSQL.FIELD_IS_PUBLIC + " = " + 1;

		ResultSet result = executeSQLQuery(sql);

		ArrayList<GroupPojo> groups = null;
		if (result != null) {
			groups = new ArrayList<GroupPojo>();
			try {
				while (result.next()) {
					groups.add(getGroupFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				closeResult(result);
				result = null;
			}
		}
		

		return groups;
	}
	
	public ArrayList<PostListPojo> getPostsOfGroup(int groupID){

		String sql = "SELECT * FROM " + PostSQL.TABLE_NAME 
				+ " LEFT JOIN " + UserSQL.TABLE_NAME
				+ " ON " + PostSQL.TABLE_NAME + "." + PostSQL.FIELD_USER_ID + "=" + UserSQL.TABLE_NAME + "." + UserSQL.FIELD_ID
				+ " WHERE "
				+ PostSQL.FIELD_GROUP_ID + " = " + groupID;

		ResultSet result = executeSQLQuery(sql);

		ArrayList<PostListPojo> posts = null;
		if (result != null) {
			posts = new ArrayList<PostListPojo>();
			try {
				while (result.next()) {
					posts.add(getPostFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				closeResult(result);
				result = null;
			}
		}
		

		return posts;
	}
	
	public ArrayList<PostListPojo> getAllPosts() {

		String sql = "SELECT * FROM " + PostSQL.TABLE_NAME
				+ " LEFT JOIN " + UserSQL.TABLE_NAME
				+ " ON " + PostSQL.TABLE_NAME + "." + PostSQL.FIELD_USER_ID + "=" + UserSQL.TABLE_NAME + "." + UserSQL.FIELD_ID;
		

		ResultSet result = executeSQLQuery(sql);

		ArrayList<PostListPojo> posts = null;
		if (result != null) {
			posts = new ArrayList<PostListPojo>();
			try {
				while (result.next()) {
					posts.add(getPostFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				posts = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}
		


		return posts;

	}
	
	private PostListPojo getPostFromResultSet(ResultSet result){
		try{
			PostListPojo post = new PostListPojo();
			post.setPostID(result.getInt(PostSQL.FIELD_ID));
			post.setLocation(new LocationPojo(result
					.getInt(PostSQL.FIELD_LAT), result
					.getInt(PostSQL.FIELD_LON)));
			post.setDate(result.getLong(PostSQL.FIELD_DATE));
			post.setPicID(result.getInt((PostSQL.FIELD_PIC_ID)));
			post.setText(result.getString(PostSQL.FIELD_TEXT));
			post.setUserID(result.getInt(PostSQL.FIELD_USER_ID));
			post.setGoodRating(result.getInt(PostSQL.FIELD_GOODRATING));
			post.setBadRating(result.getInt(PostSQL.FIELD_BADRATING));
			post.setOtherRating(result.getInt(PostSQL.FIELD_OTHER_RATING_INDEX));
			post.setCommentCount(result.getInt(PostSQL.FIELD_COMMENT_COUNT));
			post.setGroup_id(result.getInt(PostSQL.FIELD_GROUP_ID));
			String ratedGoodBy = result.getString(PostSQL.FIELD_RATED_GOOD_BY);
			if(ratedGoodBy != null){
				if(!ratedGoodBy.equals("null")){
					Integer[] a = (Integer[]) JSONConverter.fromJSONArray(ratedGoodBy, Integer[].class.getName());
					post.setRatedGoodBy(CollectionStuff.integerToIntArray(a));
				}
			}
			String ratedBadBy = result.getString(PostSQL.FIELD_RATED_BAD_BY);
			if(ratedBadBy != null){
				if(!ratedBadBy.equals("null")){
					Integer[] a = (Integer[]) JSONConverter.fromJSONArray(ratedBadBy, Integer[].class.getName());
					post.setRatedBadBy(CollectionStuff.integerToIntArray(a));
				}
			}
			post.setLast_action(result.getLong(PostSQL.FIELD_LAST_ACTION));
			post.setAttentionPoints(result.getInt(PostSQL.FIELD_ATTENTION_POINTS));
			post.setCurrent_attentionPoints(result.getInt(PostSQL.FIELD_CURRENT_ATTENTION_POINTS));
			post.setTearingDown(result.getBoolean(PostSQL.FIELD_TEARING_DOWN));
			post.setAnonymous(result.getBoolean(PostSQL.FIELD_IS_ANONYMOUS));
			post.setCategory(result.getInt(PostSQL.FIELD_CATEGORY));
			post.setLastCommentUID(result.getInt(PostSQL.FIELD_LAST_COMMENT_UID));
			
			post.setUsername(result.getString(UserSQL.FIELD_USERNAME));
			post.setUserPicID(result.getInt(UserSQL.FIELD_PIC_ID));
			return post;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}
	
	private MessagePojo getMessageFromResultSet(ResultSet result){
		try{
			MessagePojo message = new MessagePojo();
			message.setId(result.getInt(MessageSQL.FIELD_ID));
			message.setFrom(result.getInt(MessageSQL.FIELD_FROM));
			message.setTo(result.getInt(MessageSQL.FIELD_TO));
			message.setText(result.getString(MessageSQL.FIELD_TEXT));
			message.setDate(result.getLong(MessageSQL.FIELD_DATE));
			message.setRead(result.getBoolean(MessageSQL.FIELD_READ));
			message.setPostID(result.getInt(MessageSQL.FIELD_POST_ID));
			message.setPicID(result.getInt(MessageSQL.FIELD_PIC_ID));
			message.setCommentID(result.getInt(MessageSQL.FIELD_COMMENT_ID));
			message.setUsername(result.getString(UserSQL.FIELD_USERNAME));
			message.setUserPicID(result.getInt(UserSQL.FIELD_PIC_ID));
			message.setAnonymous(result.getBoolean(MessageSQL.FIELD_IS_ANONYMOUS));
			message.setInfo(result.getString(MessageSQL.FIELD_INFO));
			
			return message;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}
	
	private GroupPojo getGroupFromResultSet(ResultSet result){
		try{
			GroupPojo group = new GroupPojo();
			group.setId(result.getInt(GroupSQL.FIELD_ID));
			group.setIs_public(result.getBoolean(GroupSQL.FIELD_IS_PUBLIC));
			String members = result.getString(GroupSQL.FIELD_MEMBERS);
			if(members != null){
				if(!members.equals("null")){
					Integer[] a = (Integer[]) JSONConverter.fromJSONArray(members, Integer[].class.getName());
					group.setMembers(CollectionStuff.integerToIntArray(a));
				}
			}
			String banned_users = result.getString(GroupSQL.FIELD_BANNED_USERS);
			if(banned_users != null){
				if(!banned_users.equals("null")){
					Integer[] a = (Integer[]) JSONConverter.fromJSONArray(banned_users, Integer[].class.getName());
					group.setBanned_users(CollectionStuff.integerToIntArray(a));
				}
			}
			group.setName(result.getString(GroupSQL.FIELD_NAME));
			group.setPic_id(result.getInt(GroupSQL.FIELD_PIC_ID));
			group.setMember_count(result.getInt(GroupSQL.FIELD_MEMBER_COUNT));
			group.setPost_count(result.getInt(GroupSQL.FIELD_POST_COUNT));
			group.setCreator_id(result.getInt(GroupSQL.FIELD_CREATOR_ID));
			group.setLocation(new LocationPojo(result
					.getInt(GroupSQL.FIELD_LAT), result
					.getInt(GroupSQL.FIELD_LON)));
			String invited_users = result.getString(GroupSQL.FIELD_INVITED_USERS);
			if(invited_users != null){
				if(!invited_users.equals("null") && !invited_users.isEmpty()){
					Integer[] a = (Integer[]) JSONConverter.fromJSONArray(invited_users, Integer[].class.getName());
					group.setInvited_users(CollectionStuff.integerToIntArray(a));
				}
			}
			return group;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}
	
	private UserPojo getUserFromResultSet(ResultSet result){
		try{
			UserPojo user = new UserPojo();
			user.setId(result.getInt(UserSQL.FIELD_ID));
			String reportedCount = result.getString(UserSQL.FIELD_REPORTED_COUNT);
			if(reportedCount != null){
				if(!reportedCount.equals("null")){
					user.setReportedCount(Integer.parseInt(reportedCount));
				}
			}
			String groups = result.getString(UserSQL.FIELD_GROUPS);
			if(groups != null){
				if(!groups.equals("null")){
					Integer[] a = (Integer[]) JSONConverter.fromJSONArray(groups, Integer[].class.getName());
					user.setGroups(CollectionStuff.integerToIntArray(a));
				}
			}
			String reportedComments = result.getString(UserSQL.FIELD_REPORTED_COMMENTS);
			if(reportedComments != null){
				if(!reportedComments.equals("null")){
					Integer[] a = (Integer[]) JSONConverter.fromJSONArray(reportedComments, Integer[].class.getName());
					user.setReportedComments(CollectionStuff.integerToIntArray(a));
				}
			}
			String reportedPosts = result.getString(UserSQL.FIELD_REPORTED_POSTS);
			if(reportedPosts != null){
				if(!reportedPosts.equals("null")){
					Integer[] a = (Integer[]) JSONConverter.fromJSONArray(reportedPosts, Integer[].class.getName());
					user.setReportedPosts(CollectionStuff.integerToIntArray(a));
				}
			}
			String reportedOtherPosts = result.getString(UserSQL.FIELD_REPORTED_OTHER_POSTS);
			if(reportedOtherPosts != null){
				if(!reportedOtherPosts.equals("null")){
					Integer[] a = (Integer[]) JSONConverter.fromJSONArray(reportedOtherPosts, Integer[].class.getName());
					user.setReportedOtherPosts(CollectionStuff.integerToIntArray(a));
				}
			}
			String reportedOtherComments = result.getString(UserSQL.FIELD_REPORTED_OTHER_COMMENTS);
			if(reportedOtherComments != null){
				if(!reportedOtherComments.equals("null")){
					Integer[] a = (Integer[]) JSONConverter.fromJSONArray(reportedOtherComments, Integer[].class.getName());
					user.setReportedOtherComments(CollectionStuff.integerToIntArray(a));
				}
			}
			user.setBanned(result.getBoolean(UserSQL.FIELD_IS_BANNED));
			user.setBannedCount(result.getInt(UserSQL.FIELD_BANNED_COUNT));
			user.setBannedUntil(result.getLong(UserSQL.FIELD_BANNED_UNTIL));
			user.setUsername(result.getString(UserSQL.FIELD_USERNAME));
			user.setPicID(result.getInt(UserSQL.FIELD_PIC_ID));
			user.setGroups_available(result.getInt(UserSQL.FIELD_GROUPS_AVAILABLE));
			return user;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}
	
	private CommentPojo getCommentFromResultSet(ResultSet result){
		try{
			CommentPojo comment = new CommentPojo();
			comment.setPostID(result.getInt(CommentSQL.FIELD_POST_ID));
			comment.setLocation(new LocationPojo(result
					.getInt(CommentSQL.FIELD_LAT), result
					.getInt(CommentSQL.FIELD_LON)));
			comment.setDate(result.getLong(CommentSQL.FIELD_DATE));
			comment.setPicID(result.getInt((CommentSQL.FIELD_PIC_ID)));
			comment.setText(result.getString(CommentSQL.FIELD_TEXT));
			comment.setUserID(result.getInt(CommentSQL.FIELD_USER_ID));
			comment.setCommentID(result.getInt(CommentSQL.FIELD_ID));
			comment.setGoodRating(result.getInt(CommentSQL.FIELD_GOODRATING));
			comment.setBadRating(result.getInt(CommentSQL.FIELD_BADRATING));
			String ratedGoodBy = result.getString(CommentSQL.FIELD_RATED_GOOD_BY);
			if(ratedGoodBy != null){
				if(!ratedGoodBy.equals("null")){
					Integer[] a = (Integer[]) JSONConverter.fromJSONArray(ratedGoodBy, Integer[].class.getName());
					comment.setRatedGoodBy(CollectionStuff.integerToIntArray(a));
				}
			}
			String ratedBadBy = result.getString(CommentSQL.FIELD_RATED_BAD_BY);
			if(ratedBadBy != null){
				if(!ratedBadBy.equals("null")){
					Integer[] a = (Integer[]) JSONConverter.fromJSONArray(ratedBadBy, Integer[].class.getName());
					comment.setRatedBadBy(CollectionStuff.integerToIntArray(a));
				}
			}
			comment.setAnonymous(result.getBoolean(CommentSQL.FIELD_IS_ANONYMOUS));
			comment.setUsername(result.getString(UserSQL.FIELD_USERNAME));
			comment.setUserPicID(result.getInt(UserSQL.FIELD_PIC_ID));

			return comment;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}
	
	private PostMapPojo getPostMapFromResultSet(ResultSet result){
		try{
			PostMapPojo post = new PostMapPojo();
			post.setPostID(result.getInt(PostSQL.FIELD_ID));
			post.setLocation(new LocationPojo(result
					.getInt(PostSQL.FIELD_LAT), result
					.getInt(PostSQL.FIELD_LON)));
			post.setOtherRating(result.getInt(PostSQL.FIELD_OTHER_RATING_INDEX));
			post.setPicID(result.getInt(PostSQL.FIELD_PIC_ID));
			post.setLast_action(result.getLong(PostSQL.FIELD_LAST_ACTION));
			post.setCategory(result.getInt(PostSQL.FIELD_CATEGORY));
			post.setText(result.getString(PostSQL.FIELD_TEXT));
			return post;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}

	public UserPojo getUser(int id) {

		String sql = "SELECT * FROM " + UserSQL.TABLE_NAME + " WHERE `"
				+ UserSQL.FIELD_ID + "` = '" + id + "'";

		ResultSet result = executeSQLQuery(sql);

		UserPojo user = new UserPojo();
		if (result != null) {
			try {
				if (result.next()) {
					user = getUserFromResultSet(result);
				}
				else{
					user = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				user = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}
		


		return user;
	}
	
	public UserPojo getUser(String username) {

		String sql = "SELECT * FROM " + UserSQL.TABLE_NAME + " WHERE `"
				+ UserSQL.FIELD_USERNAME + "` = '" + username + "'";

		ResultSet result = executeSQLQuery(sql);

		UserPojo user = new UserPojo();
		if (result != null) {
			try {
				if (result.next()) {
					user = getUserFromResultSet(result);
				}
				else{
					user = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				user = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}
		


		return user;
	}

	private void executeSQL(String sql) {
		Connection con = null;
		Statement state = null;
		try {
			con = dbConnection.getConnection();
			state = con.createStatement();
			state.execute(sql);
			
			state.close();
			state = null;
			con.close();
			con = null;
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if(state != null){
				try {
					state.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				state = null;
			}
			
			if(con != null){
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				con = null;
			}
		}
	}

	private ResultSet executeSQLQuery(String sql) {
		Connection con = null;
		Statement state = null;
		ResultSet result = null;
		try {
			con = dbConnection.getConnection();
			state = con.createStatement();
			
			result = state.executeQuery(sql);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private void closeResult(ResultSet result){
		if(result != null){
				Statement state = null;
				Connection con = null;
			try {
				state = result.getStatement();
			
				con = state.getConnection();
				
				result.close();
				result = null;
				
				state.close();
				state = null;
				
				con.close();
				con = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				if(state != null){
					try {
						state.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					state = null;
				}
				
				if(con != null){
					try {
						con.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					con = null;
				}
			}
		}
	}

	public String getPicUrl(int picID) {

		String sql = "SELECT "+PictureSQL.FIELD_PIC_URL+" FROM " + PictureSQL.TABLE_NAME + " WHERE "
				+ PictureSQL.FIELD_ID + " = '" + picID + "'";

		ResultSet result = executeSQLQuery(sql);

		String resultString = "";
		
		if (result != null) {
			try {
				if (result.next()) {
					resultString = result.getString(PictureSQL.FIELD_PIC_URL);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				resultString = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}
		


		return resultString;
	}
	
	public ArrayList<CommentPojo> getCommentsFromPost(int postID){

		String sql = "SELECT * FROM " + CommentSQL.TABLE_NAME 
				+ " LEFT JOIN " + UserSQL.TABLE_NAME
				+ " ON " + CommentSQL.TABLE_NAME + "." + CommentSQL.FIELD_USER_ID + "=" + UserSQL.TABLE_NAME + "." + UserSQL.FIELD_ID
				+ " WHERE "
				+ CommentSQL.FIELD_POST_ID + " = '" + postID + "'";

		ResultSet result = executeSQLQuery(sql);
		ArrayList<CommentPojo> comments = new ArrayList<CommentPojo>();
		
		if (result != null) {
			try {
				while(result.next()) {
					comments.add(getCommentFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				comments = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}


		
		return comments;
	}
	
	public CommentPojo getSingleComment(int id) {

		String sql = "SELECT * FROM " + CommentSQL.TABLE_NAME 
				+ " LEFT JOIN " + UserSQL.TABLE_NAME
				+ " ON " + CommentSQL.TABLE_NAME + "." + CommentSQL.FIELD_USER_ID + "=" + UserSQL.TABLE_NAME + "." + UserSQL.FIELD_ID
				+ " WHERE "
				+ CommentSQL.TABLE_NAME + "." + CommentSQL.FIELD_ID + " = '" + id + "'";

		ResultSet result = executeSQLQuery(sql);

		CommentPojo resultComment = null;
		if (result != null) {
			try {
				if (result.next()) {
					resultComment = getCommentFromResultSet(result);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				closeResult(result);
				result = null;
			}
		}


		
		return resultComment;
	}
	
	public ArrayList<MessagePojo> getMessagesForUser(int id) {

		String sql = "SELECT * FROM " + MessageSQL.TABLE_NAME 
				+ " LEFT JOIN " + UserSQL.TABLE_NAME
				+ " ON " + MessageSQL.TABLE_NAME + "." + MessageSQL.FIELD_FROM + "=" + UserSQL.TABLE_NAME + "." + UserSQL.FIELD_ID
				+ " WHERE `"
				+ MessageSQL.FIELD_TO + "` = '" + id + "'";

		ResultSet result = executeSQLQuery(sql);

		ArrayList<MessagePojo> messages = null;
		if (result != null) {
			messages = new ArrayList<MessagePojo>();
			try {
				while (result.next()) {
					messages.add(getMessageFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				messages = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}


		
		return messages;
	}
	
	public PostListPojo getSinglePost(int id) {

		String sql = "SELECT * FROM " + PostSQL.TABLE_NAME 
				+ " LEFT JOIN " + UserSQL.TABLE_NAME
				+ " ON " + PostSQL.TABLE_NAME + "." + PostSQL.FIELD_USER_ID + "=" + UserSQL.TABLE_NAME + "." + UserSQL.FIELD_ID
				+ " WHERE "
				+ PostSQL.TABLE_NAME + "." + PostSQL.FIELD_ID + " = '" + id + "'";

		ResultSet result = executeSQLQuery(sql);

		PostListPojo resultPostList = null;
		if (result != null) {
			try {
				if (result.next()) {
					resultPostList = getPostFromResultSet(result);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				closeResult(result);
				result = null;
			}
		}
		


		return resultPostList;
	}

	public ArrayList<GroupPojo> getGroupsForUser(int id) {
		String sql = "SELECT " + UserSQL.FIELD_GROUPS + " FROM " + UserSQL.TABLE_NAME + " WHERE "
				+ UserSQL.FIELD_ID + " = '" + id + "'";

		ResultSet result = executeSQLQuery(sql);
		String groupStringArray = null;
		try {
			groupStringArray = result.getString(UserSQL.FIELD_GROUPS);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		int[] groupArray = null;
		
		if(groupStringArray != null){
			if(!groupStringArray.equals("null")){
				Integer[] a = (Integer[]) JSONConverter.fromJSONArray(groupStringArray, Integer[].class.getName());
				groupArray = CollectionStuff.integerToIntArray(a);
			}
		}
		
		closeResult(result);
		
		if(groupArray != null){
			StringBuffer sb = new StringBuffer();
			sql = "SELECT * FROM " + GroupSQL.TABLE_NAME + " WHERE "
					+ GroupSQL.FIELD_ID +" IN ('";
			sb.append(sql);
			for (int i = 0; i < groupArray.length-1; i++) {
				sb.append(groupArray[i] +"', '");
			}
			sb.append(groupArray[groupArray.length-1] +"')");
			
			result = executeSQLQuery(sql);
			
			ArrayList<GroupPojo> groups = null;
			if (result != null) {
				groups = new ArrayList<GroupPojo>();
				try {
					while (result.next()) {
						groups.add(getGroupFromResultSet(result));
					}
				} catch (SQLException e) {
					e.printStackTrace();
					groups = null;
				}finally{
					closeResult(result);
					result = null;
				}
			}
			

			return groups;
		}
		
		result = null;
		return null;
	}

	public ArrayList<PostListPojo> getPostsFromUser(int id) {

		String sql = "SELECT * FROM " + PostSQL.TABLE_NAME 
				+ " LEFT JOIN " + UserSQL.TABLE_NAME
				+ " ON " + PostSQL.TABLE_NAME + "." + PostSQL.FIELD_USER_ID + "=" + UserSQL.TABLE_NAME + "." + UserSQL.FIELD_ID
				+" WHERE '"
				+ PostSQL.FIELD_USER_ID + "' = '" + id + "'";

		ResultSet result = executeSQLQuery(sql);

		ArrayList<PostListPojo> posts = null;
		if (result != null) {
			posts = new ArrayList<PostListPojo>();
			try {
				while (result.next()) {
					posts.add(getPostFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				posts = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}


		
		return posts;
	}

	public void updateUser(UserPojo user) {
		String sql = "UPDATE "+ UserSQL.TABLE_NAME + " "
				
				+"SET "
				
				+ UserSQL.FIELD_GROUPS + " = '" + JSONConverter.toJSON(user.getGroups()) + "', "
				+ UserSQL.FIELD_REPORTED_COUNT + " = '" + user.getReportedCount() + "', "
				+ UserSQL.FIELD_REPORTED_POSTS + " = '" + JSONConverter.toJSON(user.getReportedPosts()) + "', "
				+ UserSQL.FIELD_REPORTED_COMMENTS + " = '" + JSONConverter.toJSON(user.getReportedComments()) + "', "
				+ UserSQL.FIELD_REPORTED_OTHER_POSTS + " = '" + JSONConverter.toJSON(user.getReportedOtherPosts()) + "', "
				+ UserSQL.FIELD_REPORTED_OTHER_COMMENTS + " = '" + JSONConverter.toJSON(user.getReportedOtherComments()) + "', "
				+ UserSQL.FIELD_BANNED_COUNT + " = '" + user.getBannedCount() + "', "
				+ UserSQL.FIELD_IS_BANNED + " = '" + String.valueOf(user.isBanned()?1:0) + "', "
				+ UserSQL.FIELD_BANNED_UNTIL + " = '" + user.getBannedUntil() + "', "
				+ UserSQL.FIELD_USERNAME + " = '" + user.getUsername() + "', "
				+ UserSQL.FIELD_PIC_ID + " = '" + user.getPicID() + "', "
				+ UserSQL.FIELD_GROUPS_AVAILABLE + " = '" + user.getGroups_available() + "'"
				
				+ " WHERE " + UserSQL.FIELD_ID + " = '" + user.getId() + "'";
		
		executeSQL(sql);
	}

	public int insertNewMessage(MessagePojo mp) {
		String sql = "INSERT INTO " + MessageSQL.TABLE_NAME + " (`"
				
					+ MessageSQL.FIELD_ID + "`, `"
					+ MessageSQL.FIELD_DATE + "`, `"
					+ MessageSQL.FIELD_FROM + "`, `"
					+ MessageSQL.FIELD_TO + "`, `"
					+ MessageSQL.FIELD_READ + "`, `"
					+ MessageSQL.FIELD_TEXT + "`, `"
					+ MessageSQL.FIELD_POST_ID + "`, `"
					+ MessageSQL.FIELD_PIC_ID + "`, `"
					+ MessageSQL.FIELD_COMMENT_ID + "`, `"
					+ MessageSQL.FIELD_IS_ANONYMOUS + "`, `"
					+ MessageSQL.FIELD_INFO + "`)"
					
					+ " VALUES (" 
					
					+ "DEFAULT, '"
					+ mp.getDate() + "', '"
					+ mp.getFrom() + "', '" 
					+ mp.getTo() + "', '" 
					+ (mp.isRead()?1:0) + "', '" 
					+ mp.getText() + "', '" 
					+ mp.getPostID() + "', '" 
					+ mp.getPicID() + "', '" 
					+ mp.getCommentID() + "', '" 
					+ (mp.isAnonymous()?1:0) + "', '" 
					+ mp.getInfo() + "')"; 

			executeSQL(sql);
			return getLastAutoIncrementID();
	}

	public void insertNotification(NotificationPojo np) {
		String sql = "INSERT INTO " + NotificationSQL.TABLE_NAME + " ("
				
					+ NotificationSQL.FIELD_ID + ", "
					+ NotificationSQL.FIELD_USER_ID + ", "
					+ NotificationSQL.FIELD_ON_POST_ID + ", "
					+ NotificationSQL.FIELD_ON_COMMENT_ID + ", "
					+ NotificationSQL.FIELD_RATING + ", "
					+ NotificationSQL.FIELD_COMMENT_ID + ", "
					+ NotificationSQL.FIELD_EXCERPT + ", "
					+ NotificationSQL.FIELD_EXCERPT_COMMENT + ", "
					+ NotificationSQL.FIELD_DATE + ", `"
					+ NotificationSQL.FIELD_READ + "`, "
					+ NotificationSQL.FIELD_PIC_ID + ")"
					
					+ " VALUES (" 
					
					+ "DEFAULT, '"
					+ np.getUser_id() + "', '"
					+ np.getOn_post_id() + "', '"
					+ np.getOn_comment_id() + "', '" 
					+ np.getRating() + "', '" 
					+ np.getComment_id() + "', '" 
					+ np.getExcerpt() + "', '" 
					+ np.getExcerpt_comment() + "', '" 
					+ np.getDate() + "', '" 
					+ (np.isRead()?1:0) + "', '" 
					+ np.getPic_id() + "')"; 

			executeSQL(sql);
	}

	public ArrayList<NotificationPojo> getNotificationsForUser(int userID) {

		String sql = "SELECT * FROM " + NotificationSQL.TABLE_NAME + " WHERE "
				+ NotificationSQL.FIELD_USER_ID + " = " + userID;

		ResultSet result = executeSQLQuery(sql);

		ArrayList<NotificationPojo> notifications = null;
		if (result != null) {
			notifications = new ArrayList<NotificationPojo>();
			try {
				while (result.next()) {
					notifications.add(getNotificationFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				notifications = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}


		
		return notifications;
	}

	private NotificationPojo getNotificationFromResultSet(ResultSet result) {
		try{
			NotificationPojo notification = new NotificationPojo();
			notification.setId(result.getInt(NotificationSQL.FIELD_ID));
			notification.setOn_post_id(result.getInt(NotificationSQL.FIELD_ON_POST_ID));
			notification.setOn_comment_id(result.getInt(NotificationSQL.FIELD_ON_COMMENT_ID));
			notification.setUser_id(result.getInt(NotificationSQL.FIELD_USER_ID));
			notification.setRating(result.getInt(NotificationSQL.FIELD_RATING));
			notification.setComment_id(result.getInt(NotificationSQL.FIELD_COMMENT_ID));
			notification.setExcerpt(result.getString(NotificationSQL.FIELD_EXCERPT));
			notification.setExcerpt_comment(result.getString(NotificationSQL.FIELD_EXCERPT_COMMENT));
			notification.setDate(result.getLong(NotificationSQL.FIELD_DATE));
			notification.setRead(result.getBoolean(NotificationSQL.FIELD_READ));
			notification.setPic_id(result.getInt(NotificationSQL.FIELD_PIC_ID));
			return notification;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}

	public int getNumberOfNews(int userID) {
		String sql = "SELECT COUNT(*) FROM " + NotificationSQL.TABLE_NAME + " WHERE "
				+ NotificationSQL.FIELD_USER_ID + " = " + userID
				+ " AND `" + NotificationSQL.FIELD_READ + "` = false";
		
		int notificationCount = 0;
		ResultSet result = executeSQLQuery(sql);
		
		try {
			result.next();
			notificationCount = result.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			closeResult(result);
		}
		

		
		sql = "SELECT COUNT(*) FROM " + MessageSQL.TABLE_NAME + " WHERE `"
				+ MessageSQL.FIELD_TO + "` = " + userID
				+ " AND `" + MessageSQL.FIELD_READ + "` = false";
		
		int messageCount = 0;
		result = executeSQLQuery(sql);
		
		try {
			result.next();
			messageCount = result.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			closeResult(result);
		}
		

		int sum = notificationCount + messageCount;
		return sum;
	}

	public void setNotificationRead(int notifID) {
		String sql = "UPDATE "+ NotificationSQL.TABLE_NAME + " "
				
				+"SET `"
				
				+ NotificationSQL.FIELD_READ + "` = true"
				
				+ " WHERE " + NotificationSQL.FIELD_ID + " = '" + notifID + "'";
		
		executeSQL(sql);
	}
	
	public void setMessageRead(int messID) {
		String sql = "UPDATE "+ MessageSQL.TABLE_NAME + " "
				
				+"SET `"
				
				+ MessageSQL.FIELD_READ + "` = true"
				
				+ " WHERE " + MessageSQL.FIELD_ID + " = '" + messID + "'";
		
		executeSQL(sql);
	}

	public void deleteNotifications(NotificationPojo[] notifsArr) {
		StringBuffer sb = new StringBuffer();
		String sql = "DELETE FROM " + NotificationSQL.TABLE_NAME + " WHERE "
				+ NotificationSQL.FIELD_ID +" IN ('";
		sb.append(sql);
		for (int i = 0; i < notifsArr.length-1; i++) {
			sb.append(notifsArr[i].getId() +"', '");
		}
		sb.append(notifsArr[notifsArr.length-1].getId() +"')");
		
		executeSQL(sb.toString());
	}
	
	public void deleteMessages(Integer[] messIDs) {
		StringBuffer sb = new StringBuffer();
		String sql = "DELETE FROM " + MessageSQL.TABLE_NAME + " WHERE "
				+ MessageSQL.FIELD_ID +" IN ('";
		sb.append(sql);
		for (int i = 0; i < messIDs.length-1; i++) {
			sb.append(messIDs[i] +"', '");
		}
		sb.append(messIDs[messIDs.length-1] +"')");
		
		executeSQL(sb.toString());
	}

	public ArrayList<GroupPojo> getGroups(Integer[] groupIDs) {
		StringBuffer sb = new StringBuffer();
		String sql = "SELECT * FROM " + GroupSQL.TABLE_NAME + " WHERE "
				+ GroupSQL.FIELD_ID +" IN ('";
		sb.append(sql);
		for (int i = 0; i < groupIDs.length-1; i++) {
			sb.append(groupIDs[i] +"', '");
		}
		sb.append(groupIDs[groupIDs.length-1] +"')");
		
		ResultSet result = executeSQLQuery(sb.toString());
		
		ArrayList<GroupPojo> groups = null;
		if (result != null) {
			groups = new ArrayList<GroupPojo>();
			try {
				while (result.next()) {
					groups.add(getGroupFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				groups = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}
		

		return groups;
	}

	public boolean checkGroupAvailable(String groupName, String groupPlace) {
		String sql = "SELECT * FROM " + GroupSQL.TABLE_NAME + " WHERE "
				+ GroupSQL.FIELD_NAME + " = '" + groupName + "'"
				+ " AND "
				+ GroupSQL.FIELD_PLACE + " = '" + groupPlace + "'";
		
		ResultSet result = executeSQLQuery(sql);
		boolean available = false;
		try {
			available = !result.next();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			closeResult(result);
		}

		return available;
	}
	
	public boolean checkUsernameExistent(String userName) {
		String sql = "SELECT * FROM " + UserSQL.TABLE_NAME + " WHERE "
				+ UserSQL.FIELD_USERNAME + " = '" + userName + "'";
		
		ResultSet result = executeSQLQuery(sql);
		boolean existent = false;
		try {
			existent = result.next();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			closeResult(result);
		}
		

		return existent;
	}

	public int insertNewGroup(GroupPojo gp) {
		String sql = "INSERT INTO " + GroupSQL.TABLE_NAME + " (`"
				
					+ GroupSQL.FIELD_ID + "`, `"
					+ GroupSQL.FIELD_NAME + "`, `"
					+ GroupSQL.FIELD_IS_PUBLIC + "`, `"
					+ GroupSQL.FIELD_MEMBERS + "`, `"
					+ GroupSQL.FIELD_PIC_ID + "`, `"
					+ GroupSQL.FIELD_MEMBER_COUNT + "`, `"
					+ GroupSQL.FIELD_POST_COUNT + "`, `"
					+ GroupSQL.FIELD_CREATOR_ID + "`, `"
					+ GroupSQL.FIELD_LAT + "`, `"
					+ GroupSQL.FIELD_LON + "`, `"
					+ GroupSQL.FIELD_INVITED_USERS + "`)"
					
					+ " VALUES (" 
					
					+ "DEFAULT, '"
					+ gp.getName() + "', '"
					+ String.valueOf(gp.isIs_public()?1:0) + "', '" 
					+ JSONConverter.toJSON(gp.getMembers()) + "', '" 
					+ gp.getPic_id() + "', '"
					+ gp.getMember_count() + "', '"
					+ gp.getPost_count() + "', '"
					+ gp.getCreator_id() + "', ";
					if(gp.getLocation() != null){
						sql += "'";
						sql += gp.getLocation().getLatitude() + "', '";
						sql += gp.getLocation().getLongitude() + "', '"; 
					}else{
						sql += "DEFAULT, ";
						sql += "DEFAULT, ";
					}
					sql += JSONConverter.toJSON(gp.getInvited_users())+"')";

		executeSQL(sql);
		return getLastAutoIncrementID();
	}

	public GroupPojo getGroup(int groupID) {
		String sql = "SELECT * FROM " + GroupSQL.TABLE_NAME + " WHERE "
				+ GroupSQL.FIELD_ID + " = " + groupID;
		ResultSet result = executeSQLQuery(sql);
		GroupPojo group = null;
		try {
			result.next();
			group = getGroupFromResultSet(result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			closeResult(result);
		}
		return group;
	}

	public void updateGroup(GroupPojo group) {
		String sql = "UPDATE "+ GroupSQL.TABLE_NAME + " "
				
				+"SET "
				
				+ GroupSQL.FIELD_MEMBERS + " = '" + JSONConverter.toJSON(group.getMembers()) + "', "
				+ GroupSQL.FIELD_INVITED_USERS + " = '" + JSONConverter.toJSON(group.getInvited_users()) + "', "
				+ GroupSQL.FIELD_MEMBER_COUNT + " = '" + group.getMember_count() + "', "
				+ GroupSQL.FIELD_POST_COUNT + " = '" + group.getPost_count() + "', "
				+ GroupSQL.FIELD_BANNED_USERS + " = '" + JSONConverter.toJSON(group.getBanned_users()) + "'"
				
				+ " WHERE " + GroupSQL.FIELD_ID + " = '" + group.getId() + "'";
		
		executeSQL(sql);
	}

	public ArrayList<GroupPojo> searchGroups(String name, String place,
			boolean isPublic) {
		String sql;
		if(isPublic){
		sql = "SELECT * FROM " + GroupSQL.TABLE_NAME + " WHERE "
				+ GroupSQL.FIELD_NAME + " LIKE '%" + name + "%'"
				+ " AND "
				+ GroupSQL.FIELD_PLACE + " LIKE '%" + place + "%'"
				+ " AND "
				+ GroupSQL.FIELD_IS_PUBLIC + " = " + String.valueOf(isPublic?1:0);
		}else{
		sql = "SELECT * FROM " + GroupSQL.TABLE_NAME + " WHERE "
					+ GroupSQL.FIELD_NAME + " = '" + name + "'"
					+ " AND "
					+ GroupSQL.FIELD_PLACE + " = '" + place + "'"
					+ " AND "
					+ GroupSQL.FIELD_IS_PUBLIC + " = " + String.valueOf(isPublic?1:0);
		}
		
		ResultSet result = executeSQLQuery(sql);
		
		ArrayList<GroupPojo> groups = null;
		if (result != null) {
			groups = new ArrayList<GroupPojo>();
			try {
				while (result.next()) {
					groups.add(getGroupFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				groups = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}
		

		return groups;
	}

	public ArrayList<MessagePojo> getMessagesWithUser(int userID) {

		String sql = "SELECT * FROM " + MessageSQL.TABLE_NAME 
				+ " LEFT JOIN " + UserSQL.TABLE_NAME
				+ " ON " + MessageSQL.TABLE_NAME + "." + MessageSQL.FIELD_FROM + "=" + UserSQL.TABLE_NAME + "." + UserSQL.FIELD_ID
				+ " WHERE `"
				+ MessageSQL.FIELD_TO + "` = '" + userID + "'"
				+ " OR `"
				+ MessageSQL.FIELD_FROM + "` = '" + userID + "'";

		ResultSet result = executeSQLQuery(sql);

		ArrayList<MessagePojo> messages = null;
		if (result != null) {
			messages = new ArrayList<MessagePojo>();
			try {
				while (result.next()) {
					messages.add(getMessageFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				messages = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}



		
		return messages;	
	}

	public void deleteAllMessagesForUser(int userID) {
		String sql = "DELETE FROM " + MessageSQL.TABLE_NAME + " WHERE `"
				+ MessageSQL.FIELD_FROM + "` = " + userID
				+ " OR `"
				+ MessageSQL.FIELD_TO + "` = " + userID;
		executeSQL(sql);
	}

	public ArrayList<MessagePojo> getMessagesWithUsers(int userID, int otherUID, int postID, int commentID) {

		String sql = "SELECT * FROM " + MessageSQL.TABLE_NAME 
				+ " LEFT JOIN " + UserSQL.TABLE_NAME
				+ " ON " + MessageSQL.TABLE_NAME + "." + MessageSQL.FIELD_FROM + "=" + UserSQL.TABLE_NAME + "." + UserSQL.FIELD_ID
				+ " WHERE (`"
				+ MessageSQL.FIELD_TO + "` = '" + userID + "'"
				+ " OR `"
				+ MessageSQL.FIELD_FROM + "` = '" + userID + "')"
				+ " AND (`"
				+ MessageSQL.FIELD_TO + "` = '" + otherUID + "'"
				+ " OR `"
				+ MessageSQL.FIELD_FROM + "` = '" + otherUID + "')"
				+ " AND ";
				
		if(postID > 0){
			sql += MessageSQL.FIELD_POST_ID + " = " + postID;
		}else{
			sql += MessageSQL.FIELD_COMMENT_ID + " = " + commentID;
		}

		ResultSet result = executeSQLQuery(sql);

		ArrayList<MessagePojo> messages = null;
		if (result != null) {
			messages = new ArrayList<MessagePojo>();
			try {
				while (result.next()) {
					messages.add(getMessageFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				messages = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}



		
		return messages;
	}
	
	public void deletePicture(int picID){
		String sql = "DELETE FROM " + PictureSQL.TABLE_NAME + " WHERE " + PictureSQL.FIELD_ID + " = " + picID;
		executeSQL(sql);
	}

	public void deleteGroup(int groupID) {
		String sql = "DELETE FROM " + GroupSQL.TABLE_NAME + " WHERE " + GroupSQL.FIELD_ID + " = " + groupID;
		executeSQL(sql);
	}

	public ArrayList<UserPojo> getUsers(Integer[] members) {
		StringBuffer sb = new StringBuffer();
		String sql = "SELECT * FROM " + UserSQL.TABLE_NAME + " WHERE "
				+ UserSQL.FIELD_ID +" IN ('";
		sb.append(sql);
		for (int i = 0; i < members.length-1; i++) {
			sb.append(members[i] +"', '");
		}
		sb.append(members[members.length-1] +"')");
		
		ResultSet result = executeSQLQuery(sb.toString());
		
		ArrayList<UserPojo> users = null;
		if (result != null) {
			users = new ArrayList<UserPojo>();
			try {
				while (result.next()) {
					users.add(getUserFromResultSet(result));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				users = null;
			}finally{
				closeResult(result);
				result = null;
			}
		}
		

		return users;
	}
	
	public void insertFeedback(int userID, String feedback, String time, String android, String version, String phone) {
		String sql = "INSERT INTO " + FeedbackSQL.TABLE_NAME + " (`"
				
					+ FeedbackSQL.FIELD_ID + "`, `"
					+ FeedbackSQL.FIELD_USER_ID + "`, `"
					+ FeedbackSQL.FIELD_FEEDBACK + "`, `"
					+ FeedbackSQL.FIELD_TIME + "`, `"
					+ FeedbackSQL.FIELD_ANDROID + "`, `"
					+ FeedbackSQL.FIELD_PHONE + "`, `"
					+ FeedbackSQL.FIELD_VERSION + "`)"
					
					+ " VALUES (" 
					
					+ "DEFAULT, '"
					+ userID + "', '"
					+ feedback + "', '"
					+ time + "', '"
					+ android + "', '"
					+ phone + "', '"
					+ version + "')"; 

			executeSQL(sql);
	}

	
}

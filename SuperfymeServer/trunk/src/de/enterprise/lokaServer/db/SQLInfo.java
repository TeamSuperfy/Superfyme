package de.enterprise.lokaServer.db;


public class SQLInfo {

	public static final String DATABASE 		= "loka_database";
	
	public static class PostSQL{
		public static final String TABLE_NAME 				= "posts";
		
		public static final String FIELD_LAT 				= "latitude";
		public static final String FIELD_LON 				= "longitude";
		public static final String FIELD_ID					= "id";
		public static final String FIELD_USER_ID			= "user_id";
		public static final String FIELD_DATE				= "date";
		public static final String FIELD_PIC_ID				= "pic_id";
		public static final String FIELD_TEXT				= "text";
		public static final String FIELD_GOODRATING	 		= "goodRating";
		public static final String FIELD_BADRATING 			= "badRating";
		public static final String FIELD_OTHER_RATING_INDEX	= "otherRatingIndex";
		public static final String FIELD_COMMENT_COUNT		= "commentCount";
		public static final String FIELD_RATED_GOOD_BY		= "ratedGoodBy";
		public static final String FIELD_RATED_BAD_BY		= "ratedBadBy";
		public static final String FIELD_GROUP_ID			= "group_id";
		public static final String FIELD_BOARD_ID			= "blackboard_id";
		public static final String FIELD_LAST_ACTION		= "last_action";
		public static final String FIELD_ATTENTION_POINTS	= "attentionPoints";
		public static final String FIELD_CURRENT_ATTENTION_POINTS	= "current_attentionPoints";
		public static final String FIELD_TEARING_DOWN		= "tearingDown";
		public static final String FIELD_IS_ANONYMOUS		= "isAnonymous";
		public static final String FIELD_CATEGORY		= "category";
		public static final String FIELD_LAST_COMMENT_UID = "lastCommentUID";
	}
	
	public static class MessageSQL{
		public static final String TABLE_NAME			= "messages";
		
		public static final String FIELD_ID				= "id";
		public static final String FIELD_TEXT			= "text";
		public static final String FIELD_FROM			= "from";
		public static final String FIELD_TO				= "to";
		public static final String FIELD_DATE			= "date";
		public static final String FIELD_READ			= "read";
		public static final String FIELD_POST_ID		= "post_id";
		public static final String FIELD_COMMENT_ID		= "comment_id";
		public static final String FIELD_PIC_ID			= "pic_id";
		public static final String FIELD_IS_ANONYMOUS 	= "isAnonymous";
		public static final String FIELD_INFO 			= "info";
	}
	
	public static class FeedbackSQL{
		public static final String TABLE_NAME		= "feedback";
		
		public static final String FIELD_ID			= "id";
		public static final String FIELD_USER_ID	= "user_id";
		public static final String FIELD_FEEDBACK	= "feedback_text";
		public static final String FIELD_TIME		= "time";
		public static final String FIELD_ANDROID	= "android";
		public static final String FIELD_PHONE		= "phone";
		public static final String FIELD_VERSION	= "version";
	}
	
	public static class GroupSQL{
		public static final String TABLE_NAME				= "groups";
		
		public static final String FIELD_ID					= "id";
		public static final String FIELD_MEMBERS			= "members";
		public static final String FIELD_IS_PUBLIC			= "is_public";
		public static final String FIELD_NAME				= "name";
		public static final String FIELD_PLACE				= "place";
		public static final String FIELD_PIC_ID				= "pic_id";
		public static final String FIELD_PASSWORD			= "password";
		public static final String FIELD_CREATOR_POST_ONLY 	= "creator_post_only";
		public static final String FIELD_MEMBER_COUNT 		= "member_count";
		public static final String FIELD_POST_COUNT 		= "post_count";
		public static final String FIELD_CREATOR_ID 		= "creator_id";
		public static final String FIELD_BANNED_USERS 		= "banned_users";
		public static final String FIELD_LAT 				= "latitude";
		public static final String FIELD_LON 				= "longitude";
		public static final String FIELD_INVITED_USERS		= "invited_users";
	}
	
	public static class BlackboardSQL{
		public static final String TABLE_NAME		= "blackboards";
		
		public static final String FIELD_ID			= "id";
		public static final String FIELD_TITLE		= "title";
	}
	
	public static class UserSQL{
		public static final String TABLE_NAME					= "user";
		
		public static final String FIELD_ID						= "id";
		public static final String FIELD_GROUPS					= "groups";
		public static final String FIELD_REPORTED_COUNT 		= "reportedCount";
		public static final String FIELD_REPORTED_POSTS 		= "reportedPosts";
		public static final String FIELD_REPORTED_COMMENTS 		= "reportedComments";
		public static final String FIELD_REPORTED_OTHER_POSTS 	= "reportedOtherPosts";
		public static final String FIELD_REPORTED_OTHER_COMMENTS= "reportedOtherComments";
		public static final String FIELD_IS_BANNED				= "is_banned";
		public static final String FIELD_BANNED_COUNT			= "banned_count";
		public static final String FIELD_BANNED_UNTIL			= "banned_until";
		public static final String FIELD_USERNAME				= "username";
		public static final String FIELD_PIC_ID					= "picID";
		public static final String FIELD_GROUPS_AVAILABLE		= "groups_available";
	}
	
	public static class PictureSQL{
		public static final String TABLE_NAME		= "pictures";
		
		public static final String FIELD_ID			= "id";
		public static final String FIELD_PIC_URL	= "url";
	}
	
	public static class NotificationSQL{
		public static final String TABLE_NAME				= "notifications";
		
		public static final String FIELD_ID					= "id";
		public static final String FIELD_USER_ID			= "user_id";
		public static final String FIELD_ON_POST_ID 		= "on_post_id";
		public static final String FIELD_ON_COMMENT_ID 		= "on_comment_id";
		public static final String FIELD_RATING				= "rating";
		public static final String FIELD_COMMENT_ID			= "comment_id";
		public static final String FIELD_EXCERPT			= "excerpt";
		public static final String FIELD_EXCERPT_COMMENT	= "excerpt_comment";
		public static final String FIELD_DATE				= "date";
		public static final String FIELD_PIC_ID				= "pic_id";
		public static final String FIELD_READ				= "read";
	}
	
	public static class CommentSQL{
		public static final String TABLE_NAME 				= "comments";
		
		public static final String FIELD_LAT 				= "latitude";
		public static final String FIELD_LON 				= "longitude";
		public static final String FIELD_ID					= "id";
		public static final String FIELD_USER_ID			= "user_id";
		public static final String FIELD_POST_ID			= "post_id";
		public static final String FIELD_DATE				= "date";
		public static final String FIELD_PIC_ID				= "pic_id";
		public static final String FIELD_TEXT				= "text";
		public static final String FIELD_GOODRATING	 		= "goodRating";
		public static final String FIELD_BADRATING 			= "badRating";
		public static final String FIELD_RATED_GOOD_BY		= "ratedGoodBy";
		public static final String FIELD_RATED_BAD_BY		= "ratedBadBy";
		public static final String FIELD_IS_ANONYMOUS		= "isAnonymous";
	}
	
}

package de.enterprise.lokaServer.pojos;

import java.io.Serializable;

public class PostListPojo implements Serializable{

	private int postID;
	private int userID;
	private int picID;
	private LocationPojo location;
	private String text;
	private long date;
	private int goodRating;
	private int badRating;
	private int commentCount;
	private float otherRating;
	private int[] ratedGoodBy;
	private int[] ratedBadBy;
	private int group_id;
	private int board_id;
	private long last_action;
	private boolean tearingDown;
	private float attentionPoints;
	private float current_attentionPoints;
	private boolean isAnonymous;
	private String username;
	private int userPicID;
	private int category;
	private int lastCommentUID;
	
	public int getLastCommentUID() {
		return lastCommentUID;
	}
	public void setLastCommentUID(int lastCommentUID) {
		this.lastCommentUID = lastCommentUID;
	}
	public int getCategory() {
		return category;
	}
	public void setCategory(int category) {
		this.category = category;
	}
	public int getUserPicID() {
		return userPicID;
	}
	public void setUserPicID(int userPicID) {
		this.userPicID = userPicID;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public boolean isAnonymous() {
		return isAnonymous;
	}
	public void setAnonymous(boolean isAnonymous) {
		this.isAnonymous = isAnonymous;
	}
	public float getCurrent_attentionPoints() {
		return current_attentionPoints;
	}
	public void setCurrent_attentionPoints(float current_attentionPoints) {
		this.current_attentionPoints = current_attentionPoints;
	}
	public boolean isTearingDown() {
		return tearingDown;
	}
	public void setTearingDown(boolean tearingDown) {
		this.tearingDown = tearingDown;
	}
	public float getAttentionPoints() {
		return attentionPoints;
	}
	public void setAttentionPoints(float attentionPoints) {
		this.attentionPoints = attentionPoints;
	}
	public long getLast_action() {
		return last_action;
	}
	public void setLast_action(long last_action) {
		this.last_action = last_action;
	}
	public int getGroup_id() {
		return group_id;
	}
	public void setGroup_id(int group_id) {
		this.group_id = group_id;
	}
	public int getBoard_id() {
		return board_id;
	}
	public void setBoard_id(int board_id) {
		this.board_id = board_id;
	}
	public int[] getRatedGoodBy() {
		return ratedGoodBy;
	}
	public int[] getRatedBadBy() {
		return ratedBadBy;
	}
	public void setRatedBadBy(int[] ratedBadBy) {
		this.ratedBadBy = ratedBadBy;
	}
	public void setRatedGoodBy(int[] ratedGoodBy) {
		this.ratedGoodBy = ratedGoodBy;
	}
	public float getOtherRating() {
		return otherRating;
	}
	public void setOtherRating(float otherRating) {
		this.otherRating = otherRating;
	}
	public int getPostID() {
		return postID;
	}
	public void setPostID(int postID) {
		this.postID = postID;
	}
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public int getPicID() {
		return picID;
	}
	public void setPicID(int picID) {
		this.picID = picID;
	}
	public LocationPojo getLocation() {
		return location;
	}
	public void setLocation(LocationPojo location) {
		this.location = location;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public int getGoodRating() {
		return goodRating;
	}
	public void setGoodRating(int goodRating) {
		this.goodRating = goodRating;
	}
	public int getBadRating() {
		return badRating;
	}
	public void setBadRating(int badRating) {
		this.badRating = badRating;
	}
	public int getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}
	
}

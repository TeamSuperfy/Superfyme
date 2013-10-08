package de.enterprise.lokaServer.pojos;

import java.io.Serializable;

public class CommentPojo implements Serializable{

	private int userID;
	private int commentID;
	private int picID;
	private int postID;
	private String tinyPic;
	private LocationPojo location;
	private long date;
	private int goodRating;
	private int badRating;
	private String text;
	private int[] ratedGoodBy;
	private int[] ratedBadBy;
	private boolean isAnonymous;
	private String username;
	private int userPicID;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getUserPicID() {
		return userPicID;
	}
	public void setUserPicID(int userPicID) {
		this.userPicID = userPicID;
	}
	public boolean isAnonymous() {
		return isAnonymous;
	}
	public void setAnonymous(boolean isAnonymous) {
		this.isAnonymous = isAnonymous;
	}
	public int[] getRatedBadBy() {
		return ratedBadBy;
	}
	public void setRatedBadBy(int[] ratedBadBy) {
		this.ratedBadBy = ratedBadBy;
	}
	public int[] getRatedGoodBy() {
		return ratedGoodBy;
	}
	public void setRatedGoodBy(int[] ratedGoodBy) {
		this.ratedGoodBy = ratedGoodBy;
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
	public int getCommentID() {
		return commentID;
	}
	public void setCommentID(int commentID) {
		this.commentID = commentID;
	}
	public int getPicID() {
		return picID;
	}
	public void setPicID(int picID) {
		this.picID = picID;
	}
	public String getTinyPic() {
		return tinyPic;
	}
	public void setTinyPic(String tinyPic) {
		this.tinyPic = tinyPic;
	}
	public LocationPojo getLocation() {
		return location;
	}
	public void setLocation(LocationPojo location) {
		this.location = location;
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
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	
	
}

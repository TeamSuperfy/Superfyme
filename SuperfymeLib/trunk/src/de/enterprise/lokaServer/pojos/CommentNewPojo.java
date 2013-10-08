package de.enterprise.lokaServer.pojos;

import java.io.Serializable;

public class CommentNewPojo implements Serializable{

	private int userID;
	private int postID;
	private LocationPojo location;
	private String pic;
	private byte[] picData;
	private long date;
	private String text;
	private boolean isAnonymous;
	
	public boolean isAnonymous() {
		return isAnonymous;
	}
	public void setAnonymous(boolean isAnonymous) {
		this.isAnonymous = isAnonymous;
	}
	public byte[] getPicData() {
		return picData;
	}
	public void setPicData(byte[] picData) {
		this.picData = picData;
	}
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public int getPostID() {
		return postID;
	}
	public void setPostID(int postID) {
		this.postID = postID;
	}
	public LocationPojo getLocation() {
		return location;
	}
	public void setLocation(LocationPojo location) {
		this.location = location;
	}
	public String getPic() {
		return pic;
	}
	public void setPic(String pic) {
		this.pic = pic;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}

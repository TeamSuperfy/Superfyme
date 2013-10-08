package de.enterprise.lokaServer.pojos;

import java.io.Serializable;

public class PostNewPojo implements Serializable{

	private int userID;
	private LocationPojo location;
	private String pic;
	private byte[] picData;
	private boolean isAnonymous;
	private int category;
	

	public int getCategory() {
		return category;
	}
	public void setCategory(int category) {
		this.category = category;
	}
	public boolean isAnonymous() {
		return isAnonymous;
	}
	public void setAnonymous(boolean isAnonymous) {
		this.isAnonymous = isAnonymous;
	}
	private long date;
	private String text;
	private int groupID;
	
	public byte[] getPicData() {
		return picData;
	}
	public void setPicData(byte[] picData) {
		this.picData = picData;
	}
	public int getGroupID() {
		return groupID;
	}
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
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

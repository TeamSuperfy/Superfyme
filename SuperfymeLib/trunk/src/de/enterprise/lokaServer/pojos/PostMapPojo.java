package de.enterprise.lokaServer.pojos;

import java.io.Serializable;

public class PostMapPojo implements Serializable{

	private LocationPojo location;
	private int postID;
	private int otherRating;
	private int picID;
	private long last_action;
	private int category;
	private String text;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getCategory() {
		return category;
	}
	public void setCategory(int category) {
		this.category = category;
	}
	public long getLast_action() {
		return last_action;
	}
	public void setLast_action(long last_action) {
		this.last_action = last_action;
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
	public int getPostID() {
		return postID;
	}
	public void setPostID(int postID) {
		this.postID = postID;
	}
	public int getOtherRating() {
		return otherRating;
	}
	public void setOtherRating(int otherRating) {
		this.otherRating = otherRating;
	}
	
}

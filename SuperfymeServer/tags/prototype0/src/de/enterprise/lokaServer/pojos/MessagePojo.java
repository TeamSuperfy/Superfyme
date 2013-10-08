package de.enterprise.lokaServer.pojos;

public class MessagePojo {

	private UserPojo user;
	private String picURL;
	private String text;
	private Long date;
	
	public UserPojo getUser() {
		return user;
	}
	public void setUser(UserPojo user) {
		this.user = user;
	}
	public String getPicURL() {
		return picURL;
	}
	public void setPicURL(String string) {
		this.picURL = string;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Long getDate() {
		return date;
	}
	public void setDate(Long date) {
		this.date = date;
	}
	
}

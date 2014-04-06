package de.enterprise.lokaServer.pojos;

import java.io.Serializable;

public class NotificationPojo implements Serializable{

	private int id;
	private int user_id;
	private int on_post_id;
	private int on_comment_id;
	private int rating;
	private int comment_id;
	private String excerpt;
	private String excerpt_comment;
	private long date;
	private int pic_id;
	private boolean read;
	
	public int getPic_id() {
		return pic_id;
	}
	public void setPic_id(int pic_id) {
		this.pic_id = pic_id;
	}
	public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public String getExcerpt() {
		return excerpt;
	}
	public void setExcerpt(String excerpt) {
		this.excerpt = excerpt;
	}
	public String getExcerpt_comment() {
		return excerpt_comment;
	}
	public void setExcerpt_comment(String excerpt_comment) {
		this.excerpt_comment = excerpt_comment;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getOn_post_id() {
		return on_post_id;
	}
	public void setOn_post_id(int on_post_id) {
		this.on_post_id = on_post_id;
	}
	public int getOn_comment_id() {
		return on_comment_id;
	}
	public void setOn_comment_id(int on_comment_id) {
		this.on_comment_id = on_comment_id;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public int getComment_id() {
		return comment_id;
	}
	public void setComment_id(int comment_id) {
		this.comment_id = comment_id;
	}
	
}

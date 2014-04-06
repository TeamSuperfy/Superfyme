package de.enterprise.lokaServer.pojos;

import java.io.Serializable;

public class GroupPojo implements Serializable{

	private int id;
	private int[] members;
	private boolean is_public;
	private String name;
	private int pic_id;
	private int member_count;
	private int post_count;
	private byte[] pic;
	private int creator_id;
	private int[] banned_users;
	private LocationPojo location;
	private int[] invited_users;

	public int[] getInvited_users() {
		return invited_users;
	}
	public void setInvited_users(int[] invited_users) {
		this.invited_users = invited_users;
	}
	public LocationPojo getLocation() {
		return location;
	}
	public void setLocation(LocationPojo location) {
		this.location = location;
	}
	public int[] getBanned_users() {
		return banned_users;
	}
	public void setBanned_users(int[] banned_users) {
		this.banned_users = banned_users;
	}
	public int getCreator_id() {
		return creator_id;
	}
	public void setCreator_id(int creator_id) {
		this.creator_id = creator_id;
	}
	public byte[] getPic() {
		return pic;
	}
	public void setPic(byte[] pic) {
		this.pic = pic;
	}
	public int getMember_count() {
		return member_count;
	}
	public void setMember_count(int member_count) {
		this.member_count = member_count;
	}
	public int getPost_count() {
		return post_count;
	}
	public void setPost_count(int post_count) {
		this.post_count = post_count;
	}
	public int getPic_id() {
		return pic_id;
	}
	public void setPic_id(int pic_id) {
		this.pic_id = pic_id;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int[] getMembers() {
		return members;
	}
	public void setMembers(int[] members) {
		this.members = members;
	}
	public boolean isIs_public() {
		return is_public;
	}
	public void setIs_public(boolean is_public) {
		this.is_public = is_public;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}

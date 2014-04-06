package de.enterprise.lokaServer.pojos;

import java.io.Serializable;
 
public class UserPojo implements Serializable{
	
	private int id;
	private int[] groups;
	private int reportedCount;
	private int[] reportedPosts;
	private int[] reportedComments;
	private int[] reportedOtherPosts;
	private int[] reportedOtherComments;
	private boolean isBanned;
	private int bannedCount;
	private long bannedUntil;
	private int picID;
	private String username;
	private int groups_available;

	public int getGroups_available() {
		return groups_available;
	}

	public void setGroups_available(int groups_available) {
		this.groups_available = groups_available;
	}

	public int getPicID() {
		return picID;
	}

	public void setPicID(int picID) {
		this.picID = picID;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public long getBannedUntil() {
		return bannedUntil;
	}

	public void setBannedUntil(long bannedUntil) {
		this.bannedUntil = bannedUntil;
	}

	public boolean isBanned() {
		return isBanned;
	}

	public void setBanned(boolean isBanned) {
		this.isBanned = isBanned;
	}

	public int getBannedCount() {
		return bannedCount;
	}

	public void setBannedCount(int bannedCount) {
		this.bannedCount = bannedCount;
	}

	public int[] getReportedOtherPosts() {
		return reportedOtherPosts;
	}

	public void setReportedOtherPosts(int[] reportedOtherPosts) {
		this.reportedOtherPosts = reportedOtherPosts;
	}

	public int[] getReportedOtherComments() {
		return reportedOtherComments;
	}

	public void setReportedOtherComments(int[] reportetOtherComments) {
		this.reportedOtherComments = reportetOtherComments;
	}

	public int getReportedCount() {
		return reportedCount;
	}

	public void setReportedCount(int reportedCount) {
		this.reportedCount = reportedCount;
	}

	public int[] getReportedPosts() {
		return reportedPosts;
	}

	public void setReportedPosts(int[] reportedPosts) {
		this.reportedPosts = reportedPosts;
	}

	public int[] getReportedComments() {
		return reportedComments;
	}

	public void setReportedComments(int[] reportedComments) {
		this.reportedComments = reportedComments;
	}

	public int[] getGroups() {
		return groups;
	}

	public void setGroups(int[] groups) {
		this.groups = groups;
	}

	public UserPojo(int i) {
		id = i;
	}
	
	public UserPojo(){}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	
	
}
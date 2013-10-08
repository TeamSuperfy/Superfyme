package de.enterprise.lokaServer.pojos;

public class RequestPostContentsPojo {

	private DoubleLocPojo dblLoc;
	private String filter;
	private String query;
	private int userID;
	
	public DoubleLocPojo getDblLoc() {
		return dblLoc;
	}
	public void setDblLoc(DoubleLocPojo dblLoc) {
		this.dblLoc = dblLoc;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}

	
}

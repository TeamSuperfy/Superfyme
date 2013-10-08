package de.enterprise.lokaServer.pojos;


public class PostPojo {

	private MessagePojo message;
	private LocationPojo loc;
	private int id;
	
	public MessagePojo getMessage() {
		return message;
	}
	public void setMessage(MessagePojo message) {
		this.message = message;
	}
	public LocationPojo getLoc() {
		return loc;
	}
	public void setLoc(LocationPojo loc) {
		this.loc = loc;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
}

package de.enterprise.lokaServer.pojos;

public class LocationPojo {

	private int longitude;
	private int latitude;
	
	public LocationPojo(){
	}
	
	public LocationPojo(int d, int e) {
		latitude = d;
		longitude = e;
	}
	public int getLongitude() {
		return longitude;
	}
	public void setLongitude(int longitude) {
		this.longitude = longitude;
	}
	public int getLatitude() {
		return latitude;
	}
	public void setLatitude(int latitude) {
		this.latitude = latitude;
	}
	
}

package de.enterprise.lokaServer.pojos;

import java.io.Serializable;

public class DoubleLocPojo implements Serializable{

	private LocationPojo upLeft, bottomRight;
	
	public DoubleLocPojo(LocationPojo a, LocationPojo b){
		upLeft = a;
		bottomRight = b;
	}
	
	public DoubleLocPojo(){
		
	}

	public LocationPojo getUpLeft() {
		return upLeft;
	}

	public void setUpLeft(LocationPojo upLeft) {
		this.upLeft = upLeft;
	}

	public LocationPojo getBottomRight() {
		return bottomRight;
	}

	public void setBottomRight(LocationPojo bottomRight) {
		this.bottomRight = bottomRight;
	}
	
}

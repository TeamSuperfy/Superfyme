package de.enterprise.lokaAndroid.tools;

import android.content.Context;
import android.location.Location;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaServer.pojos.LocationPojo;

public class GeoStuff {

	public static int getDistance(Location a, LocationPojo b){
		if(a != null && b != null){
			return (int) getDistance(a.getLatitude(), a.getLongitude(), b.getLatitude() / 1E6, b.getLongitude() / 1E6);
		}
		else{
			return -1;
		}
	}
	
	private static float getDistance(double lat1, double lon1, double lat2, double lon2){
		float[] results = new float[1];
		Location.distanceBetween(lat1, lon1, lat2, lon2, results);
		return results[0];
	}
	
	public static String getTimeText(long date, Context context){
		long deltaTime = System.currentTimeMillis() - date;
		int time = (int) (deltaTime / 1000);
		int limitSeconds = 60;
		int limitMinutes = 60*60;
		int limitHours = 60*60*24;
		String unit = "";
		if(time >= 0 && time < limitSeconds){
			if(time == 1)
				unit = " " + context.getResources().getString(R.string.second);
			else
				unit = " " + context.getResources().getString(R.string.seconds);
		}
		else if(time >= limitSeconds && time < limitMinutes){
			time = time / 60;
			if(time == 1)
				unit = " " + context.getResources().getString(R.string.minute);
			else
				unit = " " + context.getResources().getString(R.string.minutes);
		}
		else if(time >= limitMinutes && time < limitHours){
			time = time / 60 / 60;
			if(time == 1)
				unit = " " + context.getResources().getString(R.string.hour);
			else
				unit = " " + context.getResources().getString(R.string.hours);
		}
		else{
			time = time / 60 / 60 / 24;
			if(time == 1)
				unit = " " + context.getResources().getString(R.string.day);
			else
				unit = " " + context.getResources().getString(R.string.days);
		}
		
		return time + unit;
	}

	public static String getGeoText(int distance) {
		String text = "";
		if(distance >= 0 && distance < 500){
			text = distance + " m";
		}
		else if(distance > 500){
			float km = (distance/1000f);
			float result = (int) (km * 10) / 10f;
			text = result + " km";
		}
		else if(distance >= 10000){
			int km = distance/1000;
			text = km + " km";
		}
		
		return text;
	}
	
}

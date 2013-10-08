package de.enterprise.lokaServer.tools;

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;
import de.enterprise.lokaServer.pojos.LocationPojo;

public class GeoTools {

	/**
	 * Calculate a rectangular around a coordinate
	 * @param coord
	 * @param length
	 * @return [upLeft: GDC, bottomRight: GDC]
	 */
	public static LocationPojo[] getRectOfCoord(LocationPojo coord, int length){
		LatLng ltLng = new LatLng((double)(coord.getLatitude() / 1E6d), (double)(coord.getLongitude() / 1E6d));
		
		UTMRef utm = ltLng.toUTMRef();
		
		UTMRef utmUL = new UTMRef(utm.getEasting() - length, utm.getNorthing() + length, utm.getLatZone(), utm.getLngZone());
		UTMRef utmBR = new UTMRef(utm.getEasting() + length, utm.getNorthing() - length, utm.getLatZone(), utm.getLngZone());
		
		LatLng ltlngUL = utmUL.toLatLng();
		LatLng ltlngBR = utmBR.toLatLng();
		
		LocationPojo locUL = new LocationPojo((int)(ltlngUL.getLat() * 1E6), (int)(ltlngUL.getLng() * 1E6));
		LocationPojo locBR = new LocationPojo((int)(ltlngBR.getLat() * 1E6), (int)(ltlngBR.getLng() * 1E6));
		
		return new LocationPojo[]{locUL, locBR};
	}
	
}

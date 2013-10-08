package de.enterprise.lokaAndroid.fragments;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.activities.MainTabActivity;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.MyService.MapMode;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaServer.pojos.DoubleLocPojo;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.LocationPojo;
import de.enterprise.lokaServer.pojos.PostMapPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class MapFragment extends SherlockFragment {

	private GoogleMap myMap;
	public static final String TAG = "mapFragment";
	private IMyService myServiceBinder;
	private Map<String, Object> markerMap;
	private long lastOrderTime = 0;
	private int currentMarkerCount = 0;
	//10 seconds
	private final int MIN_WAIT_TIME = 1000*10;
	
	private class MyMarkerListener implements GoogleMap.OnMarkerClickListener{
		@Override
		public boolean onMarkerClick(Marker m) {
			Object mapped_obj = markerMap.get(m.getSnippet());
			if(mapped_obj instanceof PostMapPojo){
				switchToItem(((PostMapPojo)mapped_obj).getPostID());
			}else if(mapped_obj instanceof GroupPojo){
				switchToItem(((GroupPojo)mapped_obj).getId());
			}
			return true;
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message m) {
			Bundle data = m.getData();
			String json = data.getString("json");
			PostMapPojo[] postsArr = (PostMapPojo[]) JSONConverter
					.fromJSONArray(json, PostMapPojo[].class.getName());
			receivePosts(postsArr);
		}
	};
	
	private Handler groupHandler = new Handler() {
		public void handleMessage(Message m) {
			Bundle data = m.getData();
			String json = data.getString("json");
			GroupPojo[] groupArr = (GroupPojo[]) JSONConverter
					.fromJSONArray(json, GroupPojo[].class.getName());
			receiveGroups(groupArr);
		}
	};

	public void setBinder(IMyService msb) {
		myServiceBinder = msb;
		registerListener();
		if(myMap != null){
			if(myMap.getMyLocation() != null)
				orderPlacemarks();
		}
	}
	
	public void initFragment(){
		markerMap = new HashMap<String, Object>();
		initMap();
	}

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setRetainInstance(false);
	}
	
	@Override
	public void onDestroy(){
		unregisterListener();
		super.onDestroy();
	}
	
	@Override
	public void onPause(){
		if(myMap != null){
			myMap.setMyLocationEnabled(false);
		}
		super.onPause();
	}
	
	@Override
	public void onResume(){
		if(myMap != null){
			myMap.setMyLocationEnabled(true);
		}
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle data) {
		return inflater.inflate(R.layout.maplayout, null);
	}
	@Override
	public void onViewCreated(View v, Bundle b){
		initFragment();
	}
	
	
	private void receivePosts(PostMapPojo[] postsArr) {
		myMap.clear();
		for(PostMapPojo post : postsArr)
		{
			LatLng pos = new LatLng((double)(post.getLocation().getLatitude() / 1E6d),
					(double)(post.getLocation().getLongitude() / 1E6d));
			myMap.addMarker(new MarkerOptions()
            .position(pos)
            .snippet("p"+post.getPostID())
            .icon(BitmapDescriptorFactory.fromBitmap(ImageHelperAndroid.generateMarker(getActivity(), post))));
			markerMap.put("p"+post.getPostID(), post);
		}
	}
	
	private void receiveGroups(GroupPojo[] groupsArr) {
		myMap.clear();
		for(GroupPojo group : groupsArr)
		{
			LatLng pos = new LatLng((double)(group.getLocation().getLatitude() / 1E6d),
					(double)(group.getLocation().getLongitude() / 1E6d));
			myMap.addMarker(new MarkerOptions()
            .position(pos)
            .snippet("g"+group.getId())
            .icon(BitmapDescriptorFactory.fromBitmap(
            		ImageHelperAndroid.generateMarkerGroups(getActivity(), group, myServiceBinder))));
			markerMap.put("g"+group.getId(), group);
		}
	}

	private void registerListener() {
		if(myServiceBinder != null){
			myServiceBinder.registerListener("RPM", handler);
			myServiceBinder.registerListener("RGM", groupHandler);
		}
	}
	
	private void unregisterListener() {
		if(myServiceBinder != null){
			myServiceBinder.unregisterListener("RPM", handler);
			myServiceBinder.unregisterListener("RGM", groupHandler);
		}
	}

	public void resetTimer(){
		lastOrderTime = 0;
	}
	
	public void orderPlacemarks() {
		long deltaTime = System.currentTimeMillis() - lastOrderTime;
		if(deltaTime > MIN_WAIT_TIME){
			lastOrderTime = System.currentTimeMillis();
			if (myServiceBinder != null) {
				LatLng upLeft = myMap.getProjection().getVisibleRegion().farLeft;
				LatLng bottomRight = myMap.getProjection().getVisibleRegion().nearRight;
				MapMode mode = myServiceBinder.getMapMode();
				
				if(upLeft != null && bottomRight != null){
					JSONObject obj = new JSONObject();
					try {
						obj.put("L", JSONConverter.toJSON(new DoubleLocPojo(
								new LocationPojo((int)(upLeft.latitude * 1e6), (int)(upLeft.longitude * 1e6)),
								new LocationPojo((int)(bottomRight.latitude * 1e6), (int)(bottomRight.longitude * 1e6)))));
						if(mode == MapMode.Posts){
							obj.put("gID", myServiceBinder.getSelectedGroup().getId());
							obj.put("Que", myServiceBinder.getSearchWord());
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					Object obje = JSONConverter.fromJSON(obj.toString(),
							Object.class.getName());
					
					if(mode == MapMode.Posts){
						myServiceBinder.orderPosts(JSONConverter.toJSON(obje));
					}else{
						myServiceBinder.orderGroups(JSONConverter.toJSON(obje));
					}
				}
			}
		}
	}


	private void initMap() {
		FragmentManager myFragmentManager = getFragmentManager();
		SupportMapFragment myMapFragment = (SupportMapFragment)myFragmentManager.findFragmentById(R.id.mapContainer);
		if(myMapFragment != null){
			myMap = myMapFragment.getMap();
			if(myMap != null){
				myMap.setOnMarkerClickListener(new MyMarkerListener());
				myMap.setOnCameraChangeListener(new OnCameraChangeListener() {
					@Override
					public void onCameraChange(CameraPosition arg0) {
						orderPlacemarks();
					}
				});
				myMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
					@Override
					public void onMyLocationChange(Location loc) {
						if(myServiceBinder != null){
							if(myServiceBinder.getLocation() == null){
								jumpToPosition(loc);
							}
							myServiceBinder.setLastLocation(loc);
						}
					}
				});
				myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				myMap.setMyLocationEnabled(true);
				
				myMap.getUiSettings().setCompassEnabled(false);
				myMap.getUiSettings().setRotateGesturesEnabled(false);
				
				myMap.getUiSettings().setZoomGesturesEnabled(true);
				myMap.getUiSettings().setScrollGesturesEnabled(true);
				myMap.getUiSettings().setTiltGesturesEnabled(true);
			}
		}
	}

	public void switchToItem(int id) {
		MainTabActivity activity = (MainTabActivity) getActivity();
		activity.getSupportActionBar().selectTab(activity.getSupportActionBar().getTabAt(1));
		if(myServiceBinder.getMapMode() == MapMode.Posts){
			activity.selectPostInList(id);
		}else{
			activity.selectGroupInList(id);
		}
	}

	public void jumpToPosition(LocationPojo loc) {
		jumpToPosition(new LatLng((double)(loc.getLatitude() / 1E6d), (double)(loc.getLongitude() / 1E6d)));
	}
	
	public void jumpToPosition(Location loc) {
		jumpToPosition(new LatLng(loc.getLatitude(), loc.getLongitude()));
	}
	
	public void jumpToPosition(LatLng latlng) {
		resetTimer();
		myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, (myMap.getMaxZoomLevel() + myMap.getMinZoomLevel())/2), new CancelableCallback() {
			@Override
			public void onFinish() {
				orderPlacemarks();
			}
			@Override
			public void onCancel() {
				orderPlacemarks();
			}
		});
	}

	void goToLocation(final String location) {
		new Thread(){
			public void run(){
				Geocoder geocoder = new Geocoder(MapFragment.this.getActivity());
				List<Address> addresses;
				try {
					addresses = geocoder.getFromLocationName(location, 3);
					if(addresses.size() > 0){
						final Address adr = addresses.get(0);
						handler.post(new Runnable(){
							public void run(){
								jumpToPosition(new LatLng(adr.getLatitude(), adr.getLongitude()));
							}
						});
					}else{
						Toast.makeText(MapFragment.this.getActivity(), MapFragment.this.getResources().getString(R.string.location_not_found), Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e) {
					handler.post(new Runnable(){
						public void run(){
							Toast.makeText(MapFragment.this.getActivity(), MapFragment.this.getResources().getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}.start();
	}

//	public void centerPosition() {
//		if(myMap != null){
//			Location loc = myMap.getMyLocation();
//			jumpToPosition(new LatLng(loc.getLatitude(), loc.getLongitude()));
//		}
//	}
}
package de.enterprise.lokaAndroid.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;
import de.enterprise.lokaServer.pojos.DoubleLocPojo;
import de.enterprise.lokaServer.pojos.LocationPojo;
import de.enterprise.lokaServer.pojos.PostPojo;
import de.enterprise.lokaServer.pojos.RequestPostContentsPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class MapFragment extends SherlockFragment implements OnPanAndZoomListener{

	public static final String TAG = "mapFragment";
	private MapController mapController;
	private MyLocationOverlay myLocationOverlay;
	private CustomItemizedOverlay posts;
	private MyServiceBinder myServiceBinder;
	private Handler handler = new Handler(){
		public void handleMessage(Message m){
			Bundle data = m.getData();
			String json = data.getString("json");
			PostPojo[] postsArr = (PostPojo[]) JSONConverter.fromJSONArray(json, PostPojo[].class.getName());
			receivePosts(postsArr);
		}
	};
	private ServiceConnection serviceConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myServiceBinder = (MyServiceBinder) service;
			registerListener();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

	};
	
	public MapFragment() {}
	
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setRetainInstance(true);
		initMapView();
		initMyLocationOverlay();
		initPostOverlays();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle data) {
		return Exchanger.mMapView;
	}
	
	private void initMyLocationOverlay(){
		myLocationOverlay = new MyLocationOverlay(getActivity(), Exchanger.mMapView){
			@Override
			public void onLocationChanged(Location location){
				super.onLocationChanged(location);
			}
		};
		Exchanger.mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass();
		myLocationOverlay.runOnFirstFix(new Runnable(){
			public void run(){
				mapController.animateTo(myLocationOverlay.getMyLocation());
			}
		});
	}
	
	private void initPostOverlays(){
		posts = new CustomItemizedOverlay(this.getResources().getDrawable(R.drawable.ic_launcher), getActivity());
		Exchanger.mMapView.getOverlays().add(posts);	
	}
	
	private void registerListener(){
		myServiceBinder.registerListener("RP", handler);
	}
	
	public void orderPosts(){
		Rect rect = new Rect();
		Exchanger.mMapView.getLocalVisibleRect(rect);
		GeoPoint upLeft = Exchanger.mMapView.getProjection().fromPixels(rect.left, rect.top);
		GeoPoint bottomRight = Exchanger.mMapView.getProjection().fromPixels(rect.right, rect.bottom);
		RequestPostContentsPojo rpc = new RequestPostContentsPojo();
		rpc.setDblLoc(new DoubleLocPojo(new LocationPojo(upLeft.getLatitudeE6(),upLeft.getLongitudeE6()), new LocationPojo(bottomRight.getLatitudeE6(),bottomRight.getLongitudeE6())));
		rpc.setUserID(myServiceBinder.getUser().getId());
		myServiceBinder.orderPosts(rpc);
	}
	
	private void receivePosts(PostPojo[] postsArr){
		myServiceBinder.setVisiblePosts(postsArr);
		posts.deleteItems();
		for (PostPojo post : postsArr) {
			GeoPoint geo = new GeoPoint(post.getLoc().getLatitude(), post.getLoc().getLongitude());
			OverlayItem item = new OverlayItem(geo, ""+post.getId(), post.getMessage().getText());
			posts.addOverlay(item);
		}
	}
	
	private void initMapView(){
		Exchanger.mMapView.setListener(this);
		mapController = Exchanger.mMapView.getController();
		final int maxZoomLevel = Exchanger.mMapView.getMaxZoomLevel();
		mapController.setZoom(maxZoomLevel - 4);
		Exchanger.mMapView.setClickable(true);
		Exchanger.mMapView.setBuiltInZoomControls(true);
		Exchanger.mMapView.setSatellite(false);
	}
	
	public void onPause(){
		myLocationOverlay.disableMyLocation();
		myLocationOverlay.disableCompass();
		getActivity().unbindService(serviceConn);
		super.onPause();
	}
	
	public void onResume(){
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass();
		Exchanger.mMapView.invalidate();
		final Intent serviceIntent = new Intent(getActivity(), MyService.class);
		getActivity().bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
		super.onResume();
	}

	@Override
	public void onPan() {
		orderPosts();
	}

	@Override
	public void onZoom() {
		orderPosts();
	}
}
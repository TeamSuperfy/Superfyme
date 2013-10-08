package de.enterprise.lokaAndroid.fragments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockListFragment;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.adapters.NotificationsAdapter;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;
import de.enterprise.lokaAndroid.services.RamCache;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaServer.pojos.NotificationPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class NotificationsFragment extends SherlockListFragment {

	public static final String TAG = "myPostsFragment";
	private static final int TAB_INDEX = 1;
	private IMyService msb;
	private NotificationsAdapter adapter;
	private NotificationPojo[] notifsArray;
	private Button btnDeleteNotifs;
	
	private Handler notificationHandler = new Handler() {
		
		public void handleMessage(Message m) {
			Bundle data = m.getData();
			String json = data.getString("json");
			msb.cacheJSON("RN", json);
			new DecodeNotificationsTask().execute(json);
		}

	};
	
	private Handler imageHandler = new Handler() {
		
		public void handleMessage(Message m) {
			new DecodeImageTask().execute(m.getData());
		}
	};
	
	private class NotificationComparator implements Comparator<NotificationPojo>{
		@Override
		public int compare(NotificationPojo lhs, NotificationPojo rhs) {
			if(lhs.getDate() > rhs.getDate()){
				return -1;
			}
			else if(lhs.getDate() == rhs.getDate()){
				return 0;
			}
			else{
				return 1;
			}
		}
	}
	
	private class DecodeImageTask extends AsyncTask<Bundle, Void, Void> {

		@Override
		protected Void doInBackground(Bundle... data) {
			Bundle b = data[0];
			String json = b.getString("json");
			LinkedHashMap<Object, Object> obj = (LinkedHashMap<Object, Object>) JSONConverter.fromJSON(json, Object.class.getName());
			int picID = (Integer) obj.get("pID");
			Bitmap bmp = ImageHelperAndroid.getBitmapFromByteArray(b.getByteArray("img"));
			bmp = ImageHelperAndroid.getRoundedCornerBitmap(
					NotificationsFragment.this.getActivity(), bmp, 3, bmp.getWidth(), bmp.getHeight(), false,
					false, false, false);
			msb.addImageToCache(picID, bmp, RamCache.THUMB);
			imageHandler.post(new Runnable(){
				public void run(){
					adapter.notifyDataSetChanged();
				}
			});
			return null;
		}
	 }
	
	private class DecodeNotificationsTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String json = params[0];
			final NotificationPojo[] notifsArr = (NotificationPojo[]) JSONConverter
					.fromJSONArray(json, NotificationPojo[].class.getName());
			notifsArray = notifsArr;
			Integer[] requiredPicIDs = checkPics(notifsArr);
			if (requiredPicIDs.length > 0)
				msb.orderThumbnailPictures(requiredPicIDs);
			notificationHandler.post(new Runnable(){
				public void run(){
					adapter = new NotificationsAdapter(getActivity(),
							new ArrayList<NotificationPojo>(), NotificationsFragment.this, msb);
					setListAdapter(adapter);
					if(notifsArr.length > 0){
						btnDeleteNotifs.setEnabled(true);
						updateTabTitle(countUnread(notifsArr));
						adapter.addAllTrick(notifsArr);
						adapter.sort(new NotificationComparator());
					}else{
						btnDeleteNotifs.setEnabled(false);
						updateTabTitle(0);
						adapter.clear();
						NotificationPojo infoMessage = new NotificationPojo();
						infoMessage.setId(-2);
						adapter.add(infoMessage);
					}
				}
			});
			return null;
		}
		
		private int countUnread(NotificationPojo[] notifsArr) {
			int unread = 0;
			for (NotificationPojo notifPojo : notifsArr) {
				if(!notifPojo.isRead())
					unread++;
			}
			return unread;
		}
		
		private Integer[] checkPics(NotificationPojo[] notifs) {
			ArrayList<Integer> required = new ArrayList<Integer>();
			for (NotificationPojo notif : notifs) {
				if(notif.getPic_id() > 0){
					if (!msb.isImageCached(notif.getPic_id(), RamCache.THUMB)) {
						required.add(notif.getPic_id());
					}
				}
			}
			Integer[] ret = new Integer[required.size()];
		    Iterator<Integer> iterator = required.iterator();
		    for (int i = 0; i < ret.length; i++)
		    {
		        ret[i] = iterator.next().intValue();
		    }

			return ret;
		}
	}
	
	public void setBinder(MyServiceBinder msb){
		this.msb = msb;
		msb.registerListener("RN", notificationHandler);
		msb.orderNotifications();
		
		final String rnJSON = msb.getCachedJSON("RN");
		if(rnJSON != null){
			notificationHandler.post(new Runnable(){
				public void run(){
					new DecodeNotificationsTask().execute(rnJSON);
				}
			});
		}
		
	}
	
	@Override
	public void onResume(){
		if(msb != null){
			adapter.clear();
			msb.orderNotifications();
		}
		super.onResume();
	}
	
	@Override
	public void onPause(){
		if(msb != null){
			adapter.clear();
		}
		super.onPause();
	}

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setRetainInstance(true);
		adapter = new NotificationsAdapter(getActivity(),
				new ArrayList<NotificationPojo>(), this, msb);
		this.setListAdapter(adapter);
	}
	
	@Override
	public void onDestroy(){
		unregisterListener();
		adapter.clear();
		super.onDestroy();
	}
	
	private void registerListener() {
		msb.registerListener("RTHI", imageHandler);
		msb.registerListener("RN", notificationHandler);
	}
	
	private void unregisterListener() {
		if(msb != null){
			msb.unregisterListener("RN", notificationHandler);
			msb.unregisterListener("RTHI", imageHandler);
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		if(!hidden){
			if(msb != null){
				registerListener();
				msb.orderNotifications();
				//showLoading();
			}
		}
		else{
			if(msb != null){
				msb.unregisterListener("RTHI", imageHandler);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle data) {
		// Inflate the ListView layout file.
		View v = inflater.inflate(R.layout.messages_list, null);
		btnDeleteNotifs = (Button) v.findViewById(R.id.btnClear);
		btnDeleteNotifs.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				onClickClear();
			}
			
		});
		return v;
	}
	
	public void readNotification(int notifID){
		if(msb != null){
			msb.readNotification(notifID);
		}
	}
	
	private void onClickClear(){
		msb.deleteNotifications(notifsArray);
		adapter.clear();
		adapter = new NotificationsAdapter(getSherlockActivity(), new ArrayList<NotificationPojo>(), this, msb);
		updateTabTitle(0);
		btnDeleteNotifs.setEnabled(false);
	}
	
	private void updateTabTitle(int unread){
		if(unread > 0)
			getSherlockActivity().getSupportActionBar().getTabAt(TAB_INDEX).setText("("+unread+")");
		else
			getSherlockActivity().getSupportActionBar().getTabAt(TAB_INDEX).setText("");
	}

}
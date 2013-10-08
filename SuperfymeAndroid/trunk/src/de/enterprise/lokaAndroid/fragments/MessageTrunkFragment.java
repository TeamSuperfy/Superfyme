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

import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockListFragment;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.adapters.MessagesTrunkAdapter;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;
import de.enterprise.lokaAndroid.services.RamCache;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class MessageTrunkFragment extends SherlockListFragment{
	
	public static final String TAG = "messagesFragment";
	private static final int TAB_INDEX = 0;
	private IMyService msb;
	private MessagesTrunkAdapter adapter;
	private Button btnDeleteMessages;
	
	private Handler messageTrunkHandler = new Handler() {
		
		public void handleMessage(Message m) {
			Bundle data = m.getData();
			String json = data.getString("json");
			msb.cacheJSON("RMT", json);
			new DecodeMessageTrunksTask().execute(json);
		}

	};
	
	private Handler imageHandler = new Handler() {
		
		public void handleMessage(Message m) {
			if(getSherlockActivity().getSupportActionBar().getSelectedNavigationIndex() == TAB_INDEX){
				new DecodeImageTask().execute(m.getData());
			}
		}
	};

	
	private class MessageComparator implements Comparator<MessagePojo>{
		@Override
		public int compare(MessagePojo lhs, MessagePojo rhs) {
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
	
	private class DecodeMessageTrunksTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String json = params[0];
			final MessagePojo[] messageArr = (MessagePojo[]) JSONConverter
					.fromJSONArray(json, MessagePojo[].class.getName());
			
			Integer[] requiredPicIDs = checkPics(messageArr);
			if (requiredPicIDs.length > 0)
				msb.orderThumbnailPictures(requiredPicIDs);
			
			messageTrunkHandler.post(new Runnable(){
				public void run(){
					adapter = new MessagesTrunkAdapter(getActivity(),
							new ArrayList<MessagePojo>(), msb);
					setListAdapter(adapter);
					if(messageArr.length > 0){
						btnDeleteMessages.setEnabled(true);
						int unread = countUnread(messageArr);
						updateTabTitle(unread);
						adapter.addAllTrick(messageArr);
						adapter.sort(new MessageComparator());
					}else{
						btnDeleteMessages.setEnabled(false);
						updateTabTitle(0);
						adapter.clear();
					}
				}

				private int countUnread(MessagePojo[] messageArr) {
					int unread = 0;
					for (MessagePojo messagePojo : messageArr) {
						if(messagePojo.getTo() == msb.getUser().getId()){
							if(!messagePojo.isRead())
								unread++;
						}
					}
					return unread;
				}
			});
			return null;
		}
		
		private Integer[] checkPics(MessagePojo[] messages) {
			ArrayList<Integer> required = new ArrayList<Integer>();
			for (MessagePojo message : messages) {
				if(message.getPicID() > 0){
					if (!msb.isImageCached(message.getPicID(), RamCache.THUMB)) {
						required.add(message.getPicID());
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
	
	private class DecodeImageTask extends AsyncTask<Bundle, Void, Void> {

		@Override
		protected Void doInBackground(Bundle... data) {
			Bundle b = data[0];
			String json = b.getString("json");
			LinkedHashMap<Object, Object> obj = (LinkedHashMap<Object, Object>) JSONConverter.fromJSON(json, Object.class.getName());
			int picID = (Integer) obj.get("pID");
			Bitmap bmp = ImageHelperAndroid.getBitmapFromByteArray(b.getByteArray("img"));
			bmp = ImageHelperAndroid.getRoundedCornerBitmap(
					MessageTrunkFragment.this.getActivity(), bmp, 3, bmp.getWidth(), bmp.getHeight(), false,
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
	
	public void setBinder(MyServiceBinder msb){
		adapter.clear();
		this.msb = msb;
		unregisterListener();
		registerListener();
		msb.orderMessageTrunks();
		
		final String mtrJSON = msb.getCachedJSON("RMT");
		if(mtrJSON != null){
			messageTrunkHandler.post(new Runnable(){
				public void run(){
					new DecodeMessageTrunksTask().execute(mtrJSON);
				}
			});
		}
		
		showLoading();
	}
	
	@Override
	public void onResume(){
		if(msb != null){
			adapter.clear();
			msb.orderMessageTrunks();
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
		adapter = new MessagesTrunkAdapter(getActivity(),
				new ArrayList<MessagePojo>(), msb);
		this.setListAdapter(adapter);
	}
	
	@Override
	public void onDestroy(){
		unregisterListener();
		adapter.clear();
		super.onDestroy();
	}

	private void registerListener() {
		msb.registerListener("RMT", messageTrunkHandler);
		msb.registerListener("RTHI", imageHandler);
	}
	
	private void unregisterListener() {
		if(msb != null){
			msb.unregisterListener("RTHI", imageHandler);
			msb.unregisterListener("RMT", messageTrunkHandler);
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		if(!hidden){
			if(msb != null){
				msb.registerListener("RTHI", imageHandler);
				msb.orderMessageTrunks();
				//showLoading();
			}
		}
		else{
			if(msb != null){
				msb.unregisterListener("RTHI", imageHandler);
			}
		}
	}

	private void showLoading() {
		//TODO implement loading hint
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle data) {
		// Inflate the ListView layout file.
		View v = inflater.inflate(R.layout.messages_list, null);
		btnDeleteMessages = (Button) v.findViewById(R.id.btnClear);
		btnDeleteMessages.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				onClickClear();
			}
			
		});
		return v;
	}
	
	private void onClickClear() {
		msb.deleteAllMessages();
		adapter.clear();
		adapter = new MessagesTrunkAdapter(getSherlockActivity(), new ArrayList<MessagePojo>(), msb);
		updateTabTitle(0);
		btnDeleteMessages.setEnabled(false);
	}
	
	private void updateTabTitle(int unread){
		if(unread > 0){
			Tab tab = getSherlockActivity().getSupportActionBar().getTabAt(TAB_INDEX);
			tab.setText("("+unread+")");
			tab.setIcon(R.drawable.mail);
		}
		else{
			Tab tab = getSherlockActivity().getSupportActionBar().getTabAt(TAB_INDEX);
			tab.setText("");
			tab.setIcon(R.drawable.old_mails);
		}
	}
	
}

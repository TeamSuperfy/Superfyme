package de.enterprise.lokaAndroid.activities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockListActivity;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.adapters.MessagesAdapter;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class ChatActivity extends SherlockListActivity{
	
	private IMyService msb;
	private MessagesAdapter adapter;
	private Button btnSendMessage;
	private EditText edtMessageText;
	private int otherUID;
	//message containing postID/commentID of referenced post/comment
	private MessagePojo msg;
	private GroupPojo inviting_group;
	
	private ServiceConnection sc = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			setBinder((IMyService)binder);
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	};
	
	private Handler messageHandler = new Handler() {
		
		public void handleMessage(Message m) {
			Bundle data = m.getData();
			String json = data.getString("json");
			msb.cacheJSON("RM"+otherUID, json);
			new DecodeMessagesTask().execute(json);
		}

	};
	
	private Handler incomingHandler = new Handler() {
		
		public void handleMessage(Message m) {
			Bundle data = m.getData();
			String json = data.getString("json");
			MessagePojo mp = (MessagePojo) JSONConverter.fromJSON(json, MessagePojo.class.getName());
			adapter.add(mp);
		}

	};
	
	private Handler groupHandler = new Handler() {
		
		public void handleMessage(Message m) {
			Bundle data = m.getData();
			String json = data.getString("json");
			GroupPojo[] groups = (GroupPojo[]) JSONConverter.fromJSON(json, GroupPojo[].class.getName());
			inviting_group = groups[0];
			groupHandler.post(new Runnable(){
				@Override
				public void run() {
					adapter.notifyDataSetChanged();
				}
			});
		}

	};
	
	private Handler groupImageHandler = new Handler() {
		
		public void handleMessage(Message m) {
			Bundle b = m.getData();
			String json = b.getString("json");
			LinkedHashMap<Object, Object> obj = (LinkedHashMap<Object, Object>) JSONConverter.fromJSON(json, Object.class.getName());
			int picID = (Integer) obj.get("pID");
			Bitmap bmp = ImageHelperAndroid.getBitmapFromByteArray(b.getByteArray("img"));
			msb.saveUserPic(picID, bmp);
			groupHandler.post(new Runnable(){
				@Override
				public void run() {
					adapter.notifyDataSetChanged();
				}
			});
		}

	};
	
	private class MessageComparator implements Comparator<MessagePojo>{
		@Override
		public int compare(MessagePojo lhs, MessagePojo rhs) {
			if(lhs.getDate() > rhs.getDate()){
				return 1;
			}
			else if(lhs.getDate() == rhs.getDate()){
				return 0;
			}
			else{
				return -1;
			}
		}
	}
	
	private class DecodeMessagesTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String json = params[0];
			final MessagePojo[] messageArr = (MessagePojo[]) JSONConverter
					.fromJSONArray(json, MessagePojo[].class.getName());
			
			messageHandler.post(new Runnable(){
				public void run(){
					adapter = new MessagesAdapter(ChatActivity.this,
							new ArrayList<MessagePojo>(), msb, ChatActivity.this);
					setListAdapter(adapter);
					if(messageArr.length > 0){
						adapter.addAllTrick(messageArr);
						adapter.sort(new MessageComparator());
						getListView().setSelection(adapter.getCount() - 1);
					}
				}
			});
			return null;
		}
	}
	
	public void setBinder(IMyService msb){
		adapter.clear();
		this.msb = msb;
		unregisterListener();
		registerListener();
		if(msg.getFrom() > 0){
			adapter.setIMyService(msb);
			msb.orderMessages(otherUID, msg.getPostID(), msg.getCommentID());
			
			final String rmJSON = msb.getCachedJSON("RM");
			if(rmJSON != null){
				messageHandler.post(new Runnable(){
					public void run(){
						new DecodeMessagesTask().execute(rmJSON);
					}
				});
			}
			
			showLoading();
		}else if(msg.getFrom() == -1){
			//group invitation
			adapter.setIMyService(msb);
			msb.requestGroups(new Integer[]{Integer.parseInt(msg.getInfo())});
			adapter.add(msg);
		}
	}
	
	@Override
	public void onResume(){
		if(msb != null){
			adapter.clear();
			if(msg.getFrom() != -1){
				msb.orderMessages(otherUID, msg.getPostID(), msg.getCommentID());
			}else{
				adapter.add(msg);
			}
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
		getSupportActionBar().hide();
		otherUID = getIntent().getExtras().getInt("other");
		msg = (MessagePojo) getIntent().getExtras().getSerializable("msg");
		
		adapter = new MessagesAdapter(this,
				new ArrayList<MessagePojo>(), msb, this);
		this.setListAdapter(adapter);
		
		setContentView(R.layout.chat_layout);
		edtMessageText = (EditText) findViewById(R.id.edtMessageText);
		edtMessageText.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable txt) {
				if(txt.length() > 0){
					if(otherUID != -1){
						btnSendMessage.setEnabled(true);
					}
				}
				else{
					btnSendMessage.setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
			
		});
		btnSendMessage = (Button) findViewById(R.id.btnSendMessage);
		btnSendMessage.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				sendMessage();
			}
			
		});
		
		if(otherUID == -1 || msg.getFrom() == -1){
			btnSendMessage.setEnabled(false);
		}
		
		Intent serviceIntent = new Intent(this, MyService.class);
		bindService(serviceIntent, sc, 0);
	}
	
	@Override
	public void onDestroy(){
		unregisterListener();
		unbindService(sc);
		adapter.clear();
		super.onDestroy();
	}
	
	private void sendMessage(){
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(edtMessageText.getWindowToken(), 0);
		
		MessagePojo mp = new MessagePojo();
		mp.setTo(otherUID);
		mp.setFrom(msb.getUser().getId());
		mp.setText(edtMessageText.getText().toString());
		if(msg.getPostID() > 0){
			mp.setPostID(msg.getPostID());
		}else{
			mp.setCommentID(msg.getCommentID());
		}
		mp.setPicID(msg.getPicID());
		mp.setRead(false);
		mp.setAnonymous(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("anonymous", false));
		mp.setUsername(msb.getUser().getUsername());
		msb.sendMessage(mp);
		adapter.add(mp);
		edtMessageText.setText("");
	}

	private void registerListener() {
		msb.registerListener("RM", messageHandler);
		msb.registerListener("REM", incomingHandler);
		msb.registerListener("RFI", groupImageHandler);
		msb.registerListener("RGR", groupHandler);
	}
	
	private void unregisterListener() {
		if(msb != null){
			msb.unregisterListener("RM", messageHandler);
			msb.unregisterListener("REM", incomingHandler);
			msb.unregisterListener("RFI", groupImageHandler);
			msb.unregisterListener("RGR", groupHandler);
		}
	}

	private void showLoading() {
		//TODO implement loading
	}
	
	public GroupPojo getGroup(){
		return inviting_group;
	}
}

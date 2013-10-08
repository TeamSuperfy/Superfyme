package de.enterprise.lokaAndroid.activities;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.adapters.UserAdapter;
import de.enterprise.lokaAndroid.fragments.InviteUserDialog;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;
import de.enterprise.lokaServer.tools.JSONConverter;

public class GroupInfo extends SherlockFragmentActivity{

	private ImageView imgGroupIcon;
	private TextView lblGroupname;
	private Button btnInvite;
	private ListView listMembers;
	private Button btnOk;
	
	private IMyService msb;
	private GroupPojo group;
	private UserAdapter adapter;
	
	private ServiceConnection sc = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			msb = (IMyService) binder;
			registerListener();
			Bitmap bmp = msb.getIconPic(group.getPic_id());
			if(bmp != null){
				imgGroupIcon.setImageBitmap(bmp);
			}else{
				msb.orderBigPicture(group.getPic_id());
			}
			msb.requestUserPojos(CollectionStuff.intToIntegerArray(
					CollectionStuff.appendToArray(group.getMembers(), group.getInvited_users())
					));
			
			if(group.getCreator_id() == msb.getUser().getId()){
				btnInvite.setVisibility(View.VISIBLE);
			}
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	};
	
	private Handler iconHandler = new Handler() {
		
		public void handleMessage(Message m) {
			Bundle b = m.getData();
			String json = b.getString("json");
			LinkedHashMap<Object, Object> obj = (LinkedHashMap<Object, Object>) JSONConverter.fromJSON(json, Object.class.getName());
			int picID = (Integer) obj.get("pID");
			Bitmap bmp = ImageHelperAndroid.getBitmapFromByteArray(b.getByteArray("img"));
			bmp = ImageHelperAndroid.getRoundedCornerBitmap(
					GroupInfo.this, bmp, 3, bmp.getWidth(), bmp.getHeight(), false,
					false, false, false);
			msb.saveUserPic(picID, bmp);
			userHandler.post(new Runnable(){
				@Override
				public void run() {
					adapter.notifyDataSetChanged();
				}
			});
		}

	};
	
	private Handler userHandler = new Handler() {
		
		public void handleMessage(Message m) {
			Bundle b = m.getData();
			String json = b.getString("json");
			final UserPojo[] userArr = (UserPojo[]) JSONConverter
					.fromJSONArray(json, UserPojo[].class.getName());
			iconHandler.post(new Runnable(){
				public void run(){
					adapter = new UserAdapter(GroupInfo.this, new ArrayList<UserPojo>(), msb, group);
					listMembers.setAdapter(adapter);
					adapter.addAllTrick(userArr);
				}
			});
		}

	};
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		group = (GroupPojo) getIntent().getExtras().getSerializable("group");
		adapter = new UserAdapter(this, new ArrayList<UserPojo>(), msb, group);
		getSupportActionBar().hide();
		setContentView(R.layout.manage_group);
		imgGroupIcon = (ImageView) findViewById(R.id.imgGroupIcon);
		lblGroupname = (TextView) findViewById(R.id.lblGroupname);
		btnInvite = (Button) findViewById(R.id.btnInvite);
		btnInvite.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				 DialogFragment newFragment = InviteUserDialog.newInstance(msb, group.getId(), GroupInfo.this);
				    newFragment.show(getSupportFragmentManager(), "dialog");
			}
		});
		listMembers = (ListView) findViewById(R.id.listMembers);
		listMembers.setAdapter(adapter);
		btnOk = (Button) findViewById(R.id.btnOK);
		btnOk.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//TODO accept changes
				finish();
			}
		});
		
		lblGroupname.setText(group.getName());
		
		TextView lblMembers = (TextView) findViewById(R.id.lblMembers);
		lblMembers.setText(getResources().getString(R.string.members)+" ("+group.getMember_count()+"):");
		
		
		
		Intent serviceIntent = new Intent(this, MyService.class);
		bindService(serviceIntent, sc, 0);
	}
	
	@Override
	public void onDestroy(){
		unregisterListener();
		unbindService(sc);
		super.onDestroy();
	}
	
	private void registerListener(){
		if(msb != null){
			msb.registerListener("RFI", iconHandler);
			msb.registerListener("RUP", userHandler);
		}
	}
	
	private void unregisterListener(){
		if(msb != null){
			msb.unregisterListener("RFI", iconHandler);
			msb.unregisterListener("RUP", userHandler);
		}
	}
	
	public void addInvitedUser(int userID){
		group.setInvited_users(CollectionStuff.appendToArray(group.getInvited_users(), new int[]{userID}));
		msb.requestUserPojos(CollectionStuff.intToIntegerArray(
				CollectionStuff.appendToArray(group.getMembers(), group.getInvited_users())
				));
	}
	
}

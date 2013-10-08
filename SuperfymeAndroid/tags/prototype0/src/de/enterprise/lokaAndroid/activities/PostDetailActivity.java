package de.enterprise.lokaAndroid.activities;

import java.io.ByteArrayInputStream;
import java.util.Date;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;
import de.enterprise.lokaServer.pojos.PostPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class PostDetailActivity extends Activity{

	private ImageView imgView;
	private TextView timeStamp, userID, text;
	private int postID;
	private MyServiceBinder myServiceBinder;
	
	private Handler handler = new Handler(){
		public void handleMessage(Message m){
			Bundle data = m.getData();
			String json = data.getString("json");
			fillInformation(json);
		}
	};
	
	private Handler imagehandler = new Handler(){
		public void handleMessage(Message m){
			Bundle data = m.getData();
			String image = data.getString("json");
			loadImage(image);
		}
	};
	private ServiceConnection serviceConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myServiceBinder = (MyServiceBinder) service;
			registerListener();
			loadContent();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

	};
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		final Intent serviceIntent = new Intent(this, MyService.class);
		bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
		
		setContentView(R.layout.post_detail);
		imgView = (ImageView) findViewById(R.id.userImage);
		timeStamp = (TextView) findViewById(R.id.timestamp);
		userID = (TextView) findViewById(R.id.userID);
		text = (TextView) findViewById(R.id.userText);

		Bundle extras = getIntent().getExtras();
		postID = extras.getInt("id");
	}
	
	public void onResume(){
		final Intent serviceIntent = new Intent(this, MyService.class);
		bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
		super.onResume();
	}
	
	public void onPause(){
		unbindService(serviceConn);
		super.onPause();
	}
	
	private void registerListener(){
		myServiceBinder.registerListener("IMG", imagehandler);
		myServiceBinder.registerListener("RPD", handler);
	}
	
	private void loadContent(){
		myServiceBinder.orderPostDetail(myServiceBinder.getUser().getId(), postID);
	}
	
	private void fillInformation(String json) {
		PostPojo post = (PostPojo) JSONConverter.fromJSON(json, PostPojo.class.getName());
		timeStamp.setText(new Date(post.getMessage().getDate()).toString());
		userID.setText(""+post.getMessage().getUser().getId());
		text.setText(post.getMessage().getText());
	}
	
	private void loadImage(String image) {
		byte[] img;
		img = Base64.decode(image, Base64.DEFAULT);
		ByteArrayInputStream in = new ByteArrayInputStream(img);
		Bitmap bmp = BitmapFactory.decodeStream(in);
		imgView.setImageBitmap(bmp);
	}
	
}

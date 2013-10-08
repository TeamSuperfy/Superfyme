package de.enterprise.lokaAndroid.activities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;
import de.enterprise.lokaServer.pojos.LocationPojo;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.pojos.PostPojo;

public class PostActivity extends Activity{
	
	private Handler handler;
	private EditText edt_text;
	private ImageView imgView;
	MyServiceBinder myServiceBinder;
	private Bitmap image;
	ServiceConnection serviceConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myServiceBinder = (MyServiceBinder) service;
			registerListener();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

	};

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post);
		edt_text = (EditText) findViewById(R.id.editText1);
		imgView = (ImageView) findViewById(R.id.imageView1);
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
	
	public void onClickPicture(final View view){
		Intent cameraIntent = new Intent(this,CameraSurface.class);
		startActivityForResult(cameraIntent, 0);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		Bundle extras = data.getExtras();
		byte[] img = extras.getByteArray("img");
		InputStream in = new ByteArrayInputStream(img);
		BitmapDrawable bmp = new BitmapDrawable(in);
		image = bmp.getBitmap();
		imgView.setImageDrawable(bmp);
	}
	
	public void onClickCancel(final View view){
		finish();
	}
	
	public void onClickSend(final View view){
		if(checkForm()){
			myServiceBinder.sendPost(generatePost(), image);
			Toast.makeText(this, "sent post", Toast.LENGTH_SHORT);
			final Intent main = new Intent(this, MainActivity.class);
			startActivity(main);
		}
	}
	
	private boolean checkForm(){
		if(myServiceBinder.getLocation() != null &&
				image != null)
			return true;
		return false;
	}
	
	private PostPojo generatePost(){
		PostPojo p = new PostPojo();
		Location loc = myServiceBinder.getLocation();
		GeoPoint geo = new GeoPoint((int)(loc.getLatitude() * 1E6),(int) (loc.getLongitude() * 1E6));
		p.setLoc(new LocationPojo(geo.getLatitudeE6(), geo.getLongitudeE6()));
		MessagePojo msg = new MessagePojo();
		msg.setDate(System.currentTimeMillis());
		msg.setText(edt_text.getText().toString());
		msg.setUser(myServiceBinder.getUser());
		p.setMessage(msg);
		return p;
	}
	
	private void registerListener() {
	}
	
}

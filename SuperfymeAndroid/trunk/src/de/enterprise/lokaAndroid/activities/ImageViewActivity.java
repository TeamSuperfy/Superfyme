package de.enterprise.lokaAndroid.activities;

import java.util.LinkedHashMap;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockActivity;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.RamCache;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaServer.tools.JSONConverter;

public class ImageViewActivity extends SherlockActivity{

	private ImageView imgView;
	private int picID;
	private MyServiceBinder myServiceBinder;
	private Handler imageHandler = new Handler(){
		
		public void handleMessage(Message m){
			new DecodeImageTask().execute(m.getData());
		}
	};
	
	private class DecodeImageTask extends AsyncTask<Bundle, Void, Void> {

		@Override
		protected Void doInBackground(Bundle... params) {
			Bundle b = params[0];
			String json = b.getString("json");
			LinkedHashMap<Object, Object> obj = (LinkedHashMap<Object, Object>) JSONConverter.fromJSON(json, Object.class.getName());
			final Bitmap bitmap = ImageHelperAndroid.getBitmapFromByteArray(b.getByteArray("img"));
			myServiceBinder.addImageToCache((Integer)obj.get("pID"), bitmap, RamCache.BIG);
			imageHandler.post(new Runnable(){
				public void run(){
					setImage(bitmap);
				}
			});
			return null;
		}
	}
	
	private ServiceConnection serviceConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myServiceBinder = (MyServiceBinder) service;
			Bitmap big = myServiceBinder.getImage(picID, RamCache.BIG);
			Bitmap tiny;
			if(big != null){
				setImage(big);
			}else if((tiny = myServiceBinder.getImage(picID, RamCache.TINY)) != null){
				setImage(tiny);
				//myServiceBinder.registerListener("RFI", imageHandler);
				//myServiceBinder.orderBigPicture(picID);
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

	};
	
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.big_image);
		final Intent serviceIntent = new Intent(this, MyService.class);
		bindService(serviceIntent, serviceConn, 0);
		
		getSupportActionBar().hide();
		
		picID = getIntent().getExtras().getInt("picID");
		imgView = (ImageView) findViewById(R.id.imgBigImage);
		imgView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void setImage(Bitmap bitmap){
		boolean portrait = ImageHelperAndroid.getOrientation(bitmap);
		if(!portrait){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		imgView.setImageBitmap(bitmap);
	}
	
	@Override
	public void onDestroy(){
		//myServiceBinder.unregisterListener("RFI", imageHandler);
		unbindService(serviceConn);
		imgView.getDrawable().setCallback(null);
		imgView.setImageDrawable(null);
		super.onDestroy();
	}
	
}

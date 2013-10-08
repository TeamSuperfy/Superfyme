package de.enterprise.lokaAndroid.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;

public class MainActivity extends Activity {
	
	private TextView connectionText;
	private ProgressBar connectionProgress;

	MyServiceBinder myServiceBinder;
	
	ServiceConnection serviceConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myServiceBinder = (MyServiceBinder) service;
			registerStateListener();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

	};
	
	Handler stateHandler = new Handler(){
		public void handleMessage(Message msg){
			Bundle data = msg.getData();
			if(data.containsKey(MyService.STATUS_CONNECTION)){
				changeConnectionStatus(data.getString(MyService.STATUS_CONNECTION));
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);
		connectionText = (TextView) findViewById(R.id.textView1);
		connectionProgress = (ProgressBar) findViewById(R.id.progressBar1);
		final Intent serviceIntent = new Intent(this, MyService.class);
		bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
	}
	
	public void onResume(){
		final Intent serviceIntent = new Intent(this, MyService.class);
		bindService(serviceIntent, serviceConn, 0);
		super.onResume();
	}
	
	public void onPause(){
		//unbindService(serviceConn);
		super.onPause();
	}
	
	public void onClickMap(final View view){
		final Intent intent = new Intent(this, MapswithfragmentsActivity.class);
		startActivity(intent);
	}
	
	public void onClickPost(final View view){
		final Intent intent = new Intent(this, PostActivity.class);
		startActivity(intent);
	}
	
	private void registerStateListener(){
		myServiceBinder.registerStateListener(stateHandler);
	}
	
	private void changeConnectionStatus(String status){
		if(status.equals(MyService.CONNECTION_ESTABLISHED)){
			connectionProgress.setVisibility(View.INVISIBLE);
			connectionText.setText("connected");
		}
		else if(status.equals(MyService.CONNECTING)){
			connectionProgress.setVisibility(View.VISIBLE);
			connectionText.setText("connecting...");
		}
	}

}

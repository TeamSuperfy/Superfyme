package de.enterprise.lokaAndroid.activities;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.fragments.NewUserDialog;
import de.enterprise.lokaAndroid.fragments.NewUserDialog.NewUserDialogCallback;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class MainActivity extends FragmentActivity implements NewUserDialogCallback{

	private MyServiceBinder myServiceBinder;
	private boolean launched = false;
	
	private ServiceConnection serviceConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myServiceBinder = (MyServiceBinder) service;
			myServiceBinder.registerListener("GUU", userHandler);
			myServiceBinder.getUserUpdate();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

	};
	
	private Handler userHandler = new Handler(){
		public void handleMessage(Message msg){
			Bundle data = msg.getData();
			String json = data.getString("json");
			UserPojo user = (UserPojo) JSONConverter.fromJSON(json, UserPojo.class.getName());
			myServiceBinder.updateUser(user);
			if(user.getUsername() == null){
				//show dialog
				 DialogFragment newFragment = NewUserDialog.newInstance(myServiceBinder);
				    newFragment.show(MainActivity.this.getSupportFragmentManager(), "dialog");
			}else{
				finishedInput();
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startscreen);
		final Intent serviceIntent = new Intent(this, MyService.class);
		startService(serviceIntent);
		bindService(serviceIntent, serviceConn, 0);
	}
	
	public void onDestroy(){
		myServiceBinder.unregisterListener("GUU", userHandler);
		unbindService(serviceConn);
		if(!launched){
			stopService(new Intent(this, MyService.class));
		}
		super.onDestroy();
	}
	
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

	@Override
	public void finishedInput() {
		launched = true;
		Intent intent = new Intent(MainActivity.this, MainTabActivity.class);
		startActivity(intent);
		finish();
	}

}

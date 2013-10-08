package de.enterprise.lokaAndroid.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;

@SuppressWarnings("ucd")
public class FeedbackActivity extends SherlockActivity {

	private IMyService myServiceBinder;
	private EditText edtFeedback;
	
	private ServiceConnection sc = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myServiceBinder = (MyServiceBinder) service;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
	};
	
	@Override
	public void onDestroy(){
		unbindService(sc);
		super.onDestroy();
	}
	
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.feedback_layout);
		getSupportActionBar().hide();
		edtFeedback = (EditText) findViewById(R.id.edtFeedbackText);
		Intent serviceIntent = new Intent(this, MyService.class);
		bindService(serviceIntent, sc, 0);
	}
	
	@SuppressWarnings("ucd")
	public void onClickSendFeedback(final View view){
		myServiceBinder.sendFeedback(edtFeedback.getText().toString());
		finish();
	}
	
}

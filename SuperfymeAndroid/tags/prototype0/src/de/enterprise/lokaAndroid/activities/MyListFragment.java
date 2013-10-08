package de.enterprise.lokaAndroid.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;
import de.enterprise.lokaServer.pojos.PostPojo;

public class MyListFragment extends SherlockListFragment {

	public static final String TAG = "listFragment";
	private MyServiceBinder myServiceBinder;
	
	private ServiceConnection serviceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myServiceBinder = (MyServiceBinder) service;
			initView();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};
	
	public MyListFragment() {}
	
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setRetainInstance(true);
	}
	
	public void onPause(){
		getActivity().unbindService(serviceConn);
		super.onPause();
	}
	
	public void onResume(){
		final Intent serviceIntent = new Intent(getActivity(), MyService.class);
		getActivity().bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
		super.onResume();
	}
	
	public void initView(){
		setListAdapter(new MobileArrayAdapter(getActivity(), myServiceBinder.getVisiblePosts()));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle data) {
		// Inflate the ListView layout file.
		return inflater.inflate(R.layout.list_fragment, null);
	}

	@Override
	public void onViewCreated(View arg0, Bundle arg1) {
		super.onViewCreated(arg0, arg1);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long idd) {
	      PostPojo post = (PostPojo)getListAdapter().getItem(position);
	      int id = post.getId();
	      Intent detail = new Intent(getActivity(), PostDetailActivity.class);
	      detail.putExtra("id", id);
	      getActivity().startActivity(detail);
	}
}
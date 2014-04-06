package de.enterprise.lokaAndroid.activities;



import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.extra.Exchanger;
import de.enterprise.lokaAndroid.fragments.MessageTrunkFragment;
import de.enterprise.lokaAndroid.fragments.NotificationsFragment;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;

public class MeTabActivity extends SherlockFragmentActivity implements TabListener{
	
	private MessageTrunkFragment mMessagesFragment;
	private NotificationsFragment mMyPostsFragment;
	private MyServiceBinder msb;
	private Fragment current;
	
	private BroadcastReceiver receiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			if(!intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
				return;
			}
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if(cm.getActiveNetworkInfo().isConnected()){
				connectionChanged();
			}
		}
	};
	
	private ServiceConnection sc = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			msb = (MyServiceBinder) service;
			mMessagesFragment.setBinder(msb);
			mMyPostsFragment.setBinder(msb);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
	};
	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event)
//	{
//		    if ((keyCode == KeyEvent.KEYCODE_BACK))
//		    {
//		    	ActionBar ab = getSupportActionBar();
//				if(ab.getSelectedTab().getPosition() > 0){
//					ab.selectTab(ab.getTabAt(0));
//					return true;
//				}
//				else{
//				    return super.onKeyDown(keyCode, event);
//				}
//		    }
//		    else{
//		    	return super.onKeyDown(keyCode, event);
//		    }
//	}

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent serviceIntent = new Intent(this, MyService.class);
        bindService(serviceIntent, sc, 0);
        
        setContentView(R.layout.me);
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        setupFragments();
        
        Tab tab1 = actionBar.newTab()
                .setIcon(R.drawable.mail)
                .setTabListener(this)
                .setTag(MessageTrunkFragment.TAG);
        actionBar.addTab(tab1);
        
        Tab tab2 = actionBar.newTab()
                .setIcon(R.drawable.chat)
                .setTabListener(this)
                .setTag(NotificationsFragment.TAG);
        actionBar.addTab(tab2);
        
        System.gc();
    }
    
    @Override
    public void onSaveInstanceState(Bundle state) {
    	int tab = getSupportActionBar().getSelectedTab().getPosition();
        state.putInt("tab", tab);
        super.onSaveInstanceState(state);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
    	int tab = state.getInt("tab");
    	ActionBar ab = getSupportActionBar();
    	ab.selectTab(ab.getTabAt(tab));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onResume(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    	
    	if(msb != null){
    		msb.resetRequests();
    	}
    	super.onResume();
    }
    
    @Override
    public void onPause(){
    	unregisterReceiver(receiver);
    	super.onPause();
    }
    
    public void onDestroy(){
    	unbindService(sc);
    	super.onDestroy();
    }

	private void setupFragments() {
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		mMessagesFragment = (MessageTrunkFragment) getSupportFragmentManager().findFragmentByTag(MessageTrunkFragment.TAG);
		if(mMessagesFragment == null){
			mMessagesFragment = new MessageTrunkFragment();
	        ft.add(R.id.fragment_container2, mMessagesFragment, MessageTrunkFragment.TAG);
		}
        ft.hide(mMessagesFragment);
        
        mMyPostsFragment = (NotificationsFragment) getSupportFragmentManager().findFragmentByTag(NotificationsFragment.TAG);
		if(mMyPostsFragment == null){
			mMyPostsFragment = new NotificationsFragment();
	        ft.add(R.id.fragment_container2, mMyPostsFragment, NotificationsFragment.TAG);
		}
        ft.hide(mMyPostsFragment);
        
        ft.commit();
	}
	
	private void showFragment(Fragment fragmentIn, FragmentTransaction ft) {
		if (fragmentIn == null) return;
		//ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
		current = fragmentIn;
		ft.show(fragmentIn);
	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
	}


	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		String tag = (String) tab.getTag();
		Exchanger.lastTab = tab;
		if(tag.equals(MessageTrunkFragment.TAG)){
			showFragment(mMessagesFragment, ft);
		}

		else if(tag.equals(NotificationsFragment.TAG)){
			showFragment(mMyPostsFragment, ft);
		}
	}


	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		ft.hide(current);
	}

	private void connectionChanged() {
		if(msb != null){
			msb.resetRequests();
		}
	}

}


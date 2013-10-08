package de.enterprise.lokaAndroid.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.adapters.GroupAdapter;
import de.enterprise.lokaAndroid.adapters.PostsArrayAdapter.PostViewHolder;
import de.enterprise.lokaAndroid.extra.Exchanger;
import de.enterprise.lokaAndroid.extra.MenuInfo.PostContextMenu;
import de.enterprise.lokaAndroid.fragments.GoToLocationDialog;
import de.enterprise.lokaAndroid.fragments.GroupSelectionDialog;
import de.enterprise.lokaAndroid.fragments.ItemListFragment;
import de.enterprise.lokaAndroid.fragments.MapFragment;
import de.enterprise.lokaAndroid.fragments.SearchPostsDialog;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.services.MyService.MapMode;
import de.enterprise.lokaAndroid.services.MyServiceBinder;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;
import de.enterprise.lokaServer.tools.JSONConverter;

public class MainTabActivity extends SherlockFragmentActivity implements TabListener{
	
	private MapFragment mMapFragment;
	private ItemListFragment mMyListFragment;
	private IMyService msb;
	private Fragment current;
	private boolean notFromHome;
	private View contextView;
	private GroupAdapter groupAdapter;
	private boolean onFirstStart;
	
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
	
	private Handler mNewsHandler = new Handler(){
		@Override
		public void handleMessage(Message m){
			Bundle data = m.getData();
			String json = data.getString("json");
			LinkedHashMap<Object, Object> obj = (LinkedHashMap<Object, Object>) JSONConverter.fromJSON(json, Object.class.getName());
			final int numbNews = (Integer) obj.get("Count");
			if(numbNews > 0)
				getSupportActionBar().getTabAt(2).setText("("+numbNews+")");
			else{
				getSupportActionBar().getTabAt(2).setText("");
			}
		}
	};
	
	private Handler removeFromGroupHandler = new Handler(){
		@Override
		public void handleMessage(Message m){
			int groupID = Integer.parseInt(m.getData().getString("json"));
			if(msb.getSelectedGroup().getId() == groupID){
				GroupPojo noGroup = new GroupPojo();
				noGroup.setId(-1);
				msb.setSelectedGroup(noGroup);
			}
			UserPojo user = msb.getUser();
			user.setGroups(CollectionStuff.integerToIntArray(CollectionStuff.removeIntFromArray(user.getGroups(), groupID)));
			mMapFragment.orderPlacemarks();
		}
	};
	
	private Handler groupHandler = new Handler(){
		@Override
		public void handleMessage(Message m){
			Bundle data = m.getData();
			String json = data.getString("json");
			msb.cacheJSON("RGR", json);
			new DecodeGroupsTask().execute(json);
		}
	};
	
	private Handler imageHandler = new Handler() {
		public void handleMessage(Message m) {
			new DecodeImageTask().execute(m.getData());
		}
	};
	
	private class DecodeGroupsTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String json = params[0];
			final GroupPojo[] groupArr = (GroupPojo[]) JSONConverter
					.fromJSONArray(json, GroupPojo[].class.getName());
			groupHandler.post(new Runnable(){
				public void run(){
					if(groupArr.length > 0){
						resetGroupAdapter();
						Integer[] requiredPicIDs = checkPics(groupArr);
						if (requiredPicIDs.length > 0)
							msb.orderBigPictures(requiredPicIDs);
						groupAdapter.addAllTrick(groupArr);
					}
				}
			});
			return null;
		}

		private Integer[] checkPics(GroupPojo[] groups) {
			ArrayList<Integer> required = new ArrayList<Integer>();
			for (GroupPojo group : groups) {
				if(group.getPic_id() > 0){
					Bitmap bmp = msb.getIconPic(group.getPic_id());
					if (bmp == null) {
						required.add(group.getPic_id());
					}
				}
			}
			Integer[] ret = new Integer[required.size()];
		    Iterator<Integer> iterator = required.iterator();
		    for (int i = 0; i < ret.length; i++)
		    {
		        ret[i] = iterator.next().intValue();
		    }

			return ret;
		}
		
	}
	
	private class DecodeImageTask extends AsyncTask<Bundle, Void, Void> {

		@Override
		protected Void doInBackground(Bundle... data) {
			Bundle b = data[0];
			String json = b.getString("json");
			LinkedHashMap<Object, Object> obj = (LinkedHashMap<Object, Object>) JSONConverter.fromJSON(json, Object.class.getName());
			int picID = (Integer) obj.get("pID");
			Bitmap bmp = ImageHelperAndroid.getBitmapFromByteArray(b.getByteArray("img"));
			bmp = ImageHelperAndroid.getRoundedCornerBitmap(
					MainTabActivity.this, bmp, 3, bmp.getWidth(), bmp.getHeight(), false,
					false, false, false);
			msb.saveGroupPic(picID, bmp);
			imageHandler.post(new Runnable() {
				public void run() {
					groupAdapter.notifyDataSetChanged();
				}
			});
			return null;
		}
	}
	
	private ServiceConnection sc = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			msb = (MyServiceBinder) service;
			mMapFragment.setBinder(msb);
			mMyListFragment.setBinder(msb);
			registerListener();
			msb.checkForNews();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		    if ((keyCode == KeyEvent.KEYCODE_BACK))
		    {
		    	ActionBar ab = getSupportActionBar();
				if(ab.getSelectedTab().getPosition() > 0){
					ab.selectTab(ab.getTabAt(0));
					return true;
				}
				else{
			        Intent backtoHome = new Intent(Intent.ACTION_MAIN);
			        backtoHome.addCategory(Intent.CATEGORY_HOME);
			        backtoHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			        startActivity(backtoHome);
			        return true;
				}
		    }
		    else{
		    	return super.onKeyDown(keyCode, event);
		    }
	}

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        contextView = null;
        notFromHome = false;
        
        Intent serviceIntent = new Intent(this, MyService.class);
        bindService(serviceIntent, sc, 0);
        
        setContentView(R.layout.main);
        
        resetGroupAdapter();
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        LayoutInflater inflater = (LayoutInflater)this.getSystemService
      	      (Context.LAYOUT_INFLATER_SERVICE);
                
        setupFragments();
        
        Tab tab1 = actionBar.newTab()
                .setIcon(R.drawable.worldmap_2)
                .setTabListener(this)
                .setTag(MapFragment.TAG);
        actionBar.addTab(tab1);
        
        Tab tab2 = actionBar.newTab()
        		.setIcon(R.drawable.post)
                .setTabListener(this)
                .setTag(ItemListFragment.TAG);
        actionBar.addTab(tab2);

        Tab tab3 = actionBar.newTab()
        		.setIcon(R.drawable.profil)
                .setTabListener(this)
                .setTag("Me");
        actionBar.addTab(tab3);
        
        onFirstStart = true;
        actionBar.selectTab(tab2);
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
    	
    	
    	ActionBar ab = getSupportActionBar();
    	if(ab.getSelectedTab().getTag().equals("Me")){
    		ab.selectTab(ab.getTabAt(1));
    	}
    	if(msb != null){
    		msb.resetRequests();
    		msb.checkForNews();
    		if(!notFromHome){
    		}else{
    			notFromHome = false;
    		}
    	}

    	super.onResume();
    }
    
    public void setNotHomeButton(boolean notHomeButton){
    	this.notFromHome = notHomeButton;
    }
    
    public void onDestroy(){
    	final Intent serviceIntent = new Intent(this, MyService.class);
    	unbindService(sc);
    	stopService(serviceIntent);
    	super.onDestroy();
    	System.exit(0);
    }
    
    @Override
    public void onPause(){
    	if(msb != null){
    		msb.unregisterListener("RFI", imageHandler);
    	}
    	unregisterReceiver(receiver);
    	super.onPause();
    }

	private void registerListener() {
		msb.registerListener("RAN", mNewsHandler);
		msb.registerListener("RGR", groupHandler);
		msb.registerListener("RFG", removeFromGroupHandler);
	}
    
	private void setupFragments() {
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MapFragment.TAG);
		if(mMapFragment == null){
	        mMapFragment = new MapFragment();
	        ft.add(R.id.fragment_container, mMapFragment, MapFragment.TAG);
		}
		else{
			mMapFragment.initFragment();
		}
        ft.hide(mMapFragment);

        mMyListFragment = (ItemListFragment) getSupportFragmentManager().findFragmentByTag(ItemListFragment.TAG);
		if(mMyListFragment == null){
	        mMyListFragment = new ItemListFragment();
	        ft.add(R.id.fragment_container, mMyListFragment, ItemListFragment.TAG);
		}
        ft.hide(mMyListFragment);
        
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
		//do nothing?
	}


	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		String tag = (String) tab.getTag();
		Exchanger.lastTab = tab;
		if(tag.equals(MapFragment.TAG)){
			showFragment(mMapFragment, ft);
		}
		else if(tag.equals(ItemListFragment.TAG)){
			showFragment(mMyListFragment, ft);
			if(!onFirstStart){
				mMyListFragment.selectPostInList(-1);
			}else{
				onFirstStart = false;
			}
		}
		else if(tag.equals("Me")){
			changeToMeView();
		}
	}
	
	private void changeToMeView() {
		notFromHome = true;
		Intent meTabIntent = new Intent(this, MeTabActivity.class);
		startActivity(meTabIntent);
	}


	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		ft.hide(current);
	}


	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@SuppressWarnings("ucd")
	public void onNewPostClicked(final View view){
		if(!msb.getUser().isBanned()){
			Intent postIntent = new Intent(this, NewPostActivity.class);
			startActivity(postIntent);
		}else{
			Toast.makeText(this, getResources().getString(R.string.you_are_banned), Toast.LENGTH_SHORT).show();
		}

	}
	
	@SuppressWarnings("ucd")
	public void onFindLocationClicked(final View view){
		 DialogFragment newFragment = GoToLocationDialog.newInstance(mMapFragment);
		    newFragment.show(getSupportFragmentManager(), "dialog");
	}
	
	@SuppressWarnings("ucd")
	public void onSearchPostsClicked(final View view){
		 DialogFragment newFragment = SearchPostsDialog.newInstance(msb, mMapFragment);
		    newFragment.show(getSupportFragmentManager(), "dialog");
	}
	
	@SuppressWarnings("ucd")
	public void onGroupSelectionClicked(final View view){
		 DialogFragment newFragment = GroupSelectionDialog.newInstance(groupAdapter, msb, mMapFragment);
		    newFragment.show(getSupportFragmentManager(), "dialog");
		    int[] groups = msb.getUser().getGroups();
		    if(groups != null){
		    	if(groups.length > 0)
		    		msb.requestGroups(CollectionStuff.intToIntegerArray(groups));
		    		final String groupsJSON = msb.getCachedJSON("RGR");
		    		if(groupsJSON != null){
		    			groupHandler.post(new Runnable(){
		    				public void run(){
		    					new DecodeGroupsTask().execute(groupsJSON);
		    				}
		    			});
		    		}
		    }
	}
	
	@SuppressWarnings("ucd")
	public void onSwitchGroupPostClicked(final View view){
		MapMode mode = msb.getMapMode();
		if(mode == MapMode.Posts){
			((ImageButton)view).setImageDrawable(getResources().getDrawable(R.drawable.zielort));
			msb.setMapMode(MapMode.Groups);
		}else{
			((ImageButton)view).setImageDrawable(getResources().getDrawable(R.drawable.zielort_2));
			msb.setMapMode(MapMode.Posts);
		}
		
		mMyListFragment.switchMode(msb.getMapMode());
		mMapFragment.resetTimer();
		mMapFragment.orderPlacemarks();
	}
	
	private void resetGroupAdapter() {
        groupAdapter = new GroupAdapter(this, new ArrayList<GroupPojo>());
        GroupPojo noGroup = new GroupPojo();
        noGroup.setId(-1);
        groupAdapter.add(noGroup);
	}


	@SuppressWarnings("ucd")
	public void onPostContextClicked(final View view){
		contextView = (View) view.getParent().getParent();
		openContextMenu(view);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		if(contextView.getTag() != null){
			PostViewHolder pvh = (PostViewHolder) contextView.getTag();
			int itemId = item.getItemId();
			switch(itemId){
			case PostContextMenu.REPORT:
				if(!msb.getUser().isBanned()){
					msb.reportUser(pvh.userID, pvh.postID, -1);
					Toast.makeText(this, "reported post", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(this, getResources().getString(R.string.you_are_banned), Toast.LENGTH_SHORT).show();
				}
				;break;
			case PostContextMenu.SHOW_ON_MAP:
					getSupportActionBar().selectTab(getSupportActionBar().getTabAt(0));
					mMapFragment.jumpToPosition(pvh.loc);break;
			case PostContextMenu.DELETE:
					msb.deletePost(pvh.postID);
					mMyListFragment.deletePost(pvh.position)
					;break;
			case PostContextMenu.SEND_MESSAGE:
				if(!msb.getUser().isBanned()){
					notFromHome = true;
					mMyListFragment.setLastSelected(pvh.position);
					MessagePojo msg = new MessagePojo();
					msg.setPostID(pvh.postID);
					msg.setFrom(msb.getUser().getId());
					msg.setPicID(pvh.picID);
					Intent messageIntent = new Intent(this, ChatActivity.class);
					messageIntent.putExtra("other", pvh.userID);
					messageIntent.putExtra("msg", msg);
					startActivity(messageIntent);
				}else{
					Toast.makeText(this, getResources().getString(R.string.you_are_banned), Toast.LENGTH_SHORT).show();
				}
				;break;
			case PostContextMenu.BAN_USER_FROM_GROUP:
				msb.banUserFromGroup(pvh.groupID, pvh.userID);
				msb.deletePost(pvh.postID);
				mMyListFragment.deletePost(pvh.position);
				;break;
			}
		}
		return false;
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		    if(contextView.getTag() != null){
		    	PostViewHolder pvh = (PostViewHolder) contextView.getTag();
		    	int i = 0;
		    	if(pvh.userID == msb.getUser().getId()){
		    		//-------------------------------MY POST-----------------------------------------
		    		menu.add(0, PostContextMenu.DELETE, i++, getResources().getString(R.string.delete));
		    	}
		    	else{
		    		//------------------------------NOT MY POST--------------------------------------
			    	Integer[] arr = CollectionStuff.intToIntegerArray(msb.getUser().getReportedOtherPosts());
			    	if(arr != null){
				    	List<Integer> list = Arrays.asList(arr);
				    	if(!list.contains(pvh.postID)){
				    		menu.add(0, PostContextMenu.REPORT, i++, getResources().getString(R.string.report));
				    	}
			    	}
			    	else{
			    		menu.add(0, PostContextMenu.REPORT, i++, getResources().getString(R.string.report));
			    	}
			    	
			    	menu.add(0, PostContextMenu.SEND_MESSAGE, i++, getResources().getString(R.string.send_message));
			    	if(msb.getSelectedGroup().getCreator_id() == msb.getUser().getId()){
			    		menu.add(0, PostContextMenu.DELETE, i++, getResources().getString(R.string.delete));
			    		menu.add(0, PostContextMenu.BAN_USER_FROM_GROUP, i++, getResources().getString(R.string.ban_user_from_group));
			    	}
		    	}
		    	menu.add(0, PostContextMenu.SHOW_ON_MAP, i++, getResources().getString(R.string.show_on_map));


		    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.general_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(msb != null){
			if(msb.getUser().getGroups_available() == 0){
				menu.removeItem(R.id.itNewGroup);
			}else{
				if(menu.findItem(R.id.itNewGroup) == null){
					menu.add(0, R.id.itNewGroup, 0, getResources().getString(R.string.create_group));
				}
			}
		}
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item){
		switch(item.getItemId()){
			case R.id.itFeedback:{
				notFromHome = true;
				Intent feedbackIntent = new Intent(this, FeedbackActivity.class);
				startActivity(feedbackIntent);
				break;
			}
			case R.id.itNewGroup:{
				notFromHome = true;
				Intent groupCreationIntent = new Intent(this, CreateGroupActivity.class);
				startActivity(groupCreationIntent);
				break;
			}
			case R.id.itPrefs:{
				notFromHome = true;
				Intent prefsIntent = new Intent(this, Preferences.class);
				startActivity(prefsIntent);
				break;
			}
		}
		/*
		case R.id.itNewGroup:
			Intent createGroupIntent = new Intent(this, CreateGroupActivity.class);
			startActivity(createGroupIntent);
			break;
		case R.id.itSearchGroup:
			Intent searchGroupIntent = new Intent(this, SearchGroupActivity.class);
			startActivity(searchGroupIntent);
			break;
		}
		*/
		return false;
	}

	public void selectPostInList(int id) {
		mMyListFragment.selectPostInList(id);
	}
	
	public void selectGroupInList(int id) {
		mMyListFragment.selectGroupInList(id);
	}


	public void clearListTab() {
		mMyListFragment.clearList();
	}
	

	private void connectionChanged() {
		if(msb != null){
			msb.resetRequests();
		}
	}
	
}


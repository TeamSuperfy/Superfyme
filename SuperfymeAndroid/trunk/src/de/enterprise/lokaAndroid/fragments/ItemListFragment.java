package de.enterprise.lokaAndroid.fragments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockListFragment;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.activities.MainTabActivity;
import de.enterprise.lokaAndroid.adapters.GroupArrayAdapter;
import de.enterprise.lokaAndroid.adapters.PostsArrayAdapter;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.MyService.MapMode;
import de.enterprise.lokaAndroid.services.RamCache;
import de.enterprise.lokaAndroid.tools.GeoStuff;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaServer.pojos.CommentPojo;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.PostListPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.JSONConverter;
import de.enterprise.lokaServer.tools.Rating;

public class ItemListFragment extends SherlockListFragment{

	public static final String TAG = "listFragment";
	private IMyService msb;
	private PostsArrayAdapter postAdapter;
	private GroupArrayAdapter groupAdapter;
	private PostComparator mPostComp;
	private GroupComparator mGroupComp;
	private int lastSelected = -1;
	private int lastPostSelected = -1;
	private int lastGroupSelected = -1;
	private boolean fromMap;
	private View mLayoutView;
	private Spinner spnCriteria;
	private MapMode mapMode = MapMode.Posts;
	
	private class PostComparator implements Comparator<PostListPojo>{
		
		public static final int MODE_POINTS_DESCENDING = 0, MODE_DISTANCE_DESCENDING = 1, MODE_TIME_DESCENDING = 2;
		public int mMode;
		
		public PostComparator(int mode){
			mMode = mode;
		}
		
		@Override
		public int compare(PostListPojo lhs, PostListPojo rhs) {
			int returnValue = 0;
			switch(mMode){
			case MODE_POINTS_DESCENDING:
				if(lhs.getOtherRating() > rhs.getOtherRating()){
					returnValue = -1;
				}
				else if(lhs.getOtherRating() == rhs.getOtherRating()){
					returnValue =  0;
				}
				else{
					returnValue =  1;
				};break;
			case MODE_TIME_DESCENDING:
				if(lhs.getDate() > rhs.getDate()){
					returnValue = -1;
				}
				else if(lhs.getDate() == rhs.getDate()){
					returnValue =  0;
				}
				else{
					returnValue =  1;
				};break;
			case MODE_DISTANCE_DESCENDING:
				int lhs_dist = GeoStuff.getDistance(msb.getLocation(), lhs.getLocation());
				int rhs_dist = GeoStuff.getDistance(msb.getLocation(), rhs.getLocation());
				if(lhs_dist < rhs_dist){
					returnValue = -1;
				}
				else if(lhs_dist == rhs_dist){
					returnValue =  0;
				}
				else{
					returnValue =  1;
				};break;
			default:;break;
			}
			return returnValue;
		}
	}
	
	private class GroupComparator implements Comparator<GroupPojo>{
		
		public static final int MODE_DISTANCE_DESCENDING = 0;
		public int mMode;
		
		public GroupComparator(int mode){
			mMode = mode;
		}
		
		@Override
		public int compare(GroupPojo lhs, GroupPojo rhs) {
			int returnValue = 0;
			switch(mMode){
			case MODE_DISTANCE_DESCENDING:
				int lhs_dist = GeoStuff.getDistance(msb.getLocation(), lhs.getLocation());
				int rhs_dist = GeoStuff.getDistance(msb.getLocation(), rhs.getLocation());
				if(lhs_dist > rhs_dist){
					returnValue = -1;
				}
				else if(lhs_dist == rhs_dist){
					returnValue =  0;
				}
				else{
					returnValue =  1;
				};break;
			default:;break;
			}
			return returnValue;
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
					ItemListFragment.this.getActivity(), bmp, 3, bmp.getWidth(), bmp.getHeight(), false,
					false, false, false);
			msb.addImageToCache(picID, bmp, RamCache.TINY);
			imageHandler.post(new Runnable(){
				public void run(){
					postAdapter.notifyDataSetChanged();
					if(fromMap){
						fromMap = false;
						if(lastSelected > -1){
							getListView().setSelection(lastSelected);
						}
					}
				}
			});
			return null;
		}
	 }
	
	private class DecodeUserImageTask extends AsyncTask<Bundle, Void, Void> {

		@Override
		protected Void doInBackground(Bundle... data) {
			Bundle b = data[0];
			String json = b.getString("json");
			LinkedHashMap<Object, Object> obj = (LinkedHashMap<Object, Object>) JSONConverter.fromJSON(json, Object.class.getName());
			int picID = (Integer) obj.get("pID");
			Bitmap bmp = ImageHelperAndroid.getBitmapFromByteArray(b.getByteArray("img"));
			msb.saveUserPic(picID, bmp);
			imageHandler.post(new Runnable(){
				public void run(){
					postAdapter.notifyDataSetChanged();
					if(fromMap){
						fromMap = false;
						if(lastSelected > -1){
							getListView().setSelection(lastSelected);
						}
					}
				}
			});
			return null;
		}
	 }
	
	private class DecodeGroupImageTask extends AsyncTask<Bundle, Void, Void> {

		@Override
		protected Void doInBackground(Bundle... data) {
			Bundle b = data[0];
			String json = b.getString("json");
			LinkedHashMap<Object, Object> obj = (LinkedHashMap<Object, Object>) JSONConverter.fromJSON(json, Object.class.getName());
			int picID = (Integer) obj.get("pID");
			Bitmap bmp = ImageHelperAndroid.getBitmapFromByteArray(b.getByteArray("img"));
			msb.saveGroupPic(picID, bmp);
			imageHandler.post(new Runnable(){
				public void run(){
					groupAdapter.notifyDataSetChanged();
					if(fromMap){
						fromMap = false;
						if(lastSelected > -1){
							getListView().setSelection(lastSelected);
						}
					}
				}
			});
			return null;
		}
	 }
	
	private class DecodeLatestCommentsTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String json = params[0];
			CommentPojo[] comments = (CommentPojo[]) JSONConverter.fromJSONArray(json, CommentPojo[].class.getName());
			postAdapter.setLatestComments(comments);
			latestCommentsHandler.post(new Runnable(){
				@Override
				public void run() {
					postAdapter.notifyDataSetChanged();
				}
			});
			return null;
		}
	 }
	
	private class DecodeGroupsTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String json = params[0];
			final GroupPojo[] groups = (GroupPojo[]) JSONConverter.fromJSONArray(json, GroupPojo[].class.getName());
			
			groupsHandler.post(new Runnable(){
				public void run(){
					groupAdapter = new GroupArrayAdapter(getActivity(),
							new ArrayList<GroupPojo>(), msb);
					setListAdapter(groupAdapter);
					if(groups.length > 0){
						groupAdapter.addAllTrick(groups);
						if(lastGroupSelected > -1){
							getListView().setSelection(lastGroupSelected);
						}
					}
				}
			});
			return null;
		}
	 }
	
	private class DecodePostsTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String json = params[0];
			final PostListPojo[] postsArr = (PostListPojo[]) JSONConverter
					.fromJSONArray(json, PostListPojo[].class.getName());
			Integer[] postIDs = new Integer[postsArr.length];
			for (int i = 0; i < postIDs.length; i++) {
				postIDs[i] = postsArr[i].getPostID();
				Rating.calculateRatingIndex(postsArr[i], 0, 0);
			}
			msb.orderLatestComments(postIDs);
			
			postHandler.post(new Runnable(){
				public void run(){
					postAdapter = new PostsArrayAdapter(getActivity(),
							new ArrayList<PostListPojo>(), ItemListFragment.this, msb);
					setListAdapter(postAdapter);
					if(postsArr.length > 0){
						postAdapter.addAllTrick(postsArr);
						if(lastSelected > -1 || lastPostSelected > -1){
							postAdapter.sort(mPostComp);
							if(lastPostSelected > -1){
								lastSelected = getPositionOfPostID(lastPostSelected);
								lastPostSelected = -1;
							}
							checkImages();
							getListView().setSelection(lastSelected);
						}else{
							postAdapter.sort(mPostComp);
							checkImages();
						}
					}else{
						//TODO implement no posts
					}
				}
			});
			return null;
		}

		private void checkImages() {
			int size = Math.min(postAdapter.getCount(), 9);
			for (int i = 0; i < size; i++) {
				PostListPojo p = postAdapter.getItem(i);
				Bitmap b = msb.getImage(p.getPicID(), RamCache.TINY);
				if(b == null){
					msb.orderTinyPictures(new Integer[]{p.getPicID()});
				}
			}
		}
	}


	private Handler postHandler = new Handler() {
		
		public void handleMessage(Message m) {
			Bundle data = m.getData();
			String json = data.getString("json");
			new DecodePostsTask().execute(json);
		}

	};
	
	private Handler groupsHandler = new Handler() {
		
		public void handleMessage(Message m) {
			Bundle data = m.getData();
			String json = data.getString("json");
			new DecodeGroupsTask().execute(json);
		}

	};

	private Handler imageHandler = new Handler() {
		
		public void handleMessage(Message m) {
			new DecodeImageTask().execute(m.getData());
		}
	};
	
	private Handler userImageHandler = new Handler() {
		
		public void handleMessage(Message m) {
			new DecodeUserImageTask().execute(m.getData());
		}
	};
	
	private Handler groupImageHandler = new Handler() {
		
		public void handleMessage(Message m) {
			new DecodeGroupImageTask().execute(m.getData());
		}
	};
	
	private Handler latestCommentsHandler = new Handler() {
		
		public void handleMessage(Message m) {
			Bundle data = m.getData();
			String json = data.getString("json");
			new DecodeLatestCommentsTask().execute(json);
		}
	};
	
	private Handler userUpdateHandler = new Handler() {
		
		public void handleMessage(Message m) {
			Bundle data = m.getData();
			String json = data.getString("json");
			UserPojo user = (UserPojo) JSONConverter.fromJSON(json, UserPojo.class.getName());
			msb.updateUser(user);
		}
	};
	
	public void setBinder(IMyService msb){
		this.msb = msb;
		registerListenerPosts();
		msb.orderPostsList();
	}
	
	@Override
	public void onPause(){
		if(msb != null){
			if(mapMode == MapMode.Posts){
				unregisterListenerPosts();
			}else{
				unregisterListenerGroups();
			}
		}
		super.onPause();
	}
	
	@Override
	public void onResume(){
		if(msb != null){
			if(mapMode == MapMode.Posts){
				registerListenerPosts();
				msb.orderPostsList();
			}else{
				registerListenerGroups();
			}
		}
		super.onResume();
	}
	
	@Override
	public void onHiddenChanged(boolean hidden){
		if(!hidden){
			if(msb != null){
				msb.getUserUpdate();
				msb.registerListener("RFI", userImageHandler);
			}
		}else{
			if(msb != null){
				msb.unregisterListener("RFI", userImageHandler);
			}
		}
	}

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		mPostComp = new PostComparator(PostComparator.MODE_POINTS_DESCENDING);
		mGroupComp = new GroupComparator(GroupComparator.MODE_DISTANCE_DESCENDING);
		setRetainInstance(false);
	}
	
	@Override
	public void onActivityCreated(Bundle arg0){
		postAdapter = new PostsArrayAdapter(getActivity(),
				new ArrayList<PostListPojo>(), this, msb);
		groupAdapter = new GroupArrayAdapter(getActivity(),
				new ArrayList<GroupPojo>(), msb);
		this.setListAdapter(postAdapter);
		showLoading();
		super.onActivityCreated(arg0);
	}
	
	private void showLoading(){
		//TODO implement show loading
	}
	
	@Override
	public void onDestroy(){
		unregisterListenerPosts();
		postAdapter.clear();
		groupAdapter.clear();
		super.onDestroy();
	}

	public Location getLocation() {
		if(msb != null)
			return msb.getLocation();
		return null;
	}

	private void registerListenerPosts() {
		msb.registerListener("RTI", imageHandler);
		msb.registerListener("RFI", userImageHandler);
		msb.registerListener("RPL", postHandler);
		msb.registerListener("RLC", latestCommentsHandler);
		msb.registerListener("GUU", userUpdateHandler);
	}
	
	private void registerListenerGroups() {
		msb.registerListener("RGM", groupsHandler);
		msb.registerListener("RFI", groupImageHandler);
	}
	
	private void unregisterListenerPosts() {
		if(msb != null){
			msb.unregisterListener("RTI", imageHandler);
			msb.unregisterListener("RFI", userImageHandler);
			msb.unregisterListener("RPL", postHandler);
			msb.unregisterListener("RLC", latestCommentsHandler);
			msb.unregisterListener("GUU", userUpdateHandler);
		}
	}
	
	private void unregisterListenerGroups() {
		if(msb != null){
			msb.unregisterListener("RGM", groupsHandler);
			msb.unregisterListener("RFI", groupImageHandler);			
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle data) {
		// Inflate the ListView layout file.
		mLayoutView = inflater.inflate(R.layout.posts_list, null);
		spnCriteria = (Spinner) mLayoutView.findViewById(R.id.spnCriteria);
		spnCriteria.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parentView, View itemView,
					int position, long rowPosition) {
				switch(mapMode){
				case Posts:{
					mPostComp.mMode = position;
					postAdapter.sort(mPostComp);
					;break;
				}
				case Groups:{
					mGroupComp.mMode = position;
					groupAdapter.sort(mGroupComp);
					;break;
				}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		if(mapMode == MapMode.Posts){
			spnCriteria.setSelection(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("posts_default_criterion", ""+0)));
		}else{
			spnCriteria.setSelection(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("groups_default_criterion", ""+0)));
		}
		registerForContextMenu(mLayoutView);
		return mLayoutView;
	}
	
	public void deletePost(int position){
		postAdapter.remove(postAdapter.getItem(position));
	}
	
	public void setLastSelected(int position){
		lastSelected = position;
	}
	
	public void setLastSelectedPost(int postID){
		lastPostSelected = postID;
	}
	
	private int getPositionOfPostID(int postID){
		int size = postAdapter.getCount();
		for (int i = 0; i < size; i++) {
			if(postAdapter.getItem(i).getPostID() == postID){
				return i;
			}
		}
		return -1;
	}

	public void selectPostInList(int id) {
		if(postAdapter != null){
			if(id > 0){
				fromMap = true;
				ListView listView = getListView();
				int pos = getPositionOfPostID(id);
				if(pos > 0){
					lastSelected = pos;
					listView.setSelection(pos);
				}
			}else{
				ListView listView = getListView();
				lastSelected = 0;
				listView.setSelection(lastSelected);
			}
		}
	}
	
	public void selectGroupInList(int id) {
		if(groupAdapter != null){
			if(id > 0){
				fromMap = true;
				ListView listView = getListView();
				int size = groupAdapter.getCount();
				for (int i = 0; i < size; i++) {
					if(groupAdapter.getItem(i).getId() == id){
						lastSelected = i;
						listView.setSelection(i);
						break;
					}
				}
			}else{
				ListView listView = getListView();
				lastSelected = 0;
				listView.setSelection(lastSelected);
			}
		}
	}
	
	public void clearList(){
		if(mapMode == MapMode.Posts){
			postAdapter = new PostsArrayAdapter(getActivity(),
					new ArrayList<PostListPojo>(), ItemListFragment.this, msb);
			setListAdapter(postAdapter);
		}else{
			groupAdapter = new GroupArrayAdapter(getActivity(),
					new ArrayList<GroupPojo>(), msb);
			setListAdapter(groupAdapter);
		}
	}

	public void setNotHomeButton(boolean b) {
		((MainTabActivity)getSherlockActivity()).setNotHomeButton(b);
	}

	public void switchMode(MapMode mode) {
		mapMode = mode;
		
		switch(mode){
		case Posts:{
			unregisterListenerGroups();
			registerListenerPosts();
			groupAdapter.clear();
			setListAdapter(postAdapter);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.posts_criteria));
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spnCriteria.setAdapter(adapter);
			spnCriteria.setSelection(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("posts_default_criterion", ""+0)));
			msb.orderPostsList();
			break;
		}
		case Groups:{
			unregisterListenerPosts();
			registerListenerGroups();
			postAdapter.clear();
			setListAdapter(groupAdapter);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.groups_criteria));
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spnCriteria.setAdapter(adapter);
			spnCriteria.setSelection(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("groups_default_criterion", ""+0)));
			break;
		}
		}
	}
}
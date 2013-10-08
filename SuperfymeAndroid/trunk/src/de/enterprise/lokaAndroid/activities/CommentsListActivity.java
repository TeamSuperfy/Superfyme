package de.enterprise.lokaAndroid.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.adapters.CommentsArrayAdapter;
import de.enterprise.lokaAndroid.adapters.CommentsArrayAdapter.CommentViewHolder;
import de.enterprise.lokaAndroid.extra.MenuInfo.CommentContextMenu;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;
import de.enterprise.lokaAndroid.services.RamCache;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaServer.pojos.CommentPojo;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;
import de.enterprise.lokaServer.tools.JSONConverter;

public class CommentsListActivity extends SherlockListActivity {

	private CommentsArrayAdapter adapter;
	private int postID;
	private ArrayList<Integer> withoutPic;
	private CommentPojo highestRating;
	private boolean reloadPost;
	private View contextView;

	private IMyService msb;
	
	private class CommentComparator implements Comparator<CommentPojo>{
		@Override
		public int compare(CommentPojo lhs, CommentPojo rhs) {
			if(lhs.getDate() > rhs.getDate()){
				return -1;
			}
			else if(lhs.getDate() == rhs.getDate()){
				return 0;
			}
			else{
				return 1;
			}
		}
	}

	private ServiceConnection sc = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			msb = (MyServiceBinder) service;
			registerListener();
			msb.getUserUpdate();
			msb.requestComments(postID);
			if(reloadPost){
				msb.orderSinglePost(postID);
			}
			
			final String rcJSON = msb.getCachedJSON("RC"+postID);
			if(rcJSON != null){
				commentHandler.post(new Runnable(){
					public void run(){
						new DecodeCommentsTask().execute(rcJSON);
					}
				});
			}
			
			invalidateOptionsMenu();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

	};

	private class DecodeImageTask extends AsyncTask<Bundle, Void, Void> {

		@Override
		protected Void doInBackground(Bundle... params) {
			Bundle b = params[0];
			String json = b.getString("json");
			LinkedHashMap<Object, Object> obj = (LinkedHashMap<Object, Object>) JSONConverter
					.fromJSON(json, Object.class.getName());
			int picID = (Integer) obj.get("pID");
			Bitmap bmp = ImageHelperAndroid.getBitmapFromByteArray(b.getByteArray("img"));
			bmp = ImageHelperAndroid.getRoundedCornerBitmap(
					CommentsListActivity.this, bmp, 3, bmp.getWidth(), bmp.getHeight(), false,
					false, false, false);
			msb.addImageToCache(picID, bmp, RamCache.TINY);
			imageHandler.post(new Runnable() {
				public void run() {
					adapter.notifyDataSetChanged();
				}
			});
			return null;
		}
	}
	
	private class DecodeUserImageTask extends AsyncTask<Bundle, Void, Void> {

		@Override
		protected Void doInBackground(Bundle... params) {
			Bundle b = params[0];
			String json = b.getString("json");
			LinkedHashMap<Object, Object> obj = (LinkedHashMap<Object, Object>) JSONConverter
					.fromJSON(json, Object.class.getName());
			int picID = (Integer) obj.get("pID");
			Bitmap bmp = ImageHelperAndroid.getBitmapFromByteArray(b.getByteArray("img"));
			msb.saveUserPic(picID, bmp);
			imageHandler.post(new Runnable() {
				public void run() {
					adapter.notifyDataSetChanged();
				}
			});
			return null;
		}
	}

	private class DecodeCommentsTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String json = params[0];
			final CommentPojo[] commentsArr = (CommentPojo[]) JSONConverter
					.fromJSONArray(json, CommentPojo[].class.getName());
			if(commentsArr.length > 0){
				highestRating = commentsArr[0];
				setHighestRating(commentsArr);
			}

			commentHandler.post(new Runnable() {
				public void run() {
					if(commentsArr.length > 0){
						adapter = new CommentsArrayAdapter(CommentsListActivity.this,
								new ArrayList<CommentPojo>(), msb);
						setListAdapter(adapter);
						adapter.addAllTrick(commentsArr);
						adapter.sort(new CommentComparator());
						
						checkImages();
						
					}
					else{
						//TODO implement no comments 
					}
				}
			});
			return null;
		}
		
		private void checkImages() {
			int size = Math.min(adapter.getCount(), 9);
			for (int i = 0; i < size; i++) {
				CommentPojo cp = adapter.getItem(i);
				if(cp.getPicID() > 0){
					Bitmap b = msb.getImage(cp.getPicID(), RamCache.TINY);
					if(b == null){
						msb.orderTinyPictures(new Integer[]{cp.getPicID()});
					}
				}else{
					withoutPic.add(cp.getCommentID());
				}
			}
		}

		private void setHighestRating(CommentPojo[] comments) {
			for (CommentPojo comment : comments) {
				int rating = comment.getGoodRating() - comment.getBadRating();
				int highRating = highestRating.getGoodRating() - highestRating.getBadRating();
				if(rating > highRating){
					highestRating = comment;
				}
			}

		}

	}

	private Handler commentHandler = new Handler() {

		public void handleMessage(Message m) {
			Bundle data = m.getData();
			String json = data.getString("json");
			msb.cacheJSON("RC"+postID, json);
			new DecodeCommentsTask().execute(json);
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
	
	private Handler userUpdateHandler = new Handler() {
		
		public void handleMessage(Message m) {
			Bundle data = m.getData();
			String json = data.getString("json");
			UserPojo user = (UserPojo) JSONConverter.fromJSON(json, UserPojo.class.getName());
			msb.updateUser(user);
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		withoutPic = new ArrayList<Integer>();
		Intent serviceIntent = new Intent(this, MyService.class);
		bindService(serviceIntent, sc, 0);
		postID = getIntent().getExtras().getInt("pID");
		setContentView(R.layout.comments_list);
		registerForContextMenu(getListView());
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		adapter = new CommentsArrayAdapter(this, new ArrayList<CommentPojo>(), msb);
		this.setListAdapter(adapter);
		showLoading();
	}
	
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
	
	@Override
	public void onResume(){
		if(msb != null){
			withoutPic.clear();
			msb.requestComments(postID);
		}
		super.onResume();
	}

	private void showLoading() {
		//TODO implement show loading
	}

	public void onDestroy() {
		unregisterListener();
		unbindService(sc);
		adapter.clear();
		super.onDestroy();
	}

	private void registerListener() {
		msb.registerListener("RTI", imageHandler);
		msb.registerListener("RFI", userImageHandler);
		msb.registerListener("RC", commentHandler);
		msb.registerListener("GUU", userUpdateHandler);
	}
	
	private void unregisterListener() {
		msb.unregisterListener("RC", commentHandler);
		msb.unregisterListener("RTI", imageHandler);
		msb.unregisterListener("RFI", userImageHandler);
		msb.unregisterListener("GUU", userUpdateHandler);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.comments_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.comments_back) {
			finish();
			return true;
		} else if (item.getItemId() == R.id.comments_new) {
			if(!msb.getUser().isBanned()){
				Intent newComment = new Intent(this, WriteCommentActivity.class);
				newComment.putExtra("postID", postID);
				startActivity(newComment);
			}else{
				Toast.makeText(this, getResources().getString(R.string.you_are_banned), Toast.LENGTH_SHORT).show();
			}
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	public Location getLocation() {
		return msb.getLocation();
	}

	public int getPostID() {
		return postID;
	}

	public int getUserID() {
		return msb.getUser().getId();
	}
	
	public boolean hasNoPicture(int commentID){
		return withoutPic.contains(commentID);
	}
	
	@SuppressWarnings("ucd")
	public void onCommentContextClicked(final View view){
		contextView = (View) view.getParent().getParent();
		openContextMenu(view);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		    if(contextView.getTag() != null){
		    	CommentViewHolder cvh = (CommentViewHolder) contextView.getTag();
		    	int i = 0;
		    	if(cvh.userID == msb.getUser().getId()){
		    		//-------------------------------MY COMMENT-----------------------------------------
		    		menu.add(0, CommentContextMenu.DELETE, i++, getResources().getString(R.string.delete));
		    	}
		    	else{
		    		//------------------------------NOT MY COMMENT--------------------------------------
			    	Integer[] arr = CollectionStuff.intToIntegerArray(msb.getUser().getReportedOtherComments());
			    	if(arr != null){
				    	List<Integer> list = Arrays.asList(arr);
				    	if(!list.contains(cvh.commentID)){
				    		menu.add(0, CommentContextMenu.REPORT, i++, getResources().getString(R.string.report));
				    	}
			    	}
			    	else{
			    		menu.add(0, CommentContextMenu.REPORT, i++, getResources().getString(R.string.report));
			    	}
			    	
			    	menu.add(0, CommentContextMenu.SEND_MESSAGE, i++, getResources().getString(R.string.send_message));

		    	}


		    }
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item){
		if(contextView.getTag() != null){
			CommentViewHolder cvh = (CommentViewHolder) contextView.getTag();
			int itemId = item.getItemId();
			switch(itemId){
			case CommentContextMenu.REPORT:
				if(!msb.getUser().isBanned()){
					msb.reportUser(cvh.userID, -1, cvh.commentID);
					Toast.makeText(this, "reported comment", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(this, getResources().getString(R.string.you_are_banned), Toast.LENGTH_SHORT).show();
				}
				;break;
			case CommentContextMenu.DELETE:
					msb.deleteComment(cvh.commentID);
					adapter.remove(adapter.getItem(cvh.position));
					;break;
			case CommentContextMenu.SEND_MESSAGE:
				if(!msb.getUser().isBanned()){
					MessagePojo msg = new MessagePojo();
					msg.setCommentID(cvh.commentID);
					msg.setFrom(msb.getUser().getId());
					msg.setPicID(cvh.picID);
					Intent messageIntent = new Intent(this, ChatActivity.class);
					messageIntent.putExtra("other", cvh.userID);
					messageIntent.putExtra("msg", msg);
					startActivity(messageIntent);
				}else{
					Toast.makeText(this, getResources().getString(R.string.you_are_banned), Toast.LENGTH_SHORT).show();
				}
				;break;
			}
		}
		return false;
	}

}

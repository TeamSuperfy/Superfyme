package de.enterprise.lokaAndroid.services;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.zip.GZIPInputStream;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import de.enterprise.lokaAndroid.database.ImageTable;
import de.enterprise.lokaAndroid.database.LocalDatabase;
import de.enterprise.lokaAndroid.database.UserTable;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaAndroid.tools.Statistics;
import de.enterprise.lokaServer.pojos.CommentNewPojo;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.pojos.NotificationPojo;
import de.enterprise.lokaServer.pojos.PostNewPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;
import de.enterprise.lokaServer.tools.JSONConverter;

public class MyService extends Service{

	private static boolean DEBUG = false;
	private Statistics stats = new Statistics(false);

	private IBinder mMyServiceBinder;
	private boolean mAllowRebind;
	private RamCache ramCache;
	private ExternalStorageCache extCache;
	private Set<Integer> orderedTinyImagesSet, orderedBigImagesSet, orderedThumbImagesSet;

	private HashMap<String, Set<Handler>> messageListener;

	private UserPojo mUser;
	private GroupPojo selectedGroup;
	private String mSearchWord;
	public static enum MapMode{Posts, Groups};
	private MapMode mapMode = MapMode.Posts;
	
	private LocalDatabase database;
	
	private boolean userUnchecked = true;
	
	//server
	private final String mServerIP = "superfyme.com";
	
	//beim papa
//	private final String mServerIP = "192.168.2.106";
	
	//bei alexej
//	private final String mServerIP = "192.168.178.20";
	
	//quantum
//	private final String mServerIP = "138.246.67.237";
	
	private final int mHttpPortNum = 8080;
	private final String mServletName = "LokaServer/LokaServlet";
	private final String mUrlString = "http://" + mServerIP + ":"
			+ mHttpPortNum + "/" + mServletName;
	
	private ExecutorService executor;
	private Location lastLocation;

	public void onCreate() {
		super.onCreate();
        
		executor = new ScheduledThreadPoolExecutor(5);
		messageListener = new HashMap<String, Set<Handler>>();
		mMyServiceBinder = new MyServiceBinder(this);
		mAllowRebind = true;
		ramCache = new RamCache();
		mUser = new UserPojo();
		initDB();
		extCache = new ExternalStorageCache(this, database);
		orderedTinyImagesSet = new HashSet<Integer>();
		orderedBigImagesSet = new HashSet<Integer>();
		orderedThumbImagesSet = new HashSet<Integer>();

	}
	
	private void initDB() {
		database = new LocalDatabase(this);
		if (DEBUG){
			SQLiteDatabase writable = database.getWritableDatabase();
			writable.delete(UserTable.TABLE_NAME, null,
					null);
			writable.delete(ImageTable.TABLE_NAME, null, null);
		}
		Cursor user = database.getReadableDatabase().rawQuery(
				UserTable.STMT_GET_ALL, null);
		if (user.moveToFirst()) {
			mUser.setId(user.getInt(user.getColumnIndex(UserTable.ID)));
		} else {
			mUser.setId(-1);
		}
		
		//HACK TK!!!!
//		mUser.setId(1);
//		userUnchecked = false;
		
		user.close();
		
		//TODO save last selected group and load it
		GroupPojo gp = new GroupPojo();
		gp.setId(-1);
		selectedGroup = gp;
	}
	
	public void clearDB(){
		SQLiteDatabase writable = database.getWritableDatabase();
		writable.delete(UserTable.TABLE_NAME, null,
				null);
		writable.delete(ImageTable.TABLE_NAME, null, null);
	}
	
	public Bitmap getPic(int picID) {
		Bitmap bmp = ramCache.get(picID, RamCache.BIG);
		if(bmp == null){
			bmp = extCache.getPic(picID);
			if(bmp != null)
				ramCache.put(picID, bmp, RamCache.BIG);
		}
		return bmp;
	}
	
	public void saveUserPic(int picID, Bitmap bmp){
		ramCache.put(picID, bmp, RamCache.BIG);
		extCache.saveUserPic(picID, bmp);
	}
	
	public void saveGroupPic(int picID, Bitmap bmp){
		ramCache.put(picID, bmp, RamCache.BIG);
		extCache.saveGroupPic(picID, bmp);
	}

	private void saveNewUser() {
		database.getWritableDatabase().delete(UserTable.TABLE_NAME, null,null);
		ContentValues cv = new ContentValues();
		cv.put(UserTable.ID, mUser.getId());
		database.getWritableDatabase().insert(UserTable.TABLE_NAME, null, cv);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMyServiceBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
	    // All clients have unbound with unbindService()
	    return mAllowRebind;
	}

	public void onDestroy() {
		if(database != null){
			database.close();
		}
		super.onDestroy();
	}

	public void setLastLocation(Location loc) {
		lastLocation = loc;
	}

	public Location getLocation() {
		return lastLocation;
	}

	public void orderPosts(final String jsonOrder) {
		sendNetworkCommand("RPM", jsonOrder);
	}

	public void orderPostsList() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("gID", getSelectedGroup().getId());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		sendNetworkCommand("RPL", obj.toString());
	}

	public void orderTinyImages(Integer[] pIDs) {
		for (Integer picID : pIDs) {
			if(!orderedTinyImagesSet.contains(picID) && picID > 0){
				orderedTinyImagesSet.add(picID);
				JSONObject obj = new JSONObject();
	
				try {
					obj.put("PicID", picID);
				} catch (JSONException e) {
					e.printStackTrace();
				}
	
				sendNetworkCommand("RTI", obj.toString());
			}
		}
	}

	public void orderBigImage(int picID) {
		if(!orderedBigImagesSet.contains(picID) && picID > 0){
			orderedBigImagesSet.add(picID);
			JSONObject obj = new JSONObject();
	
			try {
				obj.put("PicID", picID);
			} catch (JSONException e) {
				e.printStackTrace();
			}
	
			sendNetworkCommand("RFI", obj.toString());
		}
	}
	
	public void removeFromOrderList(int picID, int size){
		switch(size){
		case RamCache.TINY:
			orderedTinyImagesSet.remove(picID);
			;break;
		case RamCache.BIG:
			orderedBigImagesSet.remove(picID);
			;break;
		case RamCache.THUMB:
			orderedThumbImagesSet.remove(picID);
			;break;
		}
	}

	
	public void sendPost(PostNewPojo post) {
		//sendNetworkCommand("SP", JSONConverter.toJSON(post));
		byte[] data = post.getPicData();
		post.setPicData(null);
		try {
			sendImageNetworkCommand("SP", JSONConverter.toJSON(post), data);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void sendComment(CommentNewPojo cmt) {
		//sendNetworkCommand("SP", JSONConverter.toJSON(post));
		byte[] data = cmt.getPicData();
		cmt.setPicData(null);
		try {
			if(data != null)
				sendImageNetworkCommand("AC", JSONConverter.toJSON(cmt), data);
			else
				sendNetworkCommand("AC", JSONConverter.toJSON(cmt));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void sendNetworkCommand(String cmd, String json) {
		HttpPost httppost = new HttpPost(mUrlString);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("cmd", cmd));
		nameValuePairs.add(new BasicNameValuePair("json", json));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		executeHttpPost(httppost);
	}
	
	public void sendImageNetworkCommand(String cmd, String json, byte[] data) throws UnsupportedEncodingException{
        HttpPost httppost = new HttpPost(mUrlString);
        MultipartEntity entity = new MultipartEntity(
                HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));

        //Set Data and Content-type header for the image
        httppost.addHeader("cmd", cmd);
        entity.addPart("json", new StringBody(json, Charset.forName("UTF-8")));
        if(data != null){
	        entity.addPart("file",
	                new ByteArrayBody(data, "image/jpeg", "file"));
        }
        httppost.setEntity(entity);
        executeHttpPost(httppost);
	}

	private void executeHttpPost(final HttpPost post) {
		new Thread("ExecuteHttpPost") {
			public void run() {
				DefaultHttpClient httpclient = new DefaultHttpClient();
				httpclient.addRequestInterceptor(new GzipRequestInterceptor());
				httpclient.addResponseInterceptor(new GzipResponseInterceptor());
				try {
					HttpResponse response = httpclient.execute(post);
					int status = response.getStatusLine().getStatusCode();
					if(status != 201){
						Header contentType = response.getEntity().getContentType();
						boolean isMultipart = false;
						if(contentType != null){
							isMultipart = contentType.getValue().contains("multipart");
						}
						
						if(!isMultipart){
							handleMessage(response.getEntity());
						}else{
							handleMultipartMessage(response.getEntity());
						}
						if(stats.enabled){
							Log.i(getClass().getName(), stats.toString());
						}
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private void handleMessage(final HttpEntity ent) {
		executor.execute(new Runnable(){
			public void run(){
				BufferedReader in;
				try {
					in = new BufferedReader(new InputStreamReader(ent.getContent(), "UTF-8"));
					String ln = in.readLine();
					in.close();
					String[] parts = ln.split("#");
					if(stats.enabled){
						stats.putTraffic(parts[0], parts[1].getBytes().length, ent.getContentLength());
					}
					String cmd = parts[0];
					final String json = parts[1];
					if (messageListener.containsKey(cmd)) {
						Set<Handler> handlers = messageListener.get(cmd);
						for (Handler handler : handlers) {
							Message msg = new Message();
							Bundle data = new Bundle();
							data.putString("json", json);
							msg.setData(data);
							handler.sendMessage(msg);
						}
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}});
	}
	
	private void handleMultipartMessage(final HttpEntity ent) {
		executor.execute(new Runnable(){
			public void run(){
				InputStream is = null;
				try {
					is = ent.getContent();
					ByteArrayDataSource ds = new ByteArrayDataSource(is, "multipart/mixed");
					MimeMultipart multipart = new MimeMultipart(ds);
					BodyPart jsonPart = multipart.getBodyPart(0);
					BodyPart imagePart = multipart.getBodyPart(1);

					BufferedReader in = new BufferedReader(new InputStreamReader(jsonPart.getInputStream(), Charset.forName("UTF-8")));
					String[] jsonParts = in.readLine().split("#");
					String cmd = jsonParts[0];
					String json = jsonParts[1];
					
					if(stats.enabled){
						stats.putTraffic(cmd, jsonPart.getSize() + imagePart.getSize(), ent.getContentLength());
					}
					
					ByteArrayOutputStream bos = new ByteArrayOutputStream(imagePart.getSize());
					byte[] buffer = new byte[4096];
					int bytesRead = 0;
					InputStream ins = imagePart.getInputStream();
					while((bytesRead = ins.read(buffer)) != -1){
						bos.write(buffer, 0, bytesRead);
					}
					byte[] imageData = bos.toByteArray();

					if (messageListener.containsKey(cmd)) {
						Set<Handler> handlers = messageListener.get(cmd);
						for (Handler handler : handlers) {
							Message msg = new Message();
							Bundle data = new Bundle();
							data.putString("json", json);
							data.putByteArray("img", imageData);
							msg.setData(data);
							handler.sendMessage(msg);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				} finally{
					if(is != null){
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	public void registerListener(String cmd, Handler handler) {
		if (!messageListener.containsKey(cmd)) {
			messageListener.put(cmd, new HashSet<Handler>());
		}
		Set<Handler> handlers = messageListener.get(cmd);
		handlers.add(handler);
	}

	public UserPojo getUser() {
		return mUser;
	}

	public void addBitmapToCache(int picID, Bitmap b, int size) {
		ramCache.put(picID, b, size);
		removeFromOrderList(picID, size);
	}

	public boolean isImageCached(int picID, int size) {
		return ramCache.has(picID, size);
	}

	public Bitmap getImage(int picID, int size) {
		return ramCache.get(picID, size);
	}

	public void orderComments(int postID) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("PID", postID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendNetworkCommand("RC", obj.toString());
	}

	public void rateItem(boolean post, int id, boolean good) {
		JSONObject obj = new JSONObject();
		if (post) {
			try {
				obj.put("PID", id);
				obj.put("UID", mUser.getId());
				obj.put("R", good);
				sendNetworkCommand("RAP", obj.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}

		} else {
			try {
				obj.put("CID", id);
				obj.put("UID", mUser.getId());
				obj.put("R", good);
				sendNetworkCommand("RAC", obj.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void clearImageCache() {
		ramCache.clear();
	}

	public void unregisterListener(String cmd, Handler handler) {
		if(messageListener.containsKey(cmd)){
			Set<Handler> listeners = messageListener.get(cmd);
			listeners.remove(handler);
		}
	}

	public void orderMessages(int otherUID, int postID, int commentID) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("UID", mUser.getId());
			obj.put("OUID", otherUID);
			obj.put("PID", postID);
			obj.put("CID", commentID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendNetworkCommand("RM", obj.toString());
	}

	public void orderNotifications() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("UID", mUser.getId());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendNetworkCommand("RN", obj.toString());
	}

	public void requestAvailableNews() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("UID", mUser.getId());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendNetworkCommand("RAN", obj.toString());
	}

	public void orderSinglePost(int postID) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("PID", postID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendNetworkCommand("RSP", obj.toString());
	}

	public void orderThumbnailImages(Integer[] picIDs) {
		for (Integer picID : picIDs) {
			if(!orderedThumbImagesSet.contains(picID) && picID > 0){
				orderedThumbImagesSet.add(picID);
				JSONObject obj = new JSONObject();
	
				try {
					obj.put("PicID", picID);
				} catch (JSONException e) {
					e.printStackTrace();
				}
	
				sendNetworkCommand("RTHI", obj.toString());
			}
		}
	}

	public void readNotification(int notifID) {
		JSONObject obj = new JSONObject();

		try {
			obj.put("NID", notifID);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		sendNetworkCommand("RNO", obj.toString());
	}
	
	public void readMessage(int messID) {
		JSONObject obj = new JSONObject();

		try {
			obj.put("MID", messID);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		sendNetworkCommand("RME", obj.toString());
	}

	public void deleteNotifications(NotificationPojo[] notifsArray) {
		sendNetworkCommand("DN", JSONConverter.toJsonArray(notifsArray));
	}

	public void reportUser(int userID, int postID, int commID) {
		JSONObject obj = new JSONObject();
		
		if(postID > 0){
			mUser.setReportedOtherPosts(CollectionStuff.appendToArray(mUser.getReportedOtherPosts(), new int[]{postID}));
		}
		else{
			mUser.setReportedOtherComments(CollectionStuff.appendToArray(mUser.getReportedOtherComments(), new int[]{commID}));
		}
		
		try {
			obj.put("UID", mUser.getId());
			obj.put("RUID", userID);
			obj.put("CID", commID);
			obj.put("PID", postID);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		sendNetworkCommand("RU", obj.toString());
	}

	public void deletePost(int postID) {
		JSONObject obj = new JSONObject();

		try {
			obj.put("PID", postID);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		sendNetworkCommand("DP", obj.toString());
	}

	public void sendMessage(MessagePojo mp) {
		sendNetworkCommand("SM", JSONConverter.toJSON(mp));
	}

	public void deleteMessages(Integer[] messIDs) {
		sendNetworkCommand("DME", JSONConverter.toJsonArray(messIDs));
	}

	public void requestGroups(Integer[] groupIDs) {
		sendNetworkCommand("RGR", JSONConverter.toJsonArray(groupIDs));
	}

	public GroupPojo getSelectedGroup(){
		return selectedGroup;
	}
	
	public void setSelectedGroup(GroupPojo group){
		selectedGroup = group;
	}

	public void checkGroupName(String groupName, String place) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("name", groupName);
			obj.put("place", place);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendNetworkCommand("CGN", obj.toString());
	}

	public void createGroup(GroupPojo gp) {
		//sendNetworkCommand("SP", JSONConverter.toJSON(post));
		byte[] data = gp.getPic();
		gp.setPic(null);
		try {
			if(data != null)
				sendImageNetworkCommand("CG", JSONConverter.toJSON(gp), data);
			else
				sendNetworkCommand("CG", JSONConverter.toJSON(gp));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	//returns true if the user starts for the first time
	public boolean updateUser(UserPojo user) {
		mUser = user;
		boolean firstTime = false;
		if(userUnchecked){
			userUnchecked = false;
			Cursor c = database.getReadableDatabase().rawQuery(
					UserTable.STMT_GET_ALL, null);
			if (c.moveToFirst()) {
				int userID = c.getInt(c.getColumnIndex(UserTable.ID));
				if(mUser.getId() != userID){
					firstTime = true;
					saveNewUser();
				}
			}else{
				firstTime = true;
				saveNewUser();
			}
			c.close();
		}
		return firstTime;
	}

	public void joinGroup(int id) {
		mUser.setGroups(CollectionStuff.appendToArray(mUser.getGroups(), new int[]{id}));
		JSONObject obj = new JSONObject();
		try {
			obj.put("UID", mUser.getId());
			obj.put("gID", id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendNetworkCommand("JG", obj.toString());
	}

	public void searchGroups(String name, String place, boolean isPublic) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("name", name);
			obj.put("place", place);
			obj.put("public", isPublic);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendNetworkCommand("SGR", obj.toString());
	}

	public void deleteAllMessages() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("UID", mUser.getId());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendNetworkCommand("DAM", obj.toString());
	}

	public void orderMessageTrunks() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("UID", mUser.getId());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendNetworkCommand("RMT", obj.toString());
	}

	public void orderBigPictures(Integer[] requiredPicIDs) {
		for (Integer picID : requiredPicIDs) {
			if(!orderedBigImagesSet.contains(picID) && picID > 0){
				orderedBigImagesSet.add(picID);
				JSONObject obj = new JSONObject();
	
				try {
					obj.put("PicID", picID);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				sendNetworkCommand("RFI", obj.toString());
			}
		}
	}

	public void setSearchWord(String searchWord) {
		this.mSearchWord = searchWord;
	}
	
	public String getSearchWord(){
		return mSearchWord;
	}

	public void leaveGroup(int groupID) {
		if(selectedGroup.getId() == groupID){
	        GroupPojo noGroup = new GroupPojo();
	        noGroup.setId(-1);
	        selectedGroup = noGroup;
	        orderPostsList();
		}
		
		int[] groups = mUser.getGroups();
		mUser.setGroups(CollectionStuff.integerToIntArray(CollectionStuff.removeIntFromArray(groups, groupID)));
		JSONObject obj = new JSONObject();
		try {
			obj.put("UID", mUser.getId());
			obj.put("GID", groupID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendNetworkCommand("LG", obj.toString());
	}

	public void deleteGroup(int groupID) {
		if(selectedGroup.getId() == groupID){
	        GroupPojo noGroup = new GroupPojo();
	        noGroup.setId(-1);
	        selectedGroup = noGroup;
	        orderPostsList();
		}
		
		int[] groups = mUser.getGroups();
		mUser.setGroups(CollectionStuff.integerToIntArray(CollectionStuff.removeIntFromArray(groups, groupID)));
		JSONObject obj = new JSONObject();
		try {
			obj.put("GID", groupID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendNetworkCommand("DG", obj.toString());
	}

	public void banUserFromGroup(int groupID, int userID) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("UID", userID);
			obj.put("GID", groupID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendNetworkCommand("BUG", obj.toString());
	}

	public void orderLatestComments(Integer[] postIDs) {
		sendNetworkCommand("RLC", JSONConverter.toJsonArray(postIDs));
	}

	public void sendFeedback(String feedback) {
		JSONObject obj = new JSONObject();
		PackageInfo pinfo;
		Date now = new Date();
		try {
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String versionName = pinfo.versionName;

			obj.put("UID", mUser.getId());
			obj.put("F", feedback);
			obj.put("V", versionName);
			obj.put("T", now.toString());
			obj.put("P", android.os.Build.MODEL);
			obj.put("A", android.os.Build.VERSION.RELEASE);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		sendNetworkCommand("SF", obj.toString());
	}

	public void getUserUpdate() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("UID", mUser.getId());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendNetworkCommand("GUU", obj.toString());
	}

	public void deleteComment(int commentID) {
		JSONObject obj = new JSONObject();

		try {
			obj.put("CID", commentID);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		sendNetworkCommand("DC", obj.toString());
	}
	
	
	/**
	 * called when: 
	 * - internet connection changed
	 * - app is resumed
	 */
	public void resetRequests(){
		orderedBigImagesSet.clear();
		orderedThumbImagesSet.clear();
		orderedTinyImagesSet.clear();
		System.out.println("---------------------------- Requests reset");
	}
	
	private final class GzipRequestInterceptor implements HttpRequestInterceptor{

		@Override
		public void process(HttpRequest request, HttpContext context)
				throws HttpException, IOException {
			 if (!request.containsHeader("Accept-Encoding")) {
				 request.addHeader("Accept-Encoding", "gzip");
	         }

		}
		
	}
	private final class GzipResponseInterceptor implements HttpResponseInterceptor{

		@Override
		public void process(HttpResponse response, HttpContext context)
				throws HttpException, IOException {
			HttpEntity entity = response.getEntity();
			Header header = entity.getContentEncoding();
			if(header != null){
				HeaderElement[] codecs = header.getElements();
				for(int i=0;i<codecs.length;i++){
					if(codecs[i].getName().equalsIgnoreCase("gzip")){
						response.setEntity(new GzipDecompressingEntity(response.getEntity()));
						return;
					}
				}
			}
		}
		
	}
	
	private static class GzipDecompressingEntity extends HttpEntityWrapper{

		private long contentLength;
		
		public GzipDecompressingEntity(final HttpEntity wrapped) {
			super(wrapped);
		}
		
		@Override
		public InputStream getContent() throws IOException, IllegalStateException {
			InputStream wrappedin = wrappedEntity.getContent();
//			
//			ByteArrayOutputStream contentBytes = new ByteArrayOutputStream();
//			byte[] buffer = new byte[4096];
//			int bytesRead = 0;
//			while((bytesRead = wrappedin.read(buffer)) != -1){
//				contentBytes.write(buffer, 0, bytesRead);
//			}
//			
//			contentLength = contentBytes.size();
			
//			return new GZIPInputStream( new ByteArrayInputStream(contentBytes.toByteArray()));
			return new GZIPInputStream(wrappedin);
		}
		
		@Override
		public long getContentLength(){
			return contentLength;
		}
	}

	public void newUser(String username, Bitmap profilepic) {
		mUser.setUsername(username);
		JSONObject obj = new JSONObject();
		try {
			obj.put("UID", mUser.getId());
			obj.put("UN", username);
			byte[] img = null;
			if(profilepic != null){
				img = ImageHelperAndroid.bitmapToByte(profilepic);
			}
			sendImageNetworkCommand("NU", obj.toString(), img);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	public MapMode getMapMode() {
		return mapMode;
	}

	public void setMapMode(MapMode mapMode) {
		this.mapMode = mapMode;
	}

	public void orderGroups(String json) {
		sendNetworkCommand("RGM", json);
	}

	public void requestUserPojos(Integer[] users) {
		sendNetworkCommand("RUP", JSONConverter.toJsonArray(users));
	}

	public void removeUserFromGroup(int groupID, int userID) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("GID", groupID);
			obj.put("UID", userID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendNetworkCommand("RUFG", obj.toString());
	}

	public void inviteUser(int groupID, String username) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("GID", groupID);
			obj.put("UN", username);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendNetworkCommand("IU", obj.toString());
	}

	public void deleteGroupInvitation(int groupID, int userID) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("GID", groupID);
			obj.put("UID", userID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendNetworkCommand("DGI", obj.toString());
	}

	public String getCachedJSON(String key) {
		return ramCache.getJSON(key);
	}

	public void cacheJSON(String key, String val) {
		ramCache.put(key, val);
	}
	
}

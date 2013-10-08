package de.enterprise.lokaAndroid.services;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;
import de.enterprise.lokaAndroid.database.LocalDatabase;
import de.enterprise.lokaAndroid.database.UserTable;
import de.enterprise.lokaServer.pojos.PostPojo;
import de.enterprise.lokaServer.pojos.RequestPostContentsPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class MyService extends Service {
	
	public static boolean DEBUG = false;

	// keys
	public static final String STATUS_CONNECTION = "con";

	// values
	public static final String CONNECTION_ESTABLISHED = "connection established";
	public static final String CONNECTING = "connecting";
	public static final String CONNECTION_REFUSED = "connection refused";

	private IBinder mMyServiceBinder;

	private LocationManager locationManager;
	private LocationListener locationListener;
	private HashMap<String, ArrayList<Handler>> messageListener;
	private ArrayList<Handler> stateListeners;
	private PostPojo[] visiblePosts = new PostPojo[]{};

	private int myUserID;

	private ListenerThread mListener;
	private Socket clientSocket;
	private LocalDatabase database;
	private BufferedReader clientIn;
	// private final String mServerIP = "88.217.73.125";
	// private final String mServerIP = "10.0.2.2";
	private final String mServerIP = "192.168.178.20";
	private final int mServerSocketPort = 8083;
	private final int mHttpPortNum = 8080;
	private final String mServletName = "LokaServer/lokaServlet";
	private final String mUrlString = "http://" + mServerIP + ":"
			+ mHttpPortNum + "/" + mServletName;

	private Location lastLocation;

	public void onCreate() {
		super.onCreate();
		messageListener = new HashMap<String, ArrayList<Handler>>();
		stateListeners = new ArrayList<Handler>();
		mMyServiceBinder = new MyServiceBinder(this);
		initLocation();
		initDB();
		initSocket();
	}

	private void initDB() {
		database = new LocalDatabase(this);
		if(DEBUG)
			database.getWritableDatabase().delete(UserTable.TABLE_NAME, null, null);
		Cursor user = database.getReadableDatabase().rawQuery(UserTable.STMT_GET_ALL, null);
		if(user.moveToFirst()){
			myUserID = user.getInt(user.getColumnIndex(UserTable.ID));
		}
		else{
			myUserID = -1;
		}
		user.close();
	}
	
	private void saveNewUser(){
		ContentValues cv = new ContentValues();
		cv.put(UserTable.ID, myUserID);
		database.getWritableDatabase().insert(UserTable.TABLE_NAME, null, cv);
	}

	private void initSocket() {
		new Thread() {
			public void run() {
				boolean connected = false;
				while (!connected) {
					try {
						try {
							sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						notifyStateListeners(STATUS_CONNECTION, CONNECTING);
						clientSocket = new Socket(mServerIP, mServerSocketPort);
						connected = true;
						notifyStateListeners(STATUS_CONNECTION,
								CONNECTION_ESTABLISHED);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try {
					PrintWriter out = new PrintWriter(
							clientSocket.getOutputStream(), true);
					if(myUserID == -1)
						out.println("null");
					else
						out.println(myUserID);

					clientIn = new BufferedReader(new InputStreamReader(
							clientSocket.getInputStream()));
					if(myUserID == -1){
						myUserID = Integer.parseInt(clientIn.readLine());
						saveNewUser();
					}
					mListener = new ListenerThread();
					mListener.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMyServiceBinder;
	}

	public void initLocation() {
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				setLastLocation(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 1, locationListener);
	}

	private void setLastLocation(Location loc) {
		lastLocation = loc;
	}

	public Location getLocation() {
		return lastLocation;
	}

	public void orderPosts(final RequestPostContentsPojo contents) {
		new Thread() {
			public void run() {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(mUrlString);

				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("cmd", "RP"));
					nameValuePairs.add(new BasicNameValuePair("json",
							JSONConverter.toJSON(contents)));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					httpclient.execute(httppost);

				} catch (ClientProtocolException e) {
				} catch (IOException e) {
				}
			}
		}.start();
	}
	
	public void updateVisiblePosts(PostPojo[] posts){
		visiblePosts = posts;
	}
	
	public PostPojo[] getLastVisiblePosts(){
		return visiblePosts;
	}
	
	public void orderPostDetail(final int id, final int postID) {
		new Thread() {
			public void run() {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(mUrlString);

				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("cmd", "RPD"));
					nameValuePairs.add(new BasicNameValuePair("json", ""+id+"#"+postID));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					httpclient.execute(httppost);

				} catch (ClientProtocolException e) {
				} catch (IOException e) {
				}
			}
		}.start();
	}

	public void sendPost(PostPojo post, Bitmap img) {
		sendNetworkCommand("SP", JSONConverter.toJSON(post), img);
	}

	private void sendNetworkCommand(String cmd, String json, Bitmap image) {
		if (image != null) {
			ByteArrayOutputStream full_stream = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.PNG, 100, full_stream);
			byte[] full_bytes = full_stream.toByteArray();
			String img_full = Base64.encodeToString(full_bytes, Base64.DEFAULT);

			HttpPost httppost = new HttpPost(mUrlString);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("cmd", cmd));
			nameValuePairs.add(new BasicNameValuePair("json", json));
			nameValuePairs.add(new BasicNameValuePair("img", img_full));
			try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			executeHttpPost(httppost);
		}
	}

	private void sendNetworkCommand(String cmd, String json) {
		HttpPost httppost = new HttpPost(mUrlString);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("cmd", cmd));
		nameValuePairs.add(new BasicNameValuePair("json", json));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		executeHttpPost(httppost);
	}

	private void executeHttpPost(final HttpPost post) {
		new Thread() {
			public void run() {
				HttpClient httpclient = new DefaultHttpClient();

				try {
					httpclient.execute(post);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private class ListenerThread extends Thread {

		public void run() {
			String ln;
			StringBuilder b = new StringBuilder();
			boolean toBuilder = false;
			try {
				while ((ln = clientIn.readLine()) != null) {
					if(ln.equals("start")){
						toBuilder = true;
						ln = "";
						b = new StringBuilder();
					}
					else if(ln.equals("end")){
						toBuilder = false;
						ln = b.toString();
					}
					
					if(toBuilder){
						b.append(ln);
					}
					if(!toBuilder){
						handleMessage(ln);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handleMessage(String ln) {
			String[] parts = ln.split("#");
			String cmd = parts[0];
			final String json = parts[1];
			if (messageListener.containsKey(cmd)) {
				ArrayList<Handler> handlers = messageListener.get(cmd);
				for (Handler handler : handlers) {
					Message msg = new Message();
					Bundle data = new Bundle();
					data.putString("json", json);
					msg.setData(data);
					handler.sendMessage(msg);
				}
			}
		}
	}

	public void registerListener(String cmd, Handler handler) {
		if (!messageListener.containsKey(cmd)) {
			messageListener.put(cmd, new ArrayList<Handler>());
		}
		ArrayList<Handler> handlers = messageListener.get(cmd);
		handlers.add(handler);
	}

	public void registerStateListener(Handler handler) {
		stateListeners.add(handler);
	}

	private void notifyStateListeners(String key, String value) {
		for (Handler handler : stateListeners) {
			Bundle data = new Bundle();
			data.putString(key, value);
			Message msg = new Message();
			msg.setData(data);
			handler.sendMessage(msg);
		}
	}

	public UserPojo getUser() {
		UserPojo me = new UserPojo();
		me.setId(myUserID);
		return me;
	}



}

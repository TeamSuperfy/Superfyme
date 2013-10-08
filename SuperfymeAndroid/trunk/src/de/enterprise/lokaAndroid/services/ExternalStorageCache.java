package de.enterprise.lokaAndroid.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import de.enterprise.lokaAndroid.database.LocalDatabase;
import de.enterprise.lokaAndroid.database.ImageColumns;
import de.enterprise.lokaAndroid.database.ImageTable;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;

public class ExternalStorageCache {
	public static final String SUPERFYME_SD_URL = "/superfyme";
	public static final String USER_IMAGE_URL = SUPERFYME_SD_URL + "/user_images";
	public static final String GROUP_IMAGE_URL = SUPERFYME_SD_URL + "/group_images";
	private boolean mExternalStorageAvailable = false;
	private boolean mExternalStorageWriteable = false;
	
	HandlerThread uiThread;
	UIHandler uiHandler;
	private MyService myService;
	private LocalDatabase database;
	
	public ExternalStorageCache(MyService myService, LocalDatabase database){
	    uiThread = new HandlerThread("UIHandler");
	    uiThread.start();
	    uiHandler = new UIHandler(uiThread.getLooper());
	    
		this.myService = myService;
		this.database = database;
		//init sd card folders
		updateExternalStorageState();
		if(mExternalStorageWriteable){
			File userImgs = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + USER_IMAGE_URL);
			if(!userImgs.exists())
				userImgs.mkdirs();
			File groupImgs = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + GROUP_IMAGE_URL);
			if(!groupImgs.exists())
				groupImgs.mkdirs();
		}else{
			handleUIRequest("Warning! Not able to write to sd card");
		}
	}
	
	public void saveUserPic(int picID, Bitmap bmp){
		savePic(picID, bmp, USER_IMAGE_URL);
	}
	
	public void saveGroupPic(int picID, Bitmap bmp){
		savePic(picID, bmp, GROUP_IMAGE_URL);
	}
	
	private void savePic(int picID, Bitmap bmp, String path){
		updateExternalStorageState();
		if(mExternalStorageWriteable){
			File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + path + "/"
					+ picID + "-" + System.currentTimeMillis() + ".jpg");
			byte[] b = ImageHelperAndroid.bitmapToByte(bmp);
			FileOutputStream fo;
			try {
				f.createNewFile();
				fo = new FileOutputStream(f);
				fo.write(b);
				fo.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			ContentValues cv = new ContentValues();
			cv.put(ImageTable.ID, picID);
			cv.put(ImageTable.URL, f.getAbsolutePath());
			database.getWritableDatabase().insert(ImageTable.TABLE_NAME, null, cv);
		}else{
			handleUIRequest("Warning! Not able to write to sd card");
		}
	}
	
	public Bitmap getPic(int picID) {
		Bitmap bmp = null;
		if(bmp == null){
			Cursor url = database.getReadableDatabase().rawQuery(ImageTable.STMT_GET_URL, new String[]{String.valueOf(picID)});
			String picURL = "";
			if(url.moveToFirst()){
				picURL = url.getString(url.getColumnIndex(ImageColumns.URL));
				updateExternalStorageState();
				if(mExternalStorageAvailable){
					Environment.getExternalStorageState();
					bmp = BitmapFactory.decodeFile(picURL);
					if(bmp == null){
						handleUIRequest("Warning! Couldn't read " + picURL);
					}
				}else{
					handleUIRequest("Warning! Not able to read from sd card");
				}
			}
		}
		return bmp;
	}

	
	void updateExternalStorageState() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		}
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		}
		else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}
	
	
	private final class UIHandler extends Handler
	{
	    public static final int DISPLAY_UI_TOAST = 0;

	    public UIHandler(Looper looper)
	    {
	        super(looper);
	    }

	    @Override
	    public void handleMessage(Message msg)
	    {
	        switch(msg.what)
	        {
	        case UIHandler.DISPLAY_UI_TOAST:
	        {
	            Context context = myService.getApplicationContext();
	            Toast t = Toast.makeText(context, (String)msg.obj, Toast.LENGTH_LONG);
	            t.show();
	        }
	        default:
	            break;
	        }
	    }
	}

	private void handleUIRequest(String message)
	{
	    Message msg = uiHandler.obtainMessage(UIHandler.DISPLAY_UI_TOAST);
	    msg.obj = message;
	    uiHandler.sendMessage(msg);
	}
}

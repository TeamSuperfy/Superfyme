package de.enterprise.lokaAndroid.activities;

import java.io.File;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.android.maps.GeoPoint;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaServer.pojos.LocationPojo;
import de.enterprise.lokaServer.pojos.PostNewPojo;

public class NewPostActivity extends SherlockActivity {

	private EditText edt_text;
	private ImageView imageView;
	private Button btnSend;
	private ImageButton btnCtg1, btnCtg2, btnCtg3, btnCtg4;
	private IMyService myServiceBinder;
	private final int FROM_CAMERA = 0, FROM_GALERY = 1;
	private Uri mCapturedImageURI;
	private ImageButton selectedCategory;
	private int selectedCategoryIndex = 1;
	private static final int COLOR_SELECTED = 0x00ffffff;
	private static final int COLOR_NORMAL = 0xffffffff;
	private boolean changedPic = false;

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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		getSupportActionBar().hide();
        Intent serviceIntent = new Intent(this, MyService.class);
        bindService(serviceIntent, sc, 0);
		createLayout();
		takePhoto();
	}

	@Override
	public void onDestroy() {
		if (imageView != null) {
			imageView.setImageBitmap(null);
		}
		unbindService(sc);
		super.onDestroy();
	}

    @Override
    public void onResume(){
    	super.onResume();
    }
    
    private String getPath(Uri uri) 
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
//        cursor.close();
        return s;
    }

    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			Bitmap bm = null;
		   if (resultCode != RESULT_OK) return;
	       switch (requestCode) {
	       case FROM_CAMERA:
	    	   bm = ImageHelperAndroid.readBitmap(mCapturedImageURI.getPath(), imageView.getWidth(), getResources().getDisplayMetrics().density);
	    	   bm = ImageHelperAndroid.getRoundedCornerBitmap(this, bm, 3, bm.getWidth(), bm.getHeight(), false, false, false, false);
	    	   imageView.setImageBitmap(bm);
	    	   new File(mCapturedImageURI.getPath()).delete();
	    	   changedPic = true;

	       break;
	 
	       case FROM_GALERY:
		       mCapturedImageURI = data.getData();
		       bm = ImageHelperAndroid.readBitmap(getPath(mCapturedImageURI), imageView.getWidth(), getResources().getDisplayMetrics().density);
	    	   bm = ImageHelperAndroid.getRoundedCornerBitmap(this, bm, 3, bm.getWidth(), bm.getHeight(), false, false, false, false);
	    	   imageView.setImageBitmap(bm);
	    	   changedPic = true;
		         
	       break;
	       }

	}

	private boolean checkForm() {
		if(changedPic && !edt_text.getText().equals("")){
				return true;
		}
		return false;
	}

	private PostNewPojo generatePost() {
		if (myServiceBinder != null) {
			PostNewPojo p = new PostNewPojo();
			Location loc = myServiceBinder.getLocation();
			if(loc != null){
				GeoPoint geo = new GeoPoint((int) (loc.getLatitude() * 1E6),
						(int) (loc.getLongitude() * 1E6));
				p.setLocation(new LocationPojo(geo.getLatitudeE6(), geo
						.getLongitudeE6()));
				p.setDate(System.currentTimeMillis());
				p.setText(edt_text.getText().toString());
				p.setUserID(myServiceBinder.getUser().getId());
				p.setPicData(ImageHelperAndroid.bitmapToByte(((BitmapDrawable)imageView.getDrawable()).getBitmap()));
				p.setGroupID(myServiceBinder.getSelectedGroup().getId());
				p.setAnonymous(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("anonymous", false));
				p.setCategory(selectedCategoryIndex);
				imageView.setImageBitmap(null);
				return p;
		}else{
			return null;
		}
	}
		return null;
	}

	private void onClickSendPost() {
		if (checkForm()) {
			PostNewPojo pnp = generatePost();
			if(pnp != null){
				myServiceBinder.sendPost(pnp);
				Toast.makeText(this, getResources().getString(R.string.sent_post), Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, getResources().getString(R.string.no_location_available), Toast.LENGTH_SHORT).show();
			}
			finish();
		}else{
			Toast.makeText(this, getResources().getString(R.string.check_form), Toast.LENGTH_SHORT).show();
		}
	}

	private void takePhoto() {
//		if(imageView.getDrawable() != null){
//			imageView.setImageBitmap(null);
//		}
		final String [] items = new String [] {getResources().getString(R.string.camera), getResources().getString(R.string.gallery)};
		 ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
		 AlertDialog.Builder builder  = new AlertDialog.Builder(this);
		 
		 builder.setTitle(getResources().getString(R.string.choose_picture));
		 builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
		    public void onClick( DialogInterface dialog, int item ) { //pick from camer
		    if (item == 0) {
		        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		 
		        mCapturedImageURI = Uri.fromFile(new File( 	Environment.getExternalStorageDirectory(),
		                 "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
		 
		        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
		 
		        try {
		        intent.putExtra("return-data", true);
		 
		        startActivityForResult(intent, FROM_CAMERA);
		        } catch (ActivityNotFoundException e) {
		        e.printStackTrace();
		        }
		       } else { //pick from file
		       Intent intent = new Intent();
		 
		       intent.setType("image/*");
		       intent.setAction(Intent.ACTION_GET_CONTENT);
		 
		       startActivityForResult(Intent.createChooser(intent, "Complete action using"), FROM_GALERY);
		       }
		    }
		 } );
		 
		builder.show();
	}

	private void createLayout() {
		setContentView(R.layout.create_post);

		imageView = (ImageView) findViewById(R.id.imgPostImage);
		imageView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				takePhoto();
			}
			
		});
		
		edt_text = (EditText) findViewById(R.id.edtPostText);
		edt_text.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				//did the user press enter?
				if(event!=null){
				InputMethodManager imm = (InputMethodManager)NewPostActivity.this.getSystemService(
					      Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});
		btnSend = (Button) findViewById(R.id.btnSendPost);
		btnSend.setClickable(true);
		btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickSendPost();
			}

		});
		
		btnCtg1 = (ImageButton) findViewById(R.id.btnCtg1);
		btnCtg1.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				runOnUiThread(new CategoryRunnable(v));
				selectedCategoryIndex = 1;
			}
			
		});
		btnCtg1.setColorFilter(COLOR_SELECTED);
		selectedCategory = btnCtg1;
		btnCtg2 = (ImageButton) findViewById(R.id.btnCtg2);
		btnCtg2.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				runOnUiThread(new CategoryRunnable(v));
				selectedCategoryIndex = 2;
			}
			
		});
		btnCtg2.setColorFilter(COLOR_NORMAL);
		btnCtg3 = (ImageButton) findViewById(R.id.btnCtg3);
		btnCtg3.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				runOnUiThread(new CategoryRunnable(v));
				selectedCategoryIndex = 3;
			}
			
		});
		btnCtg3.setColorFilter(COLOR_NORMAL);
		btnCtg4 = (ImageButton) findViewById(R.id.btnCtg4);
		btnCtg4.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				runOnUiThread(new CategoryRunnable(v));
				selectedCategoryIndex = 4;
			}
			
		});
		btnCtg4.setColorFilter(COLOR_NORMAL);
	}
	
	private class CategoryRunnable implements Runnable{
		private View v;
		public CategoryRunnable(View v){
			this.v = v;
		}
		public void run(){
			if(selectedCategory != null)
				selectedCategory.setColorFilter(COLOR_NORMAL);
			selectedCategory = (ImageButton) v;
			selectedCategory.setColorFilter(COLOR_SELECTED);
		}
	};
}
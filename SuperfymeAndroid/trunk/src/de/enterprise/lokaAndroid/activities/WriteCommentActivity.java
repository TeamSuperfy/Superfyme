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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.services.MyServiceBinder;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaServer.pojos.CommentNewPojo;
import de.enterprise.lokaServer.pojos.LocationPojo;

public class WriteCommentActivity extends SherlockActivity {

	private EditText commentText;
	private ImageView imageView;
    private Uri mCapturedImageURI;
	private int postID;
	private final int FROM_CAMERA = 0, FROM_GALERY = 1;

	private MyServiceBinder myServiceBinder;

	private ServiceConnection sc = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myServiceBinder = (MyServiceBinder) service;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		postID = getIntent().getExtras().getInt("postID");
		setContentView(R.layout.create_comment);
		commentText = (EditText) findViewById(R.id.edtCommentText);
		commentText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				//did the user press enter?
				if(event!=null){
				InputMethodManager imm = (InputMethodManager)getSystemService(
					      Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});
		
		Button button1 = (Button) findViewById(R.id.btnAddPhoto);
		button1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
	    	   	addImageView();
				takePhoto();
			}

		});
		Button button2 = (Button) findViewById(R.id.btnSendComment);
		button2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				onClickSendComment();
			}

		});
		Intent serviceIntent = new Intent(this, MyService.class);
		bindService(serviceIntent, sc, 0);
	}

	public void onDestroy() {
		if (imageView != null) {
			imageView.setImageBitmap(null);
		}
		unbindService(sc);
		super.onDestroy();
	}
	
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

	private void takePhoto() {
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
	
    private String getPath(Uri uri) 
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
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
	       break;
	 
	       case FROM_GALERY:
		       mCapturedImageURI = data.getData();
		       bm = ImageHelperAndroid.readBitmap(getPath(mCapturedImageURI), imageView.getWidth(), getResources().getDisplayMetrics().density);
	    	   bm = ImageHelperAndroid.getRoundedCornerBitmap(this, bm, 3, bm.getWidth(), bm.getHeight(), false, false, false, false);
	    	   imageView.setImageBitmap(bm);
	       break;
	       }

	}

	private void addImageView() {
		ViewGroup layout = (ViewGroup) getLayoutInflater().inflate(
				R.layout.create_comment, null);
		ViewGroup layoutContainer = (ViewGroup) layout
				.findViewById(R.id.layoutContainer);
		ImageView view = new ImageView(this);
		final float scale = getResources().getDisplayMetrics().density;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0);
		params.setMargins((int) (5 * scale), (int) (5 * scale), (int) (5 * scale), (int) (5 * scale));
		view.setAdjustViewBounds(true);
		view.setMaxHeight((int)(300 * scale));
		layoutContainer.addView(view, 1, params);
		imageView = view;

		commentText = (EditText) layout.findViewById(R.id.edtCommentText);
		Button button1 = (Button) layout.findViewById(R.id.btnAddPhoto);
		button1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				takePhoto();
			}

		});
		Button button2 = (Button) layout.findViewById(R.id.btnSendComment);
		button2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				onClickSendComment();
			}

		});
		
		layout.invalidate();
		setContentView(layout);
	}

	private boolean checkForm() {
		if (imageView != null || commentText.getText().length() > 0)
			return true;
		return false;
	}

	private void onClickSendComment() {
		if (checkForm()) {
			CommentNewPojo comment = generateComment();
			if(comment != null){
				myServiceBinder.sendNewComment(comment);
				Toast.makeText(this, getResources().getString(R.string.sent_comment), Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, getResources().getString(R.string.no_location_available), Toast.LENGTH_SHORT).show();
			}
			finish();
		}
	}

	private CommentNewPojo generateComment() {

		CommentNewPojo newComment = new CommentNewPojo();
		newComment.setDate(System.currentTimeMillis());
		Location loc = myServiceBinder.getLocation();
		if(loc != null){
			newComment.setLocation(new LocationPojo(
					(int) (loc.getLatitude() * 1E6),
					(int) (loc.getLongitude() * 1E6)));
			if (imageView != null) {
				newComment.setPicData(ImageHelperAndroid.bitmapToByte(((BitmapDrawable)imageView.getDrawable()).getBitmap()));
			}
			newComment.setPostID(postID);
			newComment.setText(commentText.getText().toString());
			newComment.setUserID(myServiceBinder.getUser().getId());
			newComment.setAnonymous(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("anonymous", false));
			return newComment;
		}else{
			return null;
		}
	}

}

package de.enterprise.lokaAndroid.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.MyService;
import de.enterprise.lokaAndroid.tools.CropOption;
import de.enterprise.lokaAndroid.tools.CropOptionAdapter;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.LocationPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;

public class CreateGroupActivity extends SherlockActivity {

	private EditText edtGroupName;
	private Button btnCreate;
	private CheckBox cbPublic;
	private ImageView imgGroupImage;
	private final int FROM_CAMERA = 0, FROM_GALERY = 1, CROP_FROM_CAMERA=2;
	private Uri mCapturedImageURI;
	private boolean changedGroupIcon = false;
	
	private IMyService myService;
	
	private ServiceConnection sc = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			myService = (IMyService) binder;
			registerListener();
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	};
	
	private Handler newGroupHandler = new Handler(){
		@Override
		public void handleMessage(Message m){
			int groupID = Integer.parseInt(m.getData().getString("json"));
			UserPojo user = myService.getUser();
			int[] groups = user.getGroups();
			if(groups == null){
				groups = new int[]{groupID};
			}
			else{
				groups = CollectionStuff.appendToArray(groups, new int[]{groupID});
			}
			user.setGroups(groups);
			user.setGroups_available(0);
			myService.updateUser(user);
			Toast.makeText(CreateGroupActivity.this, "group created", Toast.LENGTH_SHORT).show();
			finish();
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_group_layout);
		imgGroupImage = (ImageView) findViewById(R.id.imgGroupIcon);
		imgGroupImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				takeGroupPicture();
			}
		});
		btnCreate = (Button) findViewById(R.id.btnCreateGroup);
		btnCreate.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				createGroup();
			}
			
		});
		edtGroupName = (EditText) findViewById(R.id.edtGroupName);
		edtGroupName.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				if(edtGroupName.getText().length() > 0){
					btnCreate.setEnabled(true);
				}else{
					btnCreate.setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
		});
		
		cbPublic = (CheckBox) findViewById(R.id.cbPublic);
		cbPublic.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					Location position = myService.getLocation();
					if(position == null){
						cbPublic.setChecked(false);
						Toast.makeText(CreateGroupActivity.this, "No position available", Toast.LENGTH_SHORT).show();
						return;
					}
				}
			}
		});
		
		getSupportActionBar().hide();
		
		Intent service = new Intent(this, MyService.class);
		bindService(service, sc, 0);
	}
	
	private void createGroup(){
		GroupPojo gp = new GroupPojo();
		gp.setName(edtGroupName.getText().toString());
		gp.setCreator_id(myService.getUser().getId());
		gp.setMembers(new int[]{myService.getUser().getId()});
		gp.setMember_count(1);
		Location position = myService.getLocation();
		if(cbPublic.isChecked()){
			gp.setLocation(new LocationPojo((int)(position.getLatitude() * 1e6), (int)(position.getLongitude() * 1e6)));
		}
		if(changedGroupIcon){
			Bitmap bmp = ((BitmapDrawable)imgGroupImage.getDrawable()).getBitmap();
			gp.setPic(ImageHelperAndroid.bitmapToByte(bmp));
		}
		gp.setIs_public(cbPublic.isChecked());
		
		myService.createGroup(gp);
		btnCreate.setEnabled(false);
	}
	
	@Override
	public void onDestroy(){
		unregisterListener();
		unbindService(sc);
		super.onDestroy();
	}
	
	private void takeGroupPicture(){
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
		 
		        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
		 
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
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	   if (resultCode != RESULT_OK) return;
	       switch (requestCode) {
	       case FROM_CAMERA:
	        	  doCrop();
	       break;
	 
	       case FROM_GALERY:
		         mCapturedImageURI = data.getData();
	             doCrop();
	       break;
	       case CROP_FROM_CAMERA:
                Bundle extras = data.getExtras();
 
                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    imgGroupImage.buildDrawingCache();
                    Bitmap bmap = imgGroupImage.getDrawingCache();

                    photo = ImageHelperAndroid.getResizedBitmap(photo, bmap.getHeight(), bmap.getWidth());
                    imgGroupImage.setImageBitmap(photo);
                    changedGroupIcon = true;
                    InputMethodManager inputManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    inputManager.restartInput(edtGroupName);
                }
 
                File f = new File(mCapturedImageURI.getPath());
                if (f.exists()) f.delete();
	 
	       break;
	       }

	}
	
	
	private void registerListener(){
		if(myService != null){
			myService.registerListener("CG", newGroupHandler);
		}
	}
	
	private void unregisterListener(){
		if(myService != null){
			myService.registerListener("CG", newGroupHandler);
		}
	}
	
	private void doCrop() {
        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
 
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
 
        List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );
 
        int size = list.size();
 
        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
 
            return;
        } else {
            intent.setData(mCapturedImageURI);
 
            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
 
            if (size == 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);
 
                i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
 
                startActivityForResult(i, CROP_FROM_CAMERA);
            } else {
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();
 
                    co.title    = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon     = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                    co.appIntent= new Intent(intent);
 
                    co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
 
                    cropOptions.add(co);
                }
 
                CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);
 
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.crop_image));
                builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int item ) {
                        startActivityForResult( cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
                    }
                });
 
                builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel( DialogInterface dialog ) {
 
                        if (mCapturedImageURI != null ) {
                            getContentResolver().delete(mCapturedImageURI, null, null );
                            mCapturedImageURI = null;
                        }
                    }
                } );
 
                builder.show();
            }
        }
    }
	
}

package de.enterprise.lokaAndroid.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.tools.CropOption;
import de.enterprise.lokaAndroid.tools.CropOptionAdapter;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;
import de.enterprise.lokaServer.pojos.UserPojo;

public class NewUserDialog extends DialogFragment {
	
	private EditText edtUsername;
	private ImageView imgUserPic;
	private Button btnOK;
	private TextView lblErrorMsg;
	private Uri mCapturedImageURI;
	private final int FROM_CAMERA = 0, FROM_GALERY = 1, CROP_FROM_CAMERA=2;
	private boolean changedUserPic = false;
	private IMyService msb;
	private NewUserDialogCallback callback = null;
	
	private static final int CODE_OK = 0, CODE_ALREADY_IN_USE = -1;

	private Handler usernameHandler = new Handler(){
		public void handleMessage(Message m){
			String msg = m.getData().getString("json");
			int code = Integer.parseInt(msg);
			if(code == CODE_OK){
				msb.unregisterListener("NU", usernameHandler);
				UserPojo user = msb.getUser();
				user.setUsername(edtUsername.getText().toString());
				user.setGroups_available(1);
				msb.updateUser(user);
				if(callback != null){
					callback.finishedInput();
				}
				dismiss();
			}else if(code == CODE_ALREADY_IN_USE){
				lblErrorMsg.setVisibility(View.VISIBLE);
				lblErrorMsg.setText("Username '" + edtUsername.getText() + "' already in use!");
				edtUsername.setText("");
				edtUsername.requestFocus();
			}
		}
	};
	
	public interface NewUserDialogCallback{
		void finishedInput();
	}

	public void onAttach(Activity a){
		super.onAttach(a);
		try{
			callback = (NewUserDialogCallback) a;
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}
	
	public void setServiceConnection(IMyService myService){
		msb = myService;
		msb.registerListener("NU", usernameHandler);
	}
	
	public static NewUserDialog newInstance(IMyService myService) {
		NewUserDialog d = new NewUserDialog();
		d.setServiceConnection(myService);
        return d;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.new_user_dialog, container, false);
        imgUserPic = (ImageView) v.findViewById(R.id.imgUserPic);
        imgUserPic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				takeUserPic();
			}
		});
        btnOK = (Button) v.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//TODO send user stuff to server...
				if(changedUserPic){
					msb.newUser(edtUsername.getText().toString(), ((BitmapDrawable)imgUserPic.getDrawable()).getBitmap());
				}
				else{
					msb.newUser(edtUsername.getText().toString(), null);
				}
			}
        });
        
        edtUsername = (EditText) v.findViewById(R.id.edtUsername);
        edtUsername.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable txt) {
				if(txt.length() > 0){
					btnOK.setEnabled(true);
				}else{
					btnOK.setEnabled(false);
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
        
        lblErrorMsg = (TextView) v.findViewById(R.id.lblErrorMsg);
        
        getDialog().setTitle(R.string.welcome_to_superfyme);
        
        return v;
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	   if (resultCode != Activity.RESULT_OK) return;
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
                    imgUserPic.buildDrawingCache();
                    Bitmap bmap = imgUserPic.getDrawingCache();

                    photo = ImageHelperAndroid.getResizedBitmap(photo, bmap.getHeight(), bmap.getWidth());
                    imgUserPic.setImageBitmap(photo);
                    changedUserPic = true;
                    InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputManager.restartInput(edtUsername);
                }
 
                File f = new File(mCapturedImageURI.getPath());
                if (f.exists()) f.delete();
	 
	       break;
	       }

	}
    
	private void takeUserPic(){
		final String [] items = new String [] {getResources().getString(R.string.camera), getResources().getString(R.string.gallery)};
		 ArrayAdapter<String> adapter = new ArrayAdapter<String> (this.getActivity(), android.R.layout.select_dialog_item,items);
		 AlertDialog.Builder builder  = new AlertDialog.Builder(this.getActivity());
		 
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
	
	private void doCrop() {
        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
 
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
 
        List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities( intent, 0 );
 
        int size = list.size();
 
        if (size == 0) {
            Toast.makeText(getActivity(), "Can not find image crop app", Toast.LENGTH_SHORT).show();
 
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
 
                    co.title    = getActivity().getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon     = getActivity().getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                    co.appIntent= new Intent(intent);
 
                    co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
 
                    cropOptions.add(co);
                }
 
                CropOptionAdapter adapter = new CropOptionAdapter(getActivity().getApplicationContext(), cropOptions);
 
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                        	getActivity().getContentResolver().delete(mCapturedImageURI, null, null );
                            mCapturedImageURI = null;
                        }
                    }
                } );
 
                builder.show();
            }
        }
    }
}

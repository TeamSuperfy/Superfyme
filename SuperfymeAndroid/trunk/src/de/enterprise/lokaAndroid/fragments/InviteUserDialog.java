package de.enterprise.lokaAndroid.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.activities.GroupInfo;
import de.enterprise.lokaAndroid.services.IMyService;

public class InviteUserDialog extends DialogFragment {
	
	private EditText edtUsername;
	private Button btnCancel;
	private Button btnInvite;
	private TextView lblErrorMsg;
	private GroupInfo groupInfo;
	
	private IMyService msb;
	private int groupID;
	
	private static final int CODE_NOT_FOUND = -1, CODE_ALREADY_MEMBER = -2, CODE_ALREADY_INVITED = -3;
	
	private Handler inviteHandler = new Handler(){
		public void handleMessage(Message m){
			String msg = m.getData().getString("json");
			int code = Integer.parseInt(msg);
			if(code > 0){
				msb.unregisterListener("IU", inviteHandler);
				groupInfo.addInvitedUser(code);
				dismiss();
			}else if(code == CODE_NOT_FOUND){
				lblErrorMsg.setText("User '"+edtUsername.getText().toString()+"' not found!");
				lblErrorMsg.setVisibility(View.VISIBLE);
				edtUsername.requestFocus();
			}else if(code == CODE_ALREADY_MEMBER){
				lblErrorMsg.setText("User '"+edtUsername.getText().toString()+"' is already a member of this group!");
				lblErrorMsg.setVisibility(View.VISIBLE);
				edtUsername.requestFocus();
			}else if(code == CODE_ALREADY_INVITED){
				lblErrorMsg.setText("User '"+edtUsername.getText().toString()+"' has already been invited!");
				lblErrorMsg.setVisibility(View.VISIBLE);
				edtUsername.requestFocus();
			}
		}
	};
	
    private InviteUserDialog(IMyService msb, int groupID, GroupInfo groupInfo) {
    	super();
    	this.msb = msb;
    	this.groupID = groupID;
    	this.groupInfo = groupInfo;
    	msb.registerListener("IU", inviteHandler);
	}

	public static InviteUserDialog newInstance(IMyService msb, int groupID, GroupInfo groupInfo) {
        return new InviteUserDialog(msb, groupID, groupInfo);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.invite_user, container, false);
        
        edtUsername = (EditText) v.findViewById(R.id.edtUsername);
        edtUsername.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable arg0) {
				if(edtUsername.getText().length() > 0){
					btnInvite.setEnabled(true);
				}else{
					btnInvite.setEnabled(false);
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
        btnCancel = (Button) v.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				msb.unregisterListener("IU", inviteHandler);
				dismiss();
			}
        	
        });
        btnInvite = (Button) v.findViewById(R.id.btnInvite);
        btnInvite.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				msb.inviteUser(groupID, edtUsername.getText().toString());
			}
        	
        });
        
        lblErrorMsg = (TextView) v.findViewById(R.id.lblErrorMsg);
        
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        getDialog().setTitle(R.string.invite_user);
        
        return v;
    }
}

package de.enterprise.lokaAndroid.fragments;

import android.os.Bundle;
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
import de.enterprise.lokaAndroid.R;

public class GoToLocationDialog extends DialogFragment {
	
	private EditText edtLocation;
	private Button btnFindLocation;
	private MapFragment mMapFragment;
	
    private GoToLocationDialog(MapFragment mMapFragment) {
    	super();
    	this.mMapFragment = mMapFragment;
	}

	public static GoToLocationDialog newInstance(MapFragment mMapFragment) {
        return new GoToLocationDialog(mMapFragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.go_to_location_layout, container, false);
        btnFindLocation = (Button) v.findViewById(R.id.btnFindLocation);
        btnFindLocation.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mMapFragment.goToLocation(edtLocation.getText().toString());
				dismiss();
			}
        });
        
        edtLocation = (EditText) v.findViewById(R.id.edtLocation);
        edtLocation.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable txt) {
				if(txt.length() > 0){
					btnFindLocation.setEnabled(true);
				}else{
					btnFindLocation.setEnabled(false);
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
        
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        getDialog().setTitle(R.string.go_to_location);
        
        return v;
    }
}

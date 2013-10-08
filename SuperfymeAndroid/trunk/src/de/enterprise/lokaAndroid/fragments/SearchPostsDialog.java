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
import android.widget.ImageButton;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.IMyService;

public class SearchPostsDialog extends DialogFragment {
	
	private IMyService msb;
	private EditText edtSearchWord;
	private Button btnSearchPosts;
	private MapFragment mMapFragment;
	private ImageButton btnClear;
	
    private SearchPostsDialog(IMyService msb, MapFragment mMapFragment) {
    	super();
    	this.msb = msb;
    	this.mMapFragment = mMapFragment;
	}

	public static SearchPostsDialog newInstance(IMyService msb, MapFragment mMapFragment) {
        return new SearchPostsDialog(msb, mMapFragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_posts_layout, container, false);
        btnSearchPosts = (Button) v.findViewById(R.id.btnSearchPosts);
        btnSearchPosts.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				msb.setSearchWord(edtSearchWord.getText().toString());
				mMapFragment.orderPlacemarks();
				dismiss();
			}
        });
        
        btnClear = (ImageButton) v.findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				msb.setSearchWord("");
				mMapFragment.orderPlacemarks();
				dismiss();
			}
        });
        
        edtSearchWord = (EditText) v.findViewById(R.id.edtSearchWord);
        edtSearchWord.setText(msb.getSearchWord());
        edtSearchWord.selectAll();
        edtSearchWord.requestFocus();
        if(edtSearchWord.getText().length() > 0){
        	btnClear.setVisibility(View.VISIBLE);
        }
        edtSearchWord.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable txt) {
				if(txt.length() > 0){
					btnSearchPosts.setEnabled(true);
				}else{
					btnSearchPosts.setEnabled(false);
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
        getDialog().setTitle(R.string.search_posts);
        
        return v;
    }
}

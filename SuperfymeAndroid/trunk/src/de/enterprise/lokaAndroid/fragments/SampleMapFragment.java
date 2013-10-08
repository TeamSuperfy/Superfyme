package de.enterprise.lokaAndroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.GoogleMap;

import de.enterprise.lokaAndroid.R;

public class SampleMapFragment extends SherlockFragment{

	private GoogleMap myMap;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle data) {
		return inflater.inflate(R.layout.maplayout, null);
	}
	
}

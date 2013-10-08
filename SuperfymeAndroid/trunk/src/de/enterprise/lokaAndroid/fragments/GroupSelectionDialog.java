package de.enterprise.lokaAndroid.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.adapters.GroupAdapter;
import de.enterprise.lokaAndroid.services.IMyService;

@SuppressWarnings("ucd")
public class GroupSelectionDialog extends DialogFragment {
	
	private GroupAdapter adapter;
	private IMyService msb;
	
    public GroupSelectionDialog(GroupAdapter adapter, IMyService msb, MapFragment mMapFragment) {
    	super();
    	this.adapter = adapter;
    	this.adapter.setMapFragment(mMapFragment);
    	this.msb = msb;
	}

	public static GroupSelectionDialog newInstance(GroupAdapter adapter, IMyService msb, MapFragment mMapFragment) {
        return new GroupSelectionDialog(adapter, msb, mMapFragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.group_dialog, container, false);
        ListView lv = (ListView) v.findViewById(R.id.listGroups);
        lv.setAdapter(adapter);
        adapter.setDialog(this);
        adapter.setIMyService(msb);
        getDialog().setTitle(R.string.choose_group);
        return v;
    }
}

package de.enterprise.lokaAndroid.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.MapView;

import de.enterprise.lokaAndroid.R;

public class MapswithfragmentsActivity extends SherlockFragmentActivity {
	
	private MapFragment mMapFragment;
	private MyListFragment mMyListFragment;
	
	// We use this fragment as a pointer to the visible one, so we can hide it easily.
	private Fragment mVisible = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // We instantiate the MapView here, it's really important!
        Exchanger.mMapView = new MyMapView(this, this.getResources().getString(R.string.api_key));
        
        setupFragments();
        // We manually show the list Fragment.
        showFragment(mMapFragment);
    }

	/**
	 * This method does the setting up of the Fragments. It basically checks if
	 * the fragments exist and if they do, we'll hide them. If the fragments
	 * don't exist, we create them, add them to the FragmentManager and hide
	 * them.
	 */
	private void setupFragments() {
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		// If the activity is killed while in BG, it's possible that the
		// fragment still remains in the FragmentManager, so, we don't need to
		// add it again.
		mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MapFragment.TAG);
        if (mMapFragment == null) {
        	mMapFragment = new MapFragment();
        	ft.add(R.id.fragment_container, mMapFragment, MapFragment.TAG);
        }
        ft.hide(mMapFragment);
        
        mMyListFragment = (MyListFragment) getSupportFragmentManager().findFragmentByTag(MyListFragment.TAG);
        if (mMyListFragment == null) {
        	mMyListFragment = new MyListFragment();
        	ft.add(R.id.fragment_container, mMyListFragment, MyListFragment.TAG);
        }
        ft.hide(mMyListFragment);
        
        ft.commit();
	}
	
	/**
	 * This method shows the given Fragment and if there was another visible
	 * fragment, it gets hidden. We can just do this because we know that both
	 * the mMyListFragment and the mMapFragment were added in the Activity's
	 * onCreate, so we just create the fragments once at first and not every
	 * time. This will avoid facing some problems with the MapView.
	 * 
	 * @param fragmentIn
	 *            The fragment to show.
	 */
	private void showFragment(Fragment fragmentIn) {
		if (fragmentIn == null) return;
		
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
		
		if (mVisible != null) ft.hide(mVisible);
		
		ft.show(fragmentIn).commit();
		mVisible = fragmentIn;
	}
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	// Inflate the menu with the options to show the Map and the List.
    	getSupportMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.ic_list:
			// Show mMyListFragment.
			mMyListFragment.initView();
			showFragment(mMyListFragment);
			return true;
			
		case R.id.ic_map:
			// Show mMapFragment.
			showFragment(mMapFragment);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	

}


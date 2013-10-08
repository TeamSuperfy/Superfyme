package de.enterprise.lokaAndroid.activities;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class CustomItemizedOverlay extends ItemizedOverlay<OverlayItem> {
   
   private ArrayList<OverlayItem> mapOverlays = new ArrayList<OverlayItem>();
   
   private Context context;
   
   public CustomItemizedOverlay(Drawable defaultMarker) {
        super(boundCenterBottom(defaultMarker));
   }
   
   public CustomItemizedOverlay(Drawable defaultMarker, Context context) {
        this(defaultMarker);
        this.context = context;
        populate(); // Add this
   }

   @Override
   protected OverlayItem createItem(int i) {
      return mapOverlays.get(i);
   }

   @Override
   public int size() {
      return mapOverlays.size();
   }
   
   @Override
   protected boolean onTap(int index) {
      OverlayItem item = mapOverlays.get(index);
      int id = Integer.parseInt(item.getTitle());
      Intent detail = new Intent(context, PostDetailActivity.class);
      detail.putExtra("id", id);
      context.startActivity(detail);
      return true;
   }
   
   public void deleteItems(){
	   mapOverlays.clear();
   }
   
   public void addOverlay(OverlayItem overlay) {
      mapOverlays.add(overlay);
      populate();
   }

}


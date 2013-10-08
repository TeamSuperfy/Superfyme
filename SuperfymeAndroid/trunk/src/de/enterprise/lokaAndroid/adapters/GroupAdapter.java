package de.enterprise.lokaAndroid.adapters;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.activities.GroupInfo;
import de.enterprise.lokaAndroid.fragments.MapFragment;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.MyService.MapMode;
import de.enterprise.lokaServer.pojos.GroupPojo;
 
public class GroupAdapter extends ArrayAdapter<GroupPojo> {
	private final Context context;
	private DialogFragment myDialog;
	private IMyService msb;
	private MapFragment mMapFragment;
	public static final int LEAVE_DELETE = 0, GROUP_INFO = 1;
	
	public GroupAdapter(Context context, ArrayList<GroupPojo> posts) {
		super(context, R.layout.message_item, posts);
		this.context = context;
	}
	
	private static class ViewHolder {
	    public TextView text;
	    public ImageView image;
	    public GroupPojo group;
	    public TextView post_count;
	}
	
	public void setDialog(DialogFragment dialog){
		myDialog = dialog;
	}
	
	public void setIMyService(IMyService msb){
		this.msb = msb;
	}
	
	public void setMapFragment(MapFragment mMapFragment){
		this.mMapFragment = mMapFragment;
	}
	
	public void addAllTrick(GroupPojo[] items){
		for (GroupPojo groupPojo : items) {
			add(groupPojo);
		}
	}

 
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewHolder viewHolder;
		
	
		if(v == null){
			v = inflater.inflate(R.layout.group_item, parent, false);
			viewHolder = new ViewHolder();
			//get gui components
			viewHolder.text = (TextView) v.findViewById(R.id.lblGroupname);
			viewHolder.image = (ImageView) v.findViewById(R.id.imgGroupIcon);
			viewHolder.post_count = (TextView) v.findViewById(R.id.lblPostCount);
			v.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					msb.setSelectedGroup(((ViewHolder)view.getTag()).group);
					mMapFragment.resetTimer();
					mMapFragment.orderPlacemarks();
					if(msb.getMapMode() == MapMode.Posts){
						msb.orderPostsList();
					}
					if(myDialog != null){
						myDialog.dismiss();
					}
				}
			
			});
			v.setOnLongClickListener(new OnLongClickListener() {
			    @Override
			    public boolean onLongClick(View v) {
			    	final GroupPojo group = ((ViewHolder)v.getTag()).group;
			    	if(group.getId() > 0){
				    	String [] items;
				    	if(group.getCreator_id() == msb.getUser().getId()){
				    		items = new String [] {
				    				context.getResources().getString(R.string.delete_group),
				    				context.getResources().getString(R.string.group_info)};
				    	}else{
				    		items = new String [] {
				    				context.getResources().getString(R.string.leave_group),
				    				context.getResources().getString(R.string.group_info)};
				    	}
				    	
						 ArrayAdapter<String> adapter = new ArrayAdapter<String> (context, android.R.layout.select_dialog_item,items);
						 AlertDialog.Builder builder  = new AlertDialog.Builder(context);
						 final int groupID = group.getId();
						 final int creatorID = group.getCreator_id();
						 
						 builder.setAdapter( adapter, new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int item) {
						    	switch(item){
						    	case LEAVE_DELETE: 
						    		
							    	if(creatorID == msb.getUser().getId()){
							    		msb.deleteGroup(groupID);
							    		msb.getUser().setGroups_available(1);
						    			for (int i = 0; i < GroupAdapter.this.getCount(); i++) {
											GroupPojo gp = GroupAdapter.this.getItem(i);
											if(gp.getId() == groupID){
												GroupAdapter.this.remove(gp);
												break;
											}
										}
							    	}else{
							    		msb.leaveGroup(groupID);
						    			for (int i = 0; i < GroupAdapter.this.getCount(); i++) {
											GroupPojo gp = GroupAdapter.this.getItem(i);
											if(gp.getId() == groupID){
												GroupAdapter.this.remove(gp);
												break;
											}
										}
							    	}
						    		
		
						    		;break;
						    	case GROUP_INFO:
						    		Intent groupInfoIntent = new Intent(context, GroupInfo.class);
						    		groupInfoIntent.putExtra("group", group);
						    		context.startActivity(groupInfoIntent);
						    		myDialog.dismiss();
						    		;break;
						    	}
							}
							 
						 });
						 
						builder.show();
				    	return true;
				    }
			    	return true;
			    }

			});

			v.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) v.getTag();
		}
		


		GroupPojo group = getItem(position);
		if(group != null){
			//fill the gui components
			if(msb.getSelectedGroup().getId() == group.getId()){
				v.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.roundrec));
			}
			else{
				v.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.roundrec_white));
			}
			viewHolder.group = group;
			
			if(group.getId() > 0){
				viewHolder.text.setText(group.getName());
			}
			else{
				viewHolder.text.setText("-"+context.getResources().getString(R.string.openly)+"-");
			}
			
			if(group.getPic_id() > 0){
				Bitmap bm = msb.getIconPic(group.getPic_id());
				if(bm != null){
					viewHolder.image.setImageBitmap(bm);
				}else{
					viewHolder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.group_icon));
				}
			}else{
				viewHolder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.group_icon));
			}
			if(group.getId() > 0)
				viewHolder.post_count.setText("(" + group.getPost_count() + " posts)");
			else{
				viewHolder.post_count.setText("");
			}
		}
		
		return v;
	}
}

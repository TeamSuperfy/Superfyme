package de.enterprise.lokaAndroid.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;
 
public class GroupArrayAdapter extends ArrayAdapter<GroupPojo> {
	private final Context context;
	private IMyService msb;
 
	public GroupArrayAdapter(Context context, ArrayList<GroupPojo> groups, IMyService msb) {
		super(context, R.layout.group_item, groups);
		this.context = context;
		this.msb = msb;
	}
	
	public static class ViewHolder {
		public ImageView groupImage;
		public TextView groupName;
		public TextView memberCount;
		public Button joinButton;
	}
	
	public void addAllTrick(GroupPojo[] items){
		for (GroupPojo groupPojo : items) {
			add(groupPojo);
		}
	}
	
	private void inflateGroupLayout(ViewHolder viewHolder, View v){
		viewHolder.groupImage = (ImageView) v.findViewById(R.id.imgGroupIcon);
		viewHolder.groupName = (TextView) v.findViewById(R.id.lblGroupname);
		viewHolder.memberCount = (TextView) v.findViewById(R.id.lblMembers);
		viewHolder.joinButton = (Button) v.findViewById(R.id.btnJoin);
		viewHolder.joinButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GroupPojo group = (GroupPojo) v.getTag();
				msb.joinGroup(group.getId());
				UserPojo user = msb.getUser();
				int[] groups = user.getGroups();
				if(groups == null){
					groups = new int[]{group.getId()};
				}
				else{
					groups = CollectionStuff.appendToArray(groups, new int[]{group.getId()});
				}
				user.setGroups(groups);
				group.setMember_count(group.getMember_count() + 1);
				group.setMembers(CollectionStuff.appendToArray(group.getMembers(), new int[]{user.getId()}));
				GroupArrayAdapter.this.notifyDataSetChanged();
			}
		});
		
		v.setTag(viewHolder);
	}
	
 
	@Override
	public View getView(final int position, View v, ViewGroup parent) {
		
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewHolder viewHolder = null;
		
		if(v == null){
			v = inflater.inflate(R.layout.group_search_item, parent, false);
			viewHolder = new ViewHolder();
			inflateGroupLayout(viewHolder, v);
		}else{
			viewHolder = (ViewHolder) v.getTag();
		}
			
	
	
		GroupPojo group = getItem(position);
		if(group != null){
			if(group.getPic_id() > 0){
				Bitmap bmp = msb.getIconPic(group.getPic_id());
				if(bmp != null){
					viewHolder.joinButton.setTag(group);
					viewHolder.groupImage.setImageBitmap(bmp);
				}else{
					msb.orderBigPicture(group.getPic_id());
					viewHolder.groupImage.setImageDrawable(context.getResources().getDrawable(R.drawable.group_icon));
				}
			}else{
				viewHolder.groupImage.setImageDrawable(context.getResources().getDrawable(R.drawable.group_icon));
			}
			
			viewHolder.groupName.setText(group.getName());
			viewHolder.memberCount.setText(group.getMember_count()+ context.getResources().getString(R.string.members));
			
			if(CollectionStuff.arrayContains(group.getMembers(), msb.getUser().getId())){
				viewHolder.joinButton.setEnabled(false);
			}else{
				viewHolder.joinButton.setEnabled(true);
			}
		}
		
		return v;
	}
}

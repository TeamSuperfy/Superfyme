package de.enterprise.lokaAndroid.adapters;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.UserPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;
 
public class UserAdapter extends ArrayAdapter<UserPojo> {
	private final Context context;
	private IMyService msb;
	private GroupPojo group;
	
	private static final int KICK = 0;
	
	public UserAdapter(Context context, ArrayList<UserPojo> users, IMyService msb, GroupPojo group) {
		super(context, R.layout.message_item, users);
		this.context = context;
		this.msb = msb;
		this.group = group;
	}
	
	private static class ViewHolder {
	    public TextView username;
	    public ImageView userImage;
	    public UserPojo user;
	}
	
	public void addAllTrick(UserPojo[] items){
		for (UserPojo userPojo : items) {
			add(userPojo);
		}
	}

 
	@Override
	public View getView(final int position, View v, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewHolder viewHolder;
		
	
		if(v == null){
			v = inflater.inflate(R.layout.user_item, parent, false);
			viewHolder = new ViewHolder();
			//get gui components
			viewHolder.username = (TextView) v.findViewById(R.id.lblUsername);
			viewHolder.userImage = (ImageView) v.findViewById(R.id.imgUserImage);
			v.setTag(viewHolder);
			v.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					final UserPojo user = ((ViewHolder)v.getTag()).user;
					if(group.getCreator_id() == msb.getUser().getId()){
						if(!CollectionStuff.arrayContains(group.getInvited_users(), user.getId())){
							if(user.getId() != msb.getUser().getId()){
								String [] items = {"Kicken"};
								ArrayAdapter<String> adapter = new ArrayAdapter<String> (context, android.R.layout.select_dialog_item,items);
								AlertDialog.Builder builder  = new AlertDialog.Builder(context);
								builder.setAdapter( adapter, new DialogInterface.OnClickListener(){
		
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										switch(which){
										case KICK:
											msb.removeUserFromGroup(group.getId(), user.getId());
											remove(getItem(position));
											;break;
										}
									}
									
								});
								builder.show();
							}
						}
					}
				}
			});
		}else{
			viewHolder = (ViewHolder) v.getTag();
		}
		


		UserPojo user = getItem(position);
		if(user != null){
			//fill the gui components
			viewHolder.user = user;
			if(user.getPicID() > 0){
				Bitmap bm = msb.getIconPic(user.getPicID());
				if(bm != null){
					viewHolder.userImage.setImageBitmap(bm);
				}else{
					msb.orderBigPicture(user.getPicID());
					viewHolder.userImage.setImageDrawable(context.getResources().getDrawable(R.drawable.group_icon));
				}
			}else{
				viewHolder.userImage.setImageDrawable(context.getResources().getDrawable(R.drawable.group_icon));
			}
			if(CollectionStuff.arrayContains(group.getInvited_users(),user.getId())){
				viewHolder.username.setText(user.getUsername() + "(invitation)");
				v.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.roundrec));
			}else{
				viewHolder.username.setText(user.getUsername());
				v.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.roundrec_darker));
			}
		}
		
		return v;
	}
}

package de.enterprise.lokaAndroid.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.activities.ChatActivity;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.tools.GeoStuff;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.MessagePojo;
 
public class MessagesAdapter extends ArrayAdapter<MessagePojo> {
	private final Context context;
	private IMyService msb;
	private ChatActivity chatActivity;
	private GroupPojo inviting_group;
	
	public MessagesAdapter(Context context, ArrayList<MessagePojo> posts, IMyService msb, ChatActivity parent) {
		super(context, R.layout.message_item, posts);
		this.context = context;
		this.msb = msb;
		this.chatActivity = parent;
	}
	
	public void setIMyService(IMyService msb){
		this.msb = msb;
	}
	
	private static class ViewHolderMessage {
	    public TextView text;
	    public TextView date;
	    public TextView username;
	    public int fromUID;
	    public int pID;
	    public int picID;
	}
	
	private static class ViewHolderGroupInvitation {
		public ImageView groupIcon;
		public TextView groupName;
		public Button delete;
		public Button accept;
		public MessagePojo msg;
	}
	
	public void addAllTrick(MessagePojo[] items){
		for (MessagePojo messagePojo : items) {
			add(messagePojo);
		}
	}

 
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		MessagePojo message = getItem(position);
		
		if(message.getFrom() == -1){
			return handleGroupInvitation(position, v, parent);
		}else{
			return handleUserMessage(position, v, parent);
		}

	}


	private View handleUserMessage(int position, View v, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ViewHolderMessage viewHolder;
		
		if(v == null){
			v = inflater.inflate(R.layout.message_item, parent, false);
			viewHolder = new ViewHolderMessage();
			//get gui components
			viewHolder.text = (TextView) v.findViewById(R.id.lblMessageText);
			viewHolder.date = (TextView) v.findViewById(R.id.lblMessageDate);
			viewHolder.username = (TextView) v.findViewById(R.id.txtUsername);
			v.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
				}
			
			});
			v.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolderMessage) v.getTag();
		}
		

		MessagePojo message = getItem(position);
		
		if(message != null){
			//fill the gui components
			viewHolder.fromUID = message.getFrom();
			viewHolder.pID = message.getPostID();
			viewHolder.picID = message.getPicID();
			
			boolean fromMe;
			if(message.getTo() == msb.getUser().getId()){
				fromMe = false;
			}else{
				fromMe = true;
			}
			
			double density = context.getResources().getDisplayMetrics().density;
			
			LinearLayout ll = (LinearLayout) viewHolder.date.getParent();
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			
			if(fromMe){
				viewHolder.username.setVisibility(View.GONE);
				params.gravity = Gravity.RIGHT;
				params.rightMargin = (int) (1*density);
				params.leftMargin = (int) (30*density);
				ll.setLayoutParams(params);
				ll.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.message_me));
			}else{
				viewHolder.username.setVisibility(View.VISIBLE);
				if(!message.isAnonymous()){
					viewHolder.username.setText(message.getUsername());
				}else{
					viewHolder.username.setText("anonymous");
				}
				params.gravity = Gravity.LEFT;
				params.rightMargin = (int) (30*density);
				params.leftMargin = (int) (1*density);
				ll.setLayoutParams(params);
				ll.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.message_other));
				
				
				if(!message.isRead()){
					msb.readMessage(message.getId());
				}
			}

			String timeText = GeoStuff.getTimeText(message.getDate(), context);
			viewHolder.text.setText(message.getText());
			viewHolder.date.setText(timeText + " " + context.getResources().getString(R.string.ago));
		}
		
		return v;
	}


	private View handleGroupInvitation(final int position, View v, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ViewHolderGroupInvitation viewHolder;
		
		final GroupPojo group = chatActivity.getGroup();
		if(group != null){
			inviting_group = group;
		}
			
		if(v == null){
			v = inflater.inflate(R.layout.invitation_message_item, parent, false);
			viewHolder = new ViewHolderGroupInvitation();
			//get gui components
			viewHolder.groupIcon = (ImageView) v.findViewById(R.id.imgGroupIcon);
			viewHolder.groupName = (TextView) v.findViewById(R.id.lblGroupname);
			viewHolder.accept = (Button) v.findViewById(R.id.btnAccept);
			viewHolder.accept.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					ViewHolderGroupInvitation viewHolder = (ViewHolderGroupInvitation) ((View)v.getParent().getParent().getParent()).getTag();
					msb.joinGroup(inviting_group.getId());
					msb.deleteMessages(new Integer[]{viewHolder.msg.getId()});
					remove(getItem(position));
					chatActivity.finish();
				}
			});
			viewHolder.delete = (Button) v.findViewById(R.id.btnDelete);
			viewHolder.delete.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					ViewHolderGroupInvitation viewHolder = (ViewHolderGroupInvitation) ((View)v.getParent().getParent().getParent()).getTag();
					msb.deleteGroupInvitation(inviting_group.getId(), msb.getUser().getId());
					msb.deleteMessages(new Integer[]{viewHolder.msg.getId()});
					remove(getItem(position));
					chatActivity.finish();
				}
			});
			v.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolderGroupInvitation) v.getTag();
		}
		
		viewHolder.accept.setEnabled(inviting_group != null);
		
		MessagePojo message = getItem(position);
		
		if(message != null){
			//fill the gui components
			viewHolder.msg = message;
			if(inviting_group != null){
				Bitmap bmp = msb.getIconPic(inviting_group.getPic_id());
				if(bmp != null){
					viewHolder.groupIcon.setImageBitmap(bmp);
				}else{
					viewHolder.groupIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.group_icon));
				}
				
				viewHolder.groupName.setText(inviting_group.getName());
			}
		}
		
		return v;
	}
}

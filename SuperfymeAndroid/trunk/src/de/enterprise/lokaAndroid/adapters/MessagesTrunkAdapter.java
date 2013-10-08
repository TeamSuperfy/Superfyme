package de.enterprise.lokaAndroid.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.activities.ChatActivity;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.RamCache;
import de.enterprise.lokaAndroid.tools.GeoStuff;
import de.enterprise.lokaServer.pojos.MessagePojo;
 
public class MessagesTrunkAdapter extends ArrayAdapter<MessagePojo> {
	private final Context context;
	private IMyService myService;
	
	public MessagesTrunkAdapter(Context context, ArrayList<MessagePojo> posts, IMyService ms) {
		super(context, R.layout.message_item, posts);
		this.context = context;
		myService = ms;
	}

	private static class ViewHolder {
	    public TextView text;
	    public TextView date;
	    public ImageView image;
	    public MessagePojo msg;
	}
	
	public void addAllTrick(MessagePojo[] items){
		for (MessagePojo messagePojo : items) {
			add(messagePojo);
		}
	}

 
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewHolder viewHolder;
		
		
		if(v == null){
			v = inflater.inflate(R.layout.message_trunk_item, parent, false);
			viewHolder = new ViewHolder();
			//get gui components
			viewHolder.text = (TextView) v.findViewById(R.id.lblMessageText);
			viewHolder.date = (TextView) v.findViewById(R.id.lblMessageDate);
			viewHolder.image = (ImageView) v.findViewById(R.id.imgMessageIcon);
			

			v.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					ViewHolder holder = (ViewHolder)view.getTag();
					Intent messageIntent = new Intent(context, ChatActivity.class);
					int userID = myService.getUser().getId();
					int otherID;
					if(holder.msg.getTo() != userID){
						otherID = holder.msg.getTo();
					}else{
						otherID = holder.msg.getFrom();
					}
					messageIntent.putExtra("msg", holder.msg);
					messageIntent.putExtra("other", otherID);
					context.startActivity(messageIntent);
				}
			
			});
			v.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) v.getTag();
		}
		


		MessagePojo message = getItem(position);
		if(message != null){
			//fill the gui components
			viewHolder.msg = message;
			
			boolean fromMe;
			if(message.getTo() == myService.getUser().getId()){
				fromMe = false;
			}else{
				fromMe = true;
			}
			
			Bitmap bm = myService.getImage(message.getPicID(), RamCache.THUMB);
			if(bm != null){
				viewHolder.image.setImageBitmap(bm);
			}
			else{
				viewHolder.image.setImageDrawable(getContext().getResources().getDrawable(R.drawable.logo1icon));
			}
			
			String timeText = GeoStuff.getTimeText(message.getDate(), context);
			String fromString = "";
			if(message.getFrom() == -1){
				fromString = context.getResources().getString(R.string.system_message);
			}else{
				if(fromMe){
					fromString = "-> you";
				}else{
					fromString = "<- " + (message.isAnonymous()?"anonymous":message.getUsername());
					
					if(!message.isRead()){
						v.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.roundrec_darker));
						viewHolder.text.setTypeface(null,Typeface.BOLD);
					}
					else{
						v.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.roundrec));
						viewHolder.text.setTypeface(null,Typeface.NORMAL);
					}
				}
			}
	
			viewHolder.text.setText(fromString + ": " + message.getText());
			viewHolder.date.setText(timeText +" " + context.getResources().getString(R.string.ago));
		}
		
		return v;
	}
}

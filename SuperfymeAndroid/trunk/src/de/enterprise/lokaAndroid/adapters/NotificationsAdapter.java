package de.enterprise.lokaAndroid.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.activities.CommentsListActivity;
import de.enterprise.lokaAndroid.fragments.NotificationsFragment;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.RamCache;
import de.enterprise.lokaAndroid.tools.GeoStuff;
import de.enterprise.lokaServer.pojos.NotificationPojo;
 
public class NotificationsAdapter extends ArrayAdapter<NotificationPojo> {
	private final Context context;
	private NotificationsFragment nf;
	private IMyService msb;
 
	public NotificationsAdapter(Context context, ArrayList<NotificationPojo> notifications, NotificationsFragment nf, IMyService msb) {
		super(context, R.layout.my_posts_item, notifications);
		this.context = context;
		this.nf = nf;
		this.msb = msb;
	}
	
	private static class NotificationViewHolder {
	    public TextView excerpt;
	    public TextView info;
	    public ImageView image;
	    public TextView excerpt_comment;
	    public TextView date;
	    
	    public int onPostID;
	}
	
	public void addAllTrick(NotificationPojo[] items){
		for (NotificationPojo notificationPojo : items) {
			add(notificationPojo);
		}
	}

 
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		NotificationViewHolder viewHolder;
		
		//test whether this is a loading hint
		if(getItem(position).getId() == -1){
			v = inflater.inflate(R.layout.loading_hint, parent, false);
		}
		else if(getItem(position).getId() == -2){
			v = inflater.inflate(R.layout.no_notifications, parent, false);
		}
		else{
			if(v == null){
				v = inflater.inflate(R.layout.my_posts_item, parent, false);
				viewHolder = new NotificationViewHolder();
				//get gui components
				viewHolder.excerpt = (TextView) v.findViewById(R.id.lblExcerpt);
				viewHolder.info = (TextView) v.findViewById(R.id.lblNotificationInfo);
				viewHolder.excerpt_comment = (TextView) v.findViewById(R.id.lblExcerptComment);
				viewHolder.date = (TextView) v.findViewById(R.id.lblTime);
				viewHolder.image = (ImageView) v.findViewById(R.id.imgNotificationIcon);
				v.setTag(viewHolder);
				v.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View view) {
						int postID = ((NotificationViewHolder)view.getTag()).onPostID;
						Intent comments;
						if(postID > 0){
							comments = new Intent(context, CommentsListActivity.class);
							comments.putExtra("pID", postID);
							comments.putExtra("reload", true);
							context.startActivity(comments);
						}
					}
				
				});
			}else{
				viewHolder = (NotificationViewHolder) v.getTag();
			}
			
	
	
			NotificationPojo notification = getItem(position);
			if(notification != null){
				//fill the gui components
				if(!notification.isRead()){
					v.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.roundrec_darker));
					nf.readNotification(notification.getId());
				}
				else{
					v.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.roundrec));
				}
				
				String timeText = GeoStuff.getTimeText(notification.getDate(), context);
				Bitmap bm = msb.getImage(notification.getPic_id(), RamCache.THUMB);
				if(bm != null){
					viewHolder.image.setImageBitmap(bm);
				}
				else{
					viewHolder.image.setImageDrawable(getContext().getResources().getDrawable(R.drawable.logo1icon));
				}
				

				String identifyer = "";
				if(notification.getOn_comment_id() > 0){
					identifyer = context.getResources().getString(R.string.your_comment)+" ";
				}
				else{
					identifyer = context.getResources().getString(R.string.your_post)+" ";
				}
				
				viewHolder.excerpt.setText(identifyer + "\"" + notification.getExcerpt() + "\"");
				
				if(notification.getRating() != 0){
					viewHolder.onPostID = -1;
					viewHolder.excerpt_comment.setText("");
					if(notification.getRating() < 0){
						viewHolder.info.setText(context.getResources().getString(R.string.was_downed));
					}
					else{
						viewHolder.info.setText(context.getResources().getString(R.string.was_upped));
					}
				}
				else{
					viewHolder.onPostID = notification.getOn_post_id();
					viewHolder.info.setText(context.getResources().getString(R.string.was_commented)+":");
					viewHolder.excerpt_comment.setText("\"" + notification.getExcerpt_comment() + "\"");
				}
				
			
				viewHolder.date.setText(timeText+" " + context.getResources().getString(R.string.ago));
			}
		
		}
		
		return v;
	}
}

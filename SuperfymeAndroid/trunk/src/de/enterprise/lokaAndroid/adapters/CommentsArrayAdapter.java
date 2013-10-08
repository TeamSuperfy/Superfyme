package de.enterprise.lokaAndroid.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.activities.CommentsListActivity;
import de.enterprise.lokaAndroid.activities.ImageViewActivity;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.RamCache;
import de.enterprise.lokaAndroid.tools.GeoStuff;
import de.enterprise.lokaServer.pojos.CommentPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;
 
public class CommentsArrayAdapter extends ArrayAdapter<CommentPojo> {
	private final Context context;
	private CommentsListActivity myList;
	private IMyService msb;
 
	public CommentsArrayAdapter(Context context, ArrayList<CommentPojo> comments, IMyService msb) {
		super(context, R.layout.comment_item, comments);
		myList = (CommentsListActivity) context;
		this.context = context;
		this.msb = msb;
	}
 
	public static class CommentViewHolder {
	    public TextView info;
	    public TextView text;
	    public TextView rating;
	    public ImageView image;
	    public ProgressBar progress;
	    public ImageView rateGood;
	    public ImageView rateBad;
	    public int userID;
	    public int commentID;
	    public int position;
	    public int picID;
	    public ImageView profilePic;
	    public TextView username;
	}
	
	private void inflateCommentLayout(CommentViewHolder viewHolder, View v){
		//instantiate all gui components and reference them in the viewHolder
		viewHolder.info = (TextView) v.findViewById(R.id.lblCommentInfo);
		viewHolder.text = (TextView) v.findViewById(R.id.lblCommentText);
		viewHolder.rating = (TextView) v.findViewById(R.id.lblCommentRating);
		
		viewHolder.rateBad = (ImageView) v.findViewById(R.id.imgBad);
		viewHolder.rateBad.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				CommentPojo comment = (CommentPojo) view.getTag();
				if(!CollectionStuff.arrayContains(comment.getRatedBadBy(), msb.getUser().getId())
				&& !CollectionStuff.arrayContains(comment.getRatedGoodBy(), msb.getUser().getId())){
					msb.rateItem(false, comment.getCommentID(),
							false);
					comment.setRatedBadBy(CollectionStuff.appendToArray(comment.getRatedBadBy(), new int[]{msb.getUser().getId()}));
					comment.setBadRating(comment.getBadRating() + 1);
					CommentsArrayAdapter.this.notifyDataSetChanged();
				}
			}
		
		});
		viewHolder.rateGood = (ImageView) v.findViewById(R.id.imgGood);
		viewHolder.rateGood.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				CommentPojo comment = (CommentPojo) view.getTag();
				if(!CollectionStuff.arrayContains(comment.getRatedGoodBy(), msb.getUser().getId())
				&& !CollectionStuff.arrayContains(comment.getRatedBadBy(), msb.getUser().getId())){
					msb.rateItem(false, comment.getCommentID(),
							true);
					comment.setRatedGoodBy(CollectionStuff.appendToArray(comment.getRatedGoodBy(), new int[]{msb.getUser().getId()}));
					comment.setGoodRating(comment.getGoodRating() + 1);
					CommentsArrayAdapter.this.notifyDataSetChanged();
				}
			}
		
		});
		
		viewHolder.profilePic = (ImageView) v.findViewById(R.id.imgProfilePic);
		viewHolder.username = (TextView) v.findViewById(R.id.txtUsername);
		
//		MyImageView img = new MyImageView(v.getContext());
//		double density = context.getResources().getDisplayMetrics().density;
//		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, (int)(300*density), 1);
//		params.setMargins(0, (int)(20*density), 0, 0);
//		img.setLayoutParams(params);
//		img.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.roundrec_darker));


		v.setTag(viewHolder);
	}
	
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		CommentViewHolder viewHolder;

			if(v == null){
				v = inflater.inflate(R.layout.comment_item, parent, false);
				viewHolder = new CommentViewHolder();
				inflateCommentLayout(viewHolder, v);
			}else{
				viewHolder = (CommentViewHolder) v.getTag();
			}
	
			CommentPojo comment = getItem(position);
			if(comment != null){
				//Set all constant stuff
				viewHolder.rating.setTag(comment.getCommentID());
				viewHolder.rateBad.setTag(comment);
				viewHolder.rateGood.setTag(comment);
				viewHolder.userID = comment.getUserID();
				viewHolder.commentID = comment.getCommentID();
				viewHolder.position = position;
				viewHolder.picID = comment.getPicID();
				
				int distance = GeoStuff.getDistance(myList.getLocation(), comment.getLocation());
				String timeText = GeoStuff.getTimeText(comment.getDate(), context);
				String geoText = GeoStuff.getGeoText(distance);
				viewHolder.info.setText(context.getResources().getString(R.string.ca)+" " + geoText + ", "+ timeText + " " 
					+ context.getResources().getString(R.string.ago));
				viewHolder.text.setText(comment.getText());
				viewHolder.rating.setText(comment.getGoodRating()+" up, "+comment.getBadRating()+" down");
				
				if(CollectionStuff.arrayContains(comment.getRatedBadBy(), msb.getUser().getId())){
					viewHolder.rateBad.setImageResource(R.drawable.down_pressed);
				}else{
					viewHolder.rateBad.setImageResource(R.drawable.down);
				}
				
				if(CollectionStuff.arrayContains(comment.getRatedGoodBy(), msb.getUser().getId())){
					viewHolder.rateGood.setImageResource(R.drawable.up_pressed);
				}else{
					viewHolder.rateGood.setImageResource(R.drawable.up);
				}
				
				//Check if progress bar needs to be deleted
				Bitmap bm = msb.getImage(comment.getPicID(), RamCache.TINY);
				ViewGroup root = (ViewGroup)v;
				boolean hasNoImage = myList.hasNoPicture(comment.getCommentID());
				View childAt1 = root.getChildAt(1);

				if(hasNoImage){
					if(childAt1 instanceof ProgressBar || childAt1 instanceof ImageView){
						root.removeView(root.getChildAt(1));
					}
				}else{
					if(bm != null){
						if(childAt1 instanceof ProgressBar){
							root.removeView(root.getChildAt(1));
							viewHolder.progress = null;
							addImageView(viewHolder, root);
							viewHolder.image.setImageBitmap(bm);
							viewHolder.picID = comment.getPicID();
							viewHolder.image.setTag(viewHolder);
						}
						else if(childAt1 instanceof ImageView){
							viewHolder.image.setImageBitmap(bm);
							viewHolder.picID = comment.getPicID();
						}else{
							addImageView(viewHolder, root);
							viewHolder.image.setImageBitmap(bm);
							viewHolder.picID = comment.getPicID();
							viewHolder.image.setTag(viewHolder);
						}
					}else{
						if(childAt1 instanceof ProgressBar){
							//do nothing
						}
						else if(childAt1 instanceof ImageView){
							root.removeView(root.getChildAt(1));
							viewHolder.image = null;
							addProgressBar(viewHolder, root);
							msb.orderTinyPictures(new Integer[]{comment.getPicID()});
						}else{
							addProgressBar(viewHolder, root);
							msb.orderTinyPictures(new Integer[]{comment.getPicID()});
						}
					}

				}
				
				if(comment.isAnonymous()){
					viewHolder.profilePic.setImageDrawable(getContext().getResources().getDrawable(R.drawable.anonymouse_user));
					viewHolder.username.setText("anonymous");
				}else{
					if(comment.getUserPicID() != -1){
						Bitmap b = msb.getImage(comment.getUserPicID(), RamCache.BIG);
						if(b == null){
							msb.orderBigPicture(comment.getUserPicID());
							viewHolder.profilePic.setImageDrawable(getContext().getResources().getDrawable(R.drawable.anonymouse_user));
						}else{
							viewHolder.profilePic.setImageBitmap(b);
						}
					}else{
						viewHolder.profilePic.setImageDrawable(getContext().getResources().getDrawable(R.drawable.group_icon));
					}
					
					viewHolder.username.setText(comment.getUsername());
				}
		}
		return v;
	}
	
	private void addImageView(CommentViewHolder viewHolder, ViewGroup root){
		ImageView img = new ImageView(root.getContext());
		double density = context.getResources().getDisplayMetrics().density;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0);
		params.setMargins(0, (int)(10*density), 0, (int)(10*density));
		img.setLayoutParams(params);
		viewHolder.image = img;
		viewHolder.image.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				CommentViewHolder cvh = (CommentViewHolder) view.getTag();
				Intent imageIntent = new Intent(context, ImageViewActivity.class);
				imageIntent.putExtra("picID", cvh.picID);
				context.startActivity(imageIntent);
			}
		
		});
		root.addView(viewHolder.image, 1);
		root.invalidate();
	}
	
	private void addProgressBar(CommentViewHolder viewHolder, ViewGroup root){
		ProgressBar progress = new ProgressBar(context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		progress.setLayoutParams(params);
		double density = context.getResources().getDisplayMetrics().density;
		progress.setMinimumHeight((int) (76 * density));
		progress.setMinimumWidth((int) (76 * density));
		viewHolder.progress = progress;
		root.addView(progress, 1);
		root.invalidate();
	}

	public void addAllTrick(CommentPojo[] postsArr) {
		for (CommentPojo commentPojo : postsArr) {
			add(commentPojo);
		}
	}
}

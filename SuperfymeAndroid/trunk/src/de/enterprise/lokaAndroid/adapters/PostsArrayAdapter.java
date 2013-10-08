package de.enterprise.lokaAndroid.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.activities.CommentsListActivity;
import de.enterprise.lokaAndroid.activities.ImageViewActivity;
import de.enterprise.lokaAndroid.fragments.ItemListFragment;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaAndroid.services.RamCache;
import de.enterprise.lokaAndroid.tools.GeoStuff;
import de.enterprise.lokaServer.pojos.CommentPojo;
import de.enterprise.lokaServer.pojos.LocationPojo;
import de.enterprise.lokaServer.pojos.PostListPojo;
import de.enterprise.lokaServer.tools.CollectionStuff;
import de.enterprise.lokaServer.tools.Rating;
 
public class PostsArrayAdapter extends ArrayAdapter<PostListPojo> {
	private final Context context;
	private ItemListFragment myListFragment;
	private IMyService msb;
	private HashMap<Integer, CommentPojo> latestComments;
	
	public static final int CTG_STAR = 1, CTG_QUESTION = 2, CTG_EXCLAMATION = 3, CTG_PEOPLE = 4;
 
	public PostsArrayAdapter(Context context, ArrayList<PostListPojo> posts, ItemListFragment mlf, IMyService msb) {
		super(context, R.layout.post_item, posts);
		myListFragment = mlf;
		this.context = context;
		this.msb = msb;
		latestComments = new HashMap<Integer, CommentPojo>();
	}
	
	public static class PostViewHolder {
	    public TextView info;
	    public TextView text;
	    public TextView rating;
	    public ImageView image;
	    public ImageView comments;
	    public TextView latestComments;
	    public EditText commentText;
	    public Button addCommentButton;
	    public ImageView category;
	    public int comment_count;
	    public ProgressBar progress;
	    public int postID;
	    public int userID;
	    public LocationPojo loc;
	    public int position;
	    public int picID;
	    public ImageView rateGood;
	    public ImageView rateBad;
	    public int groupID;
	    public ImageView profilePic;
	    public TextView username;
	}
	
	public void addAllTrick(PostListPojo[] items){
		for (PostListPojo postListPojo : items) {
			add(postListPojo);
		}
	}

	public void setLatestComments(CommentPojo[] latestComments){
		for (CommentPojo commentPojo : latestComments) {
			this.latestComments.put(commentPojo.getPostID(), commentPojo);
		}
	}
	
	private void inflatePostLayout(final PostViewHolder viewHolder, View v){
		//get gui components
		viewHolder.info = (TextView) v.findViewById(R.id.lblPostInfo);
		viewHolder.image = (ImageView) v.findViewById(R.id.imgPostImage);
		viewHolder.image.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				PostViewHolder pvh = (PostViewHolder) view.getTag();
				myListFragment.setLastSelected(pvh.position);
				myListFragment.setNotHomeButton(true);
				Intent imageIntent = new Intent(context, ImageViewActivity.class);
				imageIntent.putExtra("picID", pvh.picID);
				context.startActivity(imageIntent);
			}
		
		});
		viewHolder.comments = (ImageView) v.findViewById(R.id.imgComment);
		viewHolder.text = (TextView) v.findViewById(R.id.lblPostText);
		viewHolder.rating = (TextView) v.findViewById(R.id.lblPostRating);
		viewHolder.latestComments = (TextView) v.findViewById(R.id.lblLatestComments);
		viewHolder.rateBad = (ImageView) v.findViewById(R.id.imgBad);
		viewHolder.rateBad.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				if(!msb.getUser().isBanned()){
					PostListPojo post = (PostListPojo) view.getTag();
					if(!CollectionStuff.arrayContains(post.getRatedBadBy(), msb.getUser().getId())
					&& !CollectionStuff.arrayContains(post.getRatedGoodBy(), msb.getUser().getId())){
						post.setRatedBadBy(CollectionStuff.appendToArray(post.getRatedBadBy(), new int[]{msb.getUser().getId()}));
						post.setBadRating(post.getBadRating() + 1);
						Rating.calculateRatingIndex(post, 1, 0);
						msb.rateItem(true, post.getPostID(),
								false);

						PostsArrayAdapter.this.notifyDataSetChanged();
					}
				}else{
					Toast.makeText(context, context.getResources().getString(R.string.you_are_banned), Toast.LENGTH_SHORT).show();
				}
			}
		
		});
		viewHolder.rateGood = (ImageView) v.findViewById(R.id.imgGood);
		viewHolder.rateGood.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				if(!msb.getUser().isBanned()){
					PostListPojo post = (PostListPojo) view.getTag();
					if(!CollectionStuff.arrayContains(post.getRatedGoodBy(), msb.getUser().getId())
					&& !CollectionStuff.arrayContains(post.getRatedBadBy(), msb.getUser().getId())){
						post.setRatedGoodBy(CollectionStuff.appendToArray(post.getRatedGoodBy(), new int[]{msb.getUser().getId()}));
						post.setGoodRating(post.getGoodRating() + 1);
						Rating.calculateRatingIndex(post, 1, 0);
						msb.rateItem(true, post.getPostID(),
								true);

						PostsArrayAdapter.this.notifyDataSetChanged();
					}
				}else{
					Toast.makeText(context, context.getResources().getString(R.string.you_are_banned), Toast.LENGTH_SHORT).show();
				}
			}
		
		});
		
		viewHolder.comments.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				PostViewHolder pvh = (PostViewHolder) view.getTag();
				myListFragment.setLastSelected(pvh.position);
				myListFragment.setNotHomeButton(true);
				Intent comments = new Intent(context, CommentsListActivity.class);
				comments.putExtra("pID", pvh.postID);
				comments.putExtra("commentCount", pvh.comment_count);
				context.startActivity(comments);
			}
		
		});
		
		viewHolder.profilePic = (ImageView) v.findViewById(R.id.imgProfilePic);
		viewHolder.username = (TextView) v.findViewById(R.id.txtUsername);
		viewHolder.category = (ImageView) v.findViewById(R.id.imgCtg);
		
		v.setTag(viewHolder);
	}
	
 
	@Override
	public View getView(final int position, View v, ViewGroup parent) {
		
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		PostViewHolder viewHolder = null;
		
		if(v == null){
			v = inflater.inflate(R.layout.post_item, parent, false);
			viewHolder = new PostViewHolder();
			inflatePostLayout(viewHolder, v);
		}else{
			viewHolder = (PostViewHolder) v.getTag();
		}
			
	
	
			PostListPojo post = getItem(position);
			if(post != null){
				viewHolder.postID = post.getPostID();
				viewHolder.userID = post.getUserID();
				viewHolder.loc = post.getLocation();
				viewHolder.position = position;
				viewHolder.picID = post.getPicID();
				viewHolder.comment_count = post.getCommentCount();
				
				viewHolder.rateBad.setTag(post);
				viewHolder.rateGood.setTag(post);
				viewHolder.groupID = post.getGroup_id();
				//fill the gui components
				if(viewHolder.image != null){
					viewHolder.image.setTag(viewHolder);
				}
				viewHolder.comments.setTag(viewHolder);
				
//				if(post.getPicID() > 0){
//					msb.orderTinyPictures(new Integer[]{post.getPicID()});
//				}
				
				if(CollectionStuff.arrayContains(post.getRatedBadBy(), msb.getUser().getId())){
					viewHolder.rateBad.setImageResource(R.drawable.down_pressed);
				}else{
					viewHolder.rateBad.setImageResource(R.drawable.down);
				}
				
				if(CollectionStuff.arrayContains(post.getRatedGoodBy(), msb.getUser().getId())){
					viewHolder.rateGood.setImageResource(R.drawable.up_pressed);
				}else{
					viewHolder.rateGood.setImageResource(R.drawable.up);
				}
				
				int distance = GeoStuff.getDistance(myListFragment.getLocation(), post.getLocation());
				String timeText = GeoStuff.getTimeText(post.getDate(), context);
				String geoText = GeoStuff.getGeoText(distance);
				viewHolder.info.setText(context.getResources().getString(R.string.ca)+ " " + geoText + ", " + timeText+ " " +  context.getResources().getString(R.string.ago));
				Bitmap bm = msb.getImage(post.getPicID(), RamCache.TINY);
				if(bm != null){
					ViewGroup root = (ViewGroup)v;
					if(viewHolder.progress != null){
						root.removeView(viewHolder.progress);
						viewHolder.progress = null;
						ImageView img = new ImageView(root.getContext());
						double density = context.getResources().getDisplayMetrics().density;
						img.setMaxHeight((int) (300*density));
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0);
						params.setMargins((int)(5*density), (int)(5*density), (int)(5*density), (int)(5*density));
						img.setLayoutParams(params);
						viewHolder.image = img;
						viewHolder.image.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View view) {
								PostViewHolder pvh = (PostViewHolder) view.getTag();
								myListFragment.setLastSelected(pvh.position);
								myListFragment.setNotHomeButton(true);
								Intent imageIntent = new Intent(context, ImageViewActivity.class);
								imageIntent.putExtra("picID", pvh.picID);
								context.startActivity(imageIntent);
							}
						
						});
						root.addView(viewHolder.image, 1);
						root.invalidate();
					}
					viewHolder.image.setImageBitmap(bm);
					viewHolder.image.setTag(viewHolder);
				}else{
					if(viewHolder.image != null){
						ViewGroup root = (ViewGroup)v;
						root.removeView(viewHolder.image);
						viewHolder.image = null;
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
					msb.orderTinyPictures(new Integer[]{post.getPicID()});
				}
				if(post.getText().equals("")){
					viewHolder.text.setVisibility(View.GONE);
				}else{
					viewHolder.text.setText(post.getText());
					viewHolder.text.setVisibility(View.VISIBLE);
				}
				viewHolder.rating.setText(post.getGoodRating()+" up, "+post.getBadRating()+" down, "+post.getCommentCount()+" comments");
				if(latestComments.containsKey(post.getPostID())){
					CommentPojo comment = latestComments.get(post.getPostID());
					viewHolder.latestComments.setText("-> " + GeoStuff.getTimeText(comment.getDate(), context)+ " " +  context.getResources().getString(R.string.ago) + ": "
							+ comment.getText());
					viewHolder.latestComments.setVisibility(View.VISIBLE);
				}else{
					viewHolder.latestComments.setVisibility(View.GONE);
				}
				
				if(post.isAnonymous()){
					viewHolder.profilePic.setImageDrawable(getContext().getResources().getDrawable(R.drawable.anonymouse_user));
					viewHolder.username.setText("anonymous");
				}else{
					if(post.getUserPicID() != -1){
						Bitmap b = msb.getIconPic(post.getUserPicID());
						if(b == null){
							msb.orderBigPicture(post.getUserPicID());
							viewHolder.profilePic.setImageDrawable(getContext().getResources().getDrawable(R.drawable.anonymouse_user));
						}else{
							viewHolder.profilePic.setImageBitmap(b);
						}
					}else{
						viewHolder.profilePic.setImageDrawable(getContext().getResources().getDrawable(R.drawable.group_icon));
					}
					
					viewHolder.username.setText(post.getUsername());
				}
			}
			
			switch(post.getCategory()){
			case CTG_STAR: viewHolder.category.setImageDrawable(context.getResources().getDrawable(R.drawable.ctg_star));break;
			case CTG_QUESTION: viewHolder.category.setImageDrawable(context.getResources().getDrawable(R.drawable.ctg_question));break;
			case CTG_EXCLAMATION: viewHolder.category.setImageDrawable(context.getResources().getDrawable(R.drawable.ctg_exclamation));break;
			case CTG_PEOPLE: viewHolder.category.setImageDrawable(context.getResources().getDrawable(R.drawable.ctg_people));break;
			}
		
		return v;
	}
}

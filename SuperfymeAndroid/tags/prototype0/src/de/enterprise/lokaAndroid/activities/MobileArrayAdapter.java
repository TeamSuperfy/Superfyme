package de.enterprise.lokaAndroid.activities;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaServer.pojos.PostPojo;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class MobileArrayAdapter extends ArrayAdapter<PostPojo> {
	private final Context context;
 
	public MobileArrayAdapter(Context context, PostPojo[] posts) {
		super(context, R.layout.list_mobile, posts);
		this.context = context;
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		PostPojo post = getItem(position);
		View rowView = inflater.inflate(R.layout.list_mobile, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
		textView.setText(post.getMessage().getText());

		imageView.setImageResource(R.drawable.ic_launcher);

 
		return rowView;
	}
}

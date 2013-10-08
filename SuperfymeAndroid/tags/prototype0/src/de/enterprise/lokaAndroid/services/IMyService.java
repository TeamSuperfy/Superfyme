package de.enterprise.lokaAndroid.services;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Handler;
import de.enterprise.lokaServer.pojos.PostPojo;
import de.enterprise.lokaServer.pojos.RequestPostContentsPojo;
import de.enterprise.lokaServer.pojos.UserPojo;

public interface IMyService {
	Location getLocation();
	void orderPosts(RequestPostContentsPojo rpc);
	void registerListener(String cmd, Handler handler);
	void registerStateListener(Handler handler);
	UserPojo getUser();
	void sendPost(PostPojo post, Bitmap img);
	void orderPostDetail(int id, int postID);
	void setVisiblePosts(PostPojo[] posts);
	PostPojo[] getVisiblePosts();
}

package de.enterprise.lokaAndroid.services;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import de.enterprise.lokaServer.pojos.PostPojo;
import de.enterprise.lokaServer.pojos.RequestPostContentsPojo;
import de.enterprise.lokaServer.pojos.UserPojo;

public class MyServiceBinder extends Binder implements IMyService{

	private MyService service;
	
	public MyServiceBinder(MyService service){
		this.service = service;
	}

	@Override
	public Location getLocation() {
		return service.getLocation();
	}

	@Override
	public void sendPost(PostPojo post, Bitmap img) {
		service.sendPost(post, img);
	}

	@Override
	public void registerListener(String cmd, Handler handler) {
		service.registerListener(cmd, handler);
	}

	@Override
	public void registerStateListener(Handler handler) {
		service.registerStateListener(handler);
	}

	@Override
	public UserPojo getUser() {
		return service.getUser();
	}

	@Override
	public void orderPosts(RequestPostContentsPojo rpc) {
		service.orderPosts(rpc);
	}

	@Override
	public void orderPostDetail(int id, int postID) {
		service.orderPostDetail(id, postID);
	}

	@Override
	public void setVisiblePosts(PostPojo[] posts) {
		service.updateVisiblePosts(posts);
	}

	@Override
	public PostPojo[] getVisiblePosts() {
		return service.getLastVisiblePosts();
	}
	
}

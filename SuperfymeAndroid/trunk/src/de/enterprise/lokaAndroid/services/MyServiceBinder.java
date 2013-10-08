package de.enterprise.lokaAndroid.services;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import de.enterprise.lokaAndroid.services.MyService.MapMode;
import de.enterprise.lokaServer.pojos.CommentNewPojo;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.MessagePojo;
import de.enterprise.lokaServer.pojos.NotificationPojo;
import de.enterprise.lokaServer.pojos.PostNewPojo;
import de.enterprise.lokaServer.pojos.UserPojo;

public class MyServiceBinder extends Binder implements IMyService{

	private MyService service;
	
	MyServiceBinder(MyService service){
		this.service = service;
	}

	@Override
	public Location getLocation() {
		return service.getLocation();
	}

	@Override
	public void sendPost(PostNewPojo post) {
		service.sendPost(post);
	}

	@Override
	public void registerListener(String cmd, Handler handler) {
		service.registerListener(cmd, handler);
	}

	@Override
	public UserPojo getUser() {
		return service.getUser();
	}

	@Override
	public void orderPosts(String jsonOrder) {
		service.orderPosts(jsonOrder);
	}

	@Override
	public void orderPostsList() {
		service.orderPostsList();
	}

	@Override
	public void sendNewComment(CommentNewPojo cmt) {
		service.sendComment(cmt);
	}

	@Override
	public void addImageToCache(int picID, Bitmap b, int size) {
		service.addBitmapToCache(picID, b, size);
	}

	@Override
	public void orderTinyPictures(Integer[] picIDs) {
		service.orderTinyImages(picIDs);
	}

	@Override
	public void orderBigPicture(int picID) {
		service.orderBigImage(picID);
	}

	@Override
	public boolean isImageCached(int picID, int size) {
		return service.isImageCached(picID, size);
	}

	@Override
	public Bitmap getImage(int picID, int size) {
		return service.getImage(picID, size);
	}

	@Override
	public void requestComments(int postID) {
		service.orderComments(postID);
	}

	@Override
	public void rateItem(boolean post, int id, boolean good) {
		service.rateItem(post, id, good);
	}

	@Override
	public void clearImageCache() {
		service.clearImageCache();
	}

	@Override
	public void unregisterListener(String cmd, Handler handler) {
		service.unregisterListener(cmd, handler);
	}

	@Override
	public void orderMessages(int otherUID, int postID, int commentID) {
		service.orderMessages(otherUID, postID, commentID);
	}

	@Override
	public void orderNotifications() {
		service.orderNotifications();
	}

	@Override
	public void checkForNews() {
		service.requestAvailableNews();
	}

	@Override
	public void orderSinglePost(int postID) {
		service.orderSinglePost(postID);
	}

	@Override
	public void orderThumbnailPictures(Integer[] picIDs) {
		service.orderThumbnailImages(picIDs);
	}

	@Override
	public void readNotification(int notifID){
		service.readNotification(notifID);
	}

	@Override
	public void readMessage(int messID) {
		service.readMessage(messID);
	}

	@Override
	public void deleteNotifications(NotificationPojo[] notifsArray) {
		service.deleteNotifications(notifsArray);
	}

	@Override
	public void reportUser(int userID, int postID, int commID) {
		service.reportUser(userID, postID, commID);
	}

	@Override
	public void deletePost(int postID) {
		service.deletePost(postID);
	}

	@Override
	public void sendMessage(MessagePojo mp) {
		service.sendMessage(mp);
	}

	@Override
	public void deleteMessages(Integer[] messIDs) {
		service.deleteMessages(messIDs);
	}

	@Override
	public void requestGroups(Integer[] groupIDs) {
		service.requestGroups(groupIDs);
	}

	@Override
	public void setSelectedGroup(GroupPojo group) {
		service.setSelectedGroup(group);
	}

	@Override
	public GroupPojo getSelectedGroup() {
		return service.getSelectedGroup();
	}

	@Override
	public void checkGroupName(String groupName, String place) {
		service.checkGroupName(groupName, place);
	}

	@Override
	public void createGroup(GroupPojo gp) {
		service.createGroup(gp);
	}

	@Override
	public boolean updateUser(UserPojo user) {
		return service.updateUser(user);
	}

	@Override
	public void joinGroup(int id) {
		service.joinGroup(id);
	}

	@Override
	public void searchGroups(String name, String place, boolean isPublic) {
		service.searchGroups(name, place, isPublic);
	}

	@Override
	public void deleteAllMessages() {
		service.deleteAllMessages();
	}

	@Override
	public void orderMessageTrunks() {
		service.orderMessageTrunks();
	}

	@Override
	public void orderBigPictures(Integer[] requiredPicIDs) {
		service.orderBigPictures(requiredPicIDs);
	}

	@Override
	public void setSearchWord(String searchWord) {
		service.setSearchWord(searchWord);
	}

	@Override
	public String getSearchWord() {
		return service.getSearchWord();
	}

	@Override
	public void leaveGroup(int groupID) {
		service.leaveGroup(groupID);
	}

	@Override
	public void deleteGroup(int groupID) {
		service.deleteGroup(groupID);
	}

	@Override
	public void banUserFromGroup(int groupID, int userID) {
		service.banUserFromGroup(groupID, userID);
	}

	@Override
	public void removeFromOrderList(int picID, int size) {
		service.removeFromOrderList(picID, size);
	}

	@Override
	public void orderLatestComments(Integer[] postIDs) {
		service.orderLatestComments(postIDs);
	}

	@Override
	public void sendFeedback(String feedback) {
		service.sendFeedback(feedback);
	}

	@Override
	public void getUserUpdate() {
		service.getUserUpdate();
	}

	@Override
	public void deleteComment(int commentID) {
		service.deleteComment(commentID);
	}

	@Override
	public void setLastLocation(Location loc) {
		service.setLastLocation(loc);
	}

	@Override
	public void resetRequests() {
		service.resetRequests();
	}

	@Override
	public void newUser(String username, Bitmap profilepic) {
		service.newUser(username, profilepic);
	}

	@Override
	public Bitmap getIconPic(int picID) {
		return service.getPic(picID);
	}

	@Override
	public void saveUserPic(int picID, Bitmap bmp) {
		service.saveUserPic(picID, bmp);
	}

	@Override
	public void setMapMode(MapMode mm) {
		service.setMapMode(mm);
	}

	@Override
	public MapMode getMapMode() {
		return service.getMapMode();
	}

	@Override
	public void orderGroups(String json) {
		service.orderGroups(json);
	}

	@Override
	public void saveGroupPic(int picID, Bitmap bmp) {
		service.saveGroupPic(picID, bmp);
	}

	@Override
	public void requestUserPojos(Integer[] users) {
		service.requestUserPojos(users);
	}

	@Override
	public void removeUserFromGroup(int groupID, int userID) {
		service.removeUserFromGroup(groupID, userID);
	}

	@Override
	public void inviteUser(int groupID, String username) {
		service.inviteUser(groupID, username);
	}

	@Override
	public void deleteGroupInvitation(int groupID, int userID) {
		service.deleteGroupInvitation(groupID, userID);
	}

	@Override
	public void clearDB() {
		service.clearDB();
	}

	@Override
	public String getCachedJSON(String key) {
		return service.getCachedJSON(key);
	}

	@Override
	public void cacheJSON(String key, String val) {
		service.cacheJSON(key, val);
	}
	
}

package de.enterprise.lokaAndroid.services;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class RamCache {

	public static final int TINY = 0, BIG = 1, THUMB = 2;
	
	private BitmapLRUCache tinyPics, bigPics, thumbNails;
	//CMD+ID->JSON
	private LruCache<String, String> jsonCache;
	
	private class BitmapLRUCache extends LruCache<Integer, Bitmap>{

		public BitmapLRUCache(int size) {
			super(size);
		}
		
		protected int sizeOf(Integer key, Bitmap value) {
	        return value.getRowBytes() * value.getHeight();
		}
		
		protected void entryRemoved (boolean evicted, Integer key, Bitmap oldValue, Bitmap newValue){
			oldValue.recycle();
		}
		
	}
	
	public RamCache(){
		int cacheSize = 4 * 1024 * 1024;
		tinyPics = new BitmapLRUCache(cacheSize*2);
		bigPics = new BitmapLRUCache(cacheSize);
		thumbNails = new BitmapLRUCache(cacheSize/2);
		jsonCache = new LruCache(cacheSize);
	}
	
	public String getJSON(String key){
		return jsonCache.get(key);
	}
	
	public void put(String key, String val){
		jsonCache.put(key , val);
	}
	
	Bitmap get(int picID, int size){
		switch(size){
		case TINY:
			synchronized(tinyPics){
				return tinyPics.get(picID);
			}
		case BIG:
			synchronized(bigPics){
				return bigPics.get(picID);
			}
		case THUMB:
			synchronized(thumbNails){
				return thumbNails.get(picID);
			}
		default:return null;
		}
	}
	
	boolean has(int picID, int size){
		switch(size){
		case TINY:
			synchronized(tinyPics){
				return tinyPics.get(picID) != null;
			}
		case BIG:
			synchronized(bigPics){
				return bigPics.get(picID) != null;
			}
		case THUMB:
			synchronized(thumbNails){
				return thumbNails.get(picID) != null;
			}
		default:return false;
		}
	}
	
	void put(int picID, Bitmap bitmap, int size){
		switch(size){
		case TINY:
			synchronized(tinyPics){
				tinyPics.put(picID, bitmap);
				break;
			}
		case BIG:
			synchronized(bigPics){
				bigPics.put(picID, bitmap);
				break;
			}
		case THUMB:
			synchronized(thumbNails){
				thumbNails.put(picID, bitmap);
				break;
			}
		default:return;
		}
	}
	
	void clear(){
		tinyPics.evictAll();
		bigPics.evictAll();
		thumbNails.evictAll();
		jsonCache.evictAll();
	}
	
}

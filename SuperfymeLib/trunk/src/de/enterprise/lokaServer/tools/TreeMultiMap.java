package de.enterprise.lokaServer.tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.collections.ComparatorUtils;

public class TreeMultiMap<K,V> {

	private ArrayList<TreeMap<K,V>> treeMaps;
	
	public TreeMultiMap(){
		treeMaps = new ArrayList<TreeMap<K,V>>();
		treeMaps.add(new TreeMap<K,V>());
	}
	
	public void put(K key, V value){
		for (TreeMap<K, V> map : treeMaps) {
			if(!map.containsKey(key)){
				map.put(key, value);
				return;
			}
		}
		TreeMap<K, V> newMap = new TreeMap<K,V>();
		newMap.put(key, value);
		treeMaps.add(newMap);
	}
	
	public V getObject(K key){
		V value;
		for (TreeMap<K, V> map : treeMaps) {
			if(map.containsKey(key)){
				value = map.get(key);
				map.remove(key);
				return value;
			}
		}
		return null;
	}
	
	public boolean isEmpty(){
		for (TreeMap<K,V> map : treeMaps) {
			if(!map.isEmpty()){
				return false;
			}
		}
		return true;
	}
	
	public K getNextKey(){
		if(!isEmpty()){
			Comparator<? super K> comp = treeMaps.get(0).comparator();
			if(comp == null){
				comp = ComparatorUtils.naturalComparator();
			}
			HashMap<Integer, K> firstKeys = new HashMap<Integer, K>();
			for (TreeMap<K, V> map : treeMaps) {
				firstKeys.put(treeMaps.indexOf(map), map.firstKey());
			}
			
			K smallestKey = null;
			int smallestMap = 0;
			
			for (Entry<Integer, K> entry : firstKeys.entrySet()) {
				smallestKey = entry.getValue();
				smallestMap = entry.getKey();
				break;
			}
			
			firstKeys.remove(smallestMap);
			
			for (Entry<Integer, K> entry : firstKeys.entrySet()) {
				K nextKey = entry.getValue();
				int nextMap = entry.getKey();
				if(comp.compare(smallestKey, nextKey) > 0){
					smallestKey = nextKey;
					smallestMap = nextMap;
				}
			}
			
			return smallestKey;
		}
		return null;
	}
	
	public void clear(){
		treeMaps = new ArrayList<TreeMap<K,V>>();
		treeMaps.add(new TreeMap<K,V>());
	}

	
	public static void main(String[] args) {
		TreeMultiMap<Integer, Integer> map = new TreeMultiMap<Integer, Integer>();
		map.getNextKey();
	}
	
}

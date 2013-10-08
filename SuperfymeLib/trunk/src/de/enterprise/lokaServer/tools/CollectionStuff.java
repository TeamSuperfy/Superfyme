package de.enterprise.lokaServer.tools;

import java.util.ArrayList;
import java.util.List;

public class CollectionStuff {

	public static int[] appendToArray(int[] dst, int[] append){
		if(dst != null){
			if(append != null){
				if(dst.length == 0){
					return append;
				}
				int[] newArray = new int[dst.length + append.length];
				System.arraycopy(dst, 0, newArray, 0, dst.length);
				System.arraycopy(append, 0, newArray, dst.length, append.length);
				return newArray;
			}else{
				return dst;
			}
		}
		else{
			return append;
		}
	}
	
	public static int[] integerToIntArray(Integer[] integerArray){
		int[] newIntArray = new int[integerArray.length];
		for (int i = 0; i < integerArray.length; i++) {
			newIntArray[i] = integerArray[i];
		}
		return newIntArray;
	}
	
	public static Integer[] intToIntegerArray(int[] arr){
		if(arr != null){
			Integer[] newArray = new Integer[arr.length];
			int i = 0;
			for (int value : arr) {
			    newArray[i++] = Integer.valueOf(value);
			}
			return newArray;
		}
		return null;
	}
	
	public static boolean arrayContains(int[] arr, int n){
		if(arr != null){
		    List<Integer> intList = new ArrayList<Integer>();
		    for (int index = 0; index < arr.length; index++)
		    {
		        intList.add(arr[index]);
		    }
		    return intList.contains(n);
		}else{
			return false;
		}
	}
	
	public static Object[] removeItemFromArray(Object[] arr, Object item){
		if(arr != null){
			if(arr.length > 0){
				ArrayList<Object> list = new ArrayList<Object>();
				for (Object obj : arr) {
					list.add(obj);
				}
				list.remove(item);
				return list.toArray();
			}
		}
		return null;
	}
	
	public static Integer[] removeIntFromArray(int[] arr, int item){
		if(arr != null){
			if(arr.length > 0){
				ArrayList<Integer> list = new ArrayList<Integer>();
				for (int integer : arr) {
					if(integer != item)
						list.add(integer);
				}
				return list.toArray(new Integer[list.size()]);
			}
		}
		return null;
	}
}

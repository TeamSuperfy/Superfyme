package de.enterprise.lokaAndroid.tools;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Statistics {
	
	private class MyComparator implements Comparator<Object> {

	    private Map<String, Integer> map;

	    private MyComparator(Map<String, Integer> map) {
	        this.map = map;
	    }

	    public int compare(Object o1, Object o2) {

	        if (map.get(o2) == map.get(o1))
	            return 1;
	        else
	            return ((Integer) map.get(o2)).compareTo((Integer)     
	                                                            map.get(o1));

	    }
	}
	
	public boolean enabled;
	private HashMap<String, Integer> commandTrafficSum = new HashMap<String, Integer>();
	private HashMap<String, Integer> commandTrafficSumGzip = new HashMap<String, Integer>();
	private HashMap<String, Integer> commandTrafficCount = new HashMap<String, Integer>();
	private int overAllTraffic = 0;
	private int overAllTrafficGZIP = 0;
	
	public Statistics(boolean enable){
		enabled = enable;
	}
	
	public void putTraffic(String cmd, int traffic, long gzipLength){
		Integer oldSum = commandTrafficSum.get(cmd)==null?0:commandTrafficSum.get(cmd);
		Integer oldSumGZIP = commandTrafficSumGzip.get(cmd)==null?0:commandTrafficSumGzip.get(cmd);
		Integer oldCount = commandTrafficCount.get(cmd)==null?0:commandTrafficCount.get(cmd);
		
		commandTrafficSum.put(cmd, oldSum + traffic);
		commandTrafficSumGzip.put(cmd, (int) (oldSumGZIP + gzipLength));
		commandTrafficCount.put(cmd, oldCount + 1);
		overAllTraffic += traffic;
		overAllTrafficGZIP += gzipLength;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		MyComparator comparator1 = new MyComparator(commandTrafficSum);
		MyComparator comparator3 = new MyComparator(commandTrafficSumGzip);
		MyComparator comparator2 = new MyComparator(commandTrafficCount);

	    Map<String, Integer> trafficSumSorted = new TreeMap<String, Integer>(comparator1);
	    trafficSumSorted.putAll(commandTrafficSum);
	    
	    Map<String, Integer> trafficSumGzipSorted = new TreeMap<String, Integer>(comparator3);
	    trafficSumGzipSorted.putAll(commandTrafficSumGzip);
	    
	    Map<String, Integer> trafficCountSorted = new TreeMap<String, Integer>(comparator2);
	    trafficCountSorted.putAll(commandTrafficCount);
	    
	    sb.append("----------------------------- STATS -----------------------------------\n");
	    sb.append("total: ");
	    sb.append(overAllTraffic);
	    sb.append(" bytes");
	    sb.append("\n");
	    
	    sb.append("total(gzip): ");
	    sb.append(overAllTrafficGZIP);
	    sb.append(" bytes");
	    sb.append("\n");
	    
	    
	    for(Entry<String, Integer> e : trafficSumSorted.entrySet()){
	    	String cmd = e.getKey();
	    	Integer sum = e.getValue();
	    	Integer sumGzip = commandTrafficSumGzip.get(cmd);
	    	Integer count = commandTrafficCount.get(cmd);
	    	int avg = sum/count;
	    	int avgGzip = sumGzip/count;
	    	
	    	//RPM: count: 3, avg: 4240 bytes, total: 121334 bytes
	    	sb.append(e.getKey());
	    	sb.append(": count: ").append(count);
	    	sb.append(", avg: ").append(avg);
	    	sb.append(" (gzipAvg: ").append(avgGzip).append(")");
	    	sb.append(", total: ").append(sum);
	    	sb.append(" (gzipTotal: ").append(sumGzip).append(")");
	    	sb.append("\n");
	    }
	    
    	sb.append("----------------------------------------------------------------------\n");
	    return sb.toString();
	}
}

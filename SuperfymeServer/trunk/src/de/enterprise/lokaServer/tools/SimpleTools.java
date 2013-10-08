package de.enterprise.lokaServer.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Comparator;

import de.enterprise.lokaServer.pojos.CommentPojo;

public class SimpleTools {

	public static int NOTIFICATION_EXCERPT_LENGTH = 18;
	
	public static String generateExcerpt(String original, int length){
		if(original.length() <= length){
			return original;
		}
		return original.substring(0, length-3) + "...";
	}
	
	public static String generateExcerpt(String original){
		if(original.length() <= NOTIFICATION_EXCERPT_LENGTH){
			return original;
		}
		return original.substring(0, NOTIFICATION_EXCERPT_LENGTH-3) + "...";
	}
	
	public static long stream(InputStream input, OutputStream output) throws IOException {
	    ReadableByteChannel inputChannel = null;
	    WritableByteChannel outputChannel = null;

	    try {
	        inputChannel = Channels.newChannel(input);
	        outputChannel = Channels.newChannel(output);
	        ByteBuffer buffer = ByteBuffer.allocate(10240);
	        long size = 0;

	        while (inputChannel.read(buffer) != -1) {
	            buffer.flip();
	            size += outputChannel.write(buffer);
	            buffer.clear();
	        }

	        return size;
	    }
	    finally {
	        if (outputChannel != null) try { outputChannel.close(); } catch (IOException ignore) { /**/ }
	        if (inputChannel != null) try { inputChannel.close(); } catch (IOException ignore) { /**/ }
	    }
	}
	
	public static class CommentComparator implements Comparator<CommentPojo>{

		@Override
		public int compare(CommentPojo o1, CommentPojo o2) {
			if(o1.getDate() > o2.getDate()){
				return -1;
			}
			else if(o1.getDate() == o2.getDate()){
				return 0;
			}
			return 1;
		}
		
	};
}

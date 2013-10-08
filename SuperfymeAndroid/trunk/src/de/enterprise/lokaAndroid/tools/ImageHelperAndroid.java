package de.enterprise.lokaAndroid.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.util.Base64;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.enterprise.lokaAndroid.R;
import de.enterprise.lokaAndroid.adapters.PostsArrayAdapter;
import de.enterprise.lokaAndroid.services.IMyService;
import de.enterprise.lokaServer.pojos.GroupPojo;
import de.enterprise.lokaServer.pojos.PostMapPojo;

public class ImageHelperAndroid {

	private static final int ORIENTATION_PORTRAIT_1 = 6, ORIENTATION_PORTRAIT_2 = 8, NO_ORIENTATION_CODE = 0;
	
	public static Bitmap getRoundedCornerBitmap(Context context, Bitmap input,
			int pixels, int w, int h, boolean squareTL, boolean squareTR,
			boolean squareBL, boolean squareBR) {

		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final float densityMultiplier = context.getResources()
				.getDisplayMetrics().density;

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, w, h);
		final RectF rectF = new RectF(rect);

		// make sure that our rounded corner is scaled appropriately
		final float roundPx = pixels * densityMultiplier;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		// draw rectangles over the corners we want to be square
		if (squareTL) {
			canvas.drawRect(0, 0, w / 2, h / 2, paint);
		}
		if (squareTR) {
			canvas.drawRect(w / 2, 0, w, h / 2, paint);
		}
		if (squareBL) {
			canvas.drawRect(0, h / 2, w / 2, h, paint);
		}
		if (squareBR) {
			canvas.drawRect(w / 2, h / 2, w, h, paint);
		}

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(input, 0, 0, paint);

		return output;
	}
	
	/**
	 * Tests if the given bitmap is in portrait or landscape orientation
	 * @param bitmap
	 * @return true for portrait, false for landscape
	 */
	public static boolean getOrientation(Bitmap bitmap){
		if(bitmap.getWidth() < bitmap.getHeight())
			return true;
		return false;
	}

	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

		int width = bm.getWidth();

		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;

		float scaleHeight = ((float) newHeight) / height;

		float minScale = Math.min(scaleWidth, scaleHeight);
		
		// create a matrix for the manipulation

		Matrix matrix = new Matrix();

		// resize the bit map

		matrix.postScale(minScale, minScale);

		// recreate the new Bitmap

		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);

		return resizedBitmap;

	}
	
	public static Bitmap getBitmapFromBase64Small(String base64){
		byte[] img;
		img = Base64.decode(base64, Base64.DEFAULT);
		ByteArrayInputStream in = new ByteArrayInputStream(img);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 3;
		
		Bitmap bmp = BitmapFactory.decodeStream(in, null, options);
		return bmp;
	}
	
	public static Bitmap getBitmapFromBase64Full(String base64){
		byte[] img;
		img = Base64.decode(base64, Base64.DEFAULT);
		ByteArrayInputStream in = new ByteArrayInputStream(img);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 0;
		Bitmap bmp = BitmapFactory.decodeStream(in, null, options);
		return bmp;
	}
	
	public static Bitmap getBitmapFromByteArray(byte[] data){
		Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
		return b;
	}
	
	public static String bitmapToString(Bitmap b){
		ByteArrayOutputStream full_stream = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.JPEG, 100, full_stream);
		return Base64.encodeToString(full_stream.toByteArray(), Base64.DEFAULT);
	}
	
	public static byte[] bitmapToByte(Bitmap b){
		ByteArrayOutputStream full_stream = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.JPEG, 100, full_stream);
		return full_stream.toByteArray();
	}
	
	private static Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}
	
	public static Bitmap readBitmap(String selectedImage, int reqWidth, double density) {

		Bitmap bitmap = decodeBitmap(selectedImage, reqWidth, density);
        
        ExifInterface exif;
        String orientationString = "-1";
        
		try {
			exif = new ExifInterface(selectedImage);
            orientationString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int orientationCode = Integer.parseInt(orientationString);
		
		if(orientationCode != NO_ORIENTATION_CODE){
			if(orientationCode == ORIENTATION_PORTRAIT_1){
				if(bitmap.getWidth() > bitmap.getHeight())
					bitmap = rotateBitmap(bitmap, 90);
			}else if(orientationCode == ORIENTATION_PORTRAIT_2){
				if(bitmap.getWidth() > bitmap.getHeight())
					bitmap = rotateBitmap(bitmap, -90);
			}
		}
		
		ByteArrayOutputStream full_stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 50, full_stream);
		bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(full_stream.toByteArray()));
		
		return bitmap;
	}
	
	private static int calculateInSampleSize(
        BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    return inSampleSize;
	}
	
	private static Bitmap decodeBitmap(String url, int reqWidth, double density){
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(url, options);
	    int reqHeight = 0;
	    int maxHeight = (int)(300*density);
	    
	    if(options.outWidth > reqWidth && options.outHeight > maxHeight){
		    int reqHeightNormal = (int) ((reqWidth / (double)options.outWidth) * options.outHeight);
		    reqHeight = Math.min(reqHeightNormal, maxHeight);
		    if(reqHeight < reqHeightNormal){
		    	reqWidth = (int) ((reqHeight / (double)reqHeightNormal)*reqWidth) ;
		    }
	    }else if(options.outWidth > reqWidth && options.outHeight <= maxHeight){
	    	reqHeight = (int) ((reqWidth/(double)options.outWidth)*options.outHeight);
	    }else if(options.outWidth <= reqWidth && options.outHeight > maxHeight){
	    	reqHeight = maxHeight;
	    	reqWidth = (int) ((maxHeight/(double)options.outHeight)*options.outWidth);
	    }else{
	    	reqWidth = options.outWidth;
	    	reqHeight = options.outHeight;
	    }
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(url, options);
	}

	public static Bitmap generateMarker(Activity activity, PostMapPojo post) {
		View v = activity.getLayoutInflater().inflate(R.layout.map_post, null);
		ImageView imgCtg = (ImageView) v.findViewById(R.id.imgCtg);
		switch(post.getCategory()){
		case PostsArrayAdapter.CTG_STAR: imgCtg.setImageDrawable(activity.getResources().getDrawable(R.drawable.ctg_star));break;
		case PostsArrayAdapter.CTG_QUESTION: imgCtg.setImageDrawable(activity.getResources().getDrawable(R.drawable.ctg_question));break;
		case PostsArrayAdapter.CTG_EXCLAMATION: imgCtg.setImageDrawable(activity.getResources().getDrawable(R.drawable.ctg_exclamation));break;
		case PostsArrayAdapter.CTG_PEOPLE: imgCtg.setImageDrawable(activity.getResources().getDrawable(R.drawable.ctg_people));break;
		}
		
		
		v.setDrawingCacheEnabled(true);
		v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
	            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight()); 
		v.buildDrawingCache(true);
		Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
		v.setDrawingCacheEnabled(false);
		
		Canvas canvas = new Canvas(bmp);
		
		//draw the image with age color
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		int age = (int) ((System.currentTimeMillis() - post.getLast_action())/3600000);
		cm.setSaturation(Math.max(0, 1 - (age*2)/100f));
		paint.setColorFilter(new ColorMatrixColorFilter(cm));
		canvas.drawBitmap(bmp, 0, 0, paint);
		
		//scale image to score
//		float scaleFactor = 1 + (post.getOtherRating() * POST_IMAGE_SCALE);
//		newBitmap = getResizedBitmap(newBitmap, (int)(newBitmap.getHeight() * scaleFactor), (int)(newBitmap.getWidth() * scaleFactor));
		
		
		return bmp;
	}

	public static Bitmap generateMarkerGroups(Activity activity, GroupPojo group, IMyService msb) {
		View v = activity.getLayoutInflater().inflate(R.layout.map_group, null);
		ImageView imgGroupImage = (ImageView) v.findViewById(R.id.imgGroupImage);
		TextView lblText = (TextView) v.findViewById(R.id.lblGroupname);
		lblText.setText(group.getName());
		
		if(group.getPic_id() > 0){
			Bitmap bmp = msb.getIconPic(group.getPic_id());
			if(bmp != null){
				imgGroupImage.setImageBitmap(bmp);
			}else{
				msb.orderBigPicture(group.getPic_id());
				imgGroupImage.setImageDrawable(activity.getResources().getDrawable(R.drawable.group_icon));
			}
		}else{
			imgGroupImage.setImageDrawable(activity.getResources().getDrawable(R.drawable.group_icon));
		}
		
		v.setDrawingCacheEnabled(true);
		v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
	            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight()); 
		v.buildDrawingCache(true);
		Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
		v.setDrawingCacheEnabled(false);
		
		//scale image to score
//		float scaleFactor = 1 + (post.getOtherRating() * POST_IMAGE_SCALE);
//		newBitmap = getResizedBitmap(newBitmap, (int)(newBitmap.getHeight() * scaleFactor), (int)(newBitmap.getWidth() * scaleFactor));
		
		
		return bmp;
	}
	
}

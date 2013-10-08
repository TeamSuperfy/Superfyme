package de.enterprise.lokaAndroid.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;
import de.enterprise.lokaAndroid.tools.ImageHelperAndroid;

public class MyImageView extends ImageView {

	private Bitmap mBitmap;

	public MyImageView(Context context) {
		super(context);
	}

	@SuppressWarnings("ucd")
	public MyImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@SuppressWarnings("ucd")
	public MyImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap != null) {
			Bitmap roundBitmap = ImageHelperAndroid.getRoundedCornerBitmap(
					getContext(), mBitmap, 3, getWidth(), getHeight(), false,
					false, false, false);

			canvas.drawBitmap(roundBitmap, 0, 0, null);
		}
		else{
			super.onDraw(canvas);
		}
	}

	@Override
	public void setImageBitmap(Bitmap bitmap) {
		if(bitmap != null){
			if(mBitmap != null){
				mBitmap.recycle();
				mBitmap = ImageHelperAndroid.getResizedBitmap(bitmap, getHeight(), getWidth());
			}
			else{
				mBitmap = bitmap;
			}
			super.setImageBitmap(mBitmap);
		}
		
		else if(mBitmap != null){
			mBitmap.recycle();
			mBitmap = null;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mBitmap != null) {
			double density = getContext().getResources().getDisplayMetrics().density;
			int height = (int) (300*density);
			mBitmap = ImageHelperAndroid.getResizedBitmap(mBitmap, height, MeasureSpec.getSize(widthMeasureSpec));
//			int width = MeasureSpec.getSize(widthMeasureSpec);
//			int height = width * mBitmap.getHeight() / mBitmap.getWidth();
			setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

}

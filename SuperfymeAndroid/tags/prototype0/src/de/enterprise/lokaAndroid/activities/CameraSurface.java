package de.enterprise.lokaAndroid.activities;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import de.enterprise.lokaAndroid.R;

public class CameraSurface extends Activity implements SurfaceHolder.Callback,
		OnClickListener {
	static final int FOTO_MODE = 0;
	private static final String TAG = "CameraTest";
	Camera mCamera;
	boolean mPreviewRunning = false;
	private Context mContext = this;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Log.e(TAG, "onCreate");

		Bundle extras = getIntent().getExtras();

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.surface_camera);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		mSurfaceView.setOnClickListener(this);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] imageData, Camera c) {

			if (imageData != null) {

				Intent mIntent = new Intent();
				mIntent.putExtra("img", imageData);
				mCamera.startPreview();

				setResult(FOTO_MODE, mIntent);
				finish();

			}
		}
	};

	protected void onResume() {
		Log.e(TAG, "onResume");
		super.onResume();
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	protected void onStop() {
		Log.e(TAG, "onStop");
		super.onStop();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.e(TAG, "surfaceCreated");
		mCamera = Camera.open();

	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.e(TAG, "surfaceChanged");

		// XXX stopPreview() will crash if preview is not running
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}

		Camera.Parameters p = mCamera.getParameters();
		List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();

		int smallestDistX = 1000;
		int smallestDistY = 1000;
		int smallestIndex = 0;

		for (Size size : previewSizes) {
			int thisDistX = Math.abs(w - size.width);
			int thisDistY = Math.abs(h - size.height);
			if (thisDistX < smallestDistX && thisDistY < smallestDistY) {
				smallestDistX = thisDistX;
				smallestDistY = thisDistY;
				smallestIndex = previewSizes.indexOf(size);
			}
		}

		// You need to choose the most appropriate previewSize for your app
		Camera.Size previewSize = previewSizes.get(smallestIndex);// .... select
																	// one of
																	// previewSizes
																	// here

		p.setPreviewSize(previewSize.width, previewSize.height);

		if (Integer.parseInt(Build.VERSION.SDK) >= 8)
			setDisplayOrientation(mCamera, 90);
		else {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				p.set("orientation", "portrait");
				p.set("rotation", 90);
			}
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				p.set("orientation", "landscape");
				p.set("rotation", 90);
			}
		}
		
		p.setRotation(90);

		mCamera.setParameters(p);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCamera.startPreview();
		mPreviewRunning = true;
	}

	protected void setDisplayOrientation(Camera camera, int angle) {
		Method downPolymorphic;
		try {
			downPolymorphic = camera.getClass().getMethod(
					"setDisplayOrientation", new Class[] { int.class });
			if (downPolymorphic != null)
				downPolymorphic.invoke(camera, new Object[] { angle });
		} catch (Exception e1) {
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e(TAG, "surfaceDestroyed");
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();
	}

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;

	public void onClick(View arg0) {

		mCamera.takePicture(null, mPictureCallback, mPictureCallback);

	}

}

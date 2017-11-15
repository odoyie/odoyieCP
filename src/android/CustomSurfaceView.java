package com.cordovaplugincamerapreview;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.hardware.Camera.*;

class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
    private final String TAG = "CustomSurfaceView";

	private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private List<Camera.Size> mSupportedPreviewSizes;
    private List<Camera.Size> mSupportedPictureSizes;
    private Camera.Size mPreviewSize;
    private Camera.Size mPictureSize;
	
    public CustomSurfaceView(Context context, Camera camera) {
        super(context);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        mCamera = camera;
        Camera.Parameters parameters = mCamera.getParameters();

        mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        mSupportedPictureSizes = parameters.getSupportedPictureSizes();

        if (parameters.getSupportedFocusModes()
                .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        mCamera.setParameters(parameters);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (mSurfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        try {
            // stop preview before making changes
            mCamera.stopPreview();
        } catch (Exception e) {
            //Timber.e(e, e.getMessage());
        }

        // set preview size and make any resize, rotate or reformatting changes here
        // start preview with new settings
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            parameters.setPictureSize(mPictureSize.width, mPictureSize.height);

            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            //Timber.e(e, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }

        if (mSupportedPictureSizes != null) {
            mPictureSize = getOptimalPreviewSize(mSupportedPictureSizes, mPreviewSize.width,
                    mPreviewSize.height);
        }

        float ratio;
        if (mPreviewSize.height >= mPreviewSize.width) {
            ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
        } else {
            ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;
        }

        setMeasuredDimension(width, (int) (width * ratio));
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double aspectTolerance = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) {
            return null;
        }

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > aspectTolerance) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }
}

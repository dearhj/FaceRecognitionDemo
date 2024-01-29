package com.test.facerecognitionbyusbcamera.utils;

import android.content.res.Resources;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class RkCameraUtils {

    private static String TAG = "RkCameraUtils";
    private static boolean DBG = false;
    public static String CAMERA_DEBUG_PROP = "persist.camera.debug";
    public Camera mCamera = null;
    private int cameraID = 0;
//    private ImageBufferQueue mImageBufferQueue;
    private Runnable mInferenceRunnable;
    private SurfaceHolderCallback mSurfaceHolderCallback;

    // 配置摄像头图像的宽高
    private int mWidth = 640;
    private int mHeight = 480;

    private boolean needAdjust;
    // 配置预览方向(0/90/180/270)
    private int mDisplayOrient = 0;
    // 配置摄像头方向(0/90/180/270)
    private int mCameraOrient = 0;
    private int mConvertType = 0;

    private SurfaceHolder mSurfaceHolder;
    private boolean mIsCameraOpened = false;
    private Thread mInferenceThread;
    // private boolean mStopInference;
//    private boolean DBG_SAVE = Boolean.parseBoolean(ProperUtils.getProperty(CAMERA_DEBUG_PROP, "false"));
    private boolean DBG_SAVE = false;


    private int BUFFER_SIZE0;//= mWidth * mHeight * 3 / 2;
    private byte[][] mPreviewData;//= new byte[][]{new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0]};
    private byte[] mRgaConvertData;// = new byte[BUFFER_SIZE0];

    private CameraPreviewCallback mCallbck = null;

    public boolean initCamera(int camera_id, int width, int height, int display_orient, int camera_orient) {
        if (Camera.getNumberOfCameras() < 1) {
            return false;
        }
        cameraID = camera_id;
        mWidth = width;
        mHeight = height;
        mDisplayOrient = display_orient;
        mCameraOrient = camera_orient;
        BUFFER_SIZE0 = mWidth * mHeight * 3 / 2;
        mPreviewData = new byte[][]{new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0],new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0]};
        mRgaConvertData = new byte[BUFFER_SIZE0];
        return true;
    }

    public boolean initCamera(int camera_id, int width, int height, int display_orient, int camera_orient, int convert_type) {
        if (Camera.getNumberOfCameras() < 1) {
            return false;
        }
        cameraID = camera_id;
        mWidth = width;
        mHeight = height;
        mDisplayOrient = display_orient;
        mCameraOrient = camera_orient;
        BUFFER_SIZE0 = mWidth * mHeight * 3 / 2;
        mPreviewData = new byte[][]{new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0],new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0]};
        mRgaConvertData = new byte[BUFFER_SIZE0];
        mConvertType = convert_type;
        Log.d(TAG, "initCamera camera_id=" + camera_id + ",width=" + width + ",height=" + height + ",display_orient=" + display_orient + ",camera_orient=" + camera_orient + ",convert_type=" + convert_type);
        return true;
    }

    public boolean initCamera(int camera_id, int width, int height, int display_orient, int camera_orient, int convert_type, boolean needAdjust) {
        if (Camera.getNumberOfCameras() < 1) {
            return false;
        }
        this.needAdjust = needAdjust;
        cameraID = camera_id;
        mWidth = width;
        mHeight = height;
        mDisplayOrient = display_orient;
        mCameraOrient = camera_orient;
        BUFFER_SIZE0 = mWidth * mHeight * 3 / 2;
        mPreviewData = new byte[][]{new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0],new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0]};
        mRgaConvertData = new byte[BUFFER_SIZE0];
        mConvertType = convert_type;
        Log.d(TAG, "initCamera camera_id=" + camera_id + ",width=" + width + ",height=" + height + ",display_orient=" + display_orient + ",camera_orient=" + camera_orient + ",convert_type=" + convert_type + ",needAdjust=" + needAdjust);
        return true;
    }

    public boolean updateCameraSettings(int camera_id, int width, int height, int display_orient, int camera_orient) {
        cameraID = camera_id;
        mWidth = width;
        mHeight = height;
        mDisplayOrient = display_orient;
        mCameraOrient = camera_orient;
        BUFFER_SIZE0 = mWidth * mHeight * 3 / 2;
        mPreviewData = new byte[][]{new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0],new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0]};
        mRgaConvertData = new byte[BUFFER_SIZE0];
        return true;
    }

    public boolean updateCameraSettings(int camera_id, int width, int height, int display_orient, int camera_orient, int convert_type) {
        cameraID = camera_id;
        mWidth = width;
        mHeight = height;
        mDisplayOrient = display_orient;
        mCameraOrient = camera_orient;
        BUFFER_SIZE0 = mWidth * mHeight * 3 / 2;
        mPreviewData = new byte[][]{new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0],new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0], new byte[BUFFER_SIZE0]};
        mRgaConvertData = new byte[BUFFER_SIZE0];
        mConvertType = convert_type;
        return true;
    }

    public void setCameraCallback(CameraPreviewCallback callbck) {
        this.mCallbck = callbck;
    }

    public void removeCameraCallback() {
        this.mCallbck = null;
    }

//    public void setCameraFunc(ImageBufferQueue imageBufferQueue) {
//        setCameraFunc(imageBufferQueue, null);
//    }
//
//    public void setCameraFunc(ImageBufferQueue imageBufferQueue, Runnable runnable) {
//        mImageBufferQueue = imageBufferQueue;
//        mInferenceRunnable = runnable;
//        if (DBG_SAVE) {
//            File save_dir = new File("/sdcard/rockface");
//            if (!save_dir.exists()) {
//                save_dir.mkdir();
//            }
//        }
//    }

    private class SurfaceHolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "surfaceChanged");
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "surfaceCreated");
            startCamera();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed");
            stopCamera();
        }
    }


    public boolean createPreviewView(SurfaceView surfaceView) {
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolderCallback = new SurfaceHolderCallback();
        mSurfaceHolder.addCallback(mSurfaceHolderCallback);

        return true;
    }

    public void destroyPreviewView() {
        if (mSurfaceHolder != null) {
            mSurfaceHolder.removeCallback(mSurfaceHolderCallback);
            mSurfaceHolderCallback = null;
            mSurfaceHolder = null;
        }
    }

    public void stopCamera() {
        if (mIsCameraOpened) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mIsCameraOpened = false;
        }
        if (null != mInferenceThread) {
            try {
                mInferenceThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean startCamera() {
        if (mIsCameraOpened) {
            return true;
        }

        try {
            mCamera = Camera.open(cameraID);
            Log.d(TAG, "mCamera = " + mCamera);
        } catch (RuntimeException e) {
            return false;
        }

        setCameraParameters();

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.setDisplayOrientation(mDisplayOrient);

            //================================
            for (byte[] buffer : mPreviewData)
                mCamera.addCallbackBuffer(buffer);
            mCamera.setPreviewCallbackWithBuffer(onPreviewFrame);
            //==================================
            mCamera.startPreview();
        } catch (Exception e) {
            mCamera.release();
            return false;
        }

        mIsCameraOpened = true;
        if (null != mInferenceRunnable) {
            mInferenceThread = new Thread(mInferenceRunnable);
            mInferenceThread.start();
        }

        return true;
    }

    private void setCameraParameters() {
        Camera.Parameters parameters;
        parameters = mCamera.getParameters();

        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        for (int i = 0; i < sizes.size(); i++) {
            Camera.Size size = sizes.get(i);
            Log.v(TAG, "Camera Supported Preview Size = " + size.width + "x" + size.height);
        }

        parameters.setPreviewSize(mWidth, mHeight);

        if (parameters.isZoomSupported()) {
            parameters.setZoom(0);
        }
        try {
            mCamera.setParameters(parameters);
        } catch (RuntimeException r) {
            Log.e(TAG, "Not support preview size set!", r);
        }
    }

    Camera.PreviewCallback onPreviewFrame = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mCallbck != null) {
                mCallbck.onCameraPreview(data, camera);
            }
            mCamera.addCallbackBuffer(data);
        }
    };

    public static List<String> getSupportPreviewSize() {
        int cameraCount = Camera.getNumberOfCameras();
        List<String> rgbPreviewList = new ArrayList<>();
        List<String> irPreviewList = new ArrayList<>();
        List<String> commonPreviewList = new ArrayList<>();
        Log.d(TAG, "getSupportPreviewSize camera number:" + cameraCount);

        if (cameraCount == 0) {//no camera found,use default.
            commonPreviewList.add("640x480");
            return commonPreviewList;
        }

        if (cameraCount > 1) { //IR as default
            Camera rgb_camera = Camera.open(0);
            Camera.Parameters rgb_params = rgb_camera.getParameters();
            List<Camera.Size> rgb_previewSizes = rgb_params.getSupportedPreviewSizes();
            int rgb_length = rgb_previewSizes.size();
            for (int i = 0; i < rgb_length; i++) {
                if (DBG)
                    Log.d(TAG, "RGB SupportedPreviewSizes " + rgb_previewSizes.get(i).width + "x" + rgb_previewSizes.get(i).height);
                if (rgb_previewSizes.get(i).width > 320 && rgb_previewSizes.get(i).height > 320)
                    rgbPreviewList.add(rgb_previewSizes.get(i).width + "x" + rgb_previewSizes.get(i).height);
            }

            Camera ir_camera = Camera.open(1);
            Camera.Parameters ir_params = ir_camera.getParameters();
            List<Camera.Size> ir_previewSizes = ir_params.getSupportedPreviewSizes();
            int ir_length = ir_previewSizes.size();
            for (int i = 0; i < ir_length; i++) {
                if (DBG)
                    Log.d(TAG, "IR SupportedPreviewSizes " + ir_previewSizes.get(i).width + "x" + ir_previewSizes.get(i).height);
                if (ir_previewSizes.get(i).width > 320 && ir_previewSizes.get(i).height > 320)
                    irPreviewList.add(ir_previewSizes.get(i).width + "x" + ir_previewSizes.get(i).height);
            }
            if (rgb_length <= ir_length) {
                for (String str : rgbPreviewList) {
                    if (irPreviewList.contains(str))
                        commonPreviewList.add(str);
                }
            } else {
                for (String str : irPreviewList) {
                    if (rgbPreviewList.contains(str))
                        commonPreviewList.add(str);
                }
            }
        } else if (cameraCount > 0) {//RGB as default
            Camera camera = Camera.open(0);
            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
            int length = previewSizes.size();
            for (int i = 0; i < length; i++) {
                if (DBG)
                    Log.d("RkCameraUtils", "PreviewSizes  " + previewSizes.get(i).width + "x" + previewSizes.get(i).height);
                if (previewSizes.get(i).width > 320 && previewSizes.get(i).height > 320)
                    commonPreviewList.add(previewSizes.get(i).width + "x" + previewSizes.get(i).height);
            }
        }

        Collections.sort(commonPreviewList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] strs_o1 = o1.split("x");
                String[] strs_o2 = o2.split("x");
                return Integer.parseInt(strs_o1[0]) - Integer.parseInt(strs_o2[0]);
            }
        });
        if (DBG) {
            for (String str : commonPreviewList)
                Log.d(TAG, str);
        }
        return commonPreviewList;
    }

    public static int sp2px(float spValue) {
        Resources r = Resources.getSystem();
        final float scale = r.getDisplayMetrics().scaledDensity;
        return (int) (spValue * scale + 0.5f);
    }

    public interface CameraPreviewCallback {
        void onCameraPreview(byte[] data, Camera camera);
    }
}

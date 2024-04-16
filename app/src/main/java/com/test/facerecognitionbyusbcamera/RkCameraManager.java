package com.test.facerecognitionbyusbcamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.List;

public class RkCameraManager {
    private static final String TAG = RkCameraManager.class.getSimpleName();
    public static final int PREVIEW_WIDTH_720P = 1280;
    public static final int PREVIEW_HEIGHT_720P = 720;

    public final static int OPENING = 2;
    public final static int OPENED = 1;
    public final static int CLOSED = 0;
    @SuppressLint("StaticFieldLeak")
    private static volatile RkCameraManager sInstance = null;
    private final int mPrevWidth = PREVIEW_WIDTH_720P;
    private final int mPrevHeight = PREVIEW_HEIGHT_720P;
    private Activity mContext;
    private android.hardware.camera2.CameraManager mCameraManager;
    private Handler mCameraHandler;
    private HandlerThread mHandlerThread;

    private ImageReader imageReader;

    private int cameraStatus = 0;
    private String openCameraId;

    private CameraDevice openCamera;
    private CameraCaptureSession openCameraSession;

    private SurfaceHolder surfaceHolder;
    private CaptureRequest previewRequest;

    private CameraListener listener = null;

    private RkCameraManager(Activity mainActivity) {
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "无摄像头权限");
            return;
        }
        this.mContext = mainActivity;
        mCameraManager = (android.hardware.camera2.CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        startHandler();
    }

    public static RkCameraManager getInstance(Activity mainActivity) {
        if (sInstance == null) {
            synchronized (RkCameraManager.class) {
                if (sInstance == null) {
                    sInstance = new RkCameraManager(mainActivity);
                }
            }
        }
        return sInstance;
    }

    public static synchronized void release() {
        if (sInstance != null) sInstance.destroy();
        sInstance = null;
    }

    private void startHandler() {
        mHandlerThread = new HandlerThread("camera");
        mHandlerThread.start();
        mCameraHandler = new Handler(mHandlerThread.getLooper());
    }

    private void destroyHandler() {
        try {
            if (mHandlerThread != null) {
                mHandlerThread.quitSafely();
                mHandlerThread.join(1);
            }
            mHandlerThread = null;
            mCameraHandler = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void repeatPreview() {
        try {
            openCameraSession.setRepeatingRequest(previewRequest, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //开启预览
    public void startPreview(String cameraId) {
        Log.i(TAG, "预览摄像头:" + cameraId);
        if (openCamera == null) {
            Log.i(TAG, "预览摄像头:" + cameraId + " init failed!");
            return;
        }
        initCaptureSession(Arrays.asList(surfaceHolder.getSurface(), imageReader.getSurface()));
    }

    /**
     * 初始化捕获会话
     *
     * @param surfacelist
     */
    private void initCaptureSession(List<Surface> surfacelist) {
        try {
            CaptureRequest.Builder previewRequestBuilder = openCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            for (Surface surface : surfacelist) {
                previewRequestBuilder.addTarget(surface);
            }
            previewRequest = previewRequestBuilder.build();

            openCamera.createCaptureSession(surfacelist,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            openCameraSession = session;
                            repeatPreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    }, null);

        } catch (CameraAccessException e) {
            Toast.makeText(mContext, "CaptureRequest.Builder访问摄像头失败", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "CaptureRequest.Builder访问摄像头失败");
        }
    }

    private void closeCamera(String cameraId) {
        Log.i(TAG, "关闭摄像头:" + cameraId);
        cameraStatus = CLOSED;
        if (openCameraSession != null) {
            try {
                openCameraSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        if (openCamera != null) {
            openCameraSession = null;
            previewRequest = null;
            openCamera.close();
        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera(String cameraId) {
        if (cameraStatus != CLOSED) {
            Log.w(TAG, "opening/opened摄像头:" + cameraId);
            return;
        }

        Log.i(TAG, "open摄像头:" + cameraId);
        try {
            cameraStatus = OPENING;
            mCameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    String cameraId = camera.getId();
                    Log.i(TAG, "摄像头打开成功:" + cameraId);
                    openCameraId = cameraId;
                    cameraStatus = OPENED;
                    openCamera = camera;
                    startPreview(cameraId);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    closeCamera(camera.getId());
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    closeCamera(camera.getId());
                    Toast.makeText(mContext, "摄像头打开错误:" + camera.getId(), Toast.LENGTH_SHORT).show();
                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "cameraManager访问摄像头失败");
        }
    }

    public void initCamera(String cameraId, SurfaceView surface) {
        Log.w(TAG, "init camera:" + cameraId);
        surfaceHolder = surface.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @SuppressLint("MissingPermission")
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                if (!TextUtils.isEmpty(cameraId)) {
                    Log.i(TAG, "open cameraId=" + cameraId);
                    openCamera(cameraId);
                } else {
                    Log.e(TAG, "cameraManager访问摄像头失败, 摄像头不存在! cameraId=" + cameraId);
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                if (openCamera != null) {
                    closeCamera(openCamera.getId());
                } else {
                    Log.e(TAG, "cameraManager访问摄像头失败");
                }
            }
        });
        //imageReader通过将得到的图片存放在队列中，再取出来进行操作
        //队列满了就不再放入新的图片，设置图片队列大小为10
        imageReader = ImageReader.newInstance(mPrevWidth, mPrevHeight, ImageFormat.YUV_420_888, 2);
        imageReader.setOnImageAvailableListener(reader -> {
            //取出最新的图片并清除队列里的旧图片
            Image image = reader.acquireLatestImage();
            if (image != null) {
                byte[] nv21 = CameraUtils.YUV_420_888toNV21(image);
                if (listener != null) listener.onCameraPreviewData(nv21);
                image.close();
            }
        }, mCameraHandler);
    }

    public void destroy() {
        closeCamera(openCameraId);
        destroyHandler();
    }

    public void setListener(CameraListener listener) {
        this.listener = listener;
    }


    public interface CameraListener {
        void onCameraPreviewData(byte[] cameraPreviewData);
    }
}

package com.test.facerecognitionbyusbcamera;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.test.facerecognitionbyusbcamera.utils.RkCameraUtils;

import java.io.File;
import java.io.FileOutputStream;

public class TakePhotoActivity extends AppCompatActivity implements RkCameraUtils.CameraPreviewCallback {

    private static final String TAG = "rockface-app";

    private RkCameraUtils rgbUtil;
    private SurfaceView mSurfaceView = null;
    private String mImageSavePath;
    private boolean mTakePhoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // hiddend navigation
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_camera);

        Bundle extras = getIntent().getExtras();
        mImageSavePath = extras.getString("IMAGE_SAVE_PATH",
                getApplicationContext().getExternalCacheDir().getPath() + "/temp.jpg");

        initView();
        initCamera();
    }

    private void initCamera() {
        rgbUtil = new RkCameraUtils();
        boolean res = rgbUtil.initCamera(Configs.CAMERA_ID, 1920, 1080, 0, 0, 0, false);
        if (!res) {
            popupCameraAlertDialog();
        }
        rgbUtil.setCameraCallback(this);
    }

    private void popupCameraAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TakePhotoActivity.this);
        builder.setTitle(R.string.settings_no_camera);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.show();
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.surfaceViewCamera1);
        ImageButton captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(v -> mTakePhoto = true);
    }

    private void takePicture(byte[] data, Camera camera) {
        File pictureFile = new File(mImageSavePath);

        Camera.Size size = camera.getParameters().getPreviewSize();
        try {
            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
            if(image!=null){
                FileOutputStream fos = new FileOutputStream(pictureFile);
                image.compressToJpeg(new Rect(0, 0, size.width, size.height), 90, fos);
                fos.write(data);
                fos.close();
                Log.i(TAG, "picture File data = " + data.length);
                returnResult();
            }
        } catch(Exception ex) {
            Log.e("Sys","Error:"+ex.getMessage());
        }
    }

    private void returnResult() {
        Intent intent = new Intent();
        intent.putExtra("IMAGE_SAVE_PATH", mImageSavePath);
        TakePhotoActivity.this.setResult(RESULT_OK, intent);
        TakePhotoActivity.this.finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        rgbUtil.stopCamera();
        rgbUtil.destroyPreviewView();
    }

    @Override
    public void onResume() {
        super.onResume();
        rgbUtil.createPreviewView(mSurfaceView);
    }

    @Override
    public void onCameraPreview(byte[] data, Camera camera) {
        if (mTakePhoto) {
            takePicture(data, camera);
        }
    }
}

package com.test.facerecognitionbyusbcamera;

import static com.test.facerecognitionbyusbcamera.AlarmLightKt.initAlarmLight;
import static com.test.facerecognitionbyusbcamera.FileUtil.showToast;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import mcv.facepass.FacePassException;
import mcv.facepass.FacePassHandler;
import mcv.facepass.auth.AuthApi.AuthApplyResponse;
import mcv.facepass.auth.AuthApi.ErrorCodeConfig;
import mcv.facepass.types.FacePassConfig;
import mcv.facepass.types.FacePassModel;
import mcv.facepass.types.FacePassPose;
import com.test.facerecognitionbyusbcamera.db.FaceDataBase;
import com.test.facerecognitionbyusbcamera.db.FaceDataDao;

public class ManageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String PERMISSION_READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_INTERNET = Manifest.permission.INTERNET;
    private static final String PERMISSION_ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;
    private String[] Permission = new String[]{PERMISSION_CAMERA, PERMISSION_WRITE_STORAGE, PERMISSION_READ_STORAGE, PERMISSION_INTERNET, PERMISSION_ACCESS_NETWORK_STATE};

    private static final int PERMISSIONS_REQUEST = 1;
    public static final String CERT_PATH = "CBG_Android_Face_Reco---30-Trial-one-stage.cert";
    public static final String FILE_ROOT_PATH = "/sdcard/fasspass";

    public static FacePassHandler mFacePassHandler;
    public static boolean FacePassHandlerHasInit = false;
    public static final String group_name = "facepass";

    public static boolean isLocalGroupExist = false;

    public static FaceDataBase database;
    public static FaceDataDao dao;

    public Button faceRecognition;
    public Button startFaceRegister;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        faceRecognition = findViewById(R.id.button_face_recognition);
        faceRecognition.setOnClickListener(this);
        startFaceRegister = findViewById(R.id.button_face_register);
        startFaceRegister.setOnClickListener(this);
        try {
            File file = new File(FILE_ROOT_PATH);
            if (!file.exists()) file.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        showToast(this, "正在验证设备是否授权，请稍后。");
        /* 申请程序所需权限 */
        if (!hasPermission()) {
            requestPermission();
        } else {
            try {
                initFacePassSDK();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        faceRecognition.setClickable(false);
        startFaceRegister.setClickable(false);
        initFaceHandler();
        initAlarmLight(this);

        database = FaceDataBase.Companion.create(this);
        dao = database.faceDataDao();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mFacePassHandler.release();
    }

    /* 判断程序是否有所需权限 android22以上需要自申请权限 */
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_READ_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_WRITE_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_INTERNET) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    /* 请求程序所需权限 */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(Permission, PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED)
                    granted = false;
            }
            if (!granted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if (!shouldShowRequestPermissionRationale(PERMISSION_CAMERA)
                            || !shouldShowRequestPermissionRationale(PERMISSION_READ_STORAGE)
                            || !shouldShowRequestPermissionRationale(PERMISSION_WRITE_STORAGE)
                            || !shouldShowRequestPermissionRationale(PERMISSION_INTERNET)
                            || !shouldShowRequestPermissionRationale(PERMISSION_ACCESS_NETWORK_STATE)) {
                        Toast.makeText(getApplicationContext(), "需要开启摄像头网络文件存储权限", Toast.LENGTH_SHORT).show();
                    }
            } else {
                try {
                    initFacePassSDK();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initFaceHandler() {

        new Thread(() -> {
            while (true) {
                while (FacePassHandler.isAvailable()) {
                    FacePassConfig config;
                    try {
                        /* 填入所需要的配置 */
                        config = new FacePassConfig();
                        config.LivenessModel = FacePassModel.initModel(getAssets(), getString(R.string.mcv_livenessrgb_A));
                        config.searchModel = FacePassModel.initModel(getAssets(), getString(R.string.mcv_feature_Ari));
                        config.poseBlurModel = FacePassModel.initModel(getAssets(), getString(R.string.mcv_poseblur_A));
                        config.postFilterModel = FacePassModel.initModel(getAssets(), getString(R.string.mcv_postfilter_A));
                        config.rcAttributeModel = FacePassModel.initModel(getAssets(), getString(R.string.mcv_rc_attribute_A));
                        config.detectModel = FacePassModel.initModel(getAssets(), getString(R.string.mcv_rk3568_det_A_det));
                        config.occlusionFilterModel = FacePassModel.initModel(getAssets(), getString(R.string.mcv_occlusion_B));
                        /* 送识别阈值参数 */
                        config.searchThreshold = 75f;
                        config.livenessThreshold = 80f; //单目推荐80
                        config.faceMinThreshold = 100;
                        config.poseThreshold = new FacePassPose(45f, 45f, 45);
                        config.blurThreshold = 0.8f;
                        config.lowBrightnessThreshold = 70f;
                        config.highBrightnessThreshold = 220f;
                        config.brightnessSTDThreshold = 80f;
                        config.rgbIrLivenessEnabled = false;
                        config.LivenessEnabled = true;
                        config.rcAttributeEnabled = true;

                        /* 其他设置 */
                        config.maxFaceEnabled = true;
                        config.retryCount = 5;
                        config.fileRootPath = FILE_ROOT_PATH;

                        /* 创建SDK实例 */
                        mFacePassHandler = new FacePassHandler();
                        int code = FacePassHandler.initHandle(config);
                        if (code == 0) FacePassHandlerHasInit = true;

                        Log.d("DEBUG_TAG", "初始化 -> " + (code == 0 ? "成功 " : ("错误 code = " + code)));


                        /* 入库阈值参数 */
                        FacePassConfig addFaceConfig = mFacePassHandler.getAddFaceConfig();
                        addFaceConfig.poseThreshold.pitch = 35f;
                        addFaceConfig.poseThreshold.roll = 35f;
                        addFaceConfig.poseThreshold.yaw = 35f;
                        addFaceConfig.blurThreshold = 0.7f;
                        addFaceConfig.lowBrightnessThreshold = 70f;
                        addFaceConfig.highBrightnessThreshold = 220f;
                        addFaceConfig.brightnessSTDThreshold = 60f;
                        addFaceConfig.faceMinThreshold = 40;
                        mFacePassHandler.setAddFaceConfig(addFaceConfig);
                        checkGroup();
                    } catch (FacePassException e) {
                        e.printStackTrace();
                        Log.d("DEBUG_TAG", "FacePassHandler is null");
                        return;
                    }
                    return;
                }
                try {
                    /* 如果SDK初始化未完成则需等待 */
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void checkGroup() {
        try {
            System.out.println("开始创建底库");
            if (mFacePassHandler == null) {
                System.out.println("开始创建底库11111 + mFacePassHandler == null");
                return;
            }
            String[] localGroups = new String[0];
            try {
                localGroups = mFacePassHandler.getLocalGroups();
            } catch (FacePassException e) {
                e.printStackTrace();
            }
            if (localGroups == null || localGroups.length == 0) {
                System.out.println("开始创建底库   localGroups为空");
                boolean flag = mFacePassHandler.createLocalGroup(group_name);
                if (flag) {
                    System.out.println("开始创建底库   创建底库成功");
                    isLocalGroupExist = true;
                } else System.out.println("开始创建底库   创建底库失败");
            } else {
                System.out.println("开始创建底库  底库已存在，无需创建   " + localGroups[0]);
                isLocalGroupExist = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        startActivity(view.getId());
    }


    public void startActivity(int id) {
        Intent intent = null;

        if (id == R.id.button_face_recognition) {
            intent = new Intent(getApplicationContext(), RecognitionActivity.class);
        } else if (id == R.id.button_face_register) {
            intent = new Intent(getApplicationContext(), FaceManageActivity.class);
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    private void initFacePassSDK() throws IOException {
        new Thread(() -> {
            try {
                FacePassHandler.initSDK(getApplicationContext());
                //授权
                singleCertification(getApplicationContext());

                Log.d("WJY", FacePassHandler.getVersion());
                boolean ret = FacePassHandler.authCheck();
                if (!ret) {
                    Log.d("mcvsafe", "Authentication result : failed.");
                    runOnUiThread(() -> {
                        showToast(this, "身份验证失败，请检查网络并重启应用或设备！");
                    });
                    System.out.println("授权不成功！");
                    // 授权不成功，根据业务需求处理
                }  else {
                    faceRecognition.setClickable(true);
                    startFaceRegister.setClickable(true);
                    System.out.println("授权成功！");
                    runOnUiThread(() -> { showToast(this, "获取设备授权成功"); });
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    private void singleCertification(Context mContext) throws IOException {
        InputStream inputStream = getAssets().open(CERT_PATH);
        String cert = FileUtil.readFileCert(inputStream);
        if (TextUtils.isEmpty(cert)) {
            Log.d("mcvsafe", "cert is null");
            return;
        }
        final AuthApplyResponse[] resp = {new AuthApplyResponse()};
        FacePassHandler.authDevice(mContext.getApplicationContext(), cert, "", result -> {
            resp[0] = result;
            if (resp[0].errorCode == ErrorCodeConfig.AUTH_SUCCESS) {
                try {
                    runOnUiThread(() -> Log.d("mcvsafe", "Apply update: OK"));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            } else {
                try {
                    runOnUiThread(() -> Log.d("mcvsafe", "Apply update: error. error code is: " + resp[0].errorCode + " error message: " + resp[0].errorMessage));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }
}
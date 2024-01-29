package com.test.facerecognitionbyusbcamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class FaceManagerMainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PERMISSION_CALLBACK = 0;
    private static final String TAG = "FaceManagerMainActivity";

    int lastStartId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_manager_main);
        initView();
    }

    private void initView() {
        Button startFaceRegister = findViewById(R.id.button_start_face_register);
        startFaceRegister.setOnClickListener(this);
        Button startFaceCamera = findViewById(R.id.button_start_face_import);
        startFaceCamera.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        lastStartId = view.getId();
        if (requestPermissions()) {
            startActivity(lastStartId);
        }
    }

    public boolean requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                this.requestPermissions(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                        },
                        PERMISSION_CALLBACK);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity(lastStartId);
        } else {
            Toast.makeText(this, R.string.permission_require, Toast.LENGTH_SHORT).show();
        }
    }

    public void startActivity(int id) {
        Intent intent = null;

        if (id == R.id.button_start_face_register) {
            intent = new Intent(getApplicationContext(), FaceRegisterActivity.class);
        } else if (id == R.id.button_start_face_import) {
            intent = new Intent(getApplicationContext(), BatchRegisterActivity.class);
        }
        if (intent != null) {
            Log.d(TAG, "startActivity:" + intent);
            startActivity(intent);
        }
    }
}

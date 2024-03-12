package com.test.facerecognitionbyusbcamera;

import static com.test.facerecognitionbyusbcamera.FileUtil.showToast;
import static com.test.facerecognitionbyusbcamera.ManageActivity.dao;
import static com.test.facerecognitionbyusbcamera.ManageActivity.group_name;
import static com.test.facerecognitionbyusbcamera.ManageActivity.isLocalGroupExist;
import static com.test.facerecognitionbyusbcamera.ManageActivity.mFacePassHandler;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.test.facerecognitionbyusbcamera.db.FaceData;

import java.io.File;
import java.util.List;

import mcv.facepass.FacePassException;
import mcv.facepass.types.FacePassAddFaceResult;
import mcv.facepass.types.FacePassExtractFeatureResult;
import mcv.facepass.types.FacePassSearchResult;

public class FaceManageActivity extends AppCompatActivity implements View.OnClickListener {
    Button faceChoose;
    Button startFaceRegister;
    Button manageFaceButton;
    TextView imagePath;
    private static final int REQUEST_CODE_CHOOSE_PICK = 1;

    @Override
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_manage);
        faceChoose = findViewById(R.id.choose);
        faceChoose.setOnClickListener(this);
        startFaceRegister = findViewById(R.id.sure);
        startFaceRegister.setOnClickListener(this);
        manageFaceButton = findViewById(R.id.manageFace);
        manageFaceButton.setOnClickListener(this);
        imagePath = findViewById(R.id.path);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.choose) {
            Intent intentFromGallery = new Intent(Intent.ACTION_GET_CONTENT);
            intentFromGallery.setType("image/*"); // 设置文件类型
            intentFromGallery.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivityForResult(intentFromGallery, REQUEST_CODE_CHOOSE_PICK);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        } else if (view.getId() == R.id.sure) {
            boolean flag = false;
            if (mFacePassHandler == null) {
                System.out.println("人脸录入中，，，，， yApplication.mFacePassHandler == null");
                showToast(this, "FacePassHandle is null ! ");
                return;
            }
            if (TextUtils.isEmpty(imagePath.getText())) {
                showToast(this, "请输入正确的图片路径！");
                return;
            }

            File imageFile = new File(imagePath.getText().toString());
            if (!imageFile.exists()) {
                showToast(this, "图片不存在 ！");
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath.getText().toString());

            try {
                FacePassExtractFeatureResult facePassExtractFeatureResult = mFacePassHandler.extractFeature(bitmap, false, 0);
                if (isLocalGroupExist) {
                    if(facePassExtractFeatureResult.retCode == 0){
                        byte[] feature = facePassExtractFeatureResult.featureData;
                        FacePassSearchResult[] searchResult = mFacePassHandler.search(feature, group_name, 1);
                        if (searchResult != null) {
                            System.out.println("人脸录入中？？？？2searchResult[0].searchScore   " + searchResult[0].searchScore + "  " + searchResult[0].searchThreshold + " " + (searchResult[0].searchScore >= searchResult[0].searchThreshold));
                            if (searchResult[0].searchScore >= searchResult[0].searchThreshold) {
                                showToast(this, "此人已经注册过，无需再次注册！");
                            } else flag = true;
                        } else flag = true;
                    } else showToast(this, "图片识别失败！");
                }
                if(flag) {
                    FacePassAddFaceResult result = mFacePassHandler.addFace(bitmap, 0);
                    if (result != null) {
                        if (result.result == 0) {
                            if (mFacePassHandler == null) {
                                return;
                            }
                            if (result.faceToken == null || result.faceToken.length == 0 || TextUtils.isEmpty(group_name)) {
                                return;
                            }
                            try {
                                showDialogInsert(result.faceToken);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (result.result == 1) {
                            showToast(this, "没有检测到人脸");
                        } else {
                            showToast(this, "图片质量过低");
                        }
                    }
                }
            } catch (FacePassException e) {
                e.printStackTrace();
                showToast(this, e.getMessage());
            }
        } else if (view.getId() == R.id.manageFace) {
            Intent intent = new Intent(this, ManageFaceInfo.class);
            startActivity(intent);
        }
    }

    @SuppressLint("MissingInflatedId")
    public void showDialogInsert(byte[] faceToken) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.face_name_update, null);
        builder.setView(view);

        final EditText inputEditText = view.findViewById(R.id.inputName);
        final Button sure = view.findViewById(R.id.sure);
        final Button cancel = view.findViewById(R.id.cancel);
        builder.setTitle("输入信息：");
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        sure.setOnClickListener(v -> {
            String inputText = inputEditText.getText().toString().trim();
            if (!inputText.equals("")) {
                List<FaceData> list = dao.selectDataByName(inputText);
                if (list.size() != 0) showToast(this, "输入name已经存在，请重新输入");
                else {
                    boolean b;
                    try {
                        b = mFacePassHandler.bindGroup(group_name, faceToken);
                        String faceTokenStr = new String(faceToken);
                        if (b) {
                            FaceData faceData = new FaceData(0, inputText, faceTokenStr);
                            dao.insertData(faceData);
                        }
                        String bindResult = b ? "成功！" : "失败！";
                        showToast(this, "添加" + bindResult);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            } else showToast(this, "输入的字符非法，请重新输入");
        });
        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //从相册选取照片后读取地址
            case REQUEST_CODE_CHOOSE_PICK:
                if (resultCode == RESULT_OK) {
                    try {
                        String path = "";
                        Uri uri = data.getData();
                        String[] pojo = {MediaStore.Images.Media.DATA};
                        CursorLoader cursorLoader = new CursorLoader(this, uri, pojo, null, null, null);
                        Cursor cursor = cursorLoader.loadInBackground();
                        if (cursor != null) {
                            cursor.moveToFirst();
                            path = cursor.getString(cursor.getColumnIndex(pojo[0]));
                        }
                        if (!TextUtils.isEmpty(path) && "file".equalsIgnoreCase(uri.getScheme())) {
                            path = uri.getPath();
                        }
                        if (TextUtils.isEmpty(path)) {
                            path = FileUtil.getPath(getApplicationContext(), uri);
                        }
                        if (TextUtils.isEmpty(path)) {
                            showToast(this, "图片选取失败！");
                            return;
                        }
                        if (!TextUtils.isEmpty(path)) {
                            imagePath.setText(path);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
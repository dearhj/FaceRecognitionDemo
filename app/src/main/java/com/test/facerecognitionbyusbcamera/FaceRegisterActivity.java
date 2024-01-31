package com.test.facerecognitionbyusbcamera;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.rockchip.iva.RockIva;
import com.rockchip.iva.RockIvaCallback;
import com.rockchip.iva.RockIvaImage;
import com.rockchip.iva.face.RockIvaFaceFeature;
import com.rockchip.iva.face.RockIvaFaceInfo;
import com.rockchip.iva.face.RockIvaFaceLibrary;
import com.test.facerecognitionbyusbcamera.utils.BitmapUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.List;

public class FaceRegisterActivity extends AppCompatActivity {

    private ImageView mRegFaceImageView;

    private static final int REQUEST_CODE_TAKE_PHOTO = 2;
    private static final int REQUEST_CODE_PICK_IMAGE = 3;
    private static final int REQUEST_CODE_TAKE_PHOTO_BY_USB = 4;
    private Uri imageUri;

    private Handler mMainHandler;

    private String mName;

    private String mRegisterImagePath;
    private Bitmap mRegisterImageBitmap;

    private RockIvaFaceInfo mFaceInfo;
    private RockIvaFaceFeature mFaceFeature;

    private IvaFaceManager ivaFaceManager = null;
    private RockIva rockiva = null;
    private RockIvaFaceLibrary ivaFaceLibrary = null;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_register0);
        initView();
        mRegisterImagePath = getApplicationContext().getExternalCacheDir() + "/iva/";
        mMainHandler = new Handler(Looper.getMainLooper());
        mContext = getApplicationContext();

        ivaFaceManager = new IvaFaceManager(getApplicationContext());
        ivaFaceManager.initForRegister();
        ivaFaceManager.setCallback(mIvaCallback);
        rockiva = ivaFaceManager.getIva();
        ivaFaceLibrary = ivaFaceManager.getIvaFaceLibrary();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ivaFaceManager.release();
    }

    void initView() {
        mRegFaceImageView = findViewById(R.id.reg_face_imageView);
        mRegFaceImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(FaceRegisterActivity.this)
                        .setTitle(R.string.take_photo)
                        .setItems(new String[]{getString(R.string.use_camera), getString(R.string.gallery)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    takePhoto();
                                } else {
                                    Intent intent = new Intent(FaceRegisterActivity.this, StartFragmentTool.class);
                                    intent.putExtra("flag", "register");
                                    startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO_BY_USB);
//                                choosePhoto();
                                }
                            }
                        })
                        .create()
                        .show();
            }
        });
        final EditText nameEditText = findViewById(R.id.name_editText);
        nameEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    mName = nameEditText.getText().toString();
                    Log.d(Configs.TAG, "name=" + mName);
                    return true;
                }
                return false;
            }
        });
        Button mRegisterButton = findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFaceInfo == null) {
                    Toast.makeText(getApplicationContext(), R.string.tips_take_photo, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mName == null || mName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.tips_enter_name, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mFaceFeature == null || mFaceFeature.data == null) {
                    Toast.makeText(getApplicationContext(), R.string.tips_get_face_info, Toast.LENGTH_SHORT).show();
                    return;
                }
                RockIvaFaceLibrary.FaceRecord faceRecord = new RockIvaFaceLibrary.FaceRecord(mName, mFaceFeature);
                ivaFaceLibrary.addFace(faceRecord);
                Toast.makeText(getApplicationContext(), R.string.register_sucdess, Toast.LENGTH_LONG).show();
                FaceRegisterActivity.this.finish();
            }
        });
    }

    RockIvaCallback mIvaCallback = new RockIvaCallback() {
        @Override
        public void onResultCallback(String result, int execureState) {
            Log.d(Configs.TAG, "" + result);
            JSONObject jobj = JSONObject.parseObject(result);
            if (JSONPath.contains(jobj, "$.faceCapResults")) {
                int num = (int) JSONPath.eval(jobj, "$.faceCapResults.num");
                for (int i = 0; i < num; i++) {
                    JSONObject faceCapResultObj = (JSONObject) JSONPath.eval(jobj, String.format("$.faceCapResults.faceResults[%d]", i));
                    final int qualityResult = (int) JSONPath.eval(faceCapResultObj, "$.qualityResult");
                    if (qualityResult == 0) {
                        Object faceInfoJobj = JSONPath.eval(faceCapResultObj, "$.faceInfo");
                        mFaceInfo = JSONObject.parseObject(faceInfoJobj.toString(), RockIvaFaceInfo.class);
                        String feature = (String) JSONPath.eval(faceCapResultObj, "$.faceAnalyseInfo.feature");
                        mFaceFeature = new RockIvaFaceFeature(feature);
                        showResultView(mRegisterImageBitmap, mFaceInfo.faceRect);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "get face feature error：" + qualityResult, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }

        @Override
        public void onReleaseCallback(List<RockIvaImage> images) {
            for (RockIvaImage image : images) {
//                Log.d(TAG, "release image " + image.toString());
                image.release();
            }
        }
    };

    void takePhoto() {
        File file = new File(mRegisterImagePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File output = new File(file, System.currentTimeMillis() + ".jpg");
        Log.d(Configs.TAG, "save to " + output.getAbsolutePath());
        try {
            if (output.exists()) {
                output.delete();
            }
            output.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        imageUri = Uri.fromFile(output);
        Intent intent = new Intent(FaceRegisterActivity.this, TakePhotoActivity.class);
        intent.putExtra("IMAGE_SAVE_PATH", output.getAbsolutePath());
        startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }

    void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    public void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        switch (req) {
            case REQUEST_CODE_TAKE_PHOTO_BY_USB:
                try {
                    File image = new File(MyApplication.savePath);
                    Uri usbImageUri = Uri.fromFile(image);
                    detectFace(usbImageUri, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            case REQUEST_CODE_TAKE_PHOTO:
                if (res == RESULT_OK) {
                    try {
                        detectFace(imageUri, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i(Configs.TAG, "Take photo fail！");
                }
                break;

            case REQUEST_CODE_PICK_IMAGE:
                if (res == RESULT_OK) {
                    try {
                        Uri uri = data.getData();
                        detectFace(uri, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(Configs.TAG, e.getMessage());
                    }
                } else {
                    Log.i(Configs.TAG, "Pick photo fail！");
                }
                break;

            default:
                break;
        }
    }

    private int detectFace(Uri imageUri, int rotate) {
        Log.d(Configs.TAG, "image path: " + imageUri.getPath() + " rotate: " + rotate);
        Bitmap bit = null;
        try {
            bit = getDrawBitmap(imageUri);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (bit == null) {
            Log.e(Configs.TAG, "bitmap == null ");
            return -1;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        bit = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
        mRegisterImageBitmap = bit;

        int bytes = bit.getByteCount();
        Buffer buffer = ByteBuffer.allocate(bytes);
        bit.copyPixelsToBuffer(buffer);
        byte[] rgba = (byte[]) buffer.array();

        RockIvaImage image = new RockIvaImage(bit.getWidth(), bit.getHeight(), RockIvaImage.PixelFormat.RGBA8888);
        image.allocMem(RockIvaImage.MEMORY_TYPE_CPU);
        image.setImageData(rgba);
        rockiva.pushFrame(image);

        return 0;
    }

    private void showResultView(Bitmap frame, Rect faceBox) {
        Rect rectPixel = RockIva.convertRectRatioToPixel(frame.getWidth(), frame.getHeight(), faceBox, RockIvaImage.TransformMode.NONE);
        Bitmap bmp = BitmapUtil.cropBitmap(frame, rectPixel);
        if (bmp != null) {
            final Bitmap finalBmp = bmp;
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mRegFaceImageView.setImageBitmap(finalBmp);
                }
            });
        }
    }

    public Bitmap getDrawBitmap(Uri imageUri) throws FileNotFoundException {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        InputStream is = null;
        is = getContentResolver().openInputStream(imageUri);
        BitmapFactory.decodeStream(is, null, opt);
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        opt.inJustDecodeBounds = false;
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        is = getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, opt);
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}

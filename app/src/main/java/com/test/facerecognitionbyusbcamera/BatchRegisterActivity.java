package com.test.facerecognitionbyusbcamera;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.rockchip.iva.RockIva;
import com.rockchip.iva.RockIvaCallback;
import com.rockchip.iva.RockIvaImage;
import com.rockchip.iva.face.RockIvaFaceLibrary;
import com.test.facerecognitionbyusbcamera.filemanager.FileManagerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BatchRegisterActivity extends AppCompatActivity implements View.OnClickListener {

    public final static int REQUEST_CHOOSE_IMPORT_IMG = 0;

    private String importImgDir;
    private String lastImportImgDir;

    private Button btnSelectImportDir;
    private Button btnStart;

    private TextView textViewImportDir;
    private TextView textViewTips;
    private TextView textViewList;

    private IvaFaceManager ivaFaceManager = null;
    private RockIva rockiva = null;
    private RockIvaFaceLibrary rockIvaFaceLibrary = null;

    private RunBatchFaceRegisterTask asyncTask;
    List<String> totalList = new ArrayList<String>();
    List<String> workList = new ArrayList<String>();
    List<String> importList = new ArrayList<String>();
    List<String> failedList = new ArrayList<String>();
    private boolean import_status = false;
    private boolean isContinue = false;

    private SparseArray<String> regImageMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_register);
        btnSelectImportDir = findViewById(R.id.button_select_import_img_dir);
        btnSelectImportDir.setOnClickListener(this);
        btnStart = findViewById(R.id.button_start);
        btnStart.setOnClickListener(this);
        btnStart.setEnabled(false);
        textViewImportDir = findViewById(R.id.textView_import_img_dir);
        textViewTips = findViewById(R.id.textView_tips);
        textViewList = findViewById(R.id.tv_failed);
        textViewList.setMovementMethod(ScrollingMovementMethod.getInstance());

        regImageMap = new SparseArray<>();

        ivaFaceManager = new IvaFaceManager(getApplicationContext());
        ivaFaceManager.initForRegister();
        ivaFaceManager.setCallback(mIvaCallback);
        rockiva = ivaFaceManager.getIva();
        rockIvaFaceLibrary = ivaFaceManager.getIvaFaceLibrary();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ivaFaceManager.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = null;
        String imgPath = null;
        if (data != null && (bundle = data.getExtras()) != null) {
            imgPath = bundle.getString("path");
            Log.d(Configs.TAG, "choose: " + imgPath);
        }
        if (imgPath == null) {
            btnStart.setEnabled(false);
            return;
        }
        if (resultCode == FileManagerActivity.RESULT_CODE_CHOOSED_PATH) {
            switch (requestCode) {
                case REQUEST_CHOOSE_IMPORT_IMG:
                    importImgDir = imgPath;
                    textViewImportDir.setText(imgPath);
                    if (importList.equals(lastImportImgDir)) {
                        isContinue = true;
                        btnStart.setText(R.string.face_import_continue);
                    } else {
                        isContinue = false;
                        textViewTips.setText(R.string.face_import_dir_select);
                    }
                    Log.d(Configs.TAG, "REQUEST_CHOOSE_IMPORT_IMG:importImgDir=" + importImgDir + ",lastImportImgDir=" + lastImportImgDir);
                    textViewList.setText("");
                    btnStart.setText(R.string.face_import_btn);
                    btnStart.setEnabled(true);
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.button_select_import_img_dir){
            Intent intent = new Intent(getApplicationContext(), FileManagerActivity.class);
            startActivityForResult(intent, REQUEST_CHOOSE_IMPORT_IMG);
        } else if(view.getId() == R.id.button_start) {
            Log.d(Configs.TAG, "button_start importImgDir=" + importImgDir + ",lastImportImgDir=" + lastImportImgDir);
            if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                import_status = false;
                asyncTask.cancel(true);
                if (importImgDir.equalsIgnoreCase(lastImportImgDir)) {
                    isContinue = true;
                    btnStart.setText(R.string.face_import_continue);
                    textViewTips.setText(String.format(getString(R.string.import_pause), importList.size(), totalList.size()));
                } else {
                    isContinue = false;
                    btnStart.setText(R.string.face_import_btn);
                    textViewTips.setText("");
                }
                Log.d(Configs.TAG, "button_start isContinue=" + isContinue);
                asyncTask = null;
            } else {
                if (importImgDir == null) {
                    Toast.makeText(getApplicationContext(), R.string.select_dir_to_import, Toast.LENGTH_LONG).show();
                    return;
                }
                lastImportImgDir = importImgDir;
                import_status = true;
                asyncTask = new RunBatchFaceRegisterTask();
                asyncTask.execute(importImgDir);
                btnStart.setText(R.string.import_stop);
            }
        }
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    RockIvaCallback mIvaCallback = new RockIvaCallback() {
        @Override
        public void onResultCallback(String result, int execureState) {
            Log.d(Configs.TAG, ""+result);

            JSONObject jobj = JSONObject.parseObject(result);
            if (JSONPath.contains(jobj, "$.faceCapResult")) {
                int frameId = (int) JSONPath.eval(jobj, String.format("$.faceCapResult.frameId"));
                int qualityResult = (int) JSONPath.eval(jobj, String.format("$.faceCapResult.qualityResult"));
                String regImagePath = regImageMap.get(frameId);
                if (regImagePath == null) {
                    Log.e(Configs.TAG, "regimagePath is null frameId="+frameId);
                    return;
                }
                if (qualityResult != 0) {
                    Log.w(Configs.TAG, String.format("frameId=%d imagePath=%s register face fail qualityResultCode=%d", frameId, regImagePath, qualityResult));
                    failedList.add(regImagePath);
                    return;
                }
                String featureStr = (String) JSONPath.eval(jobj, String.format("$.faceCapResult.faceAnalyseInfo.feature"));
                String faceId = getFileNameNoEx(new File(regImagePath).getName());
                RockIvaFaceLibrary.FaceRecord faceRecord = new RockIvaFaceLibrary.FaceRecord(faceId, featureStr);
                int ret = rockIvaFaceLibrary.addFace(faceRecord);
                if (ret != 0) {
                    Log.e(Configs.TAG, "register fail");
                    failedList.add(regImagePath);
                    return;
                }
                importList.add(regImagePath);
            }
        }

        @Override
        public void onReleaseCallback(List<RockIvaImage> images) {
            for (RockIvaImage image : images) {
//                Log.d(Configs.TAG, "release image " + image.toString());
                image.release();
            }
        }
    };

    private class RunBatchFaceRegisterTask extends AsyncTask<String, Integer, Integer> {

        // private int totalImportImgCount;
        private int count = 0;
        private String importImgDirPath;
        private int index = 0;

        protected Integer doInBackground(String... pathList) {
            publishProgress(-99);
            int ret = -1;

            Log.d(Configs.TAG, "ret=" + ret);

            importImgDirPath = pathList[0];
            Log.d(Configs.TAG, "button_start importImgDirPath=" + importImgDirPath + ", isContinue=" + isContinue);

            getFilesList(importImgDirPath);

            for (String imgPath : totalList) {
                index++;
                Log.d(Configs.TAG, "process " + imgPath);
                RockIvaImage image = RockIvaImage.read(imgPath);
                image.frameId = index;
                rockiva.pushFrame(image);
                regImageMap.put(index, imgPath);
            }

            return importList.size();
        }

        private void getFilesList(String path) {
            //ArrayList<String> result = new ArrayList<>();
            File file = new File(path);
            if (file.exists()) {
                File[] files = file.listFiles();
                if (null != files) {
                    for (File file2 : files) {
                        if (file2.isDirectory()) {
                            // Log.d(Configs.TAG,"文件夹:" + file2.getAbsolutePath());
                            getFilesList(file2.getAbsolutePath());
                        } else {
                            totalList.add(file2.getAbsolutePath());
                        }
                    }
                } else {
                    totalList.add(path);
                }
            } else {
                Log.d(Configs.TAG, getString(R.string.file_not_exist));
            }
            //return result;
        }

        protected void onProgressUpdate(Integer... progress) {
            int p = progress[0];
            if (p == -99) {
                textViewTips.setText(R.string.author_model_init);
            } else {
                textViewTips.setText(String.format(getString(R.string.importing), p, totalList.size()));
            }
        }

        protected void onPostExecute(Integer result) {
            if (result < 0) {
                textViewTips.setText(R.string.import_failed);
            } else {
                textViewTips.setText(String.format(getString(R.string.import_complete), result, totalList.size()));
                if (!failedList.isEmpty()) {
                    String str_show = getString(R.string.import_failed_file_list);
                    for (String str : failedList)
                        str_show += str + "\n";
                    textViewList.setText(str_show);
                }
            }
            btnStart.setText(R.string.face_import_btn);
            btnStart.setEnabled(false);
        }
    }

    private static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
}

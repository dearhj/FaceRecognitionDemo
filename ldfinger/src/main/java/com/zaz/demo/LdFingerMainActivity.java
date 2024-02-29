package com.zaz.demo;


import static com.zaz.demo.ExtKt.showToast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.za.finger.ZAAPI;
import com.zaz.demo.db.FingerData;
import com.zaz.demo.db.FingerDataBase;
import com.zaz.demo.db.FingerDataDao;
import com.zaz.zazjni.ZAZJni;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class LdFingerMainActivity extends AppCompatActivity {
    private static final String TAG = LdFingerMainActivity.class.getSimpleName();
    private Switch mtoumingSwitch;
    private Switch mblakredSwitch;
    private Switch mskipSwitch;
    private Switch mimgxhSwitch;

    private Button mConnBtn;
    private Button mDisConnBtn;
    private Button mCancelBtn;
    private Button mPressBtn;
    private Button mmatchBtn;
    private Button mGetfingerlist;
    private Button mEmqtyBtn;
    private Button mGetInfoBtn;
    private Button mEnrollBtn;
    private Button mVerifyBtn;

    private ImageView mImageView;
    private TextView mHintTv;
    private Context mContext;

    /////////////////
    public static final ZAAPI zaclient = new ZAAPI();
    private final ZAZJni zazJni = new ZAZJni();
    public static int DEV_ADDR = 0xffffffff;
    int fpno = 1;
    boolean iscancle = false;
    boolean isshowbmp = true;
    boolean isxihua = true;
    int timeskipok = 0;
    int timeskiperror = 0;
    //////////
    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    List<String> mPermissionList = new ArrayList<>();
    int status = 0;
    private static final int PERMISSION_REQUEST = 1;

    public static FingerDataBase database;
    public static FingerDataDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_ld);

        mContext = this;
        mtoumingSwitch = findViewById(R.id.toumingSwitch);
        mblakredSwitch = findViewById(R.id.blakredSwitch);
        mskipSwitch = findViewById(R.id.skipSwitch);
        mimgxhSwitch = findViewById(R.id.imgxhSwitch);
        mConnBtn = findViewById(R.id.connBtn);
        mDisConnBtn = findViewById(R.id.disconnBtn);
        mCancelBtn = findViewById(R.id.cancelBtn);
        mGetInfoBtn = findViewById(R.id.getInfoBtn);
        mPressBtn = findViewById(R.id.pressBtn);
        mmatchBtn = findViewById(R.id.match);
        mGetfingerlist = findViewById(R.id.getfingerlist);
        mEmqtyBtn = findViewById(R.id.getemqtyBtn);

        mEnrollBtn = findViewById(R.id.enrollBtn);
        mVerifyBtn = findViewById(R.id.verifyBtn);
        mImageView = findViewById(R.id.imgV);
        mHintTv = findViewById(R.id.hintTv);

        btnOnClick();
        checkPermission();

        database = FingerDataBase.Companion.create(this);
        dao = database.fingerDataDao();
    }

    @SuppressLint("MissingInflatedId")
    public void showDialogInsert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.finger_insert, null);
        builder.setView(view);

        final EditText inputEditText = view.findViewById(R.id.inputEditText);
        final Button sure = view.findViewById(R.id.sure);
        final Button cancel = view.findViewById(R.id.cancel);
        builder.setTitle("输入信息：");
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        sure.setOnClickListener(v -> {
            String inputText = inputEditText.getText().toString().trim();
            if (!inputText.equals("")) {
                List<FingerData> list = dao.selectDataByName(inputText);
                if (list.size() != 0) showToast(this, "输入name已经存在，请重新输入");
                else {
                    int ret = zaclient.ZAZStoreChar(DEV_ADDR, 1, fpno);
                    if (ret == 0) {
                        String msg = "注册成功 ID:" + fpno + ", name:" + inputText + "\r\n";
                        m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                        FingerData fingerData = new FingerData(0, inputText, fpno);
                        dao.insertData(fingerData);
                        fpno++;
                    } else {
                        String msg = "注册失败";
                        m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                    }
                    dialog.dismiss();
                }
            } else showToast(this, "输入的字符非法，请重新输入");
        });
        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    private void checkPermission() {
        mPermissionList.clear();
        //判断哪些权限未授予
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }
        if (!mPermissionList.isEmpty()) {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[0]);//将List转为数组
            ActivityCompat.requestPermissions(LdFingerMainActivity.this, permissions, PERMISSION_REQUEST);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSION_REQUEST) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    ////////////////
    // 心跳任务/
    ////////////////
    Handler handlers = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //要做的事情
            int ret = 0;
            byte[] uuid = new byte[4];
            if (iscancle) {
                handlers.postDelayed(this, 5000);//表示第一次运行时的时间延迟
                Log.e(TAG, "设备在通讯中");
            } else {
                ret = zaclient.ZAZGetRandomData(DEV_ADDR, uuid);
                if (ret == 0) {
                    timeskipok++;
                } else {
                    timeskiperror++;
                }
                m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "心跳  正常次数:" + timeskipok + "  异常次数" + timeskiperror));
                Log.e(TAG, "timeskip ret = " + ret);
                handlers.postDelayed(this, 5000);//表示第一次运行时的时间延迟
            }

        }
    };

    private void btnOnClick() {
        mConnBtn.setOnClickListener(v -> {
            connBtnClick();//打开设备
        });
        mDisConnBtn.setOnClickListener(v -> {
            disconnBtnClick();//关闭设备
        });
        mCancelBtn.setOnClickListener(v -> {
            cancelBtnClick();//取消操作
        });
        mGetInfoBtn.setOnClickListener(v -> {
            getDevInfo();//获取参数信息
        });
        mPressBtn.setOnClickListener(v -> {
            pressBtnClick();//获取图像
        });
        mmatchBtn.setOnClickListener(v -> {
            matchClick();//1:1
        });
        mGetfingerlist.setOnClickListener(v -> {
            getfingerlistClick();//获取指纹列表
        });
        mEmqtyBtn.setOnClickListener(v -> {
            EmqtyBtnClick();//清空指纹
        });
        mEnrollBtn.setOnClickListener(v -> {
            EnrollBtnClick();//注册指纹
        });
        mVerifyBtn.setOnClickListener(v -> {
            VerifyBtnClick();//搜索指纹
        });
        mskipSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                handlers.postDelayed(runnable, 1000);
            } else {
                handlers.removeCallbacks(runnable);
            }
        });

        mimgxhSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> isxihua = isChecked);
    }

    public static final int msgshow = 101;
    public static final int bmpshow = 102;
    public static final int alertshow = 103;
    public static final int enbtn = 104;

    @SuppressLint("HandlerLeak")
    private final Handler m_fEvent = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case msgshow, enbtn -> mHintTv.setText(msg.obj.toString());
                case bmpshow -> ShowFingerBitmap((byte[]) (msg.obj), 256, (int) (msg.arg1) / 256);
                case alertshow -> alert(msg.obj.toString());
            }
        }
    };

    void alert(String str) {
        new AlertDialog.Builder(mContext)
                .setTitle("提示信息：")
                .setMessage(str)
                .setPositiveButton("确定", null)
                .show();
    }


    private void Sleep(int times) {
        try {
            Thread.sleep(times);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void connBtnClick() {
        Runnable r = this::OpenDev;
        Thread s = new Thread(r);
        s.start();
    }

    protected void disconnBtnClick() {
        Runnable r = this::CloseDev;
        Thread s = new Thread(r);
        s.start();
    }

    protected void cancelBtnClick() {
        Runnable r = this::Cancel;
        Thread s = new Thread(r);
        s.start();
    }

    protected void getDevInfo() {
        Runnable r = this::getFingerInfo;
        Thread s = new Thread(r);
        s.start();
    }

    protected void pressBtnClick() {
        zaclient.ZAZSetImageSize(256 * 360);
        if (iscancle) {
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "请先取消"));
            return;
        }
        m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "请按压指纹采集器"));
        iscancle = true;
        Runnable r = this::GetImage;
        Thread s = new Thread(r);
        s.start();
    }

    protected void matchClick() {
        if (!openFlag) {
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "设备未打开"));
            return;
        }
        if (iscancle) {
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "请先取消"));
            return;
        }
        iscancle = true;
        Runnable r = this::match;
        Thread s = new Thread(r);
        s.start();
    }

    protected void getfingerlistClick() {
        if (openFlag) {
            Runnable r = this::GetFingerList;
            Thread s = new Thread(r);
            s.start();
        } else m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "请先打开"));
    }

    protected void EmqtyBtnClick() {
        if (openFlag) {
            if (iscancle) {
                m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "请先取消"));
                return;
            }
            Intent intent = new Intent(this, ManagementFinger.class);
            startActivity(intent);
        } else m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "请先打开"));
    }

    protected void EnrollBtnClick() {
        zaclient.ZAZSetImageSize(256 * 360);
        if (iscancle) {
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "请先取消"));
            return;
        }
        iscancle = true;
        Runnable r = this::Enrollfinger;
        Thread s = new Thread(r);
        s.start();
    }

    protected void VerifyBtnClick() {
        zaclient.ZAZSetImageSize(256 * 360);
        if (iscancle) {
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "请先取消"));
            return;
        }
        iscancle = true;
        Runnable r = this::Searchfinger;
        Thread s = new Thread(r);
        s.start();
    }

    boolean openFlag = false;

    //打开设备
    private void OpenDev() {

        int len = 0;
        int[] pParTable = new int[1024];
        isshowbmp = true;
        status = zaclient.opendevice(mContext, 1, 4, 6);
        if (status == 1) {
            len = zaclient.ZAZReadIndexTable(DEV_ADDR, pParTable);
            try {
                fpno = pParTable[len - 1] + 1;
            } catch (Exception e) {
                fpno = 1;
            }
            openFlag = true;
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "打开USB成功 status = " + status));
            if (dao.selectAllData().size() == 0) {
                Runnable r = this::EmqtyFinger;
                Thread s = new Thread(r);
                s.start();
            }
        } else if (status == 100)
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "已经打开，无需再次操作"));
        else
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "打开USB失败 status = " + status));
    }

    private void CloseDev() {
        iscancle = false;
        Sleep(200);
        int status = zaclient.ZAZCloseDeviceEx();
        if (status == 1) {
            openFlag = false;
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "关闭成功 status = " + status));
        } else
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "已经关闭!"));
    }

    private void Cancel() {
        if (!iscancle) m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "已经取消！"));
        else {
            iscancle = false;
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "取消成功！"));
        }
    }

    private void getFingerInfo() {
        byte[] pVersion = new byte[512];
        String msg;
        int ret = zaclient.ZAZReadInfPage(DEV_ADDR, pVersion);
        msg = "读取版本 ret = " + ret + "\r\n";
        msg += "库大小:" + (((pVersion[4] & 0xff) * 256) + (pVersion[5] & 0xff)) + "\r\n";
        msg += "波特率:" + (((pVersion[14] & 0xff) * 256) + (pVersion[15] & 0xff)) * 9600 + "\r\n";
        msg += "产品系列：" + new String(pVersion, 28, 8) + "\r\n";
        msg += "软件版本：" + new String(pVersion, 36, 8) + "\r\n";
        msg += "厂家名称：" + new String(pVersion, 44, 8) + "\r\n";
        m_fEvent.sendMessage(m_fEvent.obtainMessage(alertshow, 0, 0, msg));
    }

    private void match() {
        int bufferid = 1;
        byte[] Image = new byte[256 * 360];
        int[] len = new int[1];
        byte[] fp1 = new byte[512];
        byte[] fp2 = new byte[512];
        int count = 1;
        String msg = "";
        int ret = 0;
        Long startTime = System.currentTimeMillis();
        while (iscancle) {
            msg = "请按捺第" + count + "枚指纹\r\n";
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
            Long endTime = System.currentTimeMillis();
            long elapsedTime = (endTime - startTime) / 1000;
            if (elapsedTime > 20) {
                m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "采集超时"));
                return;
            }
            ret = zaclient.ZAZGetImage(DEV_ADDR);
            if (ret == 0) {
                if (isxihua) {
                    ret = zaclient.ZAZUpImagenew(DEV_ADDR, Image, len);
                } else {
                    ret = zaclient.ZAZUpImage(DEV_ADDR, Image, len);
                }
                m_fEvent.sendMessage(m_fEvent.obtainMessage(bmpshow, len[0], 0, Image));
                ret = zaclient.ZAZGenChar(DEV_ADDR, bufferid);
                if (ret == 0) {
                    msg = "生成特征成功 buf=" + bufferid + "\r\n";
                    m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                    bufferid++;
                }
                if (bufferid < 3)
                    continue;
                bufferid = 1;
                ret = zaclient.ZAZRegModule(DEV_ADDR);
                if (ret != 0) {
                    msg = "合成失败\r\n";
                    m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                    Sleep(500);
                    continue;
                }
                msg = "合成模板" + count + "成功\r\n";
                m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));

                if (count == 1) {
                    zaclient.ZAZUpChar(DEV_ADDR, 1, fp1, len);
                } else if (count == 2) {
                    zaclient.ZAZUpChar(DEV_ADDR, 1, fp2, len);
                }
                count++;
                if (count > 2) {
                    msg = "两枚指纹比对结果：" + zazJni.ZACompareC2CSTemplates(fp1, fp2) + "分";
                    m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                    break;
                }
            }
        }
        iscancle = false;
    }

    private void GetFingerList() {
        int len = 0;
        String msg;
        int[] pParTable = new int[1024];
        len = zaclient.ZAZReadIndexTable(DEV_ADDR, pParTable);
        msg = "FingerDB:";
        for (int i = 0; i < len; i++) {
            msg += "" + pParTable[i] + ",";
        }
        m_fEvent.sendMessage(m_fEvent.obtainMessage(alertshow, 0, 0, msg));
    }

    private void EmqtyFinger() {
        zaclient.ZAZEmpty(DEV_ADDR);
        fpno = 1;
    }


    private void ShowFingerBitmap(byte[] image, int width, int height) {
        if (width == 0) return;
        if (height == 0) return;
        int[] RGBbits = new int[width * height];
        mImageView.invalidate();
        zaclient.ZAZSetImageSize(256 * 360);

        for (int i = 0; i < width * height; i++) {
            int v;
            int red = 0;
            if (image != null) v = image[i] & 0xff;
            else v = 0;
            if (mblakredSwitch.isChecked()) {
                red = 255;
            } else
                red = v;
            if (mtoumingSwitch.isChecked()) {
                if (v < 200)
                    RGBbits[i] = Color.argb(255, red, v, v);
                else
                    RGBbits[i] = Color.argb(0, red, v, v);
            } else
                RGBbits[i] = Color.argb(255, red, v, v);

        }
        Bitmap bmp = Bitmap.createBitmap(RGBbits, width, height, Bitmap.Config.ARGB_8888);
        mImageView.setImageBitmap(bmp);

    }

    @SuppressLint("SdCardPath")
    private void GetImage() {
        int ret = 0;
        String msg = "";
        int qsroce = 0;
        byte[] Image = new byte[256 * 360];
        int[] len = new int[1];
        int timecount = 0;
        while (iscancle) {
            ret = zaclient.ZAZGetImage(DEV_ADDR);
            if (ret == 0) {
                if (isshowbmp) {
                    if (isxihua) {
                        ret = zaclient.ZAZUpImagenew(DEV_ADDR, Image, len);
                    } else {
                        ret = zaclient.ZAZUpImage(DEV_ADDR, Image, len);
                    }
                    qsroce = ZAZGetImgQuality(256, len[0] / 256, Image);
                    if (ret != 0) {
                        msg = "上传图像失败";
                        m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                    }
                }
                try {
                    File fp = new File("/mnt/sdcard/fp8800.bmp");
                    if (fp.exists()) fp.delete();
                    ret = zaclient.ZAZImgData2BMP(Image, "/mnt/sdcard/fp8800.bmp");
                    msg = "获取图像成功 " + ret + "质量" + qsroce + "\r\n";
                    m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                    m_fEvent.sendMessage(m_fEvent.obtainMessage(bmpshow, len[0], 0, Image));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ret != 2) {
                msg = "获取图像异常 ret=" + ret + "\r\n";
                m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                iscancle = false;
                return;
            } else {
                timecount++;
                // msg = "连续获取指纹图像中\r\n请按“取消”停止当前操作\r\n获取图像中 time="+timecount+"\r\n";
                //  m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow,0, 0,msg));
            }
        }
        msg += "操作已取消";
        m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
        iscancle = false;
    }

    private void Enrollfinger() {
        int ret = 0;
        String msg = "";
        byte[] Image = new byte[256 * 360];
        int[] len = new int[1];
        int timecount = 0;
        int bufferid = 1;
        int[] iMbAddress = new int[1];
        int[] iscore = new int[1];
        while (iscancle) {
            ret = zaclient.ZAZGetImage(DEV_ADDR);
            if (ret == 0) {
                if (isshowbmp) {
                    if (isxihua) {
                        ret = zaclient.ZAZUpImagenew(DEV_ADDR, Image, len);
                    } else {
                        ret = zaclient.ZAZUpImage(DEV_ADDR, Image, len);
                    }
                    if (ret != 0) {
                        msg = "上传图像失败";
                        m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                    }
                }
                msg = "获取图像成功\r\n";
                m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                m_fEvent.sendMessage(m_fEvent.obtainMessage(bmpshow, len[0], 0, Image));

                ret = zaclient.ZAZGenChar(DEV_ADDR, bufferid);
                if (ret == 0) {
                    bufferid++;
                    msg += "生成特征成功 buf=" + bufferid + "\r\n";
                    m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                }
                if (bufferid < 3)
                    continue;
                bufferid = 1;
                ret = zaclient.ZAZRegModule(DEV_ADDR);
                if (ret != 0) {
                    msg += "合成失败\r\n";
                    m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                    Sleep(500);
                    continue;
                }
                ret = zaclient.ZAZSearch(DEV_ADDR, 1, 0, 999, iMbAddress, iscore);
                if (ret == 0 && iscore[0] > 70) {
                    m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "该指纹已经录入，请重试"));
                    break;
                }
                runOnUiThread(this::showDialogInsert);
                iscancle = false;
            } else if (ret != 2) {
                msg = "获取图像异常 ret=" + ret + "\r\n";
                m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                iscancle = false;
                return;
            } else {
                timecount++;
                msg = "注册指纹中\r\n请按“取消”停止当前操作\r\n获取图像中 time=" + timecount + "\r\n";
                m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
            }
        }
        iscancle = false;
    }

    private void Searchfinger() {
        int ret = 0;
        String msg = "";
        byte[] Image = new byte[256 * 360];
        int[] len = new int[1];
        int[] iMbAddress = new int[1];
        int[] iscore = new int[1];
        int timecount = 0;
        while (iscancle) {
            ret = zaclient.ZAZGetImage(DEV_ADDR);
            if (ret == 0) {
                if (isshowbmp) {
                    if (isxihua) {
                        ret = zaclient.ZAZUpImagenew(DEV_ADDR, Image, len);
                    } else {
                        ret = zaclient.ZAZUpImage(DEV_ADDR, Image, len);
                    }
                    if (ret != 0) {
                        msg = "上传图像失败";
                        m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                    }
                }
                msg = "获取图像成功\r\n";
                m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                m_fEvent.sendMessage(m_fEvent.obtainMessage(bmpshow, len[0], 0, Image));

                ret = zaclient.ZAZGenChar(DEV_ADDR, 1);
                if (ret != 0) {
                    msg += "生成特征失败 \r\n";
                    m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                }
                ret = zaclient.ZAZSearch(DEV_ADDR, 1, 0, 999, iMbAddress, iscore);
                if (ret == 0) {
                    List<String> name = dao.selectNameById(iMbAddress[0]);
                    if (name.size() != 0)
                        msg += "搜索成功  ID:" + iMbAddress[0] + ", name: " + name.get(0) + ", 得分:" + iscore[0] + "\r\n";
                    else msg += "搜索成功  ID:" + iMbAddress[0] + "  得分:" + iscore[0] + "\r\n";
                    break;
                } else {
                    msg += "搜索失败" + "\r\n";
                }
                m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                Sleep(500);
            } else if (ret != 2) {
                msg = "获取图像异常 ret=" + ret;
                m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                iscancle = false;
                return;
            } else {
                timecount++;
                msg = "搜索指纹中\r\n请按“取消”停止当前操作\r\n获取图像中 time=" + timecount + "\r\n";
                m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
            }
        }
        m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
        iscancle = false;
    }

    //建议正常手指判断值为>25000
    int ZAZGetImgQuality(int width, int height, byte[] p_pbImage) {
        int y, x, nSum, nSum0, nCentPix;
        int p1 = 0;
        nSum = 0;
        for (y = 4; y <= height - 32; y += 16) {
            //p1 = p_pbImage+256*y+20;
            for (x = 8; x < width - 8; x += 8, p1 += 8) {
                nCentPix = p_pbImage[256 * y + p1];//p1[256*2+2];
                nSum0 = Math.abs(nCentPix - p_pbImage[256 * y + x])
                        + Math.abs(nCentPix - p_pbImage[256 * y + x + 4])
                        + Math.abs(nCentPix - p_pbImage[256 * y + x + 256 * 4])
                        + Math.abs(nCentPix - p_pbImage[256 * y + x + 256 * 4 + 4]);
                nSum += nSum0;
              /*  if (nSum0 >= 100){

                }*/
            }
        }
        if (nSum > 120000)
            nSum = 100;
        else
            nSum = nSum / 1200;
        return nSum;
    }
}

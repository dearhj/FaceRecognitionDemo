package com.zaz060.demo;

import static com.zaz060.demo.ExtKt.showToast;
import static java.lang.Thread.sleep;

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
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.zaz060.demo.db.FingerData;
import com.zaz060.demo.db.FingerDataBase;
import com.zaz060.demo.db.FingerDataDao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


@SuppressLint("UseSwitchCompatOrMaterialCode")
public class ZazFingerMainActivity extends AppCompatActivity {
    private static final String TAG = ZazFingerMainActivity.class.getSimpleName();
    private Switch mtoumingSwitch;
    private Switch mblakredSwitch;
    private Switch mskipSwitch;

    private Button mConnBtn;
    private Button mDisConnBtn;
    private Button mCancelBtn;
    private Button mPressBtn;
    private Button mGetSnBtn;
    private Button mGetfingerlist;
    private Button mDelBtn;
    private Button mGetInfoBtn;
    private Button mEnrollBtn;
    private Button mVerifyBtn;

    private ImageView mImageView;
    private TextView mHintTv;
    private Context mContext;

    /////////////////
    public static ZAAPI zaclient = new ZAAPI();
    public static int DEV_ADDR = 0xffffffff;
    int fpno = 1;
    boolean iscancle = false;
    boolean isshowbmp = false;
    int timeskipok = 0;
    int timeskiperror = 0;
    //////////
    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    List<String> mPermissionList = new ArrayList<>();

    private static final int PERMISSION_REQUEST = 1;

    public static FingerDataBase database;
    public static FingerDataDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_horizontal);


        mContext = this;
        mtoumingSwitch = findViewById(R.id.toumingSwitch);
        mblakredSwitch = findViewById(R.id.blakredSwitch);
        mskipSwitch = findViewById(R.id.skipSwitch);
        mConnBtn = findViewById(R.id.connBtn);
        mDisConnBtn = findViewById(R.id.disconnBtn);
        mCancelBtn = findViewById(R.id.cancelBtn);
        mGetInfoBtn = findViewById(R.id.getInfoBtn);
        mPressBtn = findViewById(R.id.pressBtn);
        mGetSnBtn = findViewById(R.id.getSnBtn);
        mGetfingerlist = findViewById(R.id.getfingerlist);
        mDelBtn = findViewById(R.id.getemqtyBtn);

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
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        /**
         * 判断是否为空
         */
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(ZazFingerMainActivity.this, permissions, PERMISSION_REQUEST);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST:
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
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
                return;
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
        mGetSnBtn.setOnClickListener(v -> {
            getSnBtnClick();//获取UUID
        });
        mGetfingerlist.setOnClickListener(v -> {
            getfingerlistClick();//获取指纹列表
        });
        mDelBtn.setOnClickListener(v -> {
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
    }

    public static final int msgshow = 101;
    public static final int bmpshow = 102;
    public static final int alertshow = 103;

    @SuppressLint("HandlerLeak")
    private final Handler m_fEvent = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case msgshow:
                    mHintTv.setText(msg.obj.toString());
                    break;
                case bmpshow:
                    ShowFingerBitmap((byte[]) (msg.obj), 256, (int) (msg.arg1) / 256);
                    break;
                case alertshow:
                    alert(msg.obj.toString());
                    break;
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
            sleep(times);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void connBtnClick() {
        Runnable r = () -> {
            Sleep(200);
            OpenDev();
        };
        Thread s = new Thread(r);
        s.start();
    }

    protected void cancelBtnClick() {
        if (!iscancle) m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "已经取消！"));
        else {
            iscancle = false;
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "取消成功！"));
        }
    }

    protected void disconnBtnClick() {
        Runnable r = this::CloseDev;
        Thread s = new Thread(r);
        s.start();
    }

    protected void getDevInfo() {
        Runnable r = this::getFingerInfo;
        Thread s = new Thread(r);
        s.start();
    }

    protected void pressBtnClick() {
        zaclient.ZAZSetImageSize(256 * 288);
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

    protected void getSnBtnClick() {
        Runnable r = this::GetFingerSn;
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
        zaclient.ZAZSetImageSize(256 * 288);
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
        zaclient.ZAZSetImageSize(256 * 288);
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
        int status = 0;
        int len = 0;
        int[] pParTable = new int[1024];
        isshowbmp = true;
        status = zaclient.opendevice(mContext, 1, 4, 6, 0, 0);
        if (status == 1) {
            len = zaclient.ZAZReadIndexTable(DEV_ADDR, pParTable);
            try {
                System.out.println("这里的结果时111？？？？？   " + len);
                fpno = pParTable[len - 1] + 1;
                System.out.println("这里的结果时？？？？？   " + fpno);
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
        Sleep(300);
        int status = zaclient.ZAZCloseDeviceEx();
        if (status == 1) {
            openFlag = false;
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "关闭成功 status = " + status));
        } else
            m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "已经关闭！"));
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

    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        // 使用String的format方法进行转换
        for (byte b : bytes) {
            sb.append(String.format("%02x", new Integer(b & 0xff)));
        }

        return sb.toString();
    }

    private void GetFingerSn() {
        int ret = 0;
        String msg;
        byte[] uuid = new byte[4];
        ret = zaclient.ZAZGetRandomData(DEV_ADDR, uuid);
        msg = "UUID:" + toHexString(uuid);
        m_fEvent.sendMessage(m_fEvent.obtainMessage(alertshow, 0, 0, msg));
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
        zaclient.ZAZSetImageSize(256 * 288);

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
        byte[] Image = new byte[256 * 288];
        int[] len = new int[1];
        int timecount = 0;
        while (iscancle) {
            ret = zaclient.ZAZGetImage(DEV_ADDR);
            if (ret == 0) {
                if (isshowbmp) {
                    ret = zaclient.ZAZUpImage(DEV_ADDR, Image, len);
                    qsroce = ZAZGetImgQuality(256, len[0] / 256, Image);
                    if (ret != 0) {
                        msg = "上传图像失败";
                        m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                    }
                }
                try {
                    File fp = new File("/mnt/sdcard/fp.bmp");
                    if (fp.exists()) fp.delete();
                    ret = zaclient.ZAZImgData2BMP(Image, "/mnt/sdcard/fp.bmp");
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
        byte[] Image = new byte[256 * 288];
        int[] len = new int[1];
        int timecount = 0;
        int bufferid = 1;
        int[] iMbAddress = new int[1];
        int[] iscore = new int[1];
        //指纹模板数据上传和下载相关项
//        byte[] upChar = new byte[512];
//        byte[] downChar = new byte[512];
//        int[] len1 = new int[1];
        while (iscancle) {
            ret = zaclient.ZAZGetImage(DEV_ADDR);
            if (ret == 0) {
                if (isshowbmp) {
                    ret = zaclient.ZAZUpImage(DEV_ADDR, Image, len);
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
                /*
                // 将/sdcard/12.txt文件中保存的指纹模板数据读取出来，并且写入缓冲区中，方便后续录入以及搜索等
                readFromFile(downChar);
                ret = zaclient.ZAZDownChar(DEV_ADDR, 1, downChar, 512);
                if(ret == 0) {
                    System.out.println("下载特征数据成功！   ");
                } else System.out.println("下载特征数据失败！   ");
                */
                ret = zaclient.ZAZRegModule(DEV_ADDR);
                if (ret != 0) {
                    msg += "合成失败\r\n";
                    m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, msg));
                    Sleep(500);
                    continue;
                }
                ret = zaclient.ZAZSearch(DEV_ADDR, 1, 0, 999, iMbAddress, iscore);
                if(ret == 0 && iscore[0] > 70) {
                    m_fEvent.sendMessage(m_fEvent.obtainMessage(msgshow, 0, 0, "该指纹已经录入，请重试"));
                    break;
                }
                /*
                //将注册时产生的指纹模板数据上传到文件/sdcard/12.txt中，方便后续做多平台使用
                ret = zaclient.ZAZUpChar(DEV_ADDR, 1, upChar, len1);
                if(ret == 0) {
                    System.out.println("上传特侦函数成功。。。。  " + len1[0]);
                    writeToFile(upChar, len1[0]);
                } else {
                    System.out.println("上传特侦函数失败 " + ret);}
                 */
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
        byte[] Image = new byte[256 * 288];
        int[] len = new int[1];
        int[] iMbAddress = new int[1];
        int[] iscore = new int[1];
        int timecount = 0;
        while (iscancle) {
            ret = zaclient.ZAZGetImage(DEV_ADDR);
            if (ret == 0) {
                if (isshowbmp) {
                    ret = zaclient.ZAZUpImage(DEV_ADDR, Image, len);
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
        return nSum;
    }


}

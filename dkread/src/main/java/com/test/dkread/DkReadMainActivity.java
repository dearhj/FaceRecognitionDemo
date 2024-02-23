package com.test.dkread;

import static com.dk.usbNfc.DeviceManager.ComByteManager.ISO14443_P4;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dk.log.DKLog;
import com.dk.log.DKLogCallback;
import com.dk.usbNfc.Card.CpuCard;
import com.dk.usbNfc.Card.DESFire;
import com.dk.usbNfc.Card.FeliCa;
import com.dk.usbNfc.Card.Iso15693Card;
import com.dk.usbNfc.Card.Mifare;
import com.dk.usbNfc.Card.Ntag21x;
import com.dk.usbNfc.Card.Topaz;
import com.dk.usbNfc.DKCloudID.IDCardData;
import com.dk.usbNfc.DeviceManager.DeviceManager;
import com.dk.usbNfc.DeviceManager.DeviceManagerCallback;
import com.dk.usbNfc.DeviceManager.UsbNfcDevice;
import com.dk.usbNfc.Exception.CardNoResponseException;
import com.dk.usbNfc.Exception.DeviceNoResponseException;
import com.dk.usbNfc.OTA.DialogUtils;
import com.dk.usbNfc.OTA.Ymodem;
import com.dk.usbNfc.Tool.StringTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class DkReadMainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private MyTTS myTTS;
    static long time_start = 0;
    static long time_end = 0;
    private TextView delayTextView = null;

    private static UsbNfcDevice usbNfcDevice = null;
    private EditText msgText = null;
    private ProgressDialog readWriteDialog = null;
    private AlertDialog.Builder alertDialog = null;

    private static String server_delay = "";
    private static int net_status = 1;

    private StringBuffer msgBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dkread_activity_main);
        //UI初始化
        initUI();

        //日志初始化
        DKLog.setLogCallback(logCallback);

        //语音初始化
        myTTS = new MyTTS(this);

        //usb_nfc设备初始化
        if (usbNfcDevice == null) {
            usbNfcDevice = new UsbNfcDevice(DkReadMainActivity.this);
            usbNfcDevice.setCallBack(deviceManagerCallback);
        }

        logViewln(null);

        //网络质量监控Demo，集成时可以去掉
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String lost = new String();
                    String delay = new String();

                    try {
                        Process p = Runtime.getRuntime().exec("ping -c 1 -w 10 " + "www.dkcloudid.cn");
                        net_status = p.waitFor();
                        //DKLog.d(TAG, "Process:" + net_status );

                        if (net_status == 0) {
                            BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            String str = new String();
                            while (true) {
                                try {
                                    if (!((str = buf.readLine()) != null)) break;
                                } catch (IOException e) {
                                    DKLog.e(TAG, e);
                                }

                                if (str.contains("avg")) {
                                    int i = str.indexOf("/", 20);
                                    int j = str.indexOf(".", i);

                                    delay = str.substring(i + 1, j);
                                    server_delay = delay;
                                }
                            }

                            //DKLog.d(TAG, "延迟:" + delay + "ms");
                        }
                        else {
                            //DKLog.d(TAG, "网络未连接！");
                        }
                    } catch (Exception e) {
                        DKLog.e(TAG, e);
                    }

                    showNETDelay();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();
    }

    //日志回调
    private DKLogCallback logCallback = new DKLogCallback() {
        @Override
        public void onReceiveLogI(String tag, String msg) {
            super.onReceiveLogI(tag, msg);
            Log.i(tag, msg);
            if (!flagLog) logViewln("[日志输出->I] " + msg);
        }

        @Override
        public void onReceiveLogD(String tag, String msg) {
            super.onReceiveLogD(tag, msg);
            Log.d(tag, msg);
            if (!flagLog) logViewln("[日志输出->D] " + msg);
        }

        @Override
        public void onReceiveLogE(String tag, String msg) {
            super.onReceiveLogE(tag, msg);
            Log.e(tag, msg);
            if (!flagLog) logViewln("[日志输出->E] " + msg);
        }
    };

    //设备操作类回调
    private DeviceManagerCallback deviceManagerCallback = new DeviceManagerCallback() {
        @Override
        public void onReceiveConnectionStatus(boolean blnIsConnection) {
            if (blnIsConnection) {
                Log.i(TAG,"USB连接成功！");
                logViewln(null);
                logViewln("USB连接成功！");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            logViewln("USB设备已连接！");
                            byte versionsByts = usbNfcDevice.getDeviceVersions();
                            logViewln(String.format("设备版本：%02x", versionsByts));

                            try {
                                usbNfcDevice.closeRf();
                            } catch (DeviceNoResponseException e) {
                                e.printStackTrace();
                            }
                        } catch (DeviceNoResponseException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            else {
                Log.i(TAG,"USB连接断开！");
                logViewln(null);
                logViewln("USB连接断开！");
            }
        }

        @Override
        //寻到卡片回调
        public void onReceiveRfnSearchCard(boolean blnIsSus, int cardType, byte[] bytCardSn, byte[] bytCarATS) {
            super.onReceiveRfnSearchCard(blnIsSus, cardType, bytCardSn, bytCarATS);
            if (!blnIsSus || cardType == UsbNfcDevice.CARD_TYPE_NO_DEFINE) {
                return;
            }

            Log.d(TAG, "Activity接收到激活卡片回调：UID->" + StringTool.byteHexToSting(bytCardSn) + " ATS->" + StringTool.byteHexToSting(bytCarATS));

            final int cardTypeTemp = cardType;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean isReadWriteCardSuc;
                    try {
                        isReadWriteCardSuc = readWriteCardDemo(cardTypeTemp);

                        //打开蜂鸣器提示读卡完成
                        if (isReadWriteCardSuc) {
                            usbNfcDevice.openBeep(50, 50, 1);  //读写卡成功快响1声
                        }
                        else {
                            usbNfcDevice.openBeep(200, 200, 1); //读写卡失败慢响1声
                            //读卡失败，关闭一次天线让读卡器自动进行重读
                            usbNfcDevice.closeRf();
                        }
                    } catch (DeviceNoResponseException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        //身份证开始请求云解析回调
        @Override
        public void onReceiveSamVIdStart(byte[] initData) {
            super.onReceiveSamVIdStart(initData);

            Log.d(TAG, "开始解析");
            logViewln(null);
            logViewln("正在读卡，请勿移动身份证!");
            myTTS.speak("正在读卡，请勿移动身份证");

            time_start = System.currentTimeMillis();
        }

        //身份证云解析进度回调
        @Override
        public void onReceiveSamVIdSchedule(int rate) {
            super.onReceiveSamVIdSchedule(rate);
            showReadWriteDialog("正在读取身份证信息,请不要移动身份证", rate);
            if (rate == 100) {
                time_end = System.currentTimeMillis();

                /**
                 * 这里已经完成读卡，可以拿开身份证了，在此提示用户读取成功或者打开蜂鸣器提示可以拿开身份证了
                 */
                myTTS.speak("读取成功");
            }
        }

        //身份证云解析异常回调
        @Override
        public void onReceiveSamVIdException(String msg) {
            super.onReceiveSamVIdException(msg);

            //显示错误信息
            logViewln(msg);

            //读卡结束关闭进度条显示
            hidDialog();

            //重复读
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        usbNfcDevice.closeRf();
//                    } catch (DeviceNoResponseException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
        }

        //身份证云解析明文结果回调
        @Override
        public void onReceiveIDCardData(IDCardData idCardData) {
            super.onReceiveIDCardData(idCardData);

            //显示身份证数据
            showIDCardData(idCardData);

            //重复读
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        usbNfcDevice.closeRf();
//                    } catch (DeviceNoResponseException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
        }
    };

    //读写卡Demo
    private synchronized boolean readWriteCardDemo(int cardType) {
        System.out.println("USBDK  寻找到卡  类型是："  + cardType);
        switch (cardType) {
            case DeviceManager.CARD_TYPE_ISO4443_A:   //寻到A CPU卡
                final CpuCard cpuCard = (CpuCard) usbNfcDevice.getCard();
                System.out.println("USBDK  寻找到A卡  " + (cpuCard != null));
                if (cpuCard != null) {
                    msgBuffer.delete(0, msgBuffer.length());
                    System.out.println("USBDK  寻到CPU卡->UID:" + cpuCard.uidToString());
                    logViewln("寻到CPU卡->UID:" + cpuCard.uidToString() + "");
                    try{
                        //选择深圳通主文件
                        byte[] bytApduRtnData = cpuCard.transceive(SZTCard.getSelectMainFileCmdByte());
                        if (bytApduRtnData.length <= 2) {
                            System.out.println("USBDK  不是深圳通卡，当成银行卡处理！" + bytApduRtnData.length);
                            Log.d(TAG, "不是深圳通卡，当成银行卡处理！");
                            //选择储蓄卡交易文件
                            String cpuCardType;
                            bytApduRtnData = cpuCard.transceive(FinancialCard.getSelectDepositCardPayFileCmdBytes());
                            if (bytApduRtnData.length <= 2) {
                                System.out.println("USBDK  不是储蓄卡，当成借记卡处理！！" + bytApduRtnData.length);
                                Log.d(TAG, "不是储蓄卡，当成借记卡处理！");
                                //选择借记卡交易文件
                                bytApduRtnData = cpuCard.transceive(FinancialCard.getSelectDebitCardPayFileCmdBytes());
                                if (bytApduRtnData.length <= 2) {
                                    System.out.println("USBDK  未知CPU卡！" + bytApduRtnData.length);
                                    logViewln("未知CPU卡！");
                                    return false;
                                }
                                else {
                                    cpuCardType = "储蓄卡";
                                    System.out.println("USBDK  储蓄卡！");
                                }
                            }
                            else {
                                System.out.println("USBDK  借记卡！");
                                cpuCardType = "借记卡";
                            }

                            bytApduRtnData = cpuCard.transceive(FinancialCard.getCardNumberCmdBytes());
                            //提取银行卡卡号
                            String cardNumberString = FinancialCard.extractCardNumberFromeRturnBytes(bytApduRtnData);
                            if (cardNumberString == null) {
                                logViewln("未知CPU卡！");
                                System.out.println("USBDK  未知CPU卡！！   提取银行卡卡号失败");
                                return false;
                            }
                            System.out.println("USBDK  储蓄卡卡号：" + cardNumberString);
                            logViewln("储蓄卡卡号：" + cardNumberString);


                            //读交易记录
                            Log.d(TAG, "USBDK 发送APDU指令-读10条交易记录");
                            for (int i = 1; i <= 10; i++) {
                                bytApduRtnData = cpuCard.transceive(FinancialCard.getTradingRecordCmdBytes((byte) i));
                                logViewln(FinancialCard.extractTradingRecordFromeRturnBytes(bytApduRtnData));

                            }
                        }
                        else {  //深圳通处理流程
                            bytApduRtnData = cpuCard.transceive(SZTCard.getBalanceCmdByte());
                            if (SZTCard.getBalance(bytApduRtnData) == null) {
                                logViewln("未知CPU卡！");
                                Log.d(TAG, "USBDK 深圳通处理流程  未知CPU卡！");
                                return false;
                            }
                            else {
                                logViewln("深圳通余额：" + SZTCard.getBalance(bytApduRtnData));
                                Log.d(TAG, "USBDK 深圳通处理流程 余额：" + SZTCard.getBalance(bytApduRtnData));
                                //读交易记录
                                Log.d(TAG, "USBDK 深圳通处理流程 发送APDU指令-读10条交易记录");
                                for (int i = 1; i <= 10; i++) {
                                    bytApduRtnData = cpuCard.transceive(SZTCard.getTradeCmdByte((byte) i));
                                    logViewln(SZTCard.getTrade(bytApduRtnData));
                                }
                            }
                        }
                    } catch (CardNoResponseException e) {
                        e.printStackTrace();
                        System.out.println("USBDK  崩溃处理流程！ ");
                        return false;
                    }
                }
                break;
            case DeviceManager.CARD_TYPE_FELICA:  //寻到FeliCa
                FeliCa feliCa = (FeliCa) usbNfcDevice.getCard();
                System.out.println("USBDK  寻到FeliCa  ！ " + (feliCa != null));
                if (feliCa != null) {
                    msgBuffer.delete(0, msgBuffer.length());
                    System.out.println("USBDK  寻到feliCa ->UID:" + feliCa.uidToString());
                    logViewln("寻到feliCa ->UID:" + feliCa.uidToString());
                }
                break;
            case DeviceManager.CARD_TYPE_ULTRALIGHT: //寻到Ultralight卡
                String writeText = System.currentTimeMillis() + "专业非接触式智能卡读写器方案商！";
                System.out.println("USBDK  寻到Ultralight卡1" );
                if (msgText.getText().toString().length() > 0) {
                    writeText = msgText.getText().toString();
                }

                msgBuffer.delete(0, msgBuffer.length());

                final Ntag21x ntag21x = (Ntag21x) usbNfcDevice.getCard();
                System.out.println("USBDK  寻到Ultralight卡2"   + (ntag21x != null));
                if (ntag21x != null) {
                    msgBuffer.delete(0, msgBuffer.length());
                    System.out.println("USBDK  寻到Ultralight卡 ->UID:" + ntag21x.uidToString());
                    logViewln("寻到Ultralight卡 ->UID:" + ntag21x.uidToString());
                    try {
                        logViewln("准备进行写入文本： hello world!");
                        boolean flag = ntag21x.NdefTextWrite("hello world!");
                        if (flag) logViewln("文本写入成功！");
                        else logViewln("文本写入失败！");
                        logViewln("准备读取文本");
                        String read = ntag21x.NdefTextRead();
                        logViewln("读取到的内容： " + read);
                    } catch (CardNoResponseException e) {
                        System.out.println("USBDK,   写或读崩溃了。");
                        e.printStackTrace();
                    }
                }
                break;
                //注意此处，
//            case DeviceManager.CARD_TYPE_MIFARE:   //寻到Mifare卡
//                final Mifare mifare = (Mifare) usbNfcDevice.getCard();
//                System.out.println("USBDK  寻到Mifare卡" + (mifare != null));
//                if (mifare != null) {
//                    msgBuffer.delete(0, msgBuffer.length());
//                    System.out.println("USBDK  寻到Mifare卡->UID:" + mifare.uidToString());
//                    logViewln("寻到Mifare卡->UID:" + mifare.uidToString());
//                }
//                break;
            case DeviceManager.CARD_TYPE_MIFARE:   //寻到Mifare卡
                final Mifare mifare = (Mifare) usbNfcDevice.getCard();
                if (mifare != null) {
                    logViewln("寻到Mifare卡->UID:" + mifare.uidToString());
                    logViewln("开始验证第1块密码");
                    byte[] key = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
                    try {
                        boolean anth = mifare.authenticate((byte) 1, Mifare.MIFARE_KEY_TYPE_A, key);
                        if (anth) {
                            logViewln("验证密码成功");
                            logViewln("写00112233445566778899001122334455到块1");
                            boolean isSuc = mifare.write((byte)1, new byte[]{0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55});
                            if (isSuc) {
                                logViewln("写成功！");
                                logViewln("读块1数据");
                                byte[] readDataBytes = mifare.read((byte) 1);
                                logViewln("块1数据:" + StringTool.byteHexToSting(readDataBytes));
                            } else {
                                logViewln("写失败！");
                                return false;
                            }
                        }
                        else {
                            logViewln("验证密码失败");
                            return false;
                        }
                    } catch (CardNoResponseException e) {
                        System.out.println("USBDK   这里崩溃了");
                        e.printStackTrace();
                        return false;
                    }
                }
                break;
            case DeviceManager.CARD_TYPE_ISO15693: //寻到15693卡
                final Iso15693Card iso15693Card = (Iso15693Card) usbNfcDevice.getCard();
                System.out.println("USBDK  寻到15693卡" + (iso15693Card != null));
                if (iso15693Card != null) {
                    msgBuffer.delete(0, msgBuffer.length());
                    System.out.println("USBDK  寻到15693卡->UID:" + iso15693Card.uidToString());
                    logViewln("寻到15693卡->UID:" + iso15693Card.uidToString());
                    logViewln("读块0数据\r\n");

                    try {
                        //读写单个块Demo
                        logViewln("写数据01020304到块4");
                        boolean isSuc = iso15693Card.write((byte)4, new byte[] {0x01, 0x02, 0x03, 0x04});
                        if (isSuc) {
                            System.out.println("USBDK  写数据01020304到块4成功");
                            logViewln("写数据成功！");
                        }
                        else {
                            System.out.println("USBDK  写数据01020304到块4失败");
                            logViewln("写数据失败！");
                        }
                        logViewln("读块4数据");
                        byte[] bytes = iso15693Card.read((byte) 4);
                        System.out.println("USBDK  读块4数据 " + StringTool.byteHexToSting(bytes));
                        logViewln("块4数据：" + StringTool.byteHexToSting(bytes));


                        //读写多个块Demo
                        logViewln("写数据0102030405060708到块5、6");
                        isSuc = iso15693Card.writeMultiple((byte)5, (byte)2, new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08});
                        if (isSuc) {
                            logViewln("写数据成功！");
                        }
                        else {
                            logViewln("写数据失败！");
                        }
                        logViewln("读块5、6数据");

                        bytes = iso15693Card.ReadMultiple((byte) 5, (byte)2);
                        logViewln("块5、6数据：" + StringTool.byteHexToSting(bytes));
                    } catch (CardNoResponseException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                break;
            case DeviceManager.CARD_TYPE_DESFire:   //寻到A CPU卡
                final DESFire desFire = (DESFire) usbNfcDevice.getCard();
                System.out.println(" USBDK   寻到A CPU卡 CARD_TYPE_DESFire" + (desFire != null));
                if (desFire != null) {
                    msgBuffer.delete(0, msgBuffer.length());
                    System.out.println(" USBDK   寻到DESFire卡->UID:" + desFire.uidToString());
                    logViewln("寻到DESFire卡->UID:" + desFire.uidToString() + "");
                    try {
                        //发送获取随机数APDU命令：0084000008
                        byte[] cmd = {0x00, (byte)0x84, 0x00, 0x00, 0x08};
                        System.out.println(" USBDK   发送获取随机数APDU命令寻到DESFire卡：0084000008");
                        logViewln("发送获取随机数APDU命令：0084000008");
                        byte[] rsp = desFire.transceive(cmd);
                        logViewln("返回：" + StringTool.byteHexToSting(rsp));
                    } catch (CardNoResponseException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case DeviceManager.CARD_TYPE_T1T:
                final Topaz topaz = (Topaz) usbNfcDevice.getCard();
                System.out.println(" USBDK   寻到T1T卡" + (topaz != null));
                if (topaz != null) {
                    logViewln("寻到T1T卡->UID:" + topaz.uidToString() + "");
                }
                break;
        }
        return true;
    }

    //固件升级
    private void startOTA() {
        msgBuffer.delete(0, msgBuffer.length());
        logViewln("正在升级固件...");


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.select_file(DkReadMainActivity.this, new DialogUtils.DialogSelection() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        if (files.length == 1) {
                            final String filePath = files[0];

                            /**
                             * 升级说明：升级需要将DK26MEEncrypt.bin文件放到手机根目录，
                             * 第一次点击升级按键APP会重启，如果APP退出去后没有自动重启，需要手动重新打开APP
                             * APP重启后如果显示的固件版本是03，这时模块处于升级模式，需要再次点击升级按键完成升级
                             * APP显示升级完成后，需要等待几秒钟模块灯灭掉后即升级完成
                             */
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Ymodem ymodem = new Ymodem(usbNfcDevice);

                                    final File file = new File(filePath);
                                    if (!file.exists()) {
                                        msgBuffer.delete(0, msgBuffer.length());
                                        logViewln("升级文件未找到！");
                                        return;
                                    }

                                    boolean isSuc = ymodem.YmodemUploadFile(file, new Ymodem.onReceiveScheduleListener() {
                                        @Override
                                        public void onReceiveSchedule(int rate) {
                                            showReadWriteDialog("正在升级", rate);
                                        }
                                    });

                                    if (isSuc) {
                                        msgBuffer.delete(0, msgBuffer.length());
                                        logViewln("升级成功！");
                                    }
                                    else {
                                        msgBuffer.delete(0, msgBuffer.length());
                                        logViewln("升级失败！");
                                    }
                                }
                            }).start();
                        }
                    }
                });
            }
        });
    }

    private static boolean flagLog = false;

    //UI初始化
    private void initUI() {
        flagLog = false;
        msgBuffer = new StringBuffer();

        msgText = (EditText)findViewById(R.id.msgText);
        Button clearButton = (Button) findViewById(R.id.clearButton);
        Button openAutoSearchCard = (Button)findViewById(R.id.openAutoSearchCard);
        Button closeAutoSearchCard = (Button)findViewById(R.id.closeAutoSearchCard);
        Button otaButton = (Button)findViewById(R.id.ota_button);
        Button closeButton = (Button)findViewById(R.id.closeLog);
        delayTextView = findViewById(R.id.delayTextView);

        msgText.setKeyListener(null);
        msgText.setTextIsSelectable(true);

        readWriteDialog = new ProgressDialog(DkReadMainActivity.this);
        readWriteDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // 设置ProgressDialog 标题
        readWriteDialog.setTitle("请稍等");
        // 设置ProgressDialog 提示信息
        readWriteDialog.setMessage("正在读写数据……");
        readWriteDialog.setMax(100);

        //清空显示按键
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logViewln(null);
            }
        });

        closeButton.setOnClickListener(view -> {
            if(flagLog) {
                flagLog = false;
                closeButton.setText("关闭日志输出");
            } else {
                flagLog = true;
                closeButton.setText("打开日志输出");
            }
        });

        //固件升级按键
        otaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usbNfcDevice.usbHidManager.isClose()) {
                    msgText.setText("USB设备未连接！");
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //查询固件版本是否处于OTA模式下
                        try {
                            int version = usbNfcDevice.getDeviceVersions();
                            if ( version >= 0x40 ) {
                                //进入升级模式
                                try {
                                    usbNfcDevice.OTACmd(new byte[]{0x00}, 1000);
                                } catch (DeviceNoResponseException e) {
                                    //e.printStackTrace();
                                }

                                Log.d(TAG, "正在进入升级模式");
                            }

                            startOTA();
                        } catch (DeviceNoResponseException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        //打开自动寻卡开关
        openAutoSearchCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usbNfcDevice.usbHidManager.isClose()) {
                    msgText.setText("USB设备未连接！");
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //打开/关闭自动寻卡，100ms间隔，寻M1/UL卡
                            boolean isSuc = usbNfcDevice.startAutoSearchCard((byte) 20, ISO14443_P4);
                            if (isSuc) {
                                logViewln(null);
                                logViewln("自动寻卡已打开！");
                            }
                            else {
                                logViewln(null);
                                logViewln("自动寻卡已关闭！");
                            }
                        } catch (DeviceNoResponseException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        //关闭自动寻卡开关
        closeAutoSearchCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usbNfcDevice.usbHidManager.isClose()) {
                    msgText.setText("USB设备未连接！");
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //打开/关闭自动寻卡，100ms间隔，寻M1/UL卡
                            boolean isSuc = usbNfcDevice.stoptAutoSearchCard();
                            if (isSuc) {
                                logViewln(null);
                                logViewln("自动寻卡已关闭！");
                            }
                            else {
                                logViewln(null);
                                logViewln("自动寻卡已打开！");
                            }
                        } catch (DeviceNoResponseException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    //显示身份证数据
    private void showIDCardData(IDCardData idCardData) {
        final IDCardData theIDCardData = idCardData;

        //显示照片和指纹
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgText.setText("解析成功，读卡用时:" + (time_end - time_start) + "ms\r\n" + theIDCardData.toString());

                SpannableString ss = new SpannableString(msgText.getText().toString()+"[smile]");
                //得到要显示图片的资源
                Drawable d = new BitmapDrawable(theIDCardData.PhotoBmp);//Drawable.createFromPath("mnt/sdcard/photo.bmp");
                if (d != null) {
                    //设置高度
                    d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                    //跨度底部应与周围文本的基线对齐
                    ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
                    //附加图片
                    ss.setSpan(span, msgText.getText().length(),msgText.getText().length()+"[smile]".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    msgText.setText(ss);
                    //msgTextView.setText("\r\n");
                    //Log.d(TAG, idCardData.PhotoBmp);
                }
            }
        });
    }

    //进度条显示
    private void showReadWriteDialog(String msg, int rate) {
        final int theRate = rate;
        final String theMsg = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((theRate == 0) || (theRate == 100)) {
                    readWriteDialog.dismiss();
                    readWriteDialog.setProgress(0);
                } else {
                    readWriteDialog.setMessage(theMsg);
                    readWriteDialog.setProgress(theRate);
                    if (!readWriteDialog.isShowing()) {
                        readWriteDialog.show();
                    }
                }
            }
        });
    }

    //隐藏进度条
    private void hidDialog() {
        //关闭进度条显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (readWriteDialog.isShowing()) {
                    readWriteDialog.dismiss();
                }
                readWriteDialog.setProgress(0);
            }
        });
    }

    //显示网络延迟
    private void showNETDelay() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ( (net_status == 0) && (server_delay != null) && (server_delay.length() > 0) ) {
                    int delay = Integer.parseInt(server_delay);
                    String pj = "优";
                    if (delay < 30) {
                        pj = "优";
                        delayTextView.setTextColor(0xF000ff00);
                    } else if (delay < 50) {
                        pj = "良";
                        delayTextView.setTextColor(0xF0EEC900);
                    } else if (delay < 100) {
                        pj = "差";
                        delayTextView.setTextColor(0xF0FF0000);
                    } else {
                        pj = "极差";
                        delayTextView.setTextColor(0xF0B22222);
                    }
                    delayTextView.setText("网络延迟：" + server_delay + "ms " + " 等级：" + pj);
                }
                else {
                    delayTextView.setTextColor(0xF0B22222);
                    delayTextView.setText("网络未连接！");
                }
            }
        });
    }

    private void logViewln(String string) {
        final String msg = string;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (msg == null) {
                    msgText.setText("");
                    return;
                }

//                if (msgText.length() > 1000) {
//                    msgText.setText("");
//                }
                msgText.append(msg + "\r\n");
                int offset = msgText.getLineCount() * msgText.getLineHeight();
                if(offset > msgText.getHeight()){
                    msgText.scrollTo(0,offset - msgText.getHeight());
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        if (readWriteDialog != null) {
            readWriteDialog.dismiss();
        }

        //销毁
        usbNfcDevice.destroy();
        usbNfcDevice = null;
    }
}

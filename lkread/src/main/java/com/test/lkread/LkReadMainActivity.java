package com.test.lkread;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

import jRF.FM1208;
import lc.comproCall;

public class LkReadMainActivity extends AppCompatActivity {

    public static final byte NFC_TYPE_MFCLS_1K		=0x00;
    public static final byte NFC_TYPE_MFCLS_4K		=0x01;
    public static final byte NFC_TYPE_MFPLUS_2K		=0x02;
    public static final byte NFC_TYPE_MFPLUS_4K		=0x03;
    public static final byte NFC_TYPE_ULT			=0x04;
    public static final byte NFC_TYPE_ULTC			=0x05;
    public static final byte NFC_TYPE_NTAG203		=0x06;
    public static final byte NFC_TYPE_NTAG210		=0x07;
    public static final byte NFC_TYPE_NTAG212		=0x08;
    public static final byte NFC_TYPE_NTAG213		=0x09;
    public static final byte NFC_TYPE_NTAG215		=0x0A;
    public static final byte NFC_TYPE_NTAG216		=0x0B;
    public static final byte NFC_TYPE_ICODE_SLI		=0x0C;
    public static final byte NFC_TYPE_ICODE_SLI_S	=0x0D;
    public static final byte NFC_TYPE_ICODE_SLI_L	=0x0E;
    public static final byte NFC_TYPE_ICODE_DNA		=0x0F;
    public static final byte NFC_TYPE_MF_MINI		=0x10;
    public static final byte NFC_TYPE_MF_DESFIRE		=0x11;
    public static final byte NFC_TYPE_14443_4		=(byte)0xF0;
    public static final byte NFC_TYPE_UNKONWN		=(byte)0xFF;

    public static final char UI_UPDATE_BTN_AUTO = 1;
    public static final char UI_UPDATE_BTN_MANUAL_DISABLE = 2;
    public static final char UI_UPDATE_BTN_MANUAL_ENABLE = 3;
    public static final char UI_UPDATE_MSG_TEXT_APPEND = 4;
    public static final char UI_UPDATE_MSG_TEXT_SET	 = 5;

    public static final char PT_USB = 2;
    public static final char PT_SERIAL = 1;
    public static final char DEV_C1 = 0;
    public static final char DEV_RF = 1;
    public static final char CARD_AUTO_IDENTIFY = 0x00;
    public static final char CARD_24CXX = 0x01;
    public static final char CARD_4442 = 0x02;
    public static final char CARD_4428 = 0x03;
    public static final char CARD_102 = 0x04;
    public static final char CARD_24C64 = 0x05;
    public static final char CARD_TC_CPU = 0x15;
    public static final char CARD_M1 = 0x20;
    public static final char CARD_ULTRILIGHT = 0x21;
    public static final char CARD_DESFIRE = 0x22;
    public static final char CARD_CTL_CPU = 0x23;
    public static final char CARD_MF_PLUS = 0x24;
    public static final char CARD_ICODE = 0x25;
    public static final char CARD_TYPEB = 0x26;
    public static final char CARD_FM1208 = 0x27;
    public static final char CARD_NDEF = 0x80;
    public static final char CARD_SSC = 0x81;
    public static final char CARD_PBOC_PAN = 0x82;
    public static final char CARD_PSAM1 = 0xf1;
    public static final char CARD_PSAM2 = 0xf2;
    public static final char FUNC_KB = 0xE1;

    public static final char INTERFACE_CONTACT = 0;
    public static final char INTERFACE_CONTACTLESS = 100;

    private RadioGroup grp_serialType;
    private RadioGroup grp_cardType_contactLess;
    private RadioGroup grp_cardType_contact;
    private RadioGroup grp_cardType_composite;
    private RadioGroup grp_compositeCard_interface;
    private Button m_btn, m_btn_autoTest,m_btn_clean;
    private EditText m_text;
    private EditText m_text_devPath;
    private EditText m_text_baud;
    private EditText m_text_pid;
    private EditText m_text_vid;
    RelativeLayout layout;
    private TextView m_dynamicTV = null;
    RelativeLayout.LayoutParams layoutPara;
    private PendingIntent pendingIntent;
    private comproCall call_comPro;
    private FM1208 call_fm1208;
    char[] pCharSingle = new char[255];
    public char gl_autoRun = 0, gl_autoRunning = 0;
    public char gl_singleTestInAutoRunning = 0;
    AutoTestThread mAutoThread= null;
    public static  Handler m_Handler;
    public Message message = new Message();
    public String gl_msg, gl_autoBtnText;
    public boolean fUI_updating  = false;
    public boolean fUI_TestItemUpdating = false;

    int struct_deviceType = DEV_RF;
    int struct_portType = PT_USB;
    int struct_cardType = CARD_AUTO_IDENTIFY;
    char struct_cardInterface = INTERFACE_CONTACT;
    private int gl_loopTestTimes = 0;//Loop times
    private int gl_loopTestSucTimes = 0;//Loop failed times
    private LocalHandler mHandler = null;// = new LocalHandler(this);



    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_test_main);

        m_btn = (Button) findViewById(R.id.button1);
        m_btn_autoTest = (Button)findViewById(R.id.btn_autoTest);
        m_btn_clean = (Button)findViewById(R.id.btn_clean);
        m_text = (EditText)findViewById(R.id.editText1);
        m_text_devPath = (EditText)findViewById(R.id.edt_devPath);
        m_text_baud = (EditText)findViewById(R.id.edt_baud);
        grp_serialType = (RadioGroup)findViewById(R.id.radioGroup_serialType);
        grp_cardType_contactLess = (RadioGroup)findViewById(R.id.radioGroup_cardTypeContactLess);
        grp_cardType_contact = (RadioGroup)findViewById(R.id.radioGroup_cardTypeContact);
        grp_cardType_composite = (RadioGroup)findViewById(R.id.radioGroup_cardTypeComposite);
        grp_compositeCard_interface = (RadioGroup)findViewById(R.id.radioGroup_cardTypeComposite_interface);
        layout = (RelativeLayout) findViewById(R.id.relativeLayout);
        m_dynamicTV = new TextView(this);
        layoutPara = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(m_dynamicTV);

        call_comPro = new comproCall();
        call_fm1208 = new FM1208();
        mHandler = new LocalHandler(this);

        UpdateUIForPortType(false);

        m_btn.setOnClickListener(arg0 -> {
            DoOneTest();
        });

        m_btn_autoTest.setOnClickListener(arg0 -> {
            if(0 == gl_autoRun)
            {
                gl_autoRunning = 1;
                gl_loopTestTimes = 0;
                gl_loopTestSucTimes = 0;

                mAutoThread = new AutoTestThread();
                mAutoThread.start();

            }
            else
            {
                gl_autoRunning = 0;
            }
        });

        m_btn_clean.setOnClickListener(arg0 -> {
            ClearMsg();
        });

        grp_serialType.setOnCheckedChangeListener((arg0, arg1) -> {
            int radioButtonId = arg0.getCheckedRadioButtonId();

            if(radioButtonId == R.id.radioSerail)
            {
                struct_portType = PT_SERIAL;
                UpdateUIForPortType(true);
            }
            else if(radioButtonId == R.id.radioUSB)
            {
                struct_portType = PT_USB;
                UpdateUIForPortType(false);
            }

        });


        grp_cardType_contactLess.setOnCheckedChangeListener((group, checkedId) -> {
            if(!fUI_TestItemUpdating) {
                fUI_TestItemUpdating = true;
                grp_cardType_contact.clearCheck();
                grp_cardType_composite.clearCheck();
                UpdateTestItem(checkedId);
                fUI_TestItemUpdating = false;
            }
        });

        grp_cardType_contact.setOnCheckedChangeListener((group, checkedId) -> {
            if(!fUI_TestItemUpdating) {
                fUI_TestItemUpdating = true;
                grp_cardType_contactLess.clearCheck();
                grp_cardType_composite.clearCheck();
                UpdateTestItem(checkedId);
                fUI_TestItemUpdating = false;
            }
        });

        grp_cardType_composite.setOnCheckedChangeListener((group, checkedId) -> {
            if(!fUI_TestItemUpdating) {
                fUI_TestItemUpdating = true;
                grp_cardType_contactLess.clearCheck();
                grp_cardType_contact.clearCheck();
                UpdateTestItem(checkedId);
                fUI_TestItemUpdating = false;
            }
        });

        grp_compositeCard_interface.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.radio_interface_contact)
                struct_cardInterface  = INTERFACE_CONTACT;
            else if(checkedId == R.id.radio_interface_contactless)
                struct_cardInterface = INTERFACE_CONTACTLESS;
        });

    }

    private void UpdateTestItem(int checkedId)
    {
        if(checkedId == R.id.radio_card_identify)
            struct_cardType = CARD_AUTO_IDENTIFY;
        else if(checkedId == R.id.radio_card_M1)
            struct_cardType = CARD_M1;
        else if(checkedId == R.id.radio_card_UltraLight)
            struct_cardType = CARD_ULTRILIGHT;
        else if(checkedId == R.id.radio_card_NDEF)
            struct_cardType = CARD_NDEF;
//				else if(checkedId == R.id.radio_card_Desfire)
//					struct_cardType = CARD_DESFIRE;
        else if(checkedId == R.id.radio_card_CTL_CPU)
            struct_cardType = CARD_CTL_CPU;
        else if(checkedId == R.id.radio_card_FM1208)
            struct_cardType = CARD_FM1208;
            //else if(checkedId == R.id.radio_card_MF_PLUS)
            //	struct_cardType = CARD_MF_PLUS;
        else if(checkedId == R.id.radio_card_ICODE)
            struct_cardType = CARD_ICODE;
        else if(checkedId == R.id.radio_card_TypeB)
            struct_cardType = CARD_TYPEB;
        else if(checkedId == R.id.radio_card_CT_24CXX)
            struct_cardType = CARD_24CXX;
        else if(checkedId == R.id.radio_card_CT_4442)
            struct_cardType = CARD_4442;
        else if(checkedId == R.id.radio_card_CT_4428)
            struct_cardType = CARD_4428;
        else if(checkedId == R.id.radio_card_CT_24C64)
            struct_cardType = CARD_24C64;
//				else if(checkedId == R.id.radio_card_102)
//					struct_cardType = CARD_102;
        else if(checkedId == R.id.radio_card_CT_CPU)
            struct_cardType = CARD_TC_CPU;
        else if(checkedId == R.id.radio_card_PSAM)
            struct_cardType = CARD_PSAM1;
        else if(checkedId == R.id.radio_card_PSAM2)
            struct_cardType = CARD_PSAM2;
//				else if(checkedId == R.id.radio_KeyBoard)
//					struct_cardType = FUNC_KB;
        else if(checkedId == R.id.radio_card_pbocPAN)
            struct_cardType = CARD_PBOC_PAN;
        else if(checkedId == R.id.radio_card_SSC)
            struct_cardType = CARD_SSC;
    }

    private static class LocalHandler extends Handler{
        private final WeakReference mActivity;

        public LocalHandler (LkReadMainActivity activity){
            mActivity = new WeakReference<LkReadMainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg){
            LkReadMainActivity localActivity = (LkReadMainActivity) mActivity.get();
            if(localActivity != null){
                int textSize;
                String strText;

                if(localActivity.fUI_updating == true)
                    return;

                localActivity.fUI_updating = true;

                switch (msg.what)
                {
                    case UI_UPDATE_MSG_TEXT_APPEND:
                    case UI_UPDATE_MSG_TEXT_SET:
                        strText = (String)msg.obj;
                        localActivity.m_text.setText(strText);
                        textSize = localActivity.m_text.getText().length();
                        localActivity.m_text.setSelection(textSize);
                        break;
                    case UI_UPDATE_BTN_AUTO:
                        localActivity.m_btn_autoTest.setText(localActivity.gl_autoBtnText);
                        break;
                    case UI_UPDATE_BTN_MANUAL_DISABLE:
                        localActivity.m_btn.setEnabled(false);
                        break;
                    case UI_UPDATE_BTN_MANUAL_ENABLE:
                        localActivity.m_btn.setEnabled(true);
                        break;
                    default :
                        break;
                }

                localActivity.fUI_updating = false;
            }
        }
    }

    private class AutoTestThread extends Thread {
        int hdev=1;
        int okCnt =0;

        public void run(){
            try {

                gl_autoRun = 1;

                SendUIMessage(UI_UPDATE_BTN_MANUAL_DISABLE, "");
                SendUIMessage(UI_UPDATE_BTN_AUTO, "stop");


                while(gl_autoRunning == 1)
                {

                    sleep(500);//50

                    if(1 == gl_singleTestInAutoRunning)
                        continue;

                    gl_singleTestInAutoRunning = 1;

                    gl_loopTestTimes ++;

                    if(0 == DoOneTest())
                        gl_loopTestSucTimes ++;

                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Total Times: "+ gl_loopTestTimes + ",Error Times: "+ Integer.toString(gl_loopTestTimes-gl_loopTestSucTimes));
                    Log.d("Reader", "Test count: "+ gl_loopTestTimes);

                    gl_singleTestInAutoRunning = 0;

                }

                gl_autoRun = 0;
                SendUIMessage(UI_UPDATE_BTN_AUTO, "AutoTest");
                SendUIMessage(UI_UPDATE_BTN_MANUAL_ENABLE, "");


            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void  UpdateUIForPortType(boolean benable)
    {
        m_text_devPath.setFocusable(benable);
        m_text_baud.setFocusable(benable);
        m_text_devPath.setEnabled(benable);
        m_text_baud.setEnabled(benable);
        m_text_devPath.setFocusableInTouchMode(benable);
        m_text_baud.setFocusableInTouchMode(benable);

    }

    private void SendUIMessage(char toWhat, String text) {
        runOnUiThread(() -> {
            switch(toWhat) {
                case UI_UPDATE_MSG_TEXT_APPEND:
                    gl_msg += text+"\n";
                    break;
                case UI_UPDATE_MSG_TEXT_SET:
                    gl_msg = text+"\n";
                    break;
                case UI_UPDATE_BTN_AUTO:
                    m_btn_autoTest.setText(text);
                    break;
            }
            m_text.setText(gl_msg);
        });
//        switch(toWhat)
//        {
//            case UI_UPDATE_MSG_TEXT_APPEND:
//                gl_msg += text+"\n";
//                break;
//            case UI_UPDATE_MSG_TEXT_SET:
//                gl_msg = text+"\n";
//                break;
//            case UI_UPDATE_BTN_AUTO:
//                gl_autoBtnText = text;
//                break;
//        }
//
//        Message msg = mHandler.obtainMessage();
//        msg.what = toWhat;
//        msg.obj = gl_msg;
//        mHandler.sendMessage(msg);

    }

    public void ClearMsg()
    {
        gl_msg = "";
        SendUIMessage(UI_UPDATE_MSG_TEXT_SET, "");
    }
    public int DoOneTest()
    {
        String devPath;
        int baud;
        int testItems = 0;
        int testRel = 0;

        ClearMsg();

        devPath = m_text_devPath.getText().toString();
        baud = Integer.parseInt(m_text_baud.getText().toString());

        if(struct_deviceType == DEV_RF)
        {
            if(struct_cardType == CARD_AUTO_IDENTIFY)
                testRel = TestIdentifyCard(struct_portType,devPath, baud);
            if(struct_cardType == CARD_M1)
                testRel= TestM1(struct_portType,devPath, baud);
            else if(struct_cardType == CARD_ULTRILIGHT )
                testRel = TestUltralight(struct_portType,devPath, baud);
            else if(struct_cardType == CARD_NDEF)
                testRel = TestNDEF(struct_portType,devPath, baud);
//			else if(struct_cardType == CARD_DESFIRE)
//				testRel = TestDesfire(struct_portType, devPath, baud);
            else if(struct_cardType == CARD_CTL_CPU)
                testRel = TestCTLcpu(struct_portType, devPath, baud);
            else if(struct_cardType == CARD_FM1208)
                testRel =(int) FM1208_Test(struct_portType, devPath, baud);
                //else if(struct_cardType == CARD_MF_PLUS)
                //	testRel = TestMFPlus(struct_portType, devPath, baud);
            else if(struct_cardType == CARD_ICODE)
                testRel = TestICODE(struct_portType, devPath, baud);
            else if(struct_cardType == CARD_TYPEB)
                testRel = TestTypeB(struct_portType, devPath, baud);
            else if(struct_cardType == CARD_4442)
                testRel = Test_4442(struct_portType, devPath, baud);
            else if(struct_cardType == CARD_4428)
                testRel = Test_4428(struct_portType, devPath, baud);
            else if(struct_cardType == CARD_24CXX)
                testRel = Test_24CXX(struct_portType, devPath, baud);
            else if(struct_cardType == CARD_24C64)
                testRel = Test_24C64(struct_portType, devPath, baud);
            else if(struct_cardType == CARD_PSAM1)
                testRel = Test_RF_PSAM1(struct_portType, devPath, baud);
            else if(struct_cardType == CARD_PSAM2)
                testRel = Test_RF_PSAM2(struct_portType, devPath, baud);
            else if(struct_cardType == CARD_TC_CPU)
                testRel = TestCTcpu(struct_portType, devPath, baud);
            else if(struct_cardType == CARD_SSC)
                testRel = TestSSC(struct_portType, devPath, baud);
            else if(struct_cardType == CARD_PBOC_PAN)
                testRel = Test_PBOC_PAN(struct_portType, devPath, baud);
        }
        return testRel;
    }

    /* Function: FlashLED
     * Description: Make LED light a flash (power on ->( delay) ->power off
     * parameters:
     * icDev: the handle of reader
     * delay: the time for delay while flash, MS
     * Return: none
     */
    private void FlashLED(int icDev, int delay)
    {
    }

    public int TestIdentifyCard(int portType, String path, int baudrate)
    {
        int result = 0, hdev=1;
        char[] pModVer = new char[512];
        byte[] pSnrM1 = new byte[255];
        byte[] snrSize = new byte[64];
        char[] pCharHex = new char[255];
        char[] pCharSingle = new char[255];
        byte[] pByteBuf = new byte[255];
        byte[] pByteStr = new byte[255];
        long sn;//Card SN in integer type
        long _sn;//Card SN in integer type (Reverse)
        char[] rv_hexCard = new char[128];//Card SN in HEX type(Reverse)
        char[] rv_strCard = new char[128];//Card SN in HEX string type (Reverse)
        long startTime = 0;
        long timePass = 0;
        int i;
        int nTest;
        int linkedReaderNum = 0;
        int[] hDevArr = new int[4];
        byte[] tagType = new byte[2];
        byte[] fSupport14443_4 = new byte[2];
        String strTagInfo;
        byte[] bRW_mode = new byte[2];
        byte[] buf_typefIDm = new byte[64];
        byte[] buf_typefPM = new byte[64];

        //连接端口
        do {
            if (portType == PT_USB) hdev = call_comPro.lc_init_ex(2, "".toCharArray(), 0);
            else hdev = call_comPro.lc_init_ex(1, path.toCharArray(), baudrate);

            if(hdev != -1) {
                hDevArr[linkedReaderNum] = hdev;
                linkedReaderNum++;
                if(portType == PT_SERIAL)break;
            }
        }while(hdev != -1);

        if(linkedReaderNum == 0)//NO Reader mounted
        {
            //Log.e("readerlog", "Link reader error");
            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_Link reader failed ");
            return result;
        }


        //连接端口成功
        for(nTest = 0; nTest < linkedReaderNum; nTest++)
        {
            hdev = hDevArr[nTest];

            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Operating Reader: " + String.valueOf(hdev) + "---------------" );

            startTime = System.currentTimeMillis();//Save the start time for test

            //获取一次硬件版本号
            //try to get module version
            result = call_comPro.lc_getver(hdev, pModVer);
            if(0 == result)
            {
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"-");
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Module Version: " + String.valueOf(pModVer));

                //Get device run mode
                result = comproCall.lc_getReaderMode(hdev, bRW_mode);
                if( result == 0)
                {
                    if(bRW_mode[0] == (byte)1) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "Run mode is read only, you'd better change to read&write !");
                    }else{
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Run mode is read&write");
                    }
                }

                call_comPro.lc_rfReset(hdev, 10);//RF Reset
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_rf reset");

                //获取卡号 UID
                result = call_comPro.lc_requestAndIdentifyTypeA(hdev, (byte)1, pSnrM1, snrSize, tagType, fSupport14443_4);
                if(0 == result)
                {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_card:ok ");


                    call_comPro.hex_asc(pSnrM1, pByteStr, snrSize[0]);
                    for(i=0;i<2*snrSize[0];i++)pCharHex[i] =(char) pByteStr[i];
                    sn = ((long)(pSnrM1[0] & 0xff) << 24) + ((long)(pSnrM1[1] & 0xff) << 16 ) + ((long)(pSnrM1[2]&0xff)<< 8  ) +(long)( pSnrM1[3]&0xff );
                    for(i=0; i< snrSize[0]; i++)rv_hexCard[i] = (char)pSnrM1[snrSize[0] -1 - i];
                    _sn = ((long)(rv_hexCard[0]&0xff)<<24) + ((long)(rv_hexCard[1]&0xff)<<16)+ ((long)(rv_hexCard[2]&0xff)<< 8) + (long)(rv_hexCard[3]&0xff);
                    for(i=0; i< snrSize[0];i++)pByteBuf[i] = (byte)rv_hexCard[i];
                    call_comPro.hex_asc(pByteBuf,pByteStr,  snrSize[0]);
                    for(i=0; i< 2*snrSize[0];i++)rv_strCard[i] = (char)pByteStr[i];
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.valueOf(pCharHex));
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.format("%010d",sn));
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.valueOf(rv_strCard));
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.format("%010d",_sn));

                    strTagInfo = "Chip Type: ";
                    switch(tagType[0])
                    {
                        case NFC_TYPE_MFCLS_1K: strTagInfo +=  "MF Class 1K";break;
                        case NFC_TYPE_MFCLS_4K: strTagInfo +=  "MF Class 4K";break;
                        case NFC_TYPE_MFPLUS_2K: strTagInfo +=  "MF Plus 2K";break;
                        case NFC_TYPE_MFPLUS_4K: strTagInfo +=  "MF Plus 4K";break;
                        case NFC_TYPE_ULT: strTagInfo +=  "MF Ultralight";break;
                        case NFC_TYPE_ULTC: strTagInfo +=  "MF Ultralight C";break;
                        case NFC_TYPE_NTAG203: strTagInfo +=  "NTAG 203";break;
                        case NFC_TYPE_NTAG210: strTagInfo +=  "NTAG 210";break;
                        case NFC_TYPE_NTAG212: strTagInfo +=  "NTAG 212";break;
                        case NFC_TYPE_NTAG213: strTagInfo +=  "NTAG 213";break;
                        case NFC_TYPE_NTAG215: strTagInfo +=  "NTAG 215";break;
                        case NFC_TYPE_NTAG216: strTagInfo +=  "NTAG 216";break;
                        case NFC_TYPE_MF_MINI: strTagInfo +=  "MF MINI";break;
                        case NFC_TYPE_MF_DESFIRE: strTagInfo +=  "MF Desfire";break;
                        case NFC_TYPE_14443_4: strTagInfo +=  "14443_4(CPU)";break;

                        default:strTagInfo +=  "Chip disIdentify";break;
                    }
                    if(fSupport14443_4[0] != 0) strTagInfo += ", Support 14443-4";

                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,strTagInfo);

                    timePass = System.currentTimeMillis() - startTime;
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Time: " + timePass+ " ms");

                    call_comPro.lc_beep(hdev, 1);//test

                }
                else
                {
                    //Dectect TypeB
                    //Dectect ISO15693
                    //Dectect Felica
                    result = comproCall.lc_findTypeF(hdev, (short)0xffff, buf_typefIDm, buf_typefPM);
                    if(0 == result)//Found typef
                    {
                        String strRel = "_found typeF card:ok: ";

                        call_comPro.hex_asc(buf_typefIDm, pByteStr, 8);
                        for(i=0;i<16;i++)pCharHex[i] =(char) pByteStr[i];
                        strRel += "ID:" +String.valueOf(pCharHex);
                        call_comPro.hex_asc(buf_typefPM, pByteStr, 8);
                        for(i=0;i<16;i++)pCharHex[i] =(char) pByteStr[i];
                        strRel += ",PM:" +String.valueOf(pCharHex);
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,strRel);
                    }
                    else
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_find no card " + "ErrCode:" + result);
                }
            }

            //call_comPro.lc_halt(hdev);//This code is used to find card only once
            call_comPro.lc_exit(hdev);

        }//for(nTest = 0; nTest < linkedReaderNum; nTest++)

        return result;
    }

    public int TestM1(int portType, String path, int baudrate) {
        // TODO Auto-generated method stub
        int result = 0, hdev=1;
        char[] pModVer = new char[512];
        byte[] pSnrM1 = new byte[255];
        byte[] snrSize = new byte[2];
        int[] tag  = new int[2];
        byte[] sak = new byte[2];
        char[] pCharHex = new char[255];
        char[] pCharSingle = new char[255];
        byte[] pByteBuf = new byte[255];
        byte[] pByteStr = new byte[255];
        long sn;//Card SN in integer type
        long _sn;//Card SN in integer type (Reverse)
        char[] rv_hexCard = new char[128];//Card SN in HEX type(Reverse)
        char[] rv_strCard = new char[128];//Card SN in HEX string type (Reverse)
        int lenSingleChar=2, lenHex;
        byte tblk = 24;
        byte val_blk = 25;
        byte chineseBlk = 26;//中文测试数据块
        int[] pCurVal = new int[1];
        byte tSec = (byte)(tblk/4);
        byte keymode = 0x60;
        byte[] defKey = new byte[]{(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff};
        byte[] newKey = {(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff};
        byte[] strNewkey = new byte[255];
        byte[] tWrite = {0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0xd,0xe,0xf};
        char[] strHexWrite =new char[100];
        byte[] tRead = new byte[100];
        char[] strHexRead = new char[100];
        char[] strCHNdata_in = new char[100];//中文对应的char
        byte[] bytesCHNdata_in= new byte[100];//实际写入卡里的数据
        byte[] bytesCHNdata_out = new byte[100];//实际读出的数据
        byte[] strCHNdata_out = new byte[100];
        String strTmp =" ";
        byte[] strKeyb = {(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff};
        byte[] strCtrlW = {(byte)0xff, 0x07,(byte)0x80,(byte)0x69};
        int tryLinkCnt = 0;
        long startTime = 0;
        long timePass = 0;
        int i;
        int nTest;
        int linkedReaderNum = 0;
        int[] hDevArr = new int[4];

        //转换工具函数测试
//		lenHex = 2*lenSingleChar;
//		pCharHex =("090a").toCharArray();

//		call_comPro.a_hex(pCharSingle,pCharHex,   lenSingleChar);
//		SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," a_hex:");
//		for(i=0;i<lenSingleChar;i++)
//		{
//			SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," "+Integer.toString(pCharSingle[i]));
//		}
//
//		pCharHex = new char[lenHex];
//		call_comPro.hex_a(pCharHex, pCharSingle,   lenHex);
//		SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," hex_a:"+String.valueOf(pCharHex));

        //连接端口
        do {
            if (portType == PT_USB) hdev = call_comPro.lc_init_ex(2, "".toCharArray(), 0);
            else hdev = call_comPro.lc_init_ex(1, path.toCharArray(), baudrate);

            if(hdev != -1) {
                hDevArr[linkedReaderNum] = hdev;
                linkedReaderNum++;
                if(portType == PT_SERIAL)break;
            }
        }while(hdev != -1);

        if(linkedReaderNum == 0)//NO Reader mounted
        {
            //Log.e("readerlog", "Link reader error");
            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_Link reader failed ");
            return result;
        }


        //连接端口成功
        for(nTest = 0; nTest < linkedReaderNum; nTest++)
        {
            hdev = hDevArr[nTest];

            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Operating Reader: " + String.valueOf(hdev) + "---------------" );

            startTime = System.currentTimeMillis();//Save the start time for test

            //获取一次硬件版本号
            //try to get module version
            result = call_comPro.lc_getver(hdev, pModVer);
            if(0 == result)
            {
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"-");
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Module Version: " + String.valueOf(pModVer));

                call_comPro.lc_rfReset(hdev, 10);//RF Reset

                //获取卡号 UID
                result = call_comPro.lc_card(hdev, (byte)1, pSnrM1, snrSize, tag,sak);
                if(0 == result)
                {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_card:ok ");

                    call_comPro.hex_asc(pSnrM1, pByteStr, snrSize[0]);
                    for(i=0;i<2*snrSize[0];i++)pCharHex[i] =(char) pByteStr[i];
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.valueOf(pCharHex));
                    //卡片验证
                    // authen
                    result = call_comPro.lc_authentication(hdev, keymode, tSec , defKey);

                    if(0 == result)
                    {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_authen:ok ");

                        //写卡测试
                        //write
                        result = call_comPro.lc_write(hdev, tblk, tWrite);

                        if(0 == result)
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_write block "+tblk +" :ok ");

                            //读卡内容
                            //read
                            result = call_comPro.lc_read(hdev, tblk, tRead);

                            if(0 == result)
                            {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_read block "+tblk +" :ok ");

                                call_comPro.hex_asc( tRead, pByteBuf,16);
                                for(i=0;i<32;i++)pCharHex[i] =(char) pByteBuf[i];
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.valueOf(pCharHex));

                                //中文读写的演示
                                bytesCHNdata_in = "中文1234测试abc".getBytes();

                                //for(i=0; i< bytesCHNdata_in.length; i++)
                                //	strCHNdata_in[i] = (char)bytesCHNdata_in[i];


                                call_comPro.lc_write(hdev, chineseBlk, bytesCHNdata_in);
                                call_comPro.lc_read(hdev, chineseBlk, strCHNdata_out);

                                try{
                                    for(i=0;i<strCHNdata_out.length;i++)
                                        bytesCHNdata_out[i] = (byte) strCHNdata_out[i];
                                    strTmp = new String(bytesCHNdata_out,"UTF-8");
                                }catch(Exception e){}

                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," chinese data test:" +strTmp);

                                //钱包操作示例
                                //钱包格式化
                                //value test
                                result = call_comPro.lc_initval(hdev, val_blk, 1000);//初值1000
                                if(0 == result)
                                {
                                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _initval block "+val_blk +" :ok");

                                    //圈存(加值）
                                    result = call_comPro.lc_increment(hdev, val_blk, 200);//+200
                                    if(0 == result)
                                    {
                                        //钱包操作生效
                                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _increment block "+val_blk +" :ok");

                                        //扣费 (减值)
                                        result = call_comPro.lc_decrement(hdev, val_blk, 100);//-100
                                        if(0 == result)
                                        {
                                            //钱包操作生效
                                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _decrement block "+val_blk +"  :ok");

                                            //读当前余额
                                            result = call_comPro.lc_readval(hdev, val_blk, pCurVal );
                                            if(0 == result)
                                            {
                                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _readval block "+val_blk +"  ok:" + pCurVal[0]);

                                            }else SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _readval error");
                                        }else SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _decrement:error");
                                    }else SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _increment:error");

                                }else SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _initval:error");

                            }
                            else
                            {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_read:error ");
                            }
                        }
                        else
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_write:error ");
                        }

                    }
                    else
                    {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_athen:error ");
                    }


                    timePass = System.currentTimeMillis() - startTime;
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Time: " + timePass+ " ms");

                }
                else
                {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_find no card ");
                }

                //call_comPro.lc_beep(hdev, 10);//debug
            }
            else
            {
                Log.e("readerlog", "_getVersion:error, st:"+result);
            }
            //call_comPro.lc_halt(hdev);//This code is used to find card only once
            call_comPro.lc_exit(hdev);

        }//for(nTest = 0; nTest < linkedReaderNum; nTest++)

        return result;

    }

    public int TestUltralight(int portType, String path, int baudrate) {
        int result = 0, hdev=1;
        char[] pModVer = new char[512];
        byte[] pSnrM1 = new byte[255];
        char[] pCharHex = new char[255];
        char[] pCharTmp = new char[255];
        byte[] pCharSingle = new byte[255];
        byte[] snrSize = new byte[2];
        int[] tag  = new int[2];
        byte[] sak = new byte[2];
        int i;
        byte tPage = 4;
        byte[] tWrite = {0x01,0x02,0x03,0x04};
        byte[] tRead = new byte[512];
        if(portType == PT_USB)hdev = call_comPro.lc_init_ex(2, null, 0);
        else hdev = call_comPro.lc_init_ex (1, path.toCharArray(), baudrate);
        if (hdev != -1) {

            call_comPro.lc_beep(hdev, 5);

            //try to get module version
            result = call_comPro.lc_getver(hdev, pModVer);
            if(0 == result)
            {
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"-");
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Module Version: " + String.valueOf(pModVer));

                result = call_comPro.lc_card(hdev, (byte)1, pSnrM1, snrSize, tag,sak);
                if(0 == result)
                {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_card:ok ");
                    //SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.valueOf(pSnrM1));
                    call_comPro.hex_asc( pSnrM1, pCharSingle,snrSize[0]);
                    for(i=0;i<2*snrSize[0]; i++)pCharHex[i] = (char)pCharSingle[i];

                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.valueOf(pCharHex));

                    // read page 0
                    result = call_comPro.lc_read_NTag(hdev,(byte) 0, pCharSingle );

                    if(0 == result)
                    {
                        //write
                        result = call_comPro.lc_write_NTag(hdev, tPage, tWrite);

                        if(0 == result)
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_write:ok ");

                            //read
                            result = call_comPro.lc_read_NTag(hdev, tPage, tRead);

                            if(0 == result)
                            {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_read:ok ");

                                for(i=0;i<4;i++)
                                {
                                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," "+Integer.toHexString(tRead[i]));
                                }

                            }
                            else
                            {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_read:error ");
                            }
                        }
                        else
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_write:error ");
                        }

                    }
                    else SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_read:error ");

                }
            }

            call_comPro.lc_exit(hdev);
        }

        return result;
    }
    public int TestNDEF(int portType, String path, int baudrate) {
        // TODO Auto-generated method stub
        int result = 0, hdev=1;
        char[] pModVer = new char[512];
        int i;
        char[] pSBuffer = new char[1024];
        char[] pRBuffer = new char[1024];
        char[] pPwd = new char[32];
        byte[] bt_buf = new byte[1024];
        int[] pPayloadType =new int[2];
        int tagType = 0x09;//0-M1, 9-NTAG213, card Types ,Defined See compro_nfch.h
        int textFormat = 0x01;//0x01-GBK,0x02-Unicode,0x03-UTF8, Defined see compro_nfch.h
        int validDataLength = 0;
        String strForChineseTest = new String("哎，离谱了。");
        String strOut="";

        if(portType == PT_USB)hdev = call_comPro.lc_init_ex(2, "".toCharArray(), 0);
        else hdev = call_comPro.lc_init_ex (1, path.toCharArray(), baudrate);
        if (hdev != -1) {

            //try to get module version
            result = call_comPro.lc_getver(hdev, pModVer);
            if(0 == result)
            {
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"-");
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Module Version: " + String.valueOf(pModVer));

                //Write NDEF(Text)
                bt_buf = strForChineseTest.getBytes();
                for(i=0; i< bt_buf.length; i++)pSBuffer[i] = (char)(bt_buf[i] & 0xff);
                result = call_comPro.lc_NFC_FormatCard_WithText(hdev, tagType, "zh".toCharArray(), pSBuffer,textFormat,"".toCharArray(), false);
                if (result != 0) {
                	SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Write text error!");
                } else {
                	SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Write text ok!\n");
                }

                //Read NDEF(Text)
                result = call_comPro.lc_NFC_ReadTag(hdev, tagType, textFormat, pPwd, pPayloadType, pRBuffer);
                if (result != 0) {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Read text error!\n");
                } else {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Read text ok!\n");
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Text[ASC]: " + String.valueOf(pRBuffer));
                    while(pRBuffer[validDataLength] != 0)
                    {
                        bt_buf[validDataLength]=(byte) pRBuffer[validDataLength];
                        validDataLength++;
                        if(validDataLength == pRBuffer.length)
                            break;
                    }

                    try{strOut = new String(bt_buf, "UTF-8");}catch(Exception e){}
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Text[UTF8]: " + strOut);
                }
            }
            else
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Get version error!\n");

            call_comPro.lc_exit(hdev);
        }

        return result;
    }
    public  void printHexString( byte[] b, int length) {
        for (int i = 0; i < length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,hex.toUpperCase() );
        }
        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"\n");
    }

    public int TestCTLcpu(int portType, String path, int baudrate) {
        // TODO Auto-generated method stub
        int result = 0, hdev=1;
        byte[] _Snr = new byte[100];
        byte[] snrSize = new byte[2];
        int[] tag  = new int[2];
        byte[] sak = new byte[2];
        char[] pLen_char = new char[4];
        char[] rBuf = new char[512];
        int[] pLen_int = new int[1];
        byte[] bufCmdSend ={0x00,(byte)0x84,0x00,0x00,0x08};
        short bufCmdLen = 5;
        char[] strCmdRev = new char[512];
        char[] strResetInfo = new char[512];
        boolean status = false;
        byte[] btTmp = new byte[512];
        short _waitTime = 9;//wait when transfer
        short _fragmentSize = 52;
        int i;
        long startTime;
        long timePass;

        if(portType == PT_USB)hdev = call_comPro.lc_init_ex(2, null, 0);
        else hdev = call_comPro.lc_init_ex (1, path.toCharArray(), baudrate);
        if (hdev != -1){

            startTime = System.currentTimeMillis();//Save the start time for test

            call_comPro.lc_rfReset(hdev, 10);

            result = call_comPro.lc_card(hdev, (byte)0, _Snr, snrSize, tag,sak);
            if(0 == result)
            {
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "Find card hex:");
                printHexString(_Snr, snrSize[0]);

                //reset cpu card
                result = call_comPro.lc_pro_reset(hdev, pLen_char, btTmp);
                if(0 == result)
                {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "Cpu card reset ok->");
                    printHexString(btTmp, pLen_char[0]);

                    //send apdu command
                    result = call_comPro.lc_pro_commandlink(hdev, (short)bufCmdLen, bufCmdSend, pLen_char, btTmp, (short)_waitTime, (short)_fragmentSize);// strCmdRev);
                    if(0 == result)
                    {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "contactless cpu card test ok. send->");

                        printHexString(btTmp, bufCmdLen);

                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "contactless cpu card test ok. rev->");
                        printHexString(btTmp, pLen_char[0]);

                        call_comPro.lc_beep(hdev, 10);

                        timePass = System.currentTimeMillis() - startTime;
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Time: " + timePass+ " ms");

                        try{
                            sleep(50);
                        }catch(Exception e){e.printStackTrace();}
                    }
                    else
                    {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, " CPU card command sent error.");
                    }
                }
                else
                {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, " CPU card reset error.");
                }
            }
            else
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, " Card not found (contactless cpu card)");



            call_comPro.lc_exit(hdev);

        }

        return result;

    }
    public long FreeLinkWithReader(int iHdev)
    {
        call_comPro.lc_exit(iHdev);
        return 0;
    }
    public long FM1208_Test(int portType, String path, int baudrate)
    {
        int result = 0, hdev=1;
        int lhdev ;
        byte[] _Snr = new byte[100];
        byte[] snrSize = new byte[2];
        int[] tag  = new int[2];
        byte[] sak = new byte[2];
        char[] pLen_char = new char[4];
        byte[] btTmp = new byte[512];
        int i;
        char[] pRBuffer = new char[1024];
        char[] pExKey ={0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff};//外部认证密钥
        long   exKeyLen = 16;//8;
        char[] pFileProKey={0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff};//文件保护密钥
        char[] pPin = {0x11,0x22,0x33,0x44,0x55,0x66};
        String strToWrite = "this is a test!";
        short lenToRW ;
        short recLenToRW = 0x10;
        char[] pWriteBuffer;
        short testBinFileID = 0x0002;
        short testLoopRecFileID = 0x0003;
        short testVarLenRecFileID = 0x0004;
        short testFixLenRecFileID = 0x0005;
        long status = 0;
        boolean fTestWithAuthenPassword = true;
        boolean fTestWithCreate = true;//true;//false;


        if(portType == PT_USB)hdev = call_comPro.lc_init_ex(2, null, 0);
        else hdev = call_comPro.lc_init_ex (1, path.toCharArray(), baudrate);
        if (hdev != -1){

            lhdev = hdev;

            result = call_comPro.lc_card(hdev, (byte)1, _Snr, snrSize, tag,sak);
            if(0 == result)
            {
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "Find card hex:");
                printHexString(btTmp, pLen_char[0]);
                //reset cpu card
                result = call_comPro.lc_pro_reset(hdev, pLen_char, btTmp);
                if(0 == result)
                {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "Cpu card reset ok->");
                    printHexString(btTmp, pLen_char[0]);

                    ////////// Functions Test //
                    pWriteBuffer = strToWrite.toCharArray();
                    lenToRW = (short)strToWrite.length();

                    //Select main app
                    status = call_fm1208.FWCosSelecteApp(lhdev);
                    if (status != 0) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Select main app error!");
                        return FreeLinkWithReader(lhdev);
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Select main app ok!");
                    }

                    if(fTestWithCreate == true)//Enable create file system?
                    {
                        if(fTestWithAuthenPassword == true)
                        {
                            //-------------------------------------  Create Files ---
                            //Create key file
                            //建立一个密钥文件，用于存放各种密钥
                            status = call_fm1208.FWCosCreateKeyFile(lhdev,
                                    (short)0x0000,//所在目录文件标识,主应用为0
                                    (short)0x0001,//密钥文件标识符
                                    (short)0x0040,//密钥文件的长度
                                    0);//在密钥中安装密钥的权限,为0表示可以任意安装密钥
                            if (status != 0) {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosCreateKeyFile error!");
                                return FreeLinkWithReader(lhdev);

                            } else {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosCreateKeyFile ok!");
                            }
                            //Setup External Key
                            //安装外部认证密钥
                            status = call_fm1208.FWCosAddKey(lhdev,
                                    (char)2,//外部认证密钥类型
                                    pExKey,//密钥值
                                    exKeyLen);//
                            //16);//密钥文件长度
                            if (status != 0) {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosAddKey[ExKey] error!");
                                return FreeLinkWithReader(lhdev);

                            } else {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosAddKey[ExKey] ok!");
                            }

                            //Setup File Protect Key
                            //安装文件保护密钥
                            status = call_fm1208.FWCosAddKey(lhdev,
                                    (char)1,//文件保护密钥类型
                                    pFileProKey,//密钥值
                                    16);//密钥文件长度
                            if (status != 0) {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosAddKey[FileProKey] error!");
                                return FreeLinkWithReader(lhdev);

                            } else {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosAddKey[FileProKey] ok!");
                            }

                            //Setup Pin
                            //安装口令
                            status = call_fm1208.FWCosAddKey(lhdev,
                                    (char)3,//口令密钥类型
                                    pPin,//口令值
                                    6);//口令长度
                            if (status != 0) {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosAddKey[Pin] error!");
                                return FreeLinkWithReader(lhdev);

                            } else {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosAddKey[Pin] ok!");
                            }
                        }
                        //Create binary file
                        status = call_fm1208.FWCosCreateBinaryFile(lhdev, (short)0, testBinFileID, (short)0x80, (char)0, FM1208.FWCOS_SEC_ALW, FM1208.FWCOS_SEC_ALW, FM1208.FWCOS_SEC_ALW);
                        if(status != 0)
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosCreateBinaryFile error!");
                            return FreeLinkWithReader(lhdev);
                        } else {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosCreateBinaryFile ok!");
                        }
                        //Create fix-length record file
                        status = call_fm1208.FWCosCreateRecordFile(lhdev, (short)0, (char)0, testFixLenRecFileID, (char)16, (short)recLenToRW, (char)0, FM1208.FWCOS_SEC_ALW, FM1208.FWCOS_SEC_ALW, FM1208.FWCOS_SEC_ALW);
                        if(status != 0)
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Create fix-length record file error!");
                            return FreeLinkWithReader(lhdev);
                        } else {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Create fix-length record file ok!");
                        }
                        //Create var-length record file
                        status = call_fm1208.FWCosCreateRecordFile(lhdev, (short)0, (char)2, testVarLenRecFileID, (char)16, (short)recLenToRW, (char)0, FM1208.FWCOS_SEC_ALW, FM1208.FWCOS_SEC_ALW, FM1208.FWCOS_SEC_ALW);
                        if(status != 0)
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Create var-length record file error!");
                            return FreeLinkWithReader(lhdev);
                        } else {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Create var-length record file ok!");
                        }
                        //Create loop record file
                        status = call_fm1208.FWCosCreateRecordFile(lhdev, (short)0, (char)1, testLoopRecFileID, (char)16, (short)recLenToRW, (char)0, FM1208.FWCOS_SEC_ALW, FM1208.FWCOS_SEC_ALW, FM1208.FWCOS_SEC_ALW);
                        if(status != 0)
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Create loop record file error!");
                            return FreeLinkWithReader(lhdev);
                        } else {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Create loop record file ok!");
                        }
                    }

                    if(fTestWithAuthenPassword == true)
                    {
                        status = call_fm1208.FWCosVerifyKey(lhdev,pExKey,exKeyLen,FM1208.FW_KEYTYPE_EXVERIFY);
                        if(status != 0)
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Verify External Key error! Code:"+String.format("%X", status));
                            return FreeLinkWithReader(lhdev);
                        }
                        else
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Verify External Key ok!");
                    }

                    //-------------------------------------  Binary File ---
                    //Select binary file
                    status = call_fm1208.FWCosSelectFile(lhdev, testBinFileID);
                    if (status != 0) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Select bin file error!");
                        return FreeLinkWithReader(lhdev);
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Select bin file ok!");
                    }

                    //写(更新)二进制一般文件
                    status = call_fm1208.FWCosUpdateBinaryFile(lhdev,
                            (short)testBinFileID,//文件标识
                            (short)0x00,//开始偏移地址
                            pWriteBuffer,//数据值
                            (short)lenToRW,//数据长度
                            (char)0,//数据加密类型：0-明文方式,1-明文+MAC校验，2-密文+MAC校验
                            pFileProKey,//文件保护密钥
                            16);//密钥长度
                    if (status != 0) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosUpdateBinaryFile error!");
                        return FreeLinkWithReader(lhdev);
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosUpdateBinaryFile ok!");
                    }

                    //读二进制一般文件
                    status = call_fm1208.FWCosReadBinaryFile(lhdev,
                            (short)testBinFileID,//文件标识
                            pRBuffer,
                            (short)0x00,//开始偏移地址
                            (short)lenToRW,//数据长度
                            (char)0,//数据加密类型：0-明文方式,1-明文+MAC校验，2-密文+MAC校验
                            pFileProKey,//文件保护密钥
                            16);//密钥长度
                    if (status != 0) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosReadBinaryFile error!");
                        return FreeLinkWithReader(lhdev);
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosReadBinaryFile ok!");
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.valueOf(pRBuffer));
                    }

                    //-------------------------------------  Fix Length Record File ---
                    //Select fix record file
                    status = call_fm1208.FWCosSelectFile(lhdev, testFixLenRecFileID);
                    if (status != 0) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Select fix-length record file error!");
                        return FreeLinkWithReader(lhdev);
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Select fix-length record file ok!");
                    }
                    //Write file
                    pWriteBuffer = new char[recLenToRW + 1];
                    for(i=0; i< recLenToRW; i++)pWriteBuffer[i] = (char)(i);
                    status = call_fm1208.FWCosUpdateRecord(lhdev,(char) 0x00, (char)testFixLenRecFileID, (char)(0x01), pWriteBuffer, recLenToRW, (char)0, pFileProKey, 16);
                    if (status != 0) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosUpdateRecord error!");
                        return FreeLinkWithReader(lhdev);
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosUpdateRecord ok!");
                    }
                    //Read file
                    status =call_fm1208.FWCosReadRecord(lhdev, (char)0x00, (char)testFixLenRecFileID,(char) 0x01, pRBuffer, recLenToRW);
                    if (status != 0) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosReadRecord error!");
                        return FreeLinkWithReader(lhdev);
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosReadRecord ok!");
                        for(i=0; i< recLenToRW; i++)
                            btTmp[i] = (byte)pRBuffer[i];
                        printHexString(btTmp, recLenToRW);
                    }
                    //-------------------------------------  Loop Record File ---
                    //Select loop record file
                    status = call_fm1208.FWCosSelectFile(lhdev, testLoopRecFileID);
                    if (status != 0) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Select loop record file error!");
                        return FreeLinkWithReader(lhdev);
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Select loop record file ok!");
                    }
                    //Write file
                    pWriteBuffer = new char[recLenToRW + 1];
                    for(i=0; i< recLenToRW; i++)pWriteBuffer[i] = (char)(i+2);
                    status = call_fm1208.FWCosAppendRecord(lhdev, (char)testLoopRecFileID, pWriteBuffer, recLenToRW, (char)0, pFileProKey, 16);
                    if (status != 0) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosAppendRecord error!");
                        return FreeLinkWithReader(lhdev);
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosAppendRecord ok!");
                    }
                    //Read file
                    status =call_fm1208.FWCosReadRecord(lhdev, (char)0x01, (char)testLoopRecFileID,(char) 0x01, pRBuffer, recLenToRW);
                    if (status != 0) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosReadRecord error!");
                        return FreeLinkWithReader(lhdev);
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosReadRecord ok!");
                        for(i=0; i< recLenToRW; i++)
                            btTmp[i] = (byte)pRBuffer[i];
                        printHexString(btTmp, recLenToRW);
                    }
                    //-------------------------------------  Varying Record File ---
                    //Select varying record file
                    status = call_fm1208.FWCosSelectFile(lhdev, testVarLenRecFileID);
                    if (status != 0) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Select var-length record file error!");
                        return FreeLinkWithReader(lhdev);
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Select var-length record file ok!");
                    }
                    //Write file
                    pWriteBuffer = new char[recLenToRW + 1];
                    for(i=0; i< recLenToRW; i++)pWriteBuffer[i] = (char)(i+1);
                    status = call_fm1208.FWCosUpdateRecord(lhdev,(char) 0x01, (char)testVarLenRecFileID, (char)(0x01), pWriteBuffer, recLenToRW, (char)0, pFileProKey, 16);
                    if (status != 0) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosUpdateRecord error!");
                        return FreeLinkWithReader(lhdev);
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosUpdateRecord ok!");
                    }
                    //Read file
                    status =call_fm1208.FWCosReadRecord(lhdev, (char)0x02, (char)testVarLenRecFileID,(char) 0x01, pRBuffer, recLenToRW);
                    if (status != 0) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosReadRecord error!");
                        return FreeLinkWithReader(lhdev);
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosReadRecord ok!");
                        for(i=0; i< recLenToRW; i++)
                            btTmp[i] = (byte)pRBuffer[i];
                        printHexString(btTmp, recLenToRW);
                    }

                    if(fTestWithCreate == true)//Enable create file system?
                    {
                        if(fTestWithAuthenPassword == true)
                        {
                            status = call_fm1208.FWCosVerifyKey(lhdev,pExKey,exKeyLen,FM1208.FW_KEYTYPE_EXVERIFY);
                            if(status != 0)
                            {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Verify External Key error! Code:"+String.format("%X", status));
                                return FreeLinkWithReader(lhdev);
                            }
                            else
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Verify External Key ok!");
                        }
                        //-------------------------------------  Delete File System ---
                        status = call_fm1208.FWCosDeleteFileSys(lhdev);
                        if(status != 0)
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosDeleteFileSys error!");
                            return FreeLinkWithReader(lhdev);
                        } else {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"FWCosDeleteFileSys ok!");
                        }
                    }
                }
                else
                {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, " CPU card reset error.");
                }
            }
            else
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, " Card not found (contactless cpu card)");

            call_comPro.lc_exit(hdev);
        }

        return result;
    }
    public int TestICODE(int portType, String path, int baudrate){
        int result = 0, hdev=1;
        char[] pModVer = new char[512];
        char[] pChSN = new char[255];
        byte[] pBSN = new byte[255];
        int tryLinkCnt = 0;
        long startTime = 0;
        long timePass = 0;
        short status;
        int[] pInt = new int[1];
        byte[] pRBuffer = new byte[128];
        byte[] pbTmp = new byte[2];
        byte[] pbRLen = new byte[2];
        byte[] pbRData = new byte[100];
        byte[] pbSData = new byte[100];
        byte[] pbUID = new byte[9];

        byte startBlock = 5;
        byte rwBlockNumber = 1;


        char[] pShowBuf=new char[1024];
        int i, j;

        do {
            if (portType == PT_USB) hdev = call_comPro.lc_init_ex(2, null, 0);
            else hdev = call_comPro.lc_init_ex(1, path.toCharArray(), baudrate);

            if(hdev != -1) {
                tryLinkCnt++;
                result = call_comPro.lc_getver(hdev, pModVer);
                if(result != 0){
                    call_comPro.lc_exit(hdev);
                    hdev = -1;
                }
            }
        }while(hdev == -1);


        if (hdev != -1) {

            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Link reader ok. try " + String.valueOf(tryLinkCnt) + " times");

            startTime = System.currentTimeMillis();//Save the start time for test

            //try to get module version
            result = call_comPro.lc_getver(hdev, pModVer);
            if(0 == result)
            {
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"-");
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Module Version: " + String.valueOf(pModVer));


                result = call_comPro.lc_find15693(hdev, (byte)0x36, pbUID, pbRLen);//As single card mode
                if(0 == result)
                {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_card:ok ");
                    call_comPro.hex_asc( pbUID, pBSN,pbRLen[0]);
                    for(i=0; i< 2*pbRLen[0];i++)pChSN[i] = (char)pBSN[i];
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.valueOf(pChSN));

                    if(0 == result)
                    {

                        //write
                        for(j=0;j<4;j++)
                        {
                            pbSData[j]=(byte)'M';
                        }

                        result = call_comPro.lc_15693_writeBlock(hdev,pbUID,startBlock, rwBlockNumber,  (byte)(4*rwBlockNumber), pbSData);//д��4ҳ

                        if(0 == result)
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_write block "+startBlock +" :ok ");
                            //read
                            result = call_comPro.lc_15693_readBlock(hdev, pbUID, startBlock, rwBlockNumber, pbRLen, pbRData);

                            if(0 == result)
                            {
                                //call_comPro.hex_asc( BytesToChars(pbRData), pRBuffer, 4);
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.valueOf(BytesToChars(pbRData)));

                                timePass = System.currentTimeMillis() - startTime;
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Time: " + timePass + " ms");
                                call_comPro.lc_beep(hdev, 2);//20);
                            }
                            else
                            {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_read:error ");
                            }
                        }
                        else
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_write:error ");
                        }

                        //Expend test
                        result = call_comPro.lc_15693_command_custom(hdev,
                                (byte)0xB2,
                                (byte)0x04,
                                pbUID,
                                0,
                                pbSData,
                                pInt,
                                pRBuffer
                                );
                        if(0 == result){
                            call_comPro.hex_asc( pRBuffer, pbRData, pInt[0]);
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.valueOf(BytesToChars(pbRData)));
                        }else{
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_write:error ");
                        }

                    }
                    else
                    {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_select:error ");

                    }

                }
                else {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_No found ICODE ");
                }
            }

            call_comPro.lc_exit(hdev);
        }
        else
        {
            //Log.e("readerlog", "Link reader error");
            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_Link reader failed ");
        }
        return result;
    }
    public int TestTypeB(int portType, String path, int baudrate){
        int result = 0, hdev=1;
        char[] pModVer = new char[512];
        char[] pChSN = new char[255];
        byte[] pBSN = new byte[255];
        byte[] tCmd = {0x00,0x36,0x00,0x00,0x08};
        int cmdSize = 5;
        int tryLinkCnt = 0;
        long startTime = 0;
        long timePass = 0;
        short status;

        char[] pSBuffer = new char[128];
        char[] pRBuffer = new char[128];

        byte[] pbTmp = new byte[2];
        byte[] pbRLen = new byte[2];
        int[] piRLen = new int[2];
        byte[] pbRData = new byte[200];
        byte[] pbSData = new byte[200];
        byte[] pbUID = new byte[64];
        byte[] ATQB = new byte[64];


        char[] pShowBuf=new char[1024];
        int i, j;

        do {
            if (portType == PT_USB) hdev = call_comPro.lc_init_ex(2, null, 0);
            else hdev = call_comPro.lc_init_ex(1, path.toCharArray(), baudrate);

            if(hdev != -1) {
                tryLinkCnt++;
                result = call_comPro.lc_getver(hdev, pModVer);
                if(result != 0){
                    call_comPro.lc_exit(hdev);
                    hdev = -1;
                }
            }
        }while(hdev == -1);


        if (hdev != -1) {

            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Link reader ok. try " + String.valueOf(tryLinkCnt) + " times");

            startTime = System.currentTimeMillis();//Save the start time for test

            //try to get module version
            result = call_comPro.lc_getver(hdev, pModVer);
            if(0 == result)
            {
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"-");
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Module Version: " + String.valueOf(pModVer));


                result = comproCall.lc_findTypeB(hdev, (byte)1,  ATQB,pbRLen );//find type b card
                if(0 == result)
                {
                    for(i=0; i< pbRLen[0]; i++)
                        pbUID[i] = ATQB[i];

                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_card:ok ");
                    call_comPro.hex_asc(pbUID,pBSN, pbRLen[0]);
                    for(i=0; i< pbRLen[0] * 2;i++)pChSN[i] = (char)pBSN[i];
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.valueOf(pChSN));

                    result = call_comPro.lc_typeB_command(hdev, cmdSize, tCmd, pbRLen,pbRData);//Send Command

                    if(0 == result)
                    {

                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "TypeB cpu card test ok. send->");

                        printHexString(tCmd, tCmd.length);

                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "rev->");

                        printHexString(pbRData, pbRLen[0]);

                        //time cost
                        timePass = System.currentTimeMillis() - startTime;
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Time: " + timePass + " ms");
                        call_comPro.lc_beep(hdev, 2);//20);
                    }
                    else
                    {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_cpu cmd:error ");
                    }


                }
                else {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_No found TYPEB ");
                }
            }

            call_comPro.lc_exit(hdev);
        }
        else
        {
            //Log.e("readerlog", "Link reader error");
            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_Link reader failed ");
        }
        return result;
    }

    public int Test_4442(int portType, String path, int baudrate) {
        // TODO Auto-generated method stub
        int result = 0, hdev=1;
		char[] pModVer = new char[512];
		int t_offset = 32;
		int t_rwlen = 16;
		byte[] tWrite = {0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0xd,0xe,0xf};
        byte[] tRead = new byte[128];
        byte[] szRev = new byte[512];
		char[] strRev = new char[512];
		byte [] defPwd = {(byte)0xff,(byte)0xff,(byte)0xff};
		byte [] newPwd = {(byte)0xff,(byte)0xff,(byte)0xff};
		byte[] cntErr = new byte[2];
        int i;

		if(portType == PT_USB)hdev = call_comPro.lc_init_ex(2, null, 0);
		else hdev = call_comPro.lc_init_ex (1, path.toCharArray(), baudrate);
		if (hdev != -1) {

			call_comPro.lc_beep(hdev, 5);

			//try to get module version
			result = call_comPro.lc_getver(hdev, pModVer);
			if(0 == result)
			{
				SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"-");
				SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Module Version: " + String.valueOf(pModVer));

				if(0== result)
				{
                    //Select Card
                    result = comproCall.lc_iccSelCardType(hdev, (short)0, 0x03);
                    if(result == 0)
                    {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Select card type ok");

                        result = call_comPro.lc_icc_ReadErrorCounter(hdev, (short)0, cntErr);
                        if(result  == 0 && cntErr[0] != 0)
                        {
                            result = call_comPro.lc_icc_VerifyUserPass(hdev,(short)0, 3, defPwd );

                            if(0 == result)
                            {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Authen password ok");

                                result = call_comPro.lc_icc_WriteMem(hdev, (short)0, t_offset,t_rwlen,tWrite );

                                if(0 == result)
                                {
                                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Write ok.");

                                    result = call_comPro.lc_icc_ReadMem(hdev, (short)0, t_offset,t_rwlen,tRead);
                                    if(0 == result)
                                    {
                                        call_comPro.hex_asc( tRead, szRev,t_rwlen);
                                        for(i=0; i< t_rwlen; i++)
                                            strRev[i] = (char)szRev[i];
                                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Read ok, info:"+ String.valueOf(strRev));

                                        result = call_comPro.lc_icc_UpdateUserPass(hdev,(short)0, 3, newPwd);
                                        if(0 == result)
                                        {
                                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Change 4442 pwd ok.");
                                        }
                                        else
                                        {
                                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Change 4442 pwd error.");
                                        }
                                    }
                                    else
                                    {
                                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," IC_Read error.");
                                    }
                                }//IC_Write == 0
                                else
                                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Write error.");
                            }//IC_CheckPass_SLE4442 == 0
                            else
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Authen password error");
                        }// IC_ReadCount_SLE4442  != 0
                        else
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Invalid 4442 card.");
                    }
                    else
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Select card type error");

				}//IC_Status == 1
				else
					SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Not find card.");
			}

			SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"\r\n");

			call_comPro.lc_exit(hdev);
		}
        return result;

    }
    public int Test_4428(int portType, String path, int baudrate) {
        // TODO Auto-generated method stub
        int result = 0, hdev=1;
        char[] pModVer = new char[512];
        int t_offset = 32;
        int t_rwlen = 16;
        byte[] tWrite = {0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0xd,0xe,0xf};
        byte[] tRead = new byte[128];
        byte[] szRev = new byte[512];
        char[] strRev = new char[512];
        byte [] defPwd = {(byte)0xff,(byte)0xff,(byte)0xff};
        byte [] newPwd = {(byte)0xff,(byte)0xff,(byte)0xff};
        byte[] cntErr = new byte[2];
        int i;

        if(portType == PT_USB)hdev = call_comPro.lc_init_ex(2, null, 0);
        else hdev = call_comPro.lc_init_ex (1, path.toCharArray(), baudrate);
        if (hdev != -1) {

            call_comPro.lc_beep(hdev, 5);

            //try to get module version
            result = call_comPro.lc_getver(hdev, pModVer);
            if(0 == result)
            {
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"-");
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Module Version: " + String.valueOf(pModVer));

                if(0== result)
                {
                    //Select Card
                    result = comproCall.lc_iccSelCardType(hdev, (short)0, 0x04);
                    if(result == 0)
                    {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Select card type ok");

                        result = call_comPro.lc_icc_ReadErrorCounter(hdev, (short)0, cntErr);
                        if(result  == 0 && cntErr[0] != 0)
                        {
                            result = call_comPro.lc_icc_VerifyUserPass(hdev,(short)0, 2, defPwd );

                            if(0 == result)
                            {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Authen password ok");

                                result = call_comPro.lc_icc_WriteMem(hdev, (short)0, t_offset,t_rwlen,tWrite );

                                if(0 == result)
                                {
                                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Write ok.");

                                    result = call_comPro.lc_icc_ReadMem(hdev, (short)0, t_offset,t_rwlen,tRead);
                                    if(0 == result)
                                    {
                                        call_comPro.hex_asc( tRead, szRev,t_rwlen);
                                        for(i=0; i< t_rwlen; i++)
                                            strRev[i] = (char)szRev[i];
                                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Read ok, info:"+ String.valueOf(strRev));

                                        result = call_comPro.lc_icc_UpdateUserPass(hdev,(short)0, 2, newPwd);
                                        if(0 == result)
                                        {
                                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Change 4428 pwd ok.");
                                        }
                                        else
                                        {
                                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Change 4428 pwd error.");
                                        }
                                    }
                                    else
                                    {
                                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," IC_Read error.");
                                    }
                                }//IC_Write == 0
                                else
                                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Write error.");
                            }//IC_CheckPass_SLE4442 == 0
                            else
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Authen password error");
                        }// IC_ReadCount_SLE4442  != 0
                        else
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Invalid 4428 card.");
                    }
                    else
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Select card type error");

                }//IC_Status == 1
                else
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Not find card.");
            }

            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"\r\n");

            call_comPro.lc_exit(hdev);
        }
        return result;

    }

    public int Test_24CXXs(int portType, String path, int baudrate, int cardType)
    {
        int result = 0, hdev=1;
        char[] pModVer = new char[512];
        int t_offset = 0;
        int t_rwlen = 16;
        byte[] stCard = new byte[2];
        byte[] tWrite = {0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0xd,0xe,0xf};
        byte[] tRead = new byte[512];
        byte[] szMsg = new byte[512];
        char[] strMsg = new char[512];
        short  card_slot = 0x00;//Main slot

        if(portType == PT_USB)hdev = comproCall.lc_init_ex(2, null, 0);
        else hdev = comproCall.lc_init_ex(1, path.toCharArray(), baudrate);
        if (hdev != -1) {

            //try to get module version
            result = comproCall.lc_getver(hdev, pModVer);
            if(0 == result)
            {
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"-");
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Module Version: " + String.valueOf(pModVer));

                result = comproCall.lc_iccGetCardState(hdev, (short)card_slot, stCard);
                if( (0 == result) && (stCard[0] == 0x01) )
                {
                    comproCall.lc_iccSelCardType(hdev, (short)card_slot, cardType);

                    result = comproCall.lc_icc_WriteMem(hdev, card_slot, t_offset, t_rwlen, tWrite );

                    if(0 == result)
                    {
                        comproCall.hex_asc(tWrite, szMsg, t_rwlen );
                        strMsg = BytesToChars(szMsg);
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Write ok, info: "+String.valueOf(strMsg));

                        result = comproCall.lc_icc_ReadMem(hdev, card_slot, t_offset, t_rwlen, tRead);
                        if(0 == result)
                        {
                            comproCall.hex_asc(tRead, szMsg, t_rwlen );
                            strMsg = BytesToChars(szMsg);
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Read ok, info:"+ String.valueOf(strMsg));
                        }
                        else
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," IC_Read error.");
                        }
                    }//IC_Write == 0
                    else
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Write error.");

                }//IC_Status == 1
                else
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Not find card.");

                comproCall.lc_beep(hdev, 10);
            }

            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"\r\n");

            comproCall.lc_exit(hdev);
        }

        return result;
    }

    public int Test_24CXX(int portType, String path, int baudrate)
    {
        int result = 0;

        result = Test_24CXXs(portType, path, baudrate, 0x01);
        return result;
    }
    public int Test_24C64(int portType, String path, int baudrate)
    {
        int result = 0;

        result = Test_24CXXs(portType, path, baudrate, 0x02);
        return result;
    }
    public int TestCTcpu(int portType, String path, int baudrate) {
        // TODO Auto-generated method stub
        int result = 0, hdev=1;
        byte[] cardSt = new byte[2];
        byte[] pLen_byte = new byte[4];
        int[] pLen_int = new int[2];
        byte[] rBuf = new byte[512];
        char[] pModVer = new char[64];
        byte[] bufCmdSend ={0x00,(byte)0x84,0x00,0x00,0x08};
        short  bufCmdLen = 5;
        char[] strCmdRev = new char[512];
        char[] strResetInfo = new char[512];
        boolean status = false;
        byte[] btTmp = new byte[512];
        short _waitTime = 9;//wait when transfer
        short _fragmentSize = 52;
        int i;
        long startTime;
        long timePass;

        if(portType == PT_USB)hdev = call_comPro.lc_init_ex(2, null, 0);
        else hdev = call_comPro.lc_init_ex (1, path.toCharArray(), baudrate);
        if (hdev != -1){

            startTime = System.currentTimeMillis();//Save the start time for test

            //try to get module version
            result = call_comPro.lc_getver(hdev, pModVer);
            if(0 == result) {
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "-");
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, " Module Version: " + String.valueOf(pModVer));

                result = call_comPro.lc_iccGetCardState(hdev, (short)0, cardSt);
                if( (0 == result) && (cardSt[0] == 1) )
                {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "Find card");

                    //comproCall.lc_iccSetBaud(hdev, (short)0, 38400);

                    //reset cpu card
                    result = call_comPro.lc_iccGetATR(hdev, (short)0, rBuf, pLen_byte);
                    if(0 == result)
                    {
                        for(i=0; i< pLen_byte[0]; i++)
                            btTmp[i] = (byte)rBuf[i];

                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "Cpu card reset ok->");
                        printHexString(btTmp, pLen_byte[0]);


                        //send apdu command
                        result = call_comPro.lc_icc_APDU(hdev, (short)0, bufCmdLen, bufCmdSend, pLen_int, rBuf);
                        if(0 == result)
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "Cpu card command test ok. send->");
                            for(i=0; i< bufCmdLen; i++)
                                btTmp[i] = (byte)bufCmdSend[i];

                            printHexString(btTmp, bufCmdLen);

                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "rev->");
                            for(i=0; i< pLen_int[0]; i++)
                                btTmp[i] = (byte)rBuf[i];
                            printHexString(btTmp, pLen_int[0]);

                            call_comPro.lc_beep(hdev, 10);

                            timePass = System.currentTimeMillis() - startTime;
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Time: " + timePass+ " ms");

                            try{
                                sleep(50);
                            }catch(Exception e){e.printStackTrace();}
                        }
                        else
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, " CPU card command sent error.");
                        }
                    }
                    else
                    {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, " CPU card reset error.");
                    }
                }
                else
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, " Card not found (Cpu card)");

            }else{
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Firmware version get fail.");
            }

            call_comPro.lc_exit(hdev);
        }

        return result;

    }
    public int Test_RF_PSAM(int portType, String path, int baudrate, int bSAM) {
        // TODO Auto-generated method stub
        int result = 0, hdev=1;
        byte[] cardSt = new byte[2];
        byte[] pLen_byte = new byte[4];
        int[] pLen_int = new int[2];
        byte[] rBuf = new byte[512];
        char[] pModVer = new char[64];
        byte[] bufCmdSend ={0x00,(byte)0x84,0x00,0x00,0x08};
        short  bufCmdLen = 5;
        char[] strCmdRev = new char[512];
        char[] strResetInfo = new char[512];
        boolean status = false;
        byte[] btTmp = new byte[512];
        short _waitTime = 9;//wait when transfer
        short _fragmentSize = 52;
        int i;
        long startTime;
        long timePass;

        if(portType == PT_USB)hdev = call_comPro.lc_init_ex(2, null, 0);
        else hdev = call_comPro.lc_init_ex (1, path.toCharArray(), baudrate);
        if (hdev != -1){

            startTime = System.currentTimeMillis();//Save the start time for test

            //try to get module version
            result = call_comPro.lc_getver(hdev, pModVer);
            if(0 == result) {
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "-");
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, " Module Version: " + String.valueOf(pModVer));

                //Set baud
                result = comproCall.lc_iccSetBaud(hdev, (short)bSAM, 9600);
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "Set baud ret->" + result);

                //reset cpu card
                result = call_comPro.lc_iccGetATR(hdev, (short) bSAM, rBuf, pLen_byte);
                if (0 == result) {
                    for (i = 0; i < pLen_byte[0]; i++)
                        btTmp[i] = (byte) rBuf[i];

                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "Cpu card reset ok->");
                    printHexString(btTmp, pLen_byte[0]);

                    //PPS
                    byte[] ppsPara = new byte[6];
                    result = comproCall.lc_iccPPS(hdev, (short)bSAM, ppsPara);
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "PPS ret->" + result);

                    //send apdu command
                    result = call_comPro.lc_icc_APDU(hdev, (short) bSAM, bufCmdLen, bufCmdSend, pLen_int, rBuf);
                    if (0 == result) {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "Cpu card command test ok. send->");
                        for (i = 0; i < bufCmdLen; i++)
                            btTmp[i] = (byte) bufCmdSend[i];

                        printHexString(btTmp, bufCmdLen);

                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "rev->");
                        for (i = 0; i < pLen_int[0]; i++)
                            btTmp[i] = (byte) rBuf[i];
                        printHexString(btTmp, pLen_int[0]);

                        call_comPro.lc_beep(hdev, 10);

                        timePass = System.currentTimeMillis() - startTime;
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, " Time: " + timePass + " ms");

                        try {
                            sleep(50);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, " CPU card command sent error.");
                    }
                } else {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, " CPU card reset error.");
                }
            }else{
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Firmware version get fail.");
            }

            call_comPro.lc_exit(hdev);
        }

        return result;
    }
    public int Test_RF_PSAM1(int portType, String path, int baudrate) {
        return Test_RF_PSAM(portType, path, baudrate, 1);
    }
    public int Test_RF_PSAM2(int portType, String path, int baudrate) {
        return Test_RF_PSAM(portType, path, baudrate, 2);
    }
    public int TestSSC(int portType, String path, int baudrate)
    {
        int result = 0, hdev=1;
        int linkedReaderNum = 0;
        char[] pModVer = new char[512];
        char[] pChar = new char[255];
        byte[] pByteStr = new byte[255];
        String strTmp="";
        long startTime = 0;
        long timePass = 0;
        int i;
        int nTest;
        int[] hDevArr = new int[4];

        //连接端口
        do {
            if (portType == PT_USB) hdev = call_comPro.lc_init_ex(2, "".toCharArray(), 0);
            else hdev = call_comPro.lc_init_ex(1, path.toCharArray(), baudrate);

            if(hdev != -1) {
                hDevArr[linkedReaderNum] = hdev;
                linkedReaderNum++;
                if(portType == PT_SERIAL)break;
            }
        }while(hdev != -1);

        if(linkedReaderNum == 0)//NO Reader mounted
        {
            //Log.e("readerlog", "Link reader error");
            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_Link reader failed ");
            return result;
        }


        //连接端口成功
        for(nTest = 0; nTest < linkedReaderNum; nTest++)
        {
            hdev = hDevArr[nTest];

            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Operating Reader: " + String.valueOf(hdev) + "---------------" );

            startTime = System.currentTimeMillis();//Save the start time for test

            //获取一次硬件版本号
            //try to get module version
            result = call_comPro.lc_getver(hdev, pModVer);
            if(0 == result)
            {

                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"-");
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Module Version: " + String.valueOf(pModVer));

                //Read card
                result = call_comPro.lc_readSSC(hdev, (byte)struct_cardInterface, pChar);
                if(0 == result)
                {
                    try{
                        for(i=0;i<pChar.length;i++)
                            pByteStr[i] = (byte) pChar[i];
                        strTmp = new String(pByteStr,"gb2312");
                    }catch(Exception e){}

                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Information:\r\n");
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,strTmp);

                    timePass = System.currentTimeMillis() - startTime;
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Time: " + timePass+ " ms");

                }
                else
                {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_find no card " + "ErrCode:" + result);
                }
            }
            call_comPro.lc_beep(hdev, 1);//test
            call_comPro.lc_exit(hdev);

        }//for(nTest = 0; nTest < linkedReaderNum; nTest++)

        return result;
    }
    public int Test_PBOC_PAN(int portType, String path, int baudrate)
    {
        int result = 0, hdev=1;
        int linkedReaderNum = 0;
        char[] pModVer = new char[512];
        char[] pChar = new char[255];
        byte[] pByteStr = new byte[255];
        String strTmp="";
        long startTime = 0;
        long timePass = 0;
        int i;
        int nTest;
        int[] hDevArr = new int[4];

        //连接端口
        do {
            if (portType == PT_USB) hdev = comproCall.lc_init_ex(2, "".toCharArray(), 0);
            else hdev = comproCall.lc_init_ex(1, path.toCharArray(), baudrate);

            if(hdev != -1) {
                hDevArr[linkedReaderNum] = hdev;
                linkedReaderNum++;
                if(portType == PT_SERIAL)break;
            }
        }while(hdev != -1);

        if(linkedReaderNum == 0)//NO Reader mounted
        {
            //Log.e("readerlog", "Link reader error");
            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_Link reader failed ");
            return result;
        }


        //连接端口成功
        for(nTest = 0; nTest < linkedReaderNum; nTest++)
        {
            hdev = hDevArr[nTest];

            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Operating Reader: " + String.valueOf(hdev) + "---------------" );

            startTime = System.currentTimeMillis();//Save the start time for test

            //获取一次硬件版本号
            //try to get module version
            result = comproCall.lc_getver(hdev, pModVer);
            if(0 == result)
            {

                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"-");
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Module Version: " + String.valueOf(pModVer));

                //Read card
                result = comproCall.lc_getPBOC_PAN(hdev, (byte)struct_cardInterface, (byte)1, pChar);
                if(0 == result)
                {
                    try{
                        for(i=0;i<pChar.length;i++)
                            pByteStr[i] = (byte) pChar[i];
                        strTmp = new String(pByteStr,"gb2312");
                    }catch(Exception e){}

                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Information:\r\n");
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,strTmp);

                    timePass = System.currentTimeMillis() - startTime;
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," Time: " + timePass+ " ms");

                }
                else
                {
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_find no card " + "ErrCode:" + result);
                }
            }
            comproCall.lc_beep(hdev, 1);//test
            comproCall.lc_exit(hdev);

        }//for(nTest = 0; nTest < linkedReaderNum; nTest++)

        return result;
    }
    //A function byte array to char array
    public char[] BytesToChars(byte[] pChars)
    {
        int length = pChars.length;
        char[]  out = new char[length];
        int i;

        for(i=0; i< length; i++)
            out[i] = (char) pChars[i];

        return out;
    }

    ////////////////////////////////////////////////////////////////// AUTO TEST ////
}
package com.dk.usbNfc;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.dk.log.DKLog;
import com.dk.usbNfc.DeviceManager.DeviceManager;
import com.dk.usbNfc.DeviceManager.DeviceManagerCallback;
import com.dk.usbNfc.DeviceManager.UsbNfcDevice;

/**
 * Created by Administrator on 2017/5/2.
 */

public class UsbNfcDeviceService extends Service {
    public static final String TAG = "BleNfcDeviceService";
    private final IBinder mBinder = new LocalBinder();

    public UsbNfcDevice usbNfcDevice;
    public DeviceManagerCallback deviceManagerCallback;

    @Override
    public void onCreate() {
        super.onCreate();

        //初始设备操作类
        usbNfcDevice = new UsbNfcDevice(UsbNfcDeviceService.this);
        usbNfcDevice.setCallBack(mDeviceManagerCallback);
        usbNfcDevice.usbHidManager.init();
    }

    //设备操作类回调
    private DeviceManagerCallback mDeviceManagerCallback = new DeviceManagerCallback() {
        @Override
        public void onReceiveInitCiphy(boolean blnIsInitSuc) {
            super.onReceiveInitCiphy(blnIsInitSuc);
            if (deviceManagerCallback != null) {
                deviceManagerCallback.onReceiveInitCiphy(blnIsInitSuc);
            }
        }

        @Override
        public void onReceiveDeviceAuth(byte[] authData) {
            super.onReceiveDeviceAuth(authData);
            if (deviceManagerCallback != null) {
                deviceManagerCallback.onReceiveDeviceAuth(authData);
            }
        }

        @Override
        public void onReceiveRfnSearchCard(boolean blnIsSus, int cardType, byte[] bytCardSn, byte[] bytCarATS) {
            super.onReceiveRfnSearchCard(blnIsSus, cardType, bytCardSn, bytCarATS);
            if (deviceManagerCallback != null) {
                deviceManagerCallback.onReceiveRfnSearchCard(blnIsSus, cardType, bytCardSn, bytCarATS);
            }
            if (!blnIsSus|| cardType == UsbNfcDevice.CARD_TYPE_NO_DEFINE) {
                return;
            }
            StringBuilder stringBuffer = new StringBuilder();
            for (byte aBytCardSn : bytCardSn) {
                stringBuffer.append(String.format("%02x", aBytCardSn));
            }

            StringBuilder stringBuffer1 = new StringBuilder();
            for (byte bytCarAT : bytCarATS) {
                stringBuffer1.append(String.format("%02x", bytCarAT));
            }
            DKLog.d(TAG, "BleNfcDeviceService接收到激活卡片回调：UID->" + stringBuffer + " ATS->" + stringBuffer1);
        }

        @Override
        public void onReceiveRfmSentApduCmd(byte[] bytApduRtnData) {
            super.onReceiveRfmSentApduCmd(bytApduRtnData);
            if (deviceManagerCallback != null) {
                deviceManagerCallback.onReceiveRfmSentApduCmd(bytApduRtnData);
            }
            StringBuilder stringBuffer = new StringBuilder();
            for (byte aBytApduRtnData : bytApduRtnData) {
                stringBuffer.append(String.format("%02x", aBytApduRtnData));
            }
            DKLog.d(TAG, "BleNfcDeviceService接收到APDU回调：" + stringBuffer);
        }

        @Override
        public void onReceiveRfmClose(boolean blnIsCloseSuc) {
            super.onReceiveRfmClose(blnIsCloseSuc);
            if (deviceManagerCallback != null) {
                deviceManagerCallback.onReceiveRfmClose(blnIsCloseSuc);
            }
        }

        @Override
        //按键返回回调
        public void onReceiveButtonEnter(byte keyValue) {
            super.onReceiveButtonEnter(keyValue);
            if (deviceManagerCallback != null) {
                deviceManagerCallback.onReceiveButtonEnter(keyValue);
            }
            if (keyValue == DeviceManager.BUTTON_VALUE_SHORT_ENTER) { //按键短按
                DKLog.d(TAG, "BleNfcDeviceService接收到按键短按回调");
            }
            else if (keyValue == DeviceManager.BUTTON_VALUE_LONG_ENTER) { //按键长按
                DKLog.d(TAG, "BleNfcDeviceService接收到按键长按回调");
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //DKLog.d(TAG, "onStartCommand() executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //DKLog.d(TAG, "onDestroy() executed");
    }

    public class LocalBinder extends Binder {
        public UsbNfcDeviceService getService() {
            return UsbNfcDeviceService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //设置设备管理回调，设备管理回调会回调会在蓝牙状态改变时（断开连接、连接成功等）回调
    public void setDeviceManagerCallback(DeviceManagerCallback deviceManagerCallback) {
        this.deviceManagerCallback = deviceManagerCallback;
        //usbNfcDevice.setCallBack(this.deviceManagerCallback);
    }
}

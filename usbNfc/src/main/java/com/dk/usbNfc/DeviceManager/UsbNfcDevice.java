package com.dk.usbNfc.DeviceManager;

import android.content.Context;

import com.dk.usbNfc.DKCloudID.DKCloudID;
import com.dk.usbNfc.Exception.CardNoResponseException;
import com.dk.usbNfc.Exception.DeviceNoResponseException;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/5/15.
 */
public class UsbNfcDevice extends DeviceManager{
    final static int DEVICE_NO_RESPONSE_TIME = 500;

    public UsbNfcDevice(Context context) {
        super(context);
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        //关闭USB
        usbHidManager.close();

        //关闭服务器连接
        DKCloudID.Close();

        usbHidManager.unregisterUsbReceiver();

        release();
    }

    /**
     * 是否正在自动寻卡
     * @return         true - 正在自动寻卡
     *                  false - 自动寻卡已经关闭
     */
    public boolean isAutoSearchCard() {
        return super.autoSearchCardFlag;
    }

    /**
     * 获取设备当前电池电压，同步阻塞方式
     * @return         设备电池电压，单位：V
     * @throws DeviceNoResponseException
     *                  操作无响应时会抛出异常
     */
    public double getDeviceBatteryVoltage() throws DeviceNoResponseException {
        final double[] returnVoltage = new double[1];

        final Semaphore semaphore = new Semaphore(0);

        requestBatteryVoltageDevice(new onReceiveBatteryVoltageDeviceListener() {
            @Override
            public void onReceiveBatteryVoltageDevice(double voltage) {
                returnVoltage[0] = voltage;
                semaphore.release();
            }
        });

        try {
            if ( !semaphore.tryAcquire(DEVICE_NO_RESPONSE_TIME, TimeUnit.MILLISECONDS) ) {
                throw new DeviceNoResponseException("设备无响应");
            }
        } catch (InterruptedException e) {
            throw new DeviceNoResponseException("设备无响应");
        }
        return returnVoltage[0];
    }

    /**
     * 获取设备版本号，同步阻塞方式
     * @return         设备版本号，1 字节
     * @throws DeviceNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte getDeviceVersions() throws DeviceNoResponseException {
        final byte[] returnBytes = new byte[1];

        final Semaphore semaphore = new Semaphore(0);

        requestVersionsDevice(new onReceiveVersionsDeviceListener() {
            @Override
            public void onReceiveVersionsDevice(byte versions) {
                returnBytes[0] = versions;
                semaphore.release();
            }
        });

        try {
            if ( !semaphore.tryAcquire(DEVICE_NO_RESPONSE_TIME, TimeUnit.MILLISECONDS) ) {
                throw new DeviceNoResponseException("设备无响应");
            }
        } catch (InterruptedException e) {
            throw new DeviceNoResponseException("");
        }
        return returnBytes[0];
    }

    /**
     * 关闭蜂鸣器，同步阻塞方式
     * @return           true - 操作成功
     *                    false - 操作失败
     * @throws DeviceNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean closeBeep(int onDelayMs, int offDelayMs, int n) throws DeviceNoResponseException {
        final byte[][] returnBytes = new byte[1][1];
        final boolean[] isCmdRunSucFlag = {false};

        final Semaphore semaphore = new Semaphore(0);
        returnBytes[0] = null;

        requestOpenBeep(0, 0, 0, new onReceiveOpenBeepCmdListener() {
            @Override
            public void onReceiveOpenBeepCmd(boolean isSuc) {
                if (isSuc) {
                    isCmdRunSucFlag[0] = true;
                }
                else {
                    isCmdRunSucFlag[0] = false;
                }
                semaphore.release();
            }
        });

        try {
            if ( !semaphore.tryAcquire(DEVICE_NO_RESPONSE_TIME, TimeUnit.MILLISECONDS) ) {
                throw new DeviceNoResponseException("设备无响应");
            }
        } catch (InterruptedException e) {
            throw new DeviceNoResponseException("设备无响应");
        }

        return isCmdRunSucFlag[0];
    }

    /**
     * 打开蜂鸣器指令，同步阻塞方式
     * @param onDelayMs  打开蜂鸣器时间：0~0xffff，单位ms
     * @param offDelayMs 关闭蜂鸣器时间：0~0xffff，单位ms
     * @param n          蜂鸣器响多少声：0~255
     * @return           true - 操作成功
     *                    false - 操作失败
     * @throws DeviceNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean openBeep(int onDelayMs, int offDelayMs, int n) throws DeviceNoResponseException {
        final byte[][] returnBytes = new byte[1][1];
        final boolean[] isCmdRunSucFlag = {false};

        final Semaphore semaphore = new Semaphore(0);
        returnBytes[0] = null;

        requestOpenBeep(onDelayMs, offDelayMs, n, new onReceiveOpenBeepCmdListener() {
            @Override
            public void onReceiveOpenBeepCmd(boolean isSuc) {
                if (isSuc) {
                    isCmdRunSucFlag[0] = true;
                }
                else {
                    isCmdRunSucFlag[0] = false;
                }
                semaphore.release();
            }
        });

        try {
            if ( !semaphore.tryAcquire(DEVICE_NO_RESPONSE_TIME, TimeUnit.MILLISECONDS) ) {
                throw new DeviceNoResponseException("设备无响应");
            }
        } catch (InterruptedException e) {
            throw new DeviceNoResponseException("设备无响应");
        }

        return isCmdRunSucFlag[0];
    }

    /**
     * 开始自动寻卡，同步阻塞方式
     * @param delayMs      寻卡间隔,单位 10毫秒
     * @param bytCardType  ISO14443_P3 - 寻M1/UL卡
     *                      ISO14443_P4-寻CPU卡
     * @return             true - 操作成功
     *                      false - 操作失败
     * @throws DeviceNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean startAutoSearchCard(byte delayMs, byte bytCardType) throws DeviceNoResponseException {
        final byte[][] returnBytes = new byte[1][1];
        final boolean[] isCmdRunSucFlag = {false};

        final Semaphore semaphore = new Semaphore(0);
        returnBytes[0] = null;

        requestRfmAutoSearchCard(true, delayMs, bytCardType, new onReceiveAutoSearchCardListener() {
            @Override
            public void onReceiveAutoSearchCard(boolean isSuc) {
                if (isSuc) {
                    isCmdRunSucFlag[0] = true;
                }
                else {
                    isCmdRunSucFlag[0] = false;
                }
                semaphore.release();
            }
        });

        try {
            if ( !semaphore.tryAcquire(DEVICE_NO_RESPONSE_TIME, TimeUnit.MILLISECONDS) ) {
                throw new DeviceNoResponseException("设备无响应");
            }
        } catch (InterruptedException e) {
            throw new DeviceNoResponseException("设备无响应");
        }

        return isCmdRunSucFlag[0];
    }

    /**
     * 停止自动寻卡，同步阻塞方式
     * @return             true - 操作成功
     *                      false - 操作失败
     * @throws DeviceNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean stoptAutoSearchCard() throws DeviceNoResponseException {
        final byte[][] returnBytes = new byte[1][1];
        final boolean[] isCmdRunSucFlag = {false};

        final Semaphore semaphore = new Semaphore(0);
        returnBytes[0] = null;

        requestRfmAutoSearchCard(false, (byte) 100, (byte) 0, new onReceiveAutoSearchCardListener() {
            @Override
            public void onReceiveAutoSearchCard(boolean isSuc) {
                if (isSuc) {
                    isCmdRunSucFlag[0] = true;
                }
                else {
                    isCmdRunSucFlag[0] = false;
                }
                semaphore.release();
            }
        });

        try {
            if ( !semaphore.tryAcquire(DEVICE_NO_RESPONSE_TIME, TimeUnit.MILLISECONDS) ) {
                throw new DeviceNoResponseException("设备无响应");
            }
        } catch (InterruptedException e) {
            throw new DeviceNoResponseException("设备无响应");
        }

        return !isCmdRunSucFlag[0];
    }

    /**
     * 关闭RF天线，同步阻塞方式
     * @return             true - 操作成功
     *                      false - 操作失败
     * @throws DeviceNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean closeRf() throws DeviceNoResponseException {
        final byte[][] returnBytes = new byte[1][1];
        final boolean[] isCmdRunSucFlag = {false};

        final Semaphore semaphore = new Semaphore(0);
        returnBytes[0] = null;

        requestRfmClose(new onReceiveRfmCloseListener() {
            @Override
            public void onReceiveRfmClose(boolean isSuc) {
                if (isSuc) {
                    isCmdRunSucFlag[0] = true;
                }
                else {
                    isCmdRunSucFlag[0] = false;
                }
                semaphore.release();
            }
        });

        try {
            if ( !semaphore.tryAcquire(DEVICE_NO_RESPONSE_TIME, TimeUnit.MILLISECONDS) ) {
                throw new DeviceNoResponseException("设备无响应");
            }
        } catch (InterruptedException e) {
            throw new DeviceNoResponseException("设备无响应");
        }

        return !isCmdRunSucFlag[0];
    }

    /**
     * 升级命令
     * @param data     发送的数据
     * @param timeout   超时时间
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] OTACmd(byte[] data, int timeout) throws DeviceNoResponseException {
        synchronized(this) {
//            if (data == null || data.length == 0) {
//                throw new CardNoResponseException("数据不能为null");
//            }

            final byte[][] returnBytes = new byte[1][1];
            final boolean[] isCmdRunSucFlag = {false};

            final Semaphore semaphore = new Semaphore(0);
            returnBytes[0] = null;

            requestOTA(data, new onReceiveGetOTAListener() {
                @Override
                public void onReceiveGetOTA(boolean isCmdRunSuc, byte[] bytRtnData) {
                    if (isCmdRunSuc) {
                        returnBytes[0] = bytRtnData;
                        isCmdRunSucFlag[0] = true;
                    } else {
                        returnBytes[0] = null;
                        isCmdRunSucFlag[0] = false;
                    }
                    semaphore.release();
                }
            });

            try {
                semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new DeviceNoResponseException("设备无响应");
            }
            if (!isCmdRunSucFlag[0]) {
                throw new DeviceNoResponseException("指令运行失败");
            }
            return returnBytes[0];
        }
    }
}

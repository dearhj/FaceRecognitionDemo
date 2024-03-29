package com.dk.usbNfc.Card;

import com.dk.usbNfc.DeviceManager.DeviceManager;
import com.dk.usbNfc.Exception.CardNoResponseException;
import com.dk.usbNfc.Tool.StringTool;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/9/21.
 */
public class CpuCard extends Card {
    public onReceiveApduExchangeListener mOnReceiveApduExchangeListener;

    public CpuCard(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public CpuCard(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }

    //APDU指令通道回调
    public interface onReceiveApduExchangeListener{
        public void onReceiveApduExchange(boolean isCmdRunSuc, byte[] bytApduRtnData);
    }

    //APDU指令通道，异步回调方式
    public void apduExchange(byte[] apduBytes, onReceiveApduExchangeListener listener) {
        mOnReceiveApduExchangeListener = listener;
        deviceManager.requestRfmSentApduCmd(apduBytes, new DeviceManager.onReceiveRfmSentApduCmdListener() {
            @Override
            public void onReceiveRfmSentApduCmd(boolean isCmdRunSuc, byte[] bytApduRtnData) {
                if (mOnReceiveApduExchangeListener != null) {
                    mOnReceiveApduExchangeListener.onReceiveApduExchange(isCmdRunSuc, bytApduRtnData);
                }
            }
        });
    }

    public String transceive(String cmdStr, int timeout) throws CardNoResponseException {
        return StringTool.byteHexToSting(transceive(StringTool.hexStringToBytes(cmdStr), timeout));
    }

    /**
     * cpu卡指令传输，同步阻塞方式
     * @param data     发送的数据
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] transceive(byte[] data) throws CardNoResponseException {
        return transceive(data, CAR_NO_RESPONSE_TIME_MS);
    }

    /**
     * cpu卡指令传输，同步阻塞方式
     * @param data     发送的数据
     * @param timeout  超时时间
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] transceive(byte[] data, int timeout) throws CardNoResponseException {
        synchronized(this) {
            if (data == null || data.length == 0) {
                throw new CardNoResponseException("数据不能为null");
            }

            final byte[][] returnBytes = new byte[1][1];
            final boolean[] isCmdRunSucFlag = {false};

            final Semaphore semaphore = new Semaphore(0);
            returnBytes[0] = null;

            apduExchange(data, new onReceiveApduExchangeListener() {
                @Override
                public void onReceiveApduExchange(boolean isCmdRunSuc, byte[] bytApduRtnData) {
                    if (isCmdRunSuc) {
                        returnBytes[0] = bytApduRtnData;
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
                deviceManager.mOnReceiveRfmSentApduCmdListener = null;
                throw new CardNoResponseException(CAR_NO_RESPONSE);
            }
            deviceManager.mOnReceiveRfmSentApduCmdListener = null;
            if (!isCmdRunSucFlag[0]) {
                throw new CardNoResponseException(CAR_RUN_CMD_FAIL);
            }
            return returnBytes[0];
        }
    }
}

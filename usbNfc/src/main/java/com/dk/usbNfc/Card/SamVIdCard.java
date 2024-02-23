package com.dk.usbNfc.Card;

import com.dk.usbNfc.DKCloudID.IDCardData;
import com.dk.usbNfc.DeviceManager.DeviceManager;
import com.dk.usbNfc.Exception.CardNoResponseException;
import com.dk.usbNfc.Tool.StringTool;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SamVIdCard  extends Card{
    DeviceManager mDeviceManager;

    public SamVIdCard(DeviceManager deviceManager) {
        super(deviceManager);
        mDeviceManager = deviceManager;
    }

    /**
     * 获取设备保存的序列号，设备默认的序列号是FFFFFFFFFFFFFFFF，同步阻塞方式，注意：不能在主线程里运行
     * @return         返回的序列号，8字节
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] getSamVInitData() throws CardNoResponseException {
        synchronized(this) {
            final byte[][] returnBytes = new byte[1][1];
            final boolean[] isCmdRunSucFlag = {false};

            final Semaphore semaphore = new Semaphore(0);
            returnBytes[0] = null;

            mDeviceManager.requestSamVInitData(new DeviceManager.onReceiveGetSamVInitDataListener() {
                @Override
                public void onReceiveGetSamVInitData(boolean isCmdRunSuc, byte[] initData) {
                    if (isCmdRunSuc) {
                        returnBytes[0] = initData;
                        isCmdRunSucFlag[0] = true;
                    } else {
                        returnBytes[0] = null;
                        isCmdRunSucFlag[0] = false;
                    }
                    semaphore.release();
                }
            });

            try {
                semaphore.tryAcquire(CAR_NO_RESPONSE_TIME_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new CardNoResponseException("设备无响应");
            }

            if (!isCmdRunSucFlag[0]) {
                throw new CardNoResponseException("读取身份证初始化数据失败，请不要移动身份证");
            }
            return returnBytes[0];
        }
    }

//    /**
//     * 获取设备保存的序列号，设备默认的序列号是FFFFFFFFFFFFFFFF，同步阻塞方式，注意：不能在主线程里运行
//     * @return         返回的序列号，8字节
//     * @throws CardNoResponseException
//     *                  操作无响应时会抛出异常
//     */
//    public byte[] getSamVAesKeyData() throws CardNoResponseException {
//        synchronized(this) {
//            final byte[][] returnBytes = new byte[1][1];
//            final boolean[] isCmdRunSucFlag = {false};
//
//            final Semaphore semaphore = new Semaphore(0);
//            returnBytes[0] = null;
//
//            mDeviceManager.requestSamVGetAESKey(new DeviceManager.onReceiveGetSamVAESKeyListener() {
//                @Override
//                public void onReceiveGetSamVAESKey(boolean isCmdRunSuc, byte[] initData) {
//                    if (isCmdRunSuc) {
//                        returnBytes[0] = initData;
//                        isCmdRunSucFlag[0] = true;
//                    } else {
//                        returnBytes[0] = null;
//                        isCmdRunSucFlag[0] = false;
//                    }
//                    semaphore.release();
//                }
//            });
//
//            try {
//                semaphore.tryAcquire(CAR_NO_RESPONSE_TIME_MS, TimeUnit.MILLISECONDS);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                throw new CardNoResponseException("设备无响应");
//            }
//
//            if (!isCmdRunSucFlag[0]) {
//                throw new CardNoResponseException("获取解析AES KEY失败");
//            }
//            return returnBytes[0];
//        }
//    }

    /**
     * 通过本地安全模块获取身份证信息，同步阻塞方式，注意：不能在主线程里运行
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public IDCardData getIdCardData() throws CardNoResponseException {
        byte[] selectIdCardDataCmdBytes = {(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, (byte)0x69, (byte)0x00, (byte)0x03, (byte)0x20, (byte)0x01, (byte)0x22};
        byte[] getIdCardDataCmdBytes = {(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, (byte)0x69, (byte)0x00, (byte)0x03, (byte)0x30, (byte)0x01, (byte)0x32};

        byte[] bytes = null;
        int cnt = 0;
        boolean isOk = false;
        do {
            try {
                bytes = transceiveLocal(selectIdCardDataCmdBytes, 100);

                if (bytes == null) {
                    isOk = false;
                }
                else if (StringTool.byteHexToSting(bytes).equals("aaaaaa9669000800009f0000000097")) {
                    isOk = true;
                }
                else {
                    return new IDCardData(null);
                }
            }catch (CardNoResponseException e) {
                e.printStackTrace();
                isOk = false;
            }
        }while (!isOk && (cnt++ < 10));

        bytes = transceiveLocal(getIdCardDataCmdBytes, 2000);
        return new IDCardData(bytes);
    }

    /**
     * cpu卡指令传输，同步阻塞方式，注意：不能在蓝牙初始化的线程里运行
     * @param data     发送的数据
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] transceive(byte[] data) throws CardNoResponseException {
        synchronized(this) {
            if (data == null || data.length == 0) {
                throw new CardNoResponseException("数据不能为null");
            }

            final byte[][] returnBytes = new byte[1][1];
            final boolean[] isCmdRunSucFlag = {false};

            final Semaphore semaphore = new Semaphore(0);
            returnBytes[0] = null;

            mDeviceManager.requestSamVDataExchange(data, new DeviceManager.onReceiveGetSamVApduListener() {
                @Override
                public void onReceiveGetSamVApdu(boolean isCmdRunSuc, byte[] bytApduRtnData) {
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
                semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new CardNoResponseException(CAR_NO_RESPONSE);
            }
            if (!isCmdRunSucFlag[0]) {
                throw new CardNoResponseException("读取身份证数据失败，请不要移动身份证");
            }
            return returnBytes[0];
        }
    }

    /**
     * 本地安全模块命令交互，同步阻塞方式，注意：不能在主线程里运行
     * @param data     发送的数据
     * @param timout   等待响应超时时间，单位ms
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] transceiveLocal(byte[] data, int timout) throws CardNoResponseException {
        synchronized(this) {
            if (data == null || data.length == 0) {
                throw new CardNoResponseException("数据不能为null");
            }

            final byte[][] returnBytes = new byte[1][1];
            final boolean[] isCmdRunSucFlag = {false};

            final Semaphore semaphore = new Semaphore(0);
            returnBytes[0] = null;

            mDeviceManager.requestSamVLocalDataExchange(data, new DeviceManager.onReceiveGetSamVLocalListener() {
                @Override
                public void onReceiveGetSamVLocal(boolean isCmdRunSuc, byte[] bytApduRtnData) {
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

            boolean isTimeout = false;
            try {
                isTimeout = !semaphore.tryAcquire(timout, TimeUnit.MILLISECONDS);
                if (isTimeout) {
                    throw new CardNoResponseException(CAR_NO_RESPONSE);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new CardNoResponseException(CAR_NO_RESPONSE);
            }

            if (!isCmdRunSucFlag[0]) {
                throw new CardNoResponseException(CAR_RUN_CMD_FAIL);
            }

            return returnBytes[0];
        }
    }
}

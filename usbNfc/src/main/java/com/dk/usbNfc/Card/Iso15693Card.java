package com.dk.usbNfc.Card;

import com.dk.usbNfc.DeviceManager.DeviceManager;
import com.dk.usbNfc.Exception.CardNoResponseException;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/9/21.
 */
public class Iso15693Card extends Card{
    public onReceiveReadListener mOnReceiveReadListener;
    public onReceiveReadMultipleListener mOnReceiveReadMultipleListener;
    public onReceiveWriteListener mOnReceiveWriteListener;
    public onReceiveLockBlockListener mOnReceiveLockBlockListener;
    public onReceiveCmdListener mOnReceiveCmdListener;
    public onReceiveWriteMultipleListener mOnReceiveWriteMultipleListener;

    public Iso15693Card(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public Iso15693Card(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }

    //ISO15693读单个块数据回调接口
    public interface onReceiveReadListener {
        public void onReceiveRead(boolean isSuc, byte[] returnBytes);
    }

    //ISO15693读多个块数据回调接口
    public interface onReceiveReadMultipleListener{
        void onReceiveReadMultiple(boolean isSuc, byte[] returnData);
    }

    //ISO15693写一个块数据接口
    public interface onReceiveWriteListener {
        public void onReceiveWrite(boolean isSuc);
    }

    //ISO15693写多个块函数接口
    public interface onReceiveWriteMultipleListener {
        void onReceiveWriteMultiple(boolean isSuc);
    }

    //ISO15693锁住一个块回调接口
    public interface onReceiveLockBlockListener{
        void onReceiveLockBlock(boolean isSuc);
    }

    //ISO1569指令通到调到接口
    public interface onReceiveCmdListener{
        void onReceiveCmd(boolean isSuc, byte returnData[]);
    }

    //ISO15693读单个块数据指令
    //addr：要读的块地址
    public void read(byte addr, onReceiveReadListener l) {
        mOnReceiveReadListener = l;

        byte[] cmdBytes = new byte[11];
        cmdBytes[0] = 0x22;
        cmdBytes[1] = 0x20;
        System.arraycopy(uid, 0, cmdBytes, 2, 8);
        cmdBytes[10] = addr;

//        deviceManager.requestRfmIso15693CmdBytes(cmdBytes, new DeviceManager.onReceiveRfIso15693CmdListener() {
//            @Override
//            public void onReceiveRfIso15693Cmd(boolean isSuc, byte[] returnData) {
//                if ((returnData == null) || (returnData.length < 5)) {
//                    if (mOnReceiveReadListener != null) {
//                        mOnReceiveReadListener.onReceiveRead(false, returnData);
//                    }
//                }
//                else {
//                    if (mOnReceiveReadListener != null) {
//                        byte[] readData = new byte[4];
//                        System.arraycopy(returnData, 1, readData, 0, 4);
//                        mOnReceiveReadListener.onReceiveRead(isSuc, readData);
//                    }
//                }
//            }
//        });

        deviceManager.requestRfmIso15693ReadSingleBlock(uid, addr, new DeviceManager.onReceiveRfIso15693ReadSingleBlockListener() {
            @Override
            public void onReceiveRfIso15693ReadSingleBlock(boolean isSuc, byte[] returnData) {
                if (returnData.length < 5) {
                    if (mOnReceiveReadListener != null) {
                        mOnReceiveReadListener.onReceiveRead(false, returnData);
                    }
                }
                else {
                    if (mOnReceiveReadListener != null) {
                        byte[] readData = new byte[4];
                        System.arraycopy(returnData, 1, readData, 0, 4);
                        mOnReceiveReadListener.onReceiveRead(isSuc, readData);
                    }
                }
            }
        });
    }

    //ISO15693读多个块数据指令
    //addr：要读的块地址
    //number:要读的块数量,必须大于0
    public void ReadMultiple(byte addr, byte number, onReceiveReadMultipleListener l) {
        mOnReceiveReadMultipleListener = l;
        deviceManager.requestRfmIso15693ReadMultipleBlock(uid, addr, number, new DeviceManager.onRecevieRfIso15693ReadMultipleBlockListener() {
            @Override
            public void onRecevieRfIso15693ReadMultipleBlock(boolean isSuc, byte[] returnData) {
                if ((returnData == null) || (returnData.length < 5)) {
                    if (mOnReceiveReadMultipleListener != null) {
                        mOnReceiveReadMultipleListener.onReceiveReadMultiple(false, returnData);
                    }
                }
                else {
                    if (mOnReceiveReadMultipleListener != null) {
                        byte[] readData = new byte[returnData.length - 1];
                        System.arraycopy(returnData, 1, readData, 0, readData.length);
                        mOnReceiveReadMultipleListener.onReceiveReadMultiple(isSuc, readData);
                    }
                }
            }
        });
    }

    //ISO15693写一个块
    //addr：要写卡片的块地址
    //writeData:要写的数据，必须4个字节
    public void write(byte addr, byte writeData[], onReceiveWriteListener l) {
        mOnReceiveWriteListener = l;
        deviceManager.requestRfmIso15693WriteSingleBlock(uid, addr, writeData, new DeviceManager.onReceiveRfIso15693WriteSingleBlockListener() {
            @Override
            public void onReceiveRfIso15693WriteSingleBlock(boolean isSuc) {
                if (mOnReceiveWriteListener != null) {
                    mOnReceiveWriteListener.onReceiveWrite(isSuc);
                }
            }
        });
    }

    //ISO15693写多个块
    //addr：要写的块地址
    //number:要写的块数量,必须大于0
    //writeData: 要写的数据，必须(number+1) * 4字节
    public void writeMultiple(byte addr, byte number, byte writeData[], onReceiveWriteMultipleListener l) {
        mOnReceiveWriteMultipleListener = l;
        deviceManager.requestRfmIso15693WriteMultipleBlock(this.uid, addr, number, writeData, new DeviceManager.onReceiveRfIso15693WriteMultipleBlockListener() {
            @Override
            public void onReceiveRfIso15693WriteMultipleBlock(boolean isSuc) {
                if (mOnReceiveWriteMultipleListener != null) {
                    mOnReceiveWriteMultipleListener.onReceiveWriteMultiple(isSuc);
                }
            }
        });
    }

    //ISO15693锁住一个块
    //addr：要锁住的块地址
    public void lockBlock(byte addr, onReceiveLockBlockListener l) {
        mOnReceiveLockBlockListener = l;
        deviceManager.requestRfmIso15693LockBlock(uid, addr, new DeviceManager.onReceiveRfIso15693LockBlockListener() {
            @Override
            public void onReceiveRfIso15693LockBlock(boolean isSuc) {
                if (mOnReceiveLockBlockListener != null) {
                    mOnReceiveLockBlockListener.onReceiveLockBlock(isSuc);
                }
            }
        });
    }

    /**
     * ISO15693锁住一个块，同步阻塞方式
     * @param addr        要锁的块的起始地址
     * @return            true:写入成功   false：写入失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean lockBlock(byte addr) throws CardNoResponseException {
        final byte[][] returnBytes = new byte[1][1];
        final boolean[] isCmdRunSucFlag = {false};

        final Semaphore semaphore = new Semaphore(0);

        lockBlock(addr, new onReceiveLockBlockListener() {
            @Override
            public void onReceiveLockBlock(boolean isCmdRunSuc) {
                if (isCmdRunSuc) {
                    isCmdRunSucFlag[0] = true;
                }
                else {
                    isCmdRunSucFlag[0] = false;
                }
                semaphore.release();
            }
        });

        try {
            semaphore.tryAcquire(CAR_NO_RESPONSE_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new CardNoResponseException(CAR_NO_RESPONSE);
        }

        return isCmdRunSucFlag[0];
    }

    //ISO15693指令通道
    public void cmd(byte cmdBytes[], onReceiveCmdListener l) {
        mOnReceiveCmdListener = l;
        deviceManager.requestRfmIso15693CmdBytes(cmdBytes, new DeviceManager.onReceiveRfIso15693CmdListener() {
            @Override
            public void onReceiveRfIso15693Cmd(boolean isSuc, byte[] returnData) {
                if (mOnReceiveCmdListener != null) {
                    mOnReceiveCmdListener.onReceiveCmd(isSuc, returnData);
                }
            }
        });
    }

    /**
     * ISO15693读单个块数据，同步阻塞方式
     * @param addr     要读的地址
     * @return         读取到的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] read(byte addr) throws CardNoResponseException {
        final byte[][] returnBytes = new byte[1][1];
        final boolean[] isCmdRunSucFlag = {false};

        final Semaphore semaphore = new Semaphore(0);
        returnBytes[0] = null;

        read(addr, new onReceiveReadListener() {
            @Override
            public void onReceiveRead(boolean isCmdRunSuc, byte[] bytApduRtnData) {
                if (isCmdRunSuc) {
                    returnBytes[0] = bytApduRtnData;
                    isCmdRunSucFlag[0] = true;
                }
                else {
                    returnBytes[0] = null;
                    isCmdRunSucFlag[0] = false;
                }
                semaphore.release();
            }
        });

        try {
            semaphore.tryAcquire(CAR_NO_RESPONSE_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new CardNoResponseException(CAR_NO_RESPONSE);
        }
        if (!isCmdRunSucFlag[0]) {
            throw new CardNoResponseException(CAR_RUN_CMD_FAIL);
        }
        return returnBytes[0];
    }

    /**
     * ISO15693读多个块数据指令，同步阻塞方式
     * @param addr     要读的块的起始地址
     * @param number   要读块的数量,必须大于0
     * @return         读取到的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] ReadMultiple(byte addr, byte number) throws CardNoResponseException {
        final byte[][] returnBytes = new byte[1][1];
        final boolean[] isCmdRunSucFlag = {false};

        final Semaphore semaphore = new Semaphore(0);
        returnBytes[0] = null;

        ReadMultiple(addr, number, new onReceiveReadMultipleListener() {
            @Override
            public void onReceiveReadMultiple(boolean isCmdRunSuc, byte[] bytApduRtnData) {
                if (isCmdRunSuc) {
                    returnBytes[0] = bytApduRtnData;
                    isCmdRunSucFlag[0] = true;
                }
                else {
                    returnBytes[0] = null;
                    isCmdRunSucFlag[0] = false;
                }
                semaphore.release();
            }
        });

        try {
            semaphore.tryAcquire(CAR_NO_RESPONSE_TIME_MS * 2, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new CardNoResponseException(CAR_NO_RESPONSE);
        }
        if (!isCmdRunSucFlag[0]) {
            throw new CardNoResponseException(CAR_RUN_CMD_FAIL);
        }
        return returnBytes[0];
    }

    /**
     * ISO15693写一个块，同步阻塞方式
     * @param addr        要写的块的地址
     * @param writeData   要写的数据，必须4个字节
     * @return            true:写入成功   false：写入失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean write(byte addr, byte writeData[]) throws CardNoResponseException {
        final byte[][] returnBytes = new byte[1][1];
        final boolean[] isCmdRunSucFlag = {false};

        final Semaphore semaphore = new Semaphore(0);

        write(addr, writeData, new onReceiveWriteListener() {
            @Override
            public void onReceiveWrite(boolean isCmdRunSuc) {
                if (isCmdRunSuc) {
                    isCmdRunSucFlag[0] = true;
                }
                else {
                    isCmdRunSucFlag[0] = false;
                }
                semaphore.release();
            }
        });

        try {
            semaphore.tryAcquire(CAR_NO_RESPONSE_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new CardNoResponseException(CAR_NO_RESPONSE);
        }

        return isCmdRunSucFlag[0];
    }

    /**
     * ISO15693写多个块，同步阻塞方式
     * @param addr        要写的块的地址
     * @param number      要写的块数量,必须大于0
     * @param writeData   要写的数据，必须4个字节
     * @return            true:写入成功   false：写入失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean writeMultiple(byte addr, byte number, byte writeData[]) throws CardNoResponseException {
        final byte[][] returnBytes = new byte[1][1];
        final boolean[] isCmdRunSucFlag = {false};

        final Semaphore semaphore = new Semaphore(0);

        writeMultiple(addr, number, writeData, new onReceiveWriteMultipleListener() {
            @Override
            public void onReceiveWriteMultiple(boolean isCmdRunSuc) {
                if (isCmdRunSuc) {
                    isCmdRunSucFlag[0] = true;
                }
                else {
                    isCmdRunSucFlag[0] = false;
                }
                semaphore.release();
            }
        });

        try {
            semaphore.tryAcquire(CAR_NO_RESPONSE_TIME_MS * 2, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new CardNoResponseException(CAR_NO_RESPONSE);
        }

        return isCmdRunSucFlag[0];
    }

    /**
     * ISO15693指令通道，同步阻塞方式
     * @param data     发送的数据
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] transceive(byte[] data) throws CardNoResponseException {
        final byte[][] returnBytes = new byte[1][1];
        final boolean[] isCmdRunSucFlag = {false};

        final Semaphore semaphore = new Semaphore(0);
        returnBytes[0] = null;

        cmd(data, new onReceiveCmdListener() {
            @Override
            public void onReceiveCmd(boolean isCmdRunSuc, byte[] bytApduRtnData) {
                if (isCmdRunSuc) {
                    returnBytes[0] = bytApduRtnData;
                    isCmdRunSucFlag[0] = true;
                }
                else {
                    returnBytes[0] = null;
                    isCmdRunSucFlag[0] = false;
                }
                semaphore.release();
            }
        });

        try {
            semaphore.tryAcquire(CAR_NO_RESPONSE_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new CardNoResponseException(CAR_NO_RESPONSE);
        }
        if (!isCmdRunSucFlag[0]) {
            throw new CardNoResponseException(CAR_RUN_CMD_FAIL);
        }
        return returnBytes[0];
    }
}

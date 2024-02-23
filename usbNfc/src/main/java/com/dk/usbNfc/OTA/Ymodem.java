package com.dk.usbNfc.OTA;

import android.util.Log;

import com.dk.log.DKLog;
import com.dk.usbNfc.DeviceManager.UsbNfcDevice;
import com.dk.usbNfc.Exception.DeviceNoResponseException;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Ymodem {
    private final static String TAG = "Ymodem";
    /* control signals */
    public static final byte SOH = 1;
    public static final byte STX = 2;  // Start of TeXt
    public static final byte EOT = 4;  // End Of Transmission
    public static final byte ACK = 6;  // Positive ACknowledgement
    public static final byte NACK = 0x15;  // Positive NACknowledgement
    public static final byte C = 67;   // capital letter C
    public static final int YMODEM_SOH_DATA_LEN = 128;
    public static final int YMODEM_STX_DATA_LEN = 1024;
    public static final int MAX_DATA_LEN = YMODEM_SOH_DATA_LEN;   // 最大数据长度


    public onReceiveScheduleListener mOnReceiveScheduleListener;

    UsbNfcDevice mUsbNfcDevice;

    public Ymodem(UsbNfcDevice usbNfcDevice) {
        mUsbNfcDevice = usbNfcDevice;
    }

    public Ymodem(UsbNfcDevice usbNfcDevice, onReceiveScheduleListener l) {
        mUsbNfcDevice = usbNfcDevice;
        mOnReceiveScheduleListener = l;
    }

    //进度回调
    public interface onReceiveScheduleListener{
        void onReceiveSchedule(int rate);
    }

    /**
     * @bieaf 固件升级回调接口设置
     * @param l 回调接口
     * @return true - 成功 false - 失败
     */
    public void setOnReceiveScheduleListener(onReceiveScheduleListener l) {
        mOnReceiveScheduleListener = l;
    }

    /**
     * @bieaf 固件升级
     * @param file 升级文件
     * @return true - 成功 false - 失败
     */
    public boolean YmodemUploadFile(File file, onReceiveScheduleListener l) {
        mOnReceiveScheduleListener = l;
        return YmodemUploadFile(file);
    }

    /**
     * @bieaf 固件升级
     * @param file 升级文件
     * @return true - 成功 false - 失败
     */
    public boolean YmodemUploadFile(InputStream file, onReceiveScheduleListener l) {
        mOnReceiveScheduleListener = l;
        return YmodemUploadFile(file);
    }

    /**
     * @bieaf 固件升级
     * @param file 升级文件
     * @return true - 成功 false - 失败
     */
    public boolean YmodemUploadFile(File file) {

        /* sizes */
        final int dataSize = MAX_DATA_LEN;

        int packetNumber = 0;
        byte[] data = new byte[dataSize];

        /* get the file */
        try {
            DataInputStream dataStream = new DataInputStream(new FileInputStream(file));

            //等待C
            byte[] OTARspBytes = mUsbNfcDevice.OTACmd(null, 200);
            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != C)) {
                DKLog.d(TAG, "wait C ok1");
                //return false;
            }
            //DKLog.d(TAG, "wait C ok");

            //发送第一包数据
            Arrays.fill(data, (byte) 0);
            OTARspBytes = sendYmodemPacket(0, data, dataSize, 3000);
            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != ACK)) {
                return false;
            }
            DKLog.d(TAG, "wait ACK ok");
            //等待C
            OTARspBytes = mUsbNfcDevice.OTACmd(null, 200);
            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != C)) {
                return false;
            }
            DKLog.d(TAG, "wait C ok");
            /* send packets with a cycle until we send the last byte */
            int fileReadCount;
            do {
                /* if this is the last packet fill the remaining bytes with 0 */
                fileReadCount = dataStream.read(data);
                if (fileReadCount == -1) {
                    break;
                }

                /* calculate packetNumber */
                packetNumber++;

                //进度回调
                if (mOnReceiveScheduleListener != null) {
                    mOnReceiveScheduleListener.onReceiveSchedule((packetNumber * 100) / (int)(file.length() / MAX_DATA_LEN));
                }

                //发送数据
                OTARspBytes = sendYmodemPacket(packetNumber % 256, data, dataSize, 200);
                if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != ACK)) {
                    return false;
                }
            } while (dataSize == fileReadCount);

            //发送EOT1
            OTARspBytes = mUsbNfcDevice.OTACmd(new byte[] { EOT }, 200);
            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != NACK)) {
                return false;
            }

            //发送EOT2
            OTARspBytes = mUsbNfcDevice.OTACmd(new byte[] { EOT }, 200);
            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != ACK)) {
                return false;
            }
            //等待C
            OTARspBytes = mUsbNfcDevice.OTACmd(null, 200);
            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != C)) {
                return false;
            }

            //发送结束帧
            Arrays.fill(data, (byte) 0);
            OTARspBytes = sendYmodemPacket(0, data, dataSize, 200);
            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != ACK)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            DKLog.e(TAG, e.getMessage());
            DKLog.e(TAG, e.getStackTrace());
        }

        return false;
    }

    /**
     * @bieaf 固件升级
     * @param file 升级文件
     * @return true - 成功 false - 失败
     */
    public boolean YmodemUploadFile(InputStream file) {

        /* sizes */
        final int dataSize = MAX_DATA_LEN;

        int packetNumber = 0;
        byte[] data = new byte[dataSize];

        /* get the file */
        try {
            DataInputStream dataStream = new DataInputStream(file);
            int total_len = file.available();

            byte[] OTARspBytes;

//            //等待C
//            OTARspBytes = mUsbNfcDevice.OTACmd(null, 2000);
//            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != C)) {
//                return false;
//            }
//            DKLog.d(TAG, "wait C ok");

            //发送第一包数据
            Arrays.fill(data, (byte) 0);
            OTARspBytes = sendYmodemPacket(0, data, dataSize, 3000);
            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != ACK)) {
                return false;
            }
            DKLog.d(TAG, "wait ACK ok");
            //等待C
            OTARspBytes = mUsbNfcDevice.OTACmd(null, 200);
            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != C)) {
                return false;
            }
            DKLog.d(TAG, "wait C ok");
            /* send packets with a cycle until we send the last byte */
            int fileReadCount;
            do {
                /* if this is the last packet fill the remaining bytes with 0 */
                fileReadCount = dataStream.read(data);
                if (fileReadCount == -1) {
                    break;
                }

                /* calculate packetNumber */
                packetNumber++;

                //进度回调
                if (mOnReceiveScheduleListener != null) {
                    mOnReceiveScheduleListener.onReceiveSchedule((packetNumber * 100) / (int)(total_len / MAX_DATA_LEN));
                }

                //发送数据
                OTARspBytes = sendYmodemPacket(packetNumber % 256, data, dataSize, 200);
                if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != ACK)) {
                    return false;
                }
            } while (dataSize == fileReadCount);

            //发送EOT1
            OTARspBytes = mUsbNfcDevice.OTACmd(new byte[] { EOT }, 200);
            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != NACK)) {
                return false;
            }

            //发送EOT2
            OTARspBytes = mUsbNfcDevice.OTACmd(new byte[] { EOT }, 200);
            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != ACK)) {
                return false;
            }
            //等待C
            OTARspBytes = mUsbNfcDevice.OTACmd(null, 200);
            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != C)) {
                return false;
            }

            //发送结束帧
            Arrays.fill(data, (byte) 0);
            OTARspBytes = sendYmodemPacket(0, data, dataSize, 200);
            if ((OTARspBytes == null) || (OTARspBytes.length != 1) || (OTARspBytes[0] != ACK)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            DKLog.e(TAG, e.getMessage());
            DKLog.e(TAG, e.getStackTrace());
        }

        return false;
    }

    private byte[] sendYmodemPacket(int packetNumber, byte[] data, int dataSize, int timeout) throws DeviceNoResponseException {
        byte[] sendBuf = new byte[MAX_DATA_LEN + 5];
        Arrays.fill(sendBuf, (byte) 0x1A);

        if (MAX_DATA_LEN == 128) {
            sendBuf[0] = SOH;
        }
        else {
            sendBuf[0] = STX;
        }
        sendBuf[1] = (byte)packetNumber;
        sendBuf[2] = ((byte) ~packetNumber);

        System.arraycopy(data, 0, sendBuf, 3, dataSize);

        int crc = 0;
        crc = crc16(data, crc);
        sendBuf[MAX_DATA_LEN + 3] = (byte)((crc >> 8) & 0xff);
        sendBuf[MAX_DATA_LEN + 4] = (byte)(crc & 0xff);

        //DKLog.d(TAG, "Ymodem send:" + StringTool.byteHexToSting(sendBuf));
        return mUsbNfcDevice.OTACmd(sendBuf, timeout);
    }

    /**
     * @bieaf CRC-16 计算
     *
     * @param data 数据
     * @param crc   CRC
     * @return crc  返回CRC的值
     */
    public int crc16(byte[] data, int crc) {
        int POLY = 0x1021;
        int index = 0;
        int i;

        int num = data.length;

        for (; num > 0; num--)	{				/* Step through bytes in memory */
            crc = crc ^ (((int)data[index++] & 0xff) << 8);   /* Fetch byte from memory, XOR into CRC top byte*/
            for (i = 0; i < 8; i++)	{			/* Prepare to rotate 8 bits */
                if ((crc & 0x8000) > 0) {       /* b15 is set... */
                    crc = (crc << 1) ^ POLY;    /* rotate and XOR with polynomic */
                }
                else {                          /* b15 is clear... */
                    crc <<= 1;                  /* just rotate */
                }
            }									/* Loop for 8 bits */
            crc &= 0xFFFF;						/* Ensure CRC remains 16-bit value */
        }										/* Loop until num=0 */
        return(crc);							/* Return updated CRC */
    }
}


















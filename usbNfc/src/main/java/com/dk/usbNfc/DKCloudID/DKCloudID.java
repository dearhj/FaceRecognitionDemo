package com.dk.usbNfc.DKCloudID;

import android.util.Log;

import com.dk.log.DKLog;
import com.dk.usbNfc.Tool.UtilTool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class DKCloudID {
    private final static String TAG = "DKCloudID";

    //private final static String ip = "192.168.3.139";
    private static String ip = "www.dkcloudid.cn";
    //    private final static String ip = "47.113.79.97";
    private final static int port = 20006;

    public static final int PACKET_HEAD_LENGTH = 2;
    private static Socket client = null;
    private static OutputStream out;
    private static InputStream in;
    private static boolean closed = false;

    public DKCloudID (){
//        try {
//            Thread.sleep(6000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        boolean isClientOk = true;
        try {
            if ((client == null) || client.isClosed() || (in == null)) {
                isClientOk = false;
            }
            else {
                client.setSoTimeout(1);
                if ( in.read() < 0 ) {
                    isClientOk = false;
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }

        //创建一个客户端socket
        if ( !isClientOk ) {
            DKLog.d(TAG, "建立连接");

            Close();
            client = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(ip, port);
            try {
                client.connect(socketAddress, 8000);
            } catch (IOException e) {
                Close();
                //连接备用服务器失败
                DKLog.d(TAG, "连接服务器失败：" + ip + ":" + port);
                DKLog.e(TAG, e.getMessage());
                DKLog.e(TAG, e.getStackTrace());
                return;
            }

            try {
                client.setTcpNoDelay(true);
                client.setSoTimeout(5000);

                //向服务器端传递信息
                out = client.getOutputStream();
                //获取服务器端传递的数据
                in = client.getInputStream();
                closed = false;
            } catch (UnknownHostException e) {
                Close();
                DKLog.e(TAG, e.getMessage());
                DKLog.e(TAG, e.getStackTrace());
            } catch (IOException e) {
                Close();
                DKLog.e(TAG, e.getMessage());
                DKLog.e(TAG, e.getStackTrace());
            }
        }
        else {
            try {
                client.setSoTimeout(5000);
            } catch (SocketException e) {
                DKLog.e(TAG, e.getMessage());
                DKLog.e(TAG, e.getStackTrace());
            }
        }
    }

    public static void setIp(String theIp) {
        if (theIp == null) {
            return;
        }

        ip = theIp;
    }

    /**
     * 获取client连接的状态
     * @return true - 已经连接， false - 已经断开
     */
    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * 使用TCP与云解析服务器进行数据交换，同步阻塞方式，必须在子线程中运行
     * @param initData NB-IOT发过来的数据（不要包含长度）
     * @return 服务器返回的数据，如果如果返回数据的长度大于300，则可以用AESKEY进行解密得到数据。
     */
    public byte[] dkCloudTcpDataExchange(byte[] initData) {
        if ( (initData == null) || closed ) {
            return null;
        }

        //发送解析请求
        SendPacket(initData);
        //等待接收数据，一直循环到关闭连接
        return ReadPacket();
    }

    private void clearInBuf() {
        if (in != null) {
            try {
                while (in.available() > 0) {
                    byte[] buf = new byte[1];
                    in.read(buf);
                }
            } catch (Exception e) {
                DKLog.e(TAG, e.getMessage());
                DKLog.e(TAG, e.getStackTrace());
            }
        }
    }

    // send packet to Server
    private void SendPacket( byte[] res ) {
        clearInBuf();

        byte[] headLen = UtilTool.shortToByte((short) res.length);
        byte[] body = UtilTool.mergeByte(headLen, res, 0, res.length);
        try {
            out.write(body);
            out.flush();
        } catch (Exception e) {
            DKLog.e(TAG, e.getMessage());
            DKLog.e(TAG, e.getStackTrace());
            Close();
        }
    }

    // read tcp stream
    private byte[] ReadPacket() {
        byte[] bodyBuff = new byte[0];
        byte[] headBuff = new byte[0];

        while (true) {
            if (closed) {
                DKLog.d(TAG, "请求已被关闭");
                return null;
            }
            try {
                // packet head size
                if (headBuff.length < PACKET_HEAD_LENGTH) {
                    byte[] head = new byte[PACKET_HEAD_LENGTH - headBuff.length];
                    int couter = in.read(head);
                    if (couter < 0) {
                        continue;
                    }

                    headBuff = UtilTool.mergeByte(headBuff, head, 0, couter);
                    if (headBuff.length < PACKET_HEAD_LENGTH) {
                        continue;
                    }
                }

                // packet body length
                short bodyLen = UtilTool.byteToShort(headBuff);

                if (bodyBuff.length < bodyLen) {
                    byte[] body = new byte[bodyLen - bodyBuff.length];
                    int couter = in.read(body);
                    if (couter < 0) {
                        continue;
                    }

                    bodyBuff = UtilTool.mergeByte(bodyBuff, body, 0, couter);
                    if (couter < body.length) {
                        continue;
                    }
                }

                return bodyBuff;
            } catch (Exception e) {
                DKLog.e(TAG, e.getMessage());
                DKLog.e(TAG, e.getStackTrace());
                Close();
                return null;
            }
        }
    }

    // close the tcp connection
    public static void Close() {
        closed = true;
        if (client != null) {
            try {
                client.close();

                if (out != null) {
                    out.close();
                }

                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                DKLog.e(TAG, e.getMessage());
                DKLog.e(TAG, e.getStackTrace());
            }
        }
    }
}

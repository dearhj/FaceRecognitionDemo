package com.za.finger;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.util.HashMap;

public class ZAAPI {

	public final int VendorId = 0x2109;
	public final int ProductId = 0x7638;
	public final int ZAZ_OK = 0x00;
	public final int ZAZ_COMM_ERR = 0x01;
	public final int ZAZ_NO_FINGER = 0x02;
	public final int ZAZ_GET_IMG_ERR = 0x03;
	public final int ZAZ_FP_TOO_DRY = 0x04;
	public final int ZAZ_FP_TOO_WET = 0x05;
	public final int ZAZ_FP_DISORDER = 0x06;
	public final int ZAZ_LITTLE_FEATURE = 0x07;
	public final int ZAZ_NOT_MATCH = 0x08;
	public final int ZAZ_NOT_SEARCHED = 0x09;
	public final int ZAZ_MERGE_ERR = 0x0a;
	public final int ZAZ_ADDRESS_OVER = 0x0b;
	public final int ZAZ_READ_ERR = 0x0c;
	public final int ZAZ_UP_TEMP_ERR = 0x0d;
	public final int ZAZ_RECV_ERR = 0x0e;
	public final int ZAZ_UP_IMG_ERR = 0x0f;
	public final int ZAZ_DEL_TEMP_ERR = 0x10;
	public final int ZAZ_CLEAR_TEMP_ERR = 0x11;
	public final int ZAZ_SLEEP_ERR = 0x12;
	public final int ZAZ_INVALID_PASSWORD = 0x13;
	public final int ZAZ_RESET_ERR = 0x14;
	public final int ZAZ_INVALID_IMAGE = 0x15;
	public final int ZAZ_HANGOVER_UNREMOVE = 0x17;

	public final int CHAR_BUFFER_A = 0x01;
	public final int CHAR_BUFFER_B = 0x02;
	public final int MODEL_BUFFER = 0x03;

	private String TAG = "ZAZAPI";
	private   int isonline = 0;
	private int isbus = 0;

	private ZAandroid a6 = new ZAandroid();

	public int opendevicestr(Context  env,int nDeviceType,String iCom,int iBaud,int nPackageSize,int iDevNum)
	{
		int status = -1;
		int DEV_ADDR = 0xffffffff;
		byte[] pPassword = new byte[4];
        if(isbus!=0)return -101;
        isbus = 1;
		status = a6.ZAZOpenDeviceExstr(-1,nDeviceType,iCom,iBaud,nPackageSize,iDevNum);
		if(status == 0 ){
			//status = a6.ZAZVfyPwd(DEV_ADDR, pPassword) ;
			isonline = 1;
            status =  1;
		}
		else
        {
            isonline = 0;
            status = -5;
        }
        isbus = 0;
		return status;

	}

	public int opendevice(Context  env,int nDeviceType,int iCom,int iBaud)
	{
		if (isonline == 1) return 100;
		int status = -1;
		isusbfinshed = 3;
        if(isbus!=0)return -101;
        isbus = 1;
        Log.i(TAG,"use by not root ");
        device = null;
        isusbfinshed  = 0;
        int fd = 0;
        isusbfinshed = getrwusbdevices(env);
        //skipshow("watting a time");
        Log.i(TAG,"waiting user put root ");
        if(WaitForInterfaces() == false)  {
            status = -1;
        }
        else {
            fd = OpenDeviceInterfaces();
            if (fd == -1) {
                status = -2;
            }
            else {
                status = a6.ZAZOpenDevice(fd, nDeviceType, iCom, iBaud, 0, 0);
                if (status == 0) {
                    isonline = 1;
                    status = 1;
                }
                else{
                    status = -3;
                }
            }
        }
        isbus=0;
		return status;
	}

//	/*****************************
//	 *打开设备函数
//	 *参数：
//	 *nDeviceType 设备类型（ 1：串口设备，  2:无驱 UDISK 设备1,  12:无驱 UDISK设备2）
//	 *iCom : 0
//	 *iBaud: 6；
//	 *nPackageSize :2
//	 *iDevNum :0
//	 *返回值：
//	 *1 为成功，其它返回值请参考错误返回码
//	 *****************************/
//	public  int ZAZOpenDevice(int fd,int nDeviceType,int iCom,int iBaud,int nPackageSize/*=2*/,int iDevNum/*=0*/)
//	{
//	    if(isbus!=0)return -101;
//        isbus = 1;
//        int ret = a6.ZAZOpenDevice(fd,nDeviceType,iCom,iBaud,nPackageSize,iDevNum);
//        isbus = 0;
//        return ret;
//	}
	public  int ZAZSetImageSize(int imagesize)
	{   if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZSetImageSize(imagesize);
        isbus = 0;
        return ret;
	}

	/*******************************
	 *关闭设备函数
	 *参数：
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************/
	public int ZAZCloseDeviceEx() {
		if(isonline!=1)return -100;
		int ret = a6.ZAZCloseDeviceEx();
		if (ret ==1) isonline = 0;
		return ret;
	}
	/*********************************
	 *获取图像函数
	 *参数：
	 *nAddr： 0xffffffff
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ***********************************/
	public int ZAZGetImage(int nAddr)
	{
		if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZGetImage(nAddr);
        isbus = 0;
		return ret;
	}
	/*********************************
	 *生成特征码函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *iBufferID： 0x01、 0x02(电容/光学) 0x01、 0x02、 0x03、 0x04(刮擦)
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ***********************************/
	public int ZAZGenChar(int nAddr,int iBufferID)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
		int ret = a6.ZAZGenChar(nAddr,iBufferID);
        isbus = 0;
        return ret;

	}
	/*********************************
	 *5、精确比对函数（比对 CharBufferA 与 CharBufferB）
	 *参数：
	 *nAddr： 0xffffffff ；
	 *iScore：iscore[0] 比对后的分数值
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ***********************************/
	public int ZAZMatch(int nAddr,int[] iScore)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZMatch(nAddr,iScore);
        isbus = 0;
        return ret;
	}

	/*********************************
	 *6、搜索比对函数（以 CharBufferA 或 CharBufferB 中的特征文件搜索整个或部分指纹库）
	 *参数：
	 *nAddr： 0xffffffff ；
	 *iBufferID： 0x01、 0x02
	 *iStartPage：起始 ID；
	 *iPageNum：结束 ID；
	 *iMbAddress：iMbAddress[0]搜索成功后模板 ID 号；
	 *iscore：默认参数（NULL）
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ***********************************/
	public int ZAZSearch(int nAddr,int iBufferID, int iStartPage, int iPageNum, int[] iMbAddress,int[]  iscore)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZSearch(nAddr,iBufferID,iStartPage,iPageNum,iMbAddress,iscore);
        isbus = 0;
        return ret;
	}
	/*********************************
	 *7、合成模板函数(将 CharBufferA 与 CharBufferB 中的特征文件合并生成模板存于 ModelBuffer)
	 *参数：
	 *nAddr： 0xffffffff ；
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ***********************************/
	public  int ZAZRegModule(int nAddr)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZRegModule(nAddr);
        isbus = 0;
        return ret;
	}

	/********************************************************************
	 *8、存储模板函数(将 ModelBuffer 中的文件储存到 flash 指纹库中)
	 *参数：
	 *nAddr： 0xffffffff ；
	 *iBufferID： 0x01、 0x02
	 *iPageID：模板存储到指纹库中的 ID 号
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 *********************************************************************/
	public int ZAZStoreChar(int nAddr,int iBufferID, int iPageID)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZStoreChar(nAddr,iBufferID,iPageID);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *9、读出模板函数(从 flash 指纹库中读取一个模板到 ModelBuffer)
	 *参数：
	 *nAddr： 0xffffffff ；
	 *iBufferID：从指纹库中读出的模板所存放的特征缓冲区(0x01、 0x02)；
	 *iPageID：在指纹库中将要读出的指纹模板 ID 号
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZLoadChar(int nAddr,int iBufferID,int iPageID)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =  a6.ZAZLoadChar(nAddr,iBufferID,iPageID);
        isbus = 0;
        return ret;
	}

	/*********************************
	 *设置特征值大小函数
	 *参数：（默认512）
	 *
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ***********************************/
	public int ZAZSetCharLen(int charLen)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZSetCharLen(charLen);
        isbus = 0;
        return ret;
	}

	/*******************************************************************
	 *10、上传特征函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *iBufferID：将要上传的模板特征缓冲区(0x01、 0x02)；
	 *pTemplet：指纹模板数据上传存放的地址；
	 *iTemplet[0]：指纹模板数据长度
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZUpChar(int nAddr,int iBufferID, byte[] pTemplet,int[] iTempletLength)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =  a6.ZAZUpChar(nAddr,iBufferID,pTemplet,iTempletLength);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *11、下载特征函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *iBufferID：将指纹模板数据下载到的特征缓冲区(0x01、 0x02)；
	 *pTemplet：下载的指纹模板数据地址；
	 *iTempletLength：下载的指纹模板数据长度
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZDownChar(int nAddr,int iBufferID, byte[] pTemplet, int iTempletLength)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZDownChar(nAddr,iBufferID,pTemplet,iTempletLength);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *12、上传图像函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *pImageData：上传的指纹图像数据地址；
	 *pImageData[0]：上传的指纹图像数据长度
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZUpImage(int nAddr,byte[] pImageData,int[] iTempletLength)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =  a6.ZAZUpImage(nAddr,pImageData,iTempletLength);
        isbus = 0;
        return ret;
	}

	public int ZAZUpImagenew(int nAddr,byte[] pImageData,int[] iTempletLength)
	{	if(isonline!=1)return -100;
		if(isbus!=0)return -101;
		isbus = 1;
		int ret =  a6.ZAZUpImagenew(nAddr,pImageData,iTempletLength);
		isbus = 0;
		return ret;
	}
	/*******************************************************************
	 *13、下载图像函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *pImageData：下载的指纹图像数据地址；
	 *iImageLength：下载的指纹图像数据长度
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZDownImage(int nAddr,byte[] pImageData, int iLength)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZDownImage(nAddr,pImageData,iLength);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *14、图像数据保存成 BMP 图片函数
	 *参数：
	 *pImageData：需保存的指纹图像数据地址；
	 *pImageFile(str)：保存的指纹图像文件名
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/

	public int ZAZImgData2BMP(byte[] pImgData,String str)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =  a6.ZAZImgData2BMP(pImgData,str);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *15、读取 BMP 图像提取图像数据函数
	 *参数：
	 *pImageData：读取后的指纹图像数据地址；
	 *pImageFile(str)：读取的指纹图像文件名；
	 *pnImageLen：指纹图像数据长度
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZGetImgDataFromBMP(String str,byte[] pImageData,int[] pnImageLen)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =  a6.ZAZGetImgDataFromBMP(str,pImageData,pnImageLen);
        isbus = 0;
        return ret;
	}

	/*******************************************************************
	 *16、删除模板函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *iStartPageID：需删除指纹区域的起始 ID 号；
	 *nDelPageNum：需删除的从起始 ID 开始的模板个数
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZDelChar(int nAddr,int iStartPageID,int nDelPageNum)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =  a6.ZAZDelChar(nAddr,iStartPageID,nDelPageNum);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *17、清空指纹库函数
	 *参数：
	 *nAddr： 0xffffffff
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZEmpty(int nAddr)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZEmpty(nAddr);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *18、读参数表函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *pParTable：系统参数数据的存放地址
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZReadParTable(int nAddr,byte[] pParTable)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZReadParTable(nAddr,pParTable);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *19、快速比对函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *iBufferID： (0x01、 0x02)以此特征缓冲区的指纹特征文件比对指纹模板库；
	 *iStartPage：比对的指纹模板库起始 ID；
	 *iPageNum：从起始 ID 开始将要比对的指纹库的指纹模板个数；
	 *iMbAddress：id_iscore[0]比对成功后返回的比对成功 ID 号； id_iscore[1](iscore)：默认值
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public  int ZAZHighSpeedSearch(int nAddr,int iBufferID, int iStartPage, int iPageNum, int[] iMbAddress)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
	    int[] isorce = new int[1];
       // return a6.ZAZSearch(nAddr,iBufferID,iStartPage,iPageNum,iMbAddress,isorce);
        int ret = a6.ZAZHighSpeedSearch(nAddr,iBufferID,iStartPage,iPageNum,iMbAddress);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *20、读取有效模板数量函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *iMbNum：模板个数
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public  int ZAZTemplateNum(int nAddr,int[] iMbNum)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =  a6.ZAZTemplateNum(nAddr,iMbNum);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *21、设置设备握手口令函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *pPassword：握手口令数据
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZSetPwd(int nAddr,byte[] pPassword)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZSetPwd(nAddr,pPassword);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *22、验证设备握手口令函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *pPassword：握手口令数据
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZVfyPwd(int nAddr,byte[] pPassword)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =  a6.ZAZVfyPwd(nAddr,pPassword);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *23、读取记事本
	 *参数：
	 *nAddr： 0xffffffff ；
	 *nPage：记事本信息页（共 512 页，每页 32 字节）；
	 *UserContent：记事本数据信息地址
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZReadInfo(int nAddr,int nPage,byte[] UserContent)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =  a6.ZAZReadInfo(nAddr,nPage,UserContent);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *24、写入记事本
	 *参数：
	 *nAddr： 0xffffffff ；
	 *nPage：记事本信息页（共 512 页，每页 32 字节）；
	 *UserContent：需写入的记事本数据信息地址
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZWriteInfo(int nAddr,int nPage,byte[] UserContent)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =  a6.ZAZWriteInfo(nAddr,nPage,UserContent);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *25、设置波特率函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *nBaudNum：需设置的波特率大小（9600-57600）
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZSetBaud(int nAddr,int nBaudNum)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZSetBaud(nAddr,nBaudNum);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *26、设置安全等级函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *nLevel：需设置的安全等级大小（1-5）
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZSetSecurLevel(int nAddr,int nLevel)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZSetSecurLevel(nAddr,nLevel);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *27、设置数据包大小函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *nSize：需设置的数据包大小（32/64/128/256）
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZSetPacketSize(int nAddr,int nSize)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZSetPacketSize(nAddr,nSize);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *28、指纹数据上传并生成一个 DAT 文件函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *iBufferID： (0x01、 0x02)上传的指纹特征缓冲区；
	 *pFileName：特征文件名
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZUpChar2File(int nAddr,int iBufferID, byte[] pFileName)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =  a6.ZAZUpChar2File(nAddr,iBufferID,pFileName);
        isbus = 0;
        return ret;
	}

	/*******************************************************************
	 *29、 DAT 文件下载函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *iBufferID： (0x01、 0x02)下载到指纹模块设备的指纹特征缓冲区；
	 *pFileName：下载的特征文件名
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZDownCharFromFile(int nAddr,int iBufferID, byte[] pFileName)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =  a6.ZAZDownCharFromFile(nAddr,iBufferID,pFileName);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *30、获取随机数函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *pRandom：随机数存放地址
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZGetRandomData(int nAddr,byte[] pRandom)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =   a6.ZAZGetRandomData(nAddr,pRandom);
        isbus = 0;
        return ret;
	}

	/*******************************************************************
	 *31、设置芯片地址函数
	 *参数：
	 *nAddr： 0xffffffff ；
	 *pChipAddr：需设置的芯片地址数据
	 *返回值：
	 *0 为成功，其它返回值请参考错误返回码
	 ********************************************************************/
	public int ZAZSetChipAddr(int nAddr,byte[] pChipAddr)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret =  a6.ZAZSetChipAddr(nAddr,pChipAddr);
        isbus = 0;
        return ret;
	}

	/**************BT_REV******************/
	public int ZAZBT_rev(byte[] pTemplet, int iTempletLength)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZBT_rev(pTemplet,iTempletLength);
        isbus = 0;
        return ret;
	}

	/*******************************************************************
	 *
	 *获取版本号
	 *
	 ********************************************************************/
	public int ZAZReadInfPage(int nAddr,byte[] pVersion)
	{	if(isonline!=1)return -100;
        if(isbus!=0)return -101;
        isbus = 1;
        int ret = a6.ZAZReadInfPage(nAddr,pVersion);
        isbus = 0;
        return ret;
	}
	/*******************************************************************
	 *
	 *获取指纹列表
	 *
	 ********************************************************************/

	public int ZAZReadIndexTable(int nAddr,int[] fpdb)
	{	if(isonline!=1)return -100;
		if(isbus!=0)return -101;
		isbus = 1;
		int ret = -1;
		byte temp=0;
		byte ttt=0;
		int fpno=0;
		byte[] pParTable = new byte[32];
 		for (int moban = 0 ;moban<4;moban++) {
			ret = a6.ZAZReadIndexTable(nAddr,moban, pParTable);
			if(ret == 0)
			{
 				for(int i=0;i<32;i++)
 				{
 					for (int j = 0; j < 8;j++) {
						temp = (byte) (pParTable[i]&0xff);
						ttt  = 	(byte)((0x01<<j)&0xff);
						temp &= ttt;
						if(temp!=0)
						{
 							fpdb[fpno++]=moban*32*8+i*8+j;
 						}
 					}
				}
			}
			else
			{
				break;
			}
 		}
		isbus = 0;
		return fpno;
	}

	public int LongDunD8800_CheckEuq()
	{
		Process process = null;
		DataOutputStream os = null;

		// for (int i = 0; i < 10; i++)
		// {
		String path = "/dev/bus/usb/00*/*";
		String path1 = "/dev/bus/usb/00*/*";
		File fpath = new File(path);
		Log.d("*** LongDun D8800 ***", " check path:" + path);
		// if (fpath.exists())
		// {
		String command = "chmod 777 " + path;
		String command1 = "chmod 777 " + path1;
		Log.d("*** LongDun D8800 ***", " exec command:" + command);
		try
		{
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command+"\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
			return 1;
		}
		catch (Exception e)
		{
			Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "+e.getMessage());
		}
		//  }
		//  }
		return 0;
	}


	private UsbManager mDevManager = null;
	private PendingIntent permissionIntent = null;
	private UsbInterface intf = null;
	private UsbDeviceConnection connection = null;
	private UsbDevice device = null;
	public int isusbfinshed = 0;
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	@SuppressLint("MutableImplicitPendingIntent")
	public int getrwusbdevices(Context env) {

		mDevManager = ((UsbManager) env.getSystemService(Context.USB_SERVICE));
		permissionIntent = PendingIntent.getBroadcast(env, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		env.registerReceiver(mUsbReceiver, filter);
		//this.registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
		HashMap<String, UsbDevice> deviceList = mDevManager.getDeviceList();
		if (true) Log.e(TAG, "news:" + "mDevManager");


		for (UsbDevice tdevice : deviceList.values()) {
			Log.i(TAG,	tdevice.getDeviceName() + " "+ Integer.toHexString(tdevice.getVendorId()) + " "
					+ Integer.toHexString(tdevice.getProductId()));
			if (tdevice.getVendorId() == 0x2109 && (tdevice.getProductId() == 0x7638))
			{
				Log.e(TAG, " 指纹设备准备好了 ");
				mDevManager.requestPermission(tdevice, permissionIntent);
				return 1;
			}
		}
		Log.e(TAG, "news:" + "mDevManager  end");
		return 2;
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			context.unregisterReceiver(mUsbReceiver);
			isusbfinshed = 0;
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (context) {
					device = (UsbDevice) intent	.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					Log.e("BroadcastReceiver","3333");
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							if (true) Log.e(TAG, "Authorize permission " + device);
							isusbfinshed = 1;
						}
					}
					else {
						if (true) Log.e(TAG, "permission denied for device " + device);
						device=null;
						isusbfinshed = 2;

					}
				}
			}
		}
	};

	private void Sleep(int times)
	{
		try {
			Thread.sleep(times);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean WaitForInterfaces() {

		while (device==null || isusbfinshed == 0) {
			Sleep(10);
			if(isusbfinshed == 2)break;
			if(isusbfinshed == 3)break;
		}
		if(isusbfinshed == 2)
			return false;
		if(isusbfinshed == 3)
			return false;
		return true;
	}

	public int OpenDeviceInterfaces() {
		UsbDevice mDevice = device;
		Log.d(TAG, "setDevice " + mDevice);
		int fd = -1;
		if (mDevice == null) return -1;
		connection = mDevManager.openDevice(mDevice);
		if (!connection.claimInterface(mDevice.getInterface(0), true)) return -1;

		if (mDevice.getInterfaceCount() < 1) return -1;
		intf = mDevice.getInterface(0);

		if (intf.getEndpointCount() == 0) 	return -1;

		if ((connection != null)) {
			if (true) Log.e(TAG, "open connection success!");
			fd = connection.getFileDescriptor();
			return fd;
		}
		else {
			if (true) Log.e(TAG, "finger device open connection FAIL");
			return -1;
		}
	}

}

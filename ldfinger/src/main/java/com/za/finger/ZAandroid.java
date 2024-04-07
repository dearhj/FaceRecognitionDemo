package com.za.finger;

public class ZAandroid {
	public  int ZAZOpenDevice(int fd,int nDeviceType,int iCom,int iBaud,int nPackageSize/*=2*/,int iDevNum/*=0*/)
	{
		int ret = -1;
		byte[] psw= new byte[4];
		ret = ZAZOpenDeviceEx(fd,2,iCom,iBaud,nPackageSize,iDevNum);
		if(ret == 0)
		{
			ret = ZAZVfyPwd(0xffffffff,psw);
		}
		if(ret!=0)
		{
			ret = ZAZOpenDeviceEx(fd,12,iCom,iBaud,nPackageSize,iDevNum);
			if(ret == 0)
			{
				ret = ZAZVfyPwd(0xffffffff,psw);
			}
		}
 		if(ret == 0)
 			return 0;
 		else
			return -2;
	}
	
	public native int ZAZOpenDeviceExstr(int fd,int nDeviceType,String iCom,int iBaud,int nPackageSize/*=2*/,int iDevNum/*=0*/);
	public native int ZAZOpenDeviceEx(int fd,int nDeviceType,int iCom,int iBaud,int nPackageSize/*=2*/,int iDevNum/*=0*/);
	public native int ZAZCloseDeviceEx();
	public native int ZAZSetImageSize(int imagesize);
	public native int ZAZGetImage(int nAddr);
	public native int ZAZGenChar(int nAddr,int iBufferID);
	public native int ZAZMatch(int nAddr,int[] iScore);
	public native int ZAZSearch(int nAddr,int iBufferID, int iStartPage, int iPageNum, int[] iMbAddress,int []iscore);
	public native int ZAZRegModule(int nAddr);
	public native int ZAZStoreChar(int nAddr,int iBufferID, int iPageID);
	public native int ZAZLoadChar(int nAddr,int iBufferID,int iPageID);
	public native int ZAZSetCharLen(int charLen);
	public native int ZAZUpChar(int nAddr,int iBufferID, byte[] pTemplet,int[] iTempletLength);
	public native int ZAZDownChar(int nAddr,int iBufferID, byte[] pTemplet, int iTempletLength);
	public native int ZAZUpImage(int nAddr,byte[] pImageData,int[] iTempletLength);
	public native int ZAZUpImagenew(int nAddr,byte[] pImageData,int[] iTempletLength);
	public native int ZAZDownImage(int nAddr,byte[] pImageData, int iLength);
	public native int ZAZImgData2BMP(byte[] pImgData,String str);
	public native int ZAZGetImgDataFromBMP(String str,byte[] pImageData,int[] pnImageLen);
	public native int ZAZDelChar(int nAddr,int iStartPageID,int nDelPageNum);
	public native int ZAZEmpty(int nAddr);
	public native int ZAZReadParTable(int nAddr,byte[] pParTable);
	public native int ZAZHighSpeedSearch(int nAddr,int iBufferID, int iStartPage, int iPageNum, int[] id_iscore);
	public native int ZAZTemplateNum(int nAddr,int[] iMbNum);
	public native int ZAZSetPwd(int nAddr,byte[] pPassword);
	public native int ZAZVfyPwd(int nAddr,byte[] pPassword);
	public native int ZAZReadInfo(int nAddr,int nPage,byte[] UserContent);
	public native int ZAZWriteInfo(int nAddr,int nPage,byte[] UserContent);
	public native int ZAZSetBaud(int nAddr,int nBaudNum);
	public native int ZAZSetSecurLevel(int nAddr,int nLevel);
	public native int ZAZSetPacketSize(int nAddr,int nSize);
	public native int ZAZUpChar2File(int nAddr,int iBufferID, byte[] pFileName);
	public native int ZAZDownCharFromFile(int nAddr,int iBufferID, byte[] pFileName);
	public native int ZAZGetRandomData(int nAddr,byte[] pRandom);
	public native int ZAZSetChipAddr(int nAddr,byte[] pChipAddr);
	public native int ZAZBT_rev(byte[] pTemplet, int iTempletLength);
	public native int ZAZReadInfPage(int nAddr,byte[] pVersion);
	public native int ZAZReadIndexTable(int nAddr,int page,byte[] usercontent);
	static {
		System.loadLibrary("ZAandroid");
	}
}

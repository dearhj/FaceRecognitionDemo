package com.dk.usbNfc.Tool;

import java.util.Random;

public class UtilTool {
    public static byte[] mergeByte(byte[] source, byte[] s, int begin, int end) {
        byte[] bytes = new byte[source.length + end - begin];
        System.arraycopy(source, 0, bytes, 0, source.length);
        System.arraycopy(s, begin, bytes, source.length, end);

        return bytes;
    }

    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        b[0] = (byte) (temp & 0xff);
        temp = temp >> 8;
        b[1] = (byte) (temp & 0xff);

        return b;
    }

    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);
        short s1 = (short) (b[1] & 0xff);

        s1 <<= 8;
        s = (short) (s0 | s1);

        return s;
    }

    // 随机生成16位字符串
    public static String getRandomStr(int len) {
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
    
    //异或和校验
    public static byte bcc_check(byte[] bytes) {
    	byte bcc_sum = 0;
    	for ( byte theByte : bytes ) {
    		bcc_sum ^= theByte;
    	}
    	
    	return bcc_sum;
    }

    /**
     * @bieaf CRC-16 校验
     *
     * @param data 数据
     * @param crc   CRC
     * @return crc  返回CRC的值
     */
    public static int crc16(byte[] data, int crc) {
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







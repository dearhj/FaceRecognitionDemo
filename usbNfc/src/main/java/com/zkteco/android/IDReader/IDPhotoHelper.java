package com.zkteco.android.IDReader;

import android.graphics.Bitmap;


/**
 * Created by scarx on 2015/12/3.
 */
public class IDPhotoHelper {
    public static Bitmap Bgr2Bitmap(byte[] bgrbuf) {
//        int width = WLTService.imgWidth;
//        int height = WLTService.imgHeight;
//        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//        int row = 0, col = width-1;
//        for (int i = bgrbuf.length-1; i >= 3; i -= 3) {
//            int color = bgrbuf[i] & 0xFF;
//            color += (bgrbuf[i-1] << 8) & 0xFF00;
//            color += ((bgrbuf[i-2]) << 16) & 0xFF0000;
//            bmp.setPixel(col--, row, color);
//            if (col < 0) {
//                col = width-1;
//                row++;
//            }
//        }
//        return bmp;

        byte[] pBmpFile = new byte[38556];
        int pSex1;
        if (bgrbuf != null) {
            byte pName2;
            for(pSex1 = 0; pSex1 < 19278; ++pSex1) {
                pName2 = bgrbuf[pSex1];
                bgrbuf[pSex1] = bgrbuf['際' - pSex1];
                bgrbuf['際' - pSex1] = pName2;
            }

            int pNation1;
            for(pSex1 = 0; pSex1 < 126; ++pSex1) {
                for(pNation1 = 0; pNation1 < 153; ++pNation1) {
                    pName2 = bgrbuf[pNation1 + pSex1 * 102 * 3];
                    bgrbuf[pNation1 + pSex1 * 102 * 3] = bgrbuf[305 - pNation1 + pSex1 * 102 * 3];
                    bgrbuf[305 - pNation1 + pSex1 * 102 * 3] = pName2;
                }
            }

            System.arraycopy(bgrbuf, 0, pBmpFile, 0, 38556);

            int []colors = convertByteToColor(pBmpFile);

            return Bitmap.createBitmap(colors, 102, 126, Bitmap.Config.ARGB_8888);
        }

        return null;
    }

    public static final int[] convertByteToColor(byte[] data) {
        int var2;
        if ((var2 = data.length) == 0) {
            return null;
        } else {
            byte var3 = 0;
            if (var2 % 3 != 0) {
                var3 = 1;
            }

            int[] var4 = new int[var2 / 3 + var3];
            int var5;
            if (var3 == 0) {
                for(var5 = 0; var5 < var4.length; ++var5) {
                    var4[var5] = data[var5 * 3] << 16 & 16711680 | data[var5 * 3 + 1] << 8 & '\uff00' | data[var5 * 3 + 2] & 255 | -16777216;
                }
            } else {
                for(var5 = 0; var5 < var4.length - 1; ++var5) {
                    var4[var5] = data[var5 * 3] << 16 & 16711680 | data[var5 * 3 + 1] << 8 & '\uff00' | data[var5 * 3 + 2] & 255 | -16777216;
                }

                var4[var4.length - 1] = -16777216;
            }

            return var4;
        }
    }

}

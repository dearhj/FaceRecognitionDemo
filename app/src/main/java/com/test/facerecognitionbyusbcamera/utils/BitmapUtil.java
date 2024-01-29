package com.test.facerecognitionbyusbcamera.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;

public class BitmapUtil {

    public static Bitmap scaleBitmap(Bitmap origin, float scale) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    public static Bitmap cropBitmap(Bitmap bitmap, Rect box) {
        return Bitmap.createBitmap(bitmap, box.left, box.top, box.width(), box.height());
    }
}

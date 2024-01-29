package com.rockchip.iva;

import static com.rockchip.iva.RockIvaImage.TransformMode.NONE;

import android.graphics.Rect;

import java.util.List;

public class RockIva {

    public int init(String jsonStr) {
        long handle = native_init(jsonStr);
        if (handle == -1) {
            return -1;
        }
        mHandle = handle;
        return 0;
    }

    private RockIvaCallback mCallback_ = new RockIvaCallback() {
        @Override
        public void onResultCallback(String result, int execureState) {
            if (mCallback != null) {
                mCallback.onResultCallback(result, execureState);
            }
        }

        @Override
        public void onReleaseCallback(List<RockIvaImage> images) {
            if (mCallback != null) {
                mCallback.onReleaseCallback(images);
            }
        }
    };

    public int release() {
        return native_release(mHandle);
    }

    public int pushFrame(RockIvaImage image) {
        return native_pushFrame(mHandle, image);
    }

    public int setAnalyseFace(int id) {
        return native_setAnalyseFace(mHandle, id);
    }

    public static Rect convertRectRatioToPixel(int width, int height, Rect rect, RockIvaImage.TransformMode mode) {
        Rect rectPixel = new Rect();
        Rect transRect = transformRect(10000, 10000, rect, mode);
        rectPixel.left = width * transRect.left / 10000;
        rectPixel.top = height * transRect.top / 10000;
        rectPixel.right = width * transRect.right / 10000;
        rectPixel.bottom = height * transRect.bottom / 10000;
        return rectPixel;
    }

    public static Rect transformRect(int width, int height, Rect rect, RockIvaImage.TransformMode mode) {
        Rect r = new Rect();
        switch (mode) {
            case NONE:
                r.set(rect);
                break;
            case ROTATE_90:
                r.left = height - rect.bottom;
                r.top = rect.left;
                r.right = height - rect.top;
                r.bottom = rect.right;
                break;
            case ROTATE_180:
                r.left = width - rect.right;
                r.top = height - rect.bottom;
                r.right = width - rect.left;
                r.bottom = height - rect.top;
                break;
            case ROTATE_270:
                r.left = rect.top;
                r.top = width - rect.right;
                r.right = rect.bottom;
                r.bottom = width - rect.left;
                break;
            case FLIP_H:
                r.left = width - rect.right;
                r.top = rect.top;
                r.right = width - rect.left;
                r.bottom = rect.bottom;
                break;
            case FLIP_V:
                r.left = rect.left;
                r.top = height - rect.bottom;
                r.right = rect.right;
                r.bottom = height - rect.top;
                break;
        }
        return r;
    }

    public void setCallback(RockIvaCallback callback) {
        native_setcallback(mHandle, callback);
    }

    private RockIvaCallback mCallback = null;

    private long mHandle;

    static {
        System.loadLibrary("rockxjni");
    }

    native long native_init(String jsonStr);
    native int native_release(long handle);
    native int native_setcallback(long handle, RockIvaCallback callback);
    native int native_pushFrame(long handle, RockIvaImage image);
    native int native_setAnalyseFace(long handle, int id);
}

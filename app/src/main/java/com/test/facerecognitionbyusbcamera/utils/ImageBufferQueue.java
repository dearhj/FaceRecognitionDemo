package com.test.facerecognitionbyusbcamera.utils;

import android.util.Log;

import com.rockchip.iva.RockIvaImage;

public class ImageBufferQueue {
    private final static String TAG = "ImageBufferQueue";

    private ImageBuffer[] mQueueBuffer;
    private int mQueueBufferSize;
    private int mCurrentFreeBufferIndex;
    private int mCurrentUsingBufferIndex;

    public ImageBufferQueue(int bufferSize) {
        mCurrentFreeBufferIndex = -1;
        mCurrentUsingBufferIndex = -1;
        mQueueBufferSize = bufferSize;
        mQueueBuffer = new ImageBuffer[mQueueBufferSize];

        for (int i=0; i<mQueueBufferSize; ++i) {
            mQueueBuffer[i] = new ImageBuffer();
        }
    }

    public int init(int width, int height, RockIvaImage.PixelFormat format) {
        int ret = 0;
        for (int i=0; i<mQueueBufferSize; ++i) {
            ret = mQueueBuffer[i].init(width, height, format);
            if (ret != 0) {
                Log.e(TAG, "init buffer fail");
                return -1;
            }
        }
        return 0;
    }

    public void release() {
        for (int i=0; i<mQueueBufferSize; ++i) {
            mQueueBuffer[i].release();
            mQueueBuffer[i] = null;
        }
        mQueueBuffer = null;
    }

    public synchronized ImageBuffer getReadyBuffer() {

        int index = mCurrentUsingBufferIndex;

        for (int i=0; i<mQueueBufferSize; ++i) {
            ++index;

            if (index >= mQueueBufferSize) {
                index = 0;
            }

            if (mQueueBuffer[index].mStatus == ImageBuffer.STATUS_READY) {
                break;
            }
        }

        if ((index != mCurrentUsingBufferIndex) && (mQueueBuffer[index].mStatus == ImageBuffer.STATUS_READY)) {
            mCurrentUsingBufferIndex = index;
            mQueueBuffer[index].mStatus = ImageBuffer.STATUS_USING;

            return mQueueBuffer[index];
        }

        return null;
    }

    public synchronized void releaseBuffer(ImageBuffer buffer) {
        buffer.mStatus = ImageBuffer.STATUS_INVAILD;
    }

    public synchronized void releaseBuffer(RockIvaImage image) {
        for (ImageBuffer buffer : mQueueBuffer) {
            if (buffer.mImage.dataAddr == image.dataAddr &&
                    buffer.mImage.dataPhyAddr == image.dataPhyAddr &&
                    buffer.mImage.dataFd == image.dataFd) {
                buffer.mStatus = ImageBuffer.STATUS_INVAILD;
            }
        }
    }

    public  synchronized ImageBuffer getFreeBuffer() {
        boolean succ = false;
        int index = mCurrentFreeBufferIndex;

        for (int i=0; i<mQueueBufferSize; ++i) {
            ++index;

            if (index >= mQueueBufferSize) {
                index = 0;
            }

            if (mQueueBuffer[index].mStatus != ImageBuffer.STATUS_USING &&
                    mQueueBuffer[index].mStatus != ImageBuffer.STATUS_READY) {
                succ = true;
                break;
            }
        }

        if (succ) {
            mCurrentFreeBufferIndex = index;

            mQueueBuffer[index].mStatus = ImageBuffer.STATUS_INVAILD;
            return mQueueBuffer[index];
        } else {
            return null;
        }
    }
    public synchronized void postBuffer(ImageBuffer buffer) {
        buffer.mStatus = ImageBuffer.STATUS_READY;
    }

    public class ImageBuffer {
        static public final int STATUS_INVAILD = 0;
        static public final int STATUS_READY = 1;
        static public final int STATUS_USING = 2;

        public int mStatus;
        public RockIvaImage mImage;

        public ImageBuffer() {
            mStatus = STATUS_INVAILD;
        }

        public int init(int width, int height, RockIvaImage.PixelFormat format) {
            mImage = new RockIvaImage(width, height, format);
            return mImage.allocMem(RockIvaImage.MEMORY_TYPE_CPU);
        }

        public int release() {
            if (mImage != null) {
                mImage.freeMem();
            }
            return 0;
        }

        public void finalize() {

        }

    }
}

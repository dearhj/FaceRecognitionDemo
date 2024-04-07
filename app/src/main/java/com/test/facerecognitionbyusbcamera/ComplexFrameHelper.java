package com.test.facerecognitionbyusbcamera;

import java.util.concurrent.ArrayBlockingQueue;


public class ComplexFrameHelper {
    public static ArrayBlockingQueue<byte[]> complexUvcFrameQueue = new ArrayBlockingQueue<>(2);
    private static byte[] uvcRgbFrameBuffer = null;


    private static void makeComplexUvcFrame() {
        if (uvcRgbFrameBuffer != null) {
            if (complexUvcFrameQueue.remainingCapacity() > 0) {
                complexUvcFrameQueue.offer(uvcRgbFrameBuffer);
            }
            uvcRgbFrameBuffer = null;
        }
    }


    public static void addUvcRgbFrame(byte[] uvcRgbFrame) {
        synchronized (ComplexFrameHelper.class) {
            if (uvcRgbFrameBuffer == null) {
                uvcRgbFrameBuffer = uvcRgbFrame;
            }
            makeComplexUvcFrame();
        }
    }

    public static byte[] takeComplexUvcFrame() throws InterruptedException {
        return complexUvcFrameQueue.take();
    }

}

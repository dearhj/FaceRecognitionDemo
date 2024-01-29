package com.rockchip.iva;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class RockIvaImage implements Serializable {

    /**
     * 图像像素格式
     */
    public PixelFormat format;

    /**
     * 图像宽
     */
    public int width = 0;

    /**
     * 图像高
     */
    public int height = 0;

    /**
     * 图像数据虚地址
     */
    public long dataAddr = 0;

    /**
     * 图像dma buffer fd
     */
    public int dataFd = 0;

    /**
     * 图像物理地址
     */
    public long dataPhyAddr = 0;

    public int transformMode = 0;

    public int channelId = 0;

    public int frameId = 0;

    public RockIvaImage() {
    }

    public RockIvaImage(int width, int height, PixelFormat format) {
        this.width = width;
        this.height = height;
        this.format = format;
    }

    public RockIvaImage(int width, int height, int format, int frameId, int channelId, int transformMode,
                        long dataAddr, int dataFd, long dataPhyAddr) {
        this.setValues(width, height, format, frameId, channelId, transformMode, dataAddr, dataFd, dataPhyAddr);
    }

    public void setValues(int width, int height, int format, int frameId, int channelId, int transformMode,
                          long dataAddr, int dataFd, long dataPhyAddr) {
        this.width = width;
        this.height = height;
        this.format = PixelFormat.get(format);
        this.dataAddr = dataAddr;
        this.dataFd = dataFd;
        this.dataPhyAddr = dataPhyAddr;
        this.frameId = frameId;
        this.channelId = channelId;
        this.transformMode = transformMode;
    }

    public int setImageData(byte[] data) {
        return set_image_data(this, data);
    }

    public byte[] getImageData() {
        int size = getSize();
        byte[] data = new byte[size];
        int ret = get_image_data(data);
        if (ret != 0) {
            return null;
        }
        return data;
    }

    public int getSize() {
        return get_image_size();
    }

    public int allocMem(int type) {
        return alloc_image_memory(this, type);
    }

    public int freeMem() {
        return free_image_memory(this);
    }

    public int write(String path) {
        return write_image(path);
    }

    public int release() {
        return release_image();
    }

    public static RockIvaImage read(String path) {
        RockIvaImage image = new RockIvaImage();
        int ret = read_image(path, image);
        if (ret != 0) {
            return null;
        }
        return image;
    }

    public static RockIvaImage parse(String jsonStr) {
        return parse_image(jsonStr);
    }

    /**
     * 获取图像像素格式索引值
     * @return 图像像素格式索引值
     */
    public int getPixelFormatIndex() {
        return this.format.index;
    }

    @NonNull
    @Override
    public String toString() {
        String format = String.format("width=%d height=%d format=%s dataAddr=%d dataFd=%d dataPhyAddr=%d",
                width, height, this.format, dataAddr, dataFd, dataPhyAddr);
        return format;
    }

    /**
     * 图像旋转模式枚举值
     */
    public enum TransformMode {
        NONE(0),        //< 不进行旋转操作
        FLIP_H(1),      //< 水平翻转
        FLIP_V(2),      //< 垂直翻转
        ROTATE_90(4),   //< 顺时针旋转90度
        ROTATE_180(3),  //< 顺时针旋转180度
        ROTATE_270(7);  //< 顺时针旋转270度

        public int index;

        TransformMode(int index) {
            this.index = index;
        }

        public static TransformMode get(int index) {
            for(TransformMode mode : TransformMode.values()){
                if(index == mode.index){
                    return mode;
                }
            }
            return null;
        }
    }

    /**
     * 图像像素格式枚举
     */
    public enum PixelFormat {
        GRAY8(0),           ///< Gray8
        RGB888(1),          ///< RGB888
        BGR888(2),          ///< BGR888
        RGBA8888(3),        ///< RGBA8888
        BGRA8888(4),        ///< BGRA8888
        YUV420P_YU12(5),    ///< YUV420P YU12: YYYYYYYYUUVV
        YUV420P_YV12(6),    ///< YUV420P YV12: YYYYYYYYVVUU
        YUV420SP_NV12(7),   ///< YUV420SP NV12: YYYYYYYYUVUV
        YUV420SP_NV21(8);   ///< YUV420SP NV21: YYYYYYYYVUVU

        public int index;

        PixelFormat(int index) {
            this.index = index;
        }

        public static PixelFormat get(int index) {
            for(PixelFormat fmt : PixelFormat.values()){
                if(index == fmt.index){
                    return fmt;
                }
            }
            return null;
        }
    }

    public final static int MEMORY_TYPE_CPU = 0;
    public final static int MEMORY_TYPE_DMA = 1;

    static {
        System.loadLibrary("rockxjni");
    }

    native int alloc_image_memory(RockIvaImage image, int type);
    native int free_image_memory(RockIvaImage image);
    native int set_image_data(RockIvaImage image, byte[] data);
    native int get_image_size();
    native int get_image_data(byte[] data);
    native int write_image(String path);
    static native int read_image(String path, RockIvaImage image);
    native int release_image();
    static native RockIvaImage parse_image(String jsonStr);
}

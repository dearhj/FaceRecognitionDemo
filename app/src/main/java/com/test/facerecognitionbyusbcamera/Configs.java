package com.test.facerecognitionbyusbcamera;

import android.os.Environment;

public class Configs {

    public final static String TAG = "IvaFaceApp";

    /**
     * 摄像头ID
     */
    public final static int CAMERA_ID = 1;

    /**
     * 摄像头图像宽度
     */
    public final static int CAMERA_IMAGE_WIDTH = 1920;

    /**
     * 摄像头图像高度
     */
    public final static int CAMERA_IMAGE_HEIGHT = 1080;

    /**
     * 人脸注册功能初始化IVA配置文件(如果该路径文件存在会优先raw文件加载)
     */
    public final static String IVA_FACE_REG_CONFIG_JSON_PATH = Environment.getExternalStorageDirectory()+ "/Android/data/iva/iva_face_reg.json";

    /**
     * 人脸注册初始化IVA配置文件（在res/raw目录下）
     */
    public final static int IVA_FACE_REG_CONFIG_JSON_RES_ID = R.raw.iva_face_reg;

    /**
     * 人脸识别初始化IVA配置文件(如果该路径文件存在会优先raw文件加载)
     */
    public final static String IVA_FACE_RECOG_CONFIG_JSON_PATH = Environment.getExternalStorageDirectory()+ "/Android/data/iva/iva_face.json";

    /**
     * 人脸识别初始化IVA配置文件（在res/raw目录下）
     */
    public final static int IVA_FACE_RECOG_CONFIG_JSON_RES_ID = R.raw.iva_face;

    /**
     * 人脸库数据存放目录（需要确保有读写权限）
     */
    public final static String IVA_FACE_DATA_DIR_PATH  = Environment.getExternalStorageDirectory()+"/Android/data/iva/face_data";

    /**
     * 人脸库名称
     */
    public final static String IVA_FACE_LIBRARY_NAME = "face";

    /**
     * 人脸搜索结果比对分数阈值（范围0~1：越高表示越严格）
     */
    public final static float IVA_FACE_RECOG_SCORE_THRESHOLD = 0.6f;
}

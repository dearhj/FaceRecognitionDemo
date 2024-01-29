package com.rockchip.iva.face;

public class RockIvaFaceQualityInfo {
    public int score;             /* 人脸质量分数(值范围0~100) */
    public int clarity;           /* 人脸清晰度(值范围0~100, 100表示最清晰) */
    public RockIvaAngle angle;         /* 人脸角度 */
    public int eyesScore;         /* 眼睛遮挡分数（值范围[0,100]，值越低表示遮挡越严重） */
    public int noseScore;         /* 鼻子遮挡分数（值范围[0,100]，值越低表示遮挡越严重） */
    public int mouthScore;        /* 嘴巴遮挡分数（值范围[0,100]，值越低表示遮挡越严重） */
    public int faceScore;         /* 人脸分数（值范围[0,100]）*/
}

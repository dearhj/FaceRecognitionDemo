package com.rockchip.iva.face;

import android.graphics.Rect;

public class RockIvaFaceInfo {
    public int objId;                                       /* 目标ID[0,2^32) */
    public int frameId;                                     /* 人脸所在帧序号 */
    public Rect faceRect;                                   /* 人脸区域原始位置 */
    public RockIvaFaceQualityInfo faceQuality;              /* 人脸质量信息 */
    public int faceState;                                   /* 人脸状态 */
}

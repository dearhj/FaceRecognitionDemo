package com.rockchip.iva.face;

import android.graphics.Rect;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.rockchip.iva.RockIva;
import com.rockchip.iva.common.RockIvaObjectInfo;

public class RockIvaHeadInfo {
    public int objId;                                       /* 目标ID[0,2^32) */
    public int frameId;                                     /* 结果所在帧序号 */
    public int detScore;                                    /* 检测分数 */
    public Rect headRect;                                   /* 人头原始位置 */
    public RockIvaFaceQualityInfo faceQuality;              /* 人脸质量信息 */
    public RockIvaFacePoseState facePoseState;                               /* 人脸状态 */
    public RockIvaObjectInfo face;                          /* 人脸 */

    public static RockIvaHeadInfo parse(String json_str) {
        RockIvaHeadInfo object = JSONObject.parseObject(json_str, RockIvaHeadInfo.class);
        return object;
    }

    public enum RockIvaFacePoseState {
        ROCKIVA_FACE_POSE_STATE_NONE(0),                   /* 人脸坐姿未知 */
        ROCKIVA_FACE_POSE_STATE_NORMAL(1),                 /* 人脸坐姿正常 */
        ROCKIVA_FACE_POSE_STATE_HUNCHBACK(2),              /* 人脸坐姿驼背、趴下、低头 */
        ROCKIVA_FACE_POSE_STATE_UP(3),                     /* 人脸坐姿仰头 */
        ROCKIVA_FACE_POSE_STATE_SIDE(4),                   /* 人脸坐姿侧脸 */
        ROCKIVA_FACE_POSE_STATE_TILT(5);                   /* 人脸坐姿歪头 */

        public int index;

        RockIvaFacePoseState(int index) {
            this.index = index;
        }

        public static RockIvaFacePoseState get(int index) {
            for(RockIvaFacePoseState v : RockIvaFacePoseState.values()){
                if(index == v.index){
                    return v;
                }
            }
            return null;
        }
    }
}

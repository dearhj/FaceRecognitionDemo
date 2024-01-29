package com.rockchip.iva.common;

import android.graphics.Rect;

public class RockIvaObjectInfo {
    /**
     * 目标ID[0,2^32)
     */
    public int objId;

    /**
     * 所在帧序号
     */
    public int frameId;

    /**
     * 目标检测分数 [1-100]
     */
    public int score;

    /**
     * 目标区域框 (万分比)
     */
    public Rect rect;

    /**
     * 目标类别
     */
    public RockIvaObjectType type;

    public enum RockIvaObjectType {
        ROCKIVA_OBJECT_TYPE_NONE(0),           /* 未知 */
        ROCKIVA_OBJECT_TYPE_PERSON(1),         /* 行人 */
        ROCKIVA_OBJECT_TYPE_VEHICLE(2),        /* 机动车 */
        ROCKIVA_OBJECT_TYPE_NON_VEHICLE(3),    /* 非机动车 */
        ROCKIVA_OBJECT_TYPE_FACE(4),           /* 人脸 */
        ROCKIVA_OBJECT_TYPE_HEAD(5),           /* 人头 */
        ROCKIVA_OBJECT_TYPE_PET(6),            /* 宠物(猫/狗) */
        ROCKIVA_OBJECT_TYPE_MOTORCYCLE(7),     /* 电瓶车 */
        ROCKIVA_OBJECT_TYPE_BICYCLE(8),        /* 自行车 */
        ROCKIVA_OBJECT_TYPE_PLATE(9),          /* 车牌 */
        ROCKIVA_OBJECT_TYPE_BABY(10);          /* 婴幼儿 */

        public int index;

        RockIvaObjectType(int index) {
            this.index = index;
        }

        public static RockIvaObjectInfo.RockIvaObjectType get(int index) {
            for(RockIvaObjectInfo.RockIvaObjectType type : RockIvaObjectInfo.RockIvaObjectType.values()){
                if(index == type.index){
                    return type;
                }
            }
            return null;
        }
    };
}

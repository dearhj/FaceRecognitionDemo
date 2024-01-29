/****************************************************************************
 *
 *    Copyright (c) 2022 by Rockchip Corp.  All rights reserved.
 *
 *    The material in this file is confidential and contains trade secrets
 *    of Rockchip Corporation. This is proprietary information owned by
 *    Rockchip Corporation. No part of this work may be disclosed,
 *    reproduced, copied, transmitted, or used in any way for any purpose,
 *    without the express written permission of Rockchip Corporation.
 *
 *****************************************************************************/

#ifndef __ROCKIVA_PLATE_API_H__
#define __ROCKIVA_PLATE_API_H__

#include "rockiva_common.h"

#ifdef __cplusplus
extern "C" {
#endif
/* ------------------------------------------------------------------ */

#define ROCKIVA_PLATE_MAX_NUM (10)      /* 车牌最大数目 */
#define ROCKIVA_PLATE_MAX_CHAR_NUM (20) /* 车牌最大长度 */

extern const char *const ROCKIVA_PLATE_CODE[80];

/* ---------------------------类型定义----------------------------------- */

/* 车牌类型 */
typedef enum {
    PLATE_TYPE_NONE = 0,      /* 无车牌 */
    PLATE_TYPE_LARGE_CAR,     /* 大型汽车号牌 */
    PLATE_TYPE_SMALL_CAR,     /* 小型汽车号牌 */
    PLATE_TYPE_EMABSSY_CAR,   /* 使馆汽车号牌 */
    PLATE_TYPE_CONSULATE_CAR, /* 领馆汽车号牌 */
    PLATE_TYPE_TRAILER,       /* 挂车号牌 */
    PLATE_TYPE_COACH_CAR,     /* 教练车号牌 */
    PLATE_TYPE_POLICE_CAR,    /* 警车号牌 */
    PLATE_TYPE_HONGKONG,      /* 香港出入境号牌 */
    PLATE_TYPE_MACAO,         /* 澳门出入境号牌 */
    PLATE_TYPE_ARMED_POLICE,  /* 武警号牌 */
    PLATE_TYPE_PLA,           /* 军队号牌 */
    PLATE_TYPE_NEW_ENERGY,    /* 新能源号牌 */
    PLATE_TYPE_OTHER,         /* 其它号牌 */
} RockIvaPlateType;

/* 车牌颜色 */
typedef enum {
    PLATE_COLOR_UNKNOWN = 0,  /* 车牌颜色未知 */
    PLATE_COLOR_BLUE,         /* 蓝牌 */
    PLATE_COLOR_YELLOW,       /* 黄牌 */
    PLATE_COLOR_GREEN,        /* 绿牌 */
    PLATE_COLOR_BLACK,        /* 黑牌 */
    PLATE_COLOR_WHITE         /* 白牌 */
} RockIvaPlateColor;

/* 机动车车身颜色 */
typedef enum {
    VEHICLE_COLOR_UNKNOWN = 0,/* 未知 */
    VEHICLE_COLOR_BLACK,      /* 黑色 */
    VEHICLE_COLOR_BLUE,       /* 蓝色（蓝、青蓝） */
    VEHICLE_COLOR_BROWN,      /* 棕色 */
    VEHICLE_COLOR_GRAY,       /* 灰色（灰、银、深灰） */
    VEHICLE_COLOR_YELLOW,     /* 黄色（黄、橘、金） */
    VEHICLE_COLOR_GREEN,      /* 绿色 */
    VEHICLE_COLOR_PURPLE,     /* 紫色 */
    VEHICLE_COLOR_RED,        /* 红色 */
    VEHICLE_COLOR_WHITE       /* 白色 */
} RockIvaVehicleColor;

/* 机动车类型 */
typedef enum {
    VEHICLE_TYPE_UNKNOWN = 0, /* 未知 */
    VEHICLE_TYPE_BUS,         /* 客车，包含大客车、中客车、公交车 */
    VEHICLE_TYPE_SEDAN,       /* 轿车，包含小轿车、掀背车、微型车、跑车 */
    VEHICLE_TYPE_VAN,         /* 面包车，包含面包车，MPV */
    VEHICLE_TYPE_SUV,         /* SUV */
    VEHICLE_TYPE_PICKUP,      /* 皮卡车 */
    VEHICLE_TYPE_TRUCK        /* 货车，包含大货车、中货车、小货车、厢式货车 */
} RockIvaVehicleType;

/* 机动车朝向 */
typedef enum {
    VEHICLE_ORIENT_UNKNOWN = 0, /* 未知 */
    VEHICLE_ORIENT_FRONT,       /* 正面 */
    VEHICLE_ORIENT_BACK,        /* 背面 */
    VEHICLE_ORIENT_SIDE         /* 侧面 */
} RockIvaVehicleOrient;

/* ---------------------------规则配置----------------------------------- */

/* 车牌识别业务初始化参数配置 */
typedef struct
{
    uint16_t vehicleMinSize;    /* 机动车车身最小宽度（万分比[0-10000] */
    uint16_t plateMinSize;      /* 车牌最小宽度（万分比[0-10000] */
    uint16_t plateMinScore;     /* 车牌识别字符最小分数(0-99) */
    uint8_t mode;               /* 运行模式（0: 单帧模式; 1: 跟踪模式,内部会记录车辆id的识别,已有识别结果的车辆不会再返回结果） */
    RockIvaAreas detectAreas;   /* 检测区域 (对每个检测区域内最大的车辆进行识别) */
} RockIvaPlateTaskParam;

/* -------------------------- 算法处理结果 --------------------------- */

/* 机动车属性 */
typedef struct 
{
    RockIvaVehicleType type;                  /* 机动车类型 */
    RockIvaVehicleColor color;                /* 机动车车身颜色 */
    RockIvaVehicleOrient orient;              /* 机动车朝向 */
} RockIvaVehicleAttribute;

/* 车牌识别结构体 */
typedef struct
{
    uint32_t vehicleId;                              /* 车牌对应的机动车跟踪Id */
    int plateCode[ROCKIVA_PLATE_MAX_CHAR_NUM];       /* 车牌字符 */
    int plateScore[ROCKIVA_PLATE_MAX_CHAR_NUM];      /* 车牌字符分数[0-100] */
    int plateLen;                                    /* 车牌字符长度 */
    RockIvaRectangle plateRect;                      /* 车牌坐标 */
    RockIvaPlateType plateType;                      /* 车牌类型 */
    RockIvaPlateColor plateColor;                    /* 车牌颜色 */
    RockIvaRectangle vehicleRect;                    /* 机动车坐标 */
    RockIvaVehicleAttribute vehicleAttr;             /* 机动车属性 */
} RockIvaPlateInfo;

/* 车牌识别处理结果 */
typedef struct
{
    uint32_t frameId;                                  /* 帧ID */
    uint32_t channelId;                                /* 通道号 */
    RockIvaImage frame;                                /* 对应的输入图像帧 */
    uint32_t objNum;                                   /* 车牌个数 */
    RockIvaPlateInfo plateInfo[ROCKIVA_PLATE_MAX_NUM]; /* 各车牌识别结果 */
} RockIvaPlateResult;

/**
 * @brief 车牌识别结果回调函数
 *
 * result 结果
 * status 状态码
 * userdata 用户自定义数据
 */
typedef void (*RockIvaPlateCallback)(const RockIvaPlateResult* result, const RockIvaExecuteStatus status,
                                     void* userdata);

/**
 * @brief 初始化
 *
 * @param handle [INOUT] 初始化完成的handle
 * @param initParams [IN] 初始化参数
 * @param resultCallback [IN] 回调函数
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_PLATE_Init(RockIvaHandle handle, const RockIvaPlateTaskParam* initParams,
                                  const RockIvaPlateCallback callback);

/**
 * @brief 运行时重新配置(重新配置会导致内部的一些记录清空复位，但是模型不会重新初始化)
 * 
 * @param handle [IN] handle
 * @param initParams [IN] 配置参数
 * @return RockIvaRetCode 
 */
RockIvaRetCode ROCKIVA_PLATE_Reset(RockIvaHandle handle, const RockIvaPlateTaskParam* initParams);

/**
 * @brief 销毁
 *
 * @param handle [IN] handle
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_PLATE_Release(RockIvaHandle handle);

#ifdef __cplusplus
}
#endif /* end of __cplusplus */

#endif
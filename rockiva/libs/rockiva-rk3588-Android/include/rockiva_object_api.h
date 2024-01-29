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

#ifndef __ROCKIVA_OBJECT_API_H__
#define __ROCKIVA_OBJECT_API_H__

#include "rockiva_common.h"

#ifdef __cplusplus
extern "C" {
#endif

/* ---------------------------规则配置----------------------------------- */

/* 非机动车检测规则设置 */
typedef struct
{
    uint8_t enable;         /* 使能 1：开启；0：关闭 */
    uint8_t detScene;       /* 检测场景 0：平视角电瓶车检测；1：俯视角（电梯内）电瓶车检测 */
    uint8_t sensitivity;    /* 报警灵敏度[1,100] */
    uint32_t minSize;       /* 最小检测非机动车属性尺寸，小于该值过滤 */
    uint32_t filterType;    /* 过滤类型, 例如： ROCKIVA_OBJECT_TYPE_BITMASK(ROCKIVA_OBJECT_TYPE_MOTORCYCLE) */
    uint32_t frameRate;     /* 运行帧率 */
    RockIvaAreas roiAreas;  /* 有效区域 */
} RockIvaNonvehicleRule;

/* 火焰检测规则设置 */
typedef struct
{
    uint8_t enable;      /* 使能 1：开启；0：关闭 */
    uint8_t sensitivity; /* 报警灵敏度[1,100] */
    uint32_t minSize;    /* 最小火焰尺寸，小于该值过滤 */
    uint32_t frameRate;  /* 运行帧率 */
} RockIvaFireRule;

/* 视频结构化初始化参数配置 */
typedef struct
{
    RockIvaNonvehicleRule nonvehicleRule; /* 非机动车检测规则 */
    RockIvaFireRule fireRule;             /* 火焰检测规则 */
} RockIvaObjectTaskParams;

/* ------------------------------------------------------------------ */

/* -------------------------- 算法处理结果 --------------------------- */

/* 非机动车属性结构体 */
typedef struct
{
    RockIvaObjectType type; /* 非机动车类别 */
} RockIvaNonvehicleAttribute;

/* 单个目标非机动车属性检测基本信息 */
typedef struct
{
    uint32_t objId;                  /* 目标ID[0,2^32) */
    uint32_t frameId;                /* 非机动车所在帧序号 */
    RockIvaRectangle objectRect;     /* 非机动车区域原始位置 */
    RockIvaNonvehicleAttribute attr; /* 非机动车属性信息 */
    uint8_t alert;                   /* 1: 电瓶车告警 */
    uint8_t firstTrigger;            /* 1: 第一次触发电瓶车告警 */
} RockIvaObjectNonvehicleInfo;

/* 非机动车检测结果全部信息 */
typedef struct
{
    uint32_t frameId;                                            /* 输入图像帧ID */
    uint32_t channelId;                                          /* 通道号 */
    RockIvaImage frame;                                          /* 对应的输入图像帧 */
    uint32_t objNum;                                             /* 目标个数 */
    RockIvaObjectNonvehicleInfo objectInfo[ROCKIVA_MAX_OBJ_NUM]; /* 非机动检测信息 */
} RockIvaObjectNonvehicleAttrResult;

/* 火焰检测结果全部信息 */
typedef struct
{
    uint32_t frameId;                                  /* 输入图像帧ID */
    uint32_t channelId;                                /* 通道号 */
    RockIvaImage frame;                                /* 对应的输入图像帧 */
    uint32_t objNum;                                   /* 目标个数 */
    RockIvaObjectInfo objectInfo[ROCKIVA_MAX_OBJ_NUM]; /* 火焰检测信息 */
} RockIvaObjectFireResult;

/* ---------------------------------------------------------------- */

/**
 * @brief 电瓶车检测(梯控)结果回调函数
 *
 * result 结果
 * status 状态码
 * userdata 用户自定义数据
 */
typedef void (*ROCKIVA_OBJECT_NonvehicleResultCallback)(const RockIvaObjectNonvehicleAttrResult* result,
                                                        const RockIvaExecuteStatus status, void* userdata);

typedef void (*ROCKIVA_OBJECT_FireResultCallback)(const RockIvaObjectFireResult* result,
                                                  const RockIvaExecuteStatus status, void* userdata);

typedef struct
{
    ROCKIVA_OBJECT_NonvehicleResultCallback nonvehicleCallback;
    ROCKIVA_OBJECT_FireResultCallback fireCallback;
} RockIvaObjectResultCallback;

/**
 * @brief 初始化
 *
 * @param handle [INOUT] 初始化完成的handle
 * @param initParams [IN] 初始化参数
 * @param resultCallback [IN] 回调函数
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_OBJECT_Init(RockIvaHandle handle, const RockIvaObjectTaskParams* initParams,
                                   const RockIvaObjectResultCallback callback);

/**
 * @brief 运行时重新配置(重新配置会导致内部的一些记录清空复位，但是模型不会重新初始化)
 *
 * @param handle [IN] handle
 * @param initParams [IN] 配置参数
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_OBJECT_Reset(RockIvaHandle handle, const RockIvaObjectTaskParams* initParams);

/**
 * @brief 销毁
 *
 * @param handle [IN] handle
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_OBJECT_Release(RockIvaHandle handle);

#ifdef __cplusplus
}
#endif /* end of __cplusplus */

#endif /* end of #ifndef __ROCKIVA_OBJECT_API_H__ */
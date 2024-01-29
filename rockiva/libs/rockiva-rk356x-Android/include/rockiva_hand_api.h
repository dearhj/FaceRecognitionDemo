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

#ifndef __ROCKIVA_HAND_API_H__
#define __ROCKIVA_HAND_API_H__

#include "rockiva_common.h"

#ifdef __cplusplus
extern "C" {
#endif
/* ------------------------------------------------------------------ */
#define ROCKIVA_HAND_MAX_NUM (10) /* 手势识别最大数目 */

/* 手势类型 */
typedef enum {
    ROCKIVA_GESTURE_TYPE_UNKNOWN = 0,       /* 未知 */
    ROCKIVA_GESTURE_TYPE_ONE = 1,           /* 1 */
    ROCKIVA_GESTURE_TYPE_FIVE = 2,          /* 5 */
    ROCKIVA_GESTURE_TYPE_FIST = 3,          /* 0/拳头 */
    ROCKIVA_GESTURE_TYPE_OK = 4,            /* OK */
    ROCKIVA_GESTURE_TYPE_TWO = 6,           /* 2 */
    ROCKIVA_GESTURE_TYPE_THREE = 7,         /* 3 */
    ROCKIVA_GESTURE_TYPE_FOUR = 8,          /* 4 */
} RockIvaGestureType;

/* ---------------------------规则配置----------------------------------- */

/* 手势识别业务初始化参数配置 */
typedef struct
{
    uint8_t mode;           /* 运行模式 (0: 单帧模式; 1: 跟踪模式) */
    uint8_t sensitivity;    /* 手势识别灵敏度[1,100]，默认0为50灵敏度。灵敏度越高，越容易确定稳定手势，准确度越低。 */
    uint16_t stableTime;    /* 手势识别稳定时间，单位毫秒。默认0为2000ms。超过稳定时间后确定一个稳定手势。 */
} RockIvaHandTaskParam;

/* -------------------------- 算法处理结果 --------------------------- */
/* 手势识别结构体 */
typedef struct
{
    uint32_t handId;                        /* 跟踪Id */
    uint8_t handScore;                      /* 手部检测分数 [1-100] */
    uint8_t gestureScore;                   /* 当前帧手势分数 [1-100] */
    uint8_t stableGestureScore;             /* 稳定手势综合分数 [1-100] */
    uint8_t trigger;                        /* 稳定手势触发标志位 */
    RockIvaRectangle handRect;              /* 手部坐标 */
    RockIvaGestureType gestureType;         /* 当前帧手势类型 */
    RockIvaGestureType stableGestureType;   /* 稳定手势类型 */
} RockIvaHandInfo;

/* 手势识别处理结果 */
typedef struct
{
    uint32_t frameId;                               /* 帧ID */
    uint32_t channelId;                             /* 通道号 */
    RockIvaImage frame;                             /* 对应的输入图像帧 */
    uint32_t objNum;                                /* 目标个数 */
    RockIvaHandInfo handInfo[ROCKIVA_HAND_MAX_NUM]; /* 各目标手部信息 */
} RockIvaHandResult;

/**
 * @brief 手部结果回调函数
 *
 * result 结果
 * status 状态码
 * userdata 用户自定义数据
 */
typedef void (*RockIvaHandCallback)(const RockIvaHandResult* result, const RockIvaExecuteStatus status, void* userdata);

/**
 * @brief 初始化
 *
 * @param handle [INOUT] 初始化完成的handle
 * @param initParams [IN] 初始化参数
 * @param resultCallback [IN] 回调函数
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_HAND_Init(RockIvaHandle handle, const RockIvaHandTaskParam* initParams,
                                 const RockIvaHandCallback callback);

/**
 * @brief 运行时重新配置(重新配置会导致内部的一些记录清空复位，但是模型不会重新初始化)
 *
 * @param handle [IN] handle
 * @param initParams [IN] 配置参数
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_HAND_Reset(RockIvaHandle handle, const RockIvaHandTaskParam* initParams);

/**
 * @brief 销毁
 *
 * @param handle [IN] handle
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_HAND_Release(RockIvaHandle handle);

#ifdef __cplusplus
}
#endif /* end of __cplusplus */

#endif /* end of #ifndef __ROCKIVA_HAND_API_H__ */
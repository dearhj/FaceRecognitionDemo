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

#ifndef __ROCKIVA_POSE_API_H__
#define __ROCKIVA_POSE_API_H__

#include "rockiva_common.h"

#ifdef __cplusplus
extern "C" {
#endif
/* ------------------------------------------------------------------ */
#define ROCKIVA_POSE_MAX_NUM (10) /* 姿态识别最大数目 */

/* 车牌类型 */
typedef enum {
    POSE_TYPE_UNKNOWN = 0,     /* 未知 */
    POSE_TYPE_FALLDOWN = 1,    /* 摔倒 */
    POSE_TYPE_WALK = 2,        /* 行走 */
    POSE_TYPE_SQUATDOWN = 3,   /* 蹲下状态 */
    POSE_TYPE_SITDOWN = 4,     /* 坐下状态 */
    POSE_TYPE_DOWN = 5,        /* 下蹲/坐下 过程 */
    POSE_TYPE_STANDUP = 6,     /* 站起 */
    POSE_TYPE_STANDING = 7,    /* 站立 */
    POSE_TYPE_LYING = 8,    /* 躺/卧 */
} RockIvaPoseType;

/* ---------------------------规则配置----------------------------------- */

/* 姿态识别业务初始化参数配置 */
typedef struct
{
    uint8_t mode;             /* 运行模式 (0: 单帧模式; 1: 跟踪模式) */
    RockIvaAreas detectAreas; /* 有效检测区域 */
} RockIvaPoseTaskParam;

/* -------------------------- 算法处理结果 --------------------------- */
/* 姿态识别结构体 */
typedef struct
{
    uint32_t poseId;           /* 跟踪Id */
    RockIvaRectangle poseRect; /* 人形坐标 */
    RockIvaPoseType poseType;  /* 姿态类型 */
    uint8_t isFalldown;        /* 0: 非摔倒; 1: 进入摔倒标志 */
} RockIvaPoseInfo;

/* 姿态识别处理结果 */
typedef struct
{
    uint32_t frameId;                               /* 帧ID */
    uint32_t channelId;                             /* 通道号 */
    RockIvaImage frame;                             /* 对应的输入图像帧 */
    uint32_t objNum;                                /* 目标个数 */
    RockIvaPoseInfo poseInfo[ROCKIVA_POSE_MAX_NUM]; /* 各目标姿态识别信息 */
} RockIvaPoseResult;

/**
 * @brief 姿态识别结果回调函数
 *
 * result 结果
 * status 状态码
 * userdata 用户自定义数据
 */
typedef void (*RockIvaPoseCallback)(const RockIvaPoseResult* result, const RockIvaExecuteStatus status, void* userdata);

/**
 * @brief 初始化
 *
 * @param handle [INOUT] 初始化完成的handle
 * @param initParams [IN] 初始化参数
 * @param resultCallback [IN] 回调函数
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_POSE_Init(RockIvaHandle handle, const RockIvaPoseTaskParam* initParams,
                                 const RockIvaPoseCallback callback);

/**
 * @brief 运行时重新配置(重新配置会导致内部的一些记录清空复位，但是模型不会重新初始化)
 *
 * @param handle [IN] handle
 * @param initParams [IN] 配置参数
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_POSE_Reset(RockIvaHandle handle, const RockIvaPoseTaskParam* initParams);

/**
 * @brief 销毁
 *
 * @param handle [IN] handle
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_POSE_Release(RockIvaHandle handle);

#ifdef __cplusplus
}
#endif /* end of __cplusplus */

#endif
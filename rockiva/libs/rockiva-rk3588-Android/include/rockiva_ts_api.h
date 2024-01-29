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

#ifndef __ROCKIVA_TS_API_H__
#define __ROCKIVA_TS_API_H__

#include "rockiva_common.h"

#ifdef __cplusplus
extern "C" {
#endif

#define ROCKIVA_TS_MAX_RULE_NUM (4) /* 业务中最多规则数量 */

/* ---------------------------规则配置----------------------------------- */

/* 越界方向 */
typedef enum {
    ROCKIVA_LINE_DIRECT_CW = 0,  /* 顺时针方向，假设越界线方向从下往上，越界方向为从左往右 */
    ROCKIVA_LINE_DIRECT_CCW = 1, /* 逆时针方向，假设越界线方向从下往上，越界方向为从右往左 */
} RockIvaLineDirect;

/* 越界人流配置 */
typedef struct
{
    uint8_t ruleEnable;        /* 规则是否启用，1->启用，0->不启用 */
    uint32_t ruleID;           /* 规则ID（不能重复） */
    RockIvaLine line;          /* 越界线配置 */
    RockIvaLineDirect direct;  /* 越界方向 */
    RockIvaSize minObjSize;    /* 万分比表示 最小目标 */
    RockIvaSize maxObjSize;    /* 万分比表示 最大目标 */
    uint8_t sense;             /* 灵敏度,1~100 */
    RockIvaObjectType objType; /* 目标类型 */
} RockIvaTsLineRule;

/* 区域人流配置 */
typedef struct
{
    uint8_t ruleEnable;        /* 规则是否启用，1->启用，0->不启用 */
    uint32_t ruleID;           /* 规则ID（不能重复） */
    RockIvaArea area;          /* 区域配置 */
    RockIvaSize minObjSize;    /* 万分比表示 最小目标 */
    RockIvaSize maxObjSize;    /* 万分比表示 最大目标 */
    uint8_t sense;             /* 灵敏度,1~100 */
    RockIvaObjectType objType; /* 目标类型 */
} RockIvaTsAreaRule;

/* 人流统计初始化参数配置 */
typedef struct
{
    RockIvaTsLineRule lines[ROCKIVA_TS_MAX_RULE_NUM]; /* 区域人数统计 */
    RockIvaTsAreaRule areas[ROCKIVA_TS_MAX_RULE_NUM]; /* 拌线人数统计 */
} RockIvaTsTaskParams;

/* ------------------------------------------------------------------ */

/* -------------------------- 算法处理结果 --------------------------- */

/* 人数统计结果 */
typedef struct
{
    uint32_t count; /* 绊线人数 */
} RockIvaLineTrafficStatic;

typedef struct
{
    uint32_t count; /* 区域内人数 */
    uint32_t in;    /* 进区域人数 */
    uint32_t out;   /* 出区域人数 */
} RockIvaAreaTrafficStatic;

/* 人数统计结果 */
typedef struct
{
    RockIvaLineTrafficStatic lines[ROCKIVA_TS_MAX_RULE_NUM]; /* 绊线人数统计结果 */
    RockIvaAreaTrafficStatic areas[ROCKIVA_TS_MAX_RULE_NUM]; /* 区域人数统计结果 */
} RockIvaTrafficResult;

/* 人流统计结果全部信息 */
typedef struct
{
    uint32_t channelId;                             /* 通道号 */
    uint32_t frameId;                               /* 输入图像帧ID */
    RockIvaImage frame;                             /* 对应的输入图像帧 */
    RockIvaTrafficResult traffic;                   /* 人数统计结果 */
    uint32_t objNum;                                /* 目标个数 */
    RockIvaObjectInfo objInfo[ROCKIVA_MAX_OBJ_NUM]; /* 各目标检测信息 */
} RockIvaTsResult;

/* ---------------------------------------------------------------- */

/**
 * @brief 结果回调函数
 *
 * result 结果
 * status 状态码
 * userdata 用户自定义数据
 */
typedef void (*ROCKIVA_TS_ResultCallback)(const RockIvaTsResult* result, const RockIvaExecuteStatus status,
                                          void* userdata);

/**
 * @brief 初始化
 *
 * @param handle [INOUT] 初始化完成的handle
 * @param initParams [IN] 初始化参数
 * @param resultCallback [IN] 回调函数
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_TS_Init(RockIvaHandle handle, const RockIvaTsTaskParams* initParams,
                               const ROCKIVA_TS_ResultCallback resultCallback);

/**
 * @brief 销毁
 *
 * @param handle [in] handle
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_TS_Release(RockIvaHandle handle);

/**
 * @brief 复位（重设规则，并清除统计信息）
 *
 * @param handle [in] handle
 * @param params [in] 初始化参数
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_TS_Reset(RockIvaHandle handle, const RockIvaTsTaskParams* params);

/**
 * @brief 复位规格计数
 *
 * @param handle [in] handle
 * @param rule_id [in] 规则ID（注意创建时候不要重复）
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_TS_ResetCount(RockIvaHandle handle, int rule_id);

#ifdef __cplusplus
}
#endif /* end of __cplusplus */

#endif /* end of #ifndef __ROCKIVA_TS_API_H__ */
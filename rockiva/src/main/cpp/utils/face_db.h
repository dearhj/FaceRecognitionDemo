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

#ifndef _FACE_DB_H
#define _FACE_DB_H

#ifdef __cplusplus
extern "C" {
#endif

#include "sqlite3.h"

#define FACE_ID_MAX_SIZE 32
#define FACE_INFO_MAX_SIZE 256

typedef struct {
    char id[FACE_ID_MAX_SIZE];
    void *feature;
    int size;
    char info[FACE_INFO_MAX_SIZE];
} face_db_record_t;

int open_db(const char *db_path, sqlite3 **db);

int close_db(sqlite3 *db);

int get_all_face(sqlite3* db, face_db_record_t* face_array, int *face_num);

int get_face_count(sqlite3* db, int *face_num);

int insert_face(sqlite3* db, face_db_record_t* face);

int delete_face(sqlite3* db, const char* face_id);

int release_face_data(face_db_record_t* face_array, int face_num);

#ifdef __cplusplus
} //extern "C"
#endif

#endif
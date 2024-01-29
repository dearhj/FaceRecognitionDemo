#ifndef _JOBJECT_CONVERT_H
#define _JOBJECT_CONVERT_H

#include "jni.h"
#include "rockiva_common.h"
#include "rockiva_face_api.h"
#include "utils/face_db.h"

void getGlobalClassLoader(JNIEnv *env);

jclass findClass(JNIEnv *env, const char* name);

jobject rockx_image_to_java(JNIEnv *env, const RockIvaImage *image_c);

int rockx_image_from_java(JNIEnv *env, jobject image_j, RockIvaImage *image);

int rockx_image_update_java(JNIEnv *env, jobject image_j, RockIvaImage *image);

int feature_size_from_java(JNIEnv* env, jobject face_record, int* featureSize);

int face_record_from_java(JNIEnv* env, jobject face_record, face_db_record_t* face_db_record);

int search_result_array_to_jave(JNIEnv *env, RockIvaFaceSearchResults *results, jobject out_list);

jobject search_result_to_java(JNIEnv* env, RockIvaFaceSearchResult *result);

#endif //_JOBJECT_CONVERT_H

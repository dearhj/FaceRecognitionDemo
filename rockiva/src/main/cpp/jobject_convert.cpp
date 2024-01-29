#include <cstdlib>
#include <cstring>
#include <utils/face_db.h>

#include "jobject_convert.h"
#include "jni.h"
#include "log.h"

static jobject gClassLoader;
static jmethodID gFindClassMethod;

void getGlobalClassLoader(JNIEnv *env) {
    jclass randomClass = env->FindClass("com/rockchip/iva/RockIva");
    jclass classClass = env->GetObjectClass(randomClass);
    jclass classLoaderClass = env->FindClass("java/lang/ClassLoader");
    jmethodID getClassLoaderMethod = env->GetMethodID(classClass, "getClassLoader",
                                                      "()Ljava/lang/ClassLoader;");
    jobject localClassLoader = env->CallObjectMethod(randomClass, getClassLoaderMethod);

    gClassLoader = env->NewGlobalRef(localClassLoader);
    gFindClassMethod = env->GetMethodID(classLoaderClass, "findClass",
                                        "(Ljava/lang/String;)Ljava/lang/Class;");
}

jclass findClass(JNIEnv *env, const char* name) {
    jclass result = nullptr;
    if (env)
    {
        result = env->FindClass(name);
        jthrowable exception = env->ExceptionOccurred();
        if (exception)
        {
            env->ExceptionClear();
            return static_cast<jclass>(env->CallObjectMethod(gClassLoader, gFindClassMethod, env->NewStringUTF(name)));
        }
    }
    return result;
}

jobject rockx_image_to_java(JNIEnv *env, const RockIvaImage *image) {
    jclass JClassRockFaceImage = findClass(env, "com/rockchip/iva/RockIvaImage");
    jmethodID RockfaceObjectConstruct = env->GetMethodID(JClassRockFaceImage, "<init>", "(IIIIIIJIJ)V");

    jvalue * args = (jvalue*)malloc(9*sizeof(jvalue));
    args[0].i = image->info.width;
    args[1].i = image->info.height;
    args[2].i = image->info.format;
    args[3].i = image->frameId;
    args[4].i = image->channelId;
    args[5].i = image->info.transformMode;
    args[6].j = reinterpret_cast<jlong>(image->dataAddr);
    args[7].i = image->dataFd;
    args[8].j = reinterpret_cast<jlong>(image->dataPhyAddr);
    jobject obj_j = env->NewObjectA(JClassRockFaceImage, RockfaceObjectConstruct, args);

    free(args);
    env->DeleteLocalRef(JClassRockFaceImage);
    return obj_j;
}

int rockx_image_from_java(JNIEnv *env, jobject image_j, RockIvaImage *image) {
    memset(image, 0, sizeof(RockIvaImage));
    jclass jClassRockFaceImage = findClass(env, "com/rockchip/iva/RockIvaImage");
    jfieldID RockFaceImage_width = env->GetFieldID(jClassRockFaceImage, "width", "I");
    jfieldID RockFaceImage_height = env->GetFieldID(jClassRockFaceImage, "height", "I");
    jmethodID RockFaceImage_getPixelFormatIndex = env->GetMethodID(jClassRockFaceImage, "getPixelFormatIndex", "()I");
    jfieldID RockFaceImage_dataAddr = env->GetFieldID(jClassRockFaceImage, "dataAddr", "J");
    jfieldID RockFaceImage_dataFd = env->GetFieldID(jClassRockFaceImage, "dataFd", "I");
    jfieldID RockFaceImage_dataPhyAddr = env->GetFieldID(jClassRockFaceImage, "dataPhyAddr", "J");
    jfieldID RockFaceImage_channelId = env->GetFieldID(jClassRockFaceImage, "channelId", "I");
    jfieldID RockFaceImage_frameId = env->GetFieldID(jClassRockFaceImage, "frameId", "I");
    jfieldID RockFaceImage_transformMode = env->GetFieldID(jClassRockFaceImage, "transformMode", "I");

    int img_w = env->GetIntField(image_j, RockFaceImage_width);
    int img_h = env->GetIntField(image_j, RockFaceImage_height);

    uint8_t* dataAddr = (uint8_t*)env->GetLongField(image_j, RockFaceImage_dataAddr);
    uint8_t* dataPhyAddr = (uint8_t*)env->GetLongField(image_j, RockFaceImage_dataPhyAddr);
    int dataFd = env->GetIntField(image_j, RockFaceImage_dataFd);
    int channelId = env->GetIntField(image_j, RockFaceImage_channelId);
    int frameId = env->GetIntField(image_j, RockFaceImage_frameId);
    int transformMode = env->GetIntField(image_j, RockFaceImage_transformMode);

    int pixel_format_index = env->CallIntMethod(image_j, RockFaceImage_getPixelFormatIndex);

    image->info.width = img_w;
    image->info.height = img_h;
    image->info.format = (RockIvaImageFormat)pixel_format_index;
    image->dataAddr = dataAddr;
    image->dataPhyAddr = dataPhyAddr;
    image->dataFd = dataFd;
    image->channelId = channelId;
    image->frameId = frameId;
    image->info.transformMode = (RockIvaImageTransform)transformMode;

    env->DeleteLocalRef(jClassRockFaceImage);
    return 0;
}

int rockx_image_update_java(JNIEnv *env, jobject image_j, RockIvaImage *image) {
    jclass JClassRockFaceImage = findClass(env, "com/rockchip/iva/RockIvaImage");
    jmethodID RockivaObjectset = env->GetMethodID(JClassRockFaceImage, "setValues", "(IIIIIIJIJ)V");
    env->CallVoidMethod(image_j, RockivaObjectset, image->info.width, image->info.height, (jint)image->info.format,
                        image->frameId, image->channelId, image->info.transformMode,
                        (jlong)image->dataAddr, image->dataFd, (jlong)image->dataPhyAddr);
    env->DeleteLocalRef(JClassRockFaceImage);
    return 0;
}

int face_record_from_java(JNIEnv* env, jobject face_record, face_db_record_t* face_db_record) {
    jclass  RockIvaFaceRecord_jclass = env->FindClass("com/rockchip/iva/face/RockIvaFaceLibrary$FaceRecord");
    jfieldID RockIvaFaceRecord_faceId = env->GetFieldID(RockIvaFaceRecord_jclass, "faceId", "Ljava/lang/String;");
    jfieldID RockIvaFaceRecord_feature = env->GetFieldID(RockIvaFaceRecord_jclass, "feature", "[B");
    jfieldID RockIvaFaceRecord_faceInfo = env->GetFieldID(RockIvaFaceRecord_jclass, "faceInfo", "Ljava/lang/String;");

    jstring faceid_jstring = (jstring)env->GetObjectField(face_record, RockIvaFaceRecord_faceId);
    if (faceid_jstring != nullptr) {
        const char *faceid = env->GetStringUTFChars(faceid_jstring, NULL);
        strncpy(face_db_record->id, faceid, FACE_ID_MAX_SIZE);
    } else {
        LOGE("face record faceId not set!");
        return -1;
    }

    jstring faceinfo_jstring = (jstring)env->GetObjectField(face_record, RockIvaFaceRecord_faceInfo);
    if (faceinfo_jstring != nullptr) {
        const char *faceinfo = env->GetStringUTFChars(faceinfo_jstring, NULL);
        strncpy(face_db_record->info, faceinfo, FACE_INFO_MAX_SIZE);
    }

    jbyteArray feature_data_j = static_cast<jbyteArray>(env->GetObjectField(face_record,
                                                                            RockIvaFaceRecord_feature));
    if (feature_data_j != nullptr) {
        int feature_size = env->GetArrayLength(feature_data_j);
        if (feature_size > 0) {
            char *feature_data_ = (char *) env->GetByteArrayElements(feature_data_j, NULL);
            face_db_record->feature = malloc(feature_size);
            face_db_record->size = feature_size;
            memcpy(face_db_record->feature, feature_data_, feature_size);
        }
    }
    return 0;
}

int feature_size_from_java(JNIEnv* env, jobject face_record, int* featureSize) {
    jclass  RockIvaFaceRecord_jclass = env->FindClass("com/rockchip/iva/face/RockIvaFaceLibrary$FaceRecord");
    jfieldID RockIvaFaceRecord_feature = env->GetFieldID(RockIvaFaceRecord_jclass, "feature", "[B");

    jbyteArray feature_data_j = static_cast<jbyteArray>(env->GetObjectField(face_record,
                                                                            RockIvaFaceRecord_feature));
    if (feature_data_j != nullptr) {
        *featureSize = env->GetArrayLength(feature_data_j);
    } else {
        *featureSize = 0;
    }
    return 0;
}

jobject search_result_to_java(JNIEnv* env, RockIvaFaceSearchResult *result) {
    // Get Java Class And Method
    jclass JClassDetectResult = env->FindClass("com/rockchip/iva/face/RockIvaFaceSearchResult");
    jmethodID SearchResultConstruct = env->GetMethodID(JClassDetectResult, "<init>",
                                                       "(Ljava/lang/String;F)V");
    // Malloc params
    jvalue * args = (jvalue*)malloc(2*sizeof(jvalue));
    jstring faceid_j = env->NewStringUTF(result->faceIdInfo);
    args[0].l = faceid_j;
    args[1].f = result->score;
    // New Java Object
    jobject obj_j = env->NewObjectA(JClassDetectResult, SearchResultConstruct, args);
    // Release
    free(args);
    env->DeleteLocalRef(faceid_j);
    // Return
    return obj_j;
}

int search_result_array_to_jave(JNIEnv *env, RockIvaFaceSearchResults *results, jobject out_list) {
    jclass cls_ArrayList = env->GetObjectClass(out_list);
    jmethodID arrayList_add = env->GetMethodID(cls_ArrayList, "add", "(Ljava/lang/Object;)Z");
    for (int i = 0; i < results->num; i++) {
        jobject jobj_result = search_result_to_java(env, &(results->faceIdScore[i]));
        env->CallBooleanMethod(out_list, arrayList_add, jobj_result);
    }
    return 0;
}
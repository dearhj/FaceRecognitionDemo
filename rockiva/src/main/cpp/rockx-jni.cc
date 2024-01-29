#include <unistd.h>
#include <string.h>
#include <stdlib.h>

#include "jni.h"

#include "rockiva_one_api.h"
#include "rockiva_image.h"
#include "rockiva_face_api.h"

#include "jobject_convert.h"
#include "log.h"

#include "utils/face_db.h"

class JNIEnvPtr {
public:
    JNIEnvPtr(JavaVM* jvm) : env_{nullptr}, need_detach_{false} {
        jvm_ = jvm;
        if (jvm_->GetEnv((void**) &env_, JNI_VERSION_1_6) ==
            JNI_EDETACHED) {
            jvm_->AttachCurrentThread(&env_, nullptr);
            need_detach_ = true;
        }
    }

    ~JNIEnvPtr() {
        if (need_detach_) {
            jvm_->DetachCurrentThread();
        }
    }

    JNIEnv* operator->() {
        return env_;
    }

    JNIEnv* get() {
        return env_;
    }

private:
    JNIEnvPtr(const JNIEnvPtr&) = delete;
    JNIEnvPtr& operator=(const JNIEnvPtr&) = delete;

private:
    JavaVM* jvm_;
    JNIEnv* env_;
    bool need_detach_;
};

typedef struct
{
    RockIvaHandle handle;
    jobject jcallback;
    JavaVM* jvm;
} IvaAppContext;

typedef struct
{
    sqlite3* db;
} IvaFaceLibContext;

void FrameReleaseCallback(const RockIvaReleaseFrames* releaseFrames, void* userdata)
{
    IvaAppContext *ivaApp = (IvaAppContext*)userdata;
    if (ivaApp->jcallback != nullptr) {
        JNIEnvPtr env(ivaApp->jvm);
        jclass jClassRockIvaCallback = findClass(env.get(), "com/rockchip/iva/RockIvaCallback");
        jmethodID RockIvaCallback_onReleasseCallback = env->GetMethodID(jClassRockIvaCallback, "onReleaseCallback",
                                                                      "(Ljava/util/List;)V");
        jclass list_jcls = findClass(env.get(), "java/util/ArrayList");
        jmethodID list_init = env->GetMethodID(list_jcls, "<init>", "()V");
        jobject list_jobj = env->NewObject(list_jcls, list_init);
        jmethodID list_add = env->GetMethodID(list_jcls, "add", "(Ljava/lang/Object;)Z");
        for (int i = 0; i < releaseFrames->count; i++) {
            jobject rockivaImage = rockx_image_to_java(env.get(), &(releaseFrames->frames[i]));
            env->CallBooleanMethod(list_jobj, list_add, rockivaImage);
        }
        env->CallVoidMethod(ivaApp->jcallback, RockIvaCallback_onReleasseCallback, list_jobj);
        env->DeleteLocalRef(list_jobj);
    }
}

void ResultCallback(const char* result, const RockIvaExecuteStatus status, void* userdata)
{
    printf("result: %s\n", result);
    IvaAppContext *ivaApp = (IvaAppContext*)userdata;
    JNIEnvPtr env(ivaApp->jvm);
    if (ivaApp->jcallback != nullptr) {
        jclass jClassRockIvaCallback = findClass(env.get(), "com/rockchip/iva/RockIvaCallback");
        jmethodID RockIvaCallback_onResultCallback = env->GetMethodID(jClassRockIvaCallback, "onResultCallback",
                                                                      "(Ljava/lang/String;I)V");
        jstring result_jstring = env->NewStringUTF(result);
        env->CallVoidMethod(ivaApp->jcallback, RockIvaCallback_onResultCallback, result_jstring, (int)status);
        env->DeleteLocalRef(result_jstring);
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_rockchip_iva_RockIva_native_1init(JNIEnv *env, jobject thiz, jstring json_str) {
    const char* json_str_c = (char*)( env )->GetStringUTFChars(json_str, NULL);

    RockIvaHandle  handle;
    RockIvaOneCallback callback;
    callback.releaseCallback = FrameReleaseCallback;
    callback.resultCallback = ResultCallback;

    IvaAppContext* iva_ctx = (IvaAppContext*)malloc(sizeof(IvaAppContext));

    RockIvaRetCode ret = ROCKIVA_ONE_Init(&handle, json_str_c, callback, iva_ctx);
    if (ret != ROCKIVA_RET_SUCCESS) {
        free(iva_ctx);
        return -1;
    }

    iva_ctx->handle = handle;
    iva_ctx->jcallback = NULL;
    env->GetJavaVM(&iva_ctx->jvm);

    return reinterpret_cast<jlong>(iva_ctx);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_RockIva_native_1release(JNIEnv *env, jobject thiz, jlong handle) {
    IvaAppContext* iva_ctx = (IvaAppContext*)handle;
    if (iva_ctx == nullptr) {
        return -1;
    }
    RockIvaRetCode ret = ROCKIVA_ONE_Release((RockIvaHandle)handle);
    if (ret != ROCKIVA_RET_SUCCESS) {
        return -1;
    }
    free(iva_ctx);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_RockIva_native_1setcallback(JNIEnv *env, jobject thiz, jlong handle,
                                                  jobject callback) {
    IvaAppContext* iva_ctx = (IvaAppContext*)handle;
    if (iva_ctx == nullptr) {
        return -1;
    }
    iva_ctx->jcallback = env->NewGlobalRef(callback);;
    getGlobalClassLoader(env);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_RockIva_native_1pushFrame(JNIEnv *env, jobject thiz, jlong handle,
                                                    jobject image) {
    IvaAppContext* iva_ctx = (IvaAppContext*)handle;
//    LOGI("iva_ctx=%p iva_ctx->handle=%p", iva_ctx, iva_ctx->handle);
    if (iva_ctx == nullptr || iva_ctx->handle == nullptr) {
        return -1;
    }
    RockIvaImage image_c;
    rockx_image_from_java(env, image, &image_c);
    if (image_c.dataAddr == nullptr) {
        LOGE("image data is nullptr");
        return -1;
    }

    RockIvaRetCode ret = ROCKIVA_PushFrame(iva_ctx->handle, &image_c, NULL);
    if (ret != ROCKIVA_RET_SUCCESS) {
        return -1;
    }
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_RockIva_native_1setAnalyseFace(JNIEnv *env, jobject thiz, jlong handle, jint id) {
    IvaAppContext* iva_ctx = (IvaAppContext*)handle;
//    LOGI("iva_ctx=%p iva_ctx->handle=%p", iva_ctx, iva_ctx->handle);
    if (iva_ctx == nullptr || iva_ctx->handle == nullptr) {
        return -1;
    }

    RockIvaRetCode ret = ROCKIVA_FACE_SetAnalyseFace(iva_ctx->handle, id);
    if (ret != ROCKIVA_RET_SUCCESS) {
        return -1;
    }
    return 0;
}

/*************************************************************************
                        rockx image jni function
**************************************************************************/

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_RockIvaImage_alloc_1image_1memory(JNIEnv *env, jobject thiz, jobject image, jint type) {
    RockIvaImage image_c;
    memset(&image_c, 0, sizeof(RockIvaImage));
    rockx_image_from_java(env, image, &image_c);

    RockIvaRetCode  ret = ROCKIVA_IMAGE_AllocMem(&image_c, (RockIvaMemType)type);
    if (ret != ROCKIVA_RET_SUCCESS) {
        return -1;
    }
    rockx_image_update_java(env, image, &image_c);

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_RockIvaImage_free_1image_1memory(JNIEnv *env, jobject thiz, jobject image) {
    RockIvaImage image_c;
    rockx_image_from_java(env, image, &image_c);
    RockIvaRetCode  ret = ROCKIVA_IMAGE_FreeMem(&image_c);
    if (ret != ROCKIVA_RET_SUCCESS) {
        return -1;
    }
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_RockIvaImage_set_1image_1data(JNIEnv *env, jobject thiz, jobject image,
                                                    jbyteArray data) {
    jboolean inputCopy = JNI_FALSE;
    jbyte* const data_c = env->GetByteArrayElements(data, &inputCopy);
    int size = env->GetArrayLength(data);

    RockIvaImage image_c;
    rockx_image_from_java(env, image, &image_c);
    if (image_c.dataAddr == nullptr) {
        return -1;
    }

    int size_c = ROCKIVA_IMAGE_Get_Size(&image_c);
    if (size_c != size) {
        LOGE("size(%d) != size_c(%d)", size, size_c);
        return -1;
    }

    memcpy(image_c.dataAddr, data_c, size);
    env->ReleaseByteArrayElements(data, data_c, JNI_ABORT);

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_RockIvaImage_get_1image_1size(JNIEnv *env, jobject thiz) {
    jboolean inputCopy = JNI_FALSE;
    RockIvaImage image_c;
    rockx_image_from_java(env, thiz, &image_c);
    if (image_c.info.width == 0 || image_c.info.height == 0) {
        return -1;
    }
    return ROCKIVA_IMAGE_Get_Size(&image_c);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_RockIvaImage_get_1image_1data(JNIEnv *env, jobject thiz, jbyteArray data) {
    jboolean inputCopy = JNI_TRUE;
    int size = env->GetArrayLength(data);

    RockIvaImage image_c;
    rockx_image_from_java(env, thiz, &image_c);
    if (image_c.dataAddr == nullptr) {
        return -1;
    }

    int size_c = ROCKIVA_IMAGE_Get_Size(&image_c);
    if (size_c != size) {
        LOGE("size(%d) != size_c(%d)", size, size_c);
        return -1;
    }

    env->SetByteArrayRegion(data, 0, size, (jbyte*)image_c.dataAddr);

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_RockIvaImage_write_1image(JNIEnv *env, jobject thiz, jstring path) {
    jboolean inputCopy = JNI_TRUE;
    const char* path_c = (char*)( env )->GetStringUTFChars(path, NULL);

    RockIvaImage image_c;
    rockx_image_from_java(env, thiz, &image_c);
    if (image_c.dataAddr == nullptr) {
        return -1;
    }

    RockIvaRetCode  ret = ROCKIVA_IMAGE_Write(path_c, &image_c);
    if (ret != ROCKIVA_RET_SUCCESS) {
        return -1;
    }
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_RockIvaImage_read_1image(JNIEnv *env, jclass cls, jstring path,
                                               jobject image) {
    const char* path_c = (char*)( env )->GetStringUTFChars(path, NULL);
    RockIvaImage image_c;
    memset(&image_c, 0, sizeof(RockIvaImage));
    RockIvaRetCode  ret = ROCKIVA_IMAGE_Read(path_c, &image_c);
    if (ret != ROCKIVA_RET_SUCCESS) {
        return -1;
    }
    rockx_image_update_java(env, image, &image_c);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_RockIvaImage_release_1image(JNIEnv *env, jobject thiz) {
    RockIvaImage image_c;
    rockx_image_from_java(env, thiz, &image_c);
    if (image_c.dataAddr == nullptr) {
        return -1;
    }
    return ROCKIVA_IMAGE_Release(&image_c);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_rockchip_iva_RockIvaImage_parse_1image(JNIEnv *env, jobject thiz, jstring json_str) {
    const char* json_str_c = (char*)( env )->GetStringUTFChars(json_str, NULL);
    RockIvaImage image_c;
    memset(&image_c, 0, sizeof(RockIvaImage));
    RockIvaRetCode ret = ROCKIVA_ONE_GetImage(json_str_c, &image_c, NULL);
    if (ret != ROCKIVA_RET_SUCCESS) {
        return rockx_image_to_java(env, &image_c);
    }
    return nullptr;
}

//extern "C"
//JNIEXPORT jint JNICALL
//Java_com_rockchip_iva_face_RockIvaFaceLibrary_controlFaceLibrary(JNIEnv *env, jobject thiz,
//                                                                 jlong handle,
//                                                                 jstring lib_name, jint action,
//                                                                 jobjectArray face_records) {
//
//    IvaFaceLibContext* face_lib_ctx = (IvaFaceLibContext*)handle;
//    if (face_lib_ctx == nullptr) {
//        return -1;
//    }
//
//    const char* lib_name_c = (char*)env->GetStringUTFChars(lib_name, NULL);
//
//    if (face_records == nullptr) {
//        LOGE("face records param error");
//        return -1;
//    }
//    int num = env->GetArrayLength(face_records);
//    if (num <= 0) {
//        LOGE("face records param error");
//        return -1;
//    }
//    RockIvaFaceIdInfo faceIdArrary[num];
//    int feature_size;
//    jobject face_record = env->GetObjectArrayElement(face_records, 0);
//    feature_size_from_java(env, face_record, &feature_size);
//    if (feature_size <= 0) {
//        LOGE("feature data error");
//        return -1;
//    }
//    char *feature_data = (char*)malloc(num*feature_size);
//    for (int i = 0; i < num; i++) {
//        jobject face_record = env->GetObjectArrayElement(face_records, i);
//        face_record_from_java(env, face_record, &faceIdArrary[i], feature_data+feature_size*i, feature_size);
//    }
//
//    for (int i = 0; i < num; i++) {
//        face_db_record_t facedata;
//        memset(&facedata, 0, sizeof(face_db_record_t));
//        strncpy(facedata.id, faceIdArrary[i].faceIdInfo, FACE_ID_MAX_SIZE);
//        facedata.feature = feature_data+feature_size*i;
//        facedata.size = feature_size;
//        insert_face(face_lib_ctx->db, &facedata);
//    }
//
//    RockIvaRetCode  ret =  ROCKIVA_FACE_FeatureLibraryControl(lib_name_c,  (RockIvaFaceLibraryAction)action, faceIdArrary, num, feature_data,
//                                              feature_size);
//
//    return 0;
//}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_face_RockIvaFaceLibrary_searchFaceLibrary(JNIEnv *env, jobject thiz,
                                                                jlong handle,
                                                                jstring lib_name,
                                                                jbyteArray feature, jint top_k,
                                                                jobject search_results) {
    const char* lib_name_c = (char*)env->GetStringUTFChars(lib_name, NULL);
    if (feature == nullptr) {
        LOGE("searchFaceLibrary: feature error");
        return -1;
    }
    int feature_size = env->GetArrayLength(feature);
    if (feature_size <= 0) {
        LOGE("searchFaceLibrary: feature error");
        return -1;
    }
    char *feature_data = (char *)env->GetByteArrayElements(feature, NULL);

    RockIvaFaceSearchResults results;
    int ret = ROCKIVA_FACE_SearchFeature(lib_name_c, feature_data, feature_size, 1, top_k, &results);
    if (ret != 0) {
        LOGE("search fail %d", ret);
        return -1;
    }

    search_result_array_to_jave(env, &results, search_results);

    return 0;
}

int load_face_lib(sqlite3* db, const char *face_lib_name) {
    int ret;
    int face_num_;

    ret = get_face_count(db, &face_num_);
    face_db_record_t face_lib_[face_num_];

    get_all_face(db, face_lib_, &face_num_);

    RockIvaFaceIdInfo face_info[face_num_];
    int feature_size = face_lib_->size;
    printf("feature_size=%d\n", feature_size);
    char *feature_data = (char *)malloc(face_num_*feature_size);
    for (int i = 0; i < face_num_; i++) {
        strncpy(face_info[i].faceIdInfo, face_lib_[i].info, ROCKIVA_FACE_INFO_SIZE_MAX);
        memcpy(feature_data+i*feature_size, face_lib_[i].feature, feature_size);
    }

    release_face_data(face_lib_, face_num_);

    ROCKIVA_FACE_FeatureLibraryControl(face_lib_name, ROCKIVA_FACE_FEATURE_INSERT, face_info, face_num_, feature_data, feature_size);

    free(feature_data);

    printf("load face num %d\n", face_num_);

    return 0;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_rockchip_iva_face_RockIvaFaceLibrary_initFacelibrary(JNIEnv *env, jobject thiz,
                                                              jstring db_path, jstring lib_name) {
    int ret;
    const char* db_path_c = (char*)env->GetStringUTFChars(db_path, NULL);
    const char* lib_name_c = (char*)env->GetStringUTFChars(lib_name, NULL);

    sqlite3* db;
    ret = open_db(db_path_c, &db);
    if (ret != 0) {
        LOGE("open face database fail %s", db_path_c);
        return -1;
    }

    IvaFaceLibContext* face_lib_ctx = (IvaFaceLibContext*)malloc(sizeof(IvaFaceLibContext));
    face_lib_ctx->db = db;

    ret = load_face_lib(db, lib_name_c);
    if (ret != 0) {
        return -1;
    }

    return (jlong)face_lib_ctx;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_face_RockIvaFaceLibrary_releaseFacelibrary(JNIEnv *env, jobject thiz,
                                                                 jlong handle) {
    IvaFaceLibContext* face_lib_ctx = (IvaFaceLibContext*)handle;
    if (face_lib_ctx == nullptr) {
        return -1;
    }
    if (face_lib_ctx->db != nullptr) {
        close_db(face_lib_ctx->db);
        face_lib_ctx->db = nullptr;
    }
    free(face_lib_ctx);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_face_RockIvaFaceLibrary_addFace(JNIEnv *env, jobject thiz, jlong handle,
                                                      jstring lib_name, jobject face_record) {
    IvaFaceLibContext* face_lib_ctx = (IvaFaceLibContext*)handle;
    if (face_lib_ctx == nullptr) {
        LOGE("face_lib_ctx is null, need init face library before");
        return -1;
    }

    const char* lib_name_c = (char*)env->GetStringUTFChars(lib_name, nullptr);
    if (face_record == nullptr) {
        LOGE("face record param error");
        return -1;
    }

    face_db_record_t face_db_record;
    memset(&face_db_record, 0, sizeof(face_db_record_t));

    face_record_from_java(env, face_record, &face_db_record);

    RockIvaFaceIdInfo faceIdArray[1];
    strncpy(faceIdArray[0].faceIdInfo, face_db_record.id, ROCKIVA_FACE_INFO_SIZE_MAX);

    RockIvaRetCode  ret =  ROCKIVA_FACE_FeatureLibraryControl(lib_name_c,  ROCKIVA_FACE_FEATURE_INSERT,
                                                              faceIdArray, 1, face_db_record.feature,
                                                              face_db_record.size);
    if (ret == ROCKIVA_RET_SUCCESS) {
        insert_face(face_lib_ctx->db, &face_db_record);
    }

    if (face_db_record.feature != nullptr) {
        free(face_db_record.feature);
    }

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_face_RockIvaFaceLibrary_deleteFace(JNIEnv *env, jobject thiz, jlong handle,
                                                         jstring lib_name, jstring face_id) {
    IvaFaceLibContext* face_lib_ctx = (IvaFaceLibContext*)handle;
    if (face_lib_ctx == nullptr) {
        return -1;
    }

    const char* lib_name_c = (char*)env->GetStringUTFChars(lib_name, NULL);
    const char* face_id_c = (char*)env->GetStringUTFChars(face_id, NULL);
    RockIvaFaceIdInfo faceIdArray[1];
    strncpy(faceIdArray[0].faceIdInfo, face_id_c, ROCKIVA_FACE_INFO_SIZE_MAX);
    RockIvaRetCode  ret =  ROCKIVA_FACE_FeatureLibraryControl(lib_name_c,  ROCKIVA_FACE_FEATURE_DELETE,
                                                              faceIdArray, 1, nullptr, 0);
    if (ret == ROCKIVA_RET_SUCCESS) {
        delete_face(face_lib_ctx->db, face_id_c);
    }

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_face_RockIvaFaceLibrary_updateFace(JNIEnv *env, jobject thiz, jlong handle,
                                                         jstring lib_name, jobject face_record) {
    LOGE("not implement");
    return -1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rockchip_iva_face_RockIvaFaceLibrary_clearAll(JNIEnv *env, jobject thiz, jlong handle,
                                                       jstring lib_name) {
    const char* lib_name_c = (char*)env->GetStringUTFChars(lib_name, NULL);
    return ROCKIVA_FACE_FeatureLibraryControl(lib_name_c, ROCKIVA_FACE_FEATURE_CLEAR,
                                              nullptr, 0, nullptr, 0);
}

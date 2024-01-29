#ifndef RK_FACE_DEMO_LOG_H
#define RK_FACE_DEMO_LOG_H

#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "rockx4j", ##__VA_ARGS__);
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "rockx4j", ##__VA_ARGS__);
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "rockx4j", ##__VA_ARGS__);

#endif //RK_FACE_DEMO_LOG_H

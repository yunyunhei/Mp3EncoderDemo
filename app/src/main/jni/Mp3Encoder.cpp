//
// Created by WuHang on 2018/8/30 0030.
//

#include <jni.h>
#include <android/log.h>
#include "cn_yunyunhei_mp3encoderdemo_studio_Mp3Encoder.h"

//#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"Mp3Encoder",__VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, "Mp3Encoder", __VA_ARGS__)

JNIEXPORT void JNICALL
Java_cn_yunyunhei_mp3encoderdemo_studio_Mp3Encoder_encode(JNIEnv *env, jobject obj) {
    LOGI("encoder encode");
}
#ifndef TOUCHCONTROLLER_ANDROID_H
#define TOUCHCONTROLLER_ANDROID_H

#include <jni.h>

JNIEXPORT jlong JNICALL Java_top_fifthlight_touchcontroller_common_platform_android_Transport_new(JNIEnv* env,
                                                                                                  jclass clazz,
                                                                                                  jstring name);

JNIEXPORT jint JNICALL Java_top_fifthlight_touchcontroller_common_platform_android_Transport_receive(JNIEnv* env,
                                                                                                     jclass clazz,
                                                                                                     jlong handle,
                                                                                                     jbyteArray buffer);

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_android_Transport_send(
    JNIEnv* env, jclass clazz, jlong handle, jbyteArray buffer, jint off, jint len);

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_android_Transport_destroy(JNIEnv* env,
                                                                                                     jclass clazz,
                                                                                                     jlong handle);

#endif

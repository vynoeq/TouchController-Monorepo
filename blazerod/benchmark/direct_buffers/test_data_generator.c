#include <jni.h>
#include <stdlib.h>

static void generate_buffer(float* buffer, int floats, int seed) {
    const long a = 1664525;
    const long c = 1013904223;
    const long m = 4294967296;

    long state = seed;
    for (int i = 0; i < floats; i++) {
        state = (a * state + c) % m;
        buffer[i] = (float)state / m;
    }
}

/*
 * Class:     top_fifthlight_blazerod_benchmark_directbuffers_BufferGenerator
 * Method:    generateBufferDirect
 * Signature: (II)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL
Java_top_fifthlight_blazerod_benchmark_directbuffers_BufferGenerator_generateBufferDirect(
    JNIEnv* env, jclass clazz, jint seed, jint floats) {
    float* buffer = (float*)malloc(floats * sizeof(float));
    if (buffer == NULL) {
        return NULL;
    }

    generate_buffer(buffer, floats, seed);

    jobject directBuffer =
        (*env)->NewDirectByteBuffer(env, buffer, floats * sizeof(float));
    if (directBuffer == NULL) {
        free(buffer);
        return NULL;
    }

    return directBuffer;
}

/*
 * Class:     top_fifthlight_blazerod_benchmark_directbuffers_BufferGenerator
 * Method:    releaseDirectBuffer
 * Signature: (Ljava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL
Java_top_fifthlight_blazerod_benchmark_directbuffers_BufferGenerator_releaseDirectBuffer(
    JNIEnv* env, jclass clazz, jobject buffer) {
    if (buffer != NULL) {
        (*env)->DeleteGlobalRef(env, buffer);
    }
}

/*
 * Class:     top_fifthlight_blazerod_benchmark_directbuffers_BufferGenerator
 * Method:    generateByteBufferHeap
 * Signature: (II)[F;
 */
JNIEXPORT jfloatArray JNICALL
Java_top_fifthlight_blazerod_benchmark_directbuffers_BufferGenerator_generateByteBufferHeap(
    JNIEnv* env, jclass clazz, jint seed, jint floats) {
    float* buffer = (float*)malloc(floats * sizeof(float));
    if (buffer == NULL) {
        return NULL;
    }

    generate_buffer(buffer, floats, seed);
    jfloatArray result = (*env)->NewFloatArray(env, floats);
    if (result == NULL) {
        free(buffer);
        return NULL;
    }
    (*env)->SetFloatArrayRegion(env, result, 0, floats, buffer);
    free(buffer);
    return result;
}

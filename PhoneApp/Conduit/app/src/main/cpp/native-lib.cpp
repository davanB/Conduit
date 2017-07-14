#include <jni.h>
#include "shoco.h"
#include <string>
#include <malloc.h>

extern "C" {
JNIEXPORT jstring JNICALL
Java_com_example_davanb_jniexample_MainActivity_compressSmallString(
        JNIEnv *env, jobject /* this */, jstring message) {
    const char* str;
    char* output;

    str = env->GetStringUTFChars(message, 0);
    size_t buffSize = strlen(str);

    output = (char*)malloc((buffSize) * sizeof(char));           // allocate memory

    size_t result = shoco_compress(str, buffSize, output, buffSize); // compress and save to output

    jstring compressedString = env->NewStringUTF(output);

    free(output);                                           // deallocate memory

    return compressedString;                                // return populated Byte[]
}

JNIEXPORT jstring JNICALL
Java_com_example_davanb_jniexample_MainActivity_decompressCompressedString(
        JNIEnv *env, jobject /* this */, jstring message) {
    const char* str;
    char* output;

    str = env->GetStringUTFChars(message, 0);
    size_t buffSize = strlen(str)*2;

    output = (char*)malloc((buffSize) * sizeof(char));              // allocate memory

    size_t result = shoco_decompress(str, buffSize, output, buffSize); // max 50% compression

    jstring uncompressedString = env->NewStringUTF(output);         // return decompress str

    free(output);

    return uncompressedString;
}
}


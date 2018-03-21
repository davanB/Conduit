#include <jni.h>
#include "shoco.h"
#include <string>
extern "C" {
JNIEXPORT jbyteArray JNICALL
Java_ca_uwaterloo_fydp_conduit_DataTransformation_compressSmallString(
        JNIEnv *env, jobject /* this */, jstring message) {
    const char *str;
    char *output;

    str = env->GetStringUTFChars(message, NULL);
    size_t buffSize = env->GetStringUTFLength(message);

    output = new char[buffSize];           // allocate memory

    size_t result = shoco_compress(str, buffSize, output, buffSize); // compress and save to output

    jbyteArray compressedString = env->NewByteArray(result);
    env->SetByteArrayRegion(compressedString, 0, result, (jbyte *) output);

    delete[] output;                                        // deallocate memory
    return compressedString;                                // return populated Byte[]
}

JNIEXPORT jbyteArray JNICALL
Java_ca_uwaterloo_fydp_conduit_DataTransformation_decompressCompressedString(
        JNIEnv *env, jobject /* this */, jbyteArray message) {
    const char *str;
    char *output;

    size_t inputSize = env->GetArrayLength(message);
    size_t buffSize = inputSize * 2; // x2 since we can get up to 50% compression

    jbyte *bytes = env->GetByteArrayElements(message, 0);

    str = new char[buffSize];
    str = reinterpret_cast<char *>(bytes);

    output = new char[buffSize];     // allocate memory

    size_t result = shoco_decompress(str, inputSize, output, buffSize); // max 50% compression

    jbyteArray uncompressedString = env->NewByteArray(result);
    env->SetByteArrayRegion(uncompressedString, 0, result,
                            (jbyte *) output);  // return decompress str

    delete[] str;
    env->ReleaseByteArrayElements(message, bytes, JNI_ABORT);
    delete[] output;

    return uncompressedString;
}
}


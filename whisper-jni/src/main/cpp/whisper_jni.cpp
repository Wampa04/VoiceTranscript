#include <jni.h>
#include <string>
#include <android/log.h>

#define TAG "WhisperJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_voicetranscript_ndk_WhisperLib_init(JNIEnv *env, jobject thiz, jstring model_path) {
    const char *path = env->GetStringUTFChars(model_path, nullptr);
    LOGD("Initialisiere Whisper mit Model: %s", path);

    // Hier kommt später die whisper.cpp Initialisierung hin

    env->ReleaseStringUTFChars(model_path, path);
    return 1; // Dummy Handle
}

JNIEXPORT jstring JNICALL
Java_com_voicetranscript_ndk_WhisperLib_transcribeFile(JNIEnv *env, jobject thiz, jstring model_path, jstring audio_path) {
    const char *m_path = env->GetStringUTFChars(model_path, nullptr);
    const char *a_path = env->GetStringUTFChars(audio_path, nullptr);

    LOGD("Transkribiere Datei: %s mit Model: %s", a_path, m_path);

    // Platzhalter für das Ergebnis
    std::string result = "Transkription von " + std::string(a_path) + " startet bald...";

    env->ReleaseStringUTFChars(model_path, m_path);
    env->ReleaseStringUTFChars(audio_path, a_path);

    return env->NewStringUTF(result.c_str());
}

}

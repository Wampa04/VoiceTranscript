#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include <android/log.h>
#include <thread>
#include <algorithm>
#include "whisper.h"
#include "ggml.h"

#define TAG "WhisperJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

bool read_wav(const std::string & filename, std::vector<float> & pcmf) {
    std::ifstream file(filename, std::ios::binary);
    if (!file.is_open()) {
        LOGE("Konnte Audio-Datei nicht öffnen: %s", filename.c_str());
        return false;
    }

    char header[44];
    file.read(header, 44);
    if (file.gcount() < 44) return false;

    file.seekg(0, std::ios::end);
    size_t file_size = file.tellg();
    file.seekg(44, std::ios::beg);

    size_t n_samples = (file_size - 44) / 2;
    std::vector<int16_t> pcm16(n_samples);
    file.read((char*)pcm16.data(), n_samples * 2);

    pcmf.resize(n_samples);
    for (size_t i = 0; i < n_samples; ++i) {
        pcmf[i] = (float)pcm16[i] / 32768.0f;
    }

    return true;
}

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_voicetranscript_ndk_WhisperLib_init(JNIEnv *env, jobject thiz, jstring model_path) {
    const char *path = env->GetStringUTFChars(model_path, nullptr);
    LOGD("Initialisiere Whisper-Kontext mit Model: %s", path);
    LOGD("System Info: %s", whisper_print_system_info());

    struct whisper_context_params params = whisper_context_default_params();
    struct whisper_context * ctx = whisper_init_from_file_with_params(path, params);

    env->ReleaseStringUTFChars(model_path, path);

    if (ctx == nullptr) {
        LOGE("Fehler beim Initialisieren des Whisper-Kontexts!");
        return 0;
    }

    return (jlong) ctx;
}

JNIEXPORT jstring JNICALL
Java_com_voicetranscript_ndk_WhisperLib_transcribeFile(JNIEnv *env, jobject thiz, jlong context_handle, jstring audio_path, jstring language) {
    const char *a_path = env->GetStringUTFChars(audio_path, nullptr);
    const char *lang = env->GetStringUTFChars(language, nullptr);
    struct whisper_context * ctx = (struct whisper_context *) context_handle;

    if (ctx == nullptr) {
        LOGE("Transkription abgebrochen: Kontext ist null!");
        env->ReleaseStringUTFChars(audio_path, a_path);
        env->ReleaseStringUTFChars(language, lang);
        return env->NewStringUTF("Fehler: Kontext nicht initialisiert.");
    }

    LOGD("Starte Inferenz für: %s (Sprache: %s)", a_path, lang);

    std::vector<float> pcmf;
    if (!read_wav(a_path, pcmf)) {
        env->ReleaseStringUTFChars(audio_path, a_path);
        env->ReleaseStringUTFChars(language, lang);
        return env->NewStringUTF("Fehler: Audio-Datei konnte nicht gelesen werden.");
    }

    whisper_full_params wparams = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);

    // Optimierte Thread-Anzahl für Mobile (4 ist meist ideal für Performance-Cores)
    int num_threads = (int)std::thread::hardware_concurrency();
    wparams.n_threads = std::min(4, std::max(1, num_threads));

    LOGD("System Info: %s", whisper_print_system_info());
    LOGD("Nutze %d Threads für Inferenz (Hardware meldet %d)", wparams.n_threads, num_threads);

    wparams.translate = false;
    wparams.language = lang;
    wparams.print_progress = false;

    long long t_start = ggml_time_ms();
    if (whisper_full(ctx, wparams, pcmf.data(), pcmf.size()) != 0) {
        env->ReleaseStringUTFChars(audio_path, a_path);
        env->ReleaseStringUTFChars(language, lang);
        return env->NewStringUTF("Fehler während der Transkription.");
    }
    long long t_end = ggml_time_ms();
    LOGD("Inferenz dauerte: %lld ms", (t_end - t_start));

    std::string result = "";
    int n_segments = whisper_full_n_segments(ctx);
    for (int i = 0; i < n_segments; ++i) {
        result += whisper_full_get_segment_text(ctx, i);
    }

    LOGD("Transkription abgeschlossen.");
    env->ReleaseStringUTFChars(audio_path, a_path);
    env->ReleaseStringUTFChars(language, lang);

    return env->NewStringUTF(result.c_str());
}

JNIEXPORT void JNICALL
Java_com_voicetranscript_ndk_WhisperLib_freeContext(JNIEnv *env, jobject thiz, jlong context_handle) {
    struct whisper_context * ctx = (struct whisper_context *) context_handle;
    if (ctx != nullptr) {
        whisper_free(ctx);
        LOGD("Whisper-Kontext freigegeben.");
    }
}

}

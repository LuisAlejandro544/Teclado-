#include <jni.h>
#include <string>
#include <vector>
#include "keyboard_core.hpp"

static keyboard::KeyboardEngine g_engine;
static bool g_isInitialized = false;

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_example_keyboard_nativebridge_NativeKeyboardBridge_nativeGetEngineVersion(
        JNIEnv* env,
        jobject /* this */) {
    std::string version = "C++ & Rust Native Core v1.2 (Trie + Levenshtein Engine)";
    return env->NewStringUTF(version.c_str());
}

JNIEXPORT void JNICALL
Java_com_example_keyboard_nativebridge_NativeKeyboardBridge_nativeInitEngine(
        JNIEnv* env,
        jobject /* this */) {
    if (!g_isInitialized) {
        g_engine.init();
        g_isInitialized = true;
    }
}

JNIEXPORT jobjectArray JNICALL
Java_com_example_keyboard_nativebridge_NativeKeyboardBridge_nativeGetSuggestions(
        JNIEnv* env,
        jobject /* this */,
        jstring jPrefix,
        jint maxCount) {
    if (!g_isInitialized) {
        g_engine.init();
        g_isInitialized = true;
    }

    const char* prefixChars = env->GetStringUTFChars(jPrefix, nullptr);
    std::string prefix(prefixChars ? prefixChars : "");
    if (prefixChars) {
        env->ReleaseStringUTFChars(jPrefix, prefixChars);
    }

    std::vector<std::string> suggestions = g_engine.getSuggestions(prefix, maxCount);

    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray strArray = env->NewObjectArray(
        static_cast<jsize>(suggestions.size()),
        stringClass,
        nullptr
    );

    for (size_t i = 0; i < suggestions.size(); ++i) {
        jstring jstr = env->NewStringUTF(suggestions[i].c_str());
        env->SetObjectArrayElement(strArray, static_cast<jsize>(i), jstr);
        env->DeleteLocalRef(jstr);
    }

    return strArray;
}

JNIEXPORT jstring JNICALL
Java_com_example_keyboard_nativebridge_NativeKeyboardBridge_nativeAutocorrect(
        JNIEnv* env,
        jobject /* this */,
        jstring jWord) {
    if (!g_isInitialized) {
        g_engine.init();
        g_isInitialized = true;
    }

    const char* wordChars = env->GetStringUTFChars(jWord, nullptr);
    std::string word(wordChars ? wordChars : "");
    if (wordChars) {
        env->ReleaseStringUTFChars(jWord, wordChars);
    }

    std::string correction = g_engine.getAutocorrect(word);
    return env->NewStringUTF(correction.c_str());
}

JNIEXPORT jfloat JNICALL
Java_com_example_keyboard_nativebridge_NativeKeyboardBridge_nativeComputeAudioRms(
        JNIEnv* env,
        jobject /* this */,
        jshortArray jAudioData,
        jint length) {
    if (!jAudioData || length <= 0) return 0.0f;
    jshort* elements = env->GetShortArrayElements(jAudioData, nullptr);
    if (!elements) return 0.0f;

    float rms = keyboard::KeyboardEngine::computeRmsEnergy(elements, static_cast<size_t>(length));
    env->ReleaseShortArrayElements(jAudioData, elements, JNI_ABORT);
    return rms;
}

JNIEXPORT jboolean JNICALL
Java_com_example_keyboard_nativebridge_NativeKeyboardBridge_nativeDetectVoiceActivity(
        JNIEnv* env,
        jobject /* this */,
        jshortArray jAudioData,
        jint length,
        jfloat thresholdDb) {
    if (!jAudioData || length <= 0) return JNI_FALSE;
    jshort* elements = env->GetShortArrayElements(jAudioData, nullptr);
    if (!elements) return JNI_FALSE;

    bool active = keyboard::KeyboardEngine::detectVoiceActivity(elements, static_cast<size_t>(length), thresholdDb);
    env->ReleaseShortArrayElements(jAudioData, elements, JNI_ABORT);
    return active ? JNI_TRUE : JNI_FALSE;
}

}

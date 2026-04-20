cmake_minimum_required(VERSION 3.22.1)
project(whisper-jni)

# JNI Library
add_library(
    whisper-jni
    SHARED
    whisper_jni.cpp
)

target_link_libraries(
    whisper-jni
    PRIVATE
    log
)

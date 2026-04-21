cmake_minimum_required(VERSION 3.22.1)
project(whisper-jni)

set(WHISPER_ROOT_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../external/whisper.cpp")

# Erzwinge Release-Optimierungen für maximale Geschwindigkeit
if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release)
endif()

set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -O3 -fomit-frame-pointer")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3 -fomit-frame-pointer")

# Wir deaktivieren BUILD_SHARED_LIBS, damit ggml und whisper_core statisch gebaut werden.
set(BUILD_SHARED_LIBS OFF CACHE BOOL "Build static libraries" FORCE)

set(GGML_STATIC OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "Disable OpenMP" FORCE)
set(GGML_NO_ACCELERATE ON CACHE BOOL "" FORCE)

# NEON-Optimierung für ARM64 (Standard bei modernen Android-Geräten)
if(ANDROID_ABI STREQUAL "arm64-v8a")
    set(GGML_NEON ON CACHE BOOL "" FORCE)
    add_definitions(-D__ARM_NEON)
    # Zusätzliche CPU-Features für arm64-v8a
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -march=armv8-a+simd")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -march=armv8-a+simd")
endif()

add_subdirectory("${WHISPER_ROOT_DIR}/ggml" ggml_build)

# Die Whisper-Core-Library
add_library(whisper_core STATIC "${WHISPER_ROOT_DIR}/src/whisper.cpp")
target_include_directories(whisper_core PUBLIC
    "${WHISPER_ROOT_DIR}/include"
    "${WHISPER_ROOT_DIR}/src"
)

target_link_libraries(whisper_core PUBLIC ggml)
target_compile_definitions(whisper_core PRIVATE WHISPER_VERSION="1.7.4")

# JNI Wrapper
add_library(whisper-jni SHARED whisper_jni.cpp)

target_link_libraries(whisper-jni
    whisper_core
    log
)

target_include_directories(whisper-jni PRIVATE
    "${WHISPER_ROOT_DIR}/include"
    "${WHISPER_ROOT_DIR}/ggml/include"
)

target_compile_definitions(whisper-jni PRIVATE
    WHISPER_VERSION="1.7.4"
    _GNU_SOURCE
)

package com.voicetranscript

enum class WhisperModel(val modelName: String, val displayName: String, val size: String) {
    TINY("tiny", "Tiny", "75 MB"),
    BASE("base", "Base", "145 MB"),
    SMALL("small", "Small", "480 MB")
}
package com.voicetranscript

import java.io.File

enum class WhisperModel(val modelName: String, val displayName: String, val size: String) {
    TINY("tiny-q8_0", "Tiny", "42 MiB"),
    BASE("base-q8_0", "Base", "78 MiB"),
    SMALL("small-q8_0", "Small", "252 MiB");

    fun getFile(baseDir: File): File {
        return File(baseDir, "ggml-$modelName.bin")
    }

    fun isDownloaded(baseDir: File): Boolean {
        return getFile(baseDir).exists()
    }

    fun delete(baseDir: File): Boolean {
        val file = getFile(baseDir)
        return if (file.exists()) {
            file.delete()
        } else {
            true
        }
    }
}
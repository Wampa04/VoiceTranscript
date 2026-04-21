package com.voicetranscript.data.filesystem

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhisperModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getModelsDirectory(): File {
        return File(context.filesDir, "whisper_models").apply {
            if (!exists()) mkdirs()
        }
    }

    fun getModelFile(modelName: String): File {
        return File(getModelsDirectory(), "ggml-$modelName.bin")
    }

    fun isModelDownloaded(modelName: String): Boolean {
        return getModelFile(modelName).exists()
    }
}
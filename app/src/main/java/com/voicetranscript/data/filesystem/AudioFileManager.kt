package com.voicetranscript.data.filesystem

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getAudioCacheDirectory(): File {
        return File(context.cacheDir, "audio_cache").apply {
            if (!exists()) mkdirs()
        }
    }

    fun getTempWavFile(): File {
        return File(getAudioCacheDirectory(), "input_16khz.wav")
    }

    fun getTempInputFile(extension: String = "tmp"): File {
        return File(getAudioCacheDirectory(), "input_raw.$extension")
    }

    fun clearCache() {
        getAudioCacheDirectory().deleteRecursively()
    }
}
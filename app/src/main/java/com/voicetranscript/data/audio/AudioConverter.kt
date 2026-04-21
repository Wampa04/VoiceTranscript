package com.voicetranscript.data.audio

import android.content.Context
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.voicetranscript.data.filesystem.AudioFileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioConverter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioFileManager: AudioFileManager
) {
    suspend fun convertToWhisperFormat(inputUri: Uri): File? = withContext(Dispatchers.IO) {
        val tempInputFile = audioFileManager.getTempInputFile()
        val outputFile = audioFileManager.getTempWavFile()
        
        if (outputFile.exists()) outputFile.delete()
        if (tempInputFile.exists()) tempInputFile.delete()

        // 1. Copy URI content to a temporary file that FFmpeg can read
        try {
            context.contentResolver.openInputStream(inputUri)?.use { input ->
                tempInputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy input URI to temp file")
            return@withContext null
        }

        // 2. FFmpeg command using the local file path
        // -i: input
        // -ar 16000: sampling rate 16kHz
        // -ac 1: mono
        // -y: overwrite output file
        val command = "-i \"${tempInputFile.absolutePath}\" -ar 16000 -ac 1 -y \"${outputFile.absolutePath}\""

        Timber.d("Running FFmpeg command: $command")
        
        val session = FFmpegKit.execute(command)
        if (ReturnCode.isSuccess(session.returnCode)) {
            Timber.d("FFmpeg conversion successful")
            outputFile
        } else {
            Timber.e("FFmpeg conversion failed with state ${session.state} and return code ${session.returnCode}")
            null
        }
    }
}
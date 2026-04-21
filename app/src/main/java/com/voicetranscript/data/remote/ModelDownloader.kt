package com.voicetranscript.data.remote

import com.voicetranscript.data.filesystem.WhisperModelManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelDownloader @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val whisperModelManager: WhisperModelManager
) {
    private val baseUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main"

    fun downloadModel(modelName: String): Flow<DownloadState> = flow {
        val modelFile = whisperModelManager.getModelFile(modelName)
        if (modelFile.exists()) {
            emit(DownloadState.Success(modelFile.absolutePath))
            return@flow
        }

        val url = "$baseUrl/ggml-$modelName.bin"
        val request = Request.Builder().url(url).build()

        try {
            emit(DownloadState.Downloading(0f))
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val body = response.body ?: throw IOException("Empty response body")
                val totalBytes = body.contentLength()
                var downloadedBytes = 0L

                modelFile.outputStream().use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            if (totalBytes > 0) {
                                emit(DownloadState.Downloading(downloadedBytes.toFloat() / totalBytes))
                            }
                        }
                    }
                }
                emit(DownloadState.Success(modelFile.absolutePath))
            }
        } catch (e: Exception) {
            if (modelFile.exists()) modelFile.delete()
            emit(DownloadState.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    sealed class DownloadState {
        data class Downloading(val progress: Float) : DownloadState()
        data class Success(val path: String) : DownloadState()
        data class Error(val message: String) : DownloadState()
    }
}
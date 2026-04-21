package com.voicetranscript.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicetranscript.AudioLanguage
import com.voicetranscript.WhisperModel
import com.voicetranscript.data.audio.AudioConverter
import com.voicetranscript.data.filesystem.AudioFileManager
import com.voicetranscript.data.filesystem.WhisperModelManager
import com.voicetranscript.data.remote.ModelDownloader
import com.voicetranscript.ndk.WhisperLib
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val modelDownloader: ModelDownloader,
    private val whisperModelManager: WhisperModelManager,
    private val audioConverter: AudioConverter,
    private val audioFileManager: AudioFileManager,
    private val whisperLib: WhisperLib
) : ViewModel() {

    private val _selectedLanguage = mutableStateOf(AudioLanguage.AUTO)
    val selectedLanguage: State<AudioLanguage> = _selectedLanguage

    private val _selectedFileUri = mutableStateOf<Uri?>(null)
    val selectedFileUri: State<Uri?> = _selectedFileUri

    private val _selectedModel = mutableStateOf(WhisperModel.TINY)
    val selectedModel: State<WhisperModel> = _selectedModel

    private val _isSettingsOpen = mutableStateOf(false)
    val isSettingsOpen: State<Boolean> = _isSettingsOpen

    private val _downloadState = mutableStateOf<ModelDownloader.DownloadState?>(null)
    val downloadState: State<ModelDownloader.DownloadState?> = _downloadState

    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    private val _downloadedModels = mutableStateOf<Set<String>>(emptySet())
    val downloadedModels: State<Set<String>> = _downloadedModels

    private val _transcriptionText = mutableStateOf("")
    val transcriptionText: State<String> = _transcriptionText

    private var currentContextHandle: Long = 0L
    private var lastModelPath: String? = null

    init {
        // Bereinige den Audio-Cache beim Start
        audioFileManager.clearCache()
        refreshDownloadedModels()
    }

    private fun refreshDownloadedModels() {
        _downloadedModels.value = WhisperModel.entries
            .filter { whisperModelManager.isModelDownloaded(it.modelName) }
            .map { it.modelName }
            .toSet()
    }

    fun selectLanguage(language: AudioLanguage) {
        _selectedLanguage.value = language
    }

    fun selectFile(uri: Uri?) {
        _selectedFileUri.value = uri
        // Optional: Cache leeren, wenn eine neue Datei gewählt wird
        audioFileManager.clearCache()
        _transcriptionText.value = ""
    }

    fun selectModel(model: WhisperModel) {
        _selectedModel.value = model
        _downloadState.value = if (whisperModelManager.isModelDownloaded(model.modelName)) {
            ModelDownloader.DownloadState.Success(whisperModelManager.getModelFile(model.modelName).absolutePath)
        } else {
            null
        }
    }

    fun setSettingsOpen(isOpen: Boolean) {
        _isSettingsOpen.value = isOpen
    }

    fun downloadSelectedModel() {
        val modelName = _selectedModel.value.modelName
        viewModelScope.launch {
            modelDownloader.downloadModel(modelName).collect { state ->
                _downloadState.value = state
                if (state is ModelDownloader.DownloadState.Success) {
                    refreshDownloadedModels()
                }
            }
        }
    }

    fun deleteModel(model: WhisperModel) {
        viewModelScope.launch {
            // Falls das Modell gerade geladen ist, Context freigeben
            val modelFile = whisperModelManager.getModelFile(model.modelName)
            if (lastModelPath == modelFile.absolutePath) {
                if (currentContextHandle != 0L) {
                    whisperLib.freeContext(currentContextHandle)
                    currentContextHandle = 0L
                    lastModelPath = null
                }
            }

            val deleted = whisperModelManager.deleteModel(model.modelName)
            if (deleted) {
                refreshDownloadedModels()
                if (_selectedModel.value == model) {
                    _downloadState.value = null
                }
            }
        }
    }

    fun transcribe() {
        val uri = _selectedFileUri.value ?: return
        val modelState = _downloadState.value
        if (modelState !is ModelDownloader.DownloadState.Success) {
            _transcriptionText.value = "Bitte Modell zuerst herunterladen."
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true

            try {
                // 1. Modell laden oder wechseln, falls nötig
                if (currentContextHandle == 0L || lastModelPath != modelState.path) {
                    _transcriptionText.value = "Lade Modell in den RAM..."
                    if (currentContextHandle != 0L) {
                        whisperLib.freeContext(currentContextHandle)
                    }
                    currentContextHandle = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        whisperLib.init(modelState.path)
                    }
                    lastModelPath = modelState.path

                    if (currentContextHandle == 0L) {
                        _transcriptionText.value = "Fehler: Modell konnte nicht initialisiert werden."
                        _isProcessing.value = false
                        return@launch
                    }
                }

                // 2. Audio konvertieren
                _transcriptionText.value = "Konvertiere Audio..."
                val wavFile = audioConverter.convertToWhisperFormat(uri)

                if (wavFile != null) {
                    _transcriptionText.value = "Transkribiere..."
                    val langCode = _selectedLanguage.value.code
                    // 3. JNI-Inferenz mit dem Handle und Sprache ausführen
                    val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                        whisperLib.transcribeFile(
                            contextHandle = currentContextHandle,
                            audioPath = wavFile.absolutePath,
                            language = langCode
                        )
                    }
                    _transcriptionText.value = result
                } else {
                    _transcriptionText.value = "Fehler bei der Audio-Konvertierung."
                }
            } catch (e: Exception) {
                _transcriptionText.value = "Fehler: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (currentContextHandle != 0L) {
            whisperLib.freeContext(currentContextHandle)
            currentContextHandle = 0L
        }
    }

    fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND) {
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            uri?.let { _selectedFileUri.value = it }
        }
    }
}
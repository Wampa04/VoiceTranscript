# 🎯 Technischer Fahrplan – Android App „Voice Transcript"

**Status:** MVP-Planung für Kotlin/Compose Anfänger  
**Zielplattform:** Android (Min SDK 28+, Kotlin/Compose)  
**Zeitschätzung:** 6–10 Wochen für MVP  

---

## 1. Projekt-Übersicht

### Kern-Features (MVP)
- ✅ Android Share-Sheet Integration (WhatsApp Audio & generische Audio-Dateien)
- ✅ Offline Spracherkennung via whisper.cpp (de/en)
- ✅ Manuelle Sprachwahl vor Transkription (Auto/Deutsch/Englisch)
- ✅ Modellauswahl & -verwaltung (tiny/base/small)
- ✅ On-Demand Modell-Downloads (nicht gebündelt)
- ✅ Copy-to-Clipboard Funktion
- ✅ Modelle einzeln löschbar

### Nicht im MVP (v1.1+)
- ❌ Zusammenfassungen (auch nicht lokal via Gemma)
- ❌ History / Verlauf
- ❌ Automatische Spracherkennung (der Nutzer wählt)
- ❌ DirectWhatsApp Integration

---

## 2. Tech-Stack

| Komponente | Wahl | Begründung |
|------------|------|-----------|
| **UI Framework** | Jetpack Compose | Modern, Kotlin-native, beste DX für Anfänger |
| **Architecture** | MVVM + Repository | Standard Android, gut dokumentiert |
| **Async** | Kotlin Coroutines + Flow | Async ohne Callbacks, leicht zu verstehen |
| **Dependency Injection** | Hilt | Offziell, einfach, wenig Boilerplate |
| **File Access** | Scoped Storage API | Android 10+ Compliance |
| **Speech Recognition** | whisper.cpp (JNI) | Offline, multi-lingual, bewährt |
| **Data Storage** | Room (lokal) | Transkript-Metadaten, Modell-Info |
| **Download Manager** | OkHttp + Custom Flow | Robust, mit Progress-Tracking |

---

## 3. Projekt-Struktur

```
VoiceTranscript/
│
├── app/
│   ├── build.gradle.kts              # App-Level Konfiguration
│   ├── proguard-rules.pro
│   ├── src/main/
│   │   ├── AndroidManifest.xml       # Share-Intent Handling
│   │   ├── kotlin/com/voicetranscript/
│   │   │   ├── MainActivity.kt       # Entry Point
│   │   │   │
│   │   │   ├── ui/
│   │   │   │   ├── screens/
│   │   │   │   │   ├── TranscribeScreen.kt      # Haupt-UI
│   │   │   │   │   ├── SettingsScreen.kt        # Modellauswahl
│   │   │   │   │   └── ModelManagerScreen.kt    # Downloads/Löschen
│   │   │   │   ├── components/
│   │   │   │   │   ├── AudioPlayerPreview.kt
│   │   │   │   │   ├── TranscriptionResult.kt
│   │   │   │   │   ├── LanguageSelector.kt
│   │   │   │   │   └── ModelDownloadProgress.kt
│   │   │   │   └── theme/
│   │   │   │       ├── Color.kt
│   │   │   │       ├── Typography.kt
│   │   │   │       └── Theme.kt
│   │   │   │
│   │   │   ├── viewmodel/
│   │   │   │   ├── TranscribeViewModel.kt
│   │   │   │   ├── SettingsViewModel.kt
│   │   │   │   └── ModelManagerViewModel.kt
│   │   │   │
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── TranscriptionResult.kt
│   │   │   │   │   ├── AudioLanguage.kt
│   │   │   │   │   ├── WhisperModel.kt
│   │   │   │   │   └── TranscriptionState.kt
│   │   │   │   │
│   │   │   │   └── usecase/
│   │   │   │       ├── TranscribeAudioUseCase.kt
│   │   │   │       ├── DownloadModelUseCase.kt
│   │   │   │       ├── DeleteModelUseCase.kt
│   │   │   │       ├── GetAvailableModelsUseCase.kt
│   │   │   │       └── SelectLanguageUseCase.kt
│   │   │   │
│   │   │   ├── data/
│   │   │   │   ├── repository/
│   │   │   │   │   ├── TranscriptionRepository.kt
│   │   │   │   │   ├── ModelRepository.kt
│   │   │   │   │   └── SettingsRepository.kt
│   │   │   │   │
│   │   │   │   ├── datasource/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── database/
│   │   │   │   │   │   │   ├── TranscriptDb.kt
│   │   │   │   │   │   │   ├── ModelInfoDao.kt
│   │   │   │   │   │   │   └── entities/
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── preferences/
│   │   │   │   │   │   │   └── SettingsDataStore.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   └── filesystem/
│   │   │   │   │   │       └── AudioFileManager.kt
│   │   │   │   │   │
│   │   │   │   │   └── remote/
│   │   │   │   │       └── ModelDownloader.kt
│   │   │   │   │
│   │   │   │   └── mapper/
│   │   │   │       └── (Entity ↔ Domain Mapper)
│   │   │   │
│   │   │   ├── integration/
│   │   │   │   ├── ndk/
│   │   │   │   │   └── WhisperNative.kt   # JNI Interface
│   │   │   │   │
│   │   │   │   └── share/
│   │   │   │       └── ShareIntentHandler.kt
│   │   │   │
│   │   │   └── di/
│   │   │       ├── AppModule.kt           # Global Singletons
│   │   │       ├── RepositoryModule.kt
│   │   │       └── DataSourceModule.kt
│   │   │
│   │   └── res/
│   │       ├── drawable/
│   │       ├── values/
│   │       └── xml/
│   │           └── file_paths.xml        # Für Scoped Storage
│   │
│   └── src/test/
│       └── (Unit Tests)
│
├── whisper-jni/                        # ← Separate NDK Module
│   ├── build.gradle.kts                # NDK Konfiguration
│   ├── src/main/
│   │   ├── cpp/
│   │   │   ├── CMakeLists.txt          # CMake Build-Config
│   │   │   ├── whisper_jni.cpp         # JNI Wrapper
│   │   │   ├── whisper_binding.h       # Header
│   │   │   └── (whisper.cpp submodule)
│   │   │
│   │   └── kotlin/
│   │       └── com/voicetranscript/ndk/
│   │           ├── WhisperNative.kt    # Kotlin Interface
│   │           └── WhisperException.kt
│   │
│   └── src/androidTest/
│       └── (Native Tests)
│
├── build.gradle.kts                   # Root Build Config
├── settings.gradle.kts                # Gradle Settings
├── gradle/
│   └── libs.versions.toml              # Version Catalog
│
└── .gitignore                         # + /whisper-jni/src/main/cpp/.../
```

---

## 4. Abhängigkeiten (Dependencies)

### build.gradle.kts (app-level)

```kotlin
android {
    compileSdk = 35
    defaultConfig {
        minSdk = 28
        targetSdk = 35
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.activity:activity-compose:1.8.0")

    // ViewModel & State Management
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Dependency Injection (Hilt)
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // DataStore (Preferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // File Access
    implementation("androidx.core:core:1.12.0")

    // Networking (für Modell-Downloads)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Local NDK Module
    implementation(project(":whisper-jni"))
}
```

### build.gradle.kts (whisper-jni)

```kotlin
plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 35

    defaultConfig {
        minSdk = 28
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64"))
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.7.0")
}
```

---

## 5. whisper.cpp Integration (JNI)

### 5.1 CMakeLists.txt (whisper-jni/src/main/cpp/)

```cmake
cmake_minimum_required(VERSION 3.22.1)
project(whisper-jni)

# whisper.cpp Submodule
add_subdirectory(whisper.cpp EXCLUDE_FROM_ALL)

# JNI Library
add_library(
    whisper-jni
    SHARED
    whisper_jni.cpp
)

target_link_libraries(
    whisper-jni
    PRIVATE
    whisper
    log
)

target_include_directories(
    whisper-jni
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/whisper.cpp/include
)
```

### 5.2 whisper_jni.cpp (Minimal Example)

```cpp
#include <jni.h>
#include <string>
#include <vector>
#include "whisper.h"
#include "log.h"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "WhisperJNI", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "WhisperJNI", __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_voicetranscript_ndk_WhisperNative_initContext(
    JNIEnv *env, jclass clazz, jstring model_path) {
    
    const char *path = env->GetStringUTFChars(model_path, nullptr);
    whisper_context *ctx = whisper_init_from_file(path);
    env->ReleaseStringUTFChars(model_path, path);
    
    if (ctx == nullptr) {
        LOGE("Failed to initialize whisper context");
        return 0;
    }
    
    return (jlong)ctx;
}

JNIEXPORT void JNICALL
Java_com_voicetranscript_ndk_WhisperNative_freeContext(
    JNIEnv *env, jclass clazz, jlong context_ptr) {
    
    auto *ctx = (whisper_context *)context_ptr;
    if (ctx != nullptr) {
        whisper_free(ctx);
    }
}

JNIEXPORT jstring JNICALL
Java_com_voicetranscript_ndk_WhisperNative_transcribe(
    JNIEnv *env, jclass clazz, jlong context_ptr, jstring audio_path, 
    jstring language) {
    
    auto *ctx = (whisper_context *)context_ptr;
    if (ctx == nullptr) return env->NewStringUTF("Error: invalid context");
    
    const char *path = env->GetStringUTFChars(audio_path, nullptr);
    const char *lang = env->GetStringUTFChars(language, nullptr);
    
    // Read WAV file (simplified)
    std::vector<float> pcmf32;
    // ... WAV parsing code ...
    
    // Set parameters
    whisper_full_params params = whisper_full_default_params(
        WHISPER_SAMPLING_GREEDY);
    params.language = lang;
    params.n_threads = 4;
    
    // Run transcription
    int ret = whisper_full(ctx, params, pcmf32.data(), pcmf32.size());
    
    std::string result;
    if (ret == 0) {
        int n_segments = whisper_full_n_segments(ctx);
        for (int i = 0; i < n_segments; ++i) {
            result += whisper_full_get_segment_text(ctx, i);
        }
    } else {
        result = "Transcription failed";
    }
    
    env->ReleaseStringUTFChars(audio_path, path);
    env->ReleaseStringUTFChars(language, lang);
    
    return env->NewStringUTF(result.c_str());
}

} // extern "C"
```

### 5.3 WhisperNative.kt (Kotlin Interface)

```kotlin
package com.voicetranscript.ndk

object WhisperNative {
    init {
        System.loadLibrary("whisper-jni")
    }

    external fun initContext(modelPath: String): Long
    external fun freeContext(contextPtr: Long)
    external fun transcribe(contextPtr: Long, audioPath: String, language: String): String

    class WhisperException(message: String) : Exception(message)
}
```

---

## 6. Core Features – Implementation Roadmap

### Phase 1: Foundation (Woche 1–2)
- [ ] Gradle Setup + whisper-jni Modul
- [ ] Basis-Projekt mit Compose MainScreen
- [ ] MVVM Struktur + Hilt DI
- [ ] Scoped Storage / File-Handling
- [ ] Share Intent Handler

**Output:** App öffnet sich über Share-Sheet, zeigt Audio-Preview

---

### Phase 2: Model Management (Woche 3–4)
- [ ] Model Download-Manager (OkHttp)
- [ ] Local Storage (App-Cache, Modell-Dateien)
- [ ] Settings-Screen (Modellauswahl: tiny/base/small)
- [ ] Model Delete Funktionalität
- [ ] Progress-UI

**Output:** User kann Modelle auswählen/downloaden/löschen

---

### Phase 3: Spracherkennung (Woche 5–6)
- [ ] WhisperNative JNI Integration testen
- [ ] Audio-Format-Konvertierung (opus/ogg → wav/pcm)
- [ ] Transcribe-ViewModell + Use Case
- [ ] Language Selector UI (Auto/DE/EN)
- [ ] Loading State + Error Handling

**Output:** User kann Audio transkribieren

---

### Phase 4: Polish (Woche 7–8)
- [ ] Copy-to-Clipboard
- [ ] UX Optimierungen
- [ ] Error Messages (User-freundlich)
- [ ] Performance Tuning
- [ ] Hardware-Testing (Mittelklasse-Geräte)

**Output:** MVP Release-Ready

---

### Phase 5: Testing & Deployment (Woche 9–10)
- [ ] Unit Tests (ViewModel, Use Cases)
- [ ] Integration Tests
- [ ] Device-Tests (Firebase Test Lab oder real)
- [ ] App Signing
- [ ] Google Play Beta Release

**Output:** App im Play Store (Beta oder Public)

---

## 7. Android Share-Intent Handling

### AndroidManifest.xml

```xml
<activity
    android:name=".MainActivity"
    android:exported="true">
    
    <!-- Eigener Launch -->
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    
    <!-- Share via Intent -->
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="audio/*" />
        <data android:mimeType="application/ogg" />
        <data android:mimeType="application/x-ogg" />
    </intent-filter>
</activity>
```

### MainActivity.kt – Intent Handler

```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val shareIntentHandler: ShareIntentHandler,
) : ViewModel() {
    
    init {
        // In der Activity:
        // val intent = LocalContext.current.currentContext?.intent
        // viewModel.handleIntent(intent)
    }
    
    fun handleIntent(intent: Intent?) {
        intent ?: return
        
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                uri?.let {
                    viewModelScope.launch {
                        shareIntentHandler.processAudio(it)
                    }
                }
            }
        }
    }
}
```

---

## 8. Scoped Storage & File-Handling

```kotlin
// AudioFileManager.kt
@HiltViewModel
class AudioFileManager @Inject constructor(
    private val context: Context,
) {
    
    suspend fun copyAudioToCache(uri: Uri): File = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open audio file")
        
        val cacheFile = File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}.wav")
        
        inputStream.use { input ->
            cacheFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        return@withContext cacheFile
    }
    
    fun getModelsDirectory(): File {
        return File(context.filesDir, "whisper_models")
            .apply { mkdirs() }
    }
    
    fun getModelPath(modelName: String): File {
        return File(getModelsDirectory(), "$modelName.bin")
    }
}
```

---

## 9. Model Download + Progress Tracking

```kotlin
// ModelDownloader.kt
@HiltViewModel
class ModelDownloader @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val audioFileManager: AudioFileManager,
) {
    
    fun downloadModel(
        modelName: String,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): Flow<Result<File>> = flow {
        try {
            val url = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/$modelName-q5_0.bin"
            val request = Request.Builder().url(url).build()
            
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Download failed: ${response.code}")
                }
                
                val body = response.body ?: throw IOException("No response body")
                val totalBytes = body.contentLength()
                var downloadedBytes = 0L
                
                val modelFile = audioFileManager.getModelPath(modelName)
                modelFile.outputStream().use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            onProgress(downloadedBytes, totalBytes)
                        }
                    }
                }
                
                emit(Result.success(modelFile))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
```

---

## 10. ViewModel Example (TranscribeViewModel)

```kotlin
@HiltViewModel
class TranscribeViewModel @Inject constructor(
    private val transcribeUseCase: TranscribeAudioUseCase,
    private val modelRepository: ModelRepository,
) : ViewModel() {
    
    private val _transcriptionState = MutableStateFlow<TranscriptionState>(
        TranscriptionState.Idle
    )
    val transcriptionState: StateFlow<TranscriptionState> = _transcriptionState.asStateFlow()
    
    private val _selectedLanguage = MutableStateFlow(AudioLanguage.AUTO)
    val selectedLanguage: StateFlow<AudioLanguage> = _selectedLanguage.asStateFlow()
    
    private val _selectedModel = MutableStateFlow("base")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()
    
    fun selectLanguage(language: AudioLanguage) {
        _selectedLanguage.value = language
    }
    
    fun selectModel(model: String) {
        _selectedModel.value = model
    }
    
    fun transcribeAudio(audioFile: File) {
        viewModelScope.launch {
            _transcriptionState.value = TranscriptionState.Loading
            
            try {
                val result = transcribeUseCase(
                    audioFile = audioFile,
                    language = _selectedLanguage.value,
                    model = _selectedModel.value,
                )
                _transcriptionState.value = TranscriptionState.Success(result)
            } catch (e: Exception) {
                _transcriptionState.value = TranscriptionState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class TranscriptionState {
    data object Idle : TranscriptionState()
    data object Loading : TranscriptionState()
    data class Success(val text: String) : TranscriptionState()
    data class Error(val message: String) : TranscriptionState()
}
```

---

## 11. Compose UI – Haupt-Screen

```kotlin
// TranscribeScreen.kt
@Composable
fun TranscribeScreen(
    viewModel: TranscribeViewModel = hiltViewModel(),
) {
    val state by viewModel.transcriptionState.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Title
        Text("Voice Transcript", style = MaterialTheme.typography.headlineLarge)
        
        // Language Selector
        LanguageSelector(
            selected = selectedLanguage,
            onSelect = viewModel::selectLanguage,
        )
        
        // Model Selector
        ModelSelector(
            selected = selectedModel,
            onSelect = viewModel::selectModel,
        )
        
        // Transcription Result
        when (state) {
            TranscriptionState.Idle -> {
                Text("Warte auf Audio-Datei...")
            }
            TranscriptionState.Loading -> {
                CircularProgressIndicator()
                Text("Wird transkribiert...")
            }
            is TranscriptionState.Success -> {
                TranscriptionResultBox(
                    text = (state as TranscriptionState.Success).text,
                    onCopy = { /* Copy to clipboard */ },
                )
            }
            is TranscriptionState.Error -> {
                Text(
                    "Fehler: ${(state as TranscriptionState.Error).message}",
                    color = Color.Red,
                )
            }
        }
    }
}

@Composable
fun LanguageSelector(
    selected: AudioLanguage,
    onSelect: (AudioLanguage) -> Unit,
) {
    val languages = listOf(
        AudioLanguage.AUTO to "Automatisch",
        AudioLanguage.DEUTSCH to "Deutsch",
        AudioLanguage.ENGLISH to "English",
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        languages.forEach { (lang, label) ->
            FilterChip(
                selected = selected == lang,
                onClick = { onSelect(lang) },
                label = { Text(label) },
            )
        }
    }
}
```

---

## 12. Testing-Strategie

### Unit Tests (ViewModel)

```kotlin
// TranscribeViewModelTest.kt
@ExtendWith(InstantExecutorExtension::class)
class TranscribeViewModelTest {
    
    private lateinit var viewModel: TranscribeViewModel
    private val mockUseCase = mockk<TranscribeAudioUseCase>()
    private val mockRepository = mockk<ModelRepository>()
    
    @Before
    fun setup() {
        viewModel = TranscribeViewModel(mockUseCase, mockRepository)
    }
    
    @Test
    fun `transcribe sets loading state`() = runTest {
        // Arrange
        val audioFile = mockk<File>()
        coEvery { mockUseCase(...) } returns "Test result"
        
        // Act
        viewModel.transcribeAudio(audioFile)
        
        // Assert
        assertEquals(TranscriptionState.Loading, viewModel.transcriptionState.value)
    }
}
```

### Integration Tests

- Teste Share-Intent Handling
- Teste Model Download + Verifikation
- Teste Scoped Storage Zugriff

---

## 13. Performance Optimierungen (für Mittelklasse-Hardware)

| Problem | Lösung |
|---------|--------|
| **Große Modell-Dateien** | Lazy-Loading, Streaming Download |
| **Audio-Konvertierung** | Threading (Dispatchers.IO) |
| **UI-Freezing** | Coroutines + StateFlow (nicht LiveData) |
| **Memory Leaks** | Proper JNI Context Release |
| **Battery Drain** | Threading tunnen, CPU-Nutzung begrenzen |

---

## 14. Häufige Anfänger-Pitfalls

| Pitfall | Vermeidung |
|---------|-----------|
| JNI Segmentation Faults | Memory Leaks → proper cleanup in C++ |
| UI-Thread blocking | Immer Dispatchers.IO für I/O |
| Permission Errors (Scoped Storage) | test on emulator + real device |
| Model Path Wrong | Logs + testing mit lokalen Dateien |
| Compose Recomposition Spam | `remember` + `derivedStateOf` nutzen |
| APK-Größe explodiert | Proguard + R8 Obfuscation |

---

## 15. Deployment Checklist

### Vor Release:
- [ ] ProGuard Rules konfiguriert
- [ ] Minify aktiviert (`minifyEnabled = true`)
- [ ] API-Level Testing (API 28–35)
- [ ] Device-Testing (mind. 2 real devices)
- [ ] Crash Reporting Setup (Firebase Crashlytics)
- [ ] App Signing Zertifikat generiert
- [ ] Privacy Policy vorbereitet

### Google Play Console:
- [ ] App-Titel & Beschreibung
- [ ] Screenshots (mind. 2)
- [ ] Icon + Feature Graphic
- [ ] Content Rating ausfüllen
- [ ] Beta oder Public Release

---

## 16. Nächste konkrete Schritte

1. **Repo initialisieren**
   ```bash
   git init VoiceTranscript
   git submodule add https://github.com/ggerganov/whisper.cpp whisper-jni/src/main/cpp/whisper.cpp
   ```

2. **Android Studio Project**
   - Neue "Empty Activity (Compose)" App erstellen
   - whisper-jni Module hinzufügen
   - build.gradle.kts Dateien kopieren/anpassen

3. **Hello World** auf echtem Gerät / Emulator
   - Verify Compose UI funktioniert
   - Verify NDK Build erfolgreich

4. **Share-Intent Handler** implementieren
   - Test mit echtem Audio von WhatsApp

5. **Model Download** implementieren
   - Test mit tiny Model (~40MB)

---

## 17. Ressourcen & Links

- **Android Docs:** https://developer.android.com/docs
- **Jetpack Compose:** https://developer.android.com/jetpack/compose
- **whisper.cpp GitHub:** https://github.com/ggerganov/whisper.cpp
- **Android NDK:** https://developer.android.com/ndk
- **Hilt DI:** https://developer.android.com/training/dependency-injection/hilt-android
- **Scoped Storage:** https://developer.android.com/about/versions/11/privacy/storage
- **OkHttp:** https://square.github.io/okhttp/

---

**Status:** Bereit für MVP-Entwicklung ✅  
**Geschätzter Aufwand:** 6–10 Wochen für Anfänger (mit Pair-Programming/Support)

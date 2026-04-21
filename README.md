# VoiceTranscript

VoiceTranscript ist eine Android-Anwendung, die mithilfe von `whisper.cpp` (OpenAI Whisper) Audioaufnahmen lokal auf dem Gerät transkribiert. Die App legt besonderen Wert auf Datenschutz, da die Verarbeitung offline erfolgt.

## Features

- **Lokale Transkription:** Nutzt die Whisper-Modelle direkt auf dem Smartphone via JNI/NDK.
- **Modell-Management:** Verschiedene Whisper-Modelle (Base, Tiny, etc.) können innerhalb der App heruntergeladen und ausgewählt werden.
- **Sprachauswahl:** Unterstützung für verschiedene Audiosprachen.
- **Share Intent:** Audio-Dateien können direkt aus anderen Apps an VoiceTranscript geteilt werden.
- **Material 3 Design:** Moderne Benutzeroberfläche mit Jetpack Compose.

## Tech Stack

- **Sprache:** Kotlin
- **UI:** Jetpack Compose
- **Architektur:** MVVM mit Dagger Hilt (Dependency Injection)
- **Audio-Engine:** `whisper.cpp` via C++ JNI
- **Netzwerk:** OkHttp für den Modell-Download
- **Hintergrund:** FFmpeg-Kit für die Audio-Konvertierung (WAV 16kHz)

## Projektstruktur

- `app/`: Die Android-Anwendung (Compose UI, ViewModels, Hilt-Module).
- `whisper-jni/`: NDK-Modul, das die C++-Anbindung an `whisper.cpp` bereitstellt.
- `external/whisper.cpp`: Git-Submodul mit der originalen Whisper-Implementierung.

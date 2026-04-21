package com.voicetranscript.ndk

import timber.log.Timber

class WhisperLib {
    /**
     * Transkribiert eine Audio-Datei mit einem bereits initialisierten Kontext.
     * @param contextHandle Der von init() zurückgegebene Handle
     * @param audioPath Pfad zur Audio-Datei (.wav, 16kHz mono)
     * @param language Sprachcode (z.B. "de", "en" oder "auto")
     * @return Der transkribierte Text
     */
    external fun transcribeFile(contextHandle: Long, audioPath: String, language: String): String

    /**
     * Initialisiert den Whisper-Kontext und gibt einen Handle zurück.
     * @param modelPath Pfad zur Whisper-Model-Datei (.bin)
     * @return Der Pointer zum C++ Kontext (als Long)
     */
    external fun init(modelPath: String): Long

    /**
     * Gibt den Speicher des Kontexts wieder frei.
     */
    external fun freeContext(contextHandle: Long)

    companion object {
        init {
            try {
                System.loadLibrary("whisper-jni")
                Timber.d("Whisper JNI Library erfolgreich geladen")
            } catch (e: UnsatisfiedLinkError) {
                Timber.e(e, "Fehler beim Laden der Whisper JNI Library")
            }
        }
    }
}

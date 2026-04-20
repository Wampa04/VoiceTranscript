package com.voicetranscript.ndk

import android.util.Log

class WhisperLib {
    /**
     * Transkribiert eine Audio-Datei.
     * @param modelPath Pfad zur Whisper-Model-Datei (.bin)
     * @param audioPath Pfad zur Audio-Datei (.wav, 16kHz mono)
     * @return Der transkribierte Text
     */
    external fun transcribeFile(modelPath: String, audioPath: String): String

    /**
     * Initialisiert das Whisper-Modul (optional, falls für Caching benötigt)
     */
    external fun init(modelPath: String): Long

    companion object {
        private const val TAG = "WhisperLib"

        init {
            try {
                System.loadLibrary("whisper-jni")
                Log.d(TAG, "Whisper JNI Library erfolgreich geladen")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Fehler beim Laden der Whisper JNI Library", e)
            }
        }
    }
}

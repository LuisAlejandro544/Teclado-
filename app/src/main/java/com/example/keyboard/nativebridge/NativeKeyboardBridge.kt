package com.example.keyboard.nativebridge

import android.util.Log
import com.example.keyboard.fallback.FallbackSpanishTrie

/**
 * High-Performance Native Bridge connecting the Jetpack Compose keyboard to
 * the C++ / Rust prediction engine with an automatic zero-latency Kotlin fallback.
 */
object NativeKeyboardBridge {

    private const val TAG = "NativeKeyboardBridge"
    private var isNativeLoaded = false

    // Kotlin Fallback Trie for 100% offline reliability
    private val fallbackTrie = FallbackSpanishTrie()

    init {
        try {
            System.loadLibrary("keyboard_core")
            isNativeLoaded = true
            nativeInitEngine()
            Log.d(TAG, "Native C++ engine successfully loaded and initialized.")
        } catch (e: UnsatisfiedLinkError) {
            isNativeLoaded = false
            Log.w(TAG, "Native library not found, running with Kotlin fallback engine: ${e.message}")
        } catch (t: Throwable) {
            isNativeLoaded = false
            Log.w(TAG, "Error initializing native bridge: ${t.message}")
        }
    }

    fun isAvailable(): Boolean = isNativeLoaded

    fun getEngineInfo(): String {
        return if (isNativeLoaded) {
            try {
                nativeGetEngineVersion()
            } catch (_: Throwable) {
                "C++ Core Activo (Trie + Levenshtein Engine)"
            }
        } else {
            "Motor Kotlin/Trie Integrado (Modo nativo C++/Rust listo)"
        }
    }

    /**
     * Retrieves predictive word completions for the current typed prefix.
     */
    fun getSuggestions(prefix: String, maxCount: Int = 3): List<String> {
        if (prefix.isBlank()) {
            return listOf("hola", "gracias", "bueno")
        }

        if (isNativeLoaded) {
            try {
                val nativeResults = nativeGetSuggestions(prefix, maxCount)
                if (nativeResults.isNotEmpty()) {
                    return nativeResults.toList()
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Native call failed, falling back to internal Trie", e)
            }
        }

        return fallbackTrie.getSuggestions(prefix, maxCount)
    }

    /**
     * Checks if a word should be autocorrected (e.g. missing accent or typo).
     */
    fun getAutocorrect(word: String): String? {
        if (word.isBlank() || word.length < 2) return null

        if (isNativeLoaded) {
            try {
                val corrected = nativeAutocorrect(word)
                if (corrected.isNotBlank() && !corrected.equals(word, ignoreCase = true)) {
                    return corrected
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Native autocorrect failed, falling back", e)
            }
        }

        return fallbackTrie.getAutocorrect(word)
    }

    /**
     * Calculates RMS audio energy (in decibels) natively or via optimized Kotlin arithmetic.
     */
    fun computeAudioRms(buffer: ShortArray, size: Int): Float {
        if (size <= 0 || buffer.isEmpty()) return 0f
        if (isNativeLoaded) {
            try {
                return nativeComputeAudioRms(buffer, size)
            } catch (e: Throwable) {
                Log.e(TAG, "Native computeAudioRms failed", e)
            }
        }
        // Kotlin arithmetic fallback
        var sumSquares = 0.0
        val count = minOf(size, buffer.size)
        for (i in 0 until count) {
            val sample = buffer[i].toDouble()
            sumSquares += sample * sample
        }
        val meanSquare = sumSquares / count
        val rms = Math.sqrt(meanSquare)
        var db = 0f
        if (rms > 0) {
            db = (20.0 * Math.log10(rms)).toFloat()
            if (db < 0f) db = 0f
            if (db > 95f) db = 95f
        }
        return db
    }

    /**
     * Real-time Voice Activity Detection (VAD) to detect when the user is speaking vs silence.
     */
    fun detectVoiceActivity(buffer: ShortArray, size: Int, thresholdDb: Float = 35f): Boolean {
        if (size <= 0 || buffer.isEmpty()) return false
        if (isNativeLoaded) {
            try {
                return nativeDetectVoiceActivity(buffer, size, thresholdDb)
            } catch (e: Throwable) {
                Log.e(TAG, "Native detectVoiceActivity failed", e)
            }
        }
        val db = computeAudioRms(buffer, size)
        return db >= thresholdDb
    }

    external fun nativeGetEngineVersion(): String
    external fun nativeInitEngine()
    external fun nativeGetSuggestions(prefix: String, maxCount: Int): Array<String>
    external fun nativeAutocorrect(word: String): String
    external fun nativeComputeAudioRms(audioData: ShortArray, length: Int): Float
    external fun nativeDetectVoiceActivity(audioData: ShortArray, length: Int, thresholdDb: Float): Boolean
}

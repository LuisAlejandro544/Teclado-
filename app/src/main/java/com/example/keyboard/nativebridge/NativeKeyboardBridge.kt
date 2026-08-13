package com.example.keyboard.nativebridge

import android.util.Log

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

/**
 * Built-in Spanish Trie with common accent normalization and frequency sorting.
 */
private class FallbackSpanishTrie {

    private class Node {
        val children = mutableMapOf<Char, Node>()
        var isWord = false
        var word: String = ""
        var frequency: Int = 0
    }

    private val root = Node()

    private val spanishVocabulary = listOf(
        Pair("hola", 100), Pair("que", 98), Pair("qué", 97), Pair("para", 95), Pair("como", 94),
        Pair("cómo", 93), Pair("está", 92), Pair("esta", 91), Pair("este", 90), Pair("pero", 89),
        Pair("todo", 88), Pair("toda", 87), Pair("bien", 86), Pair("bueno", 85), Pair("buenos", 84),
        Pair("buenas", 83), Pair("días", 82), Pair("tardes", 81), Pair("noches", 80), Pair("gracias", 95),
        Pair("por", 96), Pair("favor", 90), Pair("también", 88), Pair("tambien", 70), Pair("donde", 85),
        Pair("dónde", 86), Pair("cuando", 85), Pair("cuándo", 86), Pair("porque", 89), Pair("tiempo", 80),
        Pair("ahora", 84), Pair("después", 78), Pair("despues", 60), Pair("siempre", 82), Pair("nunca", 76),
        Pair("hacer", 84), Pair("hecho", 75), Pair("hace", 80), Pair("amigo", 78), Pair("amiga", 75),
        Pair("familia", 75), Pair("trabajo", 80), Pair("casa", 82), Pair("vida", 78), Pair("año", 80),
        Pair("años", 82), Pair("día", 85), Pair("hoy", 88), Pair("mañana", 82), Pair("ayer", 78),
        Pair("semana", 76), Pair("mes", 74), Pair("español", 85), Pair("españa", 80), Pair("méxico", 78),
        Pair("argentina", 75), Pair("colombia", 75), Pair("mundo", 79), Pair("persona", 75), Pair("personas", 76),
        Pair("hombre", 74), Pair("mujer", 74), Pair("niño", 72), Pair("niña", 72), Pair("nuevo", 78),
        Pair("nueva", 77), Pair("grande", 78), Pair("pequeño", 75), Pair("mucho", 82), Pair("mucha", 80),
        Pair("muchos", 78), Pair("poco", 78), Pair("algo", 80), Pair("nada", 80), Pair("alguien", 78),
        Pair("nadie", 76), Pair("cada", 78), Pair("primero", 75), Pair("último", 75), Pair("posible", 75),
        Pair("fácil", 78), Pair("difícil", 76), Pair("rápido", 78), Pair("claro", 80), Pair("seguro", 78),
        Pair("solo", 80), Pair("sólo", 78), Pair("pronto", 78), Pair("tarde", 78), Pair("aquí", 86),
        Pair("ahí", 82), Pair("allí", 80), Pair("arriba", 76), Pair("abajo", 76), Pair("dentro", 76),
        Pair("fuera", 76), Pair("mensaje", 85), Pair("teclado", 90), Pair("texto", 84), Pair("pantalla", 80),
        Pair("teléfono", 82), Pair("celular", 80), Pair("correo", 78), Pair("número", 80), Pair("cuenta", 78),
        Pair("aplicación", 82), Pair("app", 85), Pair("foto", 80), Pair("video", 80), Pair("música", 80),
        Pair("respuesta", 80), Pair("pregunta", 80), Pair("problema", 80), Pair("solución", 80), Pair("ayuda", 82),
        Pair("estoy", 86), Pair("estás", 84), Pair("estamos", 82), Pair("tengo", 84), Pair("tienes", 82),
        Pair("puedo", 84), Pair("puedes", 82), Pair("podemos", 80), Pair("quiero", 84), Pair("quieres", 82),
        Pair("vamos", 85), Pair("sé", 80), Pair("sabes", 82), Pair("árbol", 75), Pair("canción", 75),
        Pair("corazón", 75), Pair("avión", 72), Pair("atención", 75), Pair("dirección", 75)
    )

    init {
        for ((word, freq) in spanishVocabulary) {
            insert(word, freq)
        }
    }

    private fun normalize(input: String): String {
        return input.lowercase()
            .replace('á', 'a')
            .replace('é', 'e')
            .replace('í', 'i')
            .replace('ó', 'o')
            .replace('ú', 'u')
            .replace('ü', 'u')
    }

    private fun insert(word: String, freq: Int) {
        var current = root
        val normalized = normalize(word)
        for (ch in normalized) {
            current = current.children.getOrPut(ch) { Node() }
        }
        current.isWord = true
        current.word = word
        current.frequency = maxOf(current.frequency, freq)
    }

    fun getSuggestions(prefix: String, maxCount: Int): List<String> {
        val norm = normalize(prefix)
        var current: Node? = root
        for (ch in norm) {
            current = current?.children?.get(ch)
            if (current == null) break
        }

        val matches = mutableListOf<Pair<String, Int>>()
        if (current != null) {
            collectWords(current, matches)
        }

        matches.sortByDescending { it.second }
        val result = matches.take(maxCount).map { it.first }.toMutableList()

        if (result.isEmpty()) {
            result.add(prefix)
        }
        return result
    }

    fun getAutocorrect(word: String): String? {
        val norm = normalize(word)
        val suggestions = getSuggestions(word, 3)
        for (sug in suggestions) {
            if (normalize(sug) == norm && sug != word) {
                return sug // Missing accent found! (e.g. arbol -> árbol, tambien -> también)
            }
        }
        return null
    }

    private fun collectWords(node: Node, results: MutableList<Pair<String, Int>>) {
        if (node.isWord) {
            results.add(Pair(node.word, node.frequency))
        }
        for (child in node.children.values) {
            collectWords(child, results)
        }
    }
}

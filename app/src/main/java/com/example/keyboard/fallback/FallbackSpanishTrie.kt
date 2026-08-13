package com.example.keyboard.fallback

import com.example.keyboard.dictionary.SpanishVocabularyCorpus

/**
 * Built-in Spanish Trie with common accent normalization and frequency sorting
 * for zero-latency offline predictions and autocorrect.
 */
class FallbackSpanishTrie {

    private class Node {
        val children = mutableMapOf<Char, Node>()
        var isWord = false
        var word: String = ""
        var frequency: Int = 0
    }

    private val root = Node()

    init {
        for ((word, freq) in SpanishVocabularyCorpus.WORDS) {
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
            if (normalize(sug) == norm && !sug.equals(word, ignoreCase = false)) {
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

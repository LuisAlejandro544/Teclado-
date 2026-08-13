package com.example.keyboard.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Data model for a clipboard item.
 * @param id Unique identifier
 * @param text The copied content
 * @param timestamp Time when the text was added (in milliseconds)
 * @param isPinned If true, item will NEVER be auto-deleted after 1 hour.
 */
data class ClipboardItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
) {
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        // Items that are pinned never expire. Unpinned items expire after 1 hour (3600 * 1000 ms).
        val oneHourMillis = 3600_000L
        return !isPinned && (now - timestamp > oneHourMillis)
    }

    fun remainingMinutes(now: Long = System.currentTimeMillis()): Int {
        if (isPinned) return -1
        val oneHourMillis = 3600_000L
        val elapsed = now - timestamp
        val remaining = (oneHourMillis - elapsed).coerceAtLeast(0L)
        return (remaining / (60 * 1000L)).toInt()
    }
}

object ClipboardRepository {
    private const val PREFS_NAME = "keyboard_clipboard_store"
    private const val KEY_CLIPS = "saved_clipboard_items"
    private const val MAX_UNPINNED_ITEMS = 30

    private var prefs: SharedPreferences? = null
    private val _clips = MutableStateFlow<List<ClipboardItem>>(emptyList())
    val clips: StateFlow<List<ClipboardItem>> = _clips.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadAndPrune()
        }
    }

    private fun loadAndPrune() {
        val jsonString = prefs?.getString(KEY_CLIPS, null) ?: "[]"
        val loadedList = mutableListOf<ClipboardItem>()
        try {
            val array = JSONArray(jsonString)
            val now = System.currentTimeMillis()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", UUID.randomUUID().toString())
                val text = obj.optString("text", "")
                val timestamp = obj.optLong("timestamp", now)
                val isPinned = obj.optBoolean("isPinned", false)

                if (text.isNotBlank()) {
                    val item = ClipboardItem(id, text, timestamp, isPinned)
                    // Auto-delete unpinned items older than 1 hour
                    if (!item.isExpired(now)) {
                        loadedList.add(item)
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback gracefully on parsing issues
        }

        // Sort: Pinned first, then newest timestamp
        val sorted = loadedList.sortedWith(
            compareByDescending<ClipboardItem> { it.isPinned }
                .thenByDescending { it.timestamp }
        )
        _clips.value = sorted
        saveToPrefs(sorted)
    }

    private fun saveToPrefs(items: List<ClipboardItem>) {
        try {
            val array = JSONArray()
            for (item in items) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("text", item.text)
                    put("timestamp", item.timestamp)
                    put("isPinned", item.isPinned)
                }
                array.put(obj)
            }
            prefs?.edit()?.putString(KEY_CLIPS, array.toString())?.apply()
        } catch (_: Exception) {}
    }

    /**
     * Captures current text from the Android system clipboard if new.
     */
    fun syncWithSystemClipboard(context: Context) {
        init(context)
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val primaryClip = clipboard?.primaryClip
            if (primaryClip != null && primaryClip.itemCount > 0) {
                val clipText = primaryClip.getItemAt(0).coerceToText(context)?.toString()?.trim()
                if (!clipText.isNullOrBlank()) {
                    addClip(clipText, isPinned = false)
                }
            }
        } catch (_: Exception) {}
        loadAndPrune()
    }

    /**
     * Adds a new item to clipboard history.
     */
    fun addClip(text: String, isPinned: Boolean = false) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val currentList = _clips.value.toMutableList()
        // If already exists, update timestamp and possibly pin status
        val existingIndex = currentList.indexOfFirst { it.text == trimmed }
        val now = System.currentTimeMillis()

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            val updated = existing.copy(
                timestamp = now,
                isPinned = if (isPinned) true else existing.isPinned
            )
            currentList.removeAt(existingIndex)
            currentList.add(0, updated)
        } else {
            val newItem = ClipboardItem(
                id = UUID.randomUUID().toString(),
                text = trimmed,
                timestamp = now,
                isPinned = isPinned
            )
            currentList.add(0, newItem)
        }

        // Limit unpinned items count to preserve memory
        val unpinned = currentList.filter { !it.isPinned }.take(MAX_UNPINNED_ITEMS)
        val pinned = currentList.filter { it.isPinned }
        val finalMerged = (pinned + unpinned).sortedWith(
            compareByDescending<ClipboardItem> { it.isPinned }
                .thenByDescending { it.timestamp }
        )

        _clips.value = finalMerged
        saveToPrefs(finalMerged)
    }

    /**
     * Toggles pin status. Pinned items are saved permanently and won't be deleted after 1 hour.
     */
    fun togglePin(id: String) {
        val currentList = _clips.value.map { item ->
            if (item.id == id) {
                item.copy(isPinned = !item.isPinned)
            } else {
                item
            }
        }.sortedWith(
            compareByDescending<ClipboardItem> { it.isPinned }
                .thenByDescending { it.timestamp }
        )
        _clips.value = currentList
        saveToPrefs(currentList)
    }

    /**
     * Deletes a single item immediately.
     */
    fun deleteClip(id: String) {
        val currentList = _clips.value.filter { it.id != id }
        _clips.value = currentList
        saveToPrefs(currentList)
    }

    /**
     * Deletes all unpinned clips (leaving pinned clips intact).
     */
    fun clearUnpinned() {
        val currentList = _clips.value.filter { it.isPinned }
        _clips.value = currentList
        saveToPrefs(currentList)
    }

    /**
     * Copies text back to the system clipboard.
     */
    fun copyToSystemClipboard(context: Context, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("Teclado Blanco", text)
            clipboard?.setPrimaryClip(clip)
        } catch (_: Exception) {}
    }
}

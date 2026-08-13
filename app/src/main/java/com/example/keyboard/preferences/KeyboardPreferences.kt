package com.example.keyboard.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object KeyboardPreferences {
    private const val PREFS_NAME = "keyboard_user_preferences"
    private const val KEY_SHOW_NUMBER_ROW = "pref_show_number_row"
    private const val KEY_ENABLE_CLIPBOARD_HISTORY = "pref_enable_clipboard_history"

    private var prefs: SharedPreferences? = null

    private val _showNumberRow = MutableStateFlow(true)
    val showNumberRow: StateFlow<Boolean> = _showNumberRow.asStateFlow()

    private val _clipboardHistoryEnabled = MutableStateFlow(true)
    val clipboardHistoryEnabled: StateFlow<Boolean> = _clipboardHistoryEnabled.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val numberRowVal = prefs?.getBoolean(KEY_SHOW_NUMBER_ROW, true) ?: true
            val clipboardVal = prefs?.getBoolean(KEY_ENABLE_CLIPBOARD_HISTORY, true) ?: true
            _showNumberRow.value = numberRowVal
            _clipboardHistoryEnabled.value = clipboardVal
        }
    }

    fun setShowNumberRow(context: Context, enabled: Boolean) {
        init(context)
        prefs?.edit()?.putBoolean(KEY_SHOW_NUMBER_ROW, enabled)?.apply()
        _showNumberRow.value = enabled
    }

    fun isNumberRowEnabled(context: Context): Boolean {
        init(context)
        return _showNumberRow.value
    }

    fun setClipboardHistoryEnabled(context: Context, enabled: Boolean) {
        init(context)
        prefs?.edit()?.putBoolean(KEY_ENABLE_CLIPBOARD_HISTORY, enabled)?.apply()
        _clipboardHistoryEnabled.value = enabled
    }

    fun isClipboardHistoryEnabled(context: Context): Boolean {
        init(context)
        return _clipboardHistoryEnabled.value
    }
}

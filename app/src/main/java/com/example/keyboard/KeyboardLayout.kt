package com.example.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.example.keyboard.emoji.EmojiManager
import com.example.keyboard.nativebridge.NativeKeyboardBridge
import com.example.keyboard.ui.components.LongPressPopupOverlay
import com.example.keyboard.ui.components.SuggestionStrip
import com.example.keyboard.ui.components.VoiceDictationStrip
import com.example.keyboard.ui.layouts.EmojiKeyboardLayout
import com.example.keyboard.ui.layouts.LettersKeyboardLayout
import com.example.keyboard.ui.layouts.SymbolsPage1Layout
import com.example.keyboard.ui.layouts.SymbolsPage2Layout
import com.example.keyboard.voice.VoiceState

/**
 * Main Keyboard Composable coordinating state, layers, suggestion strip,
 * voice recognition and popups.
 */
@Composable
fun KeyboardView(
    modifier: Modifier = Modifier,
    imeActionLabel: String? = null,
    imeActionIconType: String = "enter", // "enter", "search", "send", "done", "next"
    voiceState: VoiceState = VoiceState.Idle,
    onKeyAction: (KeyAction) -> Unit
) {
    var keyboardMode by remember { mutableStateOf(KeyboardMode.LETTERS) }
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var lastShiftTapTime by remember { mutableLongStateOf(0L) }
    var composingWord by remember { mutableStateOf("") }
    var currentSuggestions by remember { mutableStateOf(NativeKeyboardBridge.getSuggestions("")) }

    // Popup state for long-press diacritics / variants
    var popupKeyTarget by remember { mutableStateOf<String?>(null) }
    var popupVariants by remember { mutableStateOf<List<String>>(emptyList()) }

    val view = LocalView.current

    val triggerHaptic: () -> Unit = {
        try {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } catch (_: Exception) {}
    }

    val updateSuggestionsForPrefix: (String) -> Unit = { prefix ->
        currentSuggestions = NativeKeyboardBridge.getSuggestions(prefix, 3)
    }

    val handleKeyAction: (KeyAction) -> Unit = { action ->
        triggerHaptic()
        when (action) {
            is KeyAction.Shift -> {
                val now = System.currentTimeMillis()
                if (now - lastShiftTapTime < 350) {
                    shiftState = if (shiftState == ShiftState.CAPS_LOCK) ShiftState.OFF else ShiftState.CAPS_LOCK
                } else {
                    shiftState = when (shiftState) {
                        ShiftState.OFF -> ShiftState.ON
                        ShiftState.ON -> ShiftState.OFF
                        ShiftState.CAPS_LOCK -> ShiftState.OFF
                    }
                }
                lastShiftTapTime = now
            }
            is KeyAction.SwitchToSymbols -> {
                keyboardMode = KeyboardMode.NUMBERS_SYMBOLS
            }
            is KeyAction.SwitchToMoreSymbols -> {
                keyboardMode = KeyboardMode.MORE_SYMBOLS
            }
            is KeyAction.SwitchToLetters -> {
                keyboardMode = KeyboardMode.LETTERS
            }
            is KeyAction.SwitchToEmojis -> {
                keyboardMode = KeyboardMode.EMOJIS
            }
            is KeyAction.InsertEmoji -> {
                EmojiManager.addRecentEmoji(action.emoji)
                onKeyAction(action)
            }
            is KeyAction.Text -> {
                val charToEmit = if (shiftState != ShiftState.OFF) {
                    action.text.uppercase()
                } else {
                    action.text.lowercase()
                }

                if (charToEmit.length == 1 && (charToEmit[0].isLetterOrDigit() || charToEmit == "'")) {
                    composingWord += charToEmit
                } else {
                    composingWord = ""
                }
                updateSuggestionsForPrefix(composingWord)

                onKeyAction(KeyAction.Text(charToEmit))

                if (shiftState == ShiftState.ON) {
                    shiftState = ShiftState.OFF
                }
            }
            is KeyAction.Backspace -> {
                if (composingWord.isNotEmpty()) {
                    composingWord = composingWord.dropLast(1)
                }
                updateSuggestionsForPrefix(composingWord)
                onKeyAction(KeyAction.Backspace)
            }
            is KeyAction.Space -> {
                val autocorrect = if (composingWord.isNotEmpty()) {
                    NativeKeyboardBridge.getAutocorrect(composingWord)
                } else null

                if (autocorrect != null && !autocorrect.equals(composingWord, ignoreCase = true)) {
                    onKeyAction(KeyAction.CommitSuggestion(autocorrect))
                } else {
                    onKeyAction(KeyAction.Space)
                }
                composingWord = ""
                updateSuggestionsForPrefix("")
            }
            is KeyAction.CommitSuggestion -> {
                onKeyAction(action)
                composingWord = ""
                updateSuggestionsForPrefix("")
            }
            is KeyAction.Enter -> {
                composingWord = ""
                updateSuggestionsForPrefix("")
                onKeyAction(KeyAction.Enter)
            }
            else -> {
                onKeyAction(action)
            }
        }
    }

    val openLongPressPopup: (String) -> Unit = { char ->
        val isUpper = shiftState != ShiftState.OFF
        val variants = SpecialCharactersMap.getVariants(char, isUpper)
        if (!variants.isNullOrEmpty()) {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            } catch (_: Exception) {}
            popupKeyTarget = char
            popupVariants = variants
        }
    }

    val dismissPopup: () -> Unit = {
        popupKeyTarget = null
        popupVariants = emptyList()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F5F9)),
        color = Color(0xFFF1F5F9),
        shadowElevation = 10.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (keyboardMode != KeyboardMode.EMOJIS) {
                    // 1. Suggestion & Autocorrect Strip OR Active Voice Dictation Strip
                    if (voiceState is VoiceState.Listening || voiceState is VoiceState.NoPermission) {
                        VoiceDictationStrip(
                            voiceState = voiceState,
                            onStop = { handleKeyAction(KeyAction.StopVoiceInput) },
                            onCancel = { handleKeyAction(KeyAction.CancelVoiceInput) }
                        )
                    } else {
                        SuggestionStrip(
                            suggestions = currentSuggestions,
                            currentWord = composingWord,
                            onSelectSuggestion = { word ->
                                handleKeyAction(KeyAction.CommitSuggestion(word))
                            },
                            onStartVoice = {
                                handleKeyAction(KeyAction.StartVoiceInput)
                            }
                        )
                    }
                }

                // 2. Main Keyboard Layouts
                when (keyboardMode) {
                    KeyboardMode.LETTERS -> {
                        LettersKeyboardLayout(
                            shiftState = shiftState,
                            imeActionLabel = imeActionLabel,
                            imeActionIconType = imeActionIconType,
                            onAction = handleKeyAction,
                            onTriggerHaptic = triggerHaptic,
                            onLongPress = openLongPressPopup
                        )
                    }
                    KeyboardMode.NUMBERS_SYMBOLS -> {
                        SymbolsPage1Layout(
                            imeActionLabel = imeActionLabel,
                            imeActionIconType = imeActionIconType,
                            onAction = handleKeyAction,
                            onTriggerHaptic = triggerHaptic,
                            onLongPress = openLongPressPopup
                        )
                    }
                    KeyboardMode.MORE_SYMBOLS -> {
                        SymbolsPage2Layout(
                            imeActionLabel = imeActionLabel,
                            imeActionIconType = imeActionIconType,
                            onAction = handleKeyAction,
                            onTriggerHaptic = triggerHaptic,
                            onLongPress = openLongPressPopup
                        )
                    }
                    KeyboardMode.EMOJIS -> {
                        EmojiKeyboardLayout(
                            imeActionLabel = imeActionLabel,
                            imeActionIconType = imeActionIconType,
                            onAction = handleKeyAction,
                            onTriggerHaptic = triggerHaptic
                        )
                    }
                }
            }

            // 3. Floating Long-Press Diacritic & Accent Popup Overlay
            val currentPopupTarget = popupKeyTarget
            if (currentPopupTarget != null && popupVariants.isNotEmpty()) {
                LongPressPopupOverlay(
                    baseKey = currentPopupTarget,
                    variants = popupVariants,
                    onSelect = { selectedVariant ->
                        handleKeyAction(KeyAction.Text(selectedVariant))
                        dismissPopup()
                    },
                    onDismiss = dismissPopup
                )
            }
        }
    }
}

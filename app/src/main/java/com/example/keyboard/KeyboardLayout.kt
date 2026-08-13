package com.example.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.keyboard.emoji.EmojiCategory
import com.example.keyboard.emoji.EmojiManager
import com.example.keyboard.nativebridge.NativeKeyboardBridge
import com.example.keyboard.voice.VoiceState
import kotlinx.coroutines.delay

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

/**
 * Emoji Keyboard View with automatic device compatibility detection.
 */
@Composable
private fun EmojiKeyboardLayout(
    imeActionLabel: String?,
    imeActionIconType: String,
    onAction: (KeyAction) -> Unit,
    onTriggerHaptic: () -> Unit
) {
    var selectedCategoryIndex by remember { mutableStateOf(0) } // 0: Recientes, 1..6: Categories
    val categories = EmojiCategory.values()

    val totalSupported = remember { EmojiManager.getTotalSupportedCount() }

    val currentEmojis = remember(selectedCategoryIndex) {
        if (selectedCategoryIndex == 0) {
            EmojiManager.getRecentEmojis()
        } else {
            val cat = categories[selectedCategoryIndex - 1]
            EmojiManager.getSupportedEmojis(cat)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Categories & Compatibility Badge Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Category Chips (Recientes, Caras, Animales, Comida, etc.)
                    CategoryChip(
                        icon = "🕒",
                        isSelected = selectedCategoryIndex == 0,
                        onClick = { selectedCategoryIndex = 0 }
                    )
                    categories.forEachIndexed { idx, cat ->
                        CategoryChip(
                            icon = cat.icon,
                            isSelected = selectedCategoryIndex == idx + 1,
                            onClick = { selectedCategoryIndex = idx + 1 }
                        )
                    }
                }

                // Supported Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFECFDF5),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFA7F3D0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$totalSupported compatibles",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }
        }

        // Emoji Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White, RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                .padding(4.dp)
        ) {
            if (currentEmojis.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Sin emojis recientes aún",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(currentEmojis, key = { it }) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onAction(KeyAction.InsertEmoji(emoji))
                                }
                                .testTag("emoji_item_$emoji"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 23.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bottom navigation row for Emoji keyboard (ABC, ?123, Space, Backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpecialKeyButton(
                modifier = Modifier.weight(1.4f),
                backgroundColor = Color(0xFFE2E8F0),
                onClick = { onAction(KeyAction.SwitchToLetters) }
            ) {
                Text(
                    text = "ABC",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
            }

            SpecialKeyButton(
                modifier = Modifier.weight(1.4f),
                backgroundColor = Color(0xFFE2E8F0),
                onClick = { onAction(KeyAction.SwitchToSymbols) }
            ) {
                Text(
                    text = "?123",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
            }

            SpecialKeyButton(
                modifier = Modifier.weight(3.5f),
                backgroundColor = Color.White,
                onClick = { onAction(KeyAction.Space) }
            ) {
                Text(
                    text = "espacio",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            RepeatingBackspaceKey(
                modifier = Modifier.weight(1.35f),
                onDelete = { onAction(KeyAction.Backspace) },
                onTriggerHaptic = onTriggerHaptic
            )
        }
    }
}

@Composable
private fun CategoryChip(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF1F5F9))
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) Color(0xFF3B82F6) else Color(0xFFCBD5E1),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 16.sp)
    }
}

/**
 * Top Suggestion Strip showing 3 real-time candidates from the C++/Rust/Trie engine
 * plus a quick Voice Dictation microphone button.
 */
@Composable
private fun SuggestionStrip(
    suggestions: List<String>,
    currentWord: String,
    onSelectSuggestion: (String) -> Unit,
    onStartVoice: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE2E8F0).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFCBD5E1).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 4.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val displayList = if (suggestions.isNotEmpty()) {
                suggestions
            } else {
                listOf("hola", "gracias", "bueno")
            }

            displayList.take(3).forEachIndexed { index, suggestion ->
                val isCenter = index == 1 || (displayList.size == 1 && index == 0)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isCenter && currentWord.isNotEmpty()) Color(0xFFDBEAFE)
                            else Color.White.copy(alpha = 0.8f)
                        )
                        .border(
                            width = if (isCenter && currentWord.isNotEmpty()) 1.dp else 0.5.dp,
                            color = if (isCenter && currentWord.isNotEmpty()) Color(0xFF93C5FD) else Color(0xFFCBD5E1),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectSuggestion(suggestion) }
                        .testTag("suggestion_chip_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        if (isCenter && currentWord.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text(
                            text = suggestion,
                            fontSize = 14.sp,
                            fontWeight = if (isCenter && currentWord.isNotEmpty()) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCenter && currentWord.isNotEmpty()) Color(0xFF1E40AF) else Color(0xFF334155),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Quick Mic Dictation Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFDBEAFE))
                    .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(6.dp))
                    .clickable { onStartVoice() }
                    .testTag("key_voice_mic"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Dictado por Voz Offline",
                    tint = Color(0xFF1D4ED8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Animated Voice Dictation Strip showing real-time live waveform, partial transcription,
 * and quick confirmation/cancellation controls.
 */
@Composable
private fun VoiceDictationStrip(
    voiceState: VoiceState,
    onStop: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                .border(1.5.dp, Color(0xFF3B82F6), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (voiceState) {
                is VoiceState.Listening -> {
                    // Pulsating Mic Icon & Dynamic Waveform
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Grabando voz",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Equalizer wave bars
                    Row(
                        modifier = Modifier.width(36.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val bars = voiceState.waveform
                        bars.forEach { factor ->
                            val barHeight = (22.dp * factor).coerceIn(4.dp, 22.dp)
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF2563EB))
                            )
                        }
                    }

                    // Live Speech Preview Text
                    Text(
                        text = if (voiceState.partialText.isNotBlank()) voiceState.partialText else "Escuchando... (100% privado)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E3A8A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Done & Commit button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .clickable { onStop() }
                            .testTag("voice_done_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Listo / Confirmar",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Cancel button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                            .clickable { onCancel() }
                            .testTag("voice_cancel_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar voz",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                is VoiceState.NoPermission -> {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "Sin permiso",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Permiso de micrófono no concedido. Actívalo en la app.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF991B1B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2))
                            .clickable { onCancel() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar aviso",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                else -> {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Floating Long Press Popup displaying accent variations (á, é, í, ó, ú, ñ, ç, 1-0)
 */
@Composable
private fun LongPressPopupOverlay(
    baseKey: String,
    variants: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, 10),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 14.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF94A3B8).copy(alpha = 0.4f)),
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .wrapContentSize()
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Variantes de '$baseKey'",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    variants.forEach { variant ->
                        Box(
                            modifier = Modifier
                                .size(width = 38.dp, height = 46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .clickable { onSelect(variant) }
                                .testTag("popup_variant_$variant"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = variant,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LettersKeyboardLayout(
    shiftState: ShiftState,
    imeActionLabel: String?,
    imeActionIconType: String,
    onAction: (KeyAction) -> Unit,
    onTriggerHaptic: () -> Unit,
    onLongPress: (String) -> Unit
) {
    val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ñ")
    val row3 = listOf("z", "x", "c", "v", "b", "n", "m")

    // Row 1 (QWERTY with number hints on long press)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        row1.forEachIndexed { index, char ->
            val displayChar = if (shiftState != ShiftState.OFF) char.uppercase() else char
            val hintDigit = ((index + 1) % 10).toString()
            KeyButton(
                modifier = Modifier.weight(1f),
                label = displayChar,
                secondaryHint = hintDigit,
                onClick = { onAction(KeyAction.Text(char)) },
                onLongPress = { onLongPress(char) }
            )
        }
    }

    // Row 2 (ASDFGHJKLÑ)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        row2.forEach { char ->
            val displayChar = if (shiftState != ShiftState.OFF) char.uppercase() else char
            KeyButton(
                modifier = Modifier.weight(1f),
                label = displayChar,
                secondaryHint = if (char == "a") "á" else if (char == "e") "é" else null,
                onClick = { onAction(KeyAction.Text(char)) },
                onLongPress = { onLongPress(char) }
            )
        }
    }

    // Row 3 (Shift + ZXCVBNM + Backspace)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shift Key
        val shiftBgColor = when (shiftState) {
            ShiftState.OFF -> Color(0xFFE2E8F0)
            ShiftState.ON -> Color(0xFFDBEAFE)
            ShiftState.CAPS_LOCK -> Color(0xFF2563EB)
        }
        val shiftIconColor = when (shiftState) {
            ShiftState.OFF -> Color(0xFF334155)
            ShiftState.ON -> Color(0xFF1D4ED8)
            ShiftState.CAPS_LOCK -> Color.White
        }

        SpecialKeyButton(
            modifier = Modifier.weight(1.35f),
            backgroundColor = shiftBgColor,
            onClick = { onAction(KeyAction.Shift) }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "Shift / Mayúsculas",
                tint = shiftIconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        row3.forEach { char ->
            val displayChar = if (shiftState != ShiftState.OFF) char.uppercase() else char
            KeyButton(
                modifier = Modifier.weight(1f),
                label = displayChar,
                secondaryHint = if (char == "c") "ç" else null,
                onClick = { onAction(KeyAction.Text(char)) },
                onLongPress = { onLongPress(char) }
            )
        }

        // Backspace Key with Repeat Delete
        RepeatingBackspaceKey(
            modifier = Modifier.weight(1.35f),
            onDelete = { onAction(KeyAction.Backspace) },
            onTriggerHaptic = onTriggerHaptic
        )
    }

    // Row 4 (Mode Switch ?123, Emoji Key, Comma, Space, Dot, Enter)
    BottomActionRow(
        modeLabel = "?123",
        onModeSwitch = { onAction(KeyAction.SwitchToSymbols) },
        imeActionLabel = imeActionLabel,
        imeActionIconType = imeActionIconType,
        onAction = onAction,
        onLongPress = onLongPress
    )
}

@Composable
private fun SymbolsPage1Layout(
    imeActionLabel: String?,
    imeActionIconType: String,
    onAction: (KeyAction) -> Unit,
    onTriggerHaptic: () -> Unit,
    onLongPress: (String) -> Unit
) {
    val row1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val row2 = listOf("@", "#", "$", "%", "&", "*", "-", "+", "(", ")")
    val row3 = listOf("!", "\"", "'", ":", ";", "/", "?")

    // Row 1 (Numbers)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        row1.forEach { char ->
            KeyButton(
                modifier = Modifier.weight(1f),
                label = char,
                onClick = { onAction(KeyAction.Text(char)) },
                onLongPress = { onLongPress(char) }
            )
        }
    }

    // Row 2 (Primary Symbols)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        row2.forEach { char ->
            KeyButton(
                modifier = Modifier.weight(1f),
                label = char,
                secondaryHint = if (char == "$") "€" else if (char == "-") "_" else null,
                onClick = { onAction(KeyAction.Text(char)) },
                onLongPress = { onLongPress(char) }
            )
        }
    }

    // Row 3 (Switch to More Symbols + Secondary Symbols + Backspace)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpecialKeyButton(
            modifier = Modifier.weight(1.35f),
            backgroundColor = Color(0xFFE2E8F0),
            onClick = { onAction(KeyAction.SwitchToMoreSymbols) }
        ) {
            Text(
                text = "=\\<",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1E293B)
            )
        }

        row3.forEach { char ->
            KeyButton(
                modifier = Modifier.weight(1f),
                label = char,
                secondaryHint = if (char == "?") "¿" else if (char == "!") "¡" else null,
                onClick = { onAction(KeyAction.Text(char)) },
                onLongPress = { onLongPress(char) }
            )
        }

        RepeatingBackspaceKey(
            modifier = Modifier.weight(1.35f),
            onDelete = { onAction(KeyAction.Backspace) },
            onTriggerHaptic = onTriggerHaptic
        )
    }

    // Row 4 (Switch to Letters ABC, Emoji Key, Comma, Space, Dot, Enter)
    BottomActionRow(
        modeLabel = "ABC",
        onModeSwitch = { onAction(KeyAction.SwitchToLetters) },
        imeActionLabel = imeActionLabel,
        imeActionIconType = imeActionIconType,
        onAction = onAction,
        onLongPress = onLongPress
    )
}

@Composable
private fun SymbolsPage2Layout(
    imeActionLabel: String?,
    imeActionIconType: String,
    onAction: (KeyAction) -> Unit,
    onTriggerHaptic: () -> Unit,
    onLongPress: (String) -> Unit
) {
    val row1 = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "§", "Δ")
    val row2 = listOf("£", "€", "¥", "¢", "^", "°", "=", "{", "}", "\\")
    val row3 = listOf("%", "_", "[", "]", "<", ">", "¿", "¡")

    // Row 1
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        row1.forEach { char ->
            KeyButton(
                modifier = Modifier.weight(1f),
                label = char,
                onClick = { onAction(KeyAction.Text(char)) },
                onLongPress = { onLongPress(char) }
            )
        }
    }

    // Row 2
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        row2.forEach { char ->
            KeyButton(
                modifier = Modifier.weight(1f),
                label = char,
                onClick = { onAction(KeyAction.Text(char)) },
                onLongPress = { onLongPress(char) }
            )
        }
    }

    // Row 3 (Switch to 123 + Symbols + Backspace)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpecialKeyButton(
            modifier = Modifier.weight(1.35f),
            backgroundColor = Color(0xFFE2E8F0),
            onClick = { onAction(KeyAction.SwitchToSymbols) }
        ) {
            Text(
                text = "123",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1E293B)
            )
        }

        row3.forEach { char ->
            KeyButton(
                modifier = Modifier.weight(1f),
                label = char,
                onClick = { onAction(KeyAction.Text(char)) },
                onLongPress = { onLongPress(char) }
            )
        }

        RepeatingBackspaceKey(
            modifier = Modifier.weight(1.35f),
            onDelete = { onAction(KeyAction.Backspace) },
            onTriggerHaptic = onTriggerHaptic
        )
    }

    // Row 4
    BottomActionRow(
        modeLabel = "ABC",
        onModeSwitch = { onAction(KeyAction.SwitchToLetters) },
        imeActionLabel = imeActionLabel,
        imeActionIconType = imeActionIconType,
        onAction = onAction,
        onLongPress = onLongPress
    )
}

@Composable
private fun BottomActionRow(
    modeLabel: String,
    onModeSwitch: () -> Unit,
    imeActionLabel: String?,
    imeActionIconType: String,
    onAction: (KeyAction) -> Unit,
    onLongPress: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mode Switch Key (?123 or ABC)
        SpecialKeyButton(
            modifier = Modifier.weight(1.3f),
            backgroundColor = Color(0xFFE2E8F0),
            onClick = onModeSwitch
        ) {
            Text(
                text = modeLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1E293B)
            )
        }

        // Emoji Switch Key
        SpecialKeyButton(
            modifier = Modifier.weight(1.1f),
            backgroundColor = Color(0xFFE2E8F0),
            onClick = { onAction(KeyAction.SwitchToEmojis) }
        ) {
            Text(
                text = "😀",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }

        // Comma key
        KeyButton(
            modifier = Modifier.weight(0.9f),
            label = ",",
            onClick = { onAction(KeyAction.Text(",")) },
            onLongPress = { onLongPress(",") }
        )

        // Spacebar
        SpecialKeyButton(
            modifier = Modifier.weight(3.8f),
            backgroundColor = Color.White,
            onClick = { onAction(KeyAction.Space) }
        ) {
            Text(
                text = "espacio",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
        }

        // Period key
        KeyButton(
            modifier = Modifier.weight(0.9f),
            label = ".",
            secondaryHint = "...",
            onClick = { onAction(KeyAction.Text(".")) },
            onLongPress = { onLongPress(".") }
        )

        // Enter / Action Key
        SpecialKeyButton(
            modifier = Modifier.weight(1.4f),
            backgroundColor = Color(0xFF2563EB),
            onClick = { onAction(KeyAction.Enter) }
        ) {
            if (imeActionLabel != null) {
                Text(
                    text = imeActionLabel,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            } else {
                when (imeActionIconType) {
                    "search" -> Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                    "send" -> Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                    "done" -> Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Listo",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                    else -> Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                        contentDescription = "Intro",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun KeyButton(
    modifier: Modifier = Modifier,
    label: String,
    secondaryHint: String? = null,
    onClick: () -> Unit,
    onLongPress: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }

    val animatedBg by animateColorAsState(
        targetValue = if (isPressed) Color(0xFFE2E8F0) else Color.White,
        label = "key_bg",
        animationSpec = tween(durationMillis = 80)
    )

    Box(
        modifier = modifier
            .height(54.dp)
            .shadow(
                elevation = if (isPressed) 0.5.dp else 1.8.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(animatedBg)
            .border(
                width = 1.dp,
                color = Color(0xFFCBD5E1),
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(label) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() },
                    onLongPress = {
                        onLongPress?.invoke()
                    }
                )
            }
            .testTag("key_$label"),
        contentAlignment = Alignment.Center
    ) {
        if (secondaryHint != null) {
            Text(
                text = secondaryHint,
                fontSize = 9.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 4.dp)
            )
        }
        Text(
            text = label,
            fontSize = 21.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SpecialKeyButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedBg by animateColorAsState(
        targetValue = if (isPressed) backgroundColor.copy(alpha = 0.7f) else backgroundColor,
        label = "special_key_bg"
    )

    Box(
        modifier = modifier
            .height(54.dp)
            .shadow(
                elevation = if (isPressed) 0.5.dp else 1.8.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(animatedBg)
            .border(
                width = 1.dp,
                color = Color(0xFFCBD5E1),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun RepeatingBackspaceKey(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    onTriggerHaptic: () -> Unit
) {
    var isHolding by remember { mutableStateOf(false) }
    val currentOnDelete by rememberUpdatedState(onDelete)
    val currentOnTriggerHaptic by rememberUpdatedState(onTriggerHaptic)

    LaunchedEffect(isHolding) {
        if (isHolding) {
            currentOnTriggerHaptic()
            currentOnDelete()
            delay(380) // Initial delay before fast repeating
            while (isHolding) {
                currentOnTriggerHaptic()
                currentOnDelete()
                delay(55) // Fast repeat
            }
        }
    }

    Box(
        modifier = modifier
            .height(54.dp)
            .shadow(
                elevation = if (isHolding) 0.5.dp else 1.8.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHolding) Color(0xFFCBD5E1) else Color(0xFFE2E8F0))
            .border(
                width = 1.dp,
                color = Color(0xFFCBD5E1),
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHolding = true
                        tryAwaitRelease()
                        isHolding = false
                    }
                )
            }
            .testTag("key_backspace"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Borrar",
            tint = Color(0xFF334155),
            modifier = Modifier.size(22.dp)
        )
    }
}

package com.example.keyboard.ui.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.keyboard.KeyAction
import com.example.keyboard.ShiftState
import com.example.keyboard.ui.components.KeyButton
import com.example.keyboard.ui.components.RepeatingBackspaceKey
import com.example.keyboard.ui.components.SpecialKeyButton

@Composable
fun LettersKeyboardLayout(
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

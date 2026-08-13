package com.example.keyboard.ui.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.KeyAction
import com.example.keyboard.ui.components.KeyButton
import com.example.keyboard.ui.components.RepeatingBackspaceKey
import com.example.keyboard.ui.components.SpecialKeyButton

@Composable
fun SymbolsPage1Layout(
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
fun SymbolsPage2Layout(
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
fun BottomActionRow(
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

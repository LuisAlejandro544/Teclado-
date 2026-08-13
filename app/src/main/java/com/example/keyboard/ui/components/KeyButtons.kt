package com.example.keyboard.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

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

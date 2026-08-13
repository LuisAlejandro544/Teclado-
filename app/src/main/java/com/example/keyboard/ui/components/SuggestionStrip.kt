package com.example.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Top Suggestion Strip displaying 3 real-time candidates from the prediction engine
 * and a quick Voice Dictation microphone trigger button.
 */
@Composable
fun SuggestionStrip(
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

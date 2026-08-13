package com.example.ui.main.tabs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.KeyAction
import com.example.keyboard.KeyboardView
import com.example.keyboard.voice.VoiceRecognitionEngine
import com.example.keyboard.voice.VoiceState
import com.example.ui.theme.BluePrimary

@Composable
fun SimulatorContent(
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit,
    previewText: String,
    onTextChange: (String) -> Unit
) {
    val context = LocalContext.current
    val voiceEngine = remember { VoiceRecognitionEngine(context) }
    val voiceState by voiceEngine.state.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            voiceEngine.release()
        }
    }

    LaunchedEffect(voiceState) {
        val state = voiceState
        if (state is VoiceState.Transcribed && state.text.isNotBlank()) {
            val space = if (previewText.isNotEmpty() && !previewText.endsWith(" ")) " " else ""
            onTextChange(previewText + space + state.text + " ")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Output display at top
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Simulador con sugerencias, emojis y voz",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BluePrimary
                            )
                        }
                        if (previewText.isNotEmpty()) {
                            IconButton(
                                onClick = { onTextChange("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (previewText.isEmpty()) "Prueba mantener pulsada una tecla (ej. 'a', 'e', 'o') para ver los acentos, pulsar 🎙️ para dictar por voz, o escribir para ver sugerencias..." else previewText,
                        fontSize = 17.sp,
                        color = if (previewText.isEmpty()) Color(0xFF94A3B8) else Color(0xFF0F172A),
                        lineHeight = 23.sp
                    )
                }

                Text(
                    text = "${previewText.length} caracteres escritos",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Live Keyboard rendering below
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp)),
            color = Color(0xFFF1F5F9)
        ) {
            KeyboardView(
                modifier = Modifier.fillMaxWidth(),
                voiceState = voiceState,
                onKeyAction = { action ->
                    when (action) {
                        is KeyAction.StartVoiceInput -> {
                            if (!hasMicPermission) {
                                onRequestMicPermission()
                            }
                            voiceEngine.startListening()
                        }
                        is KeyAction.StopVoiceInput -> {
                            voiceEngine.stopListening(commit = true)
                        }
                        is KeyAction.CancelVoiceInput -> {
                            voiceEngine.cancel()
                        }
                        is KeyAction.Text -> onTextChange(previewText + action.text)
                        is KeyAction.InsertEmoji -> onTextChange(previewText + action.emoji)
                        is KeyAction.Space -> onTextChange(previewText + " ")
                        is KeyAction.CommitSuggestion -> {
                            // Replace last word or append
                            val lastSpaceIndex = previewText.lastIndexOf(' ')
                            val newText = if (lastSpaceIndex >= 0) {
                                previewText.substring(0, lastSpaceIndex + 1) + action.word + " "
                            } else {
                                action.word + " "
                            }
                            onTextChange(newText)
                        }
                        is KeyAction.Backspace -> {
                            if (previewText.isNotEmpty()) {
                                onTextChange(previewText.dropLast(1))
                            }
                        }
                        is KeyAction.Enter -> onTextChange(previewText + "\n")
                        else -> {}
                    }
                }
            )
        }
    }
}

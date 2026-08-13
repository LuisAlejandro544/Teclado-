package com.example.ui.main.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.nativebridge.NativeKeyboardBridge
import com.example.ui.main.components.FeatureItem
import com.example.ui.main.components.QuickPhraseChip
import com.example.ui.main.components.StepCard
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GreenSuccess

@Composable
fun SetupAndTestingContent(
    isEnabled: Boolean,
    isSelected: Boolean,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit,
    testText: String,
    onTextChange: (String) -> Unit,
    onEnableClick: () -> Unit,
    onSelectClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color(0xFFECFDF5) else Color(0xFFEFF6FF)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isSelected) Color(0xFFA7F3D0) else Color(0xFFBFDBFE)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) GreenSuccess else BluePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (isSelected) "¡Teclado Blanco listo!" else "Pasos para empezar a usarlo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isSelected) Color(0xFF065F46) else Color(0xFF1E40AF)
                    )
                    Text(
                        text = if (isSelected) "Tu teclado ya está funcionando. Puedes probarlo abajo o en cualquier otra app." else "Sigue los 2 pasos a continuación para activar y usar el teclado.",
                        fontSize = 13.sp,
                        color = if (isSelected) Color(0xFF047857) else Color(0xFF3B82F6)
                    )
                }
            }
        }

        // Native Engine Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEDE9FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Motor Nativo de Predicción",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = NativeKeyboardBridge.getEngineInfo(),
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        // Voice to Text & Privacy Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Dictado por Voz Offline",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "100% Privado y Local (Motor C++ VAD)",
                                fontSize = 12.sp,
                                color = GreenSuccess,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (hasMicPermission) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Permiso Concedido ✓",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        }
                    } else {
                        Button(
                            onClick = onRequestMicPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_grant_mic_permission")
                        ) {
                            Text("Permitir Mic", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = "El dictado funciona de manera local sin enviar audio a servidores externos. Detecta pausas de voz automáticamente mediante cálculo nativo RMS/VAD.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF475569),
                    lineHeight = 17.sp
                )
            }
        }

        Text(
            text = "Pasos de Activación",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF0F172A)
        )

        // Step 1: Enable in Settings
        StepCard(
            stepNumber = "1",
            title = "Habilitar en Ajustes del Sistema",
            description = "Activa la casilla de 'Teclado Blanco' en la lista de teclados disponibles del teléfono.",
            isCompleted = isEnabled,
            actionButtonText = if (isEnabled) "Habilitado ✓" else "1. Activar en Ajustes",
            onActionClick = onEnableClick,
            testTag = "btn_enable_keyboard"
        )

        // Step 2: Select as default
        StepCard(
            stepNumber = "2",
            title = "Seleccionar como Teclado Predeterminado",
            description = "Elige 'Teclado Blanco' en el menú selector de métodos de entrada.",
            isCompleted = isSelected,
            actionButtonText = if (isSelected) "Seleccionado ✓" else "2. Elegir Teclado Blanco",
            onActionClick = onSelectClick,
            testTag = "btn_select_keyboard"
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Step 3: Interactive Live Typing Field
        Text(
            text = "Área de Prueba de Escritura",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF0F172A)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Toca el campo de texto para desplegar el teclado y escribir:",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                OutlinedTextField(
                    value = testText,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_input_field"),
                    placeholder = { Text("Escribe aquí con tu Teclado Blanco...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    trailingIcon = {
                        if (testText.isNotEmpty()) {
                            IconButton(onClick = { onTextChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar texto",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        }
                    },
                    minLines = 3,
                    maxLines = 5
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${testText.length} caracteres",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        QuickPhraseChip("¡Hola mundo!") { onTextChange(testText + "¡Hola mundo! ") }
                        QuickPhraseChip("12345") { onTextChange(testText + "12345 ") }
                    }
                }
            }
        }

        // Features Info Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Novedades integradas:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )

                FeatureItem("Acentos y Caracteres Especiales por Pulsación Prolongada (á, é, í, ó, ú, ü, ñ, ç, números rápidos)")
                FeatureItem("Barra de Sugerencias en tiempo real con 3 candidatos y autocorrector inteligente")
                FeatureItem("Teclado de Emojis categorizado con detección automática de soporte del dispositivo")
                FeatureItem("Conexión real con Motor Nativo (C++ / Rust Trie y búsqueda fuzzy Levenshtein)")
                FeatureItem("Mayúsculas con Shift de un toque y Bloqueo de Mayúsculas (doble toque)")
                FeatureItem("Borrado continuo manteniendo pulsado Backspace")
            }
        }
    }
}

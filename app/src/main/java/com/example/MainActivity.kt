package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.keyboard.KeyAction
import com.example.keyboard.KeyboardView
import com.example.keyboard.KeyboardUtils
import com.example.keyboard.nativebridge.NativeKeyboardBridge
import com.example.keyboard.voice.VoiceRecognitionEngine
import com.example.keyboard.voice.VoiceState
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KeyboardAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardAppScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isEnabled by remember { mutableStateOf(KeyboardUtils.isKeyboardEnabled(context)) }
    var isSelected by remember { mutableStateOf(KeyboardUtils.isKeyboardSelected(context)) }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }

    // Re-check status on resume when user comes back from system settings or IME picker
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isEnabled = KeyboardUtils.isKeyboardEnabled(context)
                isSelected = KeyboardUtils.isKeyboardSelected(context)
                hasMicPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var testInputText by remember { mutableStateOf("") }
    var interactivePreviewText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Activación y Prueba, 1: Vista previa interactiva

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Teclado Blanco",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = if (isSelected) "Activo como teclado actual" else if (isEnabled) "Habilitado en el sistema" else "Sin activar",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSelected) GreenSuccess else Color(0xFF64748B),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = BluePrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Configuración y Prueba", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Simulador de Teclado", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (selectedTab == 0) {
                SetupAndTestingContent(
                    isEnabled = isEnabled,
                    isSelected = isSelected,
                    hasMicPermission = hasMicPermission,
                    onRequestMicPermission = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    testText = testInputText,
                    onTextChange = { testInputText = it },
                    onEnableClick = { KeyboardUtils.openKeyboardSettings(context) },
                    onSelectClick = { KeyboardUtils.showInputMethodPicker(context) }
                )
            } else {
                SimulatorContent(
                    hasMicPermission = hasMicPermission,
                    onRequestMicPermission = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    previewText = interactivePreviewText,
                    onTextChange = { interactivePreviewText = it }
                )
            }
        }
    }
}

@Composable
private fun SetupAndTestingContent(
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
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
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

@Composable
private fun SimulatorContent(
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

@Composable
private fun StepCard(
    stepNumber: String,
    title: String,
    description: String,
    isCompleted: Boolean,
    actionButtonText: String,
    onActionClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isCompleted) Color(0xFFA7F3D0) else Color(0xFFE2E8F0)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) GreenSuccess else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
            }

            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                lineHeight = 18.sp
            )

            Button(
                onClick = onActionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) Color(0xFFECFDF5) else BluePrimary,
                    contentColor = if (isCompleted) Color(0xFF047857) else Color.White
                )
            ) {
                Text(
                    text = actionButtonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (!isCompleted) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickPhraseChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFEFF6FF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = BluePrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(BluePrimary)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF334155),
            lineHeight = 18.sp
        )
    }
}

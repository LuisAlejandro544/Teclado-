# AI Context: Teclado Blanco 🧠

Este archivo proporciona contexto técnico para asistentes de IA y desarrolladores sobre el diseño, arquitectura y convenciones de este proyecto.

---

## 🎯 Propósito del Proyecto
Crear un teclado para Android (`InputMethodService`) minimalista en color blanco, ligero, fluido y preparado para integrar lógica nativa en C++ y Rust para predicción de texto de alto rendimiento. El APK está destinado a distribución en tiendas alternativas como **Uptodown**.

---

## 🏗️ Puntos Clave de la Arquitectura

1. **Servicio IME**:
   - `com.example.keyboard.SimpleKeyboardService`: Extiende `InputMethodService` y provee una `ComposeView` como interfaz de entrada.
   - Maneja `onEvaluateInputViewShown() = true` y `onShowInputRequested() = true` para garantizar su visibilidad en emuladores y dispositivos físicos.

2. **Capa UI (Compose)**:
   - `KeyboardView`: Controla los 4 modos (`LETTERS`, `NUMBERS_SYMBOLS`, `MORE_SYMBOLS`, `EMOJIS`).
   - Soporta mayúsculas (`ShiftState.OFF`, `ON`, `CAPS_LOCK`).
   - Barra de sugerencias en tiempo real con 3 candidatos y autocorrección al presionar espacio.
   - Diacríticos y caracteres alternativos por pulsación prolongada (`LongPressPopupOverlay`).
   - Teclado de Emojis con detección nativa `PaintCompat.hasGlyph` para filtrar emojis no soportados por el firmware del usuario.
   - Tecla de borrado con repetición (`detectTapGestures` + corutina).
   - Insets ergonómicos: Margen inferior dedicado (`24.dp` o `WindowInsets.navigationBars`) para evitar colisiones con la barra de gestos de Android.

3. **Módulos Nativos y Motor de Predicción (C++ & Rust)**:
   - `app/src/main/cpp/`: C++ JNI bridge y motor de baja latencia con procesamiento de audio (cálculo de energía RMS y VAD - Voice Activity Detection).
   - `rust/`: Crate `keyboard-rust-core` con `lib.rs` para estructuras de datos Trie y predicción futura.
   - `NativeKeyboardBridge.kt`: Envoltorio seguro con motor Trie, algoritmo de Levenshtein para sugerencias, procesador de audio VAD y fallback automático si `.so` no está compilado.

4. **Gestión de Emojis**:
   - `EmojiManager.kt`: Categorización y verificación de compatibilidad de glifos en tiempo real para garantizar cero caracteres rotos (tofu). Historial en memoria de recientes.

5. **Motor de Dictado por Voz Autónomo y Privado**:
   - `VoiceRecognitionEngine.kt`: Motor de reconocimiento de voz configurado en modo preferentemente local (`EXTRA_PREFER_OFFLINE`), eliminando la dependencia de servidores en la nube.
   - Integración de VAD nativo C++ para detección de silencios acústicos y auto-confirmación continua de palabras habladas.
   - Componente UI `VoiceDictationStrip` con visualizador interactivo de onda de sonido (RMS), transcripción parcial en tiempo real y controles de aceptar/cancelar.
   - Gestión proactiva del permiso `RECORD_AUDIO` tanto en `MainActivity` como en el flujo del teclado.

6. **Distribución**:
   - Orientado a empaquetado directo APK para **Uptodown**.

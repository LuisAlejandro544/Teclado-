# Estructura del Proyecto 📁

Visión general del árbol de directorios y responsabilidades de cada módulo en **Teclado Blanco**.

```
├── .github/
│   └── workflows/
│       └── build-debug-apk.yml          # GitHub Action para compilación de APK Debug con caché y generación de llave en caliente
├── app/
│   ├── build.gradle.kts                 # Configuración del módulo de la app Android
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml      # Declaración de actividades, permisos y el servicio IME
│       │   ├── cpp/                     # [Módulo C++]
│       │   │   ├── CMakeLists.txt       # Configuración de compilación CMake para Android NDK
│       │   │   ├── keyboard_core.hpp    # Cabeceras del motor C++ (clases, interfaces y tipos)
│       │   │   └── keyboard_jni.cpp     # Enlaces JNI para comunicación con Kotlin
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt      # Pantalla principal con guía de activación, permisos y simulador
│       │   │   ├── keyboard/
│       │   │   │   ├── SimpleKeyboardService.kt  # Servicio central de método de entrada (InputMethodService)
│       │   │   │   ├── KeyboardLayout.kt         # Composables de UI del teclado (letras, símbolos, emojis, dictado por voz)
│       │   │   │   ├── KeyboardState.kt          # Enums y modelos de estado (Modos, Shift, KeyAction, Emojis, Voz)
│       │   │   │   ├── KeyboardUtils.kt          # Utilidades para comprobar y abrir ajustes de teclado
│       │   │   │   ├── SpecialCharactersMap.kt   # Mapeo de acentos y caracteres diacríticos por pulsación prolongada
│       │   │   │   ├── emoji/
│       │   │   │   │   └── EmojiManager.kt       # Gestor y detector de compatibilidad de emojis con PaintCompat
│       │   │   │   ├── voice/
│       │   │   │   │   └── VoiceRecognitionEngine.kt # Motor de dictado offline con VAD/RMS nativo C++
│       │   │   │   └── nativebridge/
│       │   │   │       └── NativeKeyboardBridge.kt # Puente seguro para motor Trie C++, VAD y Rust
│       │   │   └── ui/theme/
│       │   │       ├── Color.kt         # Paleta de colores minimalista en blanco y pizarra
│       │   │       ├── Theme.kt         # Configuración del tema Material 3
│       │   │       └── Type.kt          # Tipografía
│       │   └── res/
│       │       ├── layout/              # Layouts XML para inflado del servicio
│       │       ├── values/              # strings.xml, colors.xml
│       │       └── xml/method.xml       # Descriptor oficial del servicio IME para Android
├── rust/                                # [Módulo Rust]
│   ├── Cargo.toml                       # Manifiesto del crate de Rust para autocorrect y Trie
│   └── src/
│       └── lib.rs                       # Motor en Rust para predicción y búsqueda de palabras
├── README.md                            # Documentación principal del proyecto
├── ROADMAP.md                           # Hoja de ruta de funcionalidades futuras
├── STRUCTURE.md                         # Descripción de módulos y archivos
├── AI_CONTEXT.md                        # Contexto técnico para modelos de IA
└── AGENTS.md                            # Reglas e instrucciones para agentes de desarrollo
```

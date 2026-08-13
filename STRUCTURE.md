# Estructura del Proyecto 📁

Visión general del árbol de directorios y responsabilidades de cada módulo en **Teclado Blanco** tras la refactorización y desarrollo modular.

```
├── .github/
│   └── workflows/
│       ├── build-debug-apk.yml          # GitHub Action para compilación de APK Debug con caché y generación de llave en caliente
│       └── apply-zips.yml               # GitHub Action para auto-sincronización y actualización del repo desde archivos comprimidos
├── zips/                                # Carpeta monitoreada para subida de archivos comprimidos (.zip, .7z, etc.)
│   ├── .gitkeep
│   └── README.md
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
│       │   │   ├── MainActivity.kt      # Punto de entrada de la actividad principal (< 30 líneas)
│       │   │   ├── keyboard/
│       │   │   │   ├── SimpleKeyboardService.kt  # Servicio central de método de entrada (InputMethodService)
│       │   │   │   ├── KeyboardLayout.kt         # Orquestador composable principal del teclado
│       │   │   │   ├── KeyboardState.kt          # Enums y modelos de estado (Modos, Shift, KeyAction, SpecialCharactersMap)
│       │   │   │   ├── KeyboardUtils.kt          # Utilidades para comprobar y abrir ajustes de teclado
│       │   │   │   ├── dictionary/
│       │   │   │   │   └── SpanishVocabularyCorpus.kt # Corpus léxico y frecuencias para predicción offline
│       │   │   │   ├── fallback/
│       │   │   │   │   └── FallbackSpanishTrie.kt     # Motor Trie en Kotlin con normalización de acentos
│       │   │   │   ├── emoji/
│       │   │   │   │   └── EmojiManager.kt            # Gestor y detector de compatibilidad de emojis con PaintCompat
│       │   │   │   ├── voice/
│       │   │   │   │   └── VoiceRecognitionEngine.kt  # Motor de dictado offline con VAD/RMS nativo C++
│       │   │   │   ├── nativebridge/
│       │   │   │   │   └── NativeKeyboardBridge.kt    # Puente seguro JNI para motor Trie C++, VAD y Rust
│       │   │   │   └── ui/
│       │   │   │       ├── components/
│       │   │   │       │   ├── KeyButtons.kt          # Teclas estándar, especiales y tecla de borrado repetitivo
│       │   │   │       │   ├── SuggestionStrip.kt     # Franja de sugerencias predictivas y botón de micrófono
│       │   │   │       │   ├── VoiceDictationStrip.kt # Franja animada con ecualizador de voz y controles
│       │   │   │       │   └── LongPressPopupOverlay.kt # Menú emergente de acentos y diacríticos
│       │   │   │       └── layouts/
│       │   │   │           ├── LettersKeyboardLayout.kt # Capa de letras QWERTY en español
│       │   │   │           ├── SymbolsKeyboardLayout.kt # Capas numéricas y de símbolos (?123 y =\<)
│       │   │   │           └── EmojiKeyboardLayout.kt   # Grilla y categorías de emojis compatibles
│       │   │   └── ui/
│       │   │       ├── main/
│       │   │       │   ├── MainScreen.kt              # Pantalla principal con Scaffold, AppBar y navegación por pestañas
│       │   │       │   ├── components/
│       │   │       │   │   └── SetupStepCard.kt       # Componentes de pasos de activación y chips
│       │   │       │   └── tabs/
│       │   │       │       ├── SetupTab.kt            # Contenido de la pestaña de configuración, permisos y prueba
│       │   │       │       └── SimulatorTab.kt        # Contenido de la pestaña del simulador interactivo
│       │   │       └── theme/
│       │   │           ├── Color.kt                   # Paleta de colores minimalista en blanco y pizarra
│       │   │           ├── Theme.kt                   # Configuración del tema Material 3
│       │   │           └── Type.kt                    # Tipografía
│       │   └── res/
│       │       ├── layout/              # Layouts XML para inflado del servicio
│       │       ├── values/              # strings.xml, colors.xml
│       │       └── xml/method.xml       # Descriptor oficial del servicio IME para Android
│       └── test/
│           └── java/com/example/
│               ├── ExampleUnitTest.kt       # Tests unitarios del Trie, acentos y sugerencias
│               ├── ExampleRobolectricTest.kt # Test de lectura de recursos con Robolectric
│               └── GreetingScreenshotTest.kt # Test de captura de pantalla con Roborazzi
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

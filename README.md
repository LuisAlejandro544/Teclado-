# Teclado Blanco (White Keyboard) ⌨️

Aplicación de teclado virtual minimalista en blanco para Android, diseñada para ofrecer una experiencia de escritura cómoda, elegante y con soporte ergonómico para gestos del sistema y botones tradicionales.

---

## 🚀 Características Principales

- **Diseño Blanco Minimalista**: Teclas claras de alto contraste, animaciones táctiles suaves y bordes nítidos.
- **Distribución Completa en Español (QWERTY + Ñ)**: Acceso directo a la letra `Ñ`, acentos y signos de apertura (`¿`, `¡`).
- **Dictado por Voz Autónomo 100% Local y Privado**:
  - Reconocimiento de voz local sin envío de grabaciones ni datos a servidores externos.
  - Procesamiento nativo en C++ con cálculo de energía RMS y detector de actividad de voz (*VAD - Voice Activity Detection*) en tiempo real.
  - Franja interactiva de dictado con animación de onda de sonido, texto parcial en vivo y detección automática de silencios para auto-confirmar el texto.
  - Botón de micrófono accesible directamente desde la barra de sugerencias o la barra de acciones.
- **Acentos y Caracteres Especiales por Pulsación Prolongada**: Menú emergente flotante con variantes (`á`, `é`, `í`, `ó`, `ú`, `ü`, `ñ`, `ç`, `€`, `¿`, etc.) y números en fila superior.
- **Barra de Sugerencias y Autocorrector**: Franja superior interactiva con 3 candidatos en tiempo real y corrección automática inteligente al pulsar espacio.
- **Teclado de Emojis con Detección de Compatibilidad**:
  - Detección automática en tiempo real de qué emojis soporta la versión de Android del dispositivo mediante `PaintCompat.hasGlyph` para evitar caracteres rotos (*tofu*).
  - Categorías organizadas: Caras, Animales, Comida, Actividades, Lugares y Objetos/Símbolos.
  - Historial de emojis recientes persistente en memoria.
- **Navegación Ergonómica**: Margen inferior optimizado para evitar toques accidentales con la barra de gestos de Android y barras de 3 botones.
- **Capas de Símbolos y Emojis**:
  - `?123`: Fila numérica y símbolos de uso frecuente.
  - `=\<`: Símbolos matemáticos, monetarios y especiales.
  - `😀`: Teclado de emojis con indicador de compatibilidad y pestañas de categorías.
- **Shift y Bloqueo de Mayúsculas**: Un toque para la siguiente letra, doble toque para *Caps Lock*.
- **Borrado Rápido Continuo**: Soporte para borrar caracteres individuales o mantener pulsado para borrado acelerado.
- **Arquitectura Híbrida con Motor C++ & Rust**: Motor predictivo con árbol Trie, cálculo de distancia de Levenshtein y procesamiento de audio/VAD de ultrabaja latencia.
- **Distribución en Uptodown**: Diseñado para despliegue directo en APK compatible con Uptodown App Store.

---

## 🛠️ Stack Tecnológico

- **Lenguaje**: Kotlin 2.x
- **Framework UI**: Jetpack Compose con Material Design 3
- **Sistema Base**: Android `InputMethodService` (API 24+)
- **Módulos Nativos**:
  - C++ (`app/src/main/cpp`): Motor de procesamiento y lógica de baja latencia.
  - Rust (`rust/`): Motor para predicción y diccionarios Trie de alto rendimiento.
- **Pruebas**: Robolectric & Roborazzi para pruebas locales y capturas visuales.

---

## 🤖 Integración Continua (GitHub Actions)

El repositorio incluye un flujo de trabajo automatizado en `.github/workflows/build-debug-apk.yml` que:
- Descarga el código completo del repositorio.
- Configura JDK 17 y la caché de Gradle de alta velocidad (`gradle/actions/setup-gradle`).
- Genera en caliente la clave de firma `debug.keystore` con `keytool` si no está presente.
- Compila el proyecto generando el APK (`./gradlew assembleDebug`).
- Sube el APK compilado como artefacto listo para descargar y probar.

---

## 📦 Instalación y Configuración

1. Compilar y generar el APK mediante Gradle o exportar desde la plataforma.
2. Abrir la app **Teclado Blanco** en el dispositivo.
3. Seguir los 2 pasos en pantalla:
   - **Paso 1**: Habilitar *Teclado Blanco* en *Ajustes de Idioma y Entrada*.
   - **Paso 2**: Seleccionarlo como el teclado activo/predeterminado.
4. Probar en el área de prueba integrada o en cualquier aplicación del sistema.

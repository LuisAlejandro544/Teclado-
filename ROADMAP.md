# Roadmap del Proyecto: Teclado Blanco 🗺️

Este documento detalla las fases de desarrollo planificadas para evolucionar el teclado hacia un motor de texto predictivo completo.

---

## 📍 Fase 1: Base Funcional y Ergonomía (Completada ✅)
- [x] Implementación de `InputMethodService` y enlace de ciclo de vida con Jetpack Compose.
- [x] Distribución de letras completa en español (QWERTY con tecla `Ñ`).
- [x] Páginas de símbolos `?123` y `=\<`.
- [x] Soporte ergonómico para navegación por gestos y barra de 3 botones.
- [x] Shift simple y bloqueo permanente de mayúsculas (Caps Lock).
- [x] Borrado rápido con repetición al mantener presionado.
- [x] App de bienvenida, activación guiada y simulador interactivo.

---

## 📍 Fase 2: Integración de Núcleos Nativos C++ & Rust (Completada ✅)
- [x] Estructuración del módulo C++ (`app/src/main/cpp`) con headers de motor.
- [x] Estructuración del crate Rust (`rust/`) con tipos base y exports.
- [x] Creación de `NativeKeyboardBridge` en Kotlin con fallback seguro y motor Trie en memoria.
- [x] Implementación de estructura Trie y distancia de Levenshtein para predicción y corrección ortográfica.
- [x] Barra de sugerencias en tiempo real con 3 candidatos interactivos.
- [x] Autocorrección predictiva inteligente al pulsar la tecla espacio.

---

## 📍 Fase 3: Funcionalidades Avanzadas y Emojis (Completada ✅)
- [x] Acentos y caracteres especiales por pulsación prolongada con popup flotante (á, é, í, ó, ú, ü, ñ, ç, números rápidos).
- [x] Teclado de Emojis completo organizado por categorías (Caras, Animales, Comida, Deportes, Viajes, Objetos).
- [x] Detección automática en tiempo real de soporte de emojis del dispositivo con `PaintCompat.hasGlyph`.
- [x] Historial dinámico de Emojis recientes.
- [x] Botón de acceso directo a emojis desde la fila inferior del teclado.

---

## 📍 Fase 4: Dictado por Voz Autónomo y Privacidad (Completada ✅)
- [x] Motor de reconocimiento de voz local/offline sin conexión a internet.
- [x] Procesamiento de audio en C++ nativo para cálculo de RMS y Voice Activity Detection (VAD).
- [x] Interfaz de usuario con visualizador de onda acústica en tiempo real y transcripción parcial.
- [x] Inserción de texto continua e inteligente directamente en `InputConnection`.
- [x] Solicitud y validación transparente del permiso `RECORD_AUDIO` en pantalla de inicio.

---

## 📍 Fase 5: Próximas Mejoras y Publicación en Uptodown (En Progreso 🔄)
- [ ] Deslizamiento para escribir (*Gesture / Swipe Typing*) procesado en C++/Rust.
- [ ] Portapapeles integrado en la barra superior.
- [ ] Temas personalizables (Blanco Puro, Blanco Hielo, Modo Oscuro OLED, Sepia Cálido).
- [ ] Generación y firma de APK optimizado para tiendas alternativas (Uptodown).
- [ ] Ficha técnica, capturas de pantalla y documentación para usuarios finales.

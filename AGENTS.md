# Guía de Agentes y Reglas de Desarrollo (AGENTS.md) 🤖

Reglas e instrucciones de trabajo para cualquier agente de IA o desarrollador que modifique este proyecto:

---

## 1. Reglas Generales
- **Distribución de la App**: La aplicación se publicará en **Uptodown** (formato APK directo), no en Google Play Store.
- **Sin Dependencias Innecesarias**: Mantener la base de código ligera y optimizada para arranque instantáneo.
- **Compatibilidad**: Mantener compatibilidad con Android 7.0+ (API 24 o superior).

---

## 2. Pautas de Código
- **Kotlin & Compose**:
  - Toda la interfaz del teclado debe mantener estilos definidos en `com.example.ui.theme`.
  - Asegurar siempre que los tamaños táctiles de las teclas cumplan con los estándares ergonómicos (`>= 54.dp` de altura).
- **Servicio IME**:
  - No romper el ciclo de vida de `SimpleKeyboardService`. Cualquier vista Compose dentro del IME debe usar `ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool`.
- **Módulos C++ y Rust**:
  - Todo enlace JNI nuevo debe contar con su correspondiente método en `NativeKeyboardBridge.kt` protegido contra `UnsatisfiedLinkError`.
- **Verificación**:
  - Compilar siempre con `compile_applet` antes de finalizar cualquier turno de desarrollo.

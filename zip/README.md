# Carpeta de Paquetes Comprimidos (zip/) 📦

Esta carpeta está monitoreada por el GitHub Action **`.github/workflows/apply-zips.yml`**.

### ¿Cómo funciona?
1. Sube cualquier archivo comprimido (`.zip`, `.7z`, `.tar.gz`, `.tar.bz2`, `.rar`, etc.) dentro de esta carpeta `zip/` (o `zips/`) en tu repositorio.
2. El flujo de trabajo se activará **únicamente** cuando se detecte un archivo con extensión comprimida en esta ruta.
3. Descomprimirá los contenidos, agregará archivos nuevos, modificará y actualizará los existentes en la raíz del repositorio.
4. El archivo comprimido será eliminado tras la sincronización para evitar bucles.
5. Los cambios se confirmarán y guardarán automáticamente en tu rama de GitHub.

### Permisos para actualizar GitHub Actions y Workflows:
Si tu archivo comprimido incluye cambios a flujos de trabajo en `.github/workflows/`, asegúrate de agregar un **Personal Access Token (PAT)** con permisos de `repo` y `workflow` en los **Secrets de GitHub** con alguno de estos nombres:
- `GH_PAT`
- `AUTO_SYNC_TOKEN`
- `PAT_TOKEN`

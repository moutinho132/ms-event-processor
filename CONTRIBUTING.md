# Contributing Guide

## Flujo de Trabajo con Git

Este proyecto sigue un flujo de trabajo basado en **GitFlow** con ramas protegidas.

### Ramas Principales

| Rama | Descripción | Protección |
|------|-------------|------------|
| `main` | Código en producción | ⛔ Push directo bloqueado |
| `develop` | Código en desarrollo | ⛔ Push directo bloqueado |

### Ramas de Soporte

| Tipo | Naming | Ejemplo |
|------|--------|---------|
| Feature | `feature/*` | `feature/add-login` |
| Bugfix | `bugfix/*` | `bugfix/fix-auth` |
| Hotfix | `hotfix/*` | `hotfix/urgent-fix` |
| Release | `release/*` | `release/v1.0.0` |

---

## Reglas de Protección de Ramas

### Rama `develop`

```yaml
Protecciones:
  - ❌ No permite push directo
  - ✅ Requiere Pull Request
  - ✅ Requiere 1 aprobación
  - ✅ Requiere CI passing
  - ✅ Historial lineal (no merges)
  - ⚠️ Admins pueden saltar protección
```

### Rama `main`

```yaml
Protecciones:
  - ❌ No permite push directo
  - ✅ Requiere Pull Request
  - ✅ Requiere 2 aprobaciones
  - ✅ Requiere CI passing
  - ✅ Historial lineal (no merges)
  - ❌ Admins NO pueden saltar protección
```

---

## Cómo Contribuir

### 1. Crear una rama desde `develop`

```bash
# Asegúrate de estar en develop
git checkout develop
git pull origin develop

# Crear tu rama
git checkout -b feature/mi-nueva-funcionalidad
```

### 2. Hacer cambios y commits

```bash
# Hacer cambios...
git add .
git commit -m "feat: agrega nueva funcionalidad"
```

### 3. Push a tu rama

```bash
git push origin feature/mi-nueva-funcionalidad
```

### 4. Crear Pull Request

```bash
# Usando GitHub CLI
gh pr create --base develop --head feature/mi-nueva-funcionalidad --title "Mi funcionalidad" --body "Descripción..."
```

O ve a GitHub y crea el PR desde la UI.

### 5. Esperar aprobación

- Los revisores deben aprobar tu PR
- El CI debe pasar (tests, lint, build)
- Luego puedes hacer merge

---

## Convenciones de Commits

Usamos [Conventional Commits](https://www.conventionalcommits.org/):

| Tipo | Descripción |
|------|-------------|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de bug |
| `docs` | Documentación |
| `style` | Formato (no afecta código) |
| `refactor` | Refactorización |
| `test` | Agregar/modificar tests |
| `chore` | Tareas de mantenimiento |

**Ejemplos:**

```bash
git commit -m "feat: agrega login con JWT"
git commit -m "fix: corrige validación de email"
git commit -m "docs: actualiza README"
git commit -m "refactor: simplifica lógica de orders"
```

---

## Configurar Branch Protection

Si tienes permisos de admin en el repositorio:

```bash
# Instalar GitHub CLI
brew install gh

# Autenticarse
gh auth login

# Ejecutar script de protección
./scripts/protect-branches.sh
```

---

## Preguntas Frecuentes

### ¿Por qué no puedo hacer push a `develop`?

Las ramas `main` y `develop` están protegidas. Debes:
1. Crear una rama desde `develop`
2. Hacer tus cambios
3. Crear un Pull Request
4. Esperar aprobación

### ¿Cómo saltar la protección siendo admin?

En `develop` los admins pueden saltar la protección usando "Merge without waiting for requirements" en GitHub. En `main` NO es posible.

### ¿Qué pasa si el CI falla?

No podrás hacer merge hasta que el CI pase. Revisa los errores en la pestaña "Checks" del PR.

---

## Contacto

Para dudas sobre el proceso, contacta a los maintainers del proyecto.

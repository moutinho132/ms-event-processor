#!/bin/bash

# ============================================
# Script para proteger ramas en GitHub
# Requiere: gh CLI (GitHub CLI)
# ============================================

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Configurando Branch Protection Rules ===${NC}"
echo ""

# Verificar si gh CLI está instalado
if ! command -v gh &> /dev/null; then
    echo -e "${RED}Error: GitHub CLI (gh) no está instalado${NC}"
    echo ""
    echo "Instálalo con:"
    echo "  brew install gh"
    echo ""
    echo "Luego autentícate con:"
    echo "  gh auth login"
    exit 1
fi

# Verificar autenticación
if ! gh auth status &> /dev/null; then
    echo -e "${RED}Error: No estás autenticado en GitHub CLI${NC}"
    echo ""
    echo "Ejecuta: gh auth login"
    exit 1
fi

# Obtener el repositorio actual
REPO=$(git remote get-url origin | sed 's/https:\/\/github.com\///' | sed 's/.git$//')

echo -e "Repositorio: ${YELLOW}$REPO${NC}"
echo ""

# ============================================
# Proteger rama develop
# ============================================
echo -e "${GREEN}Protegiendo rama 'develop'...${NC}"

gh api -X PUT repos/$REPO/branches/develop/protection \
  --input - << 'EOF'
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["ci"]
  },
  "enforce_admins": false,
  "required_pull_request_reviews": {
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": false,
    "required_approving_review_count": 1
  },
  "restrictions": null,
  "required_linear_history": true,
  "allow_force_pushes": false,
  "allow_deletions": false
}
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Rama 'develop' protegida exitosamente${NC}"
else
    echo -e "${YELLOW}! La rama 'develop' puede que ya esté protegida o no existe${NC}"
fi

echo ""

# ============================================
# Proteger rama main
# ============================================
echo -e "${GREEN}Protegiendo rama 'main'...${NC}"

gh api -X PUT repos/$REPO/branches/main/protection \
  --input - << 'EOF'
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["ci"]
  },
  "enforce_admins": true,
  "required_pull_request_reviews": {
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": true,
    "required_approving_review_count": 2
  },
  "restrictions": null,
  "required_linear_history": true,
  "allow_force_pushes": false,
  "allow_deletions": false
}
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Rama 'main' protegida exitosamente${NC}"
else
    echo -e "${YELLOW}! La rama 'main' puede que ya esté protegida o no existe${NC}"
fi

echo ""
echo -e "${GREEN}=== Resumen de Protecciones ===${NC}"
echo ""
echo "📋 Rama DEVELOP:"
echo "   - Requiere 1 aprobación de PR"
echo "   - No permite push directo"
echo "   - Requiere CI passing"
echo "   - Historial lineal"
echo "   - Admins pueden saltar protección"
echo ""
echo "📋 Rama MAIN:"
echo "   - Requiere 2 aprobaciones de PR"
echo "   - No permite push directo"
echo "   - Requiere CI passing"
echo "   - Historial lineal"
echo "   - Admins NO pueden saltar protección"
echo ""
echo -e "${GREEN}=== Configuración completada ===${NC}"

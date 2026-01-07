#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}Red Hat Healthcare - Stopping All Services${NC}"
echo -e "${YELLOW}========================================${NC}"

echo -e "\n${YELLOW}Stopping and removing containers...${NC}"

# Stop and remove application containers
containers=(
  "healthcare-portal"
  "healthcare-claims-api"
  "healthcare-coverage-api"
  "healthcare-patients-api"
  "healthcare-claims-postgres"
  "healthcare-coverage-postgres"
  "healthcare-patients-postgres"
)

for container in "${containers[@]}"; do
  if podman ps -a --format "{{.Names}}" | grep -q "^${container}$"; then
    echo -e "  Stopping ${container}..."
    podman stop "${container}" 2>/dev/null || true
    echo -e "  Removing ${container}..."
    podman rm "${container}" 2>/dev/null || true
    echo -e "${GREEN}✓ ${container} stopped and removed${NC}"
  else
    echo -e "  ${container} not found (already stopped)"
  fi
done

echo -e "\n${YELLOW}Do you want to remove the podman network? (y/N)${NC}"
read -r response
if [[ "$response" =~ ^[Yy]$ ]]; then
  if podman network exists healthcare-network; then
    podman network rm healthcare-network
    echo -e "${GREEN}✓ Network removed${NC}"
  fi
fi

echo -e "\n${YELLOW}Do you want to remove persistent data volumes? (y/N)${NC}"
echo -e "${RED}WARNING: This will delete all database data!${NC}"
read -r response
if [[ "$response" =~ ^[Yy]$ ]]; then
  podman volume rm healthcare-patients-data healthcare-coverage-data healthcare-claims-data 2>/dev/null || true
  echo -e "${GREEN}✓ Volumes removed${NC}"
else
  echo -e "${YELLOW}Data volumes preserved${NC}"
fi

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}All services stopped${NC}"
echo -e "${GREEN}========================================${NC}"

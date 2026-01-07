#!/bin/bash

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Wiping All Healthcare Databases${NC}"
echo -e "${GREEN}========================================${NC}"

# Database connection details
PATIENTS_DB="healthcare_patients"
COVERAGE_DB="healthcare_coverage"
CLAIMS_DB="healthcare_claims"
DB_USER="healthcare"
DB_PASS="healthcare123"

# Container names
PATIENTS_CONTAINER="healthcare-patients-postgres"
COVERAGE_CONTAINER="healthcare-coverage-postgres"
CLAIMS_CONTAINER="healthcare-claims-postgres"

# Function to wipe a database
wipe_database() {
  local container=$1
  local database=$2

  echo -e "\n${YELLOW}Wiping database: ${database}...${NC}"

  podman exec $container psql -U $DB_USER -d $database -c "
    DO \$\$
    DECLARE
      r RECORD;
    BEGIN
      FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
        EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' CASCADE';
      END LOOP;
    END \$\$;
  "

  echo -e "${GREEN}✓ Database ${database} wiped${NC}"
}

# Wipe all databases
wipe_database $PATIENTS_CONTAINER $PATIENTS_DB
wipe_database $COVERAGE_CONTAINER $COVERAGE_DB
wipe_database $CLAIMS_CONTAINER $CLAIMS_DB

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}All Databases Wiped Successfully!${NC}"
echo -e "${GREEN}========================================${NC}"

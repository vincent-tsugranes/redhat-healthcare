#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Red Hat Healthcare - Starting All Services${NC}"
echo -e "${GREEN}========================================${NC}"

# Create podman network if it doesn't exist
echo -e "\n${YELLOW}Creating podman network...${NC}"
podman network exists healthcare-network || podman network create healthcare-network
echo -e "${GREEN}✓ Network created/exists${NC}"

# Start PostgreSQL containers
echo -e "\n${YELLOW}Starting PostgreSQL databases...${NC}"

# Patients DB
echo -e "  Starting patients database..."
podman run -d \
  --name healthcare-patients-postgres \
  --network healthcare-network \
  -e POSTGRES_DB=healthcare_patients \
  -e POSTGRES_USER=healthcare \
  -e POSTGRES_PASSWORD=healthcare \
  -p 5432:5432 \
  -v healthcare-patients-data:/var/lib/postgresql/data \
  --health-cmd "pg_isready -U healthcare" \
  --health-interval 10s \
  --health-timeout 5s \
  --health-retries 5 \
  postgres:16-alpine || echo "  (Patients DB already running)"

# Coverage DB
echo -e "  Starting coverage database..."
podman run -d \
  --name healthcare-coverage-postgres \
  --network healthcare-network \
  -e POSTGRES_DB=healthcare_coverage \
  -e POSTGRES_USER=healthcare \
  -e POSTGRES_PASSWORD=healthcare \
  -p 5433:5432 \
  -v healthcare-coverage-data:/var/lib/postgresql/data \
  --health-cmd "pg_isready -U healthcare" \
  --health-interval 10s \
  --health-timeout 5s \
  --health-retries 5 \
  postgres:16-alpine || echo "  (Coverage DB already running)"

# Claims DB
echo -e "  Starting claims database..."
podman run -d \
  --name healthcare-claims-postgres \
  --network healthcare-network \
  -e POSTGRES_DB=healthcare_claims \
  -e POSTGRES_USER=healthcare \
  -e POSTGRES_PASSWORD=healthcare \
  -p 5434:5432 \
  -v healthcare-claims-data:/var/lib/postgresql/data \
  --health-cmd "pg_isready -U healthcare" \
  --health-interval 10s \
  --health-timeout 5s \
  --health-retries 5 \
  postgres:16-alpine || echo "  (Claims DB already running)"

echo -e "${GREEN}✓ Databases started${NC}"

# Wait for databases to be healthy
echo -e "\n${YELLOW}Waiting for databases to be ready...${NC}"
sleep 10
echo -e "${GREEN}✓ Databases ready${NC}"

# Build and start Quarkus applications
echo -e "\n${YELLOW}Building Quarkus applications...${NC}"

# Patients Service
echo -e "\n  Building Patients Service..."
cd /Users/vtsugran/Code/redhat-healthcare/redhat-healthcare-patients
./mvnw clean package -DskipTests
echo -e "${GREEN}✓ Patients service built${NC}"

echo -e "  Building Patients container..."
podman build -f src/main/docker/Dockerfile.jvm -t healthcare-patients:latest .
echo -e "${GREEN}✓ Patients container built${NC}"

echo -e "  Starting Patients service..."
podman run -d \
  --name healthcare-patients-api \
  --network healthcare-network \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://healthcare-patients-postgres:5432/healthcare_patients \
  -e QUARKUS_DATASOURCE_USERNAME=healthcare \
  -e QUARKUS_DATASOURCE_PASSWORD=healthcare \
  -p 8080:8080 \
  healthcare-patients:latest || echo "  (Patients service already running)"
echo -e "${GREEN}✓ Patients service started${NC}"

# Coverage Service
echo -e "\n  Building Coverage Service..."
cd /Users/vtsugran/Code/redhat-healthcare/redhat-healthcare-patients-coverage
./mvnw clean package -DskipTests
echo -e "${GREEN}✓ Coverage service built${NC}"

echo -e "  Building Coverage container..."
podman build -f src/main/docker/Dockerfile.jvm -t healthcare-coverage:latest .
echo -e "${GREEN}✓ Coverage container built${NC}"

echo -e "  Starting Coverage service..."
podman run -d \
  --name healthcare-coverage-api \
  --network healthcare-network \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://healthcare-coverage-postgres:5432/healthcare_coverage \
  -e QUARKUS_DATASOURCE_USERNAME=healthcare \
  -e QUARKUS_DATASOURCE_PASSWORD=healthcare \
  -p 8081:8080 \
  healthcare-coverage:latest || echo "  (Coverage service already running)"
echo -e "${GREEN}✓ Coverage service started${NC}"

# Claims Service
echo -e "\n  Building Claims Service..."
cd /Users/vtsugran/Code/redhat-healthcare/redhat-healthcare-claims
./mvnw clean package -DskipTests
echo -e "${GREEN}✓ Claims service built${NC}"

echo -e "  Building Claims container..."
podman build -f src/main/docker/Dockerfile.jvm -t healthcare-claims:latest .
echo -e "${GREEN}✓ Claims container built${NC}"

echo -e "  Starting Claims service..."
podman run -d \
  --name healthcare-claims-api \
  --network healthcare-network \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://healthcare-claims-postgres:5432/healthcare_claims \
  -e QUARKUS_DATASOURCE_USERNAME=healthcare \
  -e QUARKUS_DATASOURCE_PASSWORD=healthcare \
  -p 8082:8080 \
  healthcare-claims:latest || echo "  (Claims service already running)"
echo -e "${GREEN}✓ Claims service started${NC}"

# Build and start Patient Portal
echo -e "\n${YELLOW}Building Patient Portal...${NC}"
cd /Users/vtsugran/Code/redhat-healthcare/redhat-healthcare-patient-portal

# Create Dockerfile for portal if it doesn't exist
if [ ! -f "Dockerfile" ]; then
  cat > Dockerfile << 'EOF'
FROM node:22-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
EOF
fi

# Create nginx config if it doesn't exist
if [ ! -f "nginx.conf" ]; then
  cat > nginx.conf << 'EOF'
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # API proxy to backend services (for CORS)
    location /api/patients/ {
        proxy_pass http://host.containers.internal:8080/fhir/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /api/coverage/ {
        proxy_pass http://host.containers.internal:8081/fhir/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /api/claims/ {
        proxy_pass http://host.containers.internal:8082/fhir/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF
fi

echo -e "  Building Portal container..."
podman build -t healthcare-portal:latest .
echo -e "${GREEN}✓ Portal container built${NC}"

echo -e "  Starting Portal..."
podman run -d \
  --name healthcare-portal \
  --network healthcare-network \
  -p 8888:80 \
  healthcare-portal:latest || echo "  (Portal already running)"
echo -e "${GREEN}✓ Portal started${NC}"

# Display status
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}All services started successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n${YELLOW}Service URLs:${NC}"
echo -e "  Patients API:     ${GREEN}http://localhost:8080/fhir${NC}"
echo -e "  Coverage API:     ${GREEN}http://localhost:8081/fhir${NC}"
echo -e "  Claims API:       ${GREEN}http://localhost:8082/fhir${NC}"
echo -e "  Patient Portal:   ${GREEN}http://localhost:8888${NC}"
echo -e "\n${YELLOW}API Documentation:${NC}"
echo -e "  Patients Swagger: ${GREEN}http://localhost:8080/q/swagger-ui${NC}"
echo -e "  Coverage Swagger: ${GREEN}http://localhost:8081/q/swagger-ui${NC}"
echo -e "  Claims Swagger:   ${GREEN}http://localhost:8082/q/swagger-ui${NC}"
echo -e "\n${YELLOW}View logs:${NC}"
echo -e "  podman logs -f healthcare-patients-api"
echo -e "  podman logs -f healthcare-coverage-api"
echo -e "  podman logs -f healthcare-claims-api"
echo -e "  podman logs -f healthcare-portal"
echo -e "\n${YELLOW}Stop all services:${NC}"
echo -e "  ./stop-all-services.sh"
echo -e ""

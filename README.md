# Red Hat Healthcare Platform

A complete healthcare platform built with FHIR R4 standards, consisting of microservices for patient management, insurance coverage, and claims processing, with a modern patient-facing web portal.

## Architecture

The platform consists of four main components:

### Backend Services (Quarkus + PostgreSQL)

1. **Patient Service** (Port 8080)
   - Manages patient demographic data using FHIR Patient resources
   - PostgreSQL database on port 5432
   - Location: `redhat-healthcare-patients/`

2. **Coverage Service** (Port 8081)
   - Manages insurance coverage information using FHIR Coverage resources
   - PostgreSQL database on port 5433
   - Location: `redhat-healthcare-patients-coverage/`

3. **Claims Service** (Port 8082)
   - Manages medical claims using FHIR Claim resources
   - PostgreSQL database on port 5434
   - Location: `redhat-healthcare-claims/`

### Frontend Application (Vue.js)

4. **Patient Portal** (Port 8888 when containerized, 5173 for dev)
   - Patient-facing web application for viewing personal health information
   - Built with Vue 3, TypeScript, Pinia, and Vue Router
   - Location: `redhat-healthcare-patient-portal/`

## Quick Start with Podman

### Prerequisites

- Podman installed and running
- At least 4GB of available RAM
- Ports 8080-8082, 5432-5434, and 8888 available

### Start All Services

Run the automated startup script:

```bash
./start-all-services.sh
```

This script will:
1. Create a podman network for service communication
2. Start 3 PostgreSQL database containers
3. Build and start 3 Quarkus microservices
4. Build and start the patient portal web application

The process takes approximately 5-10 minutes on the first run (builds are cached for subsequent runs).

### Access the Platform

Once started, access the services at:

| Service | URL |
|---------|-----|
| Patient Portal | http://localhost:8888 |
| Patients API | http://localhost:8080/fhir |
| Coverage API | http://localhost:8081/fhir |
| Claims API | http://localhost:8082/fhir |
| Patients Swagger | http://localhost:8080/q/swagger-ui |
| Coverage Swagger | http://localhost:8081/q/swagger-ui |
| Claims Swagger | http://localhost:8082/q/swagger-ui |

### Stop All Services

To stop and remove all containers:

```bash
./stop-all-services.sh
```

You'll be prompted to:
- Remove the podman network (optional)
- Remove persistent data volumes (optional - **WARNING: This deletes all data!**)

### View Logs

Monitor service logs using:

```bash
# View all logs
podman logs -f healthcare-patients-api
podman logs -f healthcare-coverage-api
podman logs -f healthcare-claims-api
podman logs -f healthcare-portal

# View database logs
podman logs -f healthcare-patients-postgres
podman logs -f healthcare-coverage-postgres
podman logs -f healthcare-claims-postgres
```

### Check Container Status

```bash
podman ps -a --filter network=healthcare-network
```

### Create Test Data

To populate the system with test data for development and testing:

```bash
./create-test-data.sh
```

This script creates:
- **Patient**: Vincent Tsugranes with complete demographic information
  - Name, contact info, address, identifiers
  - Date of birth: June 15, 1985
  - Gender: Male, Marital Status: Married
- **Coverage**: Blue Cross Blue Shield Gold Plan
  - Active coverage for 2024
  - $25 copay, 20% coinsurance
  - Preferred Provider Network
- **Claims**: 20 synthetic medical claims spanning recent months
  - Various diagnoses (respiratory infections, hypertension, diabetes, etc.)
  - Different procedures (office visits, lab tests, X-rays, etc.)
  - Claim amounts ranging from $150-$625
  - Realistic FHIR R4 structure with ICD-10 codes and CPT codes

**Note**: Services must be running before executing this script. Start them with `./start-all-services.sh` first.

## Development Setup

### Running Services Individually

Each service can be run individually for development:

#### Backend Services

```bash
# Start database first
cd redhat-healthcare-patients  # or coverage/claims
docker-compose up -d  # or podman-compose

# Run in dev mode
./mvnw quarkus:dev
```

#### Patient Portal

```bash
cd redhat-healthcare-patient-portal
npm install
npm run dev
```

Access at http://localhost:5173

### Technology Stack

**Backend:**
- Quarkus 3.30.5/3.30.6
- HAPI FHIR 7.0.2 (R4)
- PostgreSQL 16
- Hibernate ORM with Panache
- Flyway migrations
- RESTEasy Reactive
- SmallRye OpenAPI

**Frontend:**
- Vue 3 (Composition API)
- TypeScript
- Pinia (state management)
- Vue Router
- Axios
- Vite

**Containers:**
- Podman/Docker
- Multi-stage builds
- Alpine-based images

## FHIR Compliance

All services implement FHIR R4 standard REST operations:

- `POST /fhir/{Resource}` - Create resource
- `GET /fhir/{Resource}/{id}` - Read resource
- `PUT /fhir/{Resource}/{id}` - Update resource
- `DELETE /fhir/{Resource}/{id}` - Delete resource
- `GET /fhir/{Resource}?params` - Search resources (returns Bundle)

Error responses use FHIR OperationOutcome format.

## Data Model

Each service uses a hybrid storage approach:
- Complete FHIR resources stored as JSONB in PostgreSQL
- Key search fields extracted to indexed columns for performance
- GIN indexes on JSONB for advanced queries

## Project Structure

```
redhat-healthcare/
├── redhat-healthcare-patients/          # Patient microservice
├── redhat-healthcare-patients-coverage/ # Coverage microservice
├── redhat-healthcare-claims/            # Claims microservice
├── redhat-healthcare-patient-portal/    # Vue.js patient portal
├── start-all-services.sh               # Podman startup script
├── stop-all-services.sh                # Podman shutdown script
└── README.md                           # This file
```

## Troubleshooting

### Containers Won't Start

If containers fail to start:

```bash
# Stop and remove all containers
./stop-all-services.sh

# Remove any stale volumes (WARNING: deletes data)
podman volume rm healthcare-patients-data healthcare-coverage-data healthcare-claims-data

# Start again
./start-all-services.sh
```

### Port Conflicts

If ports are already in use:

```bash
# Check what's using the ports
lsof -i :8080
lsof -i :8081
lsof -i :8082
lsof -i :5432
lsof -i :5433
lsof -i :5434
lsof -i :8888

# Kill the processes or modify the port mappings in start-all-services.sh
```

### Build Failures

If Maven builds fail:

```bash
# Clean and rebuild manually
cd redhat-healthcare-patients
./mvnw clean install -DskipTests

# Check Java version (needs Java 17+)
java -version
```

### Database Connection Issues

Check that databases are healthy:

```bash
# Check database container health
podman ps --filter name=postgres

# Connect to database manually
podman exec -it healthcare-patients-postgres psql -U healthcare -d healthcare_patients
```

### Frontend Can't Connect to Backend

When running the portal containerized, it uses nginx proxy. When running in dev mode (`npm run dev`), ensure the API URLs in `src/services/api.ts` point to `http://localhost:808X/fhir`.

## Production Deployment

For production deployment:

1. **Security**: Update default credentials in all `application.properties` files
2. **SSL/TLS**: Configure HTTPS for all services
3. **Authentication**: Implement OAuth2/OIDC for API access
4. **Monitoring**: Add health checks and observability tools
5. **Scaling**: Use Kubernetes/OpenShift for orchestration
6. **Backup**: Configure PostgreSQL backup strategies
7. **HIPAA**: Implement audit logging and encryption at rest

## Contributing

Each service has its own README with detailed information:
- [Patient Service README](redhat-healthcare-patients/README.md)
- [Coverage Service README](redhat-healthcare-patients-coverage/README.md)
- [Claims Service README](redhat-healthcare-claims/README.md)
- [Patient Portal README](redhat-healthcare-patient-portal/README.md)

## License

Copyright 2024 Red Hat Healthcare

# Red Hat Healthcare Patients Coverage API

A Quarkus-based FHIR R4 compliant REST API for managing insurance coverage data using the FHIR Coverage resource.

## Overview

This application manages insurance coverage information for healthcare patients, providing FHIR-compliant REST endpoints for creating, reading, updating, and searching coverage records. It complements the Patient API by handling the insurance and coverage aspects of patient care.

## Architecture

- **FHIR R4 Compliance**: Uses HAPI FHIR library for Coverage resource models and validation
- **RESTful API**: FHIR-compliant REST endpoints for Coverage resource operations
- **PostgreSQL Persistence**: Hybrid JSONB storage with indexed fields for performance
- **Layered Architecture**: Clean separation between REST, service, domain, and persistence layers

## Tech Stack

- **Framework**: Quarkus 3.30.5
- **FHIR Library**: HAPI FHIR 7.0.2 (R4)
- **Database**: PostgreSQL 16 with JSONB support
- **ORM**: Hibernate ORM with Panache
- **Migrations**: Flyway
- **Java**: 21

## Prerequisites

- Java 21
- Maven 3.9+
- Docker (for PostgreSQL)

## Quick Start

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

This starts PostgreSQL on port 5433 with database `healthcare_coverage`.

### 2. Run the Application

```bash
./mvnw quarkus:dev
```

The application will start on http://localhost:8081

### 3. Access Endpoints

- **API Base**: http://localhost:8081/fhir/Coverage
- **Swagger UI**: http://localhost:8081/swagger-ui
- **Health Check**: http://localhost:8081/q/health

## FHIR Coverage API

### Create Coverage

```bash
POST /fhir/Coverage
Content-Type: application/json

{
  "resourceType": "Coverage",
  "identifier": [{
    "system": "http://insurance.org/policy",
    "value": "POL-12345"
  }],
  "status": "active",
  "type": {
    "coding": [{
      "system": "http://terminology.hl7.org/CodeSystem/v3-ActCode",
      "code": "HIP",
      "display": "health insurance plan policy"
    }]
  },
  "subscriber": {
    "reference": "Patient/123",
    "display": "John Doe"
  },
  "beneficiary": {
    "reference": "Patient/123",
    "display": "John Doe"
  },
  "payor": [{
    "reference": "Organization/ABC-Insurance",
    "display": "ABC Health Insurance"
  }],
  "period": {
    "start": "2024-01-01",
    "end": "2024-12-31"
  }
}
```

Response: `201 Created` with Location header and created Coverage resource

### Read Coverage

```bash
GET /fhir/Coverage/{id}
```

Response: `200 OK` with Coverage resource

### Update Coverage

```bash
PUT /fhir/Coverage/{id}
Content-Type: application/json

{
  "resourceType": "Coverage",
  "id": "{id}",
  ...
}
```

Response: `200 OK` with updated Coverage resource

### Delete Coverage

```bash
DELETE /fhir/Coverage/{id}
```

Response: `204 No Content` (soft delete, sets active=false)

### Search Coverage

Search by identifier:
```bash
GET /fhir/Coverage?identifier=POL-12345
```

Search by beneficiary (patient):
```bash
GET /fhir/Coverage?beneficiary=Patient/123
```

Search by subscriber:
```bash
GET /fhir/Coverage?subscriber=Patient/456
```

Search by payor (insurance organization):
```bash
GET /fhir/Coverage?payor=Organization/ABC-Insurance
```

Search by status:
```bash
GET /fhir/Coverage?status=active
```

Response: `200 OK` with FHIR Bundle containing search results

## Database Schema

The `coverage` table uses a hybrid approach:

- **fhir_resource (JSONB)**: Complete FHIR Coverage resource
- **Indexed columns**: identifier_value, beneficiary_reference, subscriber_reference, payor_reference, status, period_start, period_end
- **Metadata**: version_id, active, last_updated, created_at

## Configuration

Key application properties:

```properties
# HTTP
quarkus.http.port=8081

# Database
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5433/healthcare_coverage
quarkus.datasource.username=healthcare
quarkus.datasource.password=healthcare

# FHIR
fhir.server.base-url=http://localhost:8081/fhir
fhir.version=R4
fhir.validation.enabled=true
```

## FHIR Coverage Resource

The Coverage resource represents insurance coverage for a patient. Key elements:

- **identifier**: Policy number or coverage ID
- **status**: active | cancelled | draft | entered-in-error
- **type**: Type of coverage (health insurance, dental, vision, etc.)
- **subscriber**: Person who owns the policy (may differ from beneficiary)
- **beneficiary**: Patient receiving coverage
- **payor**: Insurance organization providing coverage
- **period**: Coverage effective dates

## Running Both APIs Together

To run both the Patient and Coverage APIs simultaneously:

1. Start Patient API (port 8080):
   ```bash
   cd redhat-healthcare-patients
   docker-compose up -d
   ./mvnw quarkus:dev
   ```

2. Start Coverage API (port 8081):
   ```bash
   cd redhat-healthcare-patients-coverage
   docker-compose up -d
   ./mvnw quarkus:dev
   ```

Both APIs can run independently or together to provide complete patient and coverage management.

## Development

Run tests:
```bash
./mvnw test
```

Run in dev mode with debugging:
```bash
./mvnw quarkus:dev -Ddebug=5006
```

## Future Enhancements

- Link Coverage to Patient resources across services
- Support for multiple coverage types per patient
- Coverage eligibility verification
- Claims processing integration
- Coordination of Benefits (COB)
- Prior authorization workflow
- Coverage period validation
- Plan benefit details

## License

Copyright Red Hat Healthcare

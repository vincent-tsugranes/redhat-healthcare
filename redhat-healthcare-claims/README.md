# Red Hat Healthcare Claims API

A Quarkus-based FHIR R4 compliant REST API for managing healthcare insurance claims using the FHIR Claim resource.

## Overview

This application manages insurance claims for healthcare services, providing FHIR-compliant REST endpoints for creating, reading, updating, and searching claim records. Claims represent requests for payment for healthcare services provided to patients.

## Architecture

- **FHIR R4 Compliance**: Uses HAPI FHIR library for Claim resource models and validation
- **RESTful API**: FHIR-compliant REST endpoints for Claim resource operations
- **PostgreSQL Persistence**: Hybrid JSONB storage with indexed fields for performance
- **Layered Architecture**: Clean separation between REST, service, domain, and persistence layers

## Tech Stack

- **Framework**: Quarkus 3.30.6
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

This starts PostgreSQL on port 5434 with database `healthcare_claims`.

### 2. Run the Application

```bash
./mvnw quarkus:dev
```

The application will start on http://localhost:8082

### 3. Access Endpoints

- **API Base**: http://localhost:8082/fhir/Claim
- **Swagger UI**: http://localhost:8082/swagger-ui
- **Health Check**: http://localhost:8082/q/health

## FHIR Claim API

### Create Claim

```bash
POST /fhir/Claim
Content-Type: application/json

{
  "resourceType": "Claim",
  "identifier": [{
    "system": "http://hospital.org/claims",
    "value": "CLM-20240001"
  }],
  "status": "active",
  "type": {
    "coding": [{
      "system": "http://terminology.hl7.org/CodeSystem/claim-type",
      "code": "institutional",
      "display": "Institutional"
    }]
  },
  "use": "claim",
  "patient": {
    "reference": "Patient/123",
    "display": "John Doe"
  },
  "created": "2024-01-15T10:30:00Z",
  "insurer": {
    "reference": "Organization/ABC-Insurance",
    "display": "ABC Health Insurance"
  },
  "provider": {
    "reference": "Organization/City-Hospital",
    "display": "City Hospital"
  },
  "priority": {
    "coding": [{
      "system": "http://terminology.hl7.org/CodeSystem/processpriority",
      "code": "normal"
    }]
  },
  "billablePeriod": {
    "start": "2024-01-10",
    "end": "2024-01-12"
  },
  "diagnosis": [{
    "sequence": 1,
    "diagnosisCodeableConcept": {
      "coding": [{
        "system": "http://hl7.org/fhir/sid/icd-10",
        "code": "J20.9",
        "display": "Acute bronchitis"
      }]
    }
  }],
  "item": [{
    "sequence": 1,
    "productOrService": {
      "coding": [{
        "system": "http://www.ama-assn.org/go/cpt",
        "code": "99213",
        "display": "Office visit"
      }]
    },
    "unitPrice": {
      "value": 150.00,
      "currency": "USD"
    },
    "net": {
      "value": 150.00,
      "currency": "USD"
    }
  }],
  "total": {
    "value": 150.00,
    "currency": "USD"
  }
}
```

Response: `201 Created` with Location header and created Claim resource

### Read Claim

```bash
GET /fhir/Claim/{id}
```

Response: `200 OK` with Claim resource

### Update Claim

```bash
PUT /fhir/Claim/{id}
Content-Type: application/json

{
  "resourceType": "Claim",
  "id": "{id}",
  ...
}
```

Response: `200 OK` with updated Claim resource

### Delete Claim

```bash
DELETE /fhir/Claim/{id}
```

Response: `204 No Content` (soft delete, sets active=false)

### Search Claims

Search by identifier:
```bash
GET /fhir/Claim?identifier=CLM-20240001
```

Search by patient:
```bash
GET /fhir/Claim?patient=Patient/123
```

Search by provider:
```bash
GET /fhir/Claim?provider=Organization/City-Hospital
```

Search by insurer:
```bash
GET /fhir/Claim?insurer=Organization/ABC-Insurance
```

Search by status:
```bash
GET /fhir/Claim?status=active
```

Search by use (claim, preauthorization, predetermination):
```bash
GET /fhir/Claim?use=claim
```

Response: `200 OK` with FHIR Bundle containing search results

## Database Schema

The `claims` table uses a hybrid approach:

- **fhir_resource (JSONB)**: Complete FHIR Claim resource
- **Indexed columns**: identifier_value, patient_reference, provider_reference, insurer_reference, status, created_date, billable_period
- **Metadata**: version_id, active, last_updated, created_at

## Configuration

Key application properties:

```properties
# HTTP
quarkus.http.port=8082

# Database
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5434/healthcare_claims
quarkus.datasource.username=healthcare
quarkus.datasource.password=healthcare

# FHIR
fhir.server.base-url=http://localhost:8082/fhir
fhir.version=R4
fhir.validation.enabled=true
```

## FHIR Claim Resource

The Claim resource represents a provider's request for payment for services rendered. Key elements:

- **identifier**: Claim number or identifier
- **status**: active | cancelled | draft | entered-in-error
- **type**: Claim type (institutional, oral, pharmacy, professional, vision)
- **use**: claim | preauthorization | predetermination
- **patient**: Patient who received the services
- **billablePeriod**: Service dates
- **provider**: Healthcare provider submitting the claim
- **insurer**: Insurance organization processing the claim
- **priority**: Claim processing priority
- **diagnosis**: Diagnosis codes (ICD-10)
- **procedure**: Procedure codes
- **item**: Services/products being claimed (CPT codes)
- **total**: Total claim amount

## Running All Healthcare APIs Together

To run the complete healthcare suite:

1. **Patient API** (port 8080):
   ```bash
   cd redhat-healthcare-patients
   docker-compose up -d
   ./mvnw quarkus:dev
   ```

2. **Coverage API** (port 8081):
   ```bash
   cd redhat-healthcare-patients-coverage
   docker-compose up -d
   ./mvnw quarkus:dev
   ```

3. **Claims API** (port 8082):
   ```bash
   cd redhat-healthcare-claims
   docker-compose up -d
   ./mvnw quarkus:dev
   ```

All three APIs can run simultaneously to provide complete patient, coverage, and claims management.

## Development

Run tests:
```bash
./mvnw test
```

Run in dev mode with debugging:
```bash
./mvnw quarkus:dev -Ddebug=5007
```

## Claim Processing Workflow

Typical claim lifecycle:
1. **Create Claim** - Provider submits claim for services rendered
2. **Link to Patient & Coverage** - Associate with patient and their coverage
3. **Review & Validation** - Insurer reviews claim details
4. **Adjudication** - Insurer processes and determines payment
5. **Payment** - Claim is paid or denied
6. **ClaimResponse** - Detailed response created (separate resource)

## Future Enhancements

- Link Claims to Patient and Coverage resources
- ClaimResponse resource for adjudication results
- Support for claim attachments (documents, images)
- Claim status tracking and workflow
- Prior authorization integration
- Electronic claim submission (EDI 837)
- Remittance advice (EDI 835)
- Claim appeal process
- Fraud detection integration
- Real-time eligibility verification

## License

Copyright Red Hat Healthcare

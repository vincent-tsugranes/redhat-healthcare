# Red Hat Healthcare Patients API

A Quarkus-based FHIR R4 compliant REST API for managing healthcare patient data using the FHIR Patient resource.

## Architecture

This application provides a healthcare patient management system with:

- **FHIR R4 Compliance**: Uses HAPI FHIR library for FHIR resource models and validation
- **RESTful API**: FHIR-compliant REST endpoints for Patient resource operations
- **PostgreSQL Persistence**: Hybrid storage approach with JSONB for full FHIR resources and indexed columns for search
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

### 2. Run the Application

```bash
./mvnw quarkus:dev
```

The application will start on http://localhost:8080

### 3. Access Endpoints

- **API Base**: http://localhost:8080/fhir/Patient
- **Swagger UI**: http://localhost:8080/swagger-ui
- **Health Check**: http://localhost:8080/q/health

## FHIR Patient API

- **POST /fhir/Patient** - Create patient
- **GET /fhir/Patient/{id}** - Read patient
- **PUT /fhir/Patient/{id}** - Update patient
- **DELETE /fhir/Patient/{id}** - Delete patient (soft delete)
- **GET /fhir/Patient?params** - Search patients

Search parameters: identifier, family, given, birthdate

## Database Schema

Hybrid approach with JSONB storage plus indexed columns for search performance.

## Next Steps

1. Start PostgreSQL: `docker-compose up -d`
2. Run application: `./mvnw quarkus:dev`
3. Visit Swagger UI: http://localhost:8080/swagger-ui

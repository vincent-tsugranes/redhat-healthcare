# Deployment Guide: Practitioner and Appointment Services

This guide walks through deploying the new Practitioner and Appointment microservices.

## Prerequisites

- Existing services running: Patient (8080), Coverage (8081), Claims (8082)
- Podman installed and running
- Maven installed
- Node.js and npm installed

## Step 1: Create PostgreSQL Databases

Create two new PostgreSQL containers for the Practitioner and Appointment services:

```bash
# Create Practitioner database (port 5435)
podman run -d \
  --name healthcare-practitioner-db \
  -e POSTGRES_DB=healthcare_practitioner \
  -e POSTGRES_USER=healthcare \
  -e POSTGRES_PASSWORD=healthcare \
  -p 5435:5432 \
  postgres:15-alpine

# Create Appointment database (port 5436)
podman run -d \
  --name healthcare-appointment-db \
  -e POSTGRES_DB=healthcare_appointment \
  -e POSTGRES_USER=healthcare \
  -e POSTGRES_PASSWORD=healthcare \
  -p 5436:5432 \
  postgres:15-alpine
```

Verify databases are running:

```bash
podman ps | grep healthcare-.*-db
```

You should see 5 database containers running (patients, coverage, claims, practitioner, appointment).

## Step 2: Build Practitioner Microservice

```bash
cd /Users/vtsugran/Code/redhat-healthcare/redhat-healthcare-practitioner

# Clean and build
./mvnw clean package -DskipTests

# Build container image
podman build -f src/main/docker/Dockerfile.jvm -t healthcare-practitioner:latest .
```

## Step 3: Build Appointment Microservice

```bash
cd /Users/vtsugran/Code/redhat-healthcare/redhat-healthcare-appointment

# Clean and build
./mvnw clean package -DskipTests

# Build container image
podman build -f src/main/docker/Dockerfile.jvm -t healthcare-appointment:latest .
```

## Step 4: Run Practitioner Service

```bash
podman run -d \
  --name healthcare-practitioner \
  -p 8083:8083 \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://host.containers.internal:5435/healthcare_practitioner \
  -e QUARKUS_DATASOURCE_USERNAME=healthcare \
  -e QUARKUS_DATASOURCE_PASSWORD=healthcare \
  healthcare-practitioner:latest
```

Verify it's running:

```bash
podman logs healthcare-practitioner

# Test the endpoint
curl http://localhost:8083/fhir/Practitioner
```

You should see an empty FHIR Bundle response.

## Step 5: Run Appointment Service

```bash
podman run -d \
  --name healthcare-appointment \
  -p 8084:8084 \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://host.containers.internal:5436/healthcare_appointment \
  -e QUARKUS_DATASOURCE_USERNAME=healthcare \
  -e QUARKUS_DATASOURCE_PASSWORD=healthcare \
  healthcare-appointment:latest
```

Verify it's running:

```bash
podman logs healthcare-appointment

# Test the endpoint
curl http://localhost:8084/fhir/Appointment
```

You should see an empty FHIR Bundle response.

## Step 6: Rebuild and Deploy Patient Portal

The portal has already been built with the new features. Deploy it:

```bash
cd /Users/vtsugran/Code/redhat-healthcare/redhat-healthcare-patient-portal

# Stop existing container if running
podman stop healthcare-portal 2>/dev/null || true
podman rm healthcare-portal 2>/dev/null || true

# Build new image with updated code
podman build -t healthcare-portal:latest .

# Run the portal
podman run -d \
  --name healthcare-portal \
  -p 8085:80 \
  healthcare-portal:latest
```

## Step 7: Generate Test Data

Now create practitioners and appointments:

```bash
cd /Users/vtsugran/Code/redhat-healthcare

# Run the test data script
./create-practitioners-appointments.sh
```

This will create:
- 30 practitioners with various specialties across NC cities
- 100 appointments (70% upcoming, 30% past) linking patients to practitioners

## Step 8: Verify Everything Works

### Test Practitioner API

```bash
# Get all practitioners
curl http://localhost:8083/fhir/Practitioner

# Search by specialty
curl "http://localhost:8083/fhir/Practitioner?specialty=Cardiology"

# Search by name
curl "http://localhost:8083/fhir/Practitioner?name=Smith"
```

### Test Appointment API

```bash
# Get all appointments
curl http://localhost:8084/fhir/Appointment

# Search by status
curl "http://localhost:8084/fhir/Appointment?status=booked"

# Search by patient
curl "http://localhost:8084/fhir/Appointment?patient=Patient/<patient-id>"
```

### Test Portal

1. Open browser: http://localhost:8085
2. Click "Providers" in navigation - should see 30 practitioners
3. Search practitioners by name or specialty
4. Filter by specialty dropdown
5. Select a patient from home page
6. Click "My Appointments" - should see their appointments
7. Check "Upcoming" and "Past" tabs
8. Go to "My Claims" - should see care team information if available

## Architecture Overview

Your complete system now includes:

```
Port Map:
- 8080: Patient API
- 8081: Coverage API
- 8082: Claims API
- 8083: Practitioner API (NEW)
- 8084: Appointment API (NEW)
- 8085: Patient Portal (UPDATED)

Database Map:
- 5432: Patients DB
- 5433: Coverage DB
- 5434: Claims DB
- 5435: Practitioner DB (NEW)
- 5436: Appointment DB (NEW)
```

## Troubleshooting

### Database Connection Issues

If services can't connect to databases:

```bash
# Check if databases are running
podman ps | grep postgres

# Check database logs
podman logs healthcare-practitioner-db
podman logs healthcare-appointment-db
```

### Service Startup Issues

```bash
# Check service logs
podman logs healthcare-practitioner
podman logs healthcare-appointment

# Common issues:
# 1. Port already in use - stop conflicting service
# 2. Database not ready - wait 30 seconds and restart service
# 3. Flyway migration failed - check database permissions
```

### Portal Not Showing Data

```bash
# Check browser console for errors
# Verify APIs are accessible:
curl http://localhost:8083/fhir/Practitioner
curl http://localhost:8084/fhir/Appointment

# Check portal logs
podman logs healthcare-portal
```

## Cleanup (Optional)

To remove everything and start fresh:

```bash
# Stop and remove containers
podman stop healthcare-practitioner healthcare-appointment
podman rm healthcare-practitioner healthcare-appointment

# Stop and remove databases
podman stop healthcare-practitioner-db healthcare-appointment-db
podman rm healthcare-practitioner-db healthcare-appointment-db

# Remove images (optional)
podman rmi healthcare-practitioner:latest healthcare-appointment:latest
```

## Next Steps

After deployment, you can:

1. **Create More Test Data** - Run the script again to add more practitioners/appointments
2. **Link Claims to Practitioners** - Update claims to include care team members
3. **Add Scheduling Features** - Implement appointment booking in the portal
4. **Add Provider Profiles** - Create detailed practitioner profile pages
5. **Implement Notifications** - Add appointment reminders
6. **Add Calendar View** - Display appointments in calendar format

## Success Criteria

✅ All 5 database containers running
✅ All 5 microservice containers running
✅ Portal accessible at http://localhost:8085
✅ Provider Directory shows 30 practitioners
✅ Patients have appointments
✅ Appointments show upcoming/past correctly
✅ Claims show care team information

You now have a complete healthcare patient portal with providers and appointment management!

package com.redhat.healthcare.appointment.domain.repository;

import com.redhat.healthcare.appointment.domain.entity.AppointmentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AppointmentRepository implements PanacheRepositoryBase<AppointmentEntity, String> {

    public Optional<AppointmentEntity> findByFhirId(String fhirId) {
        return find("fhirId", fhirId).firstResultOptional();
    }

    public List<AppointmentEntity> findByIdentifier(String system, String value) {
        if (system != null && value != null) {
            return list("identifierSystem = ?1 and identifierValue = ?2 and active = true", system, value);
        } else if (value != null) {
            return list("identifierValue = ?1 and active = true", value);
        }
        return List.of();
    }

    public List<AppointmentEntity> findByPatient(String patientReference) {
        return list("patientReference = ?1 and active = true order by startTime", patientReference);
    }

    public List<AppointmentEntity> findByPractitioner(String practitionerReference) {
        return list("practitionerReference = ?1 and active = true order by startTime", practitionerReference);
    }

    public List<AppointmentEntity> findByStatus(String status) {
        return list("status = ?1 and active = true order by startTime", status);
    }

    public List<AppointmentEntity> findByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            return list("startTime >= ?1 and startTime <= ?2 and active = true order by startTime", start, end);
        } else if (start != null) {
            return list("startTime >= ?1 and active = true order by startTime", start);
        } else if (end != null) {
            return list("startTime <= ?1 and active = true order by startTime", end);
        }
        return List.of();
    }

    public List<AppointmentEntity> findByPatientAndStatus(String patientReference, String status) {
        return list("patientReference = ?1 and status = ?2 and active = true order by startTime",
                   patientReference, status);
    }

    public List<AppointmentEntity> findBySpecialty(String specialtyCode) {
        return list("specialtyCode = ?1 and active = true order by startTime", specialtyCode);
    }

    public List<AppointmentEntity> findAllActive() {
        return list("active = true order by startTime");
    }

    public void softDelete(String fhirId) {
        update("active = false, lastUpdated = ?1 where fhirId = ?2",
               LocalDateTime.now(), fhirId);
    }
}

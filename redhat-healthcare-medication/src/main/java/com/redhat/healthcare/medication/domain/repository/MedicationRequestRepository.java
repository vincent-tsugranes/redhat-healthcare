package com.redhat.healthcare.medication.domain.repository;

import com.redhat.healthcare.medication.domain.entity.MedicationRequestEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MedicationRequestRepository implements PanacheRepositoryBase<MedicationRequestEntity, String> {

    public Optional<MedicationRequestEntity> findByFhirId(String fhirId) {
        return find("fhirId", fhirId).firstResultOptional();
    }

    public List<MedicationRequestEntity> findByPatient(String patientReference) {
        return list("patientReference = ?1 and active = true order by authoredOn desc", patientReference);
    }

    public List<MedicationRequestEntity> findByRequester(String requesterReference) {
        return list("requesterReference = ?1 and active = true order by authoredOn desc", requesterReference);
    }

    public List<MedicationRequestEntity> findByStatus(String status) {
        return list("status = ?1 and active = true order by authoredOn desc", status);
    }

    public List<MedicationRequestEntity> findByMedication(String medicationCode) {
        return list("medicationCode = ?1 and active = true order by authoredOn desc", medicationCode);
    }

    public List<MedicationRequestEntity> findByPatientAndStatus(String patientReference, String status) {
        return list("patientReference = ?1 and status = ?2 and active = true order by authoredOn desc",
                    patientReference, status);
    }

    public List<MedicationRequestEntity> findAllActive() {
        return list("active = true order by authoredOn desc");
    }

    public void softDelete(String fhirId) {
        update("active = false, lastUpdated = ?1 where fhirId = ?2",
               LocalDateTime.now(), fhirId);
    }
}

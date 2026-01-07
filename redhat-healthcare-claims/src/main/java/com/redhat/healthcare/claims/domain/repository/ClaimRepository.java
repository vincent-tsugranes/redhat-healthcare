package com.redhat.healthcare.claims.domain.repository;

import com.redhat.healthcare.claims.domain.entity.ClaimEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClaimRepository implements PanacheRepositoryBase<ClaimEntity, String> {

    public Optional<ClaimEntity> findByFhirId(String fhirId) {
        return find("fhirId", fhirId).firstResultOptional();
    }

    public List<ClaimEntity> findByIdentifier(String system, String value) {
        if (system != null && value != null) {
            return list("identifierSystem = ?1 and identifierValue = ?2 and active = true", system, value);
        } else if (value != null) {
            return list("identifierValue = ?1 and active = true", value);
        }
        return List.of();
    }

    public List<ClaimEntity> findByPatient(String patientReference) {
        return list("patientReference = ?1 and active = true", patientReference);
    }

    public List<ClaimEntity> findByProvider(String providerReference) {
        return list("providerReference = ?1 and active = true", providerReference);
    }

    public List<ClaimEntity> findByInsurer(String insurerReference) {
        return list("insurerReference = ?1 and active = true", insurerReference);
    }

    public List<ClaimEntity> findByStatus(String status) {
        return list("status = ?1 and active = true", status);
    }

    public List<ClaimEntity> findByUse(String use) {
        return list("claimUse = ?1 and active = true", use);
    }

    public void softDelete(String fhirId) {
        update("active = false, lastUpdated = ?1 where fhirId = ?2",
               LocalDateTime.now(), fhirId);
    }

    public List<ClaimEntity> findAllActive() {
        return list("active = true order by createdDate desc");
    }
}

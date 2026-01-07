package com.redhat.healthcare.coverage.domain.repository;

import com.redhat.healthcare.coverage.domain.entity.CoverageEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CoverageRepository implements PanacheRepositoryBase<CoverageEntity, String> {

    public Optional<CoverageEntity> findByFhirId(String fhirId) {
        return find("fhirId", fhirId).firstResultOptional();
    }

    public List<CoverageEntity> findByIdentifier(String system, String value) {
        if (system != null && value != null) {
            return list("identifierSystem = ?1 and identifierValue = ?2", system, value);
        } else if (value != null) {
            return list("identifierValue = ?1", value);
        }
        return List.of();
    }

    public List<CoverageEntity> findByBeneficiary(String beneficiaryReference) {
        return list("beneficiaryReference = ?1 and active = true", beneficiaryReference);
    }

    public List<CoverageEntity> findBySubscriber(String subscriberReference) {
        return list("subscriberReference = ?1 and active = true", subscriberReference);
    }

    public List<CoverageEntity> findByPayor(String payorReference) {
        return list("payorReference = ?1 and active = true", payorReference);
    }

    public List<CoverageEntity> findByStatus(String status) {
        return list("status = ?1 and active = true", status);
    }

    public void softDelete(String fhirId) {
        update("active = false, lastUpdated = ?1 where fhirId = ?2",
               LocalDateTime.now(), fhirId);
    }

    public List<CoverageEntity> findAllActive() {
        return list("active = true");
    }
}

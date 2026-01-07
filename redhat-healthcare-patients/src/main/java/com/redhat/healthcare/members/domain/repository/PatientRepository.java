package com.redhat.healthcare.members.domain.repository;

import com.redhat.healthcare.members.domain.entity.PatientEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PatientRepository implements PanacheRepositoryBase<PatientEntity, String> {

    public Optional<PatientEntity> findByFhirId(String fhirId) {
        return find("fhirId", fhirId).firstResultOptional();
    }

    public List<PatientEntity> findByIdentifier(String system, String value) {
        if (system != null && value != null) {
            return list("identifierSystem = ?1 and identifierValue = ?2", system, value);
        } else if (value != null) {
            return list("identifierValue = ?1", value);
        }
        return List.of();
    }

    public List<PatientEntity> searchByName(String family, String given) {
        StringBuilder query = new StringBuilder("active = true");

        if (family != null && !family.isEmpty()) {
            query.append(" and lower(familyName) like lower(?1)");
            if (given != null && !given.isEmpty()) {
                query.append(" and lower(givenName) like lower(?2)");
                return list(query.toString(), "%" + family + "%", "%" + given + "%");
            }
            return list(query.toString(), "%" + family + "%");
        } else if (given != null && !given.isEmpty()) {
            query.append(" and lower(givenName) like lower(?1)");
            return list(query.toString(), "%" + given + "%");
        }

        return list("active = true");
    }

    public List<PatientEntity> searchByBirthDate(LocalDate birthDate) {
        return list("birthDate = ?1 and active = true", birthDate);
    }

    public void softDelete(String fhirId) {
        update("active = false, lastUpdated = ?1 where fhirId = ?2",
               LocalDateTime.now(), fhirId);
    }

    public List<PatientEntity> findAllActive() {
        return list("active = true");
    }
}

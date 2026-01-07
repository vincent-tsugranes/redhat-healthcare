package com.redhat.healthcare.practitioner.domain.repository;

import com.redhat.healthcare.practitioner.domain.entity.PractitionerEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PractitionerRepository implements PanacheRepositoryBase<PractitionerEntity, String> {

    public Optional<PractitionerEntity> findByFhirId(String fhirId) {
        return find("fhirId", fhirId).firstResultOptional();
    }

    public List<PractitionerEntity> findByIdentifier(String system, String value) {
        if (system != null && value != null) {
            return list("identifierSystem = ?1 and identifierValue = ?2 and active = true", system, value);
        } else if (value != null) {
            return list("identifierValue = ?1 and active = true", value);
        }
        return List.of();
    }

    public Optional<PractitionerEntity> findByNpi(String npi) {
        return find("npi = ?1 and active = true", npi).firstResultOptional();
    }

    public List<PractitionerEntity> findByName(String name) {
        String pattern = "%" + name.toLowerCase() + "%";
        return list("(LOWER(familyName) LIKE ?1 OR LOWER(givenName) LIKE ?1 OR LOWER(fullName) LIKE ?1) and active = true", pattern);
    }

    public List<PractitionerEntity> findByFamilyName(String familyName) {
        String pattern = "%" + familyName.toLowerCase() + "%";
        return list("LOWER(familyName) LIKE ?1 and active = true", pattern);
    }

    public List<PractitionerEntity> findByGivenName(String givenName) {
        String pattern = "%" + givenName.toLowerCase() + "%";
        return list("LOWER(givenName) LIKE ?1 and active = true", pattern);
    }

    public List<PractitionerEntity> findBySpecialty(String specialtyCode) {
        return list("specialtyCode = ?1 and active = true", specialtyCode);
    }

    public Optional<PractitionerEntity> findByEmail(String email) {
        return find("email = ?1 and active = true", email).firstResultOptional();
    }

    public List<PractitionerEntity> findAllActive() {
        return list("active = true order by familyName, givenName");
    }

    public void softDelete(String fhirId) {
        update("active = false, lastUpdated = ?1 where fhirId = ?2",
               LocalDateTime.now(), fhirId);
    }
}

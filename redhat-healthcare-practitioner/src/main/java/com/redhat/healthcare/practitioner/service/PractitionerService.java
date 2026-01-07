package com.redhat.healthcare.practitioner.service;

import com.redhat.healthcare.practitioner.domain.entity.PractitionerEntity;
import com.redhat.healthcare.practitioner.domain.repository.PractitionerRepository;
import com.redhat.healthcare.practitioner.dto.PractitionerSearchCriteria;
import com.redhat.healthcare.practitioner.exception.ResourceNotFoundException;
import com.redhat.healthcare.practitioner.mapper.PractitionerMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hl7.fhir.r4.model.Practitioner;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PractitionerService {

    @Inject
    PractitionerRepository repository;

    @Inject
    FhirParserService parserService;

    @Inject
    FhirValidationService validationService;

    @Inject
    PractitionerMapper mapper;

    @Transactional
    public Practitioner createPractitioner(Practitioner practitioner) {
        validationService.validateOrThrow(practitioner);

        if (practitioner.getId() == null || practitioner.getId().isEmpty()) {
            practitioner.setId(parserService.generateId());
        }

        if (practitioner.getMeta() == null) {
            practitioner.setMeta(new org.hl7.fhir.r4.model.Meta());
        }
        practitioner.getMeta().setVersionId("1");
        practitioner.getMeta().setLastUpdated(new Date());

        PractitionerEntity entity = mapper.toEntity(practitioner);
        entity.versionId = 1L;
        entity.createdAt = LocalDateTime.now();
        entity.lastUpdated = LocalDateTime.now();

        repository.persist(entity);

        return practitioner;
    }

    @Transactional
    public Practitioner updatePractitioner(String id, Practitioner practitioner) {
        PractitionerEntity existing = repository.findByFhirId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Practitioner/" + id));

        validationService.validateOrThrow(practitioner);

        practitioner.setId(id);

        Long newVersion = existing.versionId + 1;
        if (practitioner.getMeta() == null) {
            practitioner.setMeta(new org.hl7.fhir.r4.model.Meta());
        }
        practitioner.getMeta().setVersionId(newVersion.toString());
        practitioner.getMeta().setLastUpdated(new Date());

        PractitionerEntity updated = mapper.toEntity(practitioner);
        updated.versionId = newVersion;
        updated.lastUpdated = LocalDateTime.now();
        updated.createdAt = existing.createdAt;

        repository.persist(updated);

        return practitioner;
    }

    public Practitioner getPractitioner(String id) {
        PractitionerEntity entity = repository.findByFhirId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Practitioner/" + id));

        return mapper.toFhir(entity);
    }

    @Transactional
    public void deletePractitioner(String id) {
        if (!repository.findByFhirId(id).isPresent()) {
            throw new ResourceNotFoundException("Practitioner/" + id);
        }
        repository.softDelete(id);
    }

    public List<Practitioner> searchPractitioners(PractitionerSearchCriteria criteria) {
        List<PractitionerEntity> entities;

        if (criteria.getIdentifier() != null && !criteria.getIdentifier().isEmpty()) {
            entities = repository.findByIdentifier(
                criteria.getIdentifierSystem(),
                criteria.getIdentifier()
            );
        } else if (criteria.getNpi() != null && !criteria.getNpi().isEmpty()) {
            entities = repository.findByNpi(criteria.getNpi())
                .map(List::of)
                .orElse(List.of());
        } else if (criteria.getName() != null && !criteria.getName().isEmpty()) {
            entities = repository.findByName(criteria.getName());
        } else if (criteria.getFamily() != null && !criteria.getFamily().isEmpty()) {
            entities = repository.findByFamilyName(criteria.getFamily());
        } else if (criteria.getGiven() != null && !criteria.getGiven().isEmpty()) {
            entities = repository.findByGivenName(criteria.getGiven());
        } else if (criteria.getEmail() != null && !criteria.getEmail().isEmpty()) {
            entities = repository.findByEmail(criteria.getEmail())
                .map(List::of)
                .orElse(List.of());
        } else if (criteria.getSpecialty() != null && !criteria.getSpecialty().isEmpty()) {
            entities = repository.findBySpecialty(criteria.getSpecialty());
        } else {
            entities = repository.findAllActive();
        }

        return entities.stream()
            .map(mapper::toFhir)
            .collect(Collectors.toList());
    }
}

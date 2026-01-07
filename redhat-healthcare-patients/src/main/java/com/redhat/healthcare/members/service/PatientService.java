package com.redhat.healthcare.members.service;

import com.redhat.healthcare.members.domain.entity.PatientEntity;
import com.redhat.healthcare.members.domain.repository.PatientRepository;
import com.redhat.healthcare.members.dto.PatientSearchCriteria;
import com.redhat.healthcare.members.exception.ResourceNotFoundException;
import com.redhat.healthcare.members.mapper.PatientMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hl7.fhir.r4.model.Patient;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PatientService {

    @Inject
    PatientRepository repository;

    @Inject
    FhirParserService parserService;

    @Inject
    FhirValidationService validationService;

    @Inject
    PatientMapper mapper;

    @Transactional
    public Patient createPatient(Patient patient) {
        validationService.validateOrThrow(patient);

        if (patient.getId() == null || patient.getId().isEmpty()) {
            patient.setId(parserService.generateId());
        }

        if (patient.getMeta() == null) {
            patient.setMeta(new org.hl7.fhir.r4.model.Meta());
        }
        patient.getMeta().setVersionId("1");
        patient.getMeta().setLastUpdated(new Date());

        PatientEntity entity = mapper.toEntity(patient);
        entity.versionId = 1L;
        entity.createdAt = LocalDateTime.now();
        entity.lastUpdated = LocalDateTime.now();

        repository.persist(entity);

        return patient;
    }

    @Transactional
    public Patient updatePatient(String id, Patient patient) {
        PatientEntity existing = repository.findByFhirId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient/" + id));

        validationService.validateOrThrow(patient);

        patient.setId(id);

        Long newVersion = existing.versionId + 1;
        if (patient.getMeta() == null) {
            patient.setMeta(new org.hl7.fhir.r4.model.Meta());
        }
        patient.getMeta().setVersionId(newVersion.toString());
        patient.getMeta().setLastUpdated(new Date());

        PatientEntity updated = mapper.toEntity(patient);
        updated.versionId = newVersion;
        updated.lastUpdated = LocalDateTime.now();
        updated.createdAt = existing.createdAt;

        repository.persist(updated);

        return patient;
    }

    public Patient getPatient(String id) {
        PatientEntity entity = repository.findByFhirId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient/" + id));

        return mapper.toFhir(entity);
    }

    @Transactional
    public void deletePatient(String id) {
        if (!repository.findByFhirId(id).isPresent()) {
            throw new ResourceNotFoundException("Patient/" + id);
        }
        repository.softDelete(id);
    }

    public List<Patient> searchPatients(PatientSearchCriteria criteria) {
        List<PatientEntity> entities;

        if (criteria.getIdentifier() != null && !criteria.getIdentifier().isEmpty()) {
            entities = repository.findByIdentifier(
                criteria.getIdentifierSystem(),
                criteria.getIdentifier()
            );
        } else if ((criteria.getFamily() != null && !criteria.getFamily().isEmpty()) ||
                   (criteria.getGiven() != null && !criteria.getGiven().isEmpty())) {
            entities = repository.searchByName(
                criteria.getFamily(),
                criteria.getGiven()
            );
        } else if (criteria.getBirthDate() != null) {
            entities = repository.searchByBirthDate(criteria.getBirthDate());
        } else {
            entities = repository.findAllActive();
        }

        return entities.stream()
            .map(mapper::toFhir)
            .collect(Collectors.toList());
    }
}

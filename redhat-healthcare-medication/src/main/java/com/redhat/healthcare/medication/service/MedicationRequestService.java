package com.redhat.healthcare.medication.service;

import com.redhat.healthcare.medication.domain.entity.MedicationRequestEntity;
import com.redhat.healthcare.medication.domain.repository.MedicationRequestRepository;
import com.redhat.healthcare.medication.dto.MedicationRequestSearchCriteria;
import com.redhat.healthcare.medication.exception.ResourceNotFoundException;
import com.redhat.healthcare.medication.mapper.MedicationRequestMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hl7.fhir.r4.model.MedicationRequest;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class MedicationRequestService {

    @Inject
    MedicationRequestRepository repository;

    @Inject
    FhirParserService parserService;

    @Inject
    FhirValidationService validationService;

    @Inject
    MedicationRequestMapper mapper;

    @Transactional
    public MedicationRequest createMedicationRequest(MedicationRequest medicationRequest) {
        validationService.validateOrThrow(medicationRequest);

        if (medicationRequest.getId() == null || medicationRequest.getId().isEmpty()) {
            medicationRequest.setId(parserService.generateId());
        }

        if (medicationRequest.getMeta() == null) {
            medicationRequest.setMeta(new org.hl7.fhir.r4.model.Meta());
        }
        medicationRequest.getMeta().setVersionId("1");
        medicationRequest.getMeta().setLastUpdated(new Date());

        MedicationRequestEntity entity = mapper.toEntity(medicationRequest);
        entity.versionId = 1L;
        entity.createdAt = LocalDateTime.now();
        entity.lastUpdated = LocalDateTime.now();

        repository.persist(entity);

        return medicationRequest;
    }

    @Transactional
    public MedicationRequest updateMedicationRequest(String id, MedicationRequest medicationRequest) {
        MedicationRequestEntity existing = repository.findByFhirId(id)
            .orElseThrow(() -> new ResourceNotFoundException("MedicationRequest/" + id));

        validationService.validateOrThrow(medicationRequest);

        medicationRequest.setId(id);

        Long newVersion = existing.versionId + 1;
        if (medicationRequest.getMeta() == null) {
            medicationRequest.setMeta(new org.hl7.fhir.r4.model.Meta());
        }
        medicationRequest.getMeta().setVersionId(newVersion.toString());
        medicationRequest.getMeta().setLastUpdated(new Date());

        MedicationRequestEntity updated = mapper.toEntity(medicationRequest);
        updated.versionId = newVersion;
        updated.lastUpdated = LocalDateTime.now();
        updated.createdAt = existing.createdAt;

        repository.persist(updated);

        return medicationRequest;
    }

    public MedicationRequest getMedicationRequest(String id) {
        MedicationRequestEntity entity = repository.findByFhirId(id)
            .orElseThrow(() -> new ResourceNotFoundException("MedicationRequest/" + id));

        return mapper.toFhir(entity);
    }

    @Transactional
    public void deleteMedicationRequest(String id) {
        if (!repository.findByFhirId(id).isPresent()) {
            throw new ResourceNotFoundException("MedicationRequest/" + id);
        }
        repository.softDelete(id);
    }

    public List<MedicationRequest> searchMedicationRequests(MedicationRequestSearchCriteria criteria) {
        List<MedicationRequestEntity> entities;

        if (criteria.getPatient() != null && !criteria.getPatient().isEmpty() &&
            criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
            entities = repository.findByPatientAndStatus(
                criteria.getPatient(),
                criteria.getStatus()
            );
        } else if (criteria.getPatient() != null && !criteria.getPatient().isEmpty()) {
            entities = repository.findByPatient(criteria.getPatient());
        } else if (criteria.getRequester() != null && !criteria.getRequester().isEmpty()) {
            entities = repository.findByRequester(criteria.getRequester());
        } else if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
            entities = repository.findByStatus(criteria.getStatus());
        } else if (criteria.getMedication() != null && !criteria.getMedication().isEmpty()) {
            entities = repository.findByMedication(criteria.getMedication());
        } else {
            entities = repository.findAllActive();
        }

        return entities.stream()
            .map(mapper::toFhir)
            .collect(Collectors.toList());
    }
}

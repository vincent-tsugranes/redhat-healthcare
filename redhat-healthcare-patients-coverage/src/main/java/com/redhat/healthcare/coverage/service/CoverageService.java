package com.redhat.healthcare.coverage.service;

import com.redhat.healthcare.coverage.domain.entity.CoverageEntity;
import com.redhat.healthcare.coverage.domain.repository.CoverageRepository;
import com.redhat.healthcare.coverage.dto.CoverageSearchCriteria;
import com.redhat.healthcare.coverage.exception.ResourceNotFoundException;
import com.redhat.healthcare.coverage.mapper.CoverageMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hl7.fhir.r4.model.Coverage;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CoverageService {

    @Inject
    CoverageRepository repository;

    @Inject
    FhirParserService parserService;

    @Inject
    FhirValidationService validationService;

    @Inject
    CoverageMapper mapper;

    @Transactional
    public Coverage createCoverage(Coverage coverage) {
        validationService.validateOrThrow(coverage);

        if (coverage.getId() == null || coverage.getId().isEmpty()) {
            coverage.setId(parserService.generateId());
        }

        if (coverage.getMeta() == null) {
            coverage.setMeta(new org.hl7.fhir.r4.model.Meta());
        }
        coverage.getMeta().setVersionId("1");
        coverage.getMeta().setLastUpdated(new Date());

        CoverageEntity entity = mapper.toEntity(coverage);
        entity.versionId = 1L;
        entity.createdAt = LocalDateTime.now();
        entity.lastUpdated = LocalDateTime.now();

        repository.persist(entity);

        return coverage;
    }

    @Transactional
    public Coverage updateCoverage(String id, Coverage coverage) {
        CoverageEntity existing = repository.findByFhirId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coverage/" + id));

        validationService.validateOrThrow(coverage);

        coverage.setId(id);

        Long newVersion = existing.versionId + 1;
        if (coverage.getMeta() == null) {
            coverage.setMeta(new org.hl7.fhir.r4.model.Meta());
        }
        coverage.getMeta().setVersionId(newVersion.toString());
        coverage.getMeta().setLastUpdated(new Date());

        CoverageEntity updated = mapper.toEntity(coverage);
        updated.versionId = newVersion;
        updated.lastUpdated = LocalDateTime.now();
        updated.createdAt = existing.createdAt;

        repository.persist(updated);

        return coverage;
    }

    public Coverage getCoverage(String id) {
        CoverageEntity entity = repository.findByFhirId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coverage/" + id));

        return mapper.toFhir(entity);
    }

    @Transactional
    public void deleteCoverage(String id) {
        if (!repository.findByFhirId(id).isPresent()) {
            throw new ResourceNotFoundException("Coverage/" + id);
        }
        repository.softDelete(id);
    }

    public List<Coverage> searchCoverage(CoverageSearchCriteria criteria) {
        List<CoverageEntity> entities;

        if (criteria.getIdentifier() != null && !criteria.getIdentifier().isEmpty()) {
            entities = repository.findByIdentifier(
                criteria.getIdentifierSystem(),
                criteria.getIdentifier()
            );
        } else if (criteria.getBeneficiary() != null && !criteria.getBeneficiary().isEmpty()) {
            entities = repository.findByBeneficiary(criteria.getBeneficiary());
        } else if (criteria.getSubscriber() != null && !criteria.getSubscriber().isEmpty()) {
            entities = repository.findBySubscriber(criteria.getSubscriber());
        } else if (criteria.getPayor() != null && !criteria.getPayor().isEmpty()) {
            entities = repository.findByPayor(criteria.getPayor());
        } else if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
            entities = repository.findByStatus(criteria.getStatus());
        } else {
            entities = repository.findAllActive();
        }

        return entities.stream()
            .map(mapper::toFhir)
            .collect(Collectors.toList());
    }
}

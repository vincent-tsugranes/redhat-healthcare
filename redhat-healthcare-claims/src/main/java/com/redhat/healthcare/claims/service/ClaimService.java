package com.redhat.healthcare.claims.service;

import com.redhat.healthcare.claims.domain.entity.ClaimEntity;
import com.redhat.healthcare.claims.domain.repository.ClaimRepository;
import com.redhat.healthcare.claims.dto.ClaimSearchCriteria;
import com.redhat.healthcare.claims.exception.ResourceNotFoundException;
import com.redhat.healthcare.claims.mapper.ClaimMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hl7.fhir.r4.model.Claim;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ClaimService {

    @Inject
    ClaimRepository repository;

    @Inject
    FhirParserService parserService;

    @Inject
    FhirValidationService validationService;

    @Inject
    ClaimMapper mapper;

    @Transactional
    public Claim createClaim(Claim claim) {
        validationService.validateOrThrow(claim);

        if (claim.getId() == null || claim.getId().isEmpty()) {
            claim.setId(parserService.generateId());
        }

        if (claim.getMeta() == null) {
            claim.setMeta(new org.hl7.fhir.r4.model.Meta());
        }
        claim.getMeta().setVersionId("1");
        claim.getMeta().setLastUpdated(new Date());

        ClaimEntity entity = mapper.toEntity(claim);
        entity.versionId = 1L;
        entity.createdAt = LocalDateTime.now();
        entity.lastUpdated = LocalDateTime.now();

        repository.persist(entity);

        return claim;
    }

    @Transactional
    public Claim updateClaim(String id, Claim claim) {
        ClaimEntity existing = repository.findByFhirId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Claim/" + id));

        validationService.validateOrThrow(claim);

        claim.setId(id);

        Long newVersion = existing.versionId + 1;
        if (claim.getMeta() == null) {
            claim.setMeta(new org.hl7.fhir.r4.model.Meta());
        }
        claim.getMeta().setVersionId(newVersion.toString());
        claim.getMeta().setLastUpdated(new Date());

        ClaimEntity updated = mapper.toEntity(claim);
        updated.versionId = newVersion;
        updated.lastUpdated = LocalDateTime.now();
        updated.createdAt = existing.createdAt;

        repository.persist(updated);

        return claim;
    }

    public Claim getClaim(String id) {
        ClaimEntity entity = repository.findByFhirId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Claim/" + id));

        return mapper.toFhir(entity);
    }

    @Transactional
    public void deleteClaim(String id) {
        if (!repository.findByFhirId(id).isPresent()) {
            throw new ResourceNotFoundException("Claim/" + id);
        }
        repository.softDelete(id);
    }

    public List<Claim> searchClaims(ClaimSearchCriteria criteria) {
        List<ClaimEntity> entities;

        if (criteria.getIdentifier() != null && !criteria.getIdentifier().isEmpty()) {
            entities = repository.findByIdentifier(
                criteria.getIdentifierSystem(),
                criteria.getIdentifier()
            );
        } else if (criteria.getPatient() != null && !criteria.getPatient().isEmpty()) {
            entities = repository.findByPatient(criteria.getPatient());
        } else if (criteria.getProvider() != null && !criteria.getProvider().isEmpty()) {
            entities = repository.findByProvider(criteria.getProvider());
        } else if (criteria.getInsurer() != null && !criteria.getInsurer().isEmpty()) {
            entities = repository.findByInsurer(criteria.getInsurer());
        } else if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
            entities = repository.findByStatus(criteria.getStatus());
        } else if (criteria.getUse() != null && !criteria.getUse().isEmpty()) {
            entities = repository.findByUse(criteria.getUse());
        } else {
            entities = repository.findAllActive();
        }

        return entities.stream()
            .map(mapper::toFhir)
            .collect(Collectors.toList());
    }
}

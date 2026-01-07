package com.redhat.healthcare.coverage.mapper;

import ca.uhn.fhir.parser.IParser;
import com.redhat.healthcare.coverage.domain.entity.CoverageEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@ApplicationScoped
public class CoverageMapper {

    @Inject
    IParser jsonParser;

    public CoverageEntity toEntity(Coverage coverage) {
        CoverageEntity entity = new CoverageEntity();

        entity.fhirId = coverage.getIdElement().getIdPart();
        entity.fhirResource = jsonParser.encodeResourceToString(coverage);
        entity.active = true;

        if (coverage.hasIdentifier() && !coverage.getIdentifier().isEmpty()) {
            Identifier identifier = coverage.getIdentifier().get(0);
            entity.identifierSystem = identifier.getSystem();
            entity.identifierValue = identifier.getValue();
        }

        if (coverage.hasStatus()) {
            entity.status = coverage.getStatus().toCode();
        }

        if (coverage.hasBeneficiary()) {
            entity.beneficiaryReference = coverage.getBeneficiary().getReference();
        }

        if (coverage.hasSubscriber()) {
            entity.subscriberReference = coverage.getSubscriber().getReference();
        }

        if (coverage.hasPayor() && !coverage.getPayor().isEmpty()) {
            Reference payor = coverage.getPayor().get(0);
            entity.payorReference = payor.getReference();
            entity.payorDisplay = payor.getDisplay();
        }

        if (coverage.hasType()) {
            CodeableConcept type = coverage.getType();
            if (type.hasCoding() && !type.getCoding().isEmpty()) {
                entity.typeCode = type.getCoding().get(0).getCode();
                entity.typeDisplay = type.getCoding().get(0).getDisplay();
            }
        }

        if (coverage.hasPeriod()) {
            Period period = coverage.getPeriod();
            if (period.hasStart()) {
                entity.periodStart = convertToLocalDate(period.getStart());
            }
            if (period.hasEnd()) {
                entity.periodEnd = convertToLocalDate(period.getEnd());
            }
        }

        entity.lastUpdated = LocalDateTime.now();

        return entity;
    }

    public Coverage toFhir(CoverageEntity entity) {
        return jsonParser.parseResource(Coverage.class, entity.fhirResource);
    }

    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}

package com.redhat.healthcare.claims.mapper;

import ca.uhn.fhir.parser.IParser;
import com.redhat.healthcare.claims.domain.entity.ClaimEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@ApplicationScoped
public class ClaimMapper {

    @Inject
    IParser jsonParser;

    public ClaimEntity toEntity(Claim claim) {
        ClaimEntity entity = new ClaimEntity();

        entity.fhirId = claim.getIdElement().getIdPart();
        entity.fhirResource = jsonParser.encodeResourceToString(claim);
        entity.active = true;

        if (claim.hasIdentifier() && !claim.getIdentifier().isEmpty()) {
            Identifier identifier = claim.getIdentifier().get(0);
            entity.identifierSystem = identifier.getSystem();
            entity.identifierValue = identifier.getValue();
        }

        if (claim.hasStatus()) {
            entity.status = claim.getStatus().toCode();
        }

        if (claim.hasType() && claim.getType().hasCoding() && !claim.getType().getCoding().isEmpty()) {
            entity.claimType = claim.getType().getCoding().get(0).getCode();
        }

        if (claim.hasUse()) {
            entity.claimUse = claim.getUse().toCode();
        }

        if (claim.hasPatient()) {
            entity.patientReference = claim.getPatient().getReference();
            entity.patientDisplay = claim.getPatient().getDisplay();
        }

        if (claim.hasProvider()) {
            entity.providerReference = claim.getProvider().getReference();
            entity.providerDisplay = claim.getProvider().getDisplay();
        }

        if (claim.hasInsurer()) {
            entity.insurerReference = claim.getInsurer().getReference();
            entity.insurerDisplay = claim.getInsurer().getDisplay();
        }

        if (claim.hasPriority()) {
            CodeableConcept priority = claim.getPriority();
            if (priority.hasCoding() && !priority.getCoding().isEmpty()) {
                entity.priorityCode = priority.getCoding().get(0).getCode();
                entity.priorityDisplay = priority.getCoding().get(0).getDisplay();
            }
        }

        if (claim.hasTotal()) {
            Money total = claim.getTotal();
            entity.totalValue = total.getValue();
            entity.totalCurrency = total.getCurrency();
        }

        if (claim.hasCreated()) {
            entity.createdDate = convertToLocalDateTime(claim.getCreated());
        }

        if (claim.hasBillablePeriod()) {
            Period period = claim.getBillablePeriod();
            if (period.hasStart()) {
                entity.billablePeriodStart = convertToLocalDate(period.getStart());
            }
            if (period.hasEnd()) {
                entity.billablePeriodEnd = convertToLocalDate(period.getEnd());
            }
        }

        entity.lastUpdated = LocalDateTime.now();

        return entity;
    }

    public Claim toFhir(ClaimEntity entity) {
        return jsonParser.parseResource(Claim.class, entity.fhirResource);
    }

    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}

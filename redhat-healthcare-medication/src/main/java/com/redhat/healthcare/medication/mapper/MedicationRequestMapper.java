package com.redhat.healthcare.medication.mapper;

import ca.uhn.fhir.parser.IParser;
import com.redhat.healthcare.medication.domain.entity.MedicationRequestEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@ApplicationScoped
public class MedicationRequestMapper {

    @Inject
    IParser jsonParser;

    public MedicationRequestEntity toEntity(MedicationRequest medicationRequest) {
        MedicationRequestEntity entity = new MedicationRequestEntity();

        entity.fhirId = medicationRequest.getIdElement().getIdPart();
        entity.fhirResource = jsonParser.encodeResourceToString(medicationRequest);
        entity.active = true;

        // Extract status and intent
        if (medicationRequest.hasStatus()) {
            entity.status = medicationRequest.getStatus().toCode();
        }

        if (medicationRequest.hasIntent()) {
            entity.intent = medicationRequest.getIntent().toCode();
        }

        // Extract medication information
        if (medicationRequest.hasMedicationCodeableConcept()) {
            CodeableConcept medication = medicationRequest.getMedicationCodeableConcept();

            if (medication.hasText()) {
                entity.medicationDisplay = medication.getText();
            }

            if (medication.hasCoding() && !medication.getCoding().isEmpty()) {
                Coding coding = medication.getCoding().get(0);
                entity.medicationCode = coding.getCode();
                entity.medicationSystem = coding.getSystem();

                if (entity.medicationDisplay == null && coding.hasDisplay()) {
                    entity.medicationDisplay = coding.getDisplay();
                }
            }
        }

        // Extract patient reference
        if (medicationRequest.hasSubject()) {
            entity.patientReference = medicationRequest.getSubject().getReference();
            entity.patientDisplay = medicationRequest.getSubject().getDisplay();
        }

        // Extract requester (practitioner) reference
        if (medicationRequest.hasRequester()) {
            entity.requesterReference = medicationRequest.getRequester().getReference();
            entity.requesterDisplay = medicationRequest.getRequester().getDisplay();
        }

        // Extract authored date
        if (medicationRequest.hasAuthoredOn()) {
            entity.authoredOn = convertToLocalDateTime(medicationRequest.getAuthoredOn());
        }

        // Extract dosage instruction
        if (medicationRequest.hasDosageInstruction() && !medicationRequest.getDosageInstruction().isEmpty()) {
            Dosage dosage = medicationRequest.getDosageInstruction().get(0);
            if (dosage.hasText()) {
                entity.dosageText = dosage.getText();
            }
        }

        // Extract dispense request
        if (medicationRequest.hasDispenseRequest()) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest =
                medicationRequest.getDispenseRequest();

            // Extract quantity
            if (dispenseRequest.hasQuantity()) {
                Quantity quantity = dispenseRequest.getQuantity();
                if (quantity.hasValue()) {
                    entity.quantityValue = quantity.getValue();
                }
                if (quantity.hasUnit()) {
                    entity.quantityUnit = quantity.getUnit();
                }
            }

            // Extract refills
            if (dispenseRequest.hasNumberOfRepeatsAllowed()) {
                entity.refillsAllowed = dispenseRequest.getNumberOfRepeatsAllowed();
            }

            // Extract supply duration
            if (dispenseRequest.hasExpectedSupplyDuration()) {
                Duration duration = dispenseRequest.getExpectedSupplyDuration();
                if (duration.hasValue()) {
                    entity.supplyDurationValue = duration.getValue();
                }
                if (duration.hasUnit()) {
                    entity.supplyDurationUnit = duration.getUnit();
                }
            }
        }

        entity.lastUpdated = LocalDateTime.now();

        return entity;
    }

    public MedicationRequest toFhir(MedicationRequestEntity entity) {
        return jsonParser.parseResource(MedicationRequest.class, entity.fhirResource);
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}

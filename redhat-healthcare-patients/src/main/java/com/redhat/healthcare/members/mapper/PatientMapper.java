package com.redhat.healthcare.members.mapper;

import ca.uhn.fhir.parser.IParser;
import com.redhat.healthcare.members.domain.entity.PatientEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@ApplicationScoped
public class PatientMapper {

    @Inject
    IParser jsonParser;

    public PatientEntity toEntity(Patient patient) {
        PatientEntity entity = new PatientEntity();

        entity.fhirId = patient.getIdElement().getIdPart();
        entity.fhirResource = jsonParser.encodeResourceToString(patient);
        entity.active = patient.getActive();

        if (patient.hasIdentifier() && !patient.getIdentifier().isEmpty()) {
            Identifier identifier = patient.getIdentifier().get(0);
            entity.identifierSystem = identifier.getSystem();
            entity.identifierValue = identifier.getValue();
        }

        if (patient.hasName() && !patient.getName().isEmpty()) {
            HumanName name = patient.getName().get(0);
            entity.familyName = name.getFamily();
            if (name.hasGiven() && !name.getGiven().isEmpty()) {
                entity.givenName = name.getGiven().get(0).getValue();
            }
        }

        if (patient.hasBirthDate()) {
            entity.birthDate = convertToLocalDate(patient.getBirthDate());
        }

        if (patient.hasGender()) {
            entity.gender = patient.getGender().toCode();
        }

        entity.lastUpdated = LocalDateTime.now();

        return entity;
    }

    public Patient toFhir(PatientEntity entity) {
        return jsonParser.parseResource(Patient.class, entity.fhirResource);
    }

    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}

package com.redhat.healthcare.practitioner.mapper;

import ca.uhn.fhir.parser.IParser;
import com.redhat.healthcare.practitioner.domain.entity.PractitionerEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@ApplicationScoped
public class PractitionerMapper {

    @Inject
    IParser jsonParser;

    public PractitionerEntity toEntity(Practitioner practitioner) {
        PractitionerEntity entity = new PractitionerEntity();

        entity.fhirId = practitioner.getIdElement().getIdPart();
        entity.fhirResource = jsonParser.encodeResourceToString(practitioner);
        entity.active = practitioner.getActive();

        // Extract identifier
        if (practitioner.hasIdentifier() && !practitioner.getIdentifier().isEmpty()) {
            Identifier identifier = practitioner.getIdentifier().get(0);
            entity.identifierSystem = identifier.getSystem();
            entity.identifierValue = identifier.getValue();

            // Look for NPI in identifiers
            for (Identifier id : practitioner.getIdentifier()) {
                if (id.hasSystem() && id.getSystem().contains("us-npi")) {
                    entity.npi = id.getValue();
                    break;
                }
            }
        }

        // Extract name
        if (practitioner.hasName() && !practitioner.getName().isEmpty()) {
            HumanName name = practitioner.getName().get(0);

            if (name.hasFamily()) {
                entity.familyName = name.getFamily();
            }

            if (name.hasGiven() && !name.getGiven().isEmpty()) {
                entity.givenName = name.getGiven().get(0).getValue();
            }

            if (name.hasText()) {
                entity.fullName = name.getText();
            } else {
                // Build full name from parts
                StringBuilder fullNameBuilder = new StringBuilder();
                if (name.hasPrefix() && !name.getPrefix().isEmpty()) {
                    entity.prefix = name.getPrefix().get(0).getValue();
                    fullNameBuilder.append(entity.prefix).append(" ");
                }
                if (entity.givenName != null) {
                    fullNameBuilder.append(entity.givenName).append(" ");
                }
                if (entity.familyName != null) {
                    fullNameBuilder.append(entity.familyName);
                }
                if (name.hasSuffix() && !name.getSuffix().isEmpty()) {
                    entity.suffix = name.getSuffix().get(0).getValue();
                    fullNameBuilder.append(" ").append(entity.suffix);
                }
                entity.fullName = fullNameBuilder.toString().trim();
            }
        }

        // Extract telecom (phone and email)
        if (practitioner.hasTelecom()) {
            for (ContactPoint telecom : practitioner.getTelecom()) {
                if (telecom.hasSystem() && telecom.hasValue()) {
                    if (telecom.getSystem() == ContactPoint.ContactPointSystem.PHONE) {
                        entity.phone = telecom.getValue();
                    } else if (telecom.getSystem() == ContactPoint.ContactPointSystem.EMAIL) {
                        entity.email = telecom.getValue();
                    }
                }
            }
        }

        // Extract gender
        if (practitioner.hasGender()) {
            entity.gender = practitioner.getGender().toCode();
        }

        // Extract birth date
        if (practitioner.hasBirthDate()) {
            entity.birthDate = convertToLocalDate(practitioner.getBirthDate());
        }

        // Extract specialty from qualifications
        if (practitioner.hasQualification() && !practitioner.getQualification().isEmpty()) {
            Practitioner.PractitionerQualificationComponent qual = practitioner.getQualification().get(0);
            if (qual.hasCode() && qual.getCode().hasCoding() && !qual.getCode().getCoding().isEmpty()) {
                Coding coding = qual.getCode().getCoding().get(0);
                entity.specialtyCode = coding.getCode();
                entity.specialtyDisplay = coding.getDisplay();
                entity.specialtySystem = coding.getSystem();
            }
        }

        // Extract address
        if (practitioner.hasAddress() && !practitioner.getAddress().isEmpty()) {
            Address address = practitioner.getAddress().get(0);

            if (address.hasLine() && !address.getLine().isEmpty()) {
                entity.addressLine = address.getLine().get(0).getValue();
            }
            if (address.hasCity()) {
                entity.city = address.getCity();
            }
            if (address.hasState()) {
                entity.state = address.getState();
            }
            if (address.hasPostalCode()) {
                entity.postalCode = address.getPostalCode();
            }
            if (address.hasCountry()) {
                entity.country = address.getCountry();
            }
        }

        entity.lastUpdated = LocalDateTime.now();

        return entity;
    }

    public Practitioner toFhir(PractitionerEntity entity) {
        return jsonParser.parseResource(Practitioner.class, entity.fhirResource);
    }

    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}

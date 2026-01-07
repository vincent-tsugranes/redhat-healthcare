package com.redhat.healthcare.appointment.mapper;

import ca.uhn.fhir.parser.IParser;
import com.redhat.healthcare.appointment.domain.entity.AppointmentEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@ApplicationScoped
public class AppointmentMapper {

    @Inject
    IParser jsonParser;

    public AppointmentEntity toEntity(Appointment appointment) {
        AppointmentEntity entity = new AppointmentEntity();

        entity.fhirId = appointment.getIdElement().getIdPart();
        entity.fhirResource = jsonParser.encodeResourceToString(appointment);
        entity.active = true;

        // Extract identifier
        if (appointment.hasIdentifier() && !appointment.getIdentifier().isEmpty()) {
            Identifier identifier = appointment.getIdentifier().get(0);
            entity.identifierSystem = identifier.getSystem();
            entity.identifierValue = identifier.getValue();
        }

        // Extract status
        if (appointment.hasStatus()) {
            entity.status = appointment.getStatus().toCode();
        }

        // Extract service category
        if (appointment.hasServiceCategory() && !appointment.getServiceCategory().isEmpty()) {
            CodeableConcept category = appointment.getServiceCategory().get(0);
            if (category.hasCoding() && !category.getCoding().isEmpty()) {
                Coding coding = category.getCoding().get(0);
                entity.serviceCategoryCode = coding.getCode();
                entity.serviceCategoryDisplay = coding.getDisplay();
            }
        }

        // Extract service type
        if (appointment.hasServiceType() && !appointment.getServiceType().isEmpty()) {
            CodeableConcept serviceType = appointment.getServiceType().get(0);
            if (serviceType.hasCoding() && !serviceType.getCoding().isEmpty()) {
                Coding coding = serviceType.getCoding().get(0);
                entity.serviceTypeCode = coding.getCode();
                entity.serviceTypeDisplay = coding.getDisplay();
            }
        }

        // Extract specialty
        if (appointment.hasSpecialty() && !appointment.getSpecialty().isEmpty()) {
            CodeableConcept specialty = appointment.getSpecialty().get(0);
            if (specialty.hasCoding() && !specialty.getCoding().isEmpty()) {
                Coding coding = specialty.getCoding().get(0);
                entity.specialtyCode = coding.getCode();
                entity.specialtyDisplay = coding.getDisplay();
            }
        }

        // Extract appointment type
        if (appointment.hasAppointmentType()) {
            CodeableConcept appointmentType = appointment.getAppointmentType();
            if (appointmentType.hasCoding() && !appointmentType.getCoding().isEmpty()) {
                Coding coding = appointmentType.getCoding().get(0);
                entity.appointmentTypeCode = coding.getCode();
                entity.appointmentTypeDisplay = coding.getDisplay();
            }
        }

        // Extract reason
        if (appointment.hasReasonCode() && !appointment.getReasonCode().isEmpty()) {
            CodeableConcept reason = appointment.getReasonCode().get(0);
            if (reason.hasCoding() && !reason.getCoding().isEmpty()) {
                Coding coding = reason.getCoding().get(0);
                entity.reasonCode = coding.getCode();
                entity.reasonDisplay = coding.getDisplay();
            }
        }

        // Extract priority
        if (appointment.hasPriority()) {
            entity.priority = appointment.getPriority();
        }

        // Extract description
        if (appointment.hasDescription()) {
            entity.description = appointment.getDescription();
        }

        // Extract timing
        if (appointment.hasStart()) {
            entity.startTime = convertToLocalDateTime(appointment.getStart());
        }

        if (appointment.hasEnd()) {
            entity.endTime = convertToLocalDateTime(appointment.getEnd());
        }

        if (appointment.hasMinutesDuration()) {
            entity.minutesDuration = appointment.getMinutesDuration();
        }

        // Extract participants
        if (appointment.hasParticipant()) {
            for (Appointment.AppointmentParticipantComponent participant : appointment.getParticipant()) {
                if (participant.hasActor() && participant.getActor().hasReference()) {
                    String reference = participant.getActor().getReference();
                    String display = participant.getActor().getDisplay();

                    // Identify participant type by reference prefix
                    if (reference.startsWith("Patient/")) {
                        entity.patientReference = reference;
                        entity.patientDisplay = display;
                    } else if (reference.startsWith("Practitioner/")) {
                        entity.practitionerReference = reference;
                        entity.practitionerDisplay = display;
                    } else if (reference.startsWith("Location/")) {
                        entity.locationReference = reference;
                        entity.locationDisplay = display;
                    }
                }
            }
        }

        // Extract comment
        if (appointment.hasComment()) {
            entity.comment = appointment.getComment();
        }

        entity.lastUpdated = LocalDateTime.now();

        return entity;
    }

    public Appointment toFhir(AppointmentEntity entity) {
        return jsonParser.parseResource(Appointment.class, entity.fhirResource);
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}

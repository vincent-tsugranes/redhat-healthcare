package com.redhat.healthcare.appointment.service;

import ca.uhn.fhir.parser.IParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.Appointment;

import java.util.UUID;

@ApplicationScoped
public class FhirParserService {

    @Inject
    IParser jsonParser;

    public Appointment parseAppointment(String json) {
        return jsonParser.parseResource(Appointment.class, json);
    }

    public String encodeAppointment(Appointment appointment) {
        return jsonParser.encodeResourceToString(appointment);
    }

    public String generateId() {
        return UUID.randomUUID().toString();
    }
}

package com.redhat.healthcare.members.service;

import ca.uhn.fhir.parser.IParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.Patient;

import java.util.UUID;

@ApplicationScoped
public class FhirParserService {

    @Inject
    IParser jsonParser;

    public Patient parsePatient(String json) {
        return jsonParser.parseResource(Patient.class, json);
    }

    public String encodePatient(Patient patient) {
        return jsonParser.encodeResourceToString(patient);
    }

    public String generateId() {
        return UUID.randomUUID().toString();
    }
}

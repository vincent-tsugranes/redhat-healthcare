package com.redhat.healthcare.medication.service;

import ca.uhn.fhir.parser.IParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.MedicationRequest;

import java.util.UUID;

@ApplicationScoped
public class FhirParserService {

    @Inject
    IParser jsonParser;

    public MedicationRequest parseMedicationRequest(String json) {
        return jsonParser.parseResource(MedicationRequest.class, json);
    }

    public String encodeMedicationRequest(MedicationRequest medicationRequest) {
        return jsonParser.encodeResourceToString(medicationRequest);
    }

    public String generateId() {
        return UUID.randomUUID().toString();
    }
}

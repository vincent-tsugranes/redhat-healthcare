package com.redhat.healthcare.practitioner.service;

import ca.uhn.fhir.parser.IParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.Practitioner;

import java.util.UUID;

@ApplicationScoped
public class FhirParserService {

    @Inject
    IParser jsonParser;

    public Practitioner parsePractitioner(String json) {
        return jsonParser.parseResource(Practitioner.class, json);
    }

    public String encodePractitioner(Practitioner practitioner) {
        return jsonParser.encodeResourceToString(practitioner);
    }

    public String generateId() {
        return UUID.randomUUID().toString();
    }
}

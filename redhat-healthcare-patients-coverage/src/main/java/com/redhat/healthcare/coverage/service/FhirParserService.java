package com.redhat.healthcare.coverage.service;

import ca.uhn.fhir.parser.IParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.Coverage;

import java.util.UUID;

@ApplicationScoped
public class FhirParserService {

    @Inject
    IParser jsonParser;

    public Coverage parseCoverage(String json) {
        return jsonParser.parseResource(Coverage.class, json);
    }

    public String encodeCoverage(Coverage coverage) {
        return jsonParser.encodeResourceToString(coverage);
    }

    public String generateId() {
        return UUID.randomUUID().toString();
    }
}

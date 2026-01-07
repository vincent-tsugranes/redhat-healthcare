package com.redhat.healthcare.claims.service;

import ca.uhn.fhir.parser.IParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.Claim;

import java.util.UUID;

@ApplicationScoped
public class FhirParserService {

    @Inject
    IParser jsonParser;

    public Claim parseClaim(String json) {
        return jsonParser.parseResource(Claim.class, json);
    }

    public String encodeClaim(Claim claim) {
        return jsonParser.encodeResourceToString(claim);
    }

    public String generateId() {
        return UUID.randomUUID().toString();
    }
}

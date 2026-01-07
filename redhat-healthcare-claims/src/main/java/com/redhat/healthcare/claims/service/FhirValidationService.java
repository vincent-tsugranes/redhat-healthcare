package com.redhat.healthcare.claims.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import com.redhat.healthcare.claims.exception.FhirValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.Claim;

import java.util.stream.Collectors;

@ApplicationScoped
public class FhirValidationService {

    @Inject
    FhirValidator validator;

    @Inject
    FhirContext fhirContext;

    public ValidationResult validate(Claim claim) {
        return validator.validateWithResult(claim);
    }

    public void validateOrThrow(Claim claim) {
        ValidationResult result = validate(claim);
        if (!result.isSuccessful()) {
            String errorMessages = result.getMessages().stream()
                .map(SingleValidationMessage::getMessage)
                .collect(Collectors.joining(", "));
            throw new FhirValidationException("FHIR validation failed: " + errorMessages);
        }
    }
}

package com.redhat.healthcare.coverage.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import com.redhat.healthcare.coverage.exception.FhirValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hl7.fhir.r4.model.Coverage;

import java.util.stream.Collectors;

@ApplicationScoped
public class FhirValidationService {

    @Inject
    FhirValidator validator;

    @Inject
    FhirContext fhirContext;

    public ValidationResult validate(Coverage coverage) {
        return validator.validateWithResult(coverage);
    }

    public void validateOrThrow(Coverage coverage) {
        ValidationResult result = validate(coverage);
        if (!result.isSuccessful()) {
            String errorMessages = result.getMessages().stream()
                .map(SingleValidationMessage::getMessage)
                .collect(Collectors.joining(", "));
            throw new FhirValidationException("FHIR validation failed: " + errorMessages);
        }
    }
}

package com.redhat.healthcare.appointment.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class FhirConfig {

    @ConfigProperty(name = "fhir.version", defaultValue = "R4")
    String fhirVersion;

    @ConfigProperty(name = "fhir.validation.enabled", defaultValue = "true")
    boolean validationEnabled;

    @Produces
    @ApplicationScoped
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }

    @Produces
    @ApplicationScoped
    public IParser jsonParser(FhirContext fhirContext) {
        return fhirContext.newJsonParser().setPrettyPrint(true);
    }

    @Produces
    @ApplicationScoped
    public FhirValidator fhirValidator(FhirContext fhirContext) {
        return fhirContext.newValidator();
    }
}

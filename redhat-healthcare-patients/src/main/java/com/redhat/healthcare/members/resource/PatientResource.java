package com.redhat.healthcare.members.resource;

import ca.uhn.fhir.parser.IParser;
import com.redhat.healthcare.members.dto.PatientSearchCriteria;
import com.redhat.healthcare.members.service.PatientService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Path("/fhir/Patient")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient", description = "FHIR Patient Resource Operations")
public class PatientResource {

    @Inject
    PatientService patientService;

    @Inject
    IParser jsonParser;

    @POST
    @Operation(summary = "Create a new Patient resource")
    public Response create(String patientJson) {
        Patient patient = jsonParser.parseResource(Patient.class, patientJson);
        Patient created = patientService.createPatient(patient);

        String location = "/fhir/Patient/" + created.getIdElement().getIdPart();

        return Response
            .created(URI.create(location))
            .entity(jsonParser.encodeResourceToString(created))
            .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Read a Patient resource by ID")
    public Response read(@PathParam("id") String id) {
        Patient patient = patientService.getPatient(id);
        return Response.ok(jsonParser.encodeResourceToString(patient)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a Patient resource")
    public Response update(@PathParam("id") String id, String patientJson) {
        Patient patient = jsonParser.parseResource(Patient.class, patientJson);
        Patient updated = patientService.updatePatient(id, patient);

        return Response.ok(jsonParser.encodeResourceToString(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a Patient resource (soft delete)")
    public Response delete(@PathParam("id") String id) {
        patientService.deletePatient(id);
        return Response.noContent().build();
    }

    @GET
    @Operation(summary = "Search for Patient resources",
               description = "Search using FHIR parameters: identifier, family, given, birthdate")
    public Response search(
        @QueryParam("identifier") String identifier,
        @QueryParam("family") String family,
        @QueryParam("given") String given,
        @QueryParam("birthdate") String birthdate
    ) {
        PatientSearchCriteria criteria = new PatientSearchCriteria();
        criteria.setIdentifier(identifier);
        criteria.setFamily(family);
        criteria.setGiven(given);
        if (birthdate != null && !birthdate.isEmpty()) {
            criteria.setBirthDate(LocalDate.parse(birthdate));
        }

        List<Patient> patients = patientService.searchPatients(criteria);

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(patients.size());

        patients.forEach(patient -> {
            Bundle.BundleEntryComponent entry = bundle.addEntry();
            entry.setResource(patient);
            entry.setFullUrl("/fhir/Patient/" + patient.getIdElement().getIdPart());
        });

        return Response.ok(jsonParser.encodeResourceToString(bundle)).build();
    }
}

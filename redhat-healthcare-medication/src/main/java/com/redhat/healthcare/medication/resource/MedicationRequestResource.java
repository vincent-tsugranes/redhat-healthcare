package com.redhat.healthcare.medication.resource;

import ca.uhn.fhir.parser.IParser;
import com.redhat.healthcare.medication.dto.MedicationRequestSearchCriteria;
import com.redhat.healthcare.medication.service.MedicationRequestService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;

import java.net.URI;
import java.util.List;

@Path("/fhir/MedicationRequest")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "MedicationRequest", description = "FHIR MedicationRequest Resource Operations")
public class MedicationRequestResource {

    @Inject
    MedicationRequestService medicationRequestService;

    @Inject
    IParser jsonParser;

    @POST
    @Operation(summary = "Create a new MedicationRequest resource")
    public Response create(String medicationRequestJson) {
        MedicationRequest medicationRequest = jsonParser.parseResource(MedicationRequest.class, medicationRequestJson);
        MedicationRequest created = medicationRequestService.createMedicationRequest(medicationRequest);

        String location = "/fhir/MedicationRequest/" + created.getIdElement().getIdPart();

        return Response
            .created(URI.create(location))
            .entity(jsonParser.encodeResourceToString(created))
            .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Read a MedicationRequest resource by ID")
    public Response read(@PathParam("id") String id) {
        MedicationRequest medicationRequest = medicationRequestService.getMedicationRequest(id);
        return Response.ok(jsonParser.encodeResourceToString(medicationRequest)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a MedicationRequest resource")
    public Response update(@PathParam("id") String id, String medicationRequestJson) {
        MedicationRequest medicationRequest = jsonParser.parseResource(MedicationRequest.class, medicationRequestJson);
        MedicationRequest updated = medicationRequestService.updateMedicationRequest(id, medicationRequest);

        return Response.ok(jsonParser.encodeResourceToString(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a MedicationRequest resource (soft delete)")
    public Response delete(@PathParam("id") String id) {
        medicationRequestService.deleteMedicationRequest(id);
        return Response.noContent().build();
    }

    @GET
    @Operation(summary = "Search for MedicationRequest resources",
               description = "Search using FHIR parameters: patient, requester, status, medication")
    public Response search(
        @QueryParam("patient") String patient,
        @QueryParam("requester") String requester,
        @QueryParam("status") String status,
        @QueryParam("medication") String medication
    ) {
        MedicationRequestSearchCriteria criteria = new MedicationRequestSearchCriteria();
        criteria.setPatient(patient);
        criteria.setRequester(requester);
        criteria.setStatus(status);
        criteria.setMedication(medication);

        List<MedicationRequest> medicationRequests = medicationRequestService.searchMedicationRequests(criteria);

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(medicationRequests.size());

        medicationRequests.forEach(medicationRequest -> {
            Bundle.BundleEntryComponent entry = bundle.addEntry();
            entry.setResource(medicationRequest);
            entry.setFullUrl("/fhir/MedicationRequest/" + medicationRequest.getIdElement().getIdPart());
        });

        return Response.ok(jsonParser.encodeResourceToString(bundle)).build();
    }
}

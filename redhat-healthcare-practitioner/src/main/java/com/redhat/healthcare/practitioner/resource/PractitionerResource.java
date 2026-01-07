package com.redhat.healthcare.practitioner.resource;

import ca.uhn.fhir.parser.IParser;
import com.redhat.healthcare.practitioner.dto.PractitionerSearchCriteria;
import com.redhat.healthcare.practitioner.service.PractitionerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Practitioner;

import java.net.URI;
import java.util.List;

@Path("/fhir/Practitioner")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Practitioner", description = "FHIR Practitioner Resource Operations")
public class PractitionerResource {

    @Inject
    PractitionerService practitionerService;

    @Inject
    IParser jsonParser;

    @POST
    @Operation(summary = "Create a new Practitioner resource")
    public Response create(String practitionerJson) {
        Practitioner practitioner = jsonParser.parseResource(Practitioner.class, practitionerJson);
        Practitioner created = practitionerService.createPractitioner(practitioner);

        String location = "/fhir/Practitioner/" + created.getIdElement().getIdPart();

        return Response
            .created(URI.create(location))
            .entity(jsonParser.encodeResourceToString(created))
            .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Read a Practitioner resource by ID")
    public Response read(@PathParam("id") String id) {
        Practitioner practitioner = practitionerService.getPractitioner(id);
        return Response.ok(jsonParser.encodeResourceToString(practitioner)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a Practitioner resource")
    public Response update(@PathParam("id") String id, String practitionerJson) {
        Practitioner practitioner = jsonParser.parseResource(Practitioner.class, practitionerJson);
        Practitioner updated = practitionerService.updatePractitioner(id, practitioner);

        return Response.ok(jsonParser.encodeResourceToString(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a Practitioner resource (soft delete)")
    public Response delete(@PathParam("id") String id) {
        practitionerService.deletePractitioner(id);
        return Response.noContent().build();
    }

    @GET
    @Operation(summary = "Search for Practitioner resources",
               description = "Search using FHIR parameters: identifier, npi, name, family, given, email, specialty")
    public Response search(
        @QueryParam("identifier") String identifier,
        @QueryParam("npi") String npi,
        @QueryParam("name") String name,
        @QueryParam("family") String family,
        @QueryParam("given") String given,
        @QueryParam("email") String email,
        @QueryParam("specialty") String specialty
    ) {
        PractitionerSearchCriteria criteria = new PractitionerSearchCriteria();
        criteria.setIdentifier(identifier);
        criteria.setNpi(npi);
        criteria.setName(name);
        criteria.setFamily(family);
        criteria.setGiven(given);
        criteria.setEmail(email);
        criteria.setSpecialty(specialty);

        List<Practitioner> practitioners = practitionerService.searchPractitioners(criteria);

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(practitioners.size());

        practitioners.forEach(practitioner -> {
            Bundle.BundleEntryComponent entry = bundle.addEntry();
            entry.setResource(practitioner);
            entry.setFullUrl("/fhir/Practitioner/" + practitioner.getIdElement().getIdPart());
        });

        return Response.ok(jsonParser.encodeResourceToString(bundle)).build();
    }
}

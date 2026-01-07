package com.redhat.healthcare.claims.resource;

import ca.uhn.fhir.parser.IParser;
import com.redhat.healthcare.claims.dto.ClaimSearchCriteria;
import com.redhat.healthcare.claims.service.ClaimService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Claim;

import java.net.URI;
import java.util.List;

@Path("/fhir/Claim")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Claim", description = "FHIR Claim Resource Operations")
public class ClaimResource {

    @Inject
    ClaimService claimService;

    @Inject
    IParser jsonParser;

    @POST
    @Operation(summary = "Create a new Claim resource")
    public Response create(String claimJson) {
        Claim claim = jsonParser.parseResource(Claim.class, claimJson);
        Claim created = claimService.createClaim(claim);

        String location = "/fhir/Claim/" + created.getIdElement().getIdPart();

        return Response
            .created(URI.create(location))
            .entity(jsonParser.encodeResourceToString(created))
            .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Read a Claim resource by ID")
    public Response read(@PathParam("id") String id) {
        Claim claim = claimService.getClaim(id);
        return Response.ok(jsonParser.encodeResourceToString(claim)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a Claim resource")
    public Response update(@PathParam("id") String id, String claimJson) {
        Claim claim = jsonParser.parseResource(Claim.class, claimJson);
        Claim updated = claimService.updateClaim(id, claim);

        return Response.ok(jsonParser.encodeResourceToString(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a Claim resource (soft delete)")
    public Response delete(@PathParam("id") String id) {
        claimService.deleteClaim(id);
        return Response.noContent().build();
    }

    @GET
    @Operation(summary = "Search for Claim resources",
               description = "Search using FHIR parameters: identifier, patient, provider, insurer, status, use")
    public Response search(
        @QueryParam("identifier") String identifier,
        @QueryParam("patient") String patient,
        @QueryParam("provider") String provider,
        @QueryParam("insurer") String insurer,
        @QueryParam("status") String status,
        @QueryParam("use") String use
    ) {
        ClaimSearchCriteria criteria = new ClaimSearchCriteria();
        criteria.setIdentifier(identifier);
        criteria.setPatient(patient);
        criteria.setProvider(provider);
        criteria.setInsurer(insurer);
        criteria.setStatus(status);
        criteria.setUse(use);

        List<Claim> claims = claimService.searchClaims(criteria);

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(claims.size());

        claims.forEach(claim -> {
            Bundle.BundleEntryComponent entry = bundle.addEntry();
            entry.setResource(claim);
            entry.setFullUrl("/fhir/Claim/" + claim.getIdElement().getIdPart());
        });

        return Response.ok(jsonParser.encodeResourceToString(bundle)).build();
    }
}

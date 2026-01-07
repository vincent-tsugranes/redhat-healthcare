package com.redhat.healthcare.coverage.resource;

import ca.uhn.fhir.parser.IParser;
import com.redhat.healthcare.coverage.dto.CoverageSearchCriteria;
import com.redhat.healthcare.coverage.service.CoverageService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;

import java.net.URI;
import java.util.List;

@Path("/fhir/Coverage")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Coverage", description = "FHIR Coverage Resource Operations")
public class CoverageResource {

    @Inject
    CoverageService coverageService;

    @Inject
    IParser jsonParser;

    @POST
    @Operation(summary = "Create a new Coverage resource")
    public Response create(String coverageJson) {
        Coverage coverage = jsonParser.parseResource(Coverage.class, coverageJson);
        Coverage created = coverageService.createCoverage(coverage);

        String location = "/fhir/Coverage/" + created.getIdElement().getIdPart();

        return Response
            .created(URI.create(location))
            .entity(jsonParser.encodeResourceToString(created))
            .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Read a Coverage resource by ID")
    public Response read(@PathParam("id") String id) {
        Coverage coverage = coverageService.getCoverage(id);
        return Response.ok(jsonParser.encodeResourceToString(coverage)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a Coverage resource")
    public Response update(@PathParam("id") String id, String coverageJson) {
        Coverage coverage = jsonParser.parseResource(Coverage.class, coverageJson);
        Coverage updated = coverageService.updateCoverage(id, coverage);

        return Response.ok(jsonParser.encodeResourceToString(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a Coverage resource (soft delete)")
    public Response delete(@PathParam("id") String id) {
        coverageService.deleteCoverage(id);
        return Response.noContent().build();
    }

    @GET
    @Operation(summary = "Search for Coverage resources",
               description = "Search using FHIR parameters: identifier, patient, beneficiary, subscriber, payor, status")
    public Response search(
        @QueryParam("identifier") String identifier,
        @QueryParam("patient") String patient,
        @QueryParam("beneficiary") String beneficiary,
        @QueryParam("subscriber") String subscriber,
        @QueryParam("payor") String payor,
        @QueryParam("status") String status
    ) {
        CoverageSearchCriteria criteria = new CoverageSearchCriteria();
        criteria.setIdentifier(identifier);
        // In FHIR, 'patient' is a synonym for 'beneficiary' in Coverage searches
        criteria.setBeneficiary(beneficiary != null ? beneficiary : patient);
        criteria.setSubscriber(subscriber);
        criteria.setPayor(payor);
        criteria.setStatus(status);

        List<Coverage> coverageList = coverageService.searchCoverage(criteria);

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(coverageList.size());

        coverageList.forEach(coverage -> {
            Bundle.BundleEntryComponent entry = bundle.addEntry();
            entry.setResource(coverage);
            entry.setFullUrl("/fhir/Coverage/" + coverage.getIdElement().getIdPart());
        });

        return Response.ok(jsonParser.encodeResourceToString(bundle)).build();
    }
}

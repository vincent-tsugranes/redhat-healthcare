package com.redhat.healthcare.appointment.resource;

import ca.uhn.fhir.parser.IParser;
import com.redhat.healthcare.appointment.dto.AppointmentSearchCriteria;
import com.redhat.healthcare.appointment.service.AppointmentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Bundle;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Path("/fhir/Appointment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Appointment", description = "FHIR Appointment Resource Operations")
public class AppointmentResource {

    @Inject
    AppointmentService appointmentService;

    @Inject
    IParser jsonParser;

    @POST
    @Operation(summary = "Create a new Appointment resource")
    public Response create(String appointmentJson) {
        Appointment appointment = jsonParser.parseResource(Appointment.class, appointmentJson);
        Appointment created = appointmentService.createAppointment(appointment);

        String location = "/fhir/Appointment/" + created.getIdElement().getIdPart();

        return Response
            .created(URI.create(location))
            .entity(jsonParser.encodeResourceToString(created))
            .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Read an Appointment resource by ID")
    public Response read(@PathParam("id") String id) {
        Appointment appointment = appointmentService.getAppointment(id);
        return Response.ok(jsonParser.encodeResourceToString(appointment)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update an Appointment resource")
    public Response update(@PathParam("id") String id, String appointmentJson) {
        Appointment appointment = jsonParser.parseResource(Appointment.class, appointmentJson);
        Appointment updated = appointmentService.updateAppointment(id, appointment);

        return Response.ok(jsonParser.encodeResourceToString(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete an Appointment resource (soft delete)")
    public Response delete(@PathParam("id") String id) {
        appointmentService.deleteAppointment(id);
        return Response.noContent().build();
    }

    @GET
    @Operation(summary = "Search for Appointment resources",
               description = "Search using FHIR parameters: identifier, patient, practitioner, status, date, specialty")
    public Response search(
        @QueryParam("identifier") String identifier,
        @QueryParam("patient") String patient,
        @QueryParam("practitioner") String practitioner,
        @QueryParam("status") String status,
        @QueryParam("date") String date,
        @QueryParam("specialty") String specialty
    ) {
        AppointmentSearchCriteria criteria = new AppointmentSearchCriteria();
        criteria.setIdentifier(identifier);
        criteria.setPatient(patient);
        criteria.setPractitioner(practitioner);
        criteria.setStatus(status);
        criteria.setSpecialty(specialty);

        // Parse date parameter (FHIR date format)
        if (date != null && !date.isEmpty()) {
            parseDateParameter(date, criteria);
        }

        List<Appointment> appointments = appointmentService.searchAppointments(criteria);

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(appointments.size());

        appointments.forEach(appointment -> {
            Bundle.BundleEntryComponent entry = bundle.addEntry();
            entry.setResource(appointment);
            entry.setFullUrl("/fhir/Appointment/" + appointment.getIdElement().getIdPart());
        });

        return Response.ok(jsonParser.encodeResourceToString(bundle)).build();
    }

    private void parseDateParameter(String date, AppointmentSearchCriteria criteria) {
        try {
            // Handle date range formats (e.g., "ge2024-01-01", "le2024-12-31")
            if (date.startsWith("ge")) {
                String dateStr = date.substring(2);
                criteria.setDateStart(parseDateTime(dateStr));
            } else if (date.startsWith("le")) {
                String dateStr = date.substring(2);
                criteria.setDateEnd(parseDateTime(dateStr));
            } else if (date.startsWith("gt")) {
                String dateStr = date.substring(2);
                criteria.setDateStart(parseDateTime(dateStr).plusDays(1));
            } else if (date.startsWith("lt")) {
                String dateStr = date.substring(2);
                criteria.setDateEnd(parseDateTime(dateStr).minusDays(1));
            } else {
                // Exact date match - set both start and end
                LocalDateTime dateTime = parseDateTime(date);
                criteria.setDateStart(dateTime.withHour(0).withMinute(0).withSecond(0));
                criteria.setDateEnd(dateTime.withHour(23).withMinute(59).withSecond(59));
            }
        } catch (Exception e) {
            // If parsing fails, ignore the date parameter
        }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        // Try various date formats
        try {
            if (dateStr.contains("T")) {
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
            } else {
                return LocalDateTime.parse(dateStr + "T00:00:00", DateTimeFormatter.ISO_DATE_TIME);
            }
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}

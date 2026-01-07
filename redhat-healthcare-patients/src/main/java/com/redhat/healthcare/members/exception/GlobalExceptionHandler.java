package com.redhat.healthcare.members.exception;

import ca.uhn.fhir.parser.IParser;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hl7.fhir.r4.model.OperationOutcome;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Inject
    IParser jsonParser;

    @Override
    public Response toResponse(Exception exception) {
        OperationOutcome outcome = new OperationOutcome();

        if (exception instanceof ResourceNotFoundException) {
            return notFound(outcome, exception.getMessage());
        } else if (exception instanceof FhirValidationException) {
            return badRequest(outcome, exception.getMessage());
        } else {
            return serverError(outcome, exception.getMessage());
        }
    }

    private Response notFound(OperationOutcome outcome, String message) {
        outcome.addIssue()
            .setSeverity(OperationOutcome.IssueSeverity.ERROR)
            .setCode(OperationOutcome.IssueType.NOTFOUND)
            .setDiagnostics(message);

        return Response.status(Response.Status.NOT_FOUND)
            .entity(jsonParser.encodeResourceToString(outcome))
            .build();
    }

    private Response badRequest(OperationOutcome outcome, String message) {
        outcome.addIssue()
            .setSeverity(OperationOutcome.IssueSeverity.ERROR)
            .setCode(OperationOutcome.IssueType.INVALID)
            .setDiagnostics(message);

        return Response.status(Response.Status.BAD_REQUEST)
            .entity(jsonParser.encodeResourceToString(outcome))
            .build();
    }

    private Response serverError(OperationOutcome outcome, String message) {
        outcome.addIssue()
            .setSeverity(OperationOutcome.IssueSeverity.ERROR)
            .setCode(OperationOutcome.IssueType.EXCEPTION)
            .setDiagnostics(message);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(jsonParser.encodeResourceToString(outcome))
            .build();
    }
}

package com.redhat.healthcare.appointment.service;

import com.redhat.healthcare.appointment.domain.entity.AppointmentEntity;
import com.redhat.healthcare.appointment.domain.repository.AppointmentRepository;
import com.redhat.healthcare.appointment.dto.AppointmentSearchCriteria;
import com.redhat.healthcare.appointment.exception.ResourceNotFoundException;
import com.redhat.healthcare.appointment.mapper.AppointmentMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hl7.fhir.r4.model.Appointment;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AppointmentService {

    @Inject
    AppointmentRepository repository;

    @Inject
    FhirParserService parserService;

    @Inject
    FhirValidationService validationService;

    @Inject
    AppointmentMapper mapper;

    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        validationService.validateOrThrow(appointment);

        if (appointment.getId() == null || appointment.getId().isEmpty()) {
            appointment.setId(parserService.generateId());
        }

        if (appointment.getMeta() == null) {
            appointment.setMeta(new org.hl7.fhir.r4.model.Meta());
        }
        appointment.getMeta().setVersionId("1");
        appointment.getMeta().setLastUpdated(new Date());

        AppointmentEntity entity = mapper.toEntity(appointment);
        entity.versionId = 1L;
        entity.createdAt = LocalDateTime.now();
        entity.lastUpdated = LocalDateTime.now();

        repository.persist(entity);

        return appointment;
    }

    @Transactional
    public Appointment updateAppointment(String id, Appointment appointment) {
        AppointmentEntity existing = repository.findByFhirId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment/" + id));

        validationService.validateOrThrow(appointment);

        appointment.setId(id);

        Long newVersion = existing.versionId + 1;
        if (appointment.getMeta() == null) {
            appointment.setMeta(new org.hl7.fhir.r4.model.Meta());
        }
        appointment.getMeta().setVersionId(newVersion.toString());
        appointment.getMeta().setLastUpdated(new Date());

        AppointmentEntity updated = mapper.toEntity(appointment);
        updated.versionId = newVersion;
        updated.lastUpdated = LocalDateTime.now();
        updated.createdAt = existing.createdAt;

        repository.persist(updated);

        return appointment;
    }

    public Appointment getAppointment(String id) {
        AppointmentEntity entity = repository.findByFhirId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment/" + id));

        return mapper.toFhir(entity);
    }

    @Transactional
    public void deleteAppointment(String id) {
        if (!repository.findByFhirId(id).isPresent()) {
            throw new ResourceNotFoundException("Appointment/" + id);
        }
        repository.softDelete(id);
    }

    public List<Appointment> searchAppointments(AppointmentSearchCriteria criteria) {
        List<AppointmentEntity> entities;

        if (criteria.getIdentifier() != null && !criteria.getIdentifier().isEmpty()) {
            entities = repository.findByIdentifier(null, criteria.getIdentifier());
        } else if (criteria.getPatient() != null && !criteria.getPatient().isEmpty()) {
            if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
                entities = repository.findByPatientAndStatus(criteria.getPatient(), criteria.getStatus());
            } else {
                entities = repository.findByPatient(criteria.getPatient());
            }
        } else if (criteria.getPractitioner() != null && !criteria.getPractitioner().isEmpty()) {
            entities = repository.findByPractitioner(criteria.getPractitioner());
        } else if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
            entities = repository.findByStatus(criteria.getStatus());
        } else if (criteria.getDateStart() != null || criteria.getDateEnd() != null) {
            entities = repository.findByDateRange(criteria.getDateStart(), criteria.getDateEnd());
        } else if (criteria.getSpecialty() != null && !criteria.getSpecialty().isEmpty()) {
            entities = repository.findBySpecialty(criteria.getSpecialty());
        } else {
            entities = repository.findAllActive();
        }

        return entities.stream()
            .map(mapper::toFhir)
            .collect(Collectors.toList());
    }
}

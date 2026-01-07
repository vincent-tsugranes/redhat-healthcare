package com.redhat.healthcare.appointment.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_appointment_identifier", columnList = "identifier_value"),
    @Index(name = "idx_appointment_status", columnList = "status"),
    @Index(name = "idx_appointment_patient", columnList = "patient_reference"),
    @Index(name = "idx_appointment_practitioner", columnList = "practitioner_reference"),
    @Index(name = "idx_appointment_start_time", columnList = "start_time"),
    @Index(name = "idx_appointment_active", columnList = "active"),
    @Index(name = "idx_appointment_last_updated", columnList = "last_updated"),
    @Index(name = "idx_appointment_specialty", columnList = "specialty_code")
})
public class AppointmentEntity extends PanacheEntityBase {

    @Id
    @Column(name = "fhir_id", length = 64)
    public String fhirId;

    @Column(name = "version_id")
    public Long versionId;

    @Column(name = "identifier_system", length = 255)
    public String identifierSystem;

    @Column(name = "identifier_value", length = 255)
    public String identifierValue;

    @Column(name = "status", length = 50, nullable = false)
    public String status;

    @Column(name = "service_category_code", length = 100)
    public String serviceCategoryCode;

    @Column(name = "service_category_display", length = 255)
    public String serviceCategoryDisplay;

    @Column(name = "service_type_code", length = 100)
    public String serviceTypeCode;

    @Column(name = "service_type_display", length = 255)
    public String serviceTypeDisplay;

    @Column(name = "specialty_code", length = 100)
    public String specialtyCode;

    @Column(name = "specialty_display", length = 255)
    public String specialtyDisplay;

    @Column(name = "appointment_type_code", length = 100)
    public String appointmentTypeCode;

    @Column(name = "appointment_type_display", length = 255)
    public String appointmentTypeDisplay;

    @Column(name = "reason_code", length = 100)
    public String reasonCode;

    @Column(name = "reason_display", length = 255)
    public String reasonDisplay;

    @Column(name = "priority")
    public Integer priority;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Column(name = "start_time")
    public LocalDateTime startTime;

    @Column(name = "end_time")
    public LocalDateTime endTime;

    @Column(name = "minutes_duration")
    public Integer minutesDuration;

    @Column(name = "patient_reference", length = 255)
    public String patientReference;

    @Column(name = "patient_display", length = 255)
    public String patientDisplay;

    @Column(name = "practitioner_reference", length = 255)
    public String practitionerReference;

    @Column(name = "practitioner_display", length = 255)
    public String practitionerDisplay;

    @Column(name = "location_reference", length = 255)
    public String locationReference;

    @Column(name = "location_display", length = 255)
    public String locationDisplay;

    @Column(name = "comment", columnDefinition = "TEXT")
    public String comment;

    @Column(name = "active")
    public Boolean active;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fhir_resource", columnDefinition = "jsonb")
    public String fhirResource;

    @Column(name = "last_updated")
    public LocalDateTime lastUpdated;

    @Column(name = "created_at")
    public LocalDateTime createdAt;
}

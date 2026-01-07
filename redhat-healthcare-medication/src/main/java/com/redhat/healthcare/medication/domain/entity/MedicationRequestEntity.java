package com.redhat.healthcare.medication.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "medication_requests", indexes = {
    @Index(name = "idx_medreq_patient", columnList = "patient_reference"),
    @Index(name = "idx_medreq_requester", columnList = "requester_reference"),
    @Index(name = "idx_medreq_status", columnList = "status"),
    @Index(name = "idx_medreq_medication", columnList = "medication_code"),
    @Index(name = "idx_medreq_active", columnList = "active"),
    @Index(name = "idx_medreq_authored", columnList = "authored_on")
})
public class MedicationRequestEntity extends PanacheEntityBase {

    @Id
    @Column(name = "fhir_id", length = 64)
    public String fhirId;

    @Column(name = "version_id")
    public Long versionId;

    @Column(name = "patient_reference", length = 255)
    public String patientReference;

    @Column(name = "patient_display", length = 255)
    public String patientDisplay;

    @Column(name = "requester_reference", length = 255)
    public String requesterReference;

    @Column(name = "requester_display", length = 255)
    public String requesterDisplay;

    @Column(name = "medication_code", length = 100)
    public String medicationCode;

    @Column(name = "medication_display", length = 500)
    public String medicationDisplay;

    @Column(name = "medication_system", length = 255)
    public String medicationSystem;

    @Column(name = "status", length = 20)
    public String status;

    @Column(name = "intent", length = 20)
    public String intent;

    @Column(name = "authored_on")
    public LocalDateTime authoredOn;

    @Column(name = "dosage_text", length = 1000)
    public String dosageText;

    @Column(name = "quantity_value", precision = 10, scale = 2)
    public BigDecimal quantityValue;

    @Column(name = "quantity_unit", length = 50)
    public String quantityUnit;

    @Column(name = "refills_allowed")
    public Integer refillsAllowed;

    @Column(name = "supply_duration_value", precision = 10, scale = 2)
    public BigDecimal supplyDurationValue;

    @Column(name = "supply_duration_unit", length = 50)
    public String supplyDurationUnit;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fhir_resource", columnDefinition = "jsonb")
    public String fhirResource;

    @Column(name = "active")
    public Boolean active;

    @Column(name = "last_updated")
    public LocalDateTime lastUpdated;

    @Column(name = "created_at")
    public LocalDateTime createdAt;
}

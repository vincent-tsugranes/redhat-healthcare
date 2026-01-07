package com.redhat.healthcare.claims.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims", indexes = {
    @Index(name = "idx_claim_identifier", columnList = "identifier_value"),
    @Index(name = "idx_claim_patient", columnList = "patient_reference"),
    @Index(name = "idx_claim_provider", columnList = "provider_reference"),
    @Index(name = "idx_claim_insurer", columnList = "insurer_reference"),
    @Index(name = "idx_claim_status", columnList = "status"),
    @Index(name = "idx_claim_created", columnList = "created_date"),
    @Index(name = "idx_claim_billable_period", columnList = "billable_period_start, billable_period_end"),
    @Index(name = "idx_claim_active", columnList = "active"),
    @Index(name = "idx_claim_last_updated", columnList = "last_updated")
})
public class ClaimEntity extends PanacheEntityBase {

    @Id
    @Column(name = "fhir_id", length = 64)
    public String fhirId;

    @Column(name = "version_id")
    public Long versionId;

    @Column(name = "identifier_system", length = 255)
    public String identifierSystem;

    @Column(name = "identifier_value", length = 255)
    public String identifierValue;

    @Column(name = "status", length = 30)
    public String status;

    @Column(name = "claim_type", length = 50)
    public String claimType;

    @Column(name = "claim_use", length = 20)
    public String claimUse;

    @Column(name = "patient_reference", length = 255)
    public String patientReference;

    @Column(name = "patient_display", length = 255)
    public String patientDisplay;

    @Column(name = "provider_reference", length = 255)
    public String providerReference;

    @Column(name = "provider_display", length = 255)
    public String providerDisplay;

    @Column(name = "insurer_reference", length = 255)
    public String insurerReference;

    @Column(name = "insurer_display", length = 255)
    public String insurerDisplay;

    @Column(name = "priority_code", length = 50)
    public String priorityCode;

    @Column(name = "priority_display", length = 100)
    public String priorityDisplay;

    @Column(name = "total_value", precision = 12, scale = 2)
    public BigDecimal totalValue;

    @Column(name = "total_currency", length = 10)
    public String totalCurrency;

    @Column(name = "created_date")
    public LocalDateTime createdDate;

    @Column(name = "billable_period_start")
    public LocalDate billablePeriodStart;

    @Column(name = "billable_period_end")
    public LocalDate billablePeriodEnd;

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

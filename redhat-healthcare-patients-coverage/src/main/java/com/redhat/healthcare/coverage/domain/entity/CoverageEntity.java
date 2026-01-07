package com.redhat.healthcare.coverage.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coverage", indexes = {
    @Index(name = "idx_coverage_identifier", columnList = "identifier_value"),
    @Index(name = "idx_coverage_beneficiary", columnList = "beneficiary_reference"),
    @Index(name = "idx_coverage_subscriber", columnList = "subscriber_reference"),
    @Index(name = "idx_coverage_payor", columnList = "payor_reference"),
    @Index(name = "idx_coverage_status", columnList = "status"),
    @Index(name = "idx_coverage_period", columnList = "period_start, period_end"),
    @Index(name = "idx_coverage_active", columnList = "active"),
    @Index(name = "idx_coverage_last_updated", columnList = "last_updated")
})
public class CoverageEntity extends PanacheEntityBase {

    @Id
    @Column(name = "fhir_id", length = 64)
    public String fhirId;

    @Column(name = "version_id")
    public Long versionId;

    @Column(name = "identifier_system", length = 255)
    public String identifierSystem;

    @Column(name = "identifier_value", length = 255)
    public String identifierValue;

    @Column(name = "subscriber_reference", length = 255)
    public String subscriberReference;

    @Column(name = "beneficiary_reference", length = 255)
    public String beneficiaryReference;

    @Column(name = "status", length = 20)
    public String status;

    @Column(name = "type_code", length = 100)
    public String typeCode;

    @Column(name = "type_display", length = 255)
    public String typeDisplay;

    @Column(name = "payor_reference", length = 255)
    public String payorReference;

    @Column(name = "payor_display", length = 255)
    public String payorDisplay;

    @Column(name = "period_start")
    public LocalDate periodStart;

    @Column(name = "period_end")
    public LocalDate periodEnd;

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

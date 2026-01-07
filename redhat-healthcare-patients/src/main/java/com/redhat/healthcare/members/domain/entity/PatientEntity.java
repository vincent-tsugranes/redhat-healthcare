package com.redhat.healthcare.members.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients", indexes = {
    @Index(name = "idx_patient_identifier", columnList = "identifier_value"),
    @Index(name = "idx_patient_family_name", columnList = "family_name"),
    @Index(name = "idx_patient_birth_date", columnList = "birth_date"),
    @Index(name = "idx_patient_active", columnList = "active"),
    @Index(name = "idx_patient_last_updated", columnList = "last_updated")
})
public class PatientEntity extends PanacheEntityBase {

    @Id
    @Column(name = "fhir_id", length = 64)
    public String fhirId;

    @Column(name = "version_id")
    public Long versionId;

    @Column(name = "identifier_system", length = 255)
    public String identifierSystem;

    @Column(name = "identifier_value", length = 255)
    public String identifierValue;

    @Column(name = "family_name", length = 255)
    public String familyName;

    @Column(name = "given_name", length = 255)
    public String givenName;

    @Column(name = "birth_date")
    public LocalDate birthDate;

    @Column(name = "gender", length = 20)
    public String gender;

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

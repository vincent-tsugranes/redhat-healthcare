package com.redhat.healthcare.practitioner.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "practitioners", indexes = {
    @Index(name = "idx_practitioner_identifier", columnList = "identifier_value"),
    @Index(name = "idx_practitioner_npi", columnList = "npi"),
    @Index(name = "idx_practitioner_family_name", columnList = "family_name"),
    @Index(name = "idx_practitioner_given_name", columnList = "given_name"),
    @Index(name = "idx_practitioner_full_name", columnList = "full_name"),
    @Index(name = "idx_practitioner_email", columnList = "email"),
    @Index(name = "idx_practitioner_specialty", columnList = "specialty_code"),
    @Index(name = "idx_practitioner_active", columnList = "active"),
    @Index(name = "idx_practitioner_last_updated", columnList = "last_updated")
})
public class PractitionerEntity extends PanacheEntityBase {

    @Id
    @Column(name = "fhir_id", length = 64)
    public String fhirId;

    @Column(name = "version_id")
    public Long versionId;

    @Column(name = "identifier_system", length = 255)
    public String identifierSystem;

    @Column(name = "identifier_value", length = 255)
    public String identifierValue;

    @Column(name = "npi", length = 20)
    public String npi;

    @Column(name = "family_name", length = 255)
    public String familyName;

    @Column(name = "given_name", length = 255)
    public String givenName;

    @Column(name = "full_name", length = 500)
    public String fullName;

    @Column(name = "prefix", length = 50)
    public String prefix;

    @Column(name = "suffix", length = 50)
    public String suffix;

    @Column(name = "active")
    public Boolean active;

    @Column(name = "phone", length = 50)
    public String phone;

    @Column(name = "email", length = 255)
    public String email;

    @Column(name = "gender", length = 20)
    public String gender;

    @Column(name = "birth_date")
    public LocalDate birthDate;

    @Column(name = "specialty_code", length = 100)
    public String specialtyCode;

    @Column(name = "specialty_display", length = 255)
    public String specialtyDisplay;

    @Column(name = "specialty_system", length = 255)
    public String specialtySystem;

    @Column(name = "address_line", length = 500)
    public String addressLine;

    @Column(name = "city", length = 100)
    public String city;

    @Column(name = "state", length = 100)
    public String state;

    @Column(name = "postal_code", length = 20)
    public String postalCode;

    @Column(name = "country", length = 100)
    public String country;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fhir_resource", columnDefinition = "jsonb")
    public String fhirResource;

    @Column(name = "last_updated")
    public LocalDateTime lastUpdated;

    @Column(name = "created_at")
    public LocalDateTime createdAt;
}

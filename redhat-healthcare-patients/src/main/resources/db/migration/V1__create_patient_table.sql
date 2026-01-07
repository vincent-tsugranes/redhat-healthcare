-- Create patients table for FHIR R4 Patient resources
CREATE TABLE patients (
    fhir_id VARCHAR(64) PRIMARY KEY,
    version_id BIGINT NOT NULL DEFAULT 1,

    -- Searchable fields extracted from FHIR resource
    identifier_system VARCHAR(255),
    identifier_value VARCHAR(255),
    family_name VARCHAR(255),
    given_name VARCHAR(255),
    birth_date DATE,
    gender VARCHAR(20),

    -- Full FHIR resource as JSON
    fhir_resource JSONB NOT NULL,

    -- Metadata
    active BOOLEAN DEFAULT true,
    last_updated TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,

    -- Constraints
    CONSTRAINT ck_gender CHECK (gender IN ('male', 'female', 'other', 'unknown'))
);

-- Indexes for FHIR search parameters
CREATE INDEX idx_patient_identifier ON patients(identifier_value);
CREATE INDEX idx_patient_family_name ON patients(family_name);
CREATE INDEX idx_patient_birth_date ON patients(birth_date);
CREATE INDEX idx_patient_active ON patients(active);
CREATE INDEX idx_patient_last_updated ON patients(last_updated);

-- GIN index for JSONB queries (advanced searching)
CREATE INDEX idx_patient_fhir_resource ON patients USING GIN(fhir_resource);

-- Comments
COMMENT ON TABLE patients IS 'FHIR R4 Patient resources for insurance members';
COMMENT ON COLUMN patients.fhir_resource IS 'Complete FHIR Patient resource stored as JSONB';
COMMENT ON COLUMN patients.fhir_id IS 'FHIR logical ID (not auto-generated)';

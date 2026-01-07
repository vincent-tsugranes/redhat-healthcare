-- Create practitioners table for FHIR R4 Practitioner resources
CREATE TABLE practitioners (
    fhir_id VARCHAR(64) PRIMARY KEY,
    version_id BIGINT NOT NULL DEFAULT 1,

    -- Searchable fields extracted from FHIR resource
    identifier_system VARCHAR(255),
    identifier_value VARCHAR(255),

    -- National Provider Identifier (NPI)
    npi VARCHAR(20),

    -- Name fields
    family_name VARCHAR(255),
    given_name VARCHAR(255),
    full_name VARCHAR(500),
    prefix VARCHAR(50),
    suffix VARCHAR(50),

    -- Status
    active BOOLEAN DEFAULT true,

    -- Contact information
    phone VARCHAR(50),
    email VARCHAR(255),

    -- Demographics
    gender VARCHAR(20),
    birth_date DATE,

    -- Specialty/Qualification
    specialty_code VARCHAR(100),
    specialty_display VARCHAR(255),
    specialty_system VARCHAR(255),

    -- Address fields
    address_line VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),

    -- Full FHIR resource as JSON
    fhir_resource JSONB NOT NULL,

    -- Metadata
    last_updated TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,

    -- Constraints
    CONSTRAINT ck_gender CHECK (gender IN ('male', 'female', 'other', 'unknown'))
);

-- Indexes for FHIR search parameters
CREATE INDEX idx_practitioner_identifier ON practitioners(identifier_value);
CREATE INDEX idx_practitioner_npi ON practitioners(npi);
CREATE INDEX idx_practitioner_family_name ON practitioners(family_name);
CREATE INDEX idx_practitioner_given_name ON practitioners(given_name);
CREATE INDEX idx_practitioner_full_name ON practitioners(full_name);
CREATE INDEX idx_practitioner_email ON practitioners(email);
CREATE INDEX idx_practitioner_specialty ON practitioners(specialty_code);
CREATE INDEX idx_practitioner_active ON practitioners(active);
CREATE INDEX idx_practitioner_last_updated ON practitioners(last_updated);

-- GIN index for JSONB queries (advanced searching)
CREATE INDEX idx_practitioner_fhir_resource ON practitioners USING GIN(fhir_resource);

-- Comments
COMMENT ON TABLE practitioners IS 'FHIR R4 Practitioner resources for healthcare providers';
COMMENT ON COLUMN practitioners.fhir_resource IS 'Complete FHIR Practitioner resource stored as JSONB';
COMMENT ON COLUMN practitioners.fhir_id IS 'FHIR logical ID (not auto-generated)';
COMMENT ON COLUMN practitioners.npi IS 'National Provider Identifier for US healthcare providers';
COMMENT ON COLUMN practitioners.specialty_code IS 'Primary specialty code from FHIR Practitioner.qualification or role';

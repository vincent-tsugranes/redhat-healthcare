-- Create claims table for FHIR R4 Claim resources
CREATE TABLE claims (
    fhir_id VARCHAR(64) PRIMARY KEY,
    version_id BIGINT NOT NULL DEFAULT 1,

    -- Searchable fields extracted from FHIR resource
    identifier_system VARCHAR(255),
    identifier_value VARCHAR(255),

    -- Status and type
    status VARCHAR(30),
    claim_type VARCHAR(50),
    claim_use VARCHAR(20),

    -- Patient reference
    patient_reference VARCHAR(255),
    patient_display VARCHAR(255),

    -- Provider references
    provider_reference VARCHAR(255),
    provider_display VARCHAR(255),

    -- Insurer/payor
    insurer_reference VARCHAR(255),
    insurer_display VARCHAR(255),

    -- Priority
    priority_code VARCHAR(50),
    priority_display VARCHAR(100),

    -- Financial
    total_value DECIMAL(12,2),
    total_currency VARCHAR(10),

    -- Dates
    created_date TIMESTAMP,
    billable_period_start DATE,
    billable_period_end DATE,

    -- Full FHIR resource as JSON
    fhir_resource JSONB NOT NULL,

    -- Metadata
    active BOOLEAN DEFAULT true,
    last_updated TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,

    -- Constraints
    CONSTRAINT ck_status CHECK (status IN ('active', 'cancelled', 'draft', 'entered-in-error')),
    CONSTRAINT ck_use CHECK (claim_use IN ('claim', 'preauthorization', 'predetermination'))
);

-- Indexes for FHIR search parameters
CREATE INDEX idx_claim_identifier ON claims(identifier_value);
CREATE INDEX idx_claim_patient ON claims(patient_reference);
CREATE INDEX idx_claim_provider ON claims(provider_reference);
CREATE INDEX idx_claim_insurer ON claims(insurer_reference);
CREATE INDEX idx_claim_status ON claims(status);
CREATE INDEX idx_claim_created ON claims(created_date);
CREATE INDEX idx_claim_billable_period ON claims(billable_period_start, billable_period_end);
CREATE INDEX idx_claim_active ON claims(active);
CREATE INDEX idx_claim_last_updated ON claims(last_updated);

-- GIN index for JSONB queries (advanced searching)
CREATE INDEX idx_claim_fhir_resource ON claims USING GIN(fhir_resource);

-- Comments
COMMENT ON TABLE claims IS 'FHIR R4 Claim resources for insurance claims';
COMMENT ON COLUMN claims.fhir_resource IS 'Complete FHIR Claim resource stored as JSONB';
COMMENT ON COLUMN claims.fhir_id IS 'FHIR logical ID (not auto-generated)';
COMMENT ON COLUMN claims.patient_reference IS 'Reference to Patient for whom claim is being made';
COMMENT ON COLUMN claims.provider_reference IS 'Reference to healthcare provider submitting the claim';
COMMENT ON COLUMN claims.insurer_reference IS 'Reference to insurance organization processing the claim';

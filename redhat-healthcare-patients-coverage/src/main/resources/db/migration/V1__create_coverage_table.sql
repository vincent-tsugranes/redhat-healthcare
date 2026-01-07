-- Create coverage table for FHIR R4 Coverage resources
CREATE TABLE coverage (
    fhir_id VARCHAR(64) PRIMARY KEY,
    version_id BIGINT NOT NULL DEFAULT 1,

    -- Searchable fields extracted from FHIR resource
    identifier_system VARCHAR(255),
    identifier_value VARCHAR(255),

    -- Subscriber/patient reference
    subscriber_reference VARCHAR(255),
    beneficiary_reference VARCHAR(255),

    -- Coverage details
    status VARCHAR(20),
    type_code VARCHAR(100),
    type_display VARCHAR(255),

    -- Payor/insurer
    payor_reference VARCHAR(255),
    payor_display VARCHAR(255),

    -- Coverage period
    period_start DATE,
    period_end DATE,

    -- Full FHIR resource as JSON
    fhir_resource JSONB NOT NULL,

    -- Metadata
    active BOOLEAN DEFAULT true,
    last_updated TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,

    -- Constraints
    CONSTRAINT ck_status CHECK (status IN ('active', 'cancelled', 'draft', 'entered-in-error'))
);

-- Indexes for FHIR search parameters
CREATE INDEX idx_coverage_identifier ON coverage(identifier_value);
CREATE INDEX idx_coverage_beneficiary ON coverage(beneficiary_reference);
CREATE INDEX idx_coverage_subscriber ON coverage(subscriber_reference);
CREATE INDEX idx_coverage_payor ON coverage(payor_reference);
CREATE INDEX idx_coverage_status ON coverage(status);
CREATE INDEX idx_coverage_period ON coverage(period_start, period_end);
CREATE INDEX idx_coverage_active ON coverage(active);
CREATE INDEX idx_coverage_last_updated ON coverage(last_updated);

-- GIN index for JSONB queries (advanced searching)
CREATE INDEX idx_coverage_fhir_resource ON coverage USING GIN(fhir_resource);

-- Comments
COMMENT ON TABLE coverage IS 'FHIR R4 Coverage resources for insurance coverage';
COMMENT ON COLUMN coverage.fhir_resource IS 'Complete FHIR Coverage resource stored as JSONB';
COMMENT ON COLUMN coverage.fhir_id IS 'FHIR logical ID (not auto-generated)';
COMMENT ON COLUMN coverage.beneficiary_reference IS 'Reference to Patient receiving coverage';
COMMENT ON COLUMN coverage.subscriber_reference IS 'Reference to subscriber (may be different from beneficiary)';

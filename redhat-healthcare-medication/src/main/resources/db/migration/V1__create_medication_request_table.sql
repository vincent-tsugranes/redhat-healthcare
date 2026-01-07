-- Create medication_requests table
CREATE TABLE medication_requests (
    fhir_id VARCHAR(64) PRIMARY KEY,
    version_id BIGINT,
    patient_reference VARCHAR(255),
    patient_display VARCHAR(255),
    requester_reference VARCHAR(255),
    requester_display VARCHAR(255),
    medication_code VARCHAR(100),
    medication_display VARCHAR(500),
    medication_system VARCHAR(255),
    status VARCHAR(20),
    intent VARCHAR(20),
    authored_on TIMESTAMP,
    dosage_text VARCHAR(1000),
    quantity_value DECIMAL(10, 2),
    quantity_unit VARCHAR(50),
    refills_allowed INTEGER,
    supply_duration_value DECIMAL(10, 2),
    supply_duration_unit VARCHAR(50),
    fhir_resource JSONB,
    active BOOLEAN DEFAULT TRUE,
    last_updated TIMESTAMP,
    created_at TIMESTAMP
);

-- Create indexes for efficient querying
CREATE INDEX idx_medreq_patient ON medication_requests(patient_reference);
CREATE INDEX idx_medreq_requester ON medication_requests(requester_reference);
CREATE INDEX idx_medreq_status ON medication_requests(status);
CREATE INDEX idx_medreq_medication ON medication_requests(medication_code);
CREATE INDEX idx_medreq_active ON medication_requests(active);
CREATE INDEX idx_medreq_authored ON medication_requests(authored_on);

-- Create index on JSONB column for faster queries
CREATE INDEX idx_medreq_fhir_resource ON medication_requests USING GIN (fhir_resource);

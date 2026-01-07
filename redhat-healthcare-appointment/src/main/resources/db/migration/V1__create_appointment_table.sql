-- Create appointments table for FHIR R4 Appointment resources
CREATE TABLE appointments (
    fhir_id VARCHAR(64) PRIMARY KEY,
    version_id BIGINT NOT NULL DEFAULT 1,

    -- Searchable fields extracted from FHIR resource
    identifier_system VARCHAR(255),
    identifier_value VARCHAR(255),

    -- Status - FHIR Appointment status codes
    status VARCHAR(50) NOT NULL,

    -- Service classification
    service_category_code VARCHAR(100),
    service_category_display VARCHAR(255),
    service_type_code VARCHAR(100),
    service_type_display VARCHAR(255),
    specialty_code VARCHAR(100),
    specialty_display VARCHAR(255),

    -- Appointment type
    appointment_type_code VARCHAR(100),
    appointment_type_display VARCHAR(255),

    -- Reason for appointment
    reason_code VARCHAR(100),
    reason_display VARCHAR(255),

    -- Priority (0-9, higher is more urgent)
    priority INTEGER,

    -- Description
    description TEXT,

    -- Timing
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    minutes_duration INTEGER,

    -- Participant references - Patient
    patient_reference VARCHAR(255),
    patient_display VARCHAR(255),

    -- Participant references - Practitioner
    practitioner_reference VARCHAR(255),
    practitioner_display VARCHAR(255),

    -- Participant references - Location
    location_reference VARCHAR(255),
    location_display VARCHAR(255),

    -- Comment/Instructions
    comment TEXT,

    -- Activity status
    active BOOLEAN DEFAULT true,

    -- Full FHIR resource as JSON
    fhir_resource JSONB NOT NULL,

    -- Metadata
    last_updated TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,

    -- Constraints
    CONSTRAINT ck_status CHECK (status IN (
        'proposed', 'pending', 'booked', 'arrived', 'fulfilled',
        'cancelled', 'noshow', 'entered-in-error', 'checked-in', 'waitlist'
    )),
    CONSTRAINT ck_end_time_after_start CHECK (end_time IS NULL OR start_time IS NULL OR end_time >= start_time)
);

-- Indexes for FHIR search parameters
CREATE INDEX idx_appointment_identifier ON appointments(identifier_value);
CREATE INDEX idx_appointment_status ON appointments(status);
CREATE INDEX idx_appointment_patient ON appointments(patient_reference);
CREATE INDEX idx_appointment_practitioner ON appointments(practitioner_reference);
CREATE INDEX idx_appointment_start_time ON appointments(start_time);
CREATE INDEX idx_appointment_active ON appointments(active);
CREATE INDEX idx_appointment_last_updated ON appointments(last_updated);

-- Composite indexes for common queries
CREATE INDEX idx_appointment_patient_status ON appointments(patient_reference, status);
CREATE INDEX idx_appointment_practitioner_date ON appointments(practitioner_reference, start_time);
CREATE INDEX idx_appointment_specialty ON appointments(specialty_code);

-- GIN index for JSONB queries (advanced searching)
CREATE INDEX idx_appointment_fhir_resource ON appointments USING GIN(fhir_resource);

-- Comments
COMMENT ON TABLE appointments IS 'FHIR R4 Appointment resources for healthcare scheduling';
COMMENT ON COLUMN appointments.fhir_resource IS 'Complete FHIR Appointment resource stored as JSONB';
COMMENT ON COLUMN appointments.fhir_id IS 'FHIR logical ID (not auto-generated)';
COMMENT ON COLUMN appointments.status IS 'FHIR Appointment status: proposed, pending, booked, arrived, fulfilled, cancelled, noshow, entered-in-error, checked-in, waitlist';
COMMENT ON COLUMN appointments.priority IS 'Appointment priority: 0-9 where higher numbers indicate more urgent';

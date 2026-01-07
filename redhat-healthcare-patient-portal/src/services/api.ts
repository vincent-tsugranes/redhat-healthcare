import axios from 'axios'

// API base URLs - use nginx proxy when in production (containerized)
// or direct URLs when in development
const isProduction = import.meta.env.PROD

export const PATIENT_API_URL = isProduction ? '/api/patients' : 'http://localhost:8080/fhir'
export const COVERAGE_API_URL = isProduction ? '/api/coverage' : 'http://localhost:8081/fhir'
export const CLAIMS_API_URL = isProduction ? '/api/claims' : 'http://localhost:8082/fhir'
export const PRACTITIONER_API_URL = isProduction ? '/api/practitioners' : 'http://localhost:8083/fhir'
export const APPOINTMENT_API_URL = isProduction ? '/api/appointments' : 'http://localhost:8084/fhir'
export const MEDICATION_API_URL = isProduction ? '/api/medications' : 'http://localhost:8085/fhir'

// Create axios instances for each service
export const patientApi = axios.create({
  baseURL: PATIENT_API_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

export const coverageApi = axios.create({
  baseURL: COVERAGE_API_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

export const claimsApi = axios.create({
  baseURL: CLAIMS_API_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

export const practitionerApi = axios.create({
  baseURL: PRACTITIONER_API_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

export const appointmentApi = axios.create({
  baseURL: APPOINTMENT_API_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

export const medicationApi = axios.create({
  baseURL: MEDICATION_API_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

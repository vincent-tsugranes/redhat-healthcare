import axios from 'axios'

// API base URLs - use nginx proxy when in production (containerized)
// or direct URLs when in development
const isProduction = import.meta.env.PROD

export const PATIENT_API_URL = isProduction ? '/api/patients' : 'http://localhost:8080/fhir'
export const COVERAGE_API_URL = isProduction ? '/api/coverage' : 'http://localhost:8081/fhir'
export const CLAIMS_API_URL = isProduction ? '/api/claims' : 'http://localhost:8082/fhir'

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

import { patientApi, coverageApi, claimsApi } from './api'

export interface FhirResource {
  resourceType: string
  id?: string
  [key: string]: any
}

export interface FhirBundle {
  resourceType: 'Bundle'
  type: string
  total: number
  entry?: Array<{
    fullUrl: string
    resource: FhirResource
  }>
}

// Patient Service
export const patientService = {
  async getPatient(id: string): Promise<FhirResource> {
    const response = await patientApi.get(`/Patient/${id}`)
    return response.data
  },

  async searchPatients(params: Record<string, string> = {}): Promise<FhirBundle> {
    const response = await patientApi.get('/Patient', { params })
    return response.data
  },

  async getAllPatients(): Promise<FhirResource[]> {
    const bundle = await this.searchPatients()
    return bundle.entry?.map(entry => entry.resource) || []
  }
}

// Coverage Service
export const coverageService = {
  async getCoverage(id: string): Promise<FhirResource> {
    const response = await coverageApi.get(`/Coverage/${id}`)
    return response.data
  },

  async searchCoverage(params: Record<string, string> = {}): Promise<FhirBundle> {
    const response = await coverageApi.get('/Coverage', { params })
    return response.data
  },

  async getCoverageByBeneficiary(patientRef: string): Promise<FhirResource[]> {
    const bundle = await this.searchCoverage({ beneficiary: patientRef })
    return bundle.entry?.map(entry => entry.resource) || []
  }
}

// Claims Service
export const claimsService = {
  async getClaim(id: string): Promise<FhirResource> {
    const response = await claimsApi.get(`/Claim/${id}`)
    return response.data
  },

  async searchClaims(params: Record<string, string> = {}): Promise<FhirBundle> {
    const response = await claimsApi.get('/Claim', { params })
    return response.data
  },

  async getClaimsByPatient(patientRef: string): Promise<FhirResource[]> {
    const bundle = await this.searchClaims({ patient: patientRef })
    return bundle.entry?.map(entry => entry.resource) || []
  }
}

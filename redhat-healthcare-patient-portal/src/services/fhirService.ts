import { patientApi, coverageApi, claimsApi, practitionerApi, appointmentApi, medicationApi } from './api'

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

// Practitioner Service
export const practitionerService = {
  async getPractitioner(id: string): Promise<FhirResource> {
    const response = await practitionerApi.get(`/Practitioner/${id}`)
    return response.data
  },

  async searchPractitioners(params: Record<string, string> = {}): Promise<FhirBundle> {
    const response = await practitionerApi.get('/Practitioner', { params })
    return response.data
  },

  async getAllPractitioners(): Promise<FhirResource[]> {
    const bundle = await this.searchPractitioners()
    return bundle.entry?.map(entry => entry.resource) || []
  },

  async searchByName(name: string): Promise<FhirResource[]> {
    const bundle = await this.searchPractitioners({ name })
    return bundle.entry?.map(entry => entry.resource) || []
  },

  async searchBySpecialty(specialty: string): Promise<FhirResource[]> {
    const bundle = await this.searchPractitioners({ specialty })
    return bundle.entry?.map(entry => entry.resource) || []
  }
}

// Appointment Service
export const appointmentService = {
  async getAppointment(id: string): Promise<FhirResource> {
    const response = await appointmentApi.get(`/Appointment/${id}`)
    return response.data
  },

  async searchAppointments(params: Record<string, string> = {}): Promise<FhirBundle> {
    const response = await appointmentApi.get('/Appointment', { params })
    return response.data
  },

  async getAppointmentsByPatient(patientRef: string): Promise<FhirResource[]> {
    const bundle = await this.searchAppointments({ patient: patientRef })
    return bundle.entry?.map(entry => entry.resource) || []
  },

  async getAppointmentsByPractitioner(practitionerRef: string): Promise<FhirResource[]> {
    const bundle = await this.searchAppointments({ practitioner: practitionerRef })
    return bundle.entry?.map(entry => entry.resource) || []
  },

  async getAppointmentsByStatus(status: string): Promise<FhirResource[]> {
    const bundle = await this.searchAppointments({ status })
    return bundle.entry?.map(entry => entry.resource) || []
  }
}

// Medication Service
export const medicationService = {
  async getMedicationRequest(id: string): Promise<FhirResource> {
    const response = await medicationApi.get(`/MedicationRequest/${id}`)
    return response.data
  },

  async searchMedicationRequests(params: Record<string, string> = {}): Promise<FhirBundle> {
    const response = await medicationApi.get('/MedicationRequest', { params })
    return response.data
  },

  async getMedicationRequestsByPatient(patientRef: string): Promise<FhirResource[]> {
    const bundle = await this.searchMedicationRequests({ patient: patientRef })
    return bundle.entry?.map(entry => entry.resource) || []
  },

  async getMedicationRequestsByRequester(requesterRef: string): Promise<FhirResource[]> {
    const bundle = await this.searchMedicationRequests({ requester: requesterRef })
    return bundle.entry?.map(entry => entry.resource) || []
  },

  async getMedicationRequestsByStatus(status: string): Promise<FhirResource[]> {
    const bundle = await this.searchMedicationRequests({ status })
    return bundle.entry?.map(entry => entry.resource) || []
  },

  async getMedicationRequestsByPatientAndStatus(patientRef: string, status: string): Promise<FhirResource[]> {
    const bundle = await this.searchMedicationRequests({ patient: patientRef, status })
    return bundle.entry?.map(entry => entry.resource) || []
  }
}

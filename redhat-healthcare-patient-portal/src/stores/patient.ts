import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { FhirResource } from '@/services/fhirService'
import {
  patientService,
  coverageService,
  claimsService,
  practitionerService,
  appointmentService
} from '@/services/fhirService'

export const usePatientStore = defineStore('patient', () => {
  // State
  const currentPatient = ref<FhirResource | null>(null)
  const patients = ref<FhirResource[]>([])
  const coverages = ref<FhirResource[]>([])
  const claims = ref<FhirResource[]>([])
  const practitioners = ref<FhirResource[]>([])
  const appointments = ref<FhirResource[]>([])
  const allAppointments = ref<FhirResource[]>([])  // All appointments for all patients
  const allClaims = ref<FhirResource[]>([])  // All claims for all patients
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const patientReference = computed(() =>
    currentPatient.value ? `Patient/${currentPatient.value.id}` : null
  )

  const patientName = computed(() => {
    if (!currentPatient.value || !currentPatient.value.name) return 'Unknown'
    const name = currentPatient.value.name[0]
    return `${name.given?.join(' ') || ''} ${name.family || ''}`.trim()
  })

  // Actions
  async function loadAllPatients() {
    loading.value = true
    error.value = null
    try {
      patients.value = await patientService.getAllPatients()
    } catch (err: any) {
      error.value = err.message || 'Failed to load patients'
      console.error('Error loading patients:', err)
    } finally {
      loading.value = false
    }
  }

  async function selectPatient(patientId: string) {
    loading.value = true
    error.value = null
    try {
      currentPatient.value = await patientService.getPatient(patientId)
      if (patientReference.value) {
        await loadPatientData()
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to load patient'
      console.error('Error loading patient:', err)
    } finally {
      loading.value = false
    }
  }

  async function loadPatientData() {
    if (!patientReference.value) return

    loading.value = true
    try {
      const [coverageData, claimsData, appointmentsData] = await Promise.all([
        coverageService.getCoverageByBeneficiary(patientReference.value),
        claimsService.getClaimsByPatient(patientReference.value),
        appointmentService.getAppointmentsByPatient(patientReference.value)
      ])
      coverages.value = coverageData
      claims.value = claimsData
      appointments.value = appointmentsData
    } catch (err: any) {
      error.value = err.message || 'Failed to load patient data'
      console.error('Error loading patient data:', err)
    } finally {
      loading.value = false
    }
  }

  async function loadAllPractitioners() {
    loading.value = true
    error.value = null
    try {
      practitioners.value = await practitionerService.getAllPractitioners()
    } catch (err: any) {
      error.value = err.message || 'Failed to load practitioners'
      console.error('Error loading practitioners:', err)
    } finally {
      loading.value = false
    }
  }

  async function searchPractitioners(query: string) {
    loading.value = true
    error.value = null
    try {
      if (!query) {
        await loadAllPractitioners()
        return
      }
      // Try searching by name first
      practitioners.value = await practitionerService.searchByName(query)
    } catch (err: any) {
      error.value = err.message || 'Failed to search practitioners'
      console.error('Error searching practitioners:', err)
    } finally {
      loading.value = false
    }
  }

  async function loadAllAppointmentsAndClaims() {
    try {
      const [appointmentsData, claimsData] = await Promise.all([
        appointmentService.searchAppointments({ _count: '1000' }),
        claimsService.searchClaims({ _count: '5000' })
      ])
      allAppointments.value = appointmentsData.entry?.map(entry => entry.resource) || []
      allClaims.value = claimsData.entry?.map(entry => entry.resource) || []
    } catch (err: any) {
      console.error('Error loading all appointments and claims:', err)
    }
  }

  function getUpcomingAppointmentCount(patientId: string): number {
    const now = new Date()
    return allAppointments.value.filter(appt => {
      // Check if this appointment belongs to the patient
      const isPatientAppt = appt.participant?.some((p: any) =>
        p.actor?.reference === `Patient/${patientId}`
      )
      if (!isPatientAppt) return false

      // Check if appointment is in the future
      const startTime = appt.start ? new Date(appt.start) : null
      return startTime && startTime >= now
    }).length
  }

  function getPendingClaimsCount(patientId: string): number {
    return allClaims.value.filter(claim => {
      // Check if this claim belongs to the patient
      const isPatientClaim = claim.patient?.reference === `Patient/${patientId}`
      if (!isPatientClaim) return false

      // Check if status is active (pending)
      return claim.status === 'active'
    }).length
  }

  function clearError() {
    error.value = null
  }

  return {
    // State
    currentPatient,
    patients,
    coverages,
    claims,
    practitioners,
    appointments,
    allAppointments,
    allClaims,
    loading,
    error,
    // Getters
    patientReference,
    patientName,
    // Actions
    loadAllPatients,
    selectPatient,
    loadPatientData,
    loadAllPractitioners,
    searchPractitioners,
    loadAllAppointmentsAndClaims,
    getUpcomingAppointmentCount,
    getPendingClaimsCount,
    clearError
  }
})

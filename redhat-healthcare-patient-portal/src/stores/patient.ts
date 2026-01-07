import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { FhirResource } from '@/services/fhirService'
import { patientService, coverageService, claimsService } from '@/services/fhirService'

export const usePatientStore = defineStore('patient', () => {
  // State
  const currentPatient = ref<FhirResource | null>(null)
  const patients = ref<FhirResource[]>([])
  const coverages = ref<FhirResource[]>([])
  const claims = ref<FhirResource[]>([])
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
      const [coverageData, claimsData] = await Promise.all([
        coverageService.getCoverageByBeneficiary(patientReference.value),
        claimsService.getClaimsByPatient(patientReference.value)
      ])
      coverages.value = coverageData
      claims.value = claimsData
    } catch (err: any) {
      error.value = err.message || 'Failed to load patient data'
      console.error('Error loading patient data:', err)
    } finally {
      loading.value = false
    }
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
    loading,
    error,
    // Getters
    patientReference,
    patientName,
    // Actions
    loadAllPatients,
    selectPatient,
    loadPatientData,
    clearError
  }
})

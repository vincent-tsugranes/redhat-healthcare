<script setup lang="ts">
import { usePatientStore } from '@/stores/patient'
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import type { FhirResource } from '@/services/fhirService'

const patientStore = usePatientStore()
const route = useRoute()
const searchQuery = ref('')
const selectedSpecialty = ref('')
const filterPractitionerId = ref('')

// Load all practitioners and appointments/medications on mount
onMounted(async () => {
  await patientStore.loadAllPractitioners()
  await patientStore.loadAllAppointmentsAndClaims()

  // Check if we need to filter by practitioner ID from URL
  if (route.query.practitioner) {
    filterPractitionerId.value = route.query.practitioner as string
  }
})

// Watch for changes to route query params
watch(() => route.query.practitioner, (newValue) => {
  filterPractitionerId.value = newValue as string || ''
})

// Get unique specialties for filter dropdown
const specialties = computed(() => {
  const specialtySet = new Set<string>()
  patientStore.practitioners.forEach((practitioner: FhirResource) => {
    const qualification = practitioner.qualification?.[0]
    const specialty = qualification?.code?.coding?.[0]?.display ||
                     qualification?.code?.text
    if (specialty) {
      specialtySet.add(specialty)
    }
  })
  return Array.from(specialtySet).sort()
})

// Filtered practitioners based on search and specialty
const filteredPractitioners = computed(() => {
  let filtered = [...patientStore.practitioners]

  // Filter by practitioner ID if specified
  if (filterPractitionerId.value) {
    filtered = filtered.filter((practitioner: FhirResource) =>
      practitioner.id === filterPractitionerId.value
    )
  }

  // Filter by specialty
  if (selectedSpecialty.value) {
    filtered = filtered.filter((practitioner: FhirResource) => {
      const qualification = practitioner.qualification?.[0]
      const specialty = qualification?.code?.coding?.[0]?.display ||
                       qualification?.code?.text
      return specialty === selectedSpecialty.value
    })
  }

  // Filter by search query
  const query = searchQuery.value.toLowerCase().trim()
  if (query) {
    filtered = filtered.filter((practitioner: FhirResource) => {
      const name = getPractitionerName(practitioner).toLowerCase()
      const qualification = practitioner.qualification?.[0]
      const specialty = (qualification?.code?.coding?.[0]?.display ||
                        qualification?.code?.text || '').toLowerCase()
      const npi = practitioner.identifier?.find((id: any) =>
        id.system?.includes('npi')
      )?.value || ''

      return name.includes(query) ||
             specialty.includes(query) ||
             npi.includes(query)
    })
  }

  // Sort by name
  return filtered.sort((a: FhirResource, b: FhirResource) => {
    const nameA = a.name?.[0]?.family || ''
    const nameB = b.name?.[0]?.family || ''
    return nameA.localeCompare(nameB)
  })
})

function clearFilters() {
  searchQuery.value = ''
  selectedSpecialty.value = ''
  filterPractitionerId.value = ''
}

function getPractitionerName(practitioner: FhirResource): string {
  if (!practitioner.name || practitioner.name.length === 0) return 'Unknown'
  const name = practitioner.name[0]
  const prefix = name.prefix?.join(' ') || ''
  const given = name.given?.join(' ') || ''
  const family = name.family || ''
  const suffix = name.suffix?.join(', ') || ''

  return `${prefix} ${given} ${family}${suffix ? ', ' + suffix : ''}`.trim()
}

function getSpecialty(practitioner: FhirResource): string {
  const qualification = practitioner.qualification?.[0]
  return qualification?.code?.coding?.[0]?.display ||
         qualification?.code?.text || 'Not specified'
}

function getNPI(practitioner: FhirResource): string {
  const npiIdentifier = practitioner.identifier?.find((id: any) =>
    id.system?.includes('npi')
  )
  return npiIdentifier?.value || 'N/A'
}

function getPhone(practitioner: FhirResource): string {
  const phoneContact = practitioner.telecom?.find((contact: any) =>
    contact.system === 'phone'
  )
  return phoneContact?.value || 'N/A'
}

function getEmail(practitioner: FhirResource): string {
  const emailContact = practitioner.telecom?.find((contact: any) =>
    contact.system === 'email'
  )
  return emailContact?.value || 'N/A'
}

function getAddress(practitioner: FhirResource): string {
  if (!practitioner.address || practitioner.address.length === 0) return 'N/A'
  const addr = practitioner.address[0]
  const line = addr.line?.join(', ') || ''
  const city = addr.city || ''
  const state = addr.state || ''
  const postal = addr.postalCode || ''
  return `${line}${line && city ? ', ' : ''}${city}${city && state ? ', ' : ''}${state} ${postal}`.trim()
}

function clearSearch() {
  searchQuery.value = ''
}

function clearSpecialty() {
  selectedSpecialty.value = ''
}
</script>

<template>
  <div class="provider-directory">
    <div class="header-section">
      <h2>Provider Directory</h2>
      <p class="subtitle">Find healthcare providers and specialists</p>
    </div>

    <div v-if="patientStore.loading" class="loading-state">
      <p>Loading providers...</p>
    </div>

    <div v-else-if="patientStore.practitioners.length === 0" class="empty-state">
      <p>No providers found</p>
    </div>

    <div v-else>
      <!-- Search and Filter Section -->
      <div class="filter-section">
        <div class="search-bar">
          <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Search by name, specialty, or NPI..."
            class="search-input"
          />
          <button v-if="searchQuery" @click="clearSearch" class="clear-btn">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div class="specialty-filter">
          <select v-model="selectedSpecialty" class="specialty-select">
            <option value="">All Specialties</option>
            <option v-for="specialty in specialties" :key="specialty" :value="specialty">
              {{ specialty }}
            </option>
          </select>
          <button v-if="selectedSpecialty" @click="clearSpecialty" class="clear-filter-btn">
            Clear Filter
          </button>
        </div>
      </div>

      <!-- Results Count -->
      <div class="results-count">
        <p>{{ filteredPractitioners.length }} provider{{ filteredPractitioners.length !== 1 ? 's' : '' }} found</p>
      </div>

      <!-- Practitioners Grid -->
      <div class="practitioners-grid">
        <div
          v-for="practitioner in filteredPractitioners"
          :key="practitioner.id"
          class="practitioner-card"
        >
          <div class="card-header">
            <div class="practitioner-icon">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <div class="practitioner-info">
              <h3>{{ getPractitionerName(practitioner) }}</h3>
              <p class="specialty">{{ getSpecialty(practitioner) }}</p>
            </div>
          </div>

          <div class="card-body">
            <div class="info-row">
              <span class="label">NPI:</span>
              <span class="value">{{ getNPI(practitioner) }}</span>
            </div>

            <div class="info-row">
              <span class="label">Phone:</span>
              <span class="value">
                <a v-if="getPhone(practitioner) !== 'N/A'" :href="`tel:${getPhone(practitioner)}`">
                  {{ getPhone(practitioner) }}
                </a>
                <span v-else>{{ getPhone(practitioner) }}</span>
              </span>
            </div>

            <div class="info-row">
              <span class="label">Email:</span>
              <span class="value">
                <a v-if="getEmail(practitioner) !== 'N/A'" :href="`mailto:${getEmail(practitioner)}`">
                  {{ getEmail(practitioner) }}
                </a>
                <span v-else>{{ getEmail(practitioner) }}</span>
              </span>
            </div>

            <div class="info-row full-width">
              <span class="label">Address:</span>
              <span class="value">{{ getAddress(practitioner) }}</span>
            </div>

            <div v-if="practitioner.id" class="provider-stats">
              <span class="stat-badge appointments">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                {{ patientStore.getProviderUpcomingAppointmentCount(practitioner.id) }} upcoming
              </span>
              <span class="stat-badge medications">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
                </svg>
                {{ patientStore.getProviderMedicationCount(practitioner.id) }} active RX
              </span>
            </div>

            <div v-if="practitioner.active" class="status-badge active">
              Active Provider
            </div>
            <div v-else class="status-badge inactive">
              Inactive
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="patientStore.loading" class="loading">
      Loading providers...
    </div>
  </div>
</template>

<style scoped>
.provider-directory {
  max-width: 1200px;
  margin: 0 auto;
}

.header-section {
  margin-bottom: 2rem;
}

.header-section h2 {
  color: #333;
  margin: 0 0 0.5rem;
  font-size: 1.8rem;
}

.subtitle {
  color: #666;
  margin: 0;
  font-size: 1rem;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 8px;
  color: #666;
}

.filter-section {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.search-bar {
  position: relative;
  flex: 1;
  min-width: 300px;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: #999;
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 0.75rem 3rem 0.75rem 3rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
}

.clear-btn {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.clear-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.clear-btn svg {
  width: 18px;
  height: 18px;
}

.specialty-filter {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.specialty-select {
  padding: 0.75rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 200px;
}

.specialty-select:focus {
  outline: none;
  border-color: #667eea;
}

.clear-filter-btn {
  padding: 0.5rem 1rem;
  border: none;
  background: #f44336;
  color: white;
  border-radius: 6px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.clear-filter-btn:hover {
  background: #d32f2f;
}

.results-count {
  margin-bottom: 1rem;
  color: #666;
  font-size: 0.9rem;
}

.results-count p {
  margin: 0;
}

.practitioners-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.practitioner-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s;
}

.practitioner-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 1.5rem;
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.practitioner-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.practitioner-icon svg {
  width: 28px;
  height: 28px;
}

.practitioner-info h3 {
  margin: 0 0 0.5rem;
  font-size: 1.2rem;
}

.practitioner-info .specialty {
  margin: 0;
  opacity: 0.9;
  font-size: 0.95rem;
  font-weight: 500;
}

.card-body {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
}

.info-row.full-width {
  flex-direction: column;
  gap: 0.25rem;
}

.info-row .label {
  font-weight: 600;
  color: #666;
  font-size: 0.875rem;
}

.info-row .value {
  color: #333;
  font-size: 0.95rem;
  text-align: right;
}

.info-row.full-width .value {
  text-align: left;
}

.info-row .value a {
  color: #667eea;
  text-decoration: none;
  transition: color 0.2s;
}

.info-row .value a:hover {
  color: #764ba2;
  text-decoration: underline;
}

.provider-stats {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.5rem;
  flex-wrap: wrap;
}

.provider-stats .stat-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.4rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.provider-stats .stat-badge svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.provider-stats .stat-badge.appointments {
  background: #e3f2fd;
  color: #1976d2;
}

.provider-stats .stat-badge.medications {
  background: #f3e5f5;
  color: #7b1fa2;
}

.status-badge {
  display: inline-block;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  margin-top: 0.5rem;
}

.status-badge.active {
  background: #4caf50;
  color: white;
}

.status-badge.inactive {
  background: #9e9e9e;
  color: white;
}

.loading,
.loading-state {
  text-align: center;
  padding: 2rem;
  color: #666;
  font-size: 1.1rem;
}
</style>

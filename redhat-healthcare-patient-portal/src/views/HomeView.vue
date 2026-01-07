<script setup lang="ts">
import { usePatientStore } from '@/stores/patient'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import PatientMap from '@/components/PatientMap.vue'

const patientStore = usePatientStore()
const router = useRouter()
const searchQuery = ref('')

const filteredPatients = computed(() => {
  const query = searchQuery.value.toLowerCase().trim()

  if (!query) {
    return [...patientStore.patients].sort((a, b) => {
      const nameA = a.name?.[0]?.family || ''
      const nameB = b.name?.[0]?.family || ''
      return nameA.localeCompare(nameB)
    })
  }

  // Split query into search terms (for multi-city search)
  const searchTerms = query.split(/\s+/)

  return [...patientStore.patients]
    .filter(patient => {
      const name = getPatientName(patient).toLowerCase()
      const city = patient.address?.[0]?.city?.toLowerCase() || ''
      const id = patient.id?.toLowerCase() || ''

      // Match if any search term matches name, city, or id
      return searchTerms.some(term =>
        name.includes(term) || city.includes(term) || id.includes(term)
      )
    })
    .sort((a, b) => {
      const nameA = a.name?.[0]?.family || ''
      const nameB = b.name?.[0]?.family || ''
      return nameA.localeCompare(nameB)
    })
})

function getPatientName(patient: any) {
  if (!patient.name || patient.name.length === 0) return 'Unknown'
  const name = patient.name[0]
  return `${name.given?.join(' ') || ''} ${name.family || ''}`.trim()
}

function getPatientLocation(patient: any) {
  if (!patient.address || patient.address.length === 0) return 'N/A'
  const addr = patient.address[0]
  return `${addr.city || ''}${addr.city && addr.state ? ', ' : ''}${addr.state || ''}`
}

function formatBirthDate(patient: any) {
  if (!patient.birthDate) return 'N/A'
  return new Date(patient.birthDate).toLocaleDateString()
}

async function handleSelectPatient(patientId: string | undefined) {
  if (patientId) {
    await patientStore.selectPatient(patientId)
    router.push('/patient')
  }
}

function clearSearch() {
  searchQuery.value = ''
}

function handleCitySelect(city: string) {
  searchQuery.value = city
}

function handleCountySelect(cities: string[]) {
  searchQuery.value = cities.join(' ')
}
</script>

<template>
  <div class="home-view">
    <div class="header-section">
      <h2>Welcome to the Patient Portal</h2>
    </div>

    <div v-if="patientStore.patients.length === 0 && !patientStore.loading" class="empty-state">
      <p>No patients found</p>
    </div>

    <div v-else>
      <!-- Search Bar -->
      <div class="search-section">
        <div class="search-bar">
          <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Search patients by name, city, or ID..."
            class="search-input"
          />
          <button v-if="searchQuery" @click="clearSearch" class="clear-button">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <div class="search-results-count">
          {{ filteredPatients.length }} patient{{ filteredPatients.length !== 1 ? 's' : '' }} found
        </div>
      </div>

      <!-- Patient Map -->
      <PatientMap
        :patients="filteredPatients"
        @select-city="handleCitySelect"
        @select-county="handleCountySelect"
      />

      <!-- Patient List -->
      <div class="patient-list">
        <div
          v-for="patient in filteredPatients"
          :key="patient.id"
          class="patient-card"
          :class="{ active: patientStore.currentPatient?.id === patient.id }"
          @click="handleSelectPatient(patient.id)"
        >
          <div class="patient-avatar">
            {{ getPatientName(patient).charAt(0).toUpperCase() }}
          </div>
          <div class="patient-info">
            <h3>{{ getPatientName(patient) }}</h3>
            <div class="patient-details">
              <span v-if="patient.gender" class="detail">
                <strong>Gender:</strong> {{ patient.gender }}
              </span>
              <span class="detail">
                <strong>Birth Date:</strong> {{ formatBirthDate(patient) }}
              </span>
              <span class="detail">
                <strong>Location:</strong> {{ getPatientLocation(patient) }}
              </span>
              <span v-if="patient.identifier && patient.identifier.length > 0" class="detail">
                <strong>ID:</strong> {{ patient.identifier[0].value }}
              </span>
            </div>
          </div>
          <div class="patient-arrow">→</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-view {
  width: 100%;
  margin: 0 auto;
}

.header-section {
  text-align: center;
  margin-bottom: 0.5rem;
}

.header-section h2 {
  color: #333;
  font-size: 1.5rem;
  margin-bottom: 0;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #999;
  font-size: 1.1rem;
}

.search-section {
  margin-bottom: 0.75rem;
}

.search-bar {
  position: relative;
  width: 100%;
  max-width: 600px;
  margin: 0 auto 0.25rem;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: #999;
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 0.6rem 2.75rem 0.6rem 2.5rem;
  font-size: 0.9rem;
  border: 2px solid #e0e0e0;
  border-radius: 50px;
  outline: none;
  transition: all 0.3s;
}

.search-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.clear-button {
  position: absolute;
  right: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  padding: 4px;
  background: #f0f0f0;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.clear-button:hover {
  background: #e0e0e0;
}

.clear-button svg {
  width: 14px;
  height: 14px;
  color: #666;
}

.search-results-count {
  text-align: center;
  color: #666;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

.patient-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.patient-card {
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 1rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
  transition: all 0.3s;
}

.patient-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.patient-card.active {
  border-color: #667eea;
  background: #f9f9ff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.patient-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 700;
  flex-shrink: 0;
}

.patient-info {
  flex: 1;
}

.patient-info h3 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1.3rem;
}

.patient-details {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  color: #666;
  font-size: 0.9rem;
}

.detail strong {
  color: #333;
  margin-right: 0.25rem;
}

.patient-arrow {
  font-size: 1.5rem;
  color: #667eea;
  flex-shrink: 0;
}
</style>

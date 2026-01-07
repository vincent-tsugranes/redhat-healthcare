<script setup lang="ts">
import { usePatientStore } from '@/stores/patient'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FhirResource } from '@/services/fhirService'

const patientStore = usePatientStore()
const router = useRouter()
const activeTab = ref<'all' | 'active' | 'completed' | 'stopped'>('active')
const searchQuery = ref('')

function getPrescriberId(medication: FhirResource): string | null {
  if (!medication.requester?.reference) return null
  return medication.requester.reference.replace('Practitioner/', '')
}

function navigateToProvider(medication: FhirResource) {
  const practitionerId = getPrescriberId(medication)
  if (practitionerId) {
    router.push({
      path: '/providers',
      query: { practitioner: practitionerId }
    })
  }
}

// Filter medications by status and search query
const filteredMedications = computed(() => {
  let filtered = patientStore.medications

  // Filter by active tab
  if (activeTab.value !== 'all') {
    filtered = filtered.filter((med: FhirResource) =>
      med.status?.toLowerCase() === activeTab.value.toLowerCase()
    )
  }

  // Filter by search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter((med: FhirResource) => {
      const medicationName = getMedicationName(med).toLowerCase()
      return medicationName.includes(query)
    })
  }

  // Sort by authored date (newest first)
  return filtered.sort((a: FhirResource, b: FhirResource) => {
    const dateA = new Date(a.authoredOn || '').getTime()
    const dateB = new Date(b.authoredOn || '').getTime()
    return dateB - dateA
  })
})

const medicationCounts = computed(() => ({
  all: patientStore.medications.length,
  active: patientStore.medications.filter((m: FhirResource) => m.status === 'active').length,
  completed: patientStore.medications.filter((m: FhirResource) => m.status === 'completed').length,
  stopped: patientStore.medications.filter((m: FhirResource) => m.status === 'stopped').length
}))

function getMedicationName(medication: FhirResource): string {
  return medication.medicationCodeableConcept?.text ||
         medication.medicationCodeableConcept?.coding?.[0]?.display ||
         'Unknown Medication'
}

function getDosageInstruction(medication: FhirResource): string {
  return medication.dosageInstruction?.[0]?.text || 'No dosage information'
}

function getPrescribedBy(medication: FhirResource): string {
  return medication.requester?.display || 'Unknown Provider'
}

function getPrescribedDate(medication: FhirResource): string {
  if (!medication.authoredOn) return 'N/A'
  const date = new Date(medication.authoredOn)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

function getRefillsRemaining(medication: FhirResource): string {
  const refills = medication.dispenseRequest?.numberOfRepeatsAllowed
  if (refills === undefined || refills === null) return 'N/A'
  return refills.toString()
}

function getQuantity(medication: FhirResource): string {
  const quantity = medication.dispenseRequest?.quantity
  if (!quantity) return 'N/A'
  return `${quantity.value || ''} ${quantity.unit || ''}`.trim()
}

function getSupplyDuration(medication: FhirResource): string {
  const duration = medication.dispenseRequest?.expectedSupplyDuration
  if (!duration) return 'N/A'
  return `${duration.value || ''} ${duration.unit || ''}`.trim()
}

function getStatusClass(status: string): string {
  const statusMap: Record<string, string> = {
    'active': 'status-active',
    'completed': 'status-completed',
    'stopped': 'status-stopped',
    'on-hold': 'status-on-hold',
    'cancelled': 'status-cancelled',
    'draft': 'status-draft',
    'entered-in-error': 'status-error'
  }
  return statusMap[status?.toLowerCase()] || 'status-default'
}

function getStatusLabel(status: string): string {
  const statusMap: Record<string, string> = {
    'active': 'Active',
    'completed': 'Completed',
    'stopped': 'Stopped',
    'on-hold': 'On Hold',
    'cancelled': 'Cancelled',
    'draft': 'Draft',
    'entered-in-error': 'Error'
  }
  return statusMap[status?.toLowerCase()] || status
}
</script>

<template>
  <div class="medications-view">
    <div v-if="!patientStore.currentPatient" class="no-patient">
      <p>Please select a patient from the home page</p>
    </div>

    <div v-else>
      <h2>My Medications</h2>

      <div v-if="patientStore.medications.length === 0" class="empty-state">
        <p>No medications found</p>
      </div>

      <div v-else class="medications-content">
        <!-- Tab Navigation -->
        <div class="tabs">
          <button
            :class="{ active: activeTab === 'all' }"
            @click="activeTab = 'all'"
          >
            All ({{ medicationCounts.all }})
          </button>
          <button
            :class="{ active: activeTab === 'active' }"
            @click="activeTab = 'active'"
          >
            Active ({{ medicationCounts.active }})
          </button>
          <button
            :class="{ active: activeTab === 'completed' }"
            @click="activeTab = 'completed'"
          >
            Completed ({{ medicationCounts.completed }})
          </button>
          <button
            :class="{ active: activeTab === 'stopped' }"
            @click="activeTab = 'stopped'"
          >
            Stopped ({{ medicationCounts.stopped }})
          </button>
        </div>

        <!-- Search -->
        <div class="search-box">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Search medications..."
            class="search-input"
          />
        </div>

        <!-- Medications List -->
        <div v-if="filteredMedications.length === 0" class="empty-state">
          <p>No medications found matching your criteria</p>
        </div>

        <div v-else class="medications-list">
          <div
            v-for="medication in filteredMedications"
            :key="medication.id"
            class="medication-card"
          >
            <div class="medication-header">
              <div class="medication-icon">💊</div>
              <div class="medication-title">
                <h3>{{ getMedicationName(medication) }}</h3>
                <span :class="['status-badge', getStatusClass(medication.status)]">
                  {{ getStatusLabel(medication.status) }}
                </span>
              </div>
            </div>

            <div class="medication-body">
              <div class="dosage-section">
                <strong>Dosage Instructions:</strong>
                <p>{{ getDosageInstruction(medication) }}</p>
              </div>

              <div class="medication-details">
                <div class="detail-row">
                  <span class="detail-label">👨‍⚕️ Prescribed by:</span>
                  <span class="detail-value">
                    <a
                      v-if="getPrescriberId(medication)"
                      href="#"
                      @click.prevent="navigateToProvider(medication)"
                      class="provider-link"
                    >
                      {{ getPrescribedBy(medication) }}
                    </a>
                    <span v-else>{{ getPrescribedBy(medication) }}</span>
                  </span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">📅 Prescribed:</span>
                  <span class="detail-value">{{ getPrescribedDate(medication) }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">🔄 Refills:</span>
                  <span class="detail-value">{{ getRefillsRemaining(medication) }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">💊 Quantity:</span>
                  <span class="detail-value">{{ getQuantity(medication) }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">📦 Supply:</span>
                  <span class="detail-value">{{ getSupplyDuration(medication) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.medications-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.no-patient {
  text-align: center;
  padding: 3rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: #666;
}

h2 {
  margin-bottom: 2rem;
  color: #2c3e50;
  font-size: 2rem;
}

.medications-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  border-bottom: 2px solid #e0e0e0;
  flex-wrap: wrap;
}

.tabs button {
  padding: 0.75rem 1.5rem;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-size: 1rem;
  color: #666;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.tabs button:hover {
  color: #667eea;
  background-color: rgba(102, 126, 234, 0.05);
}

.tabs button.active {
  color: #667eea;
  border-bottom-color: #667eea;
  font-weight: 600;
}

.search-box {
  display: flex;
  justify-content: flex-end;
}

.search-input {
  width: 100%;
  max-width: 300px;
  padding: 0.75rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
}

.medications-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.medication-card {
  background: linear-gradient(135deg, #a8c0ff 0%, #c0b3ff 100%);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.medication-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.medication-header {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1rem;
}

.medication-icon {
  font-size: 2rem;
  background: white;
  border-radius: 12px;
  padding: 0.75rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.medication-title {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.medication-title h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.5rem;
}

.status-badge {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-active {
  background-color: #4caf50;
  color: white;
}

.status-completed {
  background-color: #9e9e9e;
  color: white;
}

.status-stopped {
  background-color: #f44336;
  color: white;
}

.status-on-hold {
  background-color: #ff9800;
  color: white;
}

.status-cancelled {
  background-color: #757575;
  color: white;
}

.medication-body {
  background: white;
  border-radius: 8px;
  padding: 1rem;
}

.dosage-section {
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e0e0e0;
}

.dosage-section strong {
  color: #667eea;
  display: block;
  margin-bottom: 0.5rem;
}

.dosage-section p {
  margin: 0;
  color: #2c3e50;
  line-height: 1.6;
}

.medication-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 0.75rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  background-color: #f9f9f9;
  border-radius: 6px;
}

.detail-label {
  font-weight: 500;
  color: #666;
  font-size: 0.95rem;
}

.detail-value {
  color: #2c3e50;
  font-weight: 600;
}

.provider-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.provider-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .medications-view {
    padding: 1rem;
  }

  .medication-title {
    flex-direction: column;
    align-items: flex-start;
  }

  .medication-details {
    grid-template-columns: 1fr;
  }

  .tabs {
    overflow-x: auto;
  }

  .search-input {
    max-width: 100%;
  }
}
</style>

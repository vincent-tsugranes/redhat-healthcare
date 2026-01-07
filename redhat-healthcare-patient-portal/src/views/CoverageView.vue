<script setup lang="ts">
import { usePatientStore } from '@/stores/patient'
import { computed } from 'vue'

const patientStore = usePatientStore()

const coverages = computed(() => patientStore.coverages)

function formatDate(dateString: string) {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString()
}

function getPayorName(coverage: any) {
  if (!coverage.payor || coverage.payor.length === 0) return 'Unknown'
  return coverage.payor[0].display || coverage.payor[0].reference || 'Unknown'
}

function getCoverageClass(coverage: any) {
  if (!coverage.class || coverage.class.length === 0) return []
  return coverage.class
}

function getStatusBadgeClass(status: string) {
  const statusMap: Record<string, string> = {
    'active': 'status-active',
    'cancelled': 'status-cancelled',
    'draft': 'status-draft',
    'entered-in-error': 'status-error'
  }
  return statusMap[status?.toLowerCase()] || 'status-default'
}

function formatMoney(value: number) {
  return `$${value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function formatQuantity(value: number, unit: string) {
  return `${value.toLocaleString('en-US')}${unit}`
}
</script>

<template>
  <div class="coverage-view">
    <div v-if="!patientStore.currentPatient" class="no-patient">
      <p>Please select a patient from the home page</p>
    </div>

    <div v-else>
      <h2>My Coverage</h2>

      <div v-if="coverages.length === 0" class="empty-state">
        <p>No coverage information found</p>
      </div>

      <div v-else class="coverage-list">
        <div v-for="coverage in coverages" :key="coverage.id" class="coverage-card">
          <div class="coverage-header">
            <div>
              <h3>{{ getPayorName(coverage) }}</h3>
              <p class="coverage-id">Coverage ID: {{ coverage.id }}</p>
            </div>
            <span class="status-badge" :class="getStatusBadgeClass(coverage.status)">
              {{ coverage.status || 'Unknown' }}
            </span>
          </div>

          <div class="coverage-body">
            <div class="info-grid">
              <div class="info-item">
                <label>Type</label>
                <p>{{ coverage.type?.text || coverage.type?.coding?.[0]?.display || 'N/A' }}</p>
              </div>

              <div class="info-item" v-if="coverage.subscriberId">
                <label>Subscriber ID</label>
                <p class="monospace">{{ coverage.subscriberId }}</p>
              </div>

              <div class="info-item" v-if="coverage.dependent">
                <label>Dependent</label>
                <p>{{ coverage.dependent }}</p>
              </div>

              <div class="info-item" v-if="coverage.relationship">
                <label>Relationship</label>
                <p>{{ coverage.relationship?.text || coverage.relationship?.coding?.[0]?.display || 'N/A' }}</p>
              </div>

              <div class="info-item" v-if="coverage.period">
                <label>Coverage Period</label>
                <p>
                  {{ formatDate(coverage.period.start) }}
                  <span v-if="coverage.period.end"> - {{ formatDate(coverage.period.end) }}</span>
                  <span v-else> - Ongoing</span>
                </p>
              </div>

              <div class="info-item" v-if="coverage.network">
                <label>Network</label>
                <p>{{ coverage.network }}</p>
              </div>

              <div class="info-item" v-if="coverage.order">
                <label>Coverage Order</label>
                <p>{{ coverage.order }}</p>
              </div>
            </div>

            <div v-if="getCoverageClass(coverage).length > 0" class="coverage-classes">
              <h4>Plan Details</h4>
              <div class="class-list">
                <div v-for="(classItem, index) in getCoverageClass(coverage)" :key="index" class="class-item">
                  <span class="class-type">{{ classItem.type?.text || classItem.type?.coding?.[0]?.display || 'Class' }}</span>
                  <span class="class-value">{{ classItem.value }}</span>
                  <span v-if="classItem.name" class="class-name">{{ classItem.name }}</span>
                </div>
              </div>
            </div>

            <div v-if="coverage.costToBeneficiary && coverage.costToBeneficiary.length > 0" class="cost-section">
              <h4>Cost Information</h4>
              <div class="cost-list">
                <div v-for="(cost, index) in coverage.costToBeneficiary" :key="index" class="cost-item">
                  <span class="cost-type">
                    {{ cost.type?.text || cost.type?.coding?.[0]?.display || 'Cost' }}
                  </span>
                  <span class="cost-value" v-if="cost.valueMoney">
                    {{ formatMoney(cost.valueMoney.value) }}
                  </span>
                  <span class="cost-value" v-else-if="cost.valueQuantity">
                    {{ formatQuantity(cost.valueQuantity.value, cost.valueQuantity.unit) }}
                  </span>
                </div>
              </div>
            </div>

            <div v-if="coverage.subscriber" class="subscriber-info">
              <h4>Subscriber</h4>
              <p>{{ coverage.subscriber.display || coverage.subscriber.reference || 'N/A' }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.coverage-view {
  max-width: 1000px;
  margin: 0 auto;
}

.no-patient,
.empty-state {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 8px;
  color: #666;
}

.coverage-view h2 {
  color: #333;
  margin-bottom: 1.5rem;
  font-size: 1.8rem;
}

.coverage-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.coverage-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.coverage-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.coverage-header h3 {
  margin: 0;
  font-size: 1.4rem;
}

.coverage-id {
  margin: 0.5rem 0 0 0;
  opacity: 0.9;
  font-size: 0.9rem;
}

.status-badge {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-active {
  background: #4caf50;
}

.status-cancelled {
  background: #f44336;
}

.status-draft {
  background: #ff9800;
}

.status-error {
  background: #e91e63;
}

.status-default {
  background: #9e9e9e;
}

.coverage-body {
  padding: 1.5rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.info-item label {
  display: block;
  font-weight: 600;
  color: #666;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.5rem;
}

.info-item p {
  margin: 0;
  color: #333;
  font-size: 1.05rem;
}

.monospace {
  font-family: monospace;
}

.coverage-classes,
.cost-section,
.subscriber-info {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 2px solid #f0f0f0;
}

.coverage-classes h4,
.cost-section h4,
.subscriber-info h4 {
  color: #667eea;
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
}

.class-list,
.cost-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.class-item,
.cost-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem;
  background: #f9f9f9;
  border-radius: 6px;
}

.class-type,
.cost-type {
  font-weight: 600;
  color: #667eea;
  min-width: 150px;
}

.class-value,
.cost-value {
  font-family: monospace;
  color: #333;
  font-weight: 600;
}

.class-name {
  color: #666;
  font-style: italic;
}

.subscriber-info p {
  margin: 0;
  color: #333;
}
</style>

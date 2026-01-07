<script setup lang="ts">
import { usePatientStore } from '@/stores/patient'
import { computed } from 'vue'

const patientStore = usePatientStore()

const claims = computed(() => patientStore.claims)

function formatDate(dateString: string) {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString()
}

function formatDateTime(dateString: string) {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleString()
}

function formatMoney(money: any) {
  if (!money || !money.value) return 'N/A'
  return `$${money.value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
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

function getUseClass(use: string) {
  const useMap: Record<string, string> = {
    'claim': 'use-claim',
    'preauthorization': 'use-preauth',
    'predetermination': 'use-predet'
  }
  return useMap[use?.toLowerCase()] || 'use-default'
}

function getPriorityClass(priority: string) {
  const code = priority?.toLowerCase()
  if (code === 'stat' || code === 'emergency') return 'priority-high'
  if (code === 'normal') return 'priority-normal'
  return 'priority-default'
}
</script>

<template>
  <div class="claims-view">
    <div v-if="!patientStore.currentPatient" class="no-patient">
      <p>Please select a patient from the home page</p>
    </div>

    <div v-else>
      <h2>My Claims</h2>

      <div v-if="claims.length === 0" class="empty-state">
        <p>No claims found</p>
      </div>

      <div v-else class="claims-list">
        <div v-for="claim in claims" :key="claim.id" class="claim-card">
          <div class="claim-header">
            <div class="header-left">
              <h3>Claim #{{ claim.id }}</h3>
              <div class="header-badges">
                <span class="status-badge" :class="getStatusBadgeClass(claim.status)">
                  {{ claim.status || 'Unknown' }}
                </span>
                <span v-if="claim.use" class="use-badge" :class="getUseClass(claim.use)">
                  {{ claim.use }}
                </span>
                <span v-if="claim.priority" class="priority-badge" :class="getPriorityClass(claim.priority?.coding?.[0]?.code)">
                  {{ claim.priority?.coding?.[0]?.display || claim.priority?.coding?.[0]?.code || 'Normal' }}
                </span>
              </div>
            </div>
            <div class="header-right" v-if="claim.total">
              <div class="total-amount">
                <label>Total</label>
                <p>{{ formatMoney(claim.total) }}</p>
              </div>
            </div>
          </div>

          <div class="claim-body">
            <div class="info-grid">
              <div class="info-item" v-if="claim.created">
                <label>Created</label>
                <p>{{ formatDateTime(claim.created) }}</p>
              </div>

              <div class="info-item" v-if="claim.billablePeriod">
                <label>Billable Period</label>
                <p>
                  {{ formatDate(claim.billablePeriod.start) }}
                  <span v-if="claim.billablePeriod.end"> - {{ formatDate(claim.billablePeriod.end) }}</span>
                </p>
              </div>

              <div class="info-item" v-if="claim.provider">
                <label>Provider</label>
                <p>{{ claim.provider.display || claim.provider.reference || 'N/A' }}</p>
              </div>

              <div class="info-item" v-if="claim.insurer">
                <label>Insurer</label>
                <p>{{ claim.insurer.display || claim.insurer.reference || 'N/A' }}</p>
              </div>

              <div class="info-item" v-if="claim.type">
                <label>Claim Type</label>
                <p>{{ claim.type?.text || claim.type?.coding?.[0]?.display || 'N/A' }}</p>
              </div>

              <div class="info-item" v-if="claim.subType">
                <label>Sub Type</label>
                <p>{{ claim.subType?.text || claim.subType?.coding?.[0]?.display || 'N/A' }}</p>
              </div>

              <div class="info-item" v-if="claim.prescription">
                <label>Prescription</label>
                <p>{{ claim.prescription.display || claim.prescription.reference || 'N/A' }}</p>
              </div>

              <div class="info-item" v-if="claim.facility">
                <label>Facility</label>
                <p>{{ claim.facility.display || claim.facility.reference || 'N/A' }}</p>
              </div>
            </div>

            <div v-if="claim.diagnosis && claim.diagnosis.length > 0" class="diagnosis-section">
              <h4>Diagnoses</h4>
              <div class="diagnosis-list">
                <div v-for="(diag, index) in claim.diagnosis" :key="index" class="diagnosis-item">
                  <span class="diagnosis-sequence">{{ diag.sequence }}</span>
                  <div class="diagnosis-info">
                    <p class="diagnosis-code" v-if="diag.diagnosisCodeableConcept">
                      {{ diag.diagnosisCodeableConcept?.coding?.[0]?.code || 'N/A' }}
                      <span class="diagnosis-display">
                        {{ diag.diagnosisCodeableConcept?.text || diag.diagnosisCodeableConcept?.coding?.[0]?.display || '' }}
                      </span>
                    </p>
                    <p v-if="diag.type && diag.type.length > 0" class="diagnosis-type">
                      Type: {{ diag.type[0]?.coding?.[0]?.display || diag.type[0]?.coding?.[0]?.code || 'N/A' }}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="claim.procedure && claim.procedure.length > 0" class="procedure-section">
              <h4>Procedures</h4>
              <div class="procedure-list">
                <div v-for="(proc, index) in claim.procedure" :key="index" class="procedure-item">
                  <span class="procedure-sequence">{{ proc.sequence }}</span>
                  <div class="procedure-info">
                    <p class="procedure-code" v-if="proc.procedureCodeableConcept">
                      {{ proc.procedureCodeableConcept?.coding?.[0]?.code || 'N/A' }}
                      <span class="procedure-display">
                        {{ proc.procedureCodeableConcept?.text || proc.procedureCodeableConcept?.coding?.[0]?.display || '' }}
                      </span>
                    </p>
                    <p v-if="proc.date" class="procedure-date">
                      Date: {{ formatDateTime(proc.date) }}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="claim.item && claim.item.length > 0" class="items-section">
              <h4>Items</h4>
              <div class="items-table">
                <table>
                  <thead>
                    <tr>
                      <th>Seq</th>
                      <th>Service</th>
                      <th>Quantity</th>
                      <th>Unit Price</th>
                      <th>Net</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in claim.item" :key="item.sequence">
                      <td>{{ item.sequence }}</td>
                      <td>
                        {{ item.productOrService?.text || item.productOrService?.coding?.[0]?.display || 'N/A' }}
                      </td>
                      <td>{{ item.quantity?.value || 1 }}</td>
                      <td>{{ item.unitPrice ? formatMoney(item.unitPrice) : 'N/A' }}</td>
                      <td class="net-amount">{{ item.net ? formatMoney(item.net) : 'N/A' }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div v-if="claim.insurance && claim.insurance.length > 0" class="insurance-section">
              <h4>Insurance</h4>
              <div class="insurance-list">
                <div v-for="(ins, index) in claim.insurance" :key="index" class="insurance-item">
                  <div class="insurance-detail">
                    <span class="insurance-label">Sequence:</span>
                    <span>{{ ins.sequence }}</span>
                  </div>
                  <div class="insurance-detail">
                    <span class="insurance-label">Focal:</span>
                    <span>{{ ins.focal ? 'Yes' : 'No' }}</span>
                  </div>
                  <div class="insurance-detail" v-if="ins.coverage">
                    <span class="insurance-label">Coverage:</span>
                    <span>{{ ins.coverage.display || ins.coverage.reference || 'N/A' }}</span>
                  </div>
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
.claims-view {
  max-width: 1200px;
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

.claims-view h2 {
  color: #333;
  margin-bottom: 1.5rem;
  font-size: 1.8rem;
}

.claims-list {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.claim-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.claim-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left h3 {
  margin: 0 0 0.75rem 0;
  font-size: 1.4rem;
}

.header-badges {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.status-badge,
.use-badge,
.priority-badge {
  padding: 0.35rem 0.75rem;
  border-radius: 15px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-active { background: #4caf50; }
.status-cancelled { background: #f44336; }
.status-draft { background: #ff9800; }
.status-error { background: #e91e63; }
.status-default { background: #9e9e9e; }

.use-claim { background: #2196f3; }
.use-preauth { background: #9c27b0; }
.use-predet { background: #00bcd4; }
.use-default { background: #607d8b; }

.priority-high { background: #ff5722; }
.priority-normal { background: #8bc34a; }
.priority-default { background: #9e9e9e; }

.header-right .total-amount {
  text-align: right;
}

.total-amount label {
  display: block;
  font-size: 0.85rem;
  opacity: 0.9;
  margin-bottom: 0.25rem;
}

.total-amount p {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 700;
}

.claim-body {
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

.diagnosis-section,
.procedure-section,
.items-section,
.insurance-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 2px solid #f0f0f0;
}

.diagnosis-section h4,
.procedure-section h4,
.items-section h4,
.insurance-section h4 {
  color: #667eea;
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
}

.diagnosis-list,
.procedure-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.diagnosis-item,
.procedure-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: #f9f9f9;
  border-radius: 6px;
  border-left: 4px solid #667eea;
}

.diagnosis-sequence,
.procedure-sequence {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  font-weight: 700;
  flex-shrink: 0;
}

.diagnosis-info,
.procedure-info {
  flex: 1;
}

.diagnosis-code,
.procedure-code {
  margin: 0 0 0.5rem 0;
  font-weight: 600;
  color: #333;
}

.diagnosis-display,
.procedure-display {
  margin-left: 0.5rem;
  font-weight: normal;
  color: #666;
}

.diagnosis-type,
.procedure-date {
  margin: 0;
  font-size: 0.9rem;
  color: #666;
}

.items-table {
  overflow-x: auto;
}

.items-table table {
  width: 100%;
  border-collapse: collapse;
}

.items-table th,
.items-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.items-table th {
  background: #f5f5f5;
  font-weight: 600;
  color: #666;
  font-size: 0.9rem;
  text-transform: uppercase;
}

.items-table td {
  color: #333;
}

.net-amount {
  font-weight: 600;
  color: #667eea;
}

.insurance-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.insurance-item {
  padding: 1rem;
  background: #f9f9f9;
  border-radius: 6px;
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
}

.insurance-detail {
  display: flex;
  gap: 0.5rem;
}

.insurance-label {
  font-weight: 600;
  color: #667eea;
}
</style>

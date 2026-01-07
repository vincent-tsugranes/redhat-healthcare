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

function getBeneficiaryPayment(claim: any) {
  if (!claim.extension) return null
  const paymentExt = claim.extension.find((ext: any) =>
    ext.url === 'http://redhat.com/fhir/StructureDefinition/beneficiary-payment'
  )
  return paymentExt?.valueMoney || null
}

function getOOPTracking(claim: any) {
  if (!claim.extension) return null
  const oopExt = claim.extension.find((ext: any) =>
    ext.url === 'http://redhat.com/fhir/StructureDefinition/oop-tracking'
  )
  if (!oopExt?.extension) return null

  const paidToDate = oopExt.extension.find((e: any) => e.url === 'paidToDate')?.valueMoney
  const maximum = oopExt.extension.find((e: any) => e.url === 'maximum')?.valueMoney
  const remaining = oopExt.extension.find((e: any) => e.url === 'remaining')?.valueMoney

  return { paidToDate, maximum, remaining }
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
                <label>Primary Provider</label>
                <p class="provider-name">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" class="provider-icon">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  {{ claim.provider.display || claim.provider.reference || 'N/A' }}
                </p>
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

            <div v-if="getBeneficiaryPayment(claim) || getOOPTracking(claim)" class="payment-section">
              <h4>Patient Payment Information</h4>
              <div class="payment-grid">
                <div v-if="getBeneficiaryPayment(claim)" class="payment-card payment-patient">
                  <div class="payment-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
                    </svg>
                  </div>
                  <div class="payment-info">
                    <label>You Paid</label>
                    <p class="payment-amount">{{ formatMoney(getBeneficiaryPayment(claim)) }}</p>
                    <span class="payment-note">For this claim</span>
                  </div>
                </div>

                <div v-if="getOOPTracking(claim)" class="payment-card payment-oop">
                  <div class="payment-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                  </div>
                  <div class="payment-info">
                    <label>Out-of-Pocket Progress</label>
                    <div class="oop-summary">
                      <div class="oop-stat" v-if="getOOPTracking(claim)?.paidToDate">
                        <span class="oop-label">Spent to Date:</span>
                        <span class="oop-value spent">{{ formatMoney(getOOPTracking(claim)!.paidToDate) }}</span>
                      </div>
                      <div class="oop-stat" v-if="getOOPTracking(claim)?.remaining">
                        <span class="oop-label">Remaining:</span>
                        <span class="oop-value remaining">{{ formatMoney(getOOPTracking(claim)!.remaining) }}</span>
                      </div>
                      <div class="oop-stat" v-if="getOOPTracking(claim)?.maximum">
                        <span class="oop-label">Maximum:</span>
                        <span class="oop-value max">{{ formatMoney(getOOPTracking(claim)!.maximum) }}</span>
                      </div>
                    </div>
                    <div class="oop-progress-bar" v-if="getOOPTracking(claim)?.paidToDate && getOOPTracking(claim)?.maximum">
                      <div
                        class="oop-progress-fill"
                        :style="{ width: Math.min((getOOPTracking(claim)!.paidToDate.value / getOOPTracking(claim)!.maximum.value) * 100, 100) + '%' }"
                      ></div>
                    </div>
                    <span class="oop-percentage" v-if="getOOPTracking(claim)?.paidToDate && getOOPTracking(claim)?.maximum">
                      {{ ((getOOPTracking(claim)!.paidToDate.value / getOOPTracking(claim)!.maximum.value) * 100).toFixed(1) }}% of annual maximum
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="claim.careTeam && claim.careTeam.length > 0" class="careteam-section">
              <h4>Care Team</h4>
              <div class="careteam-list">
                <div v-for="(member, index) in claim.careTeam" :key="index" class="careteam-item">
                  <div class="careteam-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  </div>
                  <div class="careteam-info">
                    <p class="careteam-name">
                      {{ member.provider?.display || member.provider?.reference || 'Unknown Provider' }}
                    </p>
                    <p class="careteam-role" v-if="member.role">
                      {{ member.role?.coding?.[0]?.display || member.role?.text || 'Provider' }}
                    </p>
                    <p class="careteam-qualification" v-if="member.qualification">
                      Qualification: {{ member.qualification?.coding?.[0]?.display || member.qualification?.text }}
                    </p>
                    <span v-if="member.responsible" class="responsible-badge">Responsible</span>
                  </div>
                </div>
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

.provider-name {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #667eea !important;
  font-weight: 600 !important;
}

.provider-icon {
  width: 20px;
  height: 20px;
  color: #667eea;
}

.careteam-section,
.diagnosis-section,
.procedure-section,
.items-section,
.insurance-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 2px solid #f0f0f0;
}

.careteam-section h4,
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

.careteam-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.careteam-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: #f9f9f9;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.careteam-icon {
  width: 48px;
  height: 48px;
  background: #667eea;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.careteam-icon svg {
  width: 28px;
  height: 28px;
  color: white;
}

.careteam-info {
  flex: 1;
}

.careteam-name {
  margin: 0 0 0.5rem 0;
  font-weight: 600;
  color: #333;
  font-size: 1.05rem;
}

.careteam-role {
  margin: 0 0 0.25rem 0;
  color: #667eea;
  font-size: 0.9rem;
  font-weight: 500;
}

.careteam-qualification {
  margin: 0 0 0.5rem 0;
  color: #666;
  font-size: 0.85rem;
}

.responsible-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: #4caf50;
  color: white;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

/* Payment Section */
.payment-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 2px solid #f0f0f0;
}

.payment-section h4 {
  color: #667eea;
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
}

.payment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.payment-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  gap: 1.25rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
}

.payment-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.payment-patient {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-left: 4px solid #2196f3;
}

.payment-oop {
  background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
  border-left: 4px solid #9c27b0;
}

.payment-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.payment-icon svg {
  width: 28px;
  height: 28px;
  color: #667eea;
}

.payment-patient .payment-icon svg {
  color: #2196f3;
}

.payment-oop .payment-icon svg {
  color: #9c27b0;
}

.payment-info {
  flex: 1;
}

.payment-info label {
  display: block;
  font-weight: 600;
  color: #555;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.5rem;
}

.payment-amount {
  margin: 0 0 0.5rem 0;
  font-size: 2rem;
  font-weight: 700;
  color: #2196f3;
  line-height: 1;
}

.payment-note {
  font-size: 0.85rem;
  color: #666;
  font-style: italic;
}

.oop-summary {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin: 0.75rem 0;
}

.oop-stat {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.oop-label {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

.oop-value {
  font-size: 1rem;
  font-weight: 700;
}

.oop-value.spent {
  color: #9c27b0;
}

.oop-value.remaining {
  color: #4caf50;
}

.oop-value.max {
  color: #666;
}

.oop-progress-bar {
  width: 100%;
  height: 10px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 5px;
  overflow: hidden;
  margin: 1rem 0 0.5rem 0;
}

.oop-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #9c27b0 0%, #e91e63 100%);
  border-radius: 5px;
  transition: width 0.3s ease;
}

.oop-percentage {
  font-size: 0.85rem;
  color: #666;
  font-weight: 600;
}
</style>

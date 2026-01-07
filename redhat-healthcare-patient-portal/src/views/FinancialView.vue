<script setup lang="ts">
import { usePatientStore } from '@/stores/patient'
import { computed } from 'vue'
import { Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  type ChartOptions
} from 'chart.js'

// Register Chart.js components
ChartJS.register(ArcElement, Tooltip, Legend)

const patientStore = usePatientStore()
const claims = computed(() => patientStore.claims)
const coverages = computed(() => patientStore.coverages)

// Get out-of-pocket maximum from coverage
const outOfPocketMax = computed(() => {
  if (!coverages.value || coverages.value.length === 0) return null

  const coverage = coverages.value[0] // Use first active coverage
  if (!coverage || !coverage.costToBeneficiary) return null

  const oopMaxEntry = coverage.costToBeneficiary.find((cost: any) =>
    cost.type?.coding?.[0]?.code === 'gpay' ||
    cost.type?.text?.toLowerCase().includes('out of pocket')
  )

  return oopMaxEntry?.valueMoney?.value || null
})

// Calculate total patient payments from claim adjudications
const patientPaymentSummary = computed(() => {
  let totalPatientPaid = 0
  let totalPatientPending = 0
  const currency = 'USD'

  claims.value.forEach(claim => {
    if (!claim.item) return

    const isPaid = claim.status?.toLowerCase() === 'active'
    const isPending = claim.status?.toLowerCase() === 'draft'

    claim.item.forEach((item: any) => {
      if (!item.adjudication) return

      // Find the patient responsibility adjudication (code: 'eligible')
      const patientRespAdj = item.adjudication.find((adj: any) =>
        adj.category?.coding?.[0]?.code === 'eligible'
      )

      if (patientRespAdj?.amount?.value) {
        if (isPaid) {
          totalPatientPaid += patientRespAdj.amount.value
        } else if (isPending) {
          totalPatientPending += patientRespAdj.amount.value
        }
      }
    })
  })

  return {
    totalPaid: totalPatientPaid,
    totalPending: totalPatientPending,
    currency,
    oopMax: outOfPocketMax.value,
    percentageUsed: outOfPocketMax.value
      ? (totalPatientPaid / outOfPocketMax.value) * 100
      : 0,
    remaining: outOfPocketMax.value
      ? outOfPocketMax.value - totalPatientPaid
      : null
  }
})

// Compute financial summary from claims data
const financialSummary = computed(() => {
  const summary = {
    total: 0,
    currency: 'USD',
    byStatus: {} as Record<string, { total: number; count: number; currency: string }>
  }

  claims.value.forEach(claim => {
    // Skip claims without valid total
    if (!claim.total || typeof claim.total.value !== 'number') return

    const amount = claim.total.value
    const currency = claim.total.currency || 'USD'
    const status = (claim.status || 'unknown').toLowerCase()

    // Set currency from first valid claim
    if (!summary.total && currency) {
      summary.currency = currency
    }

    // Aggregate total
    summary.total += amount

    // Aggregate by status
    if (!summary.byStatus[status]) {
      summary.byStatus[status] = { total: 0, count: 0, currency }
    }
    summary.byStatus[status].total += amount
    summary.byStatus[status].count += 1
  })

  return summary
})

// Compute chart data for donut visualization
const chartData = computed(() => {
  const statusData = financialSummary.value.byStatus
  const labels: string[] = []
  const data: number[] = []
  const backgroundColors: string[] = []

  // Status color mapping (matches ClaimsView)
  const statusColors: Record<string, string> = {
    'active': '#4caf50',
    'cancelled': '#f44336',
    'draft': '#ff9800',
    'entered-in-error': '#e91e63'
  }

  Object.entries(statusData).forEach(([status, info]) => {
    labels.push(capitalizeStatus(status))
    data.push(info.total)
    backgroundColors.push(statusColors[status] || '#9e9e9e')
  })

  return {
    labels,
    datasets: [{
      data,
      backgroundColor: backgroundColors,
      borderWidth: 2,
      borderColor: '#fff'
    }]
  }
})

// Chart configuration
const chartOptions: ChartOptions<'doughnut'> = {
  responsive: true,
  maintainAspectRatio: true,
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        padding: 20,
        font: {
          size: 14,
          family: "'Inter', system-ui, sans-serif"
        },
        usePointStyle: true,
        pointStyle: 'circle'
      }
    },
    tooltip: {
      callbacks: {
        label: (context) => {
          const label = context.label || ''
          const value = context.parsed
          const percentage = ((value / financialSummary.value.total) * 100).toFixed(1)
          const formattedValue = `$${value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
          return `${label}: ${formattedValue} (${percentage}%)`
        }
      }
    }
  },
  cutout: '60%'
}

// Helper functions
function formatMoney(value: number, currency: string = 'USD'): string {
  return `$${value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function capitalizeStatus(status: string): string {
  const statusMap: Record<string, string> = {
    'active': 'Paid',
    'draft': 'Pending',
    'cancelled': 'Rejected',
    'entered-in-error': 'Error'
  }
  return statusMap[status?.toLowerCase()] || status
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}

function getStatusBadgeClass(status: string): string {
  const statusMap: Record<string, string> = {
    'active': 'status-active',
    'cancelled': 'status-cancelled',
    'draft': 'status-draft',
    'entered-in-error': 'status-error'
  }
  return statusMap[status?.toLowerCase()] || 'status-default'
}
</script>

<template>
  <div class="financial-view">
    <div v-if="!patientStore.currentPatient" class="no-patient">
      <p>Please select a patient from the home page</p>
    </div>

    <div v-else>
      <h2>Financial Summary</h2>

      <div v-if="claims.length === 0" class="empty-state">
        <p>No financial data available</p>
      </div>

      <div v-else class="financial-content">
        <!-- Metrics Grid -->
        <div class="metrics-grid">
          <!-- Total Amount Card -->
          <div class="metric-card metric-total">
            <div class="metric-header">
              <h3>Total Amount</h3>
            </div>
            <div class="metric-value">
              {{ formatMoney(financialSummary.total, financialSummary.currency) }}
            </div>
            <div class="metric-subtitle">
              Across {{ claims.length }} claim{{ claims.length !== 1 ? 's' : '' }}
            </div>
          </div>

          <!-- Patient Payment / OOP Max Card -->
          <div v-if="patientPaymentSummary.oopMax" class="metric-card metric-oop">
            <div class="metric-header">
              <h3>Out-of-Pocket (Paid)</h3>
            </div>
            <div class="metric-value oop-value">
              {{ formatMoney(patientPaymentSummary.totalPaid, patientPaymentSummary.currency) }}
            </div>
            <div class="metric-subtitle">
              of {{ formatMoney(patientPaymentSummary.oopMax, patientPaymentSummary.currency) }} maximum
            </div>
            <div class="oop-progress">
              <div class="progress-bar">
                <div
                  class="progress-fill"
                  :style="{ width: Math.min(patientPaymentSummary.percentageUsed, 100) + '%' }"
                ></div>
              </div>
              <div class="progress-text">
                {{ patientPaymentSummary.percentageUsed.toFixed(1) }}% used
                <span v-if="patientPaymentSummary.remaining && patientPaymentSummary.remaining > 0" class="remaining">
                  ({{ formatMoney(patientPaymentSummary.remaining, patientPaymentSummary.currency) }} remaining)
                </span>
              </div>
            </div>
          </div>

          <!-- Pending Patient Payment Card -->
          <div v-if="patientPaymentSummary.totalPending > 0" class="metric-card metric-pending">
            <div class="metric-header">
              <h3>Pending Payment</h3>
            </div>
            <div class="metric-value pending-value">
              {{ formatMoney(patientPaymentSummary.totalPending, patientPaymentSummary.currency) }}
            </div>
            <div class="metric-subtitle">
              Not yet counted toward OOP max
            </div>
          </div>

          <!-- Status Breakdown Cards -->
          <div
            v-for="(info, status) in financialSummary.byStatus"
            :key="status"
            class="metric-card"
            :class="`metric-${status}`"
          >
            <div class="metric-header">
              <h3>{{ capitalizeStatus(status) }}</h3>
              <span class="status-badge" :class="getStatusBadgeClass(status)">
                {{ info.count }}
              </span>
            </div>
            <div class="metric-value">
              {{ formatMoney(info.total, info.currency) }}
            </div>
            <div class="metric-subtitle">
              {{ ((info.total / financialSummary.total) * 100).toFixed(1) }}% of total
            </div>
          </div>
        </div>

        <!-- Chart Section -->
        <div class="chart-section">
          <div class="chart-card">
            <div class="chart-header">
              <h3>Claims by Status</h3>
              <p>Visual breakdown of claim amounts</p>
            </div>
            <div class="chart-container">
              <Doughnut
                v-if="chartData.labels.length > 0"
                :data="chartData"
                :options="chartOptions"
              />
              <div v-else class="no-data">
                <p>No claim data available for visualization</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.financial-view {
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

.financial-view h2 {
  color: #333;
  margin-bottom: 1.5rem;
  font-size: 1.8rem;
}

.financial-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* Metrics Grid */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.metric-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #f0f0f0;
}

.metric-header h3 {
  color: #667eea;
  font-size: 1rem;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
}

.metric-value {
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.5rem;
  line-height: 1;
}

.metric-subtitle {
  color: #666;
  font-size: 0.9rem;
}

/* Status badges */
.status-badge {
  padding: 0.35rem 0.75rem;
  border-radius: 15px;
  font-size: 0.75rem;
  font-weight: 600;
  color: white;
}

.status-active { background: #4caf50; }
.status-cancelled { background: #f44336; }
.status-draft { background: #ff9800; }
.status-error { background: #e91e63; }
.status-default { background: #9e9e9e; }

/* Out-of-pocket card */
.metric-oop {
  border-left: 4px solid #667eea;
}

.oop-value {
  color: #667eea;
}

/* Pending card */
.metric-pending {
  border-left: 4px solid #ff9800;
}

.pending-value {
  color: #ff9800;
}

.oop-progress {
  margin-top: 1.5rem;
}

.progress-bar {
  width: 100%;
  height: 12px;
  background: #e0e0e0;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 0.75rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
  border-radius: 6px;
}

.progress-text {
  font-size: 0.9rem;
  color: #666;
  font-weight: 600;
}

.progress-text .remaining {
  color: #4caf50;
  font-weight: 500;
  margin-left: 0.5rem;
}

/* Chart section */
.chart-section {
  width: 100%;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-header {
  text-align: center;
  margin-bottom: 2rem;
}

.chart-header h3 {
  color: #333;
  font-size: 1.4rem;
  margin: 0 0 0.5rem 0;
}

.chart-header p {
  color: #666;
  margin: 0;
  font-size: 0.95rem;
}

.chart-container {
  max-width: 500px;
  margin: 0 auto;
  padding: 1rem;
}

.no-data {
  text-align: center;
  padding: 3rem;
  color: #999;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .metric-value {
    font-size: 2rem;
  }
}
</style>

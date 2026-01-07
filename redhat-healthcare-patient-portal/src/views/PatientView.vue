<script setup lang="ts">
import { usePatientStore } from '@/stores/patient'
import { computed } from 'vue'

const patientStore = usePatientStore()

const patient = computed(() => patientStore.currentPatient)

function formatAddress(address: any) {
  if (!address) return 'N/A'
  const parts = []
  if (address.line) parts.push(...address.line)
  if (address.city) parts.push(address.city)
  if (address.state) parts.push(address.state)
  if (address.postalCode) parts.push(address.postalCode)
  if (address.country) parts.push(address.country)
  return parts.join(', ') || 'N/A'
}

function formatTelecom(telecom: any[]) {
  if (!telecom || telecom.length === 0) return 'N/A'
  return telecom.map(t => `${t.system}: ${t.value}`).join(', ')
}

function formatDate(dateString: string) {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString()
}
</script>

<template>
  <div class="patient-view">
    <div v-if="!patient" class="no-patient">
      <p>Please select a patient from the home page</p>
    </div>

    <div v-else class="patient-details">
      <h2>My Information</h2>

      <div class="info-card">
        <h3>Personal Details</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>Full Name</label>
            <p>{{ patientStore.patientName }}</p>
          </div>
          <div class="info-item">
            <label>Gender</label>
            <p>{{ patient.gender || 'N/A' }}</p>
          </div>
          <div class="info-item">
            <label>Birth Date</label>
            <p>{{ formatDate(patient.birthDate) }}</p>
          </div>
          <div class="info-item">
            <label>Marital Status</label>
            <p>{{ patient.maritalStatus?.text || patient.maritalStatus?.coding?.[0]?.display || 'N/A' }}</p>
          </div>
        </div>
      </div>

      <div class="info-card" v-if="patient.identifier && patient.identifier.length > 0">
        <h3>Identifiers</h3>
        <div class="identifier-list">
          <div v-for="(identifier, index) in patient.identifier" :key="index" class="identifier-item">
            <span class="identifier-system">{{ identifier.system || 'Unknown System' }}</span>
            <span class="identifier-value">{{ identifier.value }}</span>
          </div>
        </div>
      </div>

      <div class="info-card" v-if="patient.address && patient.address.length > 0">
        <h3>Address</h3>
        <div v-for="(address, index) in patient.address" :key="index" class="address-item">
          <p><strong>{{ address.use || 'Primary' }}</strong></p>
          <p>{{ formatAddress(address) }}</p>
        </div>
      </div>

      <div class="info-card" v-if="patient.telecom && patient.telecom.length > 0">
        <h3>Contact Information</h3>
        <div class="contact-list">
          <div v-for="(telecom, index) in patient.telecom" :key="index" class="contact-item">
            <span class="contact-type">{{ telecom.system }}</span>
            <span class="contact-value">{{ telecom.value }}</span>
          </div>
        </div>
      </div>

      <div class="info-card" v-if="patient.communication && patient.communication.length > 0">
        <h3>Languages</h3>
        <div class="language-list">
          <span v-for="(comm, index) in patient.communication" :key="index" class="language-tag">
            {{ comm.language?.text || comm.language?.coding?.[0]?.display || 'Unknown' }}
            <span v-if="comm.preferred" class="preferred-badge">Preferred</span>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.patient-view {
  max-width: 1000px;
  margin: 0 auto;
}

.no-patient {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 8px;
  color: #666;
}

.patient-details h2 {
  color: #333;
  margin-bottom: 1.5rem;
  font-size: 1.8rem;
}

.info-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.info-card h3 {
  color: #667eea;
  margin: 0 0 1rem 0;
  font-size: 1.2rem;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0.5rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
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
  font-size: 1.1rem;
}

.identifier-list,
.contact-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.identifier-item,
.contact-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: #f9f9f9;
  border-radius: 6px;
}

.identifier-system,
.contact-type {
  font-weight: 600;
  color: #667eea;
  font-size: 0.9rem;
}

.identifier-value,
.contact-value {
  color: #333;
  font-family: monospace;
}

.address-item {
  padding: 0.75rem;
  background: #f9f9f9;
  border-radius: 6px;
  margin-bottom: 0.75rem;
}

.address-item:last-child {
  margin-bottom: 0;
}

.address-item p {
  margin: 0.25rem 0;
  color: #333;
}

.language-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.language-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: #f0f0f0;
  border-radius: 20px;
  color: #333;
  font-size: 0.9rem;
}

.preferred-badge {
  background: #667eea;
  color: white;
  padding: 0.15rem 0.5rem;
  border-radius: 10px;
  font-size: 0.75rem;
}
</style>

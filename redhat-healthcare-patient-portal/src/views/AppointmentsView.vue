<script setup lang="ts">
import { usePatientStore } from '@/stores/patient'
import { computed, ref } from 'vue'
import type { FhirResource } from '@/services/fhirService'

const patientStore = usePatientStore()
const activeTab = ref<'upcoming' | 'past'>('upcoming')

// Separate appointments into upcoming and past
const upcomingAppointments = computed(() => {
  const now = new Date()
  return patientStore.appointments
    .filter((appt: FhirResource) => {
      const startTime = appt.start ? new Date(appt.start) : null
      return startTime && startTime >= now
    })
    .sort((a: FhirResource, b: FhirResource) => {
      const dateA = new Date(a.start).getTime()
      const dateB = new Date(b.start).getTime()
      return dateA - dateB
    })
})

const pastAppointments = computed(() => {
  const now = new Date()
  return patientStore.appointments
    .filter((appt: FhirResource) => {
      const startTime = appt.start ? new Date(appt.start) : null
      return startTime && startTime < now
    })
    .sort((a: FhirResource, b: FhirResource) => {
      const dateA = new Date(a.start).getTime()
      const dateB = new Date(b.start).getTime()
      return dateB - dateA // Most recent first
    })
})

function formatDateTime(dateString: string): string {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleString('en-US', {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
  })
}

function formatDate(dateString: string): string {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

function formatTime(dateString: string): string {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleTimeString('en-US', {
    hour: 'numeric',
    minute: '2-digit'
  })
}

function getServiceType(appointment: FhirResource): string {
  return appointment.serviceType?.[0]?.coding?.[0]?.display ||
         appointment.serviceType?.[0]?.text ||
         appointment.appointmentType?.coding?.[0]?.display ||
         appointment.appointmentType?.text ||
         'General Appointment'
}

function getSpecialty(appointment: FhirResource): string {
  return appointment.specialty?.[0]?.coding?.[0]?.display ||
         appointment.specialty?.[0]?.text ||
         'Not specified'
}

function getPractitionerName(appointment: FhirResource): string {
  const participant = appointment.participant?.find((p: any) =>
    p.actor?.reference?.startsWith('Practitioner/')
  )
  return participant?.actor?.display || 'Not assigned'
}

function getLocation(appointment: FhirResource): string {
  const participant = appointment.participant?.find((p: any) =>
    p.actor?.reference?.startsWith('Location/')
  )
  return participant?.actor?.display || 'Not specified'
}

function getReason(appointment: FhirResource): string {
  return appointment.reasonCode?.[0]?.coding?.[0]?.display ||
         appointment.reasonCode?.[0]?.text ||
         appointment.description ||
         'No reason specified'
}

function getStatusClass(status: string): string {
  const statusMap: Record<string, string> = {
    'booked': 'status-booked',
    'pending': 'status-pending',
    'proposed': 'status-proposed',
    'arrived': 'status-arrived',
    'fulfilled': 'status-fulfilled',
    'cancelled': 'status-cancelled',
    'noshow': 'status-noshow',
    'entered-in-error': 'status-error',
    'checked-in': 'status-checked-in',
    'waitlist': 'status-waitlist'
  }
  return statusMap[status?.toLowerCase()] || 'status-default'
}

function getStatusLabel(status: string): string {
  const statusMap: Record<string, string> = {
    'booked': 'Confirmed',
    'pending': 'Pending',
    'proposed': 'Proposed',
    'arrived': 'Arrived',
    'fulfilled': 'Completed',
    'cancelled': 'Cancelled',
    'noshow': 'No Show',
    'entered-in-error': 'Error',
    'checked-in': 'Checked In',
    'waitlist': 'Waitlist'
  }
  return statusMap[status?.toLowerCase()] || status
}

function getDuration(appointment: FhirResource): string {
  if (appointment.minutesDuration) {
    const minutes = appointment.minutesDuration
    if (minutes >= 60) {
      const hours = Math.floor(minutes / 60)
      const mins = minutes % 60
      return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`
    }
    return `${minutes}m`
  }
  if (appointment.start && appointment.end) {
    const start = new Date(appointment.start)
    const end = new Date(appointment.end)
    const diffMs = end.getTime() - start.getTime()
    const diffMins = Math.round(diffMs / 60000)
    if (diffMins >= 60) {
      const hours = Math.floor(diffMins / 60)
      const mins = diffMins % 60
      return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`
    }
    return `${diffMins}m`
  }
  return 'Duration not specified'
}
</script>

<template>
  <div class="appointments-view">
    <div v-if="!patientStore.currentPatient" class="no-patient">
      <p>Please select a patient from the home page</p>
    </div>

    <div v-else>
      <h2>My Appointments</h2>

      <div v-if="patientStore.appointments.length === 0" class="empty-state">
        <p>No appointments found</p>
      </div>

      <div v-else class="appointments-content">
        <!-- Tab Navigation -->
        <div class="tabs">
          <button
            :class="['tab', { active: activeTab === 'upcoming' }]"
            @click="activeTab = 'upcoming'"
          >
            Upcoming ({{ upcomingAppointments.length }})
          </button>
          <button
            :class="['tab', { active: activeTab === 'past' }]"
            @click="activeTab = 'past'"
          >
            Past ({{ pastAppointments.length }})
          </button>
        </div>

        <!-- Upcoming Appointments -->
        <div v-if="activeTab === 'upcoming'" class="appointments-list">
          <div v-if="upcomingAppointments.length === 0" class="empty-tab">
            <p>No upcoming appointments</p>
          </div>
          <div
            v-for="appointment in upcomingAppointments"
            :key="appointment.id"
            class="appointment-card"
          >
            <div class="card-header">
              <div class="appointment-icon">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <div class="appointment-info">
                <h3>{{ getServiceType(appointment) }}</h3>
                <p class="date-time">{{ formatDateTime(appointment.start) }}</p>
              </div>
              <span :class="['status-badge', getStatusClass(appointment.status)]">
                {{ getStatusLabel(appointment.status) }}
              </span>
            </div>

            <div class="card-body">
              <div class="info-row">
                <span class="label">Provider:</span>
                <span class="value">{{ getPractitionerName(appointment) }}</span>
              </div>

              <div class="info-row">
                <span class="label">Specialty:</span>
                <span class="value">{{ getSpecialty(appointment) }}</span>
              </div>

              <div class="info-row">
                <span class="label">Location:</span>
                <span class="value">{{ getLocation(appointment) }}</span>
              </div>

              <div class="info-row">
                <span class="label">Duration:</span>
                <span class="value">{{ getDuration(appointment) }}</span>
              </div>

              <div v-if="getReason(appointment) !== 'No reason specified'" class="info-row full-width">
                <span class="label">Reason:</span>
                <span class="value">{{ getReason(appointment) }}</span>
              </div>

              <div v-if="appointment.comment" class="info-row full-width">
                <span class="label">Notes:</span>
                <span class="value">{{ appointment.comment }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Past Appointments -->
        <div v-if="activeTab === 'past'" class="appointments-list">
          <div v-if="pastAppointments.length === 0" class="empty-tab">
            <p>No past appointments</p>
          </div>
          <div
            v-for="appointment in pastAppointments"
            :key="appointment.id"
            class="appointment-card past"
          >
            <div class="card-header">
              <div class="appointment-icon">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <div class="appointment-info">
                <h3>{{ getServiceType(appointment) }}</h3>
                <p class="date-time">{{ formatDateTime(appointment.start) }}</p>
              </div>
              <span :class="['status-badge', getStatusClass(appointment.status)]">
                {{ getStatusLabel(appointment.status) }}
              </span>
            </div>

            <div class="card-body">
              <div class="info-row">
                <span class="label">Provider:</span>
                <span class="value">{{ getPractitionerName(appointment) }}</span>
              </div>

              <div class="info-row">
                <span class="label">Specialty:</span>
                <span class="value">{{ getSpecialty(appointment) }}</span>
              </div>

              <div class="info-row">
                <span class="label">Location:</span>
                <span class="value">{{ getLocation(appointment) }}</span>
              </div>

              <div class="info-row">
                <span class="label">Duration:</span>
                <span class="value">{{ getDuration(appointment) }}</span>
              </div>

              <div v-if="getReason(appointment) !== 'No reason specified'" class="info-row full-width">
                <span class="label">Reason:</span>
                <span class="value">{{ getReason(appointment) }}</span>
              </div>

              <div v-if="appointment.comment" class="info-row full-width">
                <span class="label">Notes:</span>
                <span class="value">{{ appointment.comment }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.appointments-view {
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

h2 {
  color: #333;
  margin-bottom: 1.5rem;
  font-size: 1.8rem;
}

.appointments-content {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tabs {
  display: flex;
  border-bottom: 2px solid #f0f0f0;
  background: #fafafa;
}

.tab {
  flex: 1;
  padding: 1rem 1.5rem;
  background: transparent;
  border: none;
  font-size: 1rem;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 3px solid transparent;
}

.tab:hover {
  background: #f5f5f5;
  color: #667eea;
}

.tab.active {
  color: #667eea;
  background: white;
  border-bottom-color: #667eea;
}

.appointments-list {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.empty-tab {
  text-align: center;
  padding: 3rem;
  color: #999;
}

.appointment-card {
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.appointment-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #667eea;
}

.appointment-card.past {
  opacity: 0.9;
}

.card-header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 1.5rem;
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.appointment-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.appointment-icon svg {
  width: 28px;
  height: 28px;
}

.appointment-info {
  flex: 1;
}

.appointment-info h3 {
  margin: 0 0 0.5rem;
  font-size: 1.2rem;
}

.date-time {
  margin: 0;
  opacity: 0.9;
  font-size: 0.95rem;
}

.status-badge {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  flex-shrink: 0;
}

.status-booked,
.status-checked-in {
  background: #4caf50;
}

.status-pending,
.status-proposed,
.status-waitlist {
  background: #ff9800;
}

.status-arrived {
  background: #2196f3;
}

.status-fulfilled {
  background: #9e9e9e;
}

.status-cancelled,
.status-noshow {
  background: #f44336;
}

.status-error {
  background: #e91e63;
}

.status-default {
  background: #9e9e9e;
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
</style>

<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { usePatientStore } from '@/stores/patient'
import { onMounted } from 'vue'

const patientStore = usePatientStore()

onMounted(() => {
  patientStore.loadAllPatients()
})
</script>

<template>
  <div class="app">
    <header class="header">
      <div class="container">
        <h1 class="title">Healthcare Patient Portal</h1>
        <p class="subtitle" v-if="patientStore.patientName">
          Welcome, {{ patientStore.patientName }}
        </p>
      </div>
    </header>

    <nav class="nav">
      <div class="container">
        <RouterLink to="/" class="nav-link">Home</RouterLink>
        <RouterLink v-if="patientStore.currentPatient" to="/patient" class="nav-link">My Information</RouterLink>
        <RouterLink v-if="patientStore.currentPatient" to="/coverage" class="nav-link">My Coverage</RouterLink>
        <RouterLink v-if="patientStore.currentPatient" to="/claims" class="nav-link">My Claims</RouterLink>
        <RouterLink v-if="patientStore.currentPatient" to="/financial" class="nav-link">Financial Summary</RouterLink>
      </div>
    </nav>

    <main class="main">
      <div class="container">
        <div v-if="patientStore.error" class="error-message">
          {{ patientStore.error }}
          <button @click="patientStore.clearError" class="btn-close">×</button>
        </div>

        <div v-if="patientStore.loading" class="loading">
          Loading...
        </div>

        <RouterView v-else />
      </div>
    </main>

    <footer class="footer">
      <div class="container">
        <p>&copy; 2024 Red Hat Healthcare - Patient Portal</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.container {
  width: 100%;
  margin: 0 auto;
  padding: 0 1.5rem;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
}

.subtitle {
  margin: 0.25rem 0 0 0;
  font-size: 1rem;
  opacity: 0.9;
}

.nav {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  padding: 0;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.nav .container {
  display: flex;
  gap: 0;
}

.nav-link {
  padding: 0.75rem 1.25rem;
  text-decoration: none;
  color: #333;
  font-weight: 500;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
}

.nav-link:hover {
  background: #f5f5f5;
  color: #667eea;
}

.nav-link.router-link-exact-active {
  color: #667eea;
  border-bottom-color: #667eea;
  background: #f9f9ff;
}

.main {
  flex: 1;
  padding: 1.25rem 0;
  background: #f5f5f5;
}

.error-message {
  background: #fee;
  border: 1px solid #fcc;
  color: #c33;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #c33;
  cursor: pointer;
  padding: 0;
  width: 2rem;
  height: 2rem;
}

.loading {
  text-align: center;
  padding: 3rem;
  font-size: 1.2rem;
  color: #667eea;
}

.footer {
  background: #333;
  color: white;
  padding: 1.5rem 0;
  text-align: center;
  margin-top: auto;
}

.footer p {
  margin: 0;
}
</style>

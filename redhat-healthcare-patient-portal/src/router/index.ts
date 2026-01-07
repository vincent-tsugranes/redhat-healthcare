import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/patient',
      name: 'patient',
      component: () => import('../views/PatientView.vue'),
    },
    {
      path: '/coverage',
      name: 'coverage',
      component: () => import('../views/CoverageView.vue'),
    },
    {
      path: '/claims',
      name: 'claims',
      component: () => import('../views/ClaimsView.vue'),
    },
    {
      path: '/financial',
      name: 'financial',
      component: () => import('../views/FinancialView.vue'),
    },
  ],
})

export default router

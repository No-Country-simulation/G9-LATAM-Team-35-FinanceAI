import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: LoginView,
      props: { initialTab: 'register' }
    },
    {
      path: '/evaluacion',
      name: 'evaluacion',
      component: () => import('../views/GuestAnalysisView.vue')
    },
    {
      path: '/guest/transacciones',
      name: 'guest-transacciones',
      component: () => import('../views/GuestTransaccionesView.vue')
    },
    {
      path: '/guest/analisis',
      name: 'guest-analisis',
      component: () => import('../views/GuestAnalisisDetalleView.vue')
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue')
    },
    {
      path: '/transacciones',
      name: 'transacciones',
      component: () => import('../views/TransaccionesView.vue')
    },
    {
      path: '/analisis',
      name: 'analisis',
      component: () => import('../views/AnalisisView.vue')
    },
    {
      path: '/historial',
      name: 'historial',
      component: () => import('../views/HistorialView.vue')
    },
    {
      path: '/configuracion',
      name: 'configuracion',
      component: () => import('../views/ConfiguracionView.vue')
    }
  ]
})

export default router

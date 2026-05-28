import { createRouter, createWebHistory } from 'vue-router'
import keycloak from '@/services/keycloak'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@/views/SettingsView.vue'),
    },
    {
      path: '/profile-settings',
      name: 'profile-settings',
      component: () => import('@/views/ProfileSettingsView.vue'),
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/AdminView.vue'),
      meta: { requiresAdmin: true },
    },
  ],
})

router.beforeEach((to, _, next) => {
  if (to.meta.requiresAdmin && !keycloak.hasRealmRole('admin')) {
    next('/')
  } else {
    next()
  }
})

export { router }

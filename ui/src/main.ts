import { createApp } from 'vue'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'
import Aura from '@primeuix/themes/aura'
import Tooltip from 'primevue/tooltip'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'

import App from './App.vue'
import { router } from './router'
import { useUiStore } from './stores/ui'
import { useDashboardStore } from './stores/dashboard'
import { useAuthStore } from './stores/auth'
import { usePomodoroStore } from './stores/pomodoro'
import keycloak from './services/keycloak'

import './assets/main.css'
import 'primeicons/primeicons.css'
import 'primeflex/primeflex.css'

keycloak
  .init({ onLoad: 'login-required', pkceMethod: 'S256', checkLoginIframe: false })
  .then(async authenticated => {
    if (!authenticated) return

    const app = createApp(App)
    const pinia = createPinia()
    app.use(pinia)
    app.use(router)
    app.use(PrimeVue, {
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: '.app-dark',
          cssLayer: { name: 'primevue', order: 'tailwind-base, primevue, tailwind-utilities' },
        },
      },
    })
    app.use(ToastService)
    app.use(ConfirmationService)
    app.directive('tooltip', Tooltip)

    const uiStore = useUiStore()
    uiStore.initTheme()

    const authStore = useAuthStore()
    await authStore.initAuth()

    const dashboardStore = useDashboardStore()
    const pomodoroStore = usePomodoroStore()

    await Promise.allSettled([
      dashboardStore.initializeStore(),
      pomodoroStore.initPomodoro(),
    ])

    app.mount('#app')

    // Token automatisch erneuern
    keycloak.onTokenExpired = () => {
      keycloak.updateToken(30).catch(() => keycloak.login())
    }
  })
  .catch(() => {
    console.error('Keycloak Initialisierung fehlgeschlagen')
  })

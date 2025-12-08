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

import './assets/main.css'
import 'primeicons/primeicons.css'
import 'primeflex/primeflex.css'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)

app.use(router)

app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
      darkModeSelector: '.app-dark',
      cssLayer: {
        name: 'primevue',
        order: 'tailwind-base, primevue, tailwind-utilities',
      },
    },
  },
})

app.use(ToastService)
app.use(ConfirmationService)

app.directive('tooltip', Tooltip)

const uiStore = useUiStore()
uiStore.initTheme()

const dashboardStore = useDashboardStore()
dashboardStore.initializeStore()

const authStore = useAuthStore()
authStore.initAuth()

app.mount('#app')

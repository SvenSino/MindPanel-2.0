<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import MindPanelLogo from '@/components/MindPanelLogo.vue'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const username = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!username.value.trim() || !password.value.trim()) {
    toast.add({
      severity: 'warn',
      summary: 'Fehlende Angaben',
      detail: 'Bitte Benutzername und Passwort eingeben',
      life: 3000,
    })
    return
  }

  loading.value = true

  try {
    const success = authStore.login(username.value, password.value)

    if (success) {
      toast.add({
        severity: 'success',
        summary: 'Erfolgreich',
        detail: 'Willkommen zurück!',
        life: 3000,
      })
      router.push('/')
    } else {
      toast.add({
        severity: 'error',
        summary: 'Login fehlgeschlagen',
        detail: 'Benutzername oder Passwort falsch',
        life: 3000,
      })
    }
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: 'Fehler',
      detail: 'Ein Fehler ist aufgetreten',
      life: 3000,
    })
  } finally {
    loading.value = false
  }
}

function goToRegister() {
  router.push('/register')
}
</script>

<template>
  <div class="login-view">
    <div class="login-container">
      <Card class="login-card">
        <template #header>
          <div class="text-center pt-5 pb-3">
            <div class="flex justify-content-center mb-3">
              <MindPanelLogo size="large" />
            </div>
            <p class="text-color-secondary mt-2 m-0">Willkommen zurück</p>
          </div>
        </template>

        <template #content>
          <form @submit.prevent="handleLogin" class="flex flex-column gap-4">
            <div class="surface-50 border-round p-3 mb-2">
              <div class="text-sm font-semibold mb-2">Demo-Zugänge:</div>
              <div class="text-xs text-color-secondary">
                <div class="mb-1"><strong>Admin:</strong> admin / admin123</div>
                <div><strong>Demo:</strong> demo / demo123</div>
              </div>
            </div>

            <div class="flex flex-column gap-2">
              <label for="username" class="font-semibold">Benutzername</label>
              <InputText
                id="username"
                v-model="username"
                placeholder="admin oder demo"
                autocomplete="username"
                :disabled="loading"
              />
            </div>

            <div class="flex flex-column gap-2">
              <label for="password" class="font-semibold">Passwort</label>
              <Password
                id="password"
                v-model="password"
                placeholder="Passwort"
                :feedback="false"
                toggleMask
                autocomplete="current-password"
                :disabled="loading"
              />
            </div>

            <Button
              type="submit"
              label="Anmelden"
              icon="pi pi-sign-in"
              :loading="loading"
              class="w-full"
            />

            <div class="text-center">
              <p class="text-color-secondary m-0">
                Noch kein Konto?
                <Button
                  label="Registrieren"
                  link
                  @click="goToRegister"
                  class="p-0 ml-1"
                />
              </p>
            </div>
          </form>
        </template>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.login-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--surface-ground);
  padding: 1rem;
}

.login-container {
  width: 100%;
  max-width: 450px;
}

.login-card {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}
</style>

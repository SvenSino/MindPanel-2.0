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
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)

async function handleRegister() {
  if (!username.value.trim() || !email.value.trim() || !password.value.trim()) {
    toast.add({
      severity: 'warn',
      summary: 'Fehlende Angaben',
      detail: 'Bitte alle Felder ausfüllen',
      life: 3000,
    })
    return
  }

  if (password.value !== confirmPassword.value) {
    toast.add({
      severity: 'warn',
      summary: 'Passwörter stimmen nicht überein',
      detail: 'Bitte überprüfe deine Passwörter',
      life: 3000,
    })
    return
  }

  if (password.value.length < 6) {
    toast.add({
      severity: 'warn',
      summary: 'Passwort zu kurz',
      detail: 'Passwort muss mindestens 6 Zeichen lang sein',
      life: 3000,
    })
    return
  }

  loading.value = true

  try {
    const success = authStore.register(username.value, email.value, password.value)

    if (success) {
      toast.add({
        severity: 'success',
        summary: 'Erfolgreich',
        detail: 'Account erstellt! Willkommen bei MindPanel!',
        life: 3000,
      })
      router.push('/')
    }
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: 'Fehler',
      detail: 'Registrierung fehlgeschlagen',
      life: 3000,
    })
  } finally {
    loading.value = false
  }
}

function goToLogin() {
  router.push('/login')
}
</script>

<template>
  <div class="register-view">
    <div class="register-container">
      <Card class="register-card">
        <template #header>
          <div class="text-center pt-5 pb-3">
            <div class="flex justify-content-center mb-3">
              <MindPanelLogo size="large" />
            </div>
            <p class="text-color-secondary mt-2 m-0">Erstelle deinen Account</p>
          </div>
        </template>

        <template #content>
          <form @submit.prevent="handleRegister" class="flex flex-column gap-4">
            <div class="flex flex-column gap-2">
              <label for="username" class="font-semibold">Benutzername</label>
              <InputText
                id="username"
                v-model="username"
                placeholder="Dein Benutzername"
                autocomplete="username"
                :disabled="loading"
              />
            </div>

            <div class="flex flex-column gap-2">
              <label for="email" class="font-semibold">E-Mail</label>
              <InputText
                id="email"
                v-model="email"
                type="email"
                placeholder="deine@email.de"
                autocomplete="email"
                :disabled="loading"
              />
            </div>

            <div class="flex flex-column gap-2">
              <label for="password" class="font-semibold">Passwort</label>
              <Password
                id="password"
                v-model="password"
                placeholder="Mindestens 6 Zeichen"
                toggleMask
                autocomplete="new-password"
                :disabled="loading"
              />
            </div>

            <div class="flex flex-column gap-2">
              <label for="confirmPassword" class="font-semibold">Passwort bestätigen</label>
              <Password
                id="confirmPassword"
                v-model="confirmPassword"
                placeholder="Passwort wiederholen"
                :feedback="false"
                toggleMask
                autocomplete="new-password"
                :disabled="loading"
              />
            </div>

            <Button
              type="submit"
              label="Registrieren"
              icon="pi pi-user-plus"
              :loading="loading"
              class="w-full"
            />

            <div class="text-center">
              <p class="text-color-secondary m-0">
                Bereits registriert?
                <Button
                  label="Anmelden"
                  link
                  @click="goToLogin"
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
.register-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--surface-ground);
  padding: 1rem;
}

.register-container {
  width: 100%;
  max-width: 450px;
}

.register-card {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}
</style>

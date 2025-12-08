<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'
import AppShell from '@/components/layout/AppShell.vue'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import Avatar from 'primevue/avatar'
import FileUpload from 'primevue/fileupload'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const username = ref(authStore.user?.username || '')
const email = ref(authStore.user?.email || '')
const firstName = ref(authStore.user?.firstName || '')
const lastName = ref(authStore.user?.lastName || '')
const street = ref(authStore.user?.street || '')
const zipCode = ref(authStore.user?.zipCode || '')
const city = ref(authStore.user?.city || '')
const country = ref(authStore.user?.country || '')
const avatarPreview = ref(authStore.user?.avatar || null)
const loading = ref(false)

const initials = computed(() => {
  const first = firstName.value?.charAt(0) || ''
  const last = lastName.value?.charAt(0) || ''
  if (first && last) return (first + last).toUpperCase()
  if (first) return first.toUpperCase()
  if (last) return last.toUpperCase()
  return username.value?.substring(0, 2).toUpperCase() || 'U'
})

function handleAvatarUpload(event: any) {
  const file = event.files[0]
  if (!file) return

  // Check file size (max 5MB)
  if (file.size > 5 * 1024 * 1024) {
    toast.add({
      severity: 'warn',
      summary: 'Datei zu groß',
      detail: 'Bitte wähle ein Bild unter 5MB',
      life: 3000,
    })
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target?.result as string
    authStore.updateAvatar(avatarPreview.value)
  }
  reader.readAsDataURL(file)
}

function removeAvatar() {
  avatarPreview.value = null
  authStore.updateAvatar('')
}

function handleSave() {
  if (!authStore.user) return

  if (!username.value.trim() || !email.value.trim()) {
    toast.add({
      severity: 'warn',
      summary: 'Fehlende Angaben',
      detail: 'Bitte fülle alle Felder aus',
      life: 3000,
    })
    return
  }

  loading.value = true

  try {
    authStore.updateProfile({
      username: username.value.trim(),
      email: email.value.trim(),
      firstName: firstName.value.trim(),
      lastName: lastName.value.trim(),
      street: street.value.trim(),
      zipCode: zipCode.value.trim(),
      city: city.value.trim(),
      country: country.value.trim(),
      avatar: avatarPreview.value,
    })

    toast.add({
      severity: 'success',
      summary: 'Gespeichert',
      detail: 'Profil erfolgreich aktualisiert',
      life: 3000,
    })
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: 'Fehler',
      detail: 'Profil konnte nicht gespeichert werden',
      life: 3000,
    })
  } finally {
    loading.value = false
  }
}

watch(() => authStore.user, (newUser) => {
  if (newUser) {
    username.value = newUser.username || ''
    email.value = newUser.email || ''
    firstName.value = newUser.firstName || ''
    lastName.value = newUser.lastName || ''
    street.value = newUser.street || ''
    zipCode.value = newUser.zipCode || ''
    city.value = newUser.city || ''
    country.value = newUser.country || ''
    avatarPreview.value = newUser.avatar || null
  }
}, { immediate: true })

</script>

<template>
  <AppShell>
    <div class="profile-settings">
      <Card>
        <template #header>
          <div class="px-4 pt-4">
            <h2 class="text-2xl font-bold m-0">Profil-Einstellungen</h2>
            <p class="text-color-secondary mt-2 mb-0">Verwalte deine persönlichen Daten</p>
          </div>
        </template>

      <template #content>
        <div class="flex flex-column gap-5">
          <div class="flex flex-column align-items-center gap-3 pb-4 avatar-section">
            <Avatar
              v-if="avatarPreview"
              :image="avatarPreview"
              size="xlarge"
              shape="circle"
              class="avatar-large"
            />
            <Avatar
              v-else
              :label="initials"
              size="xlarge"
              shape="circle"
              class="avatar-large"
            />

            <div class="flex gap-2">
              <FileUpload
                mode="basic"
                accept="image/*"
                :maxFileSize="5000000"
                @select="handleAvatarUpload"
                :auto="true"
                chooseLabel="Avatar hochladen"
                chooseIcon="pi pi-upload"
              />
              <Button
                v-if="avatarPreview"
                label="Entfernen"
                icon="pi pi-trash"
                severity="danger"
                outlined
                @click="removeAvatar"
              />
            </div>
            <p class="text-xs text-color-secondary m-0">
              Erlaubte Formate: JPG, PNG, GIF (max. 5MB)
            </p>
          </div>

          <div class="flex flex-column gap-4">
            <div class="flex flex-column gap-2">
              <label for="username" class="font-semibold">Benutzername</label>
              <InputText
                id="username"
                v-model="username"
                placeholder="Dein Benutzername"
                :disabled="true"
              />
            </div>

            <div class="flex flex-column gap-2">
              <label for="email" class="font-semibold">E-Mail</label>
              <InputText
                id="email"
                v-model="email"
                type="email"
                placeholder="deine@email.de"
                :disabled="loading"
              />
            </div>

            <div class="grid">
              <div class="col-12 md:col-6">
                <div class="flex flex-column gap-2">
                  <label for="firstName" class="font-semibold">Vorname</label>
                  <InputText
                    id="firstName"
                    v-model="firstName"
                    placeholder="Max"
                    :disabled="loading"
                  />
                </div>
              </div>
              <div class="col-12 md:col-6">
                <div class="flex flex-column gap-2">
                  <label for="lastName" class="font-semibold">Nachname</label>
                  <InputText
                    id="lastName"
                    v-model="lastName"
                    placeholder="Mustermann"
                    :disabled="loading"
                  />
                </div>
              </div>
            </div>

            <div class="flex flex-column gap-2">
              <label for="street" class="font-semibold">Straße & Hausnummer</label>
              <InputText
                id="street"
                v-model="street"
                placeholder="Musterstraße 123"
                :disabled="loading"
              />
            </div>

            <div class="grid">
              <div class="col-12 md:col-4">
                <div class="flex flex-column gap-2">
                  <label for="zipCode" class="font-semibold">PLZ</label>
                  <InputText
                    id="zipCode"
                    v-model="zipCode"
                    placeholder="44137"
                    :disabled="loading"
                  />
                </div>
              </div>
              <div class="col-12 md:col-8">
                <div class="flex flex-column gap-2">
                  <label for="city" class="font-semibold">Stadt</label>
                  <InputText
                    id="city"
                    v-model="city"
                    placeholder="Dortmund"
                    :disabled="loading"
                  />
                </div>
              </div>
            </div>

            <div class="flex flex-column gap-2">
              <label for="country" class="font-semibold">Land</label>
              <InputText
                id="country"
                v-model="country"
                placeholder="Deutschland"
                :disabled="loading"
              />
            </div>

            <div class="flex gap-2 pt-3">
              <Button
                label="Speichern"
                icon="pi pi-check"
                :loading="loading"
                @click="handleSave"
              />
              <Button
                label="Abbrechen"
                icon="pi pi-times"
                severity="secondary"
                outlined
                @click="router.push('/settings')"
              />
            </div>
          </div>

        </div>
      </template>
    </Card>
    </div>
  </AppShell>
</template>

<style scoped>

.profile-settings {
  max-width: 800px;
  margin: 0 auto;
}

.avatar-large {
  width: 120px;
  height: 120px;
  font-size: 3rem;
}

.avatar-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-section {
  border-bottom: 1px solid var(--surface-border);
}

</style>

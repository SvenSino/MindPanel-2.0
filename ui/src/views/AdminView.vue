<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import AppShell from '@/components/layout/AppShell.vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Avatar from 'primevue/avatar'
import Tag from 'primevue/tag'
import { useToast } from 'primevue/usetoast'
import api from '@/services/api'

interface UserProfile {
  id: string
  userId: string
  firstName: string
  lastName: string
  street: string
  zipCode: string
  city: string
  country: string
  avatar: string | null
}

const toast = useToast()
const users = ref<UserProfile[]>([])
const loading = ref(false)
const editDialog = ref(false)
const saving = ref(false)

const editUser = ref<UserProfile>({
  id: '',
  userId: '',
  firstName: '',
  lastName: '',
  street: '',
  zipCode: '',
  city: '',
  country: '',
  avatar: null,
})

const totalUsers = computed(() => users.value.length)

onMounted(loadUsers)

async function loadUsers() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/users')
    users.value = data
  } catch {
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'User konnten nicht geladen werden', life: 3000 })
  } finally {
    loading.value = false
  }
}

function openEdit(user: UserProfile) {
  editUser.value = { ...user }
  editDialog.value = true
}

function initials(user: UserProfile) {
  const f = user.firstName?.[0] ?? ''
  const l = user.lastName?.[0] ?? ''
  return (f + l).toUpperCase() || '?'
}

function fullAddress(user: UserProfile) {
  if (!user.street && !user.city) return null
  return [user.street, user.zipCode, user.city, user.country].filter(Boolean).join(', ')
}

async function saveUser() {
  saving.value = true
  try {
    const { data } = await api.put(`/admin/users/${editUser.value.userId}`, {
      street: editUser.value.street,
      zipCode: editUser.value.zipCode,
      city: editUser.value.city,
      country: editUser.value.country,
      avatar: editUser.value.avatar,
    })
    const index = users.value.findIndex(u => u.userId === editUser.value.userId)
    if (index !== -1) users.value[index] = data
    editDialog.value = false
    toast.add({ severity: 'success', summary: 'Gespeichert', life: 3000 })
  } catch {
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Speichern fehlgeschlagen', life: 3000 })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <AppShell>
    <div class="mb-4 flex align-items-center justify-content-between">
      <div>
        <h2 class="text-3xl font-bold m-0 mb-1">Nutzerverwaltung</h2>
        <span class="text-color-secondary text-sm">Übersicht aller registrierten Nutzer</span>
      </div>
      <Button icon="pi pi-refresh" severity="secondary" rounded @click="loadUsers" :loading="loading" />
    </div>

    <!-- Stats -->
    <div class="flex gap-3 mb-4">
      <div class="stat-card">
        <span class="stat-label">Nutzer gesamt</span>
        <span class="stat-value">{{ totalUsers }}</span>
      </div>
    </div>

    <!-- Table -->
    <div class="table-wrapper">
      <DataTable
        :value="users"
        :loading="loading"
        stripedRows
        :rows="10"
        paginator
        paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink"
      >
        <Column header="Nutzer" style="min-width: 200px">
          <template #body="{ data }">
            <div class="flex align-items-center gap-3">
              <Avatar
                v-if="data.avatar"
                :image="data.avatar"
                shape="circle"
                size="normal"
              />
              <Avatar
                v-else
                :label="initials(data)"
                shape="circle"
                size="normal"
                class="avatar-fallback"
              />
              <div class="flex flex-column">
                <span class="font-medium">
                  {{ data.firstName || data.lastName ? `${data.firstName} ${data.lastName}`.trim() : '—' }}
                </span>
              </div>
            </div>
          </template>
        </Column>

        <Column header="Adresse">
          <template #body="{ data }">
            <span v-if="fullAddress(data)" class="text-sm">{{ fullAddress(data) }}</span>
            <Tag v-else value="Keine Adresse" severity="secondary" />
          </template>
        </Column>

        <Column header="" style="width: 4rem">
          <template #body="{ data }">
            <Button icon="pi pi-pencil" severity="secondary" text rounded @click="openEdit(data)" />
          </template>
        </Column>
      </DataTable>
    </div>

    <!-- Edit Dialog -->
    <Dialog
      v-model:visible="editDialog"
      :modal="true"
      style="width: 440px"
    >
      <template #header>
        <div class="flex align-items-center gap-3">
          <Avatar
            v-if="editUser.avatar"
            :image="editUser.avatar"
            shape="circle"
            size="large"
          />
          <Avatar
            v-else
            :label="initials(editUser)"
            shape="circle"
            size="large"
            class="avatar-fallback"
          />
          <div>
            <div class="font-bold text-lg">
              {{ editUser.firstName || editUser.lastName ? `${editUser.firstName} ${editUser.lastName}`.trim() : 'Nutzer' }}
            </div>
            <div class="text-sm text-color-secondary user-id">{{ editUser.userId }}</div>
          </div>
        </div>
      </template>

      <div class="flex flex-column gap-3 pt-3">
        <div class="flex flex-column gap-1">
          <label class="font-medium text-sm">Straße</label>
          <InputText v-model="editUser.street" class="w-full" placeholder="z.B. Musterstraße 1" />
        </div>
        <div class="flex gap-2">
          <div class="flex flex-column gap-1" style="width: 35%">
            <label class="font-medium text-sm">PLZ</label>
            <InputText v-model="editUser.zipCode" class="w-full" placeholder="12345" />
          </div>
          <div class="flex flex-column gap-1 flex-1">
            <label class="font-medium text-sm">Stadt</label>
            <InputText v-model="editUser.city" class="w-full" placeholder="Berlin" />
          </div>
        </div>
        <div class="flex flex-column gap-1">
          <label class="font-medium text-sm">Land</label>
          <InputText v-model="editUser.country" class="w-full" placeholder="Deutschland" />
        </div>
      </div>

      <template #footer>
        <Button label="Abbrechen" severity="secondary" text @click="editDialog = false" />
        <Button label="Speichern" icon="pi pi-check" @click="saveUser" :loading="saving" />
      </template>
    </Dialog>
  </AppShell>
</template>

<style scoped>
.stat-card {
  background: var(--surface-card);
  border: 1px solid var(--surface-border);
  border-radius: 12px;
  padding: 1.25rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-label {
  font-size: 0.85rem;
  color: var(--text-color-secondary);
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1;
  color: var(--text-color);
}

.table-wrapper {
  background: var(--surface-card);
  border: 1px solid var(--surface-border);
  border-radius: 12px;
  overflow: hidden;
}

.avatar-fallback {
  background: var(--p-button-primary-hover-background);
  color: var(--primary-color-text);
  font-weight: 600;
}

.user-id {
  font-family: monospace;
  font-size: 0.75rem;
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

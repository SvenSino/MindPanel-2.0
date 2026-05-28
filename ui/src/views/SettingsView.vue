<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useDashboardStore } from '@/stores/dashboard'
import { useUiStore } from '@/stores/ui'
import AppShell from '@/components/layout/AppShell.vue'
import Card from 'primevue/card'
import Button from 'primevue/button'
import ToggleSwitch from 'primevue/toggleswitch'
import Divider from 'primevue/divider'
import { useConfirm } from 'primevue/useconfirm'

const router = useRouter()
const dashboardStore = useDashboardStore()
const uiStore = useUiStore()
const confirm = useConfirm()

const widgets = computed(() => dashboardStore.widgets)
const isDarkMode = computed(() => uiStore.isDarkMode)

function toggleWidget(widgetId: string) {
  dashboardStore.toggleWidget(widgetId)
}

function confirmResetLayout() {
  confirm.require({
    message: 'Möchtest du wirklich das Layout zurücksetzen? Dies kann nicht rückgängig gemacht werden.',
    header: 'Layout zurücksetzen',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Abbrechen',
    acceptLabel: 'Zurücksetzen',
    accept: () => {
      dashboardStore.resetWidgets()
    },
  })
}

function confirmClearData() {
  confirm.require({
    message: 'Möchtest du wirklich alle Daten löschen? Dies kann nicht rückgängig gemacht werden.',
    header: 'Alle Daten löschen',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Abbrechen',
    acceptLabel: 'Löschen',
    accept: () => {
      if (typeof window !== 'undefined') {
        const keys = Object.keys(localStorage).filter(key => key.startsWith('mindpanel_'))
        keys.forEach(key => localStorage.removeItem(key))
        window.location.reload()
      }
    },
  })
}
</script>

<template>
  <AppShell>
    <div class="settings-view">
      <div class="mb-4">
        <Button
          icon="pi pi-arrow-left"
          label="Zurück"
          text
          @click="router.push('/')"
          class="mb-3"
        />
        <h2 class="text-3xl font-bold m-0">Einstellungen</h2>
        <p class="text-color-secondary mt-2">Passe MindPanel an deine Bedürfnisse an</p>
      </div>

      <div class="grid">
        <div class="col-12 lg:col-8">
          <Card class="mb-4">
            <template #title>
              <div class="flex align-items-center gap-2">
                <i class="pi pi-palette"></i>
                <span>Darstellung</span>
              </div>
            </template>
            <template #content>
              <div class="flex justify-content-between align-items-center">
                <div>
                  <div class="font-semibold mb-1">Dark Mode</div>
                  <div class="text-sm text-color-secondary">
                    Dunkles Farbschema aktivieren
                  </div>
                </div>
                <ToggleSwitch v-model="isDarkMode" @update:modelValue="uiStore.toggleDarkMode" />
              </div>
            </template>
          </Card>

          <Card class="mb-4">
            <template #title>
              <div class="flex align-items-center gap-2">
                <i class="pi pi-th-large"></i>
                <span>Widgets</span>
              </div>
            </template>
            <template #content>
              <p class="text-color-secondary mb-4">
                Wähle aus, welche Widgets auf dem Dashboard angezeigt werden sollen
              </p>

              <div class="flex flex-column gap-3">
                <div
                  v-for="widget in widgets"
                  :key="widget.id"
                  class="flex justify-content-between align-items-center p-2 border-round widget-settings-item"
                >
                  <div class="flex align-items-center gap-3">
                    <i
                      :class="[
                        'pi text-xl',
                        {
                          'pi-check-square': widget.type === 'todos',
                          'pi-book': widget.type === 'notes',
                          'pi-sun': widget.type === 'weather',
                          'pi-calendar': widget.type === 'calendar',
                          'pi-clock': widget.type === 'pomodoro',
                        },
                      ]"
                    ></i>
                    <div>
                      <div class="font-semibold"><p>{{ widget.title }}</p></div>
                    </div>
                  </div>
                  <ToggleSwitch
                    :modelValue="widget.enabled"
                    @update:modelValue="toggleWidget(widget.id)"
                  />
                </div>
              </div>

              <Divider />

              <Button
                label="Layout zurücksetzen"
                icon="pi pi-refresh"
                outlined
                severity="secondary"
                @click="confirmResetLayout"
              />
            </template>
          </Card>

          <Card class="mb-4">
            <template #title>
              <div class="flex align-items-center gap-2">
                <i class="pi pi-database"></i>
                <span>Daten</span>
              </div>
            </template>
            <template #content>
              <div class="mb-3">
                <div class="font-semibold mb-1">Lokaler Speicher</div>
                <div class="text-sm text-color-secondary">
                  Alle Daten werden lokal in deinem Browser gespeichert
                </div>
              </div>

              <Button
                label="Alle Daten löschen"
                icon="pi pi-trash"
                severity="danger"
                outlined
                @click="confirmClearData"
              />
            </template>
          </Card>
        </div>

        <div class="col-12 lg:col-4">
          <Card>
            <template #title>
              <div class="flex align-items-center gap-2">
                <i class="pi pi-info-circle"></i>
                <span>Über MindPanel</span>
              </div>
            </template>
            <template #content>
              <div class="flex flex-column gap-3 text-sm">
                <div>
                  <div class="font-semibold mb-1">Version</div>
                  <div class="text-color-secondary">0.1.0</div>
                </div>

                <Divider />

                <div>
                  <div class="font-semibold mb-1">Technologien</div>
                  <div class="flex flex-column gap-1 text-color-secondary">
                    <div>Vue 3</div>
                    <div>TypeScript</div>
                    <div>PrimeVue</div>
                    <div>Pinia</div>
                    <div>Vite</div>
                  </div>
                </div>

                <Divider />

                <div>
                  <div class="font-semibold mb-1">Features</div>
                  <div class="flex flex-column gap-1 text-color-secondary">
                    <div><i class="pi pi-check mr-2"></i>Drag & Drop</div>
                    <div><i class="pi pi-check mr-2"></i>Dark Mode</div>
                    <div><i class="pi pi-check mr-2"></i>Offline-fähig</div>
                    <div><i class="pi pi-check mr-2"></i>Responsive</div>
                  </div>
                </div>

                <Divider />

              </div>
            </template>
          </Card>
        </div>
      </div>
    </div>
  </AppShell>
</template>

<style scoped>
.settings-view {
  max-width: 100%;
}

.widget-settings-item {
  border: 1px solid var(--surface-border);
  background: var(--surface-card);
  transition: all 0.2s ease;
}

.widget-settings-item:hover {
  border-color: var(--primary-color);
  transform: translateY(-1px);
}
</style>

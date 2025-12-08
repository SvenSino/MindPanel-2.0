<script setup lang="ts">
import { computed } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'
import type { Widget } from '@/stores/dashboard'
import AppShell from '@/components/layout/AppShell.vue'
import TodosWidget from '@/components/widgets/TodosWidget.vue'
import NotesWidget from '@/components/widgets/NotesWidget.vue'
import WeatherWidget from '@/components/widgets/WeatherWidget.vue'
import CalendarWidget from '@/components/widgets/CalendarWidget.vue'
import PomodoroWidget from '@/components/widgets/PomodoroWidget.vue'
import draggable from 'vuedraggable'

const dashboardStore = useDashboardStore()

const enabledWidgets = computed({
  get: () => dashboardStore.enabledWidgets,
  set: (value: Widget[]) => {
    dashboardStore.updateWidgetOrder(value)
  }
})

const widgetComponents = {
  todos: TodosWidget,
  notes: NotesWidget,
  weather: WeatherWidget,
  calendar: CalendarWidget,
  pomodoro: PomodoroWidget,
}
</script>

<template>
  <AppShell>
    <div class="dashboard">
      <div class="mb-4">
        <h2 class="text-3xl font-bold m-0">Dashboard</h2>
        <p class="text-color-secondary mt-2">Willkommen zurück! Hier ist deine Übersicht.</p>
      </div>

      <draggable
        v-model="enabledWidgets"
        :animation="200"
        handle=".drag-handle"
        class="widgets-grid"
        item-key="id"
      >
        <template #item="{ element }">
          <div class="widget-wrapper fade-in">
            <component
              :is="widgetComponents[element.type]"
              :key="element.id"
            />
          </div>
        </template>
      </draggable>

      <div v-if="enabledWidgets.length === 0" class="empty-state text-center py-8">
        <i class="pi pi-inbox text-6xl text-color-secondary mb-4"></i>
        <h3 class="text-2xl font-semibold mb-2">Keine Widgets aktiviert</h3>
        <p class="text-color-secondary">
          Gehe zu den Einstellungen, um Widgets zu aktivieren.
        </p>
      </div>
    </div>
  </AppShell>
</template>

<style scoped>
.dashboard {
  max-width: 100%;
}

.widget-wrapper {
  height: 100%;
  min-height: 300px;
  min-width: 380px;
}

.empty-state {
  padding: 4rem 2rem;
}

@media (max-width: 768px) {
  .widget-wrapper {
    min-height: 250px;
    min-width: 100%;
  }
}
</style>

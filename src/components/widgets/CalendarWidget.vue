<script setup lang="ts">
import { ref } from 'vue'
import Card from 'primevue/card'
import DatePicker from 'primevue/datepicker'

const selectedDate = ref<Date>(new Date())

function formatDate(date: Date): string {
  return date.toLocaleDateString('de-DE', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}
</script>

<template>
  <Card class="widget-card h-full">
    <template #header>
      <div class="widget-header drag-handle">
        <div class="flex align-items-center gap-2">
          <i class="pi pi-calendar text-xl"></i>
          <h3 class="m-0 font-semibold">Kalender</h3>
        </div>
      </div>
    </template>

    <template #content>
      <div class="widget-content">
        <div class="calendar-container">
          <DatePicker
            v-model="selectedDate"
            inline
            class="w-full"
          />
        </div>

        <div class="mt-4 p-3 border-round surface-card">
          <div class="text-sm text-color-secondary mb-1">Ausgewähltes Datum</div>
          <div class="font-semibold">
            {{ formatDate(selectedDate) }}
          </div>
        </div>

        <div class="mt-3 p-3 border-round surface-50 text-center">
          <i class="pi pi-info-circle mr-2 text-color-secondary"></i>
          <span class="text-sm text-color-secondary">
            Event-Verwaltung in zukünftigen Versionen
          </span>
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.calendar-container {
  display: flex;
  justify-content: center;
}

:deep(.p-datepicker table) {
  width: 100%;
}
</style>

<script setup lang="ts">
import { ref } from 'vue'
import { usePomodoroStore } from '@/stores/pomodoro'
import Card from 'primevue/card'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import ProgressBar from 'primevue/progressbar'
import ToggleSwitch from 'primevue/toggleswitch'
import Dialog from 'primevue/dialog'

const pomodoroStore = usePomodoroStore()
const showStatsDialog = ref(false)

function formatTime(num: number): string {
  return num.toString().padStart(2, '0')
}
</script>

<template>
  <Card class="widget-card h-full">
    <template #header>
      <div class="widget-header drag-handle">
        <div class="flex align-items-center gap-2">
          <i class="pi pi-clock text-xl"></i>
          <h3 class="m-0 font-semibold">Pomodoro</h3>
        </div>
        <div class="flex align-items-center gap-2">
          <div class="flex align-items-center gap-2">
            <i class="pi pi-check-circle text-color-secondary"></i>
            <span class="text-sm font-semibold">{{ pomodoroStore.completedPomodoros }}</span>
          </div>
          <Button
            icon="pi pi-chart-bar"
            rounded
            text
            size="small"
            @click="showStatsDialog = true"
            v-tooltip.top="'Statistik'"
          />
        </div>
      </div>
    </template>

    <template #content>
      <div class="widget-content">
        <div class="flex gap-2 mb-4">
          <Button
            label="Fokus"
            :severity="pomodoroStore.currentMode === 'focus' ? 'primary' : 'secondary'"
            :outlined="pomodoroStore.currentMode !== 'focus'"
            @click="pomodoroStore.switchMode('focus')"
            class="flex-1"
            :disabled="pomodoroStore.isRunning"
          />
          <Button
            label="Pause"
            :severity="pomodoroStore.currentMode === 'break' ? 'primary' : 'secondary'"
            :outlined="pomodoroStore.currentMode !== 'break'"
            @click="pomodoroStore.switchMode('break')"
            class="flex-1"
            :disabled="pomodoroStore.isRunning"
          />
        </div>

        <div class="flex justify-content-center align-items-center gap-3 mb-4">
          <div class="text-sm font-semibold text-color-secondary">
            {{ pomodoroStore.currentFlow }}/4
          </div>
          <div class="flex gap-2">
            <div
              v-for="n in 4"
              :key="n"
              class="flow-dot"
              :class="{
                'flow-dot-active': n < pomodoroStore.currentFlow,
                'flow-dot-current': n === pomodoroStore.currentFlow,
              }"
            ></div>
          </div>
        </div>

        <div class="text-center mb-4">
          <div class="timer-display mb-3">
            {{ formatTime(pomodoroStore.displayMinutes) }}:{{ formatTime(pomodoroStore.displaySeconds) }}
          </div>
          <ProgressBar :value="pomodoroStore.progress" :showValue="false" class="h-1rem mb-3" />
          <div class="text-sm text-color-secondary">
            {{
              pomodoroStore.currentMode === 'focus'
                ? 'Fokus-Zeit'
                : pomodoroStore.currentMode === 'longBreak'
                ? 'Lange Pause'
                : 'Kurze Pause'
            }}
          </div>
        </div>

        <div class="flex gap-2 mb-4">
          <Button
            v-if="!pomodoroStore.isRunning"
            icon="pi pi-play"
            label="Start"
            @click="pomodoroStore.start"
            class="flex-1"
            severity="success"
          />
          <Button
            v-else
            icon="pi pi-pause"
            label="Pause"
            @click="pomodoroStore.pause"
            class="flex-1"
            severity="warning"
          />
          <Button
            icon="pi pi-refresh"
            @click="pomodoroStore.reset"
            outlined
            v-tooltip.top="'Zurücksetzen'"
          />
          <Button
            icon="pi pi-step-forward"
            @click="pomodoroStore.skip"
            outlined
            v-tooltip.top="'Überspringen'"
          />
        </div>

        <div class="surface-50 border-round p-3">
          <div class="font-semibold text-sm mb-3">Einstellungen</div>
          <div class="flex flex-column gap-3">
            <div class="flex align-items-center justify-content-between">
              <label class="text-sm">Auto-Start</label>
              <ToggleSwitch v-model="pomodoroStore.autoStart" />
            </div>
            <div>
              <label class="block text-sm mb-2">Fokus-Dauer (Min)</label>
              <InputNumber
                v-model="pomodoroStore.focusDuration"
                :min="1"
                :max="60"
                showButtons
                :disabled="pomodoroStore.isRunning"
                class="w-full"
              />
            </div>
            <div>
              <label class="block text-sm mb-2">Kurze Pause (Min)</label>
              <InputNumber
                v-model="pomodoroStore.breakDuration"
                :min="1"
                :max="30"
                showButtons
                :disabled="pomodoroStore.isRunning"
                class="w-full"
              />
            </div>
            <div>
              <label class="block text-sm mb-2">Lange Pause (Min)</label>
              <InputNumber
                v-model="pomodoroStore.longBreakDuration"
                :min="5"
                :max="60"
                showButtons
                :disabled="pomodoroStore.isRunning"
                class="w-full"
              />
            </div>
          </div>
        </div>
      </div>
    </template>
  </Card>

  <Dialog
    v-model:visible="showStatsDialog"
    header="Pomodoro Statistik - Diese Woche"
    :modal="true"
    :style="{ width: '90vw', maxWidth: '800px' }"
  >
    <div class="flex flex-column gap-4">
      <div class="custom-chart">
        <div class="chart-container">
          <div
            v-for="item in pomodoroStore.statsData"
            :key="item.date"
            class="chart-bar-wrapper"
          >
            <div class="chart-bar-container">
              <div
                class="chart-bar"
                :style="{
                  height: item.value > 0 ? `${(item.value / pomodoroStore.maxValue) * 100}%` : '2px',
                  backgroundColor: 'var(--primary-color)',
                  opacity: item.value > 0 ? 0.8 : 0.2
                }"
              >
                <span v-if="item.value > 0" class="chart-value">{{ item.value }}</span>
              </div>
            </div>
            <div class="chart-label">{{ item.label }}</div>
          </div>
        </div>
      </div>

      <div class="surface-50 border-round p-3">
        <div class="font-semibold mb-3">Zusammenfassung</div>
        <div class="grid">
          <div class="col-6">
            <div class="text-sm text-color-secondary mb-1">Gesamt (Lifetime)</div>
            <div class="text-2xl font-bold">{{ pomodoroStore.completedPomodoros }}</div>
          </div>
          <div class="col-6">
            <div class="text-sm text-color-secondary mb-1">Diese Woche</div>
            <div class="text-2xl font-bold">{{ pomodoroStore.totalFlowsInPeriod }}</div>
          </div>
        </div>
      </div>
    </div>
  </Dialog>
</template>

<style scoped>
.timer-display {
  font-size: 4rem;
  font-weight: bold;
  font-variant-numeric: tabular-nums;
  line-height: 1;
}

.flow-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--surface-border);
  transition: all 0.3s ease;
}

.flow-dot-active {
  background: var(--primary-color);
}

.flow-dot-current {
  background: var(--primary-color);
  box-shadow: 0 0 8px var(--primary-color);
  transform: scale(1.2);
}

.custom-chart {
  padding: 1rem;
  border-radius: 8px;
}

.chart-container {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 4px;
  height: 200px;
  padding-bottom: 30px;
}

.chart-bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.chart-bar-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.chart-bar {
  width: 100%;
  max-width: 40px;
  border-radius: 4px 4px 0 0;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 4px;
}

.chart-bar:hover {
  opacity: 1 !important;
  transform: scaleY(1.05);
}

.chart-value {
  font-size: 0.75rem;
  font-weight: 600;
  color: white;
}

.chart-label {
  font-size: 0.75rem;
  text-align: center;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .timer-display {
    font-size: 3rem;
  }

  .chart-container {
    height: 150px;
    gap: 2px;
  }

  .chart-bar {
    max-width: 30px;
  }

  .chart-label {
    font-size: 0.65rem;
  }

  .chart-value {
    font-size: 0.65rem;
  }
}
</style>

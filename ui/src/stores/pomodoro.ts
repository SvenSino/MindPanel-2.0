import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import api from '@/services/api'

type TimerMode = 'focus' | 'break' | 'longBreak'

export const usePomodoroStore = defineStore('pomodoro', () => {
  const focusDuration = ref(25)
  const breakDuration = ref(5)
  const longBreakDuration = ref(15)
  const currentMode = ref<TimerMode>('focus')
  const timeLeft = ref(0)
  const isRunning = ref(false)
  const completedPomodoros = ref(0)
  const currentFlow = ref(1)
  const autoStart = ref(false)
  const dailyStats = ref<Record<string, number>>({})

  let intervalId: number | null = null

  const currentDuration = computed(() => {
    if (currentMode.value === 'focus') return focusDuration.value
    if (currentMode.value === 'longBreak') return longBreakDuration.value
    return breakDuration.value
  })

  const totalSeconds = computed(() => currentDuration.value * 60)
  const progress = computed(() => {
    if (totalSeconds.value === 0) return 0
    return ((totalSeconds.value - timeLeft.value) / totalSeconds.value) * 100
  })
  const displayMinutes = computed(() => Math.floor(timeLeft.value / 60))
  const displaySeconds = computed(() => timeLeft.value % 60)

  const statsData = computed(() => {
    const data: { label: string; value: number; date: string }[] = []
    const now = new Date()
    for (let i = 6; i >= 0; i--) {
      const date = new Date(now)
      date.setDate(date.getDate() - i)
      const dateStr = date.toISOString().split('T')[0]
      data.push({ label: date.toLocaleDateString('de-DE', { weekday: 'short' }), value: dailyStats.value[dateStr] || 0, date: dateStr })
    }
    return data
  })

  const totalFlowsInPeriod = computed(() => statsData.value.reduce((sum, day) => sum + day.value, 0))
  const maxValue = computed(() => Math.max(...statsData.value.map(d => d.value), 10))

  function start() {
    if (timeLeft.value === 0) timeLeft.value = totalSeconds.value
    isRunning.value = true
    if (intervalId !== null) clearInterval(intervalId)
    intervalId = window.setInterval(() => {
      if (timeLeft.value > 0) timeLeft.value--
      else complete()
    }, 1000)
  }

  function pause() {
    isRunning.value = false
    if (intervalId !== null) {
      clearInterval(intervalId)
      intervalId = null
    }
  }

  function reset() {
    pause()
    currentMode.value = 'focus'
    currentFlow.value = 1
    timeLeft.value = totalSeconds.value
  }

  async function complete() {
    pause()
    if (currentMode.value === 'focus') {
      completedPomodoros.value++
      const today = new Date().toISOString().split('T')[0]
      dailyStats.value[today] = (dailyStats.value[today] || 0) + 1

      try {
        await api.post('/pomodoro/complete')
      } catch {
        // Timer sollte weiterlaufen auch wenn API nicht erreichbar
      }

      currentMode.value = currentFlow.value >= 4 ? 'longBreak' : 'break'
      if (currentFlow.value >= 4) currentFlow.value = 1
      else currentFlow.value++
    } else {
      currentMode.value = 'focus'
    }

    timeLeft.value = totalSeconds.value

    if ('Notification' in window && Notification.permission === 'granted') {
      const body = currentMode.value === 'focus' ? 'Zurück zur Arbeit!' : currentMode.value === 'longBreak' ? 'Zeit für eine lange Pause!' : 'Zeit für eine kurze Pause!'
      new Notification('Pomodoro Timer', { body })
    }

    if (autoStart.value) setTimeout(() => start(), 1000)
  }

  function switchMode(mode: TimerMode) {
    pause()
    currentMode.value = mode
    timeLeft.value = (mode === 'focus' ? focusDuration.value : mode === 'longBreak' ? longBreakDuration.value : breakDuration.value) * 60
  }

  async function skip() {
    pause()
    if (currentMode.value === 'focus') {
      completedPomodoros.value++
      const today = new Date().toISOString().split('T')[0]
      dailyStats.value[today] = (dailyStats.value[today] || 0) + 1
      try { await api.post('/pomodoro/complete') } catch { /* ignore */ }
      currentMode.value = currentFlow.value >= 4 ? 'longBreak' : 'break'
      if (currentFlow.value >= 4) currentFlow.value = 1
      else currentFlow.value++
    } else {
      currentMode.value = 'focus'
    }
    timeLeft.value = totalSeconds.value
    if (autoStart.value) setTimeout(() => start(), 1000)
  }

  async function saveSettings() {
    try {
      await api.put('/pomodoro/settings', {
        focusDuration: focusDuration.value,
        breakDuration: breakDuration.value,
        longBreakDuration: longBreakDuration.value,
        autoStart: autoStart.value,
      })
    } catch { /* ignore */ }
  }

  async function loadSettings() {
    try {
      const { data } = await api.get('/pomodoro/settings')
      focusDuration.value = data.focusDuration ?? 25
      breakDuration.value = data.breakDuration ?? 5
      longBreakDuration.value = data.longBreakDuration ?? 15
      autoStart.value = data.autoStart ?? false
    } catch { /* use defaults */ }
  }

  async function loadDailyStats() {
    try {
      const { data } = await api.get('/pomodoro/stats')
      dailyStats.value = data
    } catch { /* ignore */ }
  }

  async function initPomodoro() {
    await Promise.all([loadSettings(), loadDailyStats()])
    if (timeLeft.value === 0) timeLeft.value = totalSeconds.value
  }

  watch([focusDuration, breakDuration, longBreakDuration, autoStart], saveSettings)

  watch([focusDuration, breakDuration, longBreakDuration], () => {
    if (!isRunning.value) timeLeft.value = totalSeconds.value
  })

  if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission()
  }

  return {
    focusDuration,
    breakDuration,
    longBreakDuration,
    currentMode,
    timeLeft,
    isRunning,
    completedPomodoros,
    currentFlow,
    autoStart,
    dailyStats,
    currentDuration,
    totalSeconds,
    progress,
    displayMinutes,
    displaySeconds,
    statsData,
    totalFlowsInPeriod,
    maxValue,
    start,
    pause,
    reset,
    switchMode,
    skip,
    initPomodoro,
  }
})

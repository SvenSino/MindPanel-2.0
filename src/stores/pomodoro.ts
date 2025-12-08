import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

const STORAGE_KEY = 'mindpanel_pomodoro_settings'
const STATS_KEY = 'mindpanel_pomodoro_stats'

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

  function start() {
    if (timeLeft.value === 0) {
      timeLeft.value = totalSeconds.value
    }

    isRunning.value = true

    if (intervalId !== null) {
      clearInterval(intervalId)
    }

    intervalId = window.setInterval(() => {
      if (timeLeft.value > 0) {
        timeLeft.value--
      } else {
        complete()
      }
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

  function complete() {
    pause()

    if (currentMode.value === 'focus') {
      completedPomodoros.value++

      // Statistik aktualisieren
      const today = new Date().toISOString().split('T')[0]
      dailyStats.value[today] = (dailyStats.value[today] || 0) + 1
      saveDailyStats()

      // Nach 4 Fokus-Sessions eine lange Pause
      if (currentFlow.value >= 4) {
        currentMode.value = 'longBreak'
        currentFlow.value = 1
      } else {
        currentMode.value = 'break'
        currentFlow.value++
      }
    } else {
      currentMode.value = 'focus'
    }

    // Set timeLeft to new mode duration
    timeLeft.value = totalSeconds.value

    // Optional: Notification
    if ('Notification' in window && Notification.permission === 'granted') {
      const body = currentMode.value === 'focus'
        ? 'Zurück zur Arbeit!'
        : currentMode.value === 'longBreak'
        ? 'Zeit für eine lange Pause!'
        : 'Zeit für eine kurze Pause!'

      new Notification('Pomodoro Timer', { body })
    }

    // Auto-start nächster Timer
    if (autoStart.value) {
      setTimeout(() => start(), 1000)
    }
  }

  function switchMode(mode: TimerMode) {
    pause()
    currentMode.value = mode
    // Directly calculate the duration for the new mode
    const duration = mode === 'focus' ? focusDuration.value : mode === 'longBreak' ? longBreakDuration.value : breakDuration.value
    timeLeft.value = duration * 60
  }

  function skip() {
    pause()

    if (currentMode.value === 'focus') {
      completedPomodoros.value++

      // Statistik aktualisieren
      const today = new Date().toISOString().split('T')[0]
      dailyStats.value[today] = (dailyStats.value[today] || 0) + 1
      saveDailyStats()

      // Nach 4 Fokus-Sessions eine lange Pause
      if (currentFlow.value >= 4) {
        currentMode.value = 'longBreak'
        currentFlow.value = 1
      } else {
        currentMode.value = 'break'
        currentFlow.value++
      }
    } else {
      currentMode.value = 'focus'
    }

    timeLeft.value = totalSeconds.value

    // Auto-start wenn aktiviert
    if (autoStart.value) {
      setTimeout(() => start(), 1000)
    }
  }

  function saveSettings() {
    if (typeof window === 'undefined') return
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        focusDuration: focusDuration.value,
        breakDuration: breakDuration.value,
        longBreakDuration: longBreakDuration.value,
        completedPomodoros: completedPomodoros.value,
        currentFlow: currentFlow.value,
        autoStart: autoStart.value,
      })
    )
  }

  function loadSettings() {
    if (typeof window === 'undefined') return
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      try {
        const data = JSON.parse(saved)
        focusDuration.value = data.focusDuration || 25
        breakDuration.value = data.breakDuration || 5
        longBreakDuration.value = data.longBreakDuration || 15
        completedPomodoros.value = data.completedPomodoros || 0
        currentFlow.value = data.currentFlow || 1
        autoStart.value = data.autoStart ?? false
      } catch (e) {
        console.warn('Could not load pomodoro settings')
      }
    }
  }

  function saveDailyStats() {
    if (typeof window === 'undefined') return
    localStorage.setItem(STATS_KEY, JSON.stringify(dailyStats.value))
  }

  function loadDailyStats() {
    if (typeof window === 'undefined') return
    const saved = localStorage.getItem(STATS_KEY)
    if (saved) {
      try {
        dailyStats.value = JSON.parse(saved)
      } catch (e) {
        console.warn('Could not load pomodoro stats')
      }
    }
  }

  const statsData = computed(() => {
    const data: { label: string; value: number; date: string }[] = []
    const now = new Date()

    for (let i = 6; i >= 0; i--) {
      const date = new Date(now)
      date.setDate(date.getDate() - i)
      const dateStr = date.toISOString().split('T')[0]

      const label = date.toLocaleDateString('de-DE', { weekday: 'short' })

      data.push({
        label,
        value: dailyStats.value[dateStr] || 0,
        date: dateStr
      })
    }

    return data
  })

  const totalFlowsInPeriod = computed(() => {
    return statsData.value.reduce((sum, day) => sum + day.value, 0)
  })

  const maxValue = computed(() => {
    const max = Math.max(...statsData.value.map(d => d.value))
    return max > 0 ? max : 10
  })

  // Initialize
  loadSettings()
  loadDailyStats()

  if (timeLeft.value === 0) {
    timeLeft.value = totalSeconds.value
  }

  // Watch duration changes and reset timer if not running
  watch([focusDuration, breakDuration, longBreakDuration], () => {
    if (!isRunning.value) {
      timeLeft.value = totalSeconds.value
    }
  })

  watch([focusDuration, breakDuration, longBreakDuration, completedPomodoros, currentFlow, autoStart], saveSettings)

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
  }
})

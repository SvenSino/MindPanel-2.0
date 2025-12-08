import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

const DARK_MODE_KEY = 'mindpanel_darkmode'

export const useUiStore = defineStore('ui', () => {
  const isDarkMode = ref(false)

  function loadTheme() {
    if (typeof window === 'undefined') return
    const saved = window.localStorage.getItem(DARK_MODE_KEY)
    if (saved !== null) {
      isDarkMode.value = saved === 'true'
    } else {
      isDarkMode.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    applyTheme()
  }

  function saveTheme() {
    if (typeof window === 'undefined') return
    window.localStorage.setItem(DARK_MODE_KEY, String(isDarkMode.value))
  }

  function applyTheme() {
    if (typeof document === 'undefined') return
    const html = document.documentElement
    if (isDarkMode.value) {
      html.classList.add('app-dark')
      html.style.colorScheme = 'dark'
    } else {
      html.classList.remove('app-dark')
      html.style.colorScheme = 'light'
    }
  }

  function toggleDarkMode() {
    isDarkMode.value = !isDarkMode.value
    saveTheme()
    applyTheme()
  }

  watch(isDarkMode, applyTheme)

  function initTheme() {
    loadTheme()
  }

  return {
    toggleDarkMode,
    initTheme,
  }
})

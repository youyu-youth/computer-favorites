import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const isDark = ref(true)
  const isRouteTransitioning = ref(false)
  const routeTransitionStartedAt = ref(0)
  let routeTransitionTimer: number | null = null

  const applyTheme = () => {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('theme', 'light')
    }
  }

  const initTheme = () => {
    const savedTheme = localStorage.getItem('theme')
    isDark.value = savedTheme === null ? true : savedTheme === 'dark'
    applyTheme()
  }

  const toggleTheme = () => {
    isDark.value = !isDark.value
    applyTheme()
  }

  const setDark = (value: boolean) => {
    isDark.value = value
    applyTheme()
  }

  const startRouteTransition = () => {
    if (routeTransitionTimer !== null) {
      window.clearTimeout(routeTransitionTimer)
      routeTransitionTimer = null
    }
    routeTransitionStartedAt.value = Date.now()
    isRouteTransitioning.value = true
  }

  const finishRouteTransition = () => {
    if (!isRouteTransitioning.value) {
      return
    }
    const elapsed = Date.now() - routeTransitionStartedAt.value
    const delay = Math.max(0, 480 - elapsed)
    routeTransitionTimer = window.setTimeout(() => {
      isRouteTransitioning.value = false
      routeTransitionTimer = null
    }, delay)
  }

  return {
    isDark,
    isRouteTransitioning,
    initTheme,
    toggleTheme,
    setDark,
    startRouteTransition,
    finishRouteTransition,
  }
})

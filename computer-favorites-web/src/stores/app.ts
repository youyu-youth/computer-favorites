import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const isDark = ref(true)

  const initTheme = () => {
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme === null) {
      isDark.value = true
      localStorage.setItem('theme', 'dark')
    } else {
      isDark.value = savedTheme === 'dark'
    }

    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  const toggleTheme = () => {
    isDark.value = !isDark.value

    if (isDark.value) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('theme', 'light')
    }
  }

  return {
    isDark,
    initTheme,
    toggleTheme,
  }
})

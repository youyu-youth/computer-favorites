import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('accessToken'))
  const isAuthed = computed(() => Boolean(token.value))

  const setToken = (value: string) => {
    token.value = value
    localStorage.setItem('accessToken', value)
  }

  const clear = () => {
    token.value = null
    localStorage.removeItem('accessToken')
  }

  return {
    token,
    isAuthed,
    setToken,
    clear,
  }
})


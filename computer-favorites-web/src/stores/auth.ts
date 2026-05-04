import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUserProfile } from '@/api/user'
import { renewSession } from '@/api/auth'
import { isUnauthorizedError } from '@/utils/http'

export interface AuthUserSnapshot {
  userId: number | null
  nickname: string
  avatar: string
}

export const useAuthStore = defineStore('auth', () => {
  const SESSION_RENEW_INTERVAL_MS = 5 * 60 * 1000
  const token = ref<string | null>(localStorage.getItem('accessToken'))
  const tokenName = ref<string>(localStorage.getItem('tokenName') || 'satoken')
  const nickname = ref<string>(localStorage.getItem('userNickname') || '')
  const avatar = ref<string>(localStorage.getItem('userAvatar') || '')
  const userId = ref<number | null>(localStorage.getItem('userId') ? Number(localStorage.getItem('userId')) : null)
  const profileLoading = ref(false)
  const sessionChecked = ref(false)
  const sessionValid = ref(false)
  const lastRenewAt = ref(0)
  const isAuthed = computed(() => Boolean(token.value))
  const isSessionValid = computed(
    () => Boolean(token.value) && sessionChecked.value && sessionValid.value,
  )
  const userSnapshot = computed<AuthUserSnapshot>(() => ({
    userId: userId.value,
    nickname: nickname.value,
    avatar: avatar.value,
  }))
  let renewPromise: Promise<boolean> | null = null

  const setToken = (value: string, name = 'satoken') => {
    token.value = value
    tokenName.value = name
    sessionChecked.value = false
    sessionValid.value = false
    lastRenewAt.value = 0
    localStorage.setItem('accessToken', value)
    localStorage.setItem('tokenName', name)
  }

  const setUserSnapshot = (snapshot: Partial<AuthUserSnapshot>) => {
    if (snapshot.userId != null) {
      userId.value = snapshot.userId
      localStorage.setItem('userId', String(snapshot.userId))
    }
    nickname.value = snapshot.nickname || ''
    avatar.value = snapshot.avatar || ''
    localStorage.setItem('userNickname', nickname.value)
    localStorage.setItem('userAvatar', avatar.value)
  }

  const loadCurrentUser = async () => {
    if (!token.value || profileLoading.value) {
      return false
    }
    profileLoading.value = true
    try {
      const profile = await getCurrentUserProfile()
      setUserSnapshot({
        userId: profile.user?.id ?? null,
        nickname: profile.user?.nickname || profile.user?.username || '',
        avatar: profile.user?.avatar || '',
      })
      sessionValid.value = true
      return true
    } catch (error) {
      if (isUnauthorizedError(error)) {
        clear()
        return false
      }
      sessionValid.value = Boolean(token.value)
      return sessionValid.value
    } finally {
      sessionChecked.value = true
      profileLoading.value = false
    }
  }

  const renewSessionIfNeeded = async (force = false) => {
    if (!token.value) {
      return false
    }

    if (renewPromise) {
      return renewPromise
    }

    const now = Date.now()
    if (!force && now - lastRenewAt.value < SESSION_RENEW_INTERVAL_MS) {
      return true
    }

    renewPromise = (async () => {
      try {
        await renewSession()
        lastRenewAt.value = Date.now()
        sessionChecked.value = true
        sessionValid.value = true
        return true
      } catch (error) {
        if (isUnauthorizedError(error)) {
          clear()
          return false
        }
        return true
      } finally {
        renewPromise = null
      }
    })()

    return renewPromise
  }

  const ensureSession = async () => {
    if (!token.value) {
      return false
    }
    if (!sessionChecked.value || !sessionValid.value) {
      const loaded = await loadCurrentUser()
      if (!loaded) {
        return false
      }
    }
    return renewSessionIfNeeded()
  }

  const clear = () => {
    token.value = null
    tokenName.value = 'satoken'
    nickname.value = ''
    avatar.value = ''
    userId.value = null
    sessionChecked.value = true
    sessionValid.value = false
    lastRenewAt.value = 0
    localStorage.removeItem('accessToken')
    localStorage.removeItem('tokenName')
    localStorage.removeItem('userNickname')
    localStorage.removeItem('userAvatar')
    localStorage.removeItem('userId')
  }

  return {
    token,
    tokenName,
    nickname,
    avatar,
    userId,
    profileLoading,
    userSnapshot,
    isAuthed,
    isSessionValid,
    setToken,
    setUserSnapshot,
    loadCurrentUser,
    renewSessionIfNeeded,
    ensureSession,
    clear,
  }
})

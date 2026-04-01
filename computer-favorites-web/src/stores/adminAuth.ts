import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { adminCurrentSession, adminLogin, adminLogout, adminRenewSession } from '@/api/admin-auth'
import { getAdminProfile } from '@/api/admin-profile'
import { isUnauthorizedError } from '@/utils/http'

export interface AdminUserSnapshot {
  userId: number | null
  username: string
  nickname: string
  avatar: string
}

export const useAdminAuthStore = defineStore('adminAuth', () => {
  const SESSION_RENEW_INTERVAL_MS = 5 * 60 * 1000

  const token = ref<string | null>(localStorage.getItem('adminAccessToken'))
  const tokenName = ref<string>(localStorage.getItem('adminTokenName') || 'satoken')
  const userId = ref<number | null>(
    localStorage.getItem('adminUserId') ? Number(localStorage.getItem('adminUserId')) : null,
  )
  const username = ref<string>(localStorage.getItem('adminUsername') || '')
  const nickname = ref<string>(localStorage.getItem('adminNickname') || '')
  const avatar = ref<string>(localStorage.getItem('adminAvatar') || '')
  const profileLoading = ref(false)
  const sessionChecked = ref(false)
  const sessionValid = ref(false)
  const sessionLoading = ref(false)
  const lastRenewAt = ref(0)

  const isAuthed = computed(() => Boolean(token.value))
  const isSessionValid = computed(
    () => Boolean(token.value) && sessionChecked.value && sessionValid.value,
  )
  const userSnapshot = computed<AdminUserSnapshot>(() => ({
    userId: userId.value,
    username: username.value,
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
    localStorage.setItem('adminAccessToken', value)
    localStorage.setItem('adminTokenName', name)
  }

  const setUserSnapshot = (snapshot: Partial<AdminUserSnapshot>) => {
    userId.value = snapshot.userId ?? userId.value
    username.value = snapshot.username ?? username.value
    nickname.value = snapshot.nickname ?? nickname.value
    avatar.value = snapshot.avatar ?? avatar.value

    if (userId.value === null) {
      localStorage.removeItem('adminUserId')
    } else {
      localStorage.setItem('adminUserId', String(userId.value))
    }
    localStorage.setItem('adminUsername', username.value)
    localStorage.setItem('adminNickname', nickname.value)
    localStorage.setItem('adminAvatar', avatar.value)
  }

  const loadCurrentSession = async () => {
    if (!token.value || sessionLoading.value) {
      return false
    }
    sessionLoading.value = true
    try {
      const session = await adminCurrentSession()
      setUserSnapshot({
        userId: session.userId ?? null,
        username: session.username,
        nickname: session.nickname,
        avatar: session.avatar,
      })
      sessionValid.value = true
      return true
    } catch (error) {
      if (isUnauthorizedError(error)) {
        clear()
        return false
      }
      sessionValid.value = false
      return false
    } finally {
      sessionChecked.value = true
      sessionLoading.value = false
    }
  }

  const loadCurrentProfile = async () => {
    if (!token.value || profileLoading.value) {
      return false
    }
    profileLoading.value = true
    try {
      const profile = await getAdminProfile()
      setUserSnapshot({
        userId: profile.id,
        username: profile.username,
        nickname: profile.nickname,
        avatar: profile.avatar,
      })
      return true
    } catch (error) {
      if (isUnauthorizedError(error)) {
        clear()
        return false
      }
      return false
    } finally {
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
        await adminRenewSession()
        lastRenewAt.value = Date.now()
        sessionChecked.value = true
        sessionValid.value = true
        return true
      } catch (error) {
        if (isUnauthorizedError(error)) {
          clear()
          return false
        }
        sessionValid.value = false
        return false
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
      const loaded = await loadCurrentSession()
      if (!loaded) {
        return false
      }
    }
    const renewed = await renewSessionIfNeeded()
    if (!renewed) {
      return false
    }
    // 登录与会话接口不返回头像字段，按需补拉管理员资料，避免阻塞路由。
    if (!avatar.value) {
      void loadCurrentProfile()
    }
    return true
  }

  const login = async (usernameInput: string, password: string, deviceType: string) => {
    const result = await adminLogin({
      username: usernameInput,
      password,
      deviceType,
    })

    setToken(result.accessToken, result.tokenName || 'satoken')
    setUserSnapshot({
      userId: result.userInfo?.userId ?? null,
      username: result.userInfo?.username || '',
      nickname: result.userInfo?.nickname || '',
      avatar: result.userInfo?.avatar || '',
    })

    const valid = await loadCurrentSession()
    if (!valid) {
      throw new Error('管理员会话校验失败，请重新登录')
    }

    if (!avatar.value) {
      void loadCurrentProfile()
    }

    return result
  }

  const logout = async () => {
    if (!token.value) {
      clear()
      return
    }
    try {
      await adminLogout()
    } catch (error) {
      if (!isUnauthorizedError(error)) {
        throw error
      }
    } finally {
      clear()
    }
  }

  const clear = () => {
    token.value = null
    tokenName.value = 'satoken'
    userId.value = null
    username.value = ''
    nickname.value = ''
    avatar.value = ''
    sessionChecked.value = true
    sessionValid.value = false
    lastRenewAt.value = 0

    localStorage.removeItem('adminAccessToken')
    localStorage.removeItem('adminTokenName')
    localStorage.removeItem('adminUserId')
    localStorage.removeItem('adminUsername')
    localStorage.removeItem('adminNickname')
    localStorage.removeItem('adminAvatar')
  }

  return {
    token,
    tokenName,
    userId,
    username,
    nickname,
    avatar,
    profileLoading,
    isAuthed,
    isSessionValid,
    userSnapshot,
    setToken,
    setUserSnapshot,
    loadCurrentSession,
    loadCurrentProfile,
    renewSessionIfNeeded,
    ensureSession,
    login,
    logout,
    clear,
  }
})

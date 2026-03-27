import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import {
  i18n,
  setI18nLanguage,
  initI18nLanguage,
  type SupportedLanguage,
} from '@/i18n'

export type ThemeMode = 'light' | 'dark' | 'system'

const THEME_MODE_STORAGE_KEY = 'theme-mode'

export const useAppStore = defineStore('app', () => {
  const themeMode = ref<ThemeMode>('dark')
  const language = ref<SupportedLanguage>('zh-CN')
  const isRouteTransitioning = ref(false)
  const routeTransitionStartedAt = ref(0)
  let routeTransitionTimer: number | null = null
  let routeTransitionSafeTimer: number | null = null
  let systemThemeMedia: MediaQueryList | null = null
  const systemPrefersDark = ref(true)

  // 解析当前主题模式对应的明暗值
  const resolveIsDark = (mode: ThemeMode): boolean => {
    if (mode === 'dark') {
      return true
    }
    if (mode === 'light') {
      return false
    }
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
      return true
    }
    return systemPrefersDark.value
  }

  const isDark = computed(() => resolveIsDark(themeMode.value))

  // 将主题同步到根节点 class，并兼容旧的 localStorage theme 字段
  const applyTheme = () => {
    if (typeof document === 'undefined') {
      return
    }
    const dark = resolveIsDark(themeMode.value)
    document.documentElement.classList.toggle('dark', dark)
    localStorage.setItem('theme', dark ? 'dark' : 'light')
  }

  // 跟随系统时监听系统主题变化并实时应用
  const handleSystemThemeChange = () => {
    if (!systemThemeMedia) {
      return
    }
    systemPrefersDark.value = systemThemeMedia.matches
    if (themeMode.value !== 'system') {
      return
    }
    applyTheme()
  }

  const unbindSystemThemeListener = () => {
    if (!systemThemeMedia) {
      return
    }
    systemThemeMedia.removeEventListener('change', handleSystemThemeChange)
    systemThemeMedia = null
  }

  const bindSystemThemeListener = () => {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
      return
    }
    unbindSystemThemeListener()
    systemThemeMedia = window.matchMedia('(prefers-color-scheme: dark)')
    systemPrefersDark.value = systemThemeMedia.matches
    systemThemeMedia.addEventListener('change', handleSystemThemeChange)
  }

  const setThemeMode = (mode: ThemeMode) => {
    themeMode.value = mode
    localStorage.setItem(THEME_MODE_STORAGE_KEY, mode)
    if (mode === 'system') {
      bindSystemThemeListener()
    } else {
      unbindSystemThemeListener()
    }
    applyTheme()
  }

  const initTheme = () => {
    const savedThemeMode = localStorage.getItem(THEME_MODE_STORAGE_KEY)
    if (savedThemeMode === 'light' || savedThemeMode === 'dark' || savedThemeMode === 'system') {
      setThemeMode(savedThemeMode)
      return
    }

    const savedTheme = localStorage.getItem('theme')
    if (savedTheme === 'light' || savedTheme === 'dark') {
      setThemeMode(savedTheme)
      return
    }

    setThemeMode('dark')
  }

  // 顶部导航快捷切换保持 light/dark 两态，系统模式下优先切到与当前相反模式
  const toggleTheme = () => {
    setThemeMode(isDark.value ? 'light' : 'dark')
  }

  const setDark = (value: boolean) => {
    setThemeMode(value ? 'dark' : 'light')
  }

  /**
   * 设置语言
   *
   * @param lang - 目标语言
   */
  const setLanguage = (lang: SupportedLanguage) => {
    language.value = lang
    setI18nLanguage(lang)
  }

  /**
   * 初始化语言设置
   */
  const initLanguage = () => {
    const lang = initI18nLanguage()
    language.value = lang
  }

  /**
   * 从后端用户设置同步语言
   *
   * @param lang - 后端返回的语言设置
   */
  const syncLanguageFromBackend = (lang: string | undefined) => {
    if (lang === 'zh-CN' || lang === 'en-US') {
      setLanguage(lang)
    }
  }

  const startRouteTransition = () => {
    if (routeTransitionTimer !== null) {
      window.clearTimeout(routeTransitionTimer)
      routeTransitionTimer = null
    }
    if (routeTransitionSafeTimer !== null) {
      window.clearTimeout(routeTransitionSafeTimer)
      routeTransitionSafeTimer = null
    }
    routeTransitionStartedAt.value = Date.now()
    isRouteTransitioning.value = true
    routeTransitionSafeTimer = window.setTimeout(() => {
      isRouteTransitioning.value = false
      routeTransitionSafeTimer = null
    }, 2500)
  }

  const finishRouteTransition = () => {
    if (!isRouteTransitioning.value) {
      return
    }
    if (routeTransitionSafeTimer !== null) {
      window.clearTimeout(routeTransitionSafeTimer)
      routeTransitionSafeTimer = null
    }
    const elapsed = Date.now() - routeTransitionStartedAt.value
    const delay = Math.max(0, 480 - elapsed)
    routeTransitionTimer = window.setTimeout(() => {
      isRouteTransitioning.value = false
      routeTransitionTimer = null
    }, delay)
  }

  return {
    themeMode,
    language,
    isDark,
    isRouteTransitioning,
    initTheme,
    initLanguage,
    toggleTheme,
    setDark,
    setThemeMode,
    setLanguage,
    syncLanguageFromBackend,
    startRouteTransition,
    finishRouteTransition,
  }
})

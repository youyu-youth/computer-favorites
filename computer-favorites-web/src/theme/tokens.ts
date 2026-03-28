import { ADMIN_THEME_TOKEN_VALUE_MAP } from './admin/tokens'
import type { ThemeScope, ThemeTokenCssVarMap } from './types'
import { USER_THEME_TOKEN_VALUE_MAP } from './user/tokens'

/**
 * 主题作用域列表。
 */
export const THEME_SCOPE_LIST = ['user', 'admin'] as const

/**
 * 各主题作用域令牌值映射表。
 */
export const THEME_TOKEN_SCOPE_VALUE_MAP: Record<ThemeScope, typeof USER_THEME_TOKEN_VALUE_MAP> = {
  user: USER_THEME_TOKEN_VALUE_MAP,
  admin: ADMIN_THEME_TOKEN_VALUE_MAP,
}

/**
 * 保留旧导出，默认指向用户端主题。
 */
export const THEME_TOKEN_VALUE_MAP = THEME_TOKEN_SCOPE_VALUE_MAP.user

/**
 * 主题令牌与 CSS 变量的映射关系。
 */
export const THEME_TOKEN_CSS_VAR_MAP: ThemeTokenCssVarMap = {
  color: {
    primary50: '--cf-color-primary-50-rgb',
    primary100: '--cf-color-primary-100-rgb',
    primary400: '--cf-color-primary-400-rgb',
    primary500: '--cf-color-primary-500-rgb',
    primary600: '--cf-color-primary-600-rgb',
    darkBg: '--cf-color-dark-bg-rgb',
    darkCard: '--cf-color-dark-card-rgb',
    darkBorder: '--cf-color-dark-border-rgb',
  },
  border: {
    default: '--cf-color-border-default-rgb',
    muted: '--cf-color-border-muted-rgb',
    overlay: '--cf-color-border-overlay-rgb',
  },
  shadow: {
    elevation1: '--cf-shadow-elevation-1',
    elevation2: '--cf-shadow-elevation-2',
    elevation3: '--cf-shadow-elevation-3',
    toastLight: '--cf-shadow-toast-light',
    toastDark: '--cf-shadow-toast-dark',
    modalLight: '--cf-shadow-modal-light',
    modalDark: '--cf-shadow-modal-dark',
  },
  motion: {
    fast: '--cf-motion-fast',
    standard: '--cf-motion-standard',
    page: '--cf-motion-page',
    emphasis: '--cf-motion-emphasis',
    routeSafeGuard: '--cf-motion-route-safe-guard',
    easeStandard: '--cf-ease-standard',
  },
}

export type { ThemeScope } from './types'

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

    secondary50: '--cf-color-secondary-50-rgb',
    secondary100: '--cf-color-secondary-100-rgb',
    secondary400: '--cf-color-secondary-400-rgb',
    secondary500: '--cf-color-secondary-500-rgb',
    secondary600: '--cf-color-secondary-600-rgb',

    tertiary50: '--cf-color-tertiary-50-rgb',
    tertiary100: '--cf-color-tertiary-100-rgb',
    tertiary400: '--cf-color-tertiary-400-rgb',
    tertiary500: '--cf-color-tertiary-500-rgb',
    tertiary600: '--cf-color-tertiary-600-rgb',

    neutral50: '--cf-color-neutral-50-rgb',
    neutral100: '--cf-color-neutral-100-rgb',
    neutral400: '--cf-color-neutral-400-rgb',
    neutral500: '--cf-color-neutral-500-rgb',
    neutral600: '--cf-color-neutral-600-rgb',

    darkBg: '--cf-color-dark-bg-rgb',
    darkCard: '--cf-color-dark-card-rgb',
    darkBorder: '--cf-color-dark-border-rgb',

    semanticSuccess: '--cf-color-success-rgb',
    semanticError: '--cf-color-error-rgb',
    semanticWarning: '--cf-color-warning-rgb',
    semanticInfo: '--cf-color-info-rgb',

    surfacePage: '--cf-color-surface-page-rgb',
    surfaceCard: '--cf-color-surface-card-rgb',
    textPrimary: '--cf-color-text-primary-rgb',
    textSecondary: '--cf-color-text-secondary-rgb',
    modalHeader: '--cf-color-modal-header-rgb',
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

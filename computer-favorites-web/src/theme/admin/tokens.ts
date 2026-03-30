import type { ThemeTokenValueMap } from '../types'

/**
 * 管理端主题令牌值映射表。
 *
 * 说明：
 * 管理端采用更高对比与更强状态识别的色彩策略，
 * 但保持与用户端一致的变量命名，便于复用同一套组件语义类。
 */
export const ADMIN_THEME_TOKEN_VALUE_MAP: ThemeTokenValueMap = {
  color: {
    primary50: '#eff6ff',
    primary100: '#dbeafe',
    primary400: '#60a5fa',
    primary500: '#3b82f6',
    primary600: '#2563eb',

    secondary50: '#ecfdf5',
    secondary100: '#d1fae5',
    secondary400: '#34d399',
    secondary500: '#10b981',
    secondary600: '#059669',

    tertiary50: '#f0f9ff',
    tertiary100: '#e0f2fe',
    tertiary400: '#38bdf8',
    tertiary500: '#0ea5e9',
    tertiary600: '#0284c7',

    neutral50: '#f8fafc',
    neutral100: '#f1f5f9',
    neutral400: '#94a3b8',
    neutral500: '#e2e8f0',
    neutral600: '#cbd5e1',

    darkBg: '#0f172a',
    darkCard: '#1e293b',
    darkBorder: '#334155',
    semanticSuccess: '#10b981',
    semanticError: '#ef4444',
    semanticWarning: '#f59e0b',
    semanticInfo: '#3b82f6',
  },
  border: {
    lightDefault: 'rgb(203 213 225 / 1)',
    lightMuted: 'rgb(226 232 240 / 1)',
    darkDefault: 'rgb(51 65 85 / 1)',
    darkMuted: 'rgb(71 85 105 / 1)',
    darkOverlay: 'rgb(255 255 255 / 0.12)',
  },
  shadow: {
    elevation1: '0 1px 2px 0 rgb(15 23 42 / 0.08)',
    elevation2: '0 4px 12px -2px rgb(15 23 42 / 0.14)',
    elevation3: '0 12px 24px -6px rgb(15 23 42 / 0.2)',
    toastLight: '0 10px 28px rgb(15 23 42 / 0.12)',
    toastDark: '0 10px 28px rgb(2 6 23 / 0.45)',
    modalLight: '0 24px 48px rgb(15 23 42 / 0.22)',
    modalDark: '0 28px 56px rgb(2 6 23 / 0.6)',
  },
  motion: {
    fast: '160ms',
    standard: '240ms',
    page: '300ms',
    emphasis: '400ms',
    routeSafeGuard: '2500ms',
  },
}

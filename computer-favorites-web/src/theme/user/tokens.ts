import type { ThemeTokenValueMap } from '../types'

/**
 * 用户端主题令牌值映射表。
 */
export const USER_THEME_TOKEN_VALUE_MAP: ThemeTokenValueMap = {
  color: {
    primary50: '#fffbeb',
    primary100: '#fef3c7',
    primary400: '#fbbf24',
    primary500: '#f59e0b',
    primary600: '#d97706',
    darkBg: '#121212',
    darkCard: '#1c1c1e',
    darkBorder: '#2d2d2d',
    semanticSuccess: '#10b981',
    semanticError: '#f43f5e',
    semanticWarning: '#f59e0b',
    semanticInfo: '#3b82f6',
  },
  border: {
    lightDefault: 'rgb(229 231 235 / 1)',
    lightMuted: 'rgb(226 232 240 / 1)',
    darkDefault: 'rgb(45 45 45 / 1)',
    darkMuted: 'rgb(51 65 85 / 1)',
    darkOverlay: 'rgb(255 255 255 / 0.1)',
  },
  shadow: {
    elevation1: '0 1px 2px 0 rgb(0 0 0 / 0.05)',
    elevation2: '0 4px 6px -1px rgb(0 0 0 / 0.1)',
    elevation3: '0 10px 15px -3px rgb(0 0 0 / 0.1)',
    toastLight: '0 8px 30px rgb(0 0 0 / 0.08)',
    toastDark: '0 8px 30px rgb(0 0 0 / 0.4)',
    modalLight: '0 20px 45px rgb(15 23 42 / 0.2)',
    modalDark: '0 24px 48px rgb(2 6 23 / 0.55)',
  },
  motion: {
    fast: '160ms',
    standard: '240ms',
    page: '300ms',
    emphasis: '400ms',
    routeSafeGuard: '2500ms',
  },
}

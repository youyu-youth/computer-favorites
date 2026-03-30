import type { ThemeTokenValueMap } from '../types'

/**
 * 用户端主题令牌值映射表。
 */
export const USER_THEME_TOKEN_VALUE_MAP: ThemeTokenValueMap = {
  color: {
    primary50: '#fff7ed',
    primary100: '#ffedd5',
    primary400: '#fb923c',
    primary500: '#d97706',
    primary600: '#b45309',

    secondary50: '#f4fbe7',
    secondary100: '#e6f5c4',
    secondary400: '#9fda34',
    secondary500: '#8bca10',
    secondary600: '#72a70d',

    tertiary50: '#ebf3ef',
    tertiary100: '#d6e7de',
    tertiary400: '#4d8a74',
    tertiary500: '#33735d',
    tertiary600: '#2a5f4d',

    neutral50: '#faf8f6',
    neutral100: '#f2ece8',
    neutral400: '#d7ccc3',
    neutral500: '#e9e1db',
    neutral600: '#b8aba1',

    darkBg: '#000000',
    darkCard: '#111111',
    darkBorder: '#262626',

    semanticSuccess: '#10b981',
    semanticError: '#f43f5e',
    semanticWarning: '#d97706',
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

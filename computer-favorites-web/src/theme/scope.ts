import type { ThemeScope } from './types'

/**
 * 根据路由路径解析主题作用域。
 */
export const resolveThemeScopeByPath = (path: string): ThemeScope => {
  const lowerCasePath = path.toLowerCase()
  if (lowerCasePath.startsWith('/admin') || lowerCasePath.startsWith('/computer/admin')) {
    return 'admin'
  }
  return 'user'
}

/**
 * 应用主题作用域到根节点。
 */
export const applyThemeScope = (scope: ThemeScope): void => {
  if (typeof document === 'undefined') {
    return
  }
  document.documentElement.setAttribute('data-cf-theme', scope)
}

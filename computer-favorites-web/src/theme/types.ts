/**
 * 主题作用域类型。
 */
export type ThemeScope = 'user' | 'admin'

/**
 * 主题令牌值分组类型。
 */
export type ThemeTokenValueMap = {
  color: Record<string, string>
  border: Record<string, string>
  shadow: Record<string, string>
  motion: Record<string, string>
}

/**
 * 主题令牌与 CSS 变量映射类型。
 */
export type ThemeTokenCssVarMap = {
  color: Record<string, string>
  border: Record<string, string>
  shadow: Record<string, string>
  motion: Record<string, string>
}

/**
 * @author yyyouth zg
 * @date 2026-05-07
 * GitHub Stats 风格调色板与 ECharts 通用配置工厂
 */

export const githubStatsPalette = {
  primary: '#f59e0b',
  emerald: '#10b981',
  cyan: '#06b6d4',
  blue: '#3b82f6',
  rose: '#f43f5e',
  amber: '#fbbf24',
  lime: '#a3e635',
  slate: '#94a3b8',
} as const

export const chartCategorical = [
  '#f59e0b',
  '#10b981',
  '#06b6d4',
  '#3b82f6',
  '#f43f5e',
  '#a3e635',
  '#94a3b8',
]

export const ringTrack = {
  dark: 'rgba(255,255,255,0.06)',
  light: 'rgba(15,23,42,0.08)',
}

export const splitDashed = {
  dark: 'rgba(255,255,255,0.08)',
  light: 'rgba(15,23,42,0.08)',
}

export const tooltipTextColor = {
  dark: '#e5e7eb',
  light: '#1f2937',
}

export const tooltipBg = {
  dark: 'rgba(8,8,10,0.92)',
  light: 'rgba(255,255,255,0.96)',
}

export const axisTextColor = {
  dark: '#94a3b8',
  light: '#475569',
}

/**
 * 构造 ECharts 通用 tooltip 配置
 */
export const buildTooltip = (isDark: boolean) => ({
  backgroundColor: isDark ? tooltipBg.dark : tooltipBg.light,
  borderColor: 'transparent',
  borderWidth: 0,
  textStyle: {
    color: isDark ? tooltipTextColor.dark : tooltipTextColor.light,
    fontSize: 12,
    fontFamily: 'ui-monospace, SFMono-Regular, Menlo, monospace',
  },
  extraCssText: isDark
    ? 'box-shadow: 0 8px 24px rgba(0,0,0,0.6); border: 1px solid rgba(255,255,255,0.06);'
    : 'box-shadow: 0 8px 24px rgba(15,23,42,0.08); border: 1px solid rgba(15,23,42,0.06);',
})

/**
 * 构造通用 axisLabel
 */
export const buildAxisLabel = (isDark: boolean) => ({
  color: isDark ? axisTextColor.dark : axisTextColor.light,
  fontSize: 11,
  fontFamily: 'ui-monospace, SFMono-Regular, Menlo, monospace',
})

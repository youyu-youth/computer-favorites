/**
 * Vue I18n 配置文件
 *
 * @author yyyouth zg
 * @date 2025-03-23
 */

import { createI18n } from 'vue-i18n'
import type { MessageSchema, SupportedLanguage } from './types'
import { zhCN } from './locales/zh-CN'
import { enUS } from './locales/en-US'

/**
 * 支持的语言列表
 */
export const SUPPORTED_LANGUAGES: SupportedLanguage[] = ['zh-CN', 'en-US']

/**
 * 默认语言
 */
export const DEFAULT_LANGUAGE: SupportedLanguage = 'zh-CN'

/**
 * 语言本地存储键
 */
export const LANGUAGE_STORAGE_KEY = 'language'

/**
 * 创建 i18n 实例
 */
export const i18n = createI18n<[MessageSchema], SupportedLanguage>({
  // 使用 Composition API 模式
  legacy: false,

  // 默认语言
  locale: DEFAULT_LANGUAGE,

  // 回退语言
  fallbackLocale: 'zh-CN',

  // 语言包消息
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
  },

  // 全局注入 $t 函数
  globalInjection: true,

  // 缺失翻译处理
  missingWarn: false,
  fallbackWarn: false,
})

/**
 * 设置语言
 *
 * @param lang - 目标语言
 */
export function setI18nLanguage(lang: SupportedLanguage): void {
  if (i18n.mode === 'legacy') {
    i18n.global.locale = lang
  } else {
    i18n.global.locale.value = lang
  }

  // 更新 HTML 语言与字体模式
  document.documentElement.setAttribute('lang', lang)
  document.documentElement.setAttribute('data-lang', lang)
  document.documentElement.classList.toggle('lang-zh', lang === 'zh-CN')
  document.documentElement.classList.toggle('lang-en', lang === 'en-US')

  // 保存到本地存储
  localStorage.setItem(LANGUAGE_STORAGE_KEY, lang)
}

/**
 * 获取当前语言
 *
 * @returns 当前语言
 */
export function getCurrentLanguage(): SupportedLanguage {
  if (i18n.mode === 'legacy') {
    return i18n.global.locale as SupportedLanguage
  }
  return i18n.global.locale.value as SupportedLanguage
}

/**
 * 初始化语言设置
 * 优先级：localStorage > 浏览器语言 > 默认语言
 *
 * @returns 初始化后的语言
 */
export function initI18nLanguage(): SupportedLanguage {
  // 尝试从本地存储读取
  const savedLanguage = localStorage.getItem(LANGUAGE_STORAGE_KEY) as SupportedLanguage | null

  if (savedLanguage && SUPPORTED_LANGUAGES.includes(savedLanguage)) {
    setI18nLanguage(savedLanguage)
    return savedLanguage
  }

  // 检测浏览器语言
  if (typeof navigator !== 'undefined') {
    const browserLanguage = navigator.language

    // 如果浏览器语言是中文
    if (browserLanguage.startsWith('zh')) {
      setI18nLanguage('zh-CN')
      return 'zh-CN'
    }

    // 如果浏览器语言是英文
    if (browserLanguage.startsWith('en')) {
      setI18nLanguage('en-US')
      return 'en-US'
    }
  }

  // 使用默认语言
  setI18nLanguage(DEFAULT_LANGUAGE)
  return DEFAULT_LANGUAGE
}

/**
 * 懒加载语言包（预留扩展）
 *
 * @param lang - 目标语言
 */
export async function loadLocaleMessages(lang: SupportedLanguage): Promise<void> {
  // 当前已经加载了所有语言包，此函数预留用于后续动态加载
  // 如果后续需要按需加载，可以使用动态 import
  // const messages = await import(`./locales/${lang}/index.ts`)
  // i18n.global.setLocaleMessage(lang, messages.default)
}

// 导出类型
export type { MessageSchema, SupportedLanguage }

// 导出 useI18n 用于组件中
export { useI18n } from 'vue-i18n'

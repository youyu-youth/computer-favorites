/**
 * 国际化类型定义
 *
 * @author yyyouth zg
 * @date 2025-03-23
 */

/**
 * 支持的语言类型
 */
export type SupportedLanguage = 'zh-CN' | 'en-US'

/**
 * 公共模块翻译模式
 */
export interface CommonMessages {
  loading: string
  save: string
  cancel: string
  confirm: string
  delete: string
  edit: string
  add: string
  search: string
  close: string
  success: string
  error: string
  warning: string
  info: string
  required: string
  optional: string
  enable: string
  disable: string
  yes: string
  no: string
  theme: {
    light: string
    dark: string
    system: string
    toggle: string
  }
  language: {
    current: string
    switch: string
    zhCN: string
    enUS: string
  }
  pagination: {
    prev: string
    next: string
    total: string
    items: string
  }
  empty: string
  copy: {
    success: string
    failed: string
  }
}

/**
 * 认证模块翻译模式
 */
export interface AuthMessages {
  login: {
    title: string
    subtitle: string
    username: string
    password: string
    confirmPassword: string
    email: string
    emailCode: string
    sendCode: string
    resendCode: string
    rememberMe: string
    forgotPassword: string
    noAccount: string
    hasAccount: string
    loginButton: string
    registerButton: string
    forgotButton: string
    usernamePlaceholder: string
    passwordPlaceholder: string
    emailPlaceholder: string
    codePlaceholder: string
    usernameRequired: string
    passwordRequired: string
    emailRequired: string
    emailInvalid: string
    codeRequired: string
    codeSent: string
    loginSuccess: string
    registerSuccess: string
    loginFailed: string
    registerFailed: string
  }
  logout: {
    button: string
    success: string
    confirm: string
  }
  session: {
    expired: string
    renewed: string
  }
}

/**
 * 用户模块翻译模式
 */
export interface UserMessages {
  profile: {
    title: string
    editProfile: string
    viewProfile: string
    basicInfo: string
    nickname: string
    avatar: string
    gender: string
    male: string
    female: string
    other: string
    country: string
    city: string
    signature: string
    hobbies: string
    techStack: string
    socialLinks: string
    github: string
    gitee: string
    blog: string
    otherRepo: string
    contribution: string
    contributions: string
    noContribution: string
    projects: string
    websites: string
    skills: string
    noSkills: string
  }
  home: {
    hero: {
      title: string
      subtitle: string
      uploadButton: string
      browseButton: string
    }
    filter: {
      search: string
      searchPlaceholder: string
      categories: string
      allCategories: string
      logic: {
        and: string
        or: string
      }
    }
    grid: {
      visit: string
      copyLink: string
      noWebsite: string
      noUrl: string
    }
  }
}

/**
 * 设置模块翻译模式
 */
export interface SettingsMessages {
  title: string
  sidebar: {
    profile: string
    account: string
    preference: string
    message: string
    data: string
  }
  profile: {
    title: string
    subtitle: string
    basicInfo: {
      title: string
      nickname: string
      nicknamePlaceholder: string
      gender: string
      male: string
      female: string
      other: string
      country: string
      countryPlaceholder: string
      city: string
      cityPlaceholder: string
      signature: string
      signaturePlaceholder: string
      signatureCount: string
    }
    social: {
      title: string
      github: string
      gitee: string
      blog: string
      otherRepo: string
      placeholder: string
    }
    hobbies: {
      title: string
      add: string
      placeholder: string
      popular: string
    }
    techStack: {
      title: string
      placeholder: string
      popular: string
    }
    save: string
    saving: string
    saved: string
    saveFailed: string
  }
  account: {
    title: string
    subtitle: string
    username: {
      title: string
      current: string
      edit: string
      placeholder: string
      rules: {
        minLength: string
        maxLength: string
        pattern: string
        cooldown: string
      }
    }
    email: {
      title: string
      current: string
      verified: string
      unverified: string
      edit: string
      placeholder: string
      sendCode: string
      codePlaceholder: string
      verify: string
    }
    password: {
      title: string
      change: string
      current: string
      new: string
      confirm: string
      placeholder: string
      rules: {
        minLength: string
        pattern: string
        match: string
      }
    }
    danger: {
      title: string
      delete: string
      deleteConfirm: string
    }
  }
  preference: {
    title: string
    subtitle: string
    theme: {
      title: string
      light: string
      dark: string
      system: string
    }
    language: {
      title: string
      subtitle: string
    }
    homepage: {
      title: string
      subtitle: string
      card: string
      list: string
    }
    pageSize: {
      title: string
      subtitle: string
    }
  }
  message: {
    title: string
    subtitle: string
    email: {
      title: string
      collect: string
      comment: string
    }
    push: {
      title: string
      system: string
      update: string
    }
  }
  data: {
    title: string
    subtitle: string
    export: {
      title: string
      description: string
      button: string
    }
    import: {
      title: string
      description: string
      button: string
    }
    clear: {
      title: string
      description: string
      button: string
      confirm: string
    }
  }
}

/**
 * 完整的消息模式
 */
export interface MessageSchema {
  common: CommonMessages
  auth: AuthMessages
  user: UserMessages
  settings: SettingsMessages
}

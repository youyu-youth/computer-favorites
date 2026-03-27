/**
 * 中文设置模块翻译
 *
 * @author yyyouth zg
 * @date 2025-03-23
 */

import type { SettingsMessages } from '../types'

export const settings: SettingsMessages = {
  title: '设置',
  sidebar: {
    profile: '个人资料',
    account: '账户信息',
    preference: '偏好设置',
    message: '消息通知',
    data: '数据管理',
  },
  profile: {
    title: '个人资料',
    subtitle: '更新你的个人资料和社交信息。',
    basicInfo: {
      title: '基本信息',
      nickname: '昵称',
      nicknamePlaceholder: '请输入昵称',
      gender: '性别',
      male: '男',
      female: '女',
      other: '其他',
      country: '国家',
      countryPlaceholder: '请选择国家',
      city: '城市',
      cityPlaceholder: '请输入城市',
      signature: '个性签名',
      signaturePlaceholder: '一句话介绍自己...',
      signatureCount: '{count}/200',
    },
    social: {
      title: '社交链接',
      github: 'GitHub',
      gitee: 'Gitee',
      blog: '博客',
      otherRepo: '其他仓库',
      placeholder: '请输入链接',
    },
    hobbies: {
      title: '兴趣爱好',
      add: '添加兴趣',
      placeholder: '输入后按回车添加',
      popular: '热门标签',
    },
    techStack: {
      title: '技术栈',
      placeholder: '输入后按回车添加',
      popular: '常用技术',
    },
    save: '保存全部更改',
    saving: '保存中...',
    saved: '保存成功',
    saveFailed: '保存失败，请重试',
  },
  account: {
    title: '账户信息',
    subtitle: '管理你的账户信息和安全设置。',
    username: {
      title: '用户名',
      current: '当前用户名',
      edit: '修改用户名',
      placeholder: '请输入新用户名',
      rules: {
        minLength: '用户名至少需要 {min} 个字符',
        maxLength: '用户名不能超过 {max} 个字符',
        pattern: '用户名只能包含字母、数字和下划线',
        cooldown: '用户名修改后 {days} 天内不能再次修改',
      },
    },
    email: {
      title: '邮箱',
      current: '当前邮箱',
      verified: '已验证',
      unverified: '未验证',
      edit: '修改邮箱',
      placeholder: '请输入新邮箱',
      sendCode: '发送验证码',
      codePlaceholder: '请输入验证码',
      verify: '验证邮箱',
    },
    password: {
      title: '密码',
      change: '修改密码',
      current: '当前密码',
      new: '新密码',
      confirm: '确认新密码',
      placeholder: '请输入密码',
      rules: {
        minLength: '密码至少需要 {min} 个字符',
        pattern: '密码必须包含字母和数字',
        match: '两次输入的密码不一致',
      },
    },
    danger: {
      title: '危险操作',
      delete: '删除账户',
      deleteConfirm: '确定要删除账户吗？此操作不可恢复！',
    },
  },
  preference: {
    title: '偏好设置',
    subtitle: '调整系统语言、主题模式和首页默认样式。',
    theme: {
      title: '系统主题',
      light: '浅色模式',
      dark: '深色模式',
      system: '跟随系统',
    },
    language: {
      title: '系统语言',
      subtitle: '界面默认显示的语言',
    },
    homepage: {
      title: '默认主页视图',
      subtitle: '资源广场默认展示的形式',
      card: '网格视图',
      list: '列表视图',
    },
    pageSize: {
      title: '默认分页大小',
      subtitle: '列表每页显示的数据条数',
    },
  },
  message: {
    title: '消息通知',
    subtitle: '配置你的通知偏好。',
    email: {
      title: '邮件通知',
      collect: '收藏通知',
      comment: '评论通知',
    },
    push: {
      title: '推送通知',
      system: '系统通知',
      update: '更新通知',
    },
  },
  data: {
    title: '数据管理',
    subtitle: '导出、导入或清除你的数据。',
    export: {
      title: '导出数据',
      description: '下载你的所有数据备份。',
      button: '导出数据',
    },
    import: {
      title: '导入数据',
      description: '从备份文件恢复数据。',
      button: '导入数据',
    },
    clear: {
      title: '清除数据',
      description: '删除所有本地缓存数据。',
      button: '清除数据',
      confirm: '确定要清除所有数据吗？',
    },
  },
}

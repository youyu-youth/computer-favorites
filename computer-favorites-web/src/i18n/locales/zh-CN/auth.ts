/**
 * 中文认证模块翻译
 *
 * @author yyyouth zg
 * @date 2025-03-23
 */

import type { AuthMessages } from '../types'

export const auth: AuthMessages = {
  login: {
    title: '登录',
    subtitle: '欢迎回到编程爱好者宝藏收藏',
    username: '用户名',
    password: '密码',
    confirmPassword: '确认密码',
    email: '邮箱',
    emailCode: '验证码',
    sendCode: '发送验证码',
    resendCode: '重新发送',
    rememberMe: '记住我',
    forgotPassword: '忘记密码？',
    noAccount: '还没有账号？',
    hasAccount: '已有账号？',
    loginButton: '登录',
    registerButton: '注册',
    forgotButton: '找回密码',
    usernamePlaceholder: '请输入用户名',
    passwordPlaceholder: '请输入密码',
    emailPlaceholder: '请输入邮箱',
    codePlaceholder: '请输入验证码',
    usernameRequired: '用户名不能为空',
    passwordRequired: '密码不能为空',
    emailRequired: '邮箱不能为空',
    emailInvalid: '邮箱格式不正确',
    codeRequired: '验证码不能为空',
    codeSent: '验证码已发送',
    loginSuccess: '登录成功',
    registerSuccess: '注册成功',
    loginFailed: '登录失败',
    registerFailed: '注册失败',
  },
  logout: {
    button: '退出登录',
    success: '退出成功',
    confirm: '确定要退出登录吗？',
  },
  session: {
    expired: '登录已过期，请重新登录',
    renewed: '会话已续期',
  },
}

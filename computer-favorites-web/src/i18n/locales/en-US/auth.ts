/**
 * English auth messages
 *
 * @author yyyouth zg
 * @date 2025-03-23
 */

import type { AuthMessages } from '../../types'

export const auth: AuthMessages = {
  login: {
    title: 'Login',
    subtitle: 'Welcome back to Developer\'s Treasure Collection',
    username: 'Username',
    password: 'Password',
    confirmPassword: 'Confirm Password',
    email: 'Email',
    emailCode: 'Verification Code',
    sendCode: 'Send Code',
    resendCode: 'Resend',
    rememberMe: 'Remember Me',
    forgotPassword: 'Forgot Password?',
    noAccount: 'Don\'t have an account?',
    hasAccount: 'Already have an account?',
    loginButton: 'Login',
    registerButton: 'Register',
    forgotButton: 'Reset Password',
    usernamePlaceholder: 'Enter username',
    passwordPlaceholder: 'Enter password',
    emailPlaceholder: 'Enter email',
    codePlaceholder: 'Enter verification code',
    usernameRequired: 'Username is required',
    passwordRequired: 'Password is required',
    emailRequired: 'Email is required',
    emailInvalid: 'Invalid email format',
    codeRequired: 'Verification code is required',
    codeSent: 'Verification code sent',
    loginSuccess: 'Login successful',
    registerSuccess: 'Registration successful',
    loginFailed: 'Login failed',
    registerFailed: 'Registration failed',
  },
  logout: {
    button: 'Logout',
    success: 'Logged out successfully',
    confirm: 'Are you sure you want to logout?',
  },
  session: {
    expired: 'Session expired, please login again',
    renewed: 'Session renewed',
  },
}

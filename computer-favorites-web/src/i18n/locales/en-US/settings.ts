/**
 * English settings messages
 *
 * @author yyyouth zg
 * @date 2025-03-23
 */

import type { SettingsMessages } from '../types'

export const settings: SettingsMessages = {
  title: 'Settings',
  sidebar: {
    profile: 'Profile',
    account: 'Account',
    preference: 'Preferences',
    message: 'Notifications',
    data: 'Data Management',
  },
  profile: {
    title: 'Profile',
    subtitle: 'Update your personal profile and social information.',
    basicInfo: {
      title: 'Basic Information',
      nickname: 'Nickname',
      nicknamePlaceholder: 'Enter nickname',
      gender: 'Gender',
      male: 'Male',
      female: 'Female',
      other: 'Other',
      country: 'Country',
      countryPlaceholder: 'Select country',
      city: 'City',
      cityPlaceholder: 'Enter city',
      signature: 'Signature',
      signaturePlaceholder: 'Introduce yourself in one sentence...',
      signatureCount: '{count}/200',
    },
    social: {
      title: 'Social Links',
      github: 'GitHub',
      gitee: 'Gitee',
      blog: 'Blog',
      otherRepo: 'Other Repository',
      placeholder: 'Enter link',
    },
    hobbies: {
      title: 'Hobbies',
      add: 'Add Hobby',
      placeholder: 'Press Enter to add',
      popular: 'Popular Tags',
    },
    techStack: {
      title: 'Tech Stack',
      placeholder: 'Press Enter to add',
      popular: 'Common Technologies',
    },
    save: 'Save All Changes',
    saving: 'Saving...',
    saved: 'Saved successfully',
    saveFailed: 'Save failed, please try again',
  },
  account: {
    title: 'Account Information',
    subtitle: 'Manage your account information and security settings.',
    username: {
      title: 'Username',
      current: 'Current Username',
      edit: 'Change Username',
      placeholder: 'Enter new username',
      rules: {
        minLength: 'Username must be at least {min} characters',
        maxLength: 'Username cannot exceed {max} characters',
        pattern: 'Username can only contain letters, numbers and underscores',
        cooldown: 'Username cannot be changed within {days} days',
      },
    },
    email: {
      title: 'Email',
      current: 'Current Email',
      verified: 'Verified',
      unverified: 'Unverified',
      edit: 'Change Email',
      placeholder: 'Enter new email',
      sendCode: 'Send Code',
      codePlaceholder: 'Enter verification code',
      verify: 'Verify Email',
    },
    password: {
      title: 'Password',
      change: 'Change Password',
      current: 'Current Password',
      new: 'New Password',
      confirm: 'Confirm New Password',
      placeholder: 'Enter password',
      rules: {
        minLength: 'Password must be at least {min} characters',
        pattern: 'Password must contain letters and numbers',
        match: 'Passwords do not match',
      },
    },
    danger: {
      title: 'Danger Zone',
      delete: 'Delete Account',
      deleteConfirm: 'Are you sure you want to delete your account? This action cannot be undone!',
    },
  },
  preference: {
    title: 'Preferences',
    subtitle: 'Adjust system language, theme mode and homepage default style.',
    theme: {
      title: 'System Theme',
      light: 'Light Mode',
      dark: 'Dark Mode',
      system: 'Follow System',
    },
    language: {
      title: 'System Language',
      subtitle: 'Default display language for the interface',
    },
    homepage: {
      title: 'Default Homepage View',
      subtitle: 'Default display format for resource plaza',
      card: 'Grid View',
      list: 'List View',
    },
    pageSize: {
      title: 'Default Page Size',
      subtitle: 'Number of items per page in lists',
    },
  },
  message: {
    title: 'Notifications',
    subtitle: 'Configure your notification preferences.',
    email: {
      title: 'Email Notifications',
      collect: 'Collection Notifications',
      comment: 'Comment Notifications',
    },
    push: {
      title: 'Push Notifications',
      system: 'System Notifications',
      update: 'Update Notifications',
    },
  },
  data: {
    title: 'Data Management',
    subtitle: 'Export, import or clear your data.',
    export: {
      title: 'Export Data',
      description: 'Download a backup of all your data.',
      button: 'Export Data',
    },
    import: {
      title: 'Import Data',
      description: 'Restore data from a backup file.',
      button: 'Import Data',
    },
    clear: {
      title: 'Clear Data',
      description: 'Delete all local cached data.',
      button: 'Clear Data',
      confirm: 'Are you sure you want to clear all data?',
    },
  },
}

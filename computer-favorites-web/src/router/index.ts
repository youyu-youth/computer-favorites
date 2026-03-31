import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import WebsitesManagementView from '@/views/admin/WebsitesManagementView.vue'
import UserLayout from '@/layouts/UserLayout.vue'
import HomeView from '@/views/user/HomeView.vue'
import ProfileView from '@/views/user/ProfileView.vue'
import SettingsView from '@/views/user/SettingsView.vue'
import AccountView from '@/views/user/AccountView.vue'
import PasswordChangeView from '@/views/user/PasswordChangeView.vue'
import LoginView from '@/views/user/LoginView.vue'
import { useAdminAuthStore } from '@/stores/adminAuth'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { applyThemeScope, resolveThemeScopeByPath } from '@/theme/scope'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/computer/admin',
      component: AdminLayout,
      meta: { requiresAdminAuth: true },
      children: [
        {
          path: '',
          redirect: '/computer/admin/websites',
        },
        {
          path: 'websites',
          name: 'adminWebsites',
          component: WebsitesManagementView,
          meta: { requiresAdminAuth: true },
        },
        {
          path: 'profile',
          name: 'adminProfile',
          component: () => import('@/views/admin/AdminProfileView.vue'),
          meta: { requiresAdminAuth: true },
        }
      ]
    },
    {
      path: '/',
      redirect: '/computer/home',
    },
    {
      path: '/computer/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/computer/admin/login',
      name: 'adminLogin',
      component: () => import('@/views/admin/AdminLoginView.vue'),
      meta: { requiresAuth: false, requiresAdminAuth: false },
    },
    {
      path: '/computer',
      component: UserLayout,
      children: [
        {
          path: '',
          redirect: '/computer/home',
        },
        {
          path: 'home',
          name: 'home',
          component: HomeView,
          meta: { requiresAuth: false },
        },
        {
          path: 'profile',
          name: 'profile',
          component: ProfileView,
          meta: { requiresAuth: true },
        },
        {
          path: 'settings',
          name: 'settings',
          component: SettingsView,
          meta: { requiresAuth: true },
        },
        {
          path: 'account',
          name: 'account',
          component: AccountView,
          meta: { requiresAuth: true },
        },
        {
          path: 'password-change',
          name: 'passwordChange',
          component: PasswordChangeView,
          meta: { requiresAuth: true },
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  applyThemeScope(resolveThemeScopeByPath(to.path))
  const appStore = useAppStore()
  appStore.startRouteTransition()
  const adminAuthStore = useAdminAuthStore()
  const authStore = useAuthStore()
  const redirectQuery = typeof to.query.redirect === 'string' ? to.query.redirect : '/computer/home'
  const adminRedirectQuery =
    typeof to.query.redirect === 'string' ? to.query.redirect : '/computer/admin/websites'

  if (to.name === 'login') {
    if (!authStore.isAuthed) {
      return true
    }
    const valid = await authStore.ensureSession()
    if (valid) {
      return redirectQuery.startsWith('/computer/') ? redirectQuery : '/computer/home'
    }
    return true
  }

  if (to.name === 'adminLogin') {
    if (!adminAuthStore.isAuthed) {
      return true
    }
    const valid = await adminAuthStore.ensureSession()
    if (valid) {
      return adminRedirectQuery.startsWith('/computer/admin/')
        ? adminRedirectQuery
        : '/computer/admin/websites'
    }
    return true
  }

  if (to.meta.requiresAdminAuth) {
    if (!adminAuthStore.isAuthed) {
      return {
        name: 'adminLogin',
        query: { redirect: to.fullPath },
      }
    }

    const valid = await adminAuthStore.ensureSession()
    if (valid) {
      return true
    }

    return {
      name: 'adminLogin',
      query: { redirect: to.fullPath },
    }
  }

  if (!to.meta.requiresAuth) {
    return true
  }

  if (!authStore.isAuthed) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }

  const valid = await authStore.ensureSession()
  if (valid) {
    return true
  }

  return {
    name: 'login',
    query: { redirect: to.fullPath },
  }
})

router.afterEach(() => {
  useAppStore().finishRouteTransition()
})

router.onError(() => {
  useAppStore().finishRouteTransition()
})

export default router

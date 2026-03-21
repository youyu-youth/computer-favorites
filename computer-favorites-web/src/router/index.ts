import { createRouter, createWebHistory } from 'vue-router'
import UserLayout from '@/layouts/UserLayout.vue'
import HomeView from '@/views/user/HomeView.vue'
import ProfileView from '@/views/user/ProfileView.vue'
import SettingsView from '@/views/user/SettingsView.vue'
import AccountView from '@/views/user/AccountView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
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
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const appStore = useAppStore()
  appStore.startRouteTransition()
  const authStore = useAuthStore()
  const redirectQuery = typeof to.query.redirect === 'string' ? to.query.redirect : '/computer/home'

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

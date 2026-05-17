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
import { useAdminNavStore } from '@/stores/adminNav'
import { applyThemeScope, resolveThemeScopeByPath } from '@/theme/scope'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/computer/admin',
      component: AdminLayout,
      meta: { requiresAdminAuth: true, title: '管理后台' },
      children: [
        {
          path: '',
          redirect: '/computer/admin/websites',
        },
        {
          path: 'tags',
          name: 'adminTags',
          component: () => import('@/views/admin/TagManagementView.vue'),
          meta: { requiresAdminAuth: true, title: '标签管理' },
        },
        {
          path: 'categories',
          name: 'adminCategories',
          component: () => import('@/views/admin/CategoryManagementView.vue'),
          meta: { requiresAdminAuth: true, title: '分类管理' },
        },
        {
          path: 'users',
          name: 'adminUsers',
          component: () => import('@/views/admin/UserManagementView.vue'),
          meta: { requiresAdminAuth: true, title: '用户管理' },
        },
        {
          path: 'reports',
          name: 'adminReports',
          component: () => import('@/views/admin/AdminReportManagementView.vue'),
          meta: { requiresAdminAuth: true, title: '举报处置' },
        },
        {
          path: 'feedbacks',
          name: 'adminFeedbacks',
          component: () => import('@/views/admin/AdminFeedbackManagementView.vue'),
          meta: { requiresAdminAuth: true, title: '反馈处理' },
        },
        {
          path: 'comments',
          name: 'adminComments',
          component: () => import('@/views/admin/AdminCommentManagementView.vue'),
          meta: { requiresAdminAuth: true, title: '评论管理' },
        },
        {
          path: 'announcements',
          name: 'adminAnnouncements',
          component: () => import('@/views/admin/AdminAnnouncementManagementView.vue'),
          meta: { requiresAdminAuth: true, title: '公告管理' },
        },
        {
          path: 'audit-logs',
          name: 'adminAuditLogs',
          component: () => import('@/views/admin/AuditLogManagementView.vue'),
          meta: { requiresAdminAuth: true, title: '审计日志' },
        },
        {
          path: 'tech-stack',
          name: 'adminTechStack',
          component: () => import('@/views/admin/TechStackManagementView.vue'),
          meta: { requiresAdminAuth: true, title: '技术栈管理' },
        },
        {
          path: 'websites',
          name: 'adminWebsites',
          component: WebsitesManagementView,
          meta: { requiresAdminAuth: true, title: '网站管理' },
        },
        {
          path: 'websites/:id',
          name: 'adminWebsiteDetail',
          component: () => import('@/views/admin/AdminWebsiteDetailView.vue'),
          meta: { requiresAdminAuth: true, title: '网站详情' },
        },
        {
          path: 'websites/:id/edit',
          name: 'adminWebsiteEdit',
          component: () => import('@/views/admin/AdminWebsiteEditView.vue'),
          meta: { requiresAdminAuth: true, title: '修改网站' },
        },
        {
          path: 'profile',
          name: 'adminProfile',
          component: () => import('@/views/admin/AdminProfileView.vue'),
          meta: { requiresAdminAuth: true, title: '个人资料', hideAdminSidebar: true },
        },
        {
          path: 'agent',
          name: 'adminAgent',
          component: () => import('@/views/admin/AdminAgentView.vue'),
          meta: { requiresAdminAuth: true, title: 'AI 管理助手' },
        },
      ],
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
          path: 'website/:id',
          name: 'websiteDetail',
          component: () => import('@/views/user/WebsiteDetailView.vue'),
          meta: { requiresAuth: false },
        },
        {
          path: 'profile',
          name: 'profile',
          component: ProfileView,
          meta: { requiresAuth: true },
        },
        {
          path: 'profile/:username',
          name: 'profilePublic',
          component: () => import('@/views/user/ProfilePublicView.vue'),
          props: true,
          meta: { requiresAuth: false },
        },
        {
          path: 'profile/:username/collections',
          name: 'profilePublicCollections',
          component: () => import('@/views/user/PublicCollectionView.vue'),
          props: true,
          meta: { requiresAuth: false, title: '公开收藏夹' },
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
        {
          path: 'collection',
          name: 'collection',
          component: () => import('@/views/user/CollectionView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'website/upload',
          name: 'uploadWebsite',
          component: () => import('@/views/user/UploadWebsiteView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'website/submissions',
          name: 'websiteSubmissions',
          component: () => import('@/views/user/UploadListView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'website/upload-list',
          name: 'websitesUpload',
          component: () => import('@/views/user/WebsitesUploadView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'notifications',
          name: 'notifications',
          redirect: '/computer/settings',
          meta: { requiresAuth: true },
        },
        {
          path: 'message-center',
          name: 'messageCenter',
          component: () => import('@/views/user/MessageCenterView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'announcement',
          name: 'announcement',
          component: () => import('@/views/user/AnnouncementView.vue'),
          meta: { requiresAuth: false },
        },
        {
          path: 'agent',
          name: 'agent',
          component: () => import('@/views/user/AgentView.vue'),
          meta: { requiresAuth: true, title: 'AI 助手' },
        },
      ],
    },
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
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

router.afterEach((to) => {
  useAppStore().finishRouteTransition()

  if (to.name === 'adminTags') {
    useAdminNavStore().setActiveMenu('tags')
    return
  }

  if (to.name === 'adminCategories') {
    useAdminNavStore().setActiveMenu('categories')
    return
  }

  if (to.name === 'adminUsers') {
    useAdminNavStore().setActiveMenu('users')
    return
  }

  if (to.name === 'adminReports') {
    useAdminNavStore().setActiveMenu('reports')
    return
  }

  if (to.name === 'adminFeedbacks') {
    useAdminNavStore().setActiveMenu('feedbacks')
    return
  }

  if (to.name === 'adminComments') {
    useAdminNavStore().setActiveMenu('comments')
    return
  }

  if (to.name === 'adminAnnouncements') {
    useAdminNavStore().setActiveMenu('announcements')
    return
  }

  if (to.name === 'adminAuditLogs') {
    useAdminNavStore().setActiveMenu('auditLogs')
    return
  }

  if (to.name === 'adminTechStack') {
    useAdminNavStore().setActiveMenu('techStack')
    return
  }

  if (
    to.name === 'adminWebsites' ||
    to.name === 'adminWebsiteDetail' ||
    to.name === 'adminWebsiteEdit'
  ) {
    useAdminNavStore().setActiveMenu('websites')
  }
})

router.onError(() => {
  useAppStore().finishRouteTransition()
})

export default router

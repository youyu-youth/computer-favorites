import { createRouter, createWebHistory } from 'vue-router'
import UserLayout from '@/layouts/UserLayout.vue'
import HomeView from '@/views/user/HomeView.vue'
import LoginView from '@/views/auth/LoginView.vue'

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
        },
      ],
    },
  ],
})

export default router

<script setup lang="ts">
import { ref } from 'vue'
import { useAppStore } from '@/stores/app'
import { storeToRefs } from 'pinia'

const appStore = useAppStore()
const { isDark } = storeToRefs(appStore)

const mobileMenuOpen = ref(false)

const toggleDarkMode = () => {
  appStore.toggleTheme()
}
</script>

<template>
  <header class="sticky top-0 z-50 bg-white/80 dark:bg-darkBg/80 backdrop-blur-md border-b border-gray-200 dark:border-darkBorder">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo & 左侧导航 -->
        <div class="flex items-center gap-8">
          <div class="flex-shrink-0 flex items-center gap-2 cursor-pointer">
            <i class="fa-solid fa-ghost text-2xl text-gray-900 dark:text-white"></i>
            <span class="font-bold text-xl text-gray-900 dark:text-white tracking-tight">Glama Admin</span>
          </div>
          <nav class="hidden md:flex space-x-1">
            <a href="#" class="px-3 py-2 rounded-md text-sm font-medium text-gray-900 dark:text-white border-b-2 border-brandOrange">Websites</a>
            <a href="#" class="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white transition-colors">Users</a>
            <a href="#" class="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white transition-colors">Settings</a>
          </nav>
        </div>
        
        <!-- 右侧操作区 -->
        <div class="flex items-center gap-4">
          <button @click="toggleDarkMode" class="text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white w-8 h-8 rounded-full flex items-center justify-center bg-gray-100 dark:bg-darkCard transition-colors" title="切换明暗模式">
            <i class="fas" :class="isDark ? 'fa-sun' : 'fa-moon'"></i>
          </button>
          
          <button class="bg-gray-900 text-white dark:bg-white dark:text-gray-900 px-4 py-2 rounded-md text-sm font-medium hover:bg-gray-800 dark:hover:bg-gray-100 transition-colors">Sign Out</button>
          
          <!-- 移动端菜单按钮 -->
          <button class="md:hidden text-gray-500 dark:text-gray-400 text-xl" @click="mobileMenuOpen = !mobileMenuOpen">
            <i class="fas fa-bars"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 移动端下拉菜单 -->
    <div v-show="mobileMenuOpen" class="md:hidden border-t border-gray-200 dark:border-darkBorder bg-white dark:bg-darkBg">
      <div class="px-2 pt-2 pb-3 space-y-1 sm:px-3">
        <a href="#" class="block px-3 py-2 rounded-md text-base font-medium text-gray-900 dark:text-white bg-gray-100 dark:bg-darkCard">Websites</a>
        <a href="#" class="block px-3 py-2 rounded-md text-base font-medium text-gray-500 dark:text-gray-400">Users</a>
        <a href="#" class="block px-3 py-2 rounded-md text-base font-medium text-gray-500 dark:text-gray-400">Settings</a>
      </div>
    </div>
  </header>
</template>

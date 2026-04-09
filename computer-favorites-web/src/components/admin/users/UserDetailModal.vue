<script setup lang="ts">
import type { AdminUserItem } from '@/types/user'

const props = defineProps<{
  open: boolean
  user: AdminUserItem | null
}>()

const emit = defineEmits<{
  (e: 'update:open', val: boolean): void
  (e: 'edit', user: AdminUserItem): void
}>()

const handleClose = () => {
  emit('update:open', false)
}

const formatDateTime = (value: string | null): string => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

const getAvatarText = (user: AdminUserItem): string => {
  const name = user.nickname || user.username || ''
  return name.charAt(0).toUpperCase()
}

const getAvatarBg = (userId: number): string => {
  const colors = [
    'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300',
    'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
    'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300',
    'bg-rose-100 text-rose-700 dark:bg-rose-900/30 dark:text-rose-300',
    'bg-cyan-100 text-cyan-700 dark:bg-cyan-900/30 dark:text-cyan-300',
    'bg-indigo-100 text-indigo-700 dark:bg-indigo-900/30 dark:text-indigo-300',
  ]
  return colors[userId % colors.length]
}
</script>

<template>
  <Transition name="cf-user-detail-fade" appear>
    <div
      v-if="open && user"
      class="fixed inset-0 z-[130] flex items-center justify-center p-4 sm:p-6"
      @click.self="handleClose"
    >
      <div class="absolute inset-0 bg-black/45 backdrop-blur-[1px]" @click="handleClose"></div>

      <section
        class="relative z-[1] w-full max-w-[500px] overflow-hidden rounded-xl border border-gray-200 bg-white shadow-[0_22px_48px_rgba(15,23,42,0.3)] dark:border-dark-border dark:bg-dark-card dark:shadow-[0_30px_58px_rgba(2,6,23,0.7)]"
      >
        <!-- 关闭按钮 -->
        <button
          type="button"
          class="absolute right-4 top-4 inline-flex h-8 w-8 cursor-pointer items-center justify-center rounded-md border border-gray-200 bg-white text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:border-dark-border dark:bg-dark-bg dark:text-gray-300 dark:hover:bg-gray-700 dark:hover:text-gray-100"
          @click="handleClose"
        >
          <i class="fas fa-times text-sm"></i>
        </button>

        <!-- 用户头部信息区 -->
        <div class="border-b border-gray-200 bg-gray-50 px-5 py-5 pr-16 dark:border-dark-border dark:bg-dark-bg">
          <div class="flex items-center gap-4">
            <div
              v-if="user.avatar"
              class="h-14 w-14 flex-shrink-0 overflow-hidden rounded-full border-2 border-white shadow-md dark:border-dark-border"
            >
              <img :src="user.avatar" :alt="user.nickname || user.username" class="h-full w-full object-cover" />
            </div>
            <div
              v-else
              class="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-full border-2 border-white text-xl font-bold shadow-md dark:border-dark-border"
              :class="getAvatarBg(user.id)"
            >
              {{ getAvatarText(user) }}
            </div>

            <div class="min-w-0">
              <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">
                {{ user.nickname || user.username }}
              </h3>
              <p class="text-sm text-gray-500 dark:text-gray-400">@{{ user.username }}</p>
              <div class="mt-1.5 flex items-center gap-2">
                <span
                  class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
                  :class="
                    user.status === 1
                      ? 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-300'
                      : 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-300'
                  "
                >
                  <span
                    class="mr-1 h-1.5 w-1.5 rounded-full"
                    :class="user.status === 1 ? 'bg-green-500' : 'bg-red-500'"
                  ></span>
                  {{ user.status === 1 ? '正常' : '禁用' }}
                </span>
                <span class="text-xs text-gray-400 dark:text-gray-500">ID: {{ user.id }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 详细信息 -->
        <div class="px-5 py-4 space-y-3">
          <div class="grid grid-cols-2 gap-3">
            <div class="rounded-lg border border-gray-100 bg-gray-50 px-3 py-2.5 dark:border-dark-border dark:bg-dark-bg/40">
              <p class="text-xs text-gray-500 dark:text-gray-400 mb-0.5">邮箱</p>
              <div class="flex items-center gap-1">
                <p class="text-sm font-medium text-gray-800 dark:text-gray-200 truncate">{{ user.email }}</p>
                <span
                  v-if="user.emailVerified === 1"
                  class="flex-shrink-0 inline-flex h-4 w-4 items-center justify-center rounded-full bg-green-100 dark:bg-green-900/20"
                >
                  <i class="fas fa-check text-[9px] text-green-600 dark:text-green-400"></i>
                </span>
              </div>
            </div>

            <div class="rounded-lg border border-gray-100 bg-gray-50 px-3 py-2.5 dark:border-dark-border dark:bg-dark-bg/40">
              <p class="text-xs text-gray-500 dark:text-gray-400 mb-0.5">手机号</p>
              <p class="text-sm font-medium text-gray-800 dark:text-gray-200">
                {{ user.phone || '未绑定' }}
              </p>
            </div>

            <div class="rounded-lg border border-gray-100 bg-gray-50 px-3 py-2.5 dark:border-dark-border dark:bg-dark-bg/40">
              <p class="text-xs text-gray-500 dark:text-gray-400 mb-0.5">注册时间</p>
              <p class="text-sm font-medium text-gray-800 dark:text-gray-200">{{ formatDateTime(user.createTime) }}</p>
            </div>

            <div class="rounded-lg border border-gray-100 bg-gray-50 px-3 py-2.5 dark:border-dark-border dark:bg-dark-bg/40">
              <p class="text-xs text-gray-500 dark:text-gray-400 mb-0.5">最后登录</p>
              <p class="text-sm font-medium text-gray-800 dark:text-gray-200">{{ formatDateTime(user.lastLoginTime) }}</p>
            </div>

            <div class="rounded-lg border border-gray-100 bg-gray-50 px-3 py-2.5 dark:border-dark-border dark:bg-dark-bg/40 col-span-2">
              <p class="text-xs text-gray-500 dark:text-gray-400 mb-0.5">最后登录 IP</p>
              <p class="text-sm font-medium text-gray-800 dark:text-gray-200">{{ user.lastLoginIp || '-' }}</p>
            </div>
          </div>
        </div>

        <!-- 底部 -->
        <footer class="flex justify-end gap-2 border-t border-gray-200 px-5 py-4 dark:border-dark-border bg-gray-50 dark:bg-dark-bg">
          <button
            type="button"
            class="inline-flex h-9 cursor-pointer items-center justify-center rounded-md border border-gray-300 bg-white px-4 text-sm font-medium text-gray-700 shadow-sm transition-colors hover:bg-gray-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg"
            @click="handleClose"
          >
            关闭
          </button>
          <button
            type="button"
            class="inline-flex h-9 cursor-pointer items-center justify-center rounded-md border border-transparent bg-blue-600 px-4 text-sm font-medium text-white shadow-sm transition-colors hover:bg-blue-700 dark:bg-blue-500 dark:hover:bg-blue-600"
            @click="emit('edit', user)"
          >
            <i class="fas fa-edit mr-1.5"></i>
            编辑用户
          </button>
        </footer>
      </section>
    </div>
  </Transition>
</template>

<style scoped>
.cf-user-detail-fade-enter-active,
.cf-user-detail-fade-leave-active {
  transition: opacity 0.18s ease;
}

.cf-user-detail-fade-enter-from,
.cf-user-detail-fade-leave-to {
  opacity: 0;
}
</style>

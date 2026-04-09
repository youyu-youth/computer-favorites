<script setup lang="ts">
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import type { AdminUserItem } from '@/types/user'

const props = defineProps<{
  rows: AdminUserItem[]
  selectedRowIds: number[]
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'selection-change', ids: number[]): void
  (e: 'edit', user: AdminUserItem): void
  (e: 'toggle-status', user: AdminUserItem): void
  (e: 'delete', user: AdminUserItem): void
  (e: 'view', user: AdminUserItem): void
}>()

import { computed } from 'vue'

const selectedRows = computed<AdminUserItem[]>(() => {
  return props.rows.filter((item) => props.selectedRowIds.includes(item.id))
})

const handleSelectionUpdate = (value: AdminUserItem[] | AdminUserItem | null | undefined): void => {
  const rows = Array.isArray(value) ? value : value ? [value] : []
  emit(
    'selection-change',
    rows.map((item) => item.id),
  )
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
  })
}

const selectionCheckboxPt = {
  pcHeaderCheckbox: {
    root: { class: 'relative inline-flex h-4 w-4 shrink-0 cursor-pointer items-center justify-center align-bottom' },
    input: { class: 'absolute inset-0 z-10 m-0 h-full w-full cursor-pointer opacity-0' },
    box: { class: 'flex h-4 w-4 items-center justify-center rounded border border-gray-300 bg-white transition-colors dark:border-dark-border dark:bg-dark-bg [&[data-p-checked=true]]:border-blue-600 [&[data-p-checked=true]]:bg-blue-600 [&[data-p-checked=true]]:dark:border-blue-500 [&[data-p-checked=true]]:dark:bg-blue-500' },
    icon: { class: 'h-2.5 w-2.5 text-white transition-opacity' },
  },
  pcRowCheckbox: {
    root: { class: 'relative inline-flex h-4 w-4 shrink-0 cursor-pointer items-center justify-center align-bottom' },
    input: { class: 'absolute inset-0 z-10 m-0 h-full w-full cursor-pointer opacity-0' },
    box: { class: 'flex h-4 w-4 items-center justify-center rounded border border-gray-300 bg-white transition-colors dark:border-dark-border dark:bg-dark-bg [&[data-p-checked=true]]:border-blue-600 [&[data-p-checked=true]]:bg-blue-600 [&[data-p-checked=true]]:dark:border-blue-500 [&[data-p-checked=true]]:dark:bg-blue-500' },
    icon: { class: 'h-2.5 w-2.5 text-white transition-opacity' },
  },
} as const

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
  <section class="border border-gray-200 bg-white dark:border-dark-border dark:bg-dark-card mt-4">
    <div class="overflow-x-auto">
      <DataTable
        :value="rows"
        :selection="selectedRows"
        :loading="loading"
        dataKey="id"
        class="min-w-[1100px]"
        :pt="{
          table: { class: 'w-full border-separate border-spacing-0' },
          thead: { class: 'bg-gray-50 dark:bg-dark-bg/60' },
          headerRow: { class: 'border-b border-gray-200 dark:border-dark-border' },
          bodyRow: {
            class: 'border-b border-gray-100 transition-colors hover:bg-gray-50/70 dark:border-dark-border/70 dark:hover:bg-dark-bg/40',
          },
          emptyMessage: { class: 'px-4 py-10 text-center text-sm text-gray-500 dark:text-gray-400' },
          loadingOverlay: { class: 'bg-white/70 dark:bg-dark-card/70' },
        }"
        @update:selection="handleSelectionUpdate"
      >
        <template #empty>
          <div class="py-8 text-center">
            <i class="fas fa-users text-2xl text-gray-300 dark:text-gray-600"></i>
            <p class="mt-3 text-sm text-gray-500 dark:text-gray-400">未找到用户</p>
          </div>
        </template>

        <Column
          selectionMode="multiple"
          :pt="selectionCheckboxPt"
          headerClass="px-3 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-3 py-3"
        />

        <Column
          field="id"
          header="ID"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3 text-sm text-gray-500 dark:text-gray-400"
        />

        <!-- 用户信息列 -->
        <Column
          field="username"
          header="用户信息"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div class="flex items-center gap-3">
              <!-- 头像 -->
              <div
                v-if="data.avatar"
                class="h-9 w-9 flex-shrink-0 overflow-hidden rounded-full border border-gray-200 dark:border-dark-border"
              >
                <img :src="data.avatar" :alt="data.nickname || data.username" class="h-full w-full object-cover" />
              </div>
              <div
                v-else
                class="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full text-sm font-semibold"
                :class="getAvatarBg(data.id)"
              >
                {{ getAvatarText(data) }}
              </div>

              <!-- 用户名 + 昵称 -->
              <div class="min-w-0">
                <p class="text-sm font-medium text-gray-900 dark:text-gray-100 truncate">
                  {{ data.nickname || data.username }}
                </p>
                <p class="text-xs text-gray-500 dark:text-gray-400 truncate">@{{ data.username }}</p>
              </div>
            </div>
          </template>
        </Column>

        <!-- 邮箱列 -->
        <Column
          field="email"
          header="邮箱"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <div class="flex items-center gap-1.5">
              <span class="text-sm text-gray-700 dark:text-gray-300">{{ data.email }}</span>
              <span
                v-if="data.emailVerified === 1"
                class="inline-flex h-4 w-4 flex-shrink-0 items-center justify-center rounded-full bg-green-100 dark:bg-green-900/20"
                title="邮箱已验证"
              >
                <i class="fas fa-check text-[9px] text-green-600 dark:text-green-400"></i>
              </span>
            </div>
          </template>
        </Column>

        <!-- 手机列 -->
        <Column
          field="phone"
          header="手机号"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3 text-sm text-gray-600 dark:text-gray-300"
        >
          <template #body="{ data }">
            {{ data.phone || '-' }}
          </template>
        </Column>

        <!-- 状态列 -->
        <Column
          field="status"
          header="状态"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <span
              class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
              :class="[
                data.status === 1
                  ? 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-300'
                  : 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-300',
              ]"
            >
              <span
                class="mr-1 h-1.5 w-1.5 rounded-full"
                :class="data.status === 1 ? 'bg-green-500' : 'bg-red-500'"
              ></span>
              {{ data.status === 1 ? '正常' : '禁用' }}
            </span>
          </template>
        </Column>

        <!-- 最后登录时间 -->
        <Column
          field="lastLoginTime"
          header="最后登录"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <span class="text-sm text-gray-500 dark:text-gray-400">
              {{ formatDateTime(data.lastLoginTime) }}
            </span>
          </template>
        </Column>

        <!-- 注册时间 -->
        <Column
          field="createTime"
          header="注册时间"
          headerClass="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3"
        >
          <template #body="{ data }">
            <span class="text-sm text-gray-500 dark:text-gray-400">
              {{ formatDateTime(data.createTime) }}
            </span>
          </template>
        </Column>

        <!-- 操作列 -->
        <Column
          header="操作"
          headerClass="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
          bodyClass="px-4 py-3 text-right whitespace-nowrap"
        >
          <template #body="{ data }">
            <!-- 查看详情 -->
            <button
              type="button"
              @click.stop="emit('view', data)"
              class="cursor-pointer mr-1.5 inline-flex h-8 items-center rounded bg-gray-50 px-2.5 text-xs font-medium text-gray-600 hover:bg-gray-100 dark:bg-gray-800/60 dark:text-gray-300 dark:hover:bg-gray-700"
              title="查看"
            >
              <i class="fas fa-eye mr-1"></i> 查看
            </button>

            <!-- 编辑 -->
            <button
              type="button"
              @click.stop="emit('edit', data)"
              class="cursor-pointer mr-1.5 inline-flex h-8 items-center rounded bg-blue-50 px-2.5 text-xs font-medium text-blue-700 hover:bg-blue-100 dark:bg-blue-900/20 dark:text-blue-300 dark:hover:bg-blue-900/40"
              title="编辑"
            >
              <i class="fas fa-edit mr-1"></i> 编辑
            </button>

            <!-- 禁用/启用 -->
            <button
              type="button"
              @click.stop="emit('toggle-status', data)"
              class="cursor-pointer mr-1.5 inline-flex h-8 items-center rounded px-2.5 text-xs font-medium transition-colors"
              :class="
                data.status === 1
                  ? 'bg-amber-50 text-amber-700 hover:bg-amber-100 dark:bg-amber-900/20 dark:text-amber-300 dark:hover:bg-amber-900/40'
                  : 'bg-green-50 text-green-700 hover:bg-green-100 dark:bg-green-900/20 dark:text-green-300 dark:hover:bg-green-900/40'
              "
              :title="data.status === 1 ? '禁用用户' : '启用用户'"
            >
              <i :class="data.status === 1 ? 'fas fa-ban mr-1' : 'fas fa-check mr-1'"></i>
              {{ data.status === 1 ? '禁用' : '启用' }}
            </button>

            <!-- 删除 -->
            <button
              type="button"
              @click.stop="emit('delete', data)"
              class="cursor-pointer inline-flex h-8 items-center rounded bg-red-50 px-2.5 text-xs font-medium text-red-700 hover:bg-red-100 dark:bg-red-900/20 dark:text-red-300 dark:hover:bg-red-900/40"
              title="删除"
            >
              <i class="fas fa-trash-alt"></i>
            </button>
          </template>
        </Column>
      </DataTable>
    </div>
  </section>
</template>

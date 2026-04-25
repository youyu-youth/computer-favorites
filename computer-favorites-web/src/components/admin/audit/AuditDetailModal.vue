<script setup lang="ts">
import UModal from '@/components/ui-adapter/UModal.vue'
import { AuditResult, AUDIT_MODULE_LABELS, AUDIT_ACTION_LABELS, AUDIT_USER_TYPE_LABELS } from '@/types/audit-log'
import type { AuditLogDetail } from '@/types/audit-log'

const props = defineProps<{
  open: boolean
  detail: AuditLogDetail | null
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
}>()

const moduleStyleMap: Record<string, { badge: string; dot: string; label: string }> = {
  auth:         { badge: 'bg-blue-50 text-blue-700 dark:bg-blue-400/10 dark:text-blue-300',       dot: 'bg-blue-500',       label: 'border-blue-100 dark:border-blue-400/15' },
  website:      { badge: 'bg-indigo-50 text-indigo-700 dark:bg-indigo-400/10 dark:text-indigo-300',   dot: 'bg-indigo-500',   label: 'border-indigo-100 dark:border-indigo-400/15' },
  user:         { badge: 'bg-slate-50 text-slate-700 dark:bg-slate-400/10 dark:text-slate-300',       dot: 'bg-slate-500',       label: 'border-slate-100 dark:border-slate-400/15' },
  admin:        { badge: 'bg-sky-50 text-sky-700 dark:bg-sky-400/10 dark:text-sky-300',          dot: 'bg-sky-500',          label: 'border-sky-100 dark:border-sky-400/15' },
  comment:      { badge: 'bg-cyan-50 text-cyan-700 dark:bg-cyan-400/10 dark:text-cyan-300',        dot: 'bg-cyan-500',        label: 'border-cyan-100 dark:border-cyan-400/15' },
  report:       { badge: 'bg-red-50 text-red-700 dark:bg-red-400/10 dark:text-red-300',           dot: 'bg-red-500',           label: 'border-red-100 dark:border-red-400/15' },
  feedback:     { badge: 'bg-amber-50 text-amber-700 dark:bg-amber-400/10 dark:text-amber-300',     dot: 'bg-amber-500',     label: 'border-amber-100 dark:border-amber-400/15' },
  announcement: { badge: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-400/10 dark:text-emerald-300', dot: 'bg-emerald-500', label: 'border-emerald-100 dark:border-emerald-400/15' },
  system:       { badge: 'bg-gray-50 text-gray-700 dark:bg-gray-400/10 dark:text-gray-300',        dot: 'bg-gray-500',        label: 'border-gray-100 dark:border-gray-400/15' },
}

const getModuleStyle = (module: string) => moduleStyleMap[module] ?? moduleStyleMap.system

const actionStyleMap: Record<string, { badge: string; icon: string }> = {
  login:  { badge: 'bg-green-50 text-green-700 dark:bg-green-400/10 dark:text-green-300', icon: 'fa-arrow-right-to-bracket' },
  logout: { badge: 'bg-gray-50 text-gray-700 dark:bg-gray-400/10 dark:text-gray-300', icon: 'fa-arrow-right-from-bracket' },
  create: { badge: 'bg-blue-50 text-blue-700 dark:bg-blue-400/10 dark:text-blue-300', icon: 'fa-plus' },
  update: { badge: 'bg-amber-50 text-amber-700 dark:bg-amber-400/10 dark:text-amber-300', icon: 'fa-pen' },
  delete: { badge: 'bg-red-50 text-red-700 dark:bg-red-400/10 dark:text-red-300', icon: 'fa-trash' },
  review: { badge: 'bg-indigo-50 text-indigo-700 dark:bg-indigo-400/10 dark:text-indigo-300', icon: 'fa-eye' },
  export: { badge: 'bg-cyan-50 text-cyan-700 dark:bg-cyan-400/10 dark:text-cyan-300', icon: 'fa-file-export' },
  import: { badge: 'bg-sky-50 text-sky-700 dark:bg-sky-400/10 dark:text-sky-300', icon: 'fa-file-import' },
  status: { badge: 'bg-amber-50 text-amber-700 dark:bg-amber-400/10 dark:text-amber-300', icon: 'fa-toggle-on' },
  ban:    { badge: 'bg-red-50 text-red-700 dark:bg-red-400/10 dark:text-red-300', icon: 'fa-ban' },
  unban:  { badge: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-400/10 dark:text-emerald-300', icon: 'fa-unlock' },
}

const getActionStyle = (action: string) => actionStyleMap[action] ?? { badge: 'bg-gray-50 text-gray-700 dark:bg-gray-400/10 dark:text-gray-300', icon: 'fa-circle' }

const requestMethodColorMap: Record<string, string> = {
  GET:    'bg-blue-50 text-blue-700 dark:bg-blue-400/10 dark:text-blue-300 border-blue-200 dark:border-blue-400/20',
  POST:   'bg-green-50 text-green-700 dark:bg-green-400/10 dark:text-green-300 border-green-200 dark:border-green-400/20',
  PUT:    'bg-amber-50 text-amber-700 dark:bg-amber-400/10 dark:text-amber-300 border-amber-200 dark:border-amber-400/20',
  DELETE: 'bg-red-50 text-red-700 dark:bg-red-400/10 dark:text-red-300 border-red-200 dark:border-red-400/20',
}

const getMethodBadgeClass = (method: string): string => {
  return requestMethodColorMap[method] ?? 'bg-gray-50 text-gray-700 dark:bg-gray-400/10 dark:text-gray-300 border-gray-200 dark:border-gray-400/20'
}

const formatDateTime = (value: string | null): string => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  const sec = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}:${sec}`
}
</script>

<template>
  <UModal
    :open="props.open"
    :title="`审计日志详情`"
    :description="`日志编号 #${props.detail?.id ?? ''}`"
    :ui="{
      overlay: 'bg-black/60 backdrop-blur-sm z-[130]',
      content: 'w-[min(92vw,640px)] rounded-2xl border border-gray-200/80 dark:border-dark-border bg-white dark:bg-dark-card overflow-hidden shadow-[0_32px_64px_rgba(15,23,42,0.28)] dark:shadow-[0_32px_64px_rgba(2,6,23,0.65)]',
      header: 'relative overflow-hidden px-6 pt-5 pb-4',
      title: 'relative z-10 text-lg font-bold tracking-tight text-gray-950 dark:text-white',
      description: 'relative z-10 mt-1 text-sm text-gray-500 dark:text-gray-400 tabular-nums',
      body: 'px-6 pb-2',
      footer: 'px-6 py-3 flex justify-end',
    }"
    @update:open="emit('update:open', $event)"
  >
    <template #headerExtra>
      <div
        v-if="props.detail"
        class="absolute inset-x-0 top-0 h-1"
        :class="props.detail.result === AuditResult.Success ? 'bg-emerald-500' : 'bg-red-500'"
      />
    </template>

    <template #body>
      <div v-if="props.detail" class="space-y-3">
        <!-- 操作摘要卡片 -->
        <div
          class="relative overflow-hidden rounded-xl border p-3"
          :class="props.detail.result === AuditResult.Success
            ? 'border-emerald-200/60 bg-emerald-50/40 dark:border-emerald-400/15 dark:bg-emerald-400/[0.06]'
            : 'border-red-200/60 bg-red-50/40 dark:border-red-400/15 dark:bg-red-400/[0.06]'"
        >
          <div class="flex items-center gap-3">
            <div
              class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl"
              :class="props.detail.result === AuditResult.Success
                ? 'bg-emerald-100 text-emerald-600 dark:bg-emerald-400/15 dark:text-emerald-300'
                : 'bg-red-100 text-red-600 dark:bg-red-400/15 dark:text-red-300'"
            >
              <i class="fas" :class="props.detail.result === AuditResult.Success ? 'fa-check' : 'fa-xmark'" />
            </div>
            <div class="min-w-0 flex-1">
              <div class="text-sm font-semibold text-gray-900 dark:text-white">
                {{ props.detail.result === AuditResult.Success ? '操作成功' : '操作失败' }}
              </div>
              <div class="mt-0.5 flex flex-wrap items-center gap-2 text-xs text-gray-500 dark:text-gray-400">
                <span class="tabular-nums">{{ formatDateTime(props.detail.createTime) }}</span>
                <span class="h-3 w-px bg-gray-300 dark:bg-gray-600" />
                <span class="tabular-nums">{{ props.detail.ip }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作信息 -->
        <div class="rounded-xl border border-gray-200/70 bg-gray-50/40 dark:border-dark-border/60 dark:bg-dark-bg/40">
          <div class="px-4 py-2.5 border-b border-gray-200/60 dark:border-dark-border/50">
            <div class="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400">
              <i class="fas fa-fingerprint text-[10px]" />
              操作信息
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3 p-3">
            <div>
              <div class="mb-1 flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
                <i class="fas fa-cube text-[10px]" />
                模块
              </div>
              <span
                class="inline-flex items-center gap-1.5 rounded-lg border px-2.5 py-1 text-xs font-semibold"
                :class="[getModuleStyle(props.detail.module).badge, getModuleStyle(props.detail.module).label]"
              >
                <span class="h-1.5 w-1.5 rounded-full" :class="getModuleStyle(props.detail.module).dot" />
                {{ AUDIT_MODULE_LABELS[props.detail.module] ?? props.detail.module }}
              </span>
            </div>
            <div>
              <div class="mb-1 flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
                <i class="fas fa-bolt text-[10px]" />
                动作
              </div>
              <span
                class="inline-flex items-center gap-1.5 rounded-lg border border-transparent px-2.5 py-1 text-xs font-semibold"
                :class="getActionStyle(props.detail.action).badge"
              >
                <i class="fas text-[10px]" :class="getActionStyle(props.detail.action).icon" />
                {{ AUDIT_ACTION_LABELS[props.detail.action] ?? props.detail.action }}
              </span>
            </div>
            <div>
              <div class="mb-1 flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
                <i class="fas fa-bullseye text-[10px]" />
                目标类型
              </div>
              <div class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ props.detail.targetType ?? '-' }}</div>
            </div>
            <div>
              <div class="mb-1 flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
                <i class="fas fa-hashtag text-[10px]" />
                目标ID
              </div>
              <div class="text-sm font-medium text-gray-900 dark:text-gray-100 tabular-nums">{{ props.detail.targetId ?? '-' }}</div>
            </div>
          </div>
        </div>

        <!-- 用户信息 -->
        <div class="rounded-xl border border-gray-200/70 bg-gray-50/40 dark:border-dark-border/60 dark:bg-dark-bg/40">
          <div class="px-4 py-2.5 border-b border-gray-200/60 dark:border-dark-border/50">
            <div class="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400">
              <i class="fas fa-user text-[10px]" />
              用户信息
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3 p-3">
            <div>
              <div class="mb-1 flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
                <i class="fas fa-id-badge text-[10px]" />
                用户ID
              </div>
              <div class="text-sm font-medium text-gray-900 dark:text-gray-100 tabular-nums">{{ props.detail.userId }}</div>
            </div>
            <div>
              <div class="mb-1 flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
                <i class="fas fa-users text-[10px]" />
                用户类型
              </div>
              <span
                class="inline-flex items-center gap-1.5 rounded-lg border px-2.5 py-1 text-xs font-semibold"
                :class="props.detail.userType === 'admin'
                  ? 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-400/10 dark:text-blue-300 dark:border-blue-400/20'
                  : 'bg-gray-50 text-gray-700 border-gray-200 dark:bg-gray-400/10 dark:text-gray-300 dark:border-gray-400/20'"
              >
                <i class="fas text-[10px]" :class="props.detail.userType === 'admin' ? 'fa-user-shield' : 'fa-user'" />
                {{ AUDIT_USER_TYPE_LABELS[props.detail.userType] ?? props.detail.userType }}
              </span>
            </div>
          </div>
        </div>

        <!-- 请求信息 -->
        <div class="rounded-xl border border-gray-200/70 bg-gray-50/40 dark:border-dark-border/60 dark:bg-dark-bg/40">
          <div class="px-4 py-2.5 border-b border-gray-200/60 dark:border-dark-border/50">
            <div class="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400">
              <i class="fas fa-network-wired text-[10px]" />
              请求信息
            </div>
          </div>
          <div class="space-y-2 p-3">
            <div>
              <div class="mb-1 flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
                <i class="fas fa-link text-[10px]" />
                请求URL
              </div>
              <div class="break-all rounded-lg border border-gray-200/80 bg-white px-3 py-1.5 text-xs font-mono text-gray-700 dark:border-dark-border/60 dark:bg-dark-bg dark:text-gray-300">
                {{ props.detail.requestUrl ?? '-' }}
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <div class="mb-1 flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
                  <i class="fas fa-code text-[10px]" />
                  请求方法
                </div>
                <span
                  v-if="props.detail.requestMethod"
                  class="inline-flex items-center rounded-md border px-2 py-0.5 text-xs font-bold font-mono"
                  :class="getMethodBadgeClass(props.detail.requestMethod)"
                >
                  {{ props.detail.requestMethod }}
                </span>
                <span v-else class="text-sm text-gray-900 dark:text-gray-100">-</span>
              </div>
              <div>
                <div class="mb-1 flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
                  <i class="fas fa-clock text-[10px]" />
                  创建时间
                </div>
                <div class="text-sm font-medium text-gray-900 dark:text-gray-100 tabular-nums">{{ formatDateTime(props.detail.createTime) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 错误信息 -->
        <div
          v-if="props.detail.result === AuditResult.Failed && props.detail.errorMsg"
          class="relative overflow-hidden rounded-xl border border-red-200/60 bg-red-50/40 p-3 dark:border-red-400/15 dark:bg-red-400/[0.06]"
        >
          <div class="absolute inset-x-0 top-0 h-0.5 bg-red-400 dark:bg-red-500/60" />
          <div class="flex items-start gap-3">
            <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-red-100 text-red-600 dark:bg-red-400/15 dark:text-red-300">
              <i class="fas fa-circle-exclamation text-sm" />
            </div>
            <div class="min-w-0 flex-1">
              <div class="mb-1 text-xs font-semibold uppercase tracking-wider text-red-700 dark:text-red-300">错误详情</div>
              <div class="text-sm leading-relaxed text-red-800 dark:text-red-200">{{ props.detail.errorMsg }}</div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template #footer>
      <button
        type="button"
        class="inline-flex h-10 cursor-pointer items-center justify-center rounded-xl border-0 bg-[rgb(var(--cf-color-primary-500-rgb)/1)] px-5 text-sm font-medium text-white transition-all hover:bg-[rgb(var(--cf-color-primary-600-rgb)/1)] active:scale-[0.97] shadow-sm"
        @click="emit('update:open', false)"
      >
        关闭
      </button>
    </template>
  </UModal>
</template>

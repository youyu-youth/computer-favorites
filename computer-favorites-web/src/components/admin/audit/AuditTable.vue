<script setup lang="ts">
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import type { AuditAction, AuditLogListItem, AuditModule, AuditUserType } from '@/types/audit-log'
import { AuditResult, AUDIT_MODULE_LABELS, AUDIT_ACTION_LABELS, AUDIT_USER_TYPE_LABELS } from '@/types/audit-log'

defineProps<{
  rows: AuditLogListItem[]
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'view', id: number): void
}>()

const tablePt = {
  table: { class: 'w-full border-separate border-spacing-0' },
  thead: { class: 'bg-gray-50 dark:bg-dark-bg/60' },
  headerRow: { class: 'border-b border-gray-200 dark:border-dark-border' },
  bodyRow: {
    class: 'border-b border-gray-100 transition-colors hover:bg-gray-50/70 dark:border-dark-border/70 dark:hover:bg-dark-bg/40',
  },
  emptyMessage: { class: 'px-4 py-10 text-center text-sm text-gray-500 dark:text-gray-400' },
  loadingOverlay: { class: 'bg-white/70 dark:bg-dark-card/70' },
}

const headerClass = 'px-4 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400'
const bodyClass = 'px-4 py-3 text-sm text-gray-600 dark:text-gray-300'

const formatDateTime = (value: string | null): string => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}`
}

const moduleColorMap: Record<string, string> = {
  auth: 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-300',
  website: 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900/20 dark:text-indigo-300',
  user: 'bg-slate-100 text-slate-800 dark:bg-slate-700/30 dark:text-slate-300',
  admin: 'bg-sky-100 text-sky-800 dark:bg-sky-900/20 dark:text-sky-300',
  comment: 'bg-cyan-100 text-cyan-800 dark:bg-cyan-900/20 dark:text-cyan-300',
  report: 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-300',
  feedback: 'bg-amber-100 text-amber-800 dark:bg-amber-900/20 dark:text-amber-300',
  announcement: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/20 dark:text-emerald-300',
  system: 'bg-gray-100 text-gray-800 dark:bg-gray-700/30 dark:text-gray-300',
}

const getModuleBadgeClass = (module: string): string => {
  return moduleColorMap[module] ?? 'bg-gray-100 text-gray-800 dark:bg-gray-700/30 dark:text-gray-300'
}

const actionColorMap: Record<string, string> = {
  login: 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-300',
  logout: 'bg-gray-100 text-gray-800 dark:bg-gray-700/30 dark:text-gray-300',
  create: 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-300',
  update: 'bg-amber-100 text-amber-800 dark:bg-amber-900/20 dark:text-amber-300',
  delete: 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-300',
  review: 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900/20 dark:text-indigo-300',
  export: 'bg-cyan-100 text-cyan-800 dark:bg-cyan-900/20 dark:text-cyan-300',
  import: 'bg-sky-100 text-sky-800 dark:bg-sky-900/20 dark:text-sky-300',
  status: 'bg-amber-100 text-amber-800 dark:bg-amber-900/20 dark:text-amber-300',
  ban: 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-300',
  unban: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/20 dark:text-emerald-300',
}

const getActionBadgeClass = (action: string): string => {
  return actionColorMap[action] ?? 'bg-gray-100 text-gray-800 dark:bg-gray-700/30 dark:text-gray-300'
}
</script>

<template>
  <div class="overflow-x-auto">
    <DataTable
      :value="rows"
      :loading="loading"
      dataKey="id"
      class="min-w-[1100px]"
      :pt="tablePt"
    >
      <Column field="userType" header="用户类型" :headerClass="headerClass" :bodyClass="bodyClass" style="width:100px">
        <template #body="{ data }">
          <span
            class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
            :class="
              data.userType === 'admin'
                ? 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-300'
                : 'bg-gray-100 text-gray-800 dark:bg-gray-700/30 dark:text-gray-300'
            "
          >
            {{ AUDIT_USER_TYPE_LABELS[data.userType as AuditUserType] ?? data.userType }}
          </span>
        </template>
      </Column>

      <Column field="module" header="模块" :headerClass="headerClass" :bodyClass="bodyClass" style="width:100px">
        <template #body="{ data }">
          <span
            class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
            :class="getModuleBadgeClass(data.module)"
          >
            {{ AUDIT_MODULE_LABELS[data.module as AuditModule] ?? data.module }}
          </span>
        </template>
      </Column>

      <Column field="action" header="操作" :headerClass="headerClass" :bodyClass="bodyClass" style="width:90px">
        <template #body="{ data }">
          <span
            class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
            :class="getActionBadgeClass(data.action)"
          >
            {{ AUDIT_ACTION_LABELS[data.action as AuditAction] ?? data.action }}
          </span>
        </template>
      </Column>

      <Column field="targetType" header="目标类型" :headerClass="headerClass" :bodyClass="bodyClass" style="width:100px">
        <template #body="{ data }">
          <span class="text-sm text-gray-600 dark:text-gray-300">{{ data.targetType ?? '-' }}</span>
        </template>
      </Column>

      <Column field="targetId" header="目标ID" :headerClass="headerClass" bodyClass="px-4 py-3 text-sm text-right text-gray-600 dark:text-gray-300" style="width:90px">
        <template #body="{ data }">
          {{ data.targetId ?? '-' }}
        </template>
      </Column>

      <Column field="result" header="结果" :headerClass="headerClass" :bodyClass="bodyClass" style="width:80px">
        <template #body="{ data }">
          <span
            class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
            :class="
              data.result === AuditResult.Success
                ? 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-300'
                : 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-300'
            "
          >
            <span
              class="mr-1 h-1.5 w-1.5 rounded-full"
              :class="data.result === AuditResult.Success ? 'bg-green-500' : 'bg-red-500'"
            ></span>
            {{ data.result === AuditResult.Success ? '成功' : '失败' }}
          </span>
        </template>
      </Column>

      <Column field="ip" header="IP地址" :headerClass="headerClass" :bodyClass="bodyClass" style="width:130px" />

      <Column field="createTime" header="时间" :headerClass="headerClass" :bodyClass="bodyClass" style="width:170px">
        <template #body="{ data }">
          {{ formatDateTime(data.createTime) }}
        </template>
      </Column>

      <Column header="操作" :headerClass="headerClass" :bodyClass="bodyClass" style="width:80px">
        <template #body="{ data }">
          <button
            type="button"
            class="cursor-pointer text-[rgb(var(--cf-color-primary-500-rgb)/1)] hover:text-[rgb(var(--cf-color-primary-600-rgb)/1)] transition-colors"
            @click="emit('view', data.id)"
          >
            <i class="fas fa-eye"></i>
          </button>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

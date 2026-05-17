<template>
  <div class="flex justify-center my-4">
    <div
      class="w-full max-w-md rounded-xl border overflow-hidden"
      :class="riskBorderClass"
    >
      <!-- Header -->
      <div class="px-4 py-2.5 flex items-center gap-2" :class="riskBgClass">
        <svg class="w-4 h-4 shrink-0 text-amber-600 dark:text-amber-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
        </svg>
        <span class="text-sm font-semibold text-gray-900 dark:text-gray-100">执行计划确认</span>
        <span class="ml-auto text-xs px-1.5 py-0.5 rounded-full" :class="riskBadgeClass">
          {{ riskLabel }}
        </span>
      </div>

      <!-- Body -->
      <div class="px-4 py-3 bg-white dark:bg-gray-900">
        <p class="text-sm text-gray-700 dark:text-gray-300">{{ plan.summary }}</p>

        <div class="flex gap-2 mt-3" v-if="plan.status === 'wait_confirm'">
          <button
            class="px-3 py-1.5 text-xs rounded-lg border border-gray-300 dark:border-gray-600
                   text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800
                   transition-colors cursor-pointer"
            @click="$emit('reject', plan.planId)"
          >
            拒绝
          </button>
          <button
            class="px-3 py-1.5 text-xs rounded-lg bg-green-500 text-white hover:bg-green-600
                   transition-colors cursor-pointer"
            @click="$emit('confirm', plan.planId)"
          >
            确认执行
          </button>
        </div>
        <div v-else class="mt-2 text-xs text-gray-500">
          {{ plan.status === 'approved' ? '已确认执行' : '已拒绝' }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AgentPlan } from '@/types/agent'

defineOptions({ name: 'AgentPlanCard' })

const props = defineProps<{
  plan: AgentPlan
}>()

defineEmits<{
  confirm: [planId: string]
  reject: [planId: string]
}>()

const riskLabel = computed(() => {
  switch (props.plan.riskLevel) {
    case 'high': return '高风险'
    case 'medium': return '中风险'
    case 'low': return '低风险'
    default: return props.plan.riskLevel
  }
})

const riskBorderClass = computed(() => {
  switch (props.plan.riskLevel) {
    case 'high': return 'border-red-300 dark:border-red-700'
    case 'medium': return 'border-amber-300 dark:border-amber-700'
    case 'low': return 'border-green-300 dark:border-green-700'
    default: return 'border-gray-300 dark:border-gray-700'
  }
})

const riskBgClass = computed(() => {
  switch (props.plan.riskLevel) {
    case 'high': return 'bg-red-50 dark:bg-red-950/30'
    case 'medium': return 'bg-amber-50 dark:bg-amber-950/30'
    case 'low': return 'bg-green-50 dark:bg-green-950/30'
    default: return 'bg-gray-50 dark:bg-gray-800'
  }
})

const riskBadgeClass = computed(() => {
  switch (props.plan.riskLevel) {
    case 'high': return 'bg-red-100 dark:bg-red-900/50 text-red-700 dark:text-red-400'
    case 'medium': return 'bg-amber-100 dark:bg-amber-900/50 text-amber-700 dark:text-amber-400'
    case 'low': return 'bg-green-100 dark:bg-green-900/50 text-green-700 dark:text-green-400'
    default: return 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400'
  }
})
</script>

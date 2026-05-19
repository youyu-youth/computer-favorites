<template>
  <div class="flex justify-center my-4">
    <div
      class="cf-plan-card group relative w-full max-w-md overflow-hidden rounded-2xl bg-white/95 dark:bg-stone-900/70 ring-1 ring-stone-200/80 dark:ring-stone-700/60 backdrop-blur-sm shadow-[inset_0_1px_0_rgba(255,255,255,0.6),0_22px_44px_-22px_rgba(15,23,42,0.18)]"
    >
      <!-- 左侧风险等级 rail（4px 高对比，避免一圈彩边） -->
      <span aria-hidden="true" class="cf-plan-rail absolute left-0 top-0 bottom-0 w-[3px] rounded-r-full" :class="railClass" />

      <!-- Header -->
      <div class="flex items-center gap-2.5 px-5 pt-4 pb-3">
        <span class="grid h-7 w-7 shrink-0 place-items-center rounded-lg ring-1 ring-inset" :class="iconWrapClass">
          <component :is="riskIcon" :size="14" :stroke-width="1.75" />
        </span>
        <div class="flex flex-1 items-baseline gap-2 min-w-0">
          <span class="text-[13px] font-semibold tracking-tight text-stone-900 dark:text-stone-100">执行计划确认</span>
          <span class="text-[10px] font-medium tracking-[0.08em] uppercase text-stone-400 dark:text-stone-500">Plan</span>
        </div>
        <span class="text-[11px] font-medium tabular-nums px-2 py-0.5 rounded-full ring-1 ring-inset" :class="badgeClass">
          {{ riskLabel }}
        </span>
      </div>

      <!-- Body -->
      <div class="px-5 pb-4">
        <p class="text-[13px] text-stone-700 dark:text-stone-200 leading-relaxed">{{ plan.summary }}</p>

        <div v-if="plan.status === 'wait_confirm'" class="mt-4 flex items-center gap-2">
          <button
            type="button"
            class="cf-plan-btn cf-plan-btn--ghost"
            @click="$emit('reject', plan.planId)"
          >
            拒绝
          </button>
          <button
            type="button"
            class="cf-plan-btn cf-plan-btn--primary"
            @click="$emit('confirm', plan.planId)"
          >
            <Check :size="13" :stroke-width="2.25" />
            确认执行
          </button>
        </div>
        <div v-else class="mt-3 inline-flex items-center gap-1.5 rounded-full bg-stone-900/[0.04] dark:bg-white/[0.05] px-2.5 py-0.5 text-[11px] text-stone-500 dark:text-stone-400">
          <span class="h-1.5 w-1.5 rounded-full" :class="plan.status === 'approved' ? 'bg-emerald-500' : 'bg-stone-400'" />
          {{ plan.status === 'approved' ? '已确认执行' : '已拒绝' }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ShieldCheck, ShieldAlert, AlertOctagon, Check } from 'lucide-vue-next'
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

/** 风险等级 → lucide 图标（绿盾 / 警示盾 / 八角警告） */
const riskIcon = computed(() => {
  switch (props.plan.riskLevel) {
    case 'high': return AlertOctagon
    case 'medium': return ShieldAlert
    case 'low': return ShieldCheck
    default: return ShieldAlert
  }
})

/** 左侧 rail 颜色 */
const railClass = computed(() => {
  switch (props.plan.riskLevel) {
    case 'high': return 'bg-rose-500'
    case 'medium': return 'bg-amber-500'
    case 'low': return 'bg-emerald-500'
    default: return 'bg-stone-300'
  }
})

/** 图标包装：tinted bg + ring */
const iconWrapClass = computed(() => {
  switch (props.plan.riskLevel) {
    case 'high': return 'bg-rose-500/[0.08] text-rose-500 ring-rose-500/15'
    case 'medium': return 'bg-amber-500/[0.08] text-amber-600 ring-amber-500/15 dark:text-amber-400'
    case 'low': return 'bg-emerald-500/[0.08] text-emerald-600 ring-emerald-500/15 dark:text-emerald-400'
    default: return 'bg-stone-500/[0.08] text-stone-500 ring-stone-500/15'
  }
})

/** 风险 badge：tinted pill */
const badgeClass = computed(() => {
  switch (props.plan.riskLevel) {
    case 'high': return 'bg-rose-500/[0.08] text-rose-600 ring-rose-500/15 dark:text-rose-400'
    case 'medium': return 'bg-amber-500/[0.08] text-amber-700 ring-amber-500/15 dark:text-amber-400'
    case 'low': return 'bg-emerald-500/[0.08] text-emerald-700 ring-emerald-500/15 dark:text-emerald-400'
    default: return 'bg-stone-500/[0.08] text-stone-600 ring-stone-500/15'
  }
})
</script>

<style scoped>
.cf-plan-card {
  animation: cf-plan-in 420ms cubic-bezier(0.16, 1, 0.3, 1) both;
}
@keyframes cf-plan-in {
  from { opacity: 0; transform: translateY(6px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.cf-plan-rail {
  animation: cf-rail-grow 480ms cubic-bezier(0.16, 1, 0.3, 1) both;
  transform-origin: top center;
}
@keyframes cf-rail-grow {
  from { transform: scaleY(0); opacity: 0; }
  to { transform: scaleY(1); opacity: 1; }
}

/* 计划卡按钮：以 brand 主色为唯一 accent，避免冲突的绿色 CTA */
.cf-plan-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  height: 1.875rem;
  padding-inline: 0.875rem;
  border-radius: 0.625rem;
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: -0.01em;
  cursor: pointer;
  transition:
    transform 160ms cubic-bezier(0.16, 1, 0.3, 1),
    background-color 200ms cubic-bezier(0.16, 1, 0.3, 1),
    color 200ms cubic-bezier(0.16, 1, 0.3, 1),
    box-shadow 200ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-plan-btn:active {
  transform: scale(0.97) translateY(1px);
}

.cf-plan-btn--ghost {
  color: rgb(82 82 91 / 1);
  background-color: transparent;
  box-shadow: inset 0 0 0 1px rgb(0 0 0 / 0.08);
}
.cf-plan-btn--ghost:hover {
  background-color: rgb(0 0 0 / 0.04);
  color: rgb(24 24 27 / 1);
}

.cf-plan-btn--primary {
  color: white;
  background-color: rgb(var(--cf-color-primary-500-rgb) / 1);
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.22),
    inset 0 -1px 0 0 rgba(0, 0, 0, 0.08),
    0 1px 2px 0 rgba(15, 23, 42, 0.08),
    0 8px 18px -6px rgb(var(--cf-color-primary-500-rgb) / 0.45);
}
.cf-plan-btn--primary:hover {
  background-color: rgb(var(--cf-color-primary-600-rgb) / 1);
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.2),
    inset 0 -1px 0 0 rgba(0, 0, 0, 0.12),
    0 2px 4px 0 rgba(15, 23, 42, 0.1),
    0 12px 24px -8px rgb(var(--cf-color-primary-500-rgb) / 0.55);
}

@media (prefers-reduced-motion: reduce) {
  .cf-plan-card,
  .cf-plan-rail {
    animation: none;
    opacity: 1;
    transform: none;
  }
}
</style>

<style>
html.dark .cf-plan-btn--ghost {
  color: rgb(168 162 158 / 1);
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 0.1);
}
html.dark .cf-plan-btn--ghost:hover {
  background-color: rgb(255 255 255 / 0.06);
  color: rgb(231 229 228 / 1);
}
</style>

<template>
  <div class="thinking-panel text-xs">
    <!-- Toggle 按钮：胶囊 + chevron 旋转动画 -->
    <button
      type="button"
      class="cf-thinking-toggle group inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[11px] font-medium tracking-tight text-stone-500 dark:text-stone-400 ring-1 ring-inset ring-stone-200/80 dark:ring-stone-700/70 bg-white/60 dark:bg-stone-900/40 backdrop-blur-sm transition-all duration-200 ease-[cubic-bezier(0.16,1,0.3,1)] hover:text-stone-800 dark:hover:text-stone-100 hover:ring-stone-300 dark:hover:ring-stone-600 active:scale-[0.97] cursor-pointer"
      :aria-expanded="!collapsed"
      @click="toggle"
    >
      <ChevronRight
        class="text-stone-400 transition-transform duration-300 ease-[cubic-bezier(0.16,1,0.3,1)]"
        :class="{ 'rotate-90 text-primary-500': !collapsed }"
        :size="12"
        :stroke-width="2"
      />
      <Sparkles class="text-primary-500" :size="11" :stroke-width="2" />
      <span>
        思考过程
        <span class="tabular-nums">· {{ steps.length }} 步</span>
        <template v-if="toolCallCount > 0"><span class="tabular-nums"> · 工具 {{ toolCallCount }}</span></template>
      </span>
    </button>

    <!-- 展开的步骤：左侧 timeline rail -->
    <Transition name="cf-thinking-expand">
      <div v-if="!collapsed" class="cf-thinking-expand-wrap relative mt-2 pl-4">
        <!-- 渐变 timeline rail -->
        <span class="cf-thinking-rail" aria-hidden="true" />

        <ol class="space-y-1.5">
          <li
            v-for="(step, idx) in steps"
            :key="idx"
            class="cf-thinking-step relative"
            :style="{ '--cf-i': idx }"
          >
            <!-- 步骤节点圆点 -->
            <span
              class="cf-thinking-dot absolute -left-4 top-[7px] grid h-2.5 w-2.5 -translate-x-[5px] place-items-center rounded-full ring-2"
              :class="dotClass(step)"
              aria-hidden="true"
            />

            <!-- thinking -->
            <div v-if="step.type === 'thinking'" class="flex items-start gap-2 text-stone-600 dark:text-stone-300">
              <span class="text-[11px] font-medium tracking-tight">
                <span class="text-stone-400 dark:text-stone-500 tabular-nums">#{{ step.step }}</span>
                · 分析中
              </span>
              <span class="cf-thinking-typing inline-flex items-center gap-0.5 text-stone-400 dark:text-stone-500" aria-hidden="true">
                <span class="cf-typing-dot" style="--cf-i: 0" />
                <span class="cf-typing-dot" style="--cf-i: 1" />
                <span class="cf-typing-dot" style="--cf-i: 2" />
              </span>
            </div>

            <!-- tool_call -->
            <div v-else-if="step.type === 'tool_call'" class="flex items-start gap-2">
              <Wrench class="mt-[3px] shrink-0 text-sky-500" :size="11" :stroke-width="2" />
              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-baseline gap-x-1.5 gap-y-0.5 text-[11px] text-stone-600 dark:text-stone-300">
                  <span>调用工具</span>
                  <code class="cf-tool-tag font-mono text-[10.5px] tracking-tight px-1.5 py-px rounded-md bg-sky-500/[0.08] text-sky-700 dark:text-sky-300 ring-1 ring-inset ring-sky-500/15">
                    {{ step.toolName }}
                  </code>
                </div>
                <p v-if="step.args" class="mt-0.5 text-[10.5px] leading-relaxed text-stone-400 dark:text-stone-500 break-all">
                  {{ truncate(String(step.args), 120) }}
                </p>
              </div>
            </div>

            <!-- tool_result -->
            <div v-else-if="step.type === 'tool_result'" class="flex items-start gap-2">
              <component
                :is="step.result ? Check : X"
                class="mt-[3px] shrink-0"
                :class="step.result ? 'text-emerald-500' : 'text-rose-400'"
                :size="11"
                :stroke-width="2.5"
              />
              <span class="text-[11px] text-stone-500 dark:text-stone-400 leading-relaxed break-words">
                {{ truncate(String(step.result || '无结果'), 120) }}
              </span>
            </div>
          </li>
        </ol>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ChevronRight, Sparkles, Wrench, Check, X } from 'lucide-vue-next'
import type { ThinkingStep } from '@/types/agent'

defineOptions({ name: 'AgentThinkingSteps' })

const props = defineProps<{
  steps: ThinkingStep[]
}>()

const collapsed = ref(true)

function toggle() {
  collapsed.value = !collapsed.value
}

const toolCallCount = computed(() =>
  props.steps.filter((s) => s.type === 'tool_call').length,
)

function truncate(text: string, maxLen: number): string {
  if (text.length <= maxLen) return text
  return text.slice(0, maxLen) + '...'
}

/** 不同步骤类型的节点 dot 配色，统一通过 ring 表达层次 */
function dotClass(step: ThinkingStep): string {
  if (step.type === 'thinking') {
    return 'bg-primary-500 ring-primary-500/20 dark:ring-primary-500/30'
  }
  if (step.type === 'tool_call') {
    return 'bg-sky-500 ring-sky-500/20 dark:ring-sky-500/30'
  }
  if (step.type === 'tool_result') {
    return step.result
      ? 'bg-emerald-500 ring-emerald-500/20 dark:ring-emerald-500/30'
      : 'bg-rose-400 ring-rose-400/20 dark:ring-rose-400/30'
  }
  return 'bg-stone-300 ring-stone-300/30'
}
</script>

<style scoped>
/* 渐变 timeline rail：从主色到透明，模拟 AI 思考流 */
.cf-thinking-rail {
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 1.5px;
  border-radius: 999px;
  background: linear-gradient(
    180deg,
    rgb(var(--cf-color-primary-500-rgb) / 0.5) 0%,
    rgb(var(--cf-color-primary-500-rgb) / 0.18) 60%,
    transparent 100%
  );
}

/* 步骤入场动画：stagger fade-up */
.cf-thinking-step {
  opacity: 0;
  transform: translateY(4px);
  animation: cf-thinking-step-in 380ms cubic-bezier(0.16, 1, 0.3, 1) forwards;
  animation-delay: calc(var(--cf-i, 0) * 60ms + 80ms);
}
@keyframes cf-thinking-step-in {
  to { opacity: 1; transform: translateY(0); }
}

/* 思考 typing dots */
.cf-typing-dot {
  width: 3px;
  height: 3px;
  border-radius: 999px;
  background-color: currentColor;
  animation: cf-typing 1.2s cubic-bezier(0.16, 1, 0.3, 1) infinite;
  animation-delay: calc(var(--cf-i, 0) * 140ms);
}
@keyframes cf-typing {
  0%, 80%, 100% { opacity: 0.25; transform: translateY(0); }
  40% { opacity: 1; transform: translateY(-1.5px); }
}

/* 容器展开过渡：高度 + opacity */
.cf-thinking-expand-enter-active,
.cf-thinking-expand-leave-active {
  transition:
    grid-template-rows 320ms cubic-bezier(0.16, 1, 0.3, 1),
    opacity 240ms cubic-bezier(0.16, 1, 0.3, 1);
  display: grid;
  grid-template-rows: 1fr;
}
.cf-thinking-expand-enter-from,
.cf-thinking-expand-leave-to {
  grid-template-rows: 0fr;
  opacity: 0;
}

@media (prefers-reduced-motion: reduce) {
  .cf-thinking-step,
  .cf-typing-dot {
    animation: none;
    opacity: 1;
    transform: none;
  }
}
</style>

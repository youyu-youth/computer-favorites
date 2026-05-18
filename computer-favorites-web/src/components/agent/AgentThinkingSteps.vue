<template>
  <div class="thinking-panel text-xs">
    <button
      type="button"
      class="cf-thinking-toggle group inline-flex cursor-pointer items-center gap-1.5 rounded-full border border-stone-200 bg-white px-3 py-1.5 text-[11px] font-medium tracking-tight text-stone-600 transition-colors duration-200 hover:border-primary-500/50 hover:text-stone-900 dark:border-white/10 dark:bg-[#0f0f11] dark:text-stone-400 dark:hover:text-stone-100"
      :aria-expanded="!collapsed"
      @click="toggle"
    >
      <ChevronRight
        class="text-stone-400 transition-transform duration-300 ease-[cubic-bezier(0.16,1,0.3,1)]"
        :class="{ 'rotate-90 text-primary-500': !collapsed }"
        :size="12"
        :stroke-width="2"
      />
      <Sparkles class="text-primary-400" :size="11" :stroke-width="2" />
      <span>
        思考过程
        <span class="tabular-nums">· {{ steps.length }} 步</span>
        <template v-if="toolCallCount > 0"
          ><span class="tabular-nums"> · 工具 {{ toolCallCount }}</span></template
        >
      </span>
    </button>

    <Transition name="cf-thinking-expand">
      <div v-if="!collapsed" class="cf-thinking-expand-wrap relative mt-2 pl-4">
        <span class="cf-thinking-rail" aria-hidden="true" />

        <ol class="space-y-1.5">
          <li
            v-for="(step, idx) in steps"
            :key="idx"
            class="cf-thinking-step relative"
            :style="{ '--cf-i': idx }"
          >
            <span
              class="cf-thinking-dot absolute -left-4 top-[7px] grid h-2.5 w-2.5 -translate-x-[5px] place-items-center rounded-full ring-2"
              :class="dotClass(step)"
              aria-hidden="true"
            />

            <div
              v-if="step.type === 'thinking'"
              class="flex items-start gap-2 text-stone-700 dark:text-stone-300"
            >
              <span class="text-[11px] font-medium tracking-tight">
                <span class="tabular-nums text-stone-500">#{{ step.step }}</span>
                · 分析中
              </span>
              <span
                class="cf-thinking-typing inline-flex items-center gap-0.5 text-stone-500"
                aria-hidden="true"
              >
                <span class="cf-typing-dot" style="--cf-i: 0" />
                <span class="cf-typing-dot" style="--cf-i: 1" />
                <span class="cf-typing-dot" style="--cf-i: 2" />
              </span>
            </div>

            <div v-else-if="step.type === 'tool_call'" class="flex items-start gap-2">
              <Wrench class="mt-[3px] shrink-0 text-sky-500" :size="11" :stroke-width="2" />
              <div class="min-w-0 flex-1">
                <div
                  class="flex flex-wrap items-baseline gap-x-1.5 gap-y-0.5 text-[11px] text-stone-700 dark:text-stone-300"
                >
                  <span>调用工具</span>
                  <code
                    class="cf-tool-tag rounded-md bg-sky-500/[0.08] px-1.5 py-px font-mono text-[10.5px] tracking-tight text-sky-600 ring-1 ring-inset ring-sky-500/15 dark:text-sky-300"
                  >
                    {{ step.toolName }}
                  </code>
                </div>
                <p
                  v-if="step.args"
                  class="mt-0.5 break-all text-[10.5px] leading-relaxed text-stone-500"
                >
                  {{ truncate(String(step.args), 120) }}
                </p>
              </div>
            </div>

            <div v-else-if="step.type === 'tool_result'" class="flex items-start gap-2">
              <component
                :is="step.result ? Check : X"
                class="mt-[3px] shrink-0"
                :class="step.result ? 'text-emerald-500' : 'text-rose-400'"
                :size="11"
                :stroke-width="2.5"
              />
              <span class="break-words text-[11px] leading-relaxed text-stone-400">
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

const toolCallCount = computed(() => props.steps.filter((s) => s.type === 'tool_call').length)

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
.cf-thinking-rail {
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 1.5px;
  border-radius: 999px;
  background-color: rgb(var(--cf-color-primary-500-rgb) / 0.35);
}

.cf-thinking-step {
  opacity: 0;
  transform: translateY(4px);
  animation: cf-thinking-step-in 380ms cubic-bezier(0.16, 1, 0.3, 1) forwards;
  animation-delay: calc(var(--cf-i, 0) * 60ms + 80ms);
}
@keyframes cf-thinking-step-in {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.cf-typing-dot {
  width: 3px;
  height: 3px;
  border-radius: 999px;
  background-color: currentColor;
  animation: cf-typing 1.2s cubic-bezier(0.16, 1, 0.3, 1) infinite;
  animation-delay: calc(var(--cf-i, 0) * 140ms);
}
@keyframes cf-typing {
  0%,
  80%,
  100% {
    opacity: 0.25;
    transform: translateY(0);
  }
  40% {
    opacity: 1;
    transform: translateY(-1.5px);
  }
}

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

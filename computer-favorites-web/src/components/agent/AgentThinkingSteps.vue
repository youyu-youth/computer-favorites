<template>
  <div class="thinking-panel text-xs">
    <!-- Toggle button -->
    <button
      class="flex items-center gap-1.5 px-2 py-1 rounded-md
             bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400
             hover:text-gray-700 dark:hover:text-gray-200 transition-colors cursor-pointer"
      @click="toggle"
    >
      <svg
        class="w-3 h-3 transition-transform duration-200"
        :class="{ 'rotate-90': !collapsed }"
        fill="none" stroke="currentColor" viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
      </svg>
      <span>
        思考过程 · {{ steps.length }} 步
        <template v-if="toolCallCount > 0">· 工具调用 {{ toolCallCount }} 次</template>
      </span>
    </button>

    <!-- Expanded steps -->
    <div
      v-if="!collapsed"
      class="mt-1.5 space-y-1 pl-4 border-l-2 border-gray-200 dark:border-gray-700"
    >
      <div v-for="(step, idx) in steps" :key="idx" class="text-gray-500 dark:text-gray-400 py-0.5">
        <!-- thinking step -->
        <div v-if="step.type === 'thinking'" class="flex items-start gap-1.5">
          <span class="text-amber-500 shrink-0 mt-0.5">
            <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09z" />
            </svg>
          </span>
          <span>第{{ step.step }}步：分析中...</span>
        </div>

        <!-- tool_call step -->
        <div v-else-if="step.type === 'tool_call'" class="flex items-start gap-1.5">
          <span class="text-sky-500 shrink-0 mt-0.5">
            <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M14.25 9.75L16.5 12l-2.25 2.25m-4.5 0L7.5 12l2.25-2.25M6 20.25h12A2.25 2.25 0 0020.25 18V6A2.25 2.25 0 0018 3.75H6A2.25 2.25 0 003.75 6v12A2.25 2.25 0 006 20.25z" />
            </svg>
          </span>
          <div>
            <span>调用工具: </span>
            <code class="font-mono text-[11px] bg-gray-100 dark:bg-gray-800 px-1 rounded">
              {{ step.toolName }}
            </code>
            <div v-if="step.args" class="text-[11px] text-gray-400 dark:text-gray-500 mt-0.5">
              {{ truncate(String(step.args), 120) }}
            </div>
          </div>
        </div>

        <!-- tool_result step -->
        <div v-else-if="step.type === 'tool_result'" class="flex items-start gap-1.5">
          <span class="shrink-0 mt-0.5">
            <svg v-if="!step.result" class="w-3 h-3 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
            <svg v-else class="w-3 h-3 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </span>
          <span class="text-gray-400 dark:text-gray-500">
            {{ truncate(String(step.result || '无结果'), 120) }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
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
</script>

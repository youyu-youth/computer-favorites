<template>
  <div class="flex min-h-full items-center px-5 py-10 sm:px-8 md:px-12">
    <div class="mx-auto w-full max-w-4xl">
      <div class="cf-fade-up mb-8 flex items-center gap-2" style="--cf-i: 0">
        <span
          class="inline-flex items-center gap-1.5 rounded-full border border-primary-500/20 bg-primary-500/[0.08] px-3 py-1 text-[12px] font-semibold text-primary-400"
        >
          <span class="h-1.5 w-1.5 rounded-full bg-primary-500" aria-hidden="true" />
          Nova AI 在线
        </span>
        <span class="text-[12px] font-medium text-stone-600">私密 · 上下文感知 · 支持工具调用</span>
      </div>

      <div class="max-w-2xl">
        <h2
          class="cf-fade-up m-0 text-4xl font-semibold leading-tight tracking-[-0.04em] text-stone-950 md:text-5xl dark:text-white"
          style="--cf-i: 1"
        >
          今天想让 AI 帮你解决什么问题？
        </h2>
        <p
          class="cf-fade-up m-0 mt-4 max-w-[40rem] text-[15px] leading-relaxed text-stone-500"
          style="--cf-i: 2"
        >
          可以让 Nova AI 帮你分析代码、拆解算法、规划学习路线，也可以协助完成网站投稿与资料整理。
        </p>
      </div>

      <div class="mt-10 grid grid-cols-1 gap-3 sm:grid-cols-2">
        <button
          v-for="(item, idx) in quickPrompts"
          :key="item.label"
          class="cf-prompt-card cf-fade-up group relative flex cursor-pointer items-start gap-3 rounded-2xl border border-stone-200 bg-white px-4 py-4 text-left shadow-sm transition-colors duration-200 hover:border-primary-500/60 hover:bg-stone-50 dark:border-white/10 dark:bg-[#0f0f11] dark:hover:bg-[#141414]"
          :style="{ '--cf-i': idx + 3 }"
          @click="$emit('quickPrompt', item.label)"
        >
          <span
            class="grid h-9 w-9 shrink-0 place-items-center rounded-xl border border-primary-500/20 bg-primary-500/[0.08] text-primary-400 transition-colors duration-200 group-hover:bg-primary-500 group-hover:text-white"
          >
            <component :is="item.icon" :size="16" :stroke-width="1.75" />
          </span>
          <span class="min-w-0 flex-1">
            <span class="block text-[11px] font-semibold tracking-[0.08em] text-stone-600">
              {{ item.kind }}
            </span>
            <span
              class="mt-1 block text-[14px] font-semibold leading-snug text-stone-900 dark:text-stone-100"
            >
              {{ item.label }}
            </span>
          </span>
          <ArrowUpRight
            class="absolute right-4 top-4 text-stone-700 transition-colors duration-200 group-hover:text-primary-400"
            :size="15"
            :stroke-width="1.75"
          />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Binary, GitBranch, Gauge, Compass, ArrowUpRight } from 'lucide-vue-next'

defineOptions({ name: 'AgentWelcomeScreen' })

defineEmits<{
  quickPrompt: [prompt: string]
}>()

const quickPrompts = [
  { kind: '算法分析', label: '解释时间复杂度 O(n log n)', icon: Binary },
  { kind: '工程协作', label: 'Git Flow 和 GitHub Flow 的区别', icon: GitBranch },
  { kind: '性能优化', label: '如何优化 Java 代码性能', icon: Gauge },
  { kind: '学习路线', label: '推荐计算机专业学习路线', icon: Compass },
]
</script>

<style scoped>
.cf-fade-up {
  opacity: 0;
  transform: translateY(8px);
  animation: cf-fade-up 540ms cubic-bezier(0.16, 1, 0.3, 1) forwards;
  animation-delay: calc(var(--cf-i, 0) * 90ms + 60ms);
}

@keyframes cf-fade-up {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .cf-fade-up {
    animation: none;
    opacity: 1;
    transform: none;
  }
}
</style>

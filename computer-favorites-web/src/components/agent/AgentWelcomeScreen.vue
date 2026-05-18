<template>
  <div class="cf-welcome relative h-full overflow-hidden px-6 md:px-12 lg:px-16 py-10 md:py-14">
    <!-- 装饰：右上角 sparkle 光斑（仅视觉，pointer-events-none） -->
    <div class="cf-welcome-aura pointer-events-none absolute -right-24 -top-24 h-[420px] w-[420px] rounded-full" aria-hidden="true" />
    <div class="cf-welcome-grid pointer-events-none absolute inset-0 opacity-[0.04] dark:opacity-[0.06]" aria-hidden="true" />

    <div class="relative grid h-full grid-rows-[auto_1fr_auto] gap-8 md:gap-10">
      <!-- 顶部 brand line -->
      <div class="cf-fade-up flex items-center gap-2.5" style="--cf-i: 0">
        <span class="inline-flex h-6 items-center gap-1.5 rounded-full bg-primary-500/10 px-2.5 text-[11px] font-medium text-primary-600 ring-1 ring-inset ring-primary-500/20 dark:bg-primary-500/15 dark:text-primary-400">
          <span class="cf-pulse-dot h-1.5 w-1.5 rounded-full bg-primary-500" aria-hidden="true" />
          CS Copilot · v1
        </span>
        <span class="text-[11px] text-stone-400 dark:text-stone-500 tracking-wide">在线 · 私密 · 上下文感知</span>
      </div>

      <!-- 主标题 + 副标 -->
      <div class="flex flex-col justify-center max-w-[34rem]">
        <h2 class="cf-fade-up text-4xl md:text-5xl font-semibold tracking-tighter leading-[1.04] text-stone-900 dark:text-stone-50" style="--cf-i: 1">
          有什么<span class="text-primary-500">可以帮你</span>的？
        </h2>
        <p class="cf-fade-up mt-4 text-base text-stone-500 dark:text-stone-400 leading-relaxed max-w-[36ch]" style="--cf-i: 2">
          我熟悉算法、数据结构、系统设计、源码阅读；可以分析你的代码、推荐学习路径、拆解难题。
        </p>
      </div>

      <!-- 快捷 prompts: 2x2 bento -->
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 md:gap-4 max-w-[42rem]">
        <button
          v-for="(item, idx) in quickPrompts"
          :key="item.label"
          class="cf-prompt-card cf-fade-up group relative flex items-start gap-3 rounded-2xl border border-stone-200/80 bg-white/60 px-4 py-3.5 text-left backdrop-blur-sm transition-all duration-300 ease-[cubic-bezier(0.16,1,0.3,1)] hover:-translate-y-[2px] hover:border-primary-400/60 hover:bg-white hover:shadow-[0_18px_36px_-18px_rgba(var(--cf-color-primary-500-rgb),0.32),inset_0_1px_0_rgba(255,255,255,0.7)] active:scale-[0.985] dark:border-stone-700/70 dark:bg-stone-900/40 dark:hover:bg-stone-900/70 dark:hover:border-primary-500/50"
          :style="{ '--cf-i': idx + 3 }"
          @click="$emit('quickPrompt', item.label)"
        >
          <span class="grid h-8 w-8 shrink-0 place-items-center rounded-xl bg-primary-500/[0.08] text-primary-500 ring-1 ring-inset ring-primary-500/15 transition-colors duration-300 group-hover:bg-primary-500 group-hover:text-white dark:bg-primary-500/[0.14] dark:ring-primary-500/25">
            <component :is="item.icon" :size="15" :stroke-width="1.75" />
          </span>
          <span class="flex-1 min-w-0">
            <span class="block text-[11px] font-medium uppercase tracking-[0.08em] text-stone-400 dark:text-stone-500">
              {{ item.kind }}
            </span>
            <span class="mt-0.5 block text-sm font-medium text-stone-800 dark:text-stone-100 leading-snug">
              {{ item.label }}
            </span>
          </span>
          <ArrowUpRight
            class="absolute right-3.5 top-3.5 text-stone-300 transition-all duration-300 ease-[cubic-bezier(0.16,1,0.3,1)] group-hover:translate-x-0.5 group-hover:-translate-y-0.5 group-hover:text-primary-500 dark:text-stone-600"
            :size="14"
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
  { kind: '算法', label: '解释时间复杂度 O(n log n)', icon: Binary },
  { kind: '工作流', label: 'Git Flow 和 GitHub Flow 的区别', icon: GitBranch },
  { kind: '性能', label: '如何优化 Java 代码性能', icon: Gauge },
  { kind: '路线', label: '推荐计算机学习路线', icon: Compass },
]
</script>

<style scoped>
.cf-welcome-aura {
  background: radial-gradient(closest-side, rgb(var(--cf-color-primary-500-rgb) / 0.22), transparent 70%);
  filter: blur(2px);
}
:global(html.dark) .cf-welcome-aura {
  background: radial-gradient(closest-side, rgb(var(--cf-color-primary-500-rgb) / 0.28), transparent 70%);
}

.cf-welcome-grid {
  background-image:
    linear-gradient(to right, currentColor 1px, transparent 1px),
    linear-gradient(to bottom, currentColor 1px, transparent 1px);
  background-size: 28px 28px;
  color: rgb(120 113 108 / 1);
  mask-image: radial-gradient(ellipse at top right, black 0%, transparent 70%);
  -webkit-mask-image: radial-gradient(ellipse at top right, black 0%, transparent 70%);
}

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

.cf-pulse-dot {
  animation: cf-pulse-dot 1.8s cubic-bezier(0.16, 1, 0.3, 1) infinite;
  box-shadow: 0 0 0 0 rgb(var(--cf-color-primary-500-rgb) / 0.55);
}

@keyframes cf-pulse-dot {
  0%, 100% { box-shadow: 0 0 0 0 rgb(var(--cf-color-primary-500-rgb) / 0.55); }
  60% { box-shadow: 0 0 0 6px rgb(var(--cf-color-primary-500-rgb) / 0); }
}

@media (prefers-reduced-motion: reduce) {
  .cf-fade-up,
  .cf-pulse-dot {
    animation: none;
    opacity: 1;
    transform: none;
  }
}
</style>

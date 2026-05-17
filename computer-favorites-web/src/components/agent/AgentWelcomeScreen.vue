<template>
  <div class="flex flex-col items-center justify-center h-full text-center px-4 py-12">
    <div class="w-16 h-16 rounded-full bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center mb-4">
      <!-- AI sparkle SVG icon -->
      <svg class="w-8 h-8 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
          d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.455 2.456L21.75 6l-1.036.259a3.375 3.375 0 00-2.455 2.456z" />
      </svg>
    </div>
    <h2 class="text-xl font-semibold text-gray-800 dark:text-gray-100 mb-2">
      有什么可以帮你的？
    </h2>
    <p class="text-sm text-gray-500 dark:text-gray-400 max-w-md mb-6">
      我是你的 CS 学习助手，可以帮你解答编程问题、分析代码、推荐学习资源。
    </p>

    <div class="flex flex-wrap gap-2 max-w-md justify-center">
      <button
        v-for="prompt in quickPrompts"
        :key="prompt"
        class="px-3 py-1.5 text-xs rounded-full border border-gray-200 dark:border-gray-700
               text-gray-600 dark:text-gray-300
               hover:border-amber-400 hover:text-amber-600 dark:hover:border-amber-500 dark:hover:text-amber-400
               transition-colors cursor-pointer bg-transparent"
        @click="$emit('quickPrompt', prompt)"
      >
        {{ prompt }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'AgentWelcomeScreen' })

defineEmits<{
  quickPrompt: [prompt: string]
}>()

const quickPrompts = [
  '解释时间复杂度 O(n log n)',
  'Git Flow 和 GitHub Flow 的区别',
  '如何优化 Java 代码性能',
  '推荐计算机学习路线',
]
</script>

<script setup lang="ts">
const emit = defineEmits<{
  uploadClick: []
  browseClick: []
}>()
</script>

<template>
  <div class="text-center py-12 md:py-16">
    <h1
      class="text-4xl md:text-5xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-primary-400 to-amber-600 tracking-tight mb-4"
    >
      Computer Favorites
    </h1>
    <p class="text-lg text-gray-500 dark:text-gray-400 max-w-2xl mx-auto mb-8">
      精选全网优质网站与开发工具，让每一次浏览与工作更高效。
    </p>
    <div class="flex justify-center gap-4">
      <button
        class="px-6 py-2.5 rounded-full bg-primary-500/10 text-primary-600 dark:text-primary-400 font-medium hover:bg-primary-500/20 transition-colors flex items-center gap-2"
        @click="emit('uploadClick')"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"
          ></path>
        </svg>
        上传你的推荐
      </button>
      <button
        class="px-6 py-2.5 rounded-full bg-gray-900 text-white dark:bg-white dark:text-gray-900 font-medium hover:bg-gray-800 dark:hover:bg-gray-100 transition-colors flex items-center gap-2"
        @click="emit('browseClick')"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M13 10V3L4 14h7v7l9-11h-7z"
          ></path>
        </svg>
        浏览热门
      </button>
    </div>
  </div>
</template>

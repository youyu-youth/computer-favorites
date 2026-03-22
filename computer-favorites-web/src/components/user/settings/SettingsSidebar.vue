<script setup lang="ts">
interface Tab {
  id: string
  label: string
  icon: string
  component: any
}

defineProps<{
  tabs: Tab[]
  activeId: string
}>()

defineEmits<{
  (e: 'change', id: string): void
}>()
</script>

<template>
  <div class="md:sticky md:top-24">
    <h2
      class="hidden text-2xl font-semibold text-slate-900 dark:text-white md:mb-6 md:block md:px-2 lg:px-0"
    >
      设置
    </h2>

    <!-- Mobile: Horizontal scrolling tabs -->
    <div
      class="-mx-4 flex space-x-2 overflow-x-auto scroll-smooth border-b border-slate-200 px-4 pb-2 scrollbar-none dark:border-0 md:hidden"
    >
      <button
        v-for="tab in tabs"
        :key="tab.id"
        type="button"
        @click="$emit('change', tab.id)"
        class="flex cursor-pointer items-center space-x-2 whitespace-nowrap rounded-full px-4 py-2.5 text-sm font-medium transition-colors"
        :class="[
          activeId === tab.id
            ? 'bg-[#f59e0b] text-white dark:bg-[#f59e0b] dark:text-white'
            : 'text-slate-600 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-white/5 dark:hover:text-slate-200',
        ]"
      >
        <UIcon :name="tab.icon" class="h-4 w-4" />
        <span>{{ tab.label }}</span>
      </button>
    </div>

    <!-- Desktop: Vertical tabs -->
    <nav class="hidden flex-col space-y-1 md:flex">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        type="button"
        @click="$emit('change', tab.id)"
        class="flex w-full cursor-pointer items-center space-x-3 rounded-xl px-4 py-3 text-left text-sm font-medium transition-all"
        :class="[
          activeId === tab.id
            ? 'bg-white text-[#f59e0b] shadow-sm ring-1 ring-amber-200 dark:bg-amber-500/20 dark:text-[#f59e0b] dark:shadow-none dark:ring-amber-500/30'
            : 'text-slate-600 hover:bg-white/60 dark:text-slate-400 dark:hover:bg-white/5 dark:hover:text-slate-200',
        ]"
      >
        <UIcon
          :name="tab.icon"
          class="h-5 w-5"
          :class="
            activeId === tab.id
              ? 'text-[#f59e0b] dark:text-[#f59e0b]'
              : 'text-slate-400 dark:text-slate-500'
          "
        />
        <span>{{ tab.label }}</span>
      </button>
    </nav>
  </div>
</template>

<style scoped>
.scrollbar-none::-webkit-scrollbar {
  display: none;
}
.scrollbar-none {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>

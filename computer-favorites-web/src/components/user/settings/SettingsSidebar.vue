<script setup lang="ts">
interface Tab {
  id: string
  label: string
  icon: string
  component: any
  hint?: string
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
  <div class="cf-settings-sidebar md:sticky md:top-24">
    <!-- Mobile: Horizontal scrolling tabs -->
    <div class="-mx-4 flex space-x-2 overflow-x-auto scroll-smooth px-4 pb-3 scrollbar-none md:hidden">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        type="button"
        @click="$emit('change', tab.id)"
        class="flex shrink-0 cursor-pointer items-center gap-2 whitespace-nowrap rounded-full border px-4 py-2 text-sm font-medium transition-all duration-200"
        :class="[
          activeId === tab.id
            ? 'border-amber-400/70 bg-amber-50 text-amber-700 dark:border-amber-400/50 dark:bg-amber-500/15 dark:text-amber-300'
            : 'border-slate-200 bg-white/60 text-slate-600 hover:border-slate-300 hover:text-slate-900 dark:border-white/[0.10] dark:bg-[#16161d] dark:text-slate-300 dark:hover:border-white/20 dark:hover:bg-[#1c1c25] dark:hover:text-slate-100'
        ]"
      >
        <UIcon :name="tab.icon" class="h-4 w-4" />
        <span>{{ tab.label }}</span>
      </button>
    </div>

    <!-- Desktop: Vertical tabs -->
    <nav class="hidden md:block">
      <div class="mb-4 px-2 text-[11px] font-semibold uppercase tracking-[0.2em] text-slate-400 dark:text-slate-500">
        Sections
      </div>
      <div class="cf-tabs space-y-1">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          type="button"
          @click="$emit('change', tab.id)"
          class="cf-tab group relative flex w-full cursor-pointer items-center gap-3 rounded-xl px-4 py-3 text-left text-sm transition-all duration-200"
          :class="[
            activeId === tab.id
              ? 'cf-tab-active bg-amber-50/70 text-slate-900 dark:bg-amber-500/[0.08] dark:text-white'
              : 'text-slate-600 hover:bg-slate-100/70 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-white/[0.04] dark:hover:text-slate-100'
          ]"
        >
          <span
            class="flex h-9 w-9 items-center justify-center rounded-lg border transition-colors"
            :class="activeId === tab.id
              ? 'border-amber-400/40 bg-amber-100/70 text-amber-600 dark:border-amber-400/40 dark:bg-amber-500/15 dark:text-amber-300'
              : 'border-slate-200 bg-white text-slate-500 group-hover:border-slate-300 group-hover:text-slate-700 dark:border-white/[0.10] dark:bg-[#16161d] dark:text-slate-400 dark:group-hover:border-white/20 dark:group-hover:bg-[#1c1c25] dark:group-hover:text-slate-200'"
          >
            <UIcon :name="tab.icon" class="h-4 w-4" />
          </span>

          <span class="flex flex-1 flex-col items-start gap-0.5">
            <span class="font-medium leading-tight">{{ tab.label }}</span>
            <span
              v-if="tab.hint"
              class="text-[11px] leading-tight tracking-wide"
              :class="activeId === tab.id ? 'text-amber-600/80 dark:text-amber-300/70' : 'text-slate-400 dark:text-slate-500'"
            >
              {{ tab.hint }}
            </span>
          </span>

          <UIcon
            v-if="activeId === tab.id"
            name="i-lucide-chevron-right"
            class="h-4 w-4 text-amber-500 transition-transform duration-200"
            aria-hidden="true"
          />
        </button>
      </div>

      <!-- Sidebar footer / version stamp -->
      <div class="mt-8 border-t border-slate-200/80 pt-4 dark:border-white/[0.10]">
        <div class="flex items-center justify-between px-2 text-[11px] text-slate-400 dark:text-slate-500">
          <span class="font-mono tracking-wider">v1.0.0</span>
          <span class="inline-flex items-center gap-1">
            <span class="inline-block h-1 w-1 rounded-full bg-emerald-500"></span>
            <span>已同步</span>
          </span>
        </div>
      </div>
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

.cf-tab-active {
  box-shadow: 0 1px 0 rgb(255 255 255 / 0.04) inset;
}

:where(html.dark) .cf-tab-active {
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.06) inset,
    0 0 0 1px rgb(245 158 11 / 0.10);
}
</style>

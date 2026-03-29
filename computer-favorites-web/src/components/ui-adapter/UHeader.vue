<script setup lang="ts">
import { computed, useSlots } from 'vue'

const props = withDefaults(defineProps<{
  open?: boolean
  ui?: {
    container?: string
  }
}>(), {
  open: false,
  ui: () => ({}),
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
}>()

const slots = useSlots()

const menuOpen = computed({
  get: () => Boolean(props.open),
  set: (value: boolean) => emit('update:open', value),
})

const hasBody = computed(() => Boolean(slots.body))
</script>

<template>
  <header>
    <div :class="ui.container || 'mx-auto w-full px-4 sm:px-6 lg:px-8'">
      <div class="flex h-full min-h-16 items-center justify-between gap-3">
        <div class="flex items-center gap-3">
          <slot name="left" />
        </div>

        <div class="hidden flex-1 items-center justify-center md:flex">
          <slot />
        </div>

        <div class="flex items-center gap-2">
          <slot name="right" />
          <button
            v-if="hasBody"
            type="button"
            class="inline-flex h-10 w-10 cursor-pointer items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-700 transition-colors hover:bg-slate-100 dark:border-white/10 dark:bg-slate-900 dark:text-slate-200 dark:hover:bg-slate-800 md:hidden"
            @click="menuOpen = !menuOpen"
          >
            <span class="sr-only">Toggle navigation</span>
            <svg
              v-if="!menuOpen"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              class="h-5 w-5"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
            <svg
              v-else
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              class="h-5 w-5"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <Transition name="uheader-collapse">
      <div
        v-if="menuOpen && hasBody"
        class="border-t border-slate-200 bg-white px-2 py-2 dark:border-white/10 dark:bg-slate-900 md:hidden"
      >
        <slot name="body" />
      </div>
    </Transition>
  </header>
</template>

<style scoped>
.uheader-collapse-enter-active,
.uheader-collapse-leave-active {
  transition: all var(--cf-motion-fast) var(--cf-ease-standard);
}

.uheader-collapse-enter-from,
.uheader-collapse-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>

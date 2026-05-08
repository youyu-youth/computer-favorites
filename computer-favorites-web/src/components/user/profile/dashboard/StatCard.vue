<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-07
 * 通用紧凑卡：标题 + 默认插槽 + 右侧操作 / 装饰区
 */
defineProps<{
  title?: string
  subtitle?: string
  accent?: string
  dense?: boolean
}>()
</script>

<template>
  <section
    class="cf-gh-card group relative flex flex-col rounded-md border border-black/5 bg-white transition-colors dark:border-white/[0.06] dark:bg-black"
    :class="dense ? 'p-3' : 'p-4'"
  >
    <header
      v-if="title || $slots.header || $slots.actions"
      class="flex items-center justify-between gap-2"
      :class="dense ? 'mb-2' : 'mb-3'"
    >
      <div class="flex min-w-0 items-center gap-2">
        <slot name="header">
          <span
            v-if="accent"
            class="inline-block h-3 w-[3px] rounded-sm"
            :style="{ backgroundColor: accent }"
          />
          <p v-if="subtitle" class="truncate text-[11px] text-gray-500 dark:text-gray-400">
            {{ subtitle }}
          </p>
        </slot>
      </div>
      <div v-if="$slots.actions" class="shrink-0">
        <slot name="actions" />
      </div>
    </header>

    <div class="min-w-0 flex-1">
      <slot />
    </div>

    <footer v-if="$slots.footer" class="mt-3 border-t border-black/5 pt-2 dark:border-white/[0.06]">
      <slot name="footer" />
    </footer>
  </section>
</template>

<script setup lang="ts">
import Toast from 'primevue/toast'

const iconClassMap: Record<string, string> = {
  success: 'pi pi-check-circle text-emerald-500',
  error: 'pi pi-times-circle text-rose-500',
  warn: 'pi pi-exclamation-triangle text-amber-500',
  info: 'pi pi-info-circle text-blue-500',
}
</script>

<template>
  <Toast
    position="top-right"
    group="app-headless"
    :pt="{ root: { class: 'w-full sm:max-w-[360px]' } }"
  >
    <template #container="{ message, closeCallback }">
      <div
        class="pointer-events-auto flex w-full items-start gap-3 rounded-xl border border-gray-100 bg-white/95 p-4 shadow-[var(--cf-shadow-toast-light)] backdrop-blur-md dark:border-gray-800/80 dark:bg-gray-900/95 dark:shadow-[var(--cf-shadow-toast-dark)]"
        role="alert"
      >
        <i
          :class="[iconClassMap[message.severity || 'info'] || iconClassMap.info, 'mt-0.5 text-lg']"
        />
        <div class="min-w-0 flex-1 pt-[2px]">
          <p class="truncate text-[14px] font-medium leading-snug text-gray-900 dark:text-gray-100">
            {{ message.summary }}
          </p>
          <p
            v-if="message.detail"
            class="mt-1 break-words text-[13px] leading-relaxed text-gray-500 dark:text-gray-400"
          >
            {{ message.detail }}
          </p>
        </div>
        <button
          class="-mr-1 ml-3 flex-shrink-0 rounded-md p-1 text-gray-400 transition-colors hover:bg-gray-100/50 hover:text-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-200 dark:hover:bg-gray-800/50 dark:hover:text-gray-300 dark:focus:ring-gray-700"
          @click="closeCallback"
        >
          <i class="pi pi-times text-sm" />
        </button>
      </div>
    </template>
  </Toast>
</template>

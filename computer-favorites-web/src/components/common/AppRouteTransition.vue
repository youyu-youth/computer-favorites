<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { DotLottieVue } from '@lottiefiles/dotlottie-vue'
import { useAppStore } from '@/stores/app'
import loadingRocketAnimation from '@/assets/animation/loading/Flying rocket in the sky.lottie?url'

const appStore = useAppStore()
const { isDark, isRouteTransitioning } = storeToRefs(appStore)
const overlayClass = computed(() => {
  if (isDark.value) {
    return 'bg-black/80'
  }
  return 'bg-white/85'
})
</script>

<template>
  <Transition name="route-overlay-fade">
    <div
      v-if="isRouteTransitioning"
      :class="overlayClass"
      class="fixed inset-0 z-[80] flex items-center justify-center backdrop-blur-sm"
    >
      <div class="flex flex-col items-center gap-2">
        <DotLottieVue
          :src="loadingRocketAnimation"
          autoplay
          loop
          class="h-40 w-40"
        />
        <p class="text-xs font-medium text-slate-600 dark:text-slate-300">页面切换中...</p>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.route-overlay-fade-enter-active,
.route-overlay-fade-leave-active {
  transition: opacity 0.24s ease;
}

.route-overlay-fade-enter-from,
.route-overlay-fade-leave-to {
  opacity: 0;
}
</style>

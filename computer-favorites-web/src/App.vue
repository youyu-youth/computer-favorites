<script setup lang="ts">
import AppToast from '@/components/common/AppToast.vue'
import AppMessage from '@/components/common/AppMessage.vue'
import AppRouteTransition from '@/components/common/AppRouteTransition.vue'
</script>

<template>
  <AppToast />
  <AppMessage />
  <AppRouteTransition />
  <RouterView v-slot="{ Component, route }">
    <Transition name="page-fade" mode="out-in">
      <component :is="Component" :key="route.fullPath" />
    </Transition>
  </RouterView>
</template>

<style>
.page-fade-enter-active,
.page-fade-leave-active {
  transition:
    opacity var(--cf-motion-page) var(--cf-ease-standard),
    transform var(--cf-motion-page) var(--cf-ease-standard);
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>

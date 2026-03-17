<script setup lang="ts">
import { onMounted } from 'vue'
import { useAppStore } from '@/stores/app'
import AppToast from '@/components/common/AppToast.vue'

const appStore = useAppStore()

onMounted(() => {
  appStore.initTheme()
})
</script>

<template>
  <AppToast />
  <RouterView v-slot="{ Component, route }">
    <Transition name="page-fade" mode="out-in">
      <component :is="Component" :key="route.fullPath" />
    </Transition>
  </RouterView>
</template>

<style>
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1), transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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

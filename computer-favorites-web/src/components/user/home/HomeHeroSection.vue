<script setup lang="ts">
// 首页 Hero 展示文案配置
withDefaults(defineProps<{
  bannerText?: string
  titlePrefix?: string
  titleHighlight?: string
  commandText?: string
  description?: string
}>(), {
  bannerText: '> initial_payload_received.exe',
  titlePrefix: 'Curated intelligence for',
  titleHighlight: 'hyper-scale',
  commandText: 'locate --all resources where stack="future"',
  description: 'A high-density archive of technical gold, curated for the modern developer who demands editorial precision over noise.',
})
</script>

<template>
  <section class="flex flex-col items-start gap-6">
    <div class="mb-2 font-mono text-sm uppercase tracking-widest text-primary-500 opacity-80">
      {{ bannerText }}
    </div>
    <h1 class="max-w-4xl font-headline text-3xl font-extrabold leading-tight tracking-tighter text-text-primary sm:text-5xl md:text-6xl lg:text-7xl">
      {{ titlePrefix }} <span class="text-primary-500">{{ titleHighlight }}</span> engineers.
    </h1>
    <div class="cyber-border mt-2 flex w-full max-w-2xl items-center gap-2 bg-surface-card p-3 font-mono text-base text-text-primary sm:p-4 sm:text-xl md:text-2xl">
      <span class="text-primary-500">$</span>
      <span>{{ commandText }}</span>
      <span class="terminal-cursor"></span>
    </div>
    <p class="mt-1 max-w-xl text-base text-text-secondary sm:text-lg">
      {{ description }}
    </p>
  </section>
</template>

<style scoped>
@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0;
  }
}

.terminal-cursor {
  animation: blink 1s step-end infinite;
}
</style>

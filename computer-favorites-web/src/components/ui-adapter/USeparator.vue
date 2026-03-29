<script setup lang="ts">
import { useAttrs } from 'vue'
import Divider from 'primevue/divider'

const props = withDefaults(defineProps<{
  orientation?: 'horizontal' | 'vertical'
}>(), {
  orientation: 'horizontal',
})

const attrs = useAttrs()
</script>

<template>
  <Divider :layout="orientation === 'vertical' ? 'vertical' : 'horizontal'" :class="attrs.class" />
</template>

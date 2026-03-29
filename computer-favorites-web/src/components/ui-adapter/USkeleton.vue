<script setup lang="ts">
import { useAttrs } from 'vue'
import Skeleton from 'primevue/skeleton'

const props = withDefaults(defineProps<{
  shape?: string
  width?: string
  height?: string
  size?: string
  borderRadius?: string
}>(), {
  shape: undefined,
  width: undefined,
  height: undefined,
  size: undefined,
  borderRadius: undefined,
})

const attrs = useAttrs()
</script>

<template>
  <Skeleton
    :shape="shape"
    :width="width"
    :height="height"
    :size="size"
    :borderRadius="borderRadius"
    :class="attrs.class"
  />
</template>

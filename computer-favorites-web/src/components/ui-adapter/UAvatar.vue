<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import Avatar from 'primevue/avatar'

const props = withDefaults(
  defineProps<{
    src?: string
    alt?: string
    size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  }>(),
  {
    src: '',
    alt: '',
    size: 'md',
  },
)

const attrs = useAttrs()

const mappedSize = computed(() => {
  if (props.size === 'xs' || props.size === 'sm') {
    return 'small'
  }
  if (props.size === 'lg' || props.size === 'xl') {
    return 'large'
  }
  return undefined
})
</script>

<template>
  <Avatar :image="src" :label="alt" shape="circle" :size="mappedSize" :class="attrs.class">
    <slot />
  </Avatar>
</template>

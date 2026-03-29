<script setup lang="ts">
import { useAttrs } from 'vue'
import ToggleSwitch from 'primevue/toggleswitch'

const props = withDefaults(defineProps<{
  modelValue?: boolean
  disabled?: boolean
}>(), {
  modelValue: false,
  disabled: false,
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const attrs = useAttrs()
</script>

<template>
  <ToggleSwitch
    :modelValue="modelValue"
    :disabled="disabled"
    :class="attrs.class"
    @update:modelValue="(value) => emit('update:modelValue', Boolean(value))"
  />
</template>

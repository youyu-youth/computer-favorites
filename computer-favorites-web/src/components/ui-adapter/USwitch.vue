<script setup lang="ts">
import { useAttrs } from 'vue'
import ToggleSwitch from 'primevue/toggleswitch'

const props = withDefaults(
  defineProps<{
    modelValue?: boolean
    disabled?: boolean
  }>(),
  {
    modelValue: false,
    disabled: false,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const attrs = useAttrs()
</script>

<template>
  <ToggleSwitch
    v-bind="attrs"
    :modelValue="modelValue"
    :disabled="disabled"
    :class="['cf-switch', attrs.class]"
    @update:modelValue="(value) => emit('update:modelValue', Boolean(value))"
  />
</template>

<style scoped>
:deep(.cf-switch) {
  display: inline-flex;
  align-items: center;
}

:deep(.cf-switch [data-pc-section='input']) {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

:deep(.cf-switch [data-pc-section='slider']) {
  position: relative;
  width: 44px;
  height: 24px;
  border-radius: 9999px;
  background: rgb(203 213 225);
  border: 1px solid rgb(148 163 184 / 0.4);
  transition: all 0.2s ease;
}

:deep(.cf-switch [data-pc-section='handle']) {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 18px;
  height: 18px;
  border-radius: 9999px;
  background: rgb(255 255 255);
  box-shadow: 0 2px 5px rgb(15 23 42 / 0.25);
  transition: transform 0.2s ease;
}

:deep(.cf-switch[data-p-checked='true'] [data-pc-section='slider']) {
  background: rgb(245 158 11);
  border-color: rgb(217 119 6 / 0.55);
  box-shadow: 0 0 0 4px rgb(245 158 11 / 0.12);
}

:deep(.cf-switch[data-p-checked='true'] [data-pc-section='handle']) {
  transform: translateX(20px);
}

:deep(.dark .cf-switch [data-pc-section='slider']) {
  background: rgb(71 85 105 / 0.75);
  border-color: rgb(148 163 184 / 0.28);
}

:deep(.dark .cf-switch[data-p-checked='true'] [data-pc-section='slider']) {
  background: rgb(245 158 11);
  border-color: rgb(245 158 11 / 0.6);
  box-shadow: 0 0 0 4px rgb(245 158 11 / 0.18);
}

:deep(.cf-switch[data-p-disabled='true']) {
  opacity: 0.55;
  cursor: not-allowed;
}

:deep(.cf-switch[data-p-disabled='true'] [data-pc-section='input']) {
  cursor: not-allowed;
}
</style>

<script setup lang="ts">
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

type Props = {
  modelValue: string
  placeholder?: string
  minHeight?: number
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请输入 Markdown 内容',
  minHeight: 220,
  disabled: false,
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const hostRef = ref<HTMLDivElement | null>(null)
const editorRef = ref<Vditor | null>(null)
const isDarkTheme = ref(false)
let classObserver: MutationObserver | null = null
let fromEditor = false
let isUnmounted = false

type VditorRuntime = Vditor & {
  vditor?: {
    element?: HTMLElement
  }
}

const resolveTheme = (): 'dark' | 'classic' => {
  return document.documentElement.classList.contains('dark') ? 'dark' : 'classic'
}

const resolveCodeTheme = (): string => {
  return resolveTheme() === 'dark' ? 'github-dark' : 'github'
}

const syncDarkThemeState = (): void => {
  isDarkTheme.value = resolveTheme() === 'dark'
}

const applyEditorTheme = (): void => {
  syncDarkThemeState()
  const editor = editorRef.value as VditorRuntime | null
  if (!editor || !editor.vditor?.element || isUnmounted) {
    return
  }
  editor.setTheme(resolveTheme(), undefined, resolveCodeTheme())
}

const getActiveEditor = (): VditorRuntime | null => {
  if (isUnmounted) {
    return null
  }
  const editor = editorRef.value as VditorRuntime | null
  if (!editor || !editor.vditor?.element) {
    return null
  }
  return editor
}

const destroyEditorSafely = (): void => {
  const editor = editorRef.value
  editorRef.value = null
  if (!editor) {
    return
  }

  void Promise.resolve()
    .then(() => editor.destroy())
    .catch(() => {
      // Vditor 在初始化未完成时销毁可能抛错，这里忽略销毁期异常，避免未捕获 Promise 报错。
    })
}

const setupEditor = (): void => {
  if (!hostRef.value) {
    return
  }

  editorRef.value = new Vditor(hostRef.value, {
    mode: 'ir',
    minHeight: props.minHeight,
    placeholder: props.placeholder,
    value: props.modelValue,
    theme: resolveTheme(),
    preview: {
      hljs: {
        style: resolveCodeTheme(),
      },
      markdown: {
        mark: true,
      },
    },
    cache: {
      enable: false,
    },
    toolbarConfig: {
      pin: false,
    },
    input: (value: string) => {
      fromEditor = true
      emit('update:modelValue', value)
      nextTick(() => {
        fromEditor = false
      })
    },
    after: () => {
      if (isUnmounted) {
        return
      }
      applyEditorTheme()
      const editor = getActiveEditor()
      if (props.disabled && editor) {
        editor.disabled()
      }
    },
  })
}

onMounted(() => {
  isUnmounted = false
  syncDarkThemeState()
  setupEditor()

  classObserver = new MutationObserver(() => {
    applyEditorTheme()
  })

  classObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['class'],
  })
})

watch(
  () => props.modelValue,
  (nextValue) => {
    const editor = getActiveEditor()
    if (!editor || fromEditor) {
      return
    }
    if (editor.getValue() !== nextValue) {
      editor.setValue(nextValue)
    }
  },
)

watch(
  () => props.disabled,
  (disabled) => {
    const editor = getActiveEditor()
    if (!editor) {
      return
    }
    if (disabled) {
      editor.disabled()
      return
    }
    editor.enable()
  },
)

onBeforeUnmount(() => {
  isUnmounted = true
  classObserver?.disconnect()
  classObserver = null
  destroyEditorSafely()
})
</script>

<template>
  <div class="cf-vditor-wrapper" :class="{ 'is-dark': isDarkTheme }">
    <div ref="hostRef" class="cf-vditor-host" />
  </div>
</template>

<style scoped>
.cf-vditor-wrapper {
  width: 100%;
  min-width: 0;
}

:deep(.vditor) {
  --textarea-text-color: rgb(17 24 39 / 1);
  --ir-heading-color: rgb(17 24 39 / 1);
  --ir-title-color: rgb(17 24 39 / 1);
  --ir-bi-color: rgb(17 24 39 / 1);
  --ir-link-color: rgb(17 24 39 / 1);
  --ir-bracket-color: rgb(17 24 39 / 1);
  --ir-paren-color: rgb(17 24 39 / 1);
  border-radius: 0.5rem;
  border: 1px solid rgb(209 213 219 / 1);
  background-color: rgb(255 255 255 / 1);
}

:deep(.vditor-toolbar) {
  border-bottom: 1px solid rgb(229 231 235 / 1);
  background-color: rgb(249 250 251 / 1);
}

:deep(.vditor-toolbar__item) {
  color: rgb(75 85 99 / 1);
}

:deep(.vditor-toolbar__item:hover) {
  background-color: rgb(243 244 246 / 1);
}

:deep(.vditor-reset) {
  color: rgb(17 24 39 / 1);
}

:deep(.vditor-reset ol),
:deep(.vditor-ir__preview ol) {
  list-style-type: decimal;
  list-style-position: outside;
  padding-left: 2em;
}

:deep(.vditor-reset ol ol),
:deep(.vditor-ir__preview ol ol) {
  list-style-type: lower-alpha;
}

:deep(.vditor-reset ol ol ol),
:deep(.vditor-ir__preview ol ol ol) {
  list-style-type: lower-roman;
}

:deep(.vditor-reset li::marker),
:deep(.vditor-ir__preview li::marker) {
  color: currentColor;
}

:deep(.vditor-reset mark),
:deep(.vditor-ir__preview mark) {
  color: inherit;
  background-color: rgb(250 204 21 / 0.4);
  border-radius: 0.25rem;
  padding: 0 0.2em;
}

:deep(.vditor-content) {
  background-color: rgb(255 255 255 / 1);
}

:deep(.vditor-ir pre.vditor-reset),
:deep(.vditor-ir__marker),
:deep(.vditor-ir__preview) {
  color: rgb(17 24 39 / 1);
}

.cf-vditor-wrapper.is-dark :deep(.vditor) {
  --textarea-text-color: rgb(255 255 255 / 1);
  --ir-heading-color: rgb(255 255 255 / 1);
  --ir-title-color: rgb(255 255 255 / 1);
  --ir-bi-color: rgb(255 255 255 / 1);
  --ir-link-color: rgb(255 255 255 / 1);
  --ir-bracket-color: rgb(255 255 255 / 1);
  --ir-paren-color: rgb(255 255 255 / 1);
  border-color: rgb(var(--cf-color-dark-border-rgb) / 1);
  background-color: rgb(var(--cf-color-dark-card-rgb) / 1);
}

.cf-vditor-wrapper.is-dark :deep(.vditor-toolbar) {
  border-bottom-color: rgb(var(--cf-color-dark-border-rgb) / 1) !important;
  background-color: rgb(var(--cf-color-dark-bg-rgb) / 1) !important;
}

.cf-vditor-wrapper.is-dark :deep(.vditor-toolbar__item) {
  color: rgb(148 163 184 / 1) !important;
}

.cf-vditor-wrapper.is-dark :deep(.vditor-toolbar__item:hover) {
  background-color: rgb(30 41 59 / 1) !important;
}

.cf-vditor-wrapper.is-dark :deep(.vditor-toolbar__item svg),
.cf-vditor-wrapper.is-dark :deep(.vditor-toolbar__item i) {
  color: rgb(148 163 184 / 1) !important;
  fill: rgb(148 163 184 / 1) !important;
}

.cf-vditor-wrapper.is-dark :deep(.vditor-toolbar__item:hover svg),
.cf-vditor-wrapper.is-dark :deep(.vditor-toolbar__item:hover i) {
  color: rgb(226 232 240 / 1) !important;
  fill: rgb(226 232 240 / 1) !important;
}

.cf-vditor-wrapper.is-dark :deep(.vditor-toolbar__divider) {
  border-left-color: rgb(var(--cf-color-dark-border-rgb) / 1) !important;
}

.cf-vditor-wrapper.is-dark :deep(.vditor-reset),
.cf-vditor-wrapper.is-dark :deep(.vditor-content),
.cf-vditor-wrapper.is-dark :deep(.vditor-ir pre.vditor-reset),
.cf-vditor-wrapper.is-dark :deep(.vditor-ir__marker),
.cf-vditor-wrapper.is-dark :deep(.vditor-ir__preview) {
  color: rgb(241 245 249 / 1);
  background-color: rgb(var(--cf-color-dark-card-rgb) / 1);
}

.cf-vditor-wrapper.is-dark :deep(.vditor-reset pre > code),
.cf-vditor-wrapper.is-dark :deep(.vditor-reset pre > code.hljs),
.cf-vditor-wrapper.is-dark :deep(.hljs) {
  background-color: rgb(15 23 42 / 0.92) !important;
  color: rgb(226 232 240 / 1) !important;
  -webkit-text-fill-color: currentColor !important;
}

.cf-vditor-wrapper.is-dark :deep(.vditor-reset code:not(.hljs):not(.highlight-chroma)) {
  background-color: rgb(30 41 59 / 1);
  color: rgb(241 245 249 / 1);
}

.cf-vditor-wrapper.is-dark :deep(.vditor-reset mark),
.cf-vditor-wrapper.is-dark :deep(.vditor-ir__preview mark) {
  background-color: rgb(234 179 8 / 0.35);
}

:deep(.vditor-reset:focus-visible) {
  outline: 2px solid rgb(233 83 34 / 1);
  outline-offset: 0;
}

@media (max-width: 640px) {
  :deep(.vditor-toolbar) {
    overflow-x: auto;
    white-space: nowrap;
  }

  :deep(.vditor-toolbar__item) {
    flex: 0 0 auto;
  }

  :deep(.vditor-ir),
  :deep(.vditor-content) {
    min-height: 180px;
  }
}
</style>

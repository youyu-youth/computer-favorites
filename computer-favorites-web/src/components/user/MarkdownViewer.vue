<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue'
import { Marked } from 'marked'
import { markedHighlight } from 'marked-highlight'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css' // Import style for dark mode compatibility

const props = defineProps<{
  content: string
}>()

// Initialize a new Marked instance with the highlighting configuration
const markedObj = new Marked(
  markedHighlight({
    emptyLangClass: 'hljs',
    langPrefix: 'hljs language-',
    highlight(code, lang) {
      const language = hljs.getLanguage(lang) ? lang : 'plaintext'
      return hljs.highlight(code, { language }).value
    }
  })
)

const renderedMarkdown = computed(() => {
  return markedObj.parse(props.content || '') as string
})

const markdownContainer = ref<HTMLElement | null>(null)

watch(renderedMarkdown, async () => {
  await nextTick()
  if (!markdownContainer.value) return

  const pres = markdownContainer.value.querySelectorAll('pre')
  pres.forEach(pre => {
    if (pre.parentElement?.classList.contains('code-wrapper')) return

    // Wrap the pre in a relative div to safely position the button
    const wrapper = document.createElement('div')
    wrapper.className = 'code-wrapper relative group my-4 rounded-md overflow-hidden'
    pre.parentNode?.insertBefore(wrapper, pre)
    wrapper.appendChild(pre)

    // Remove margin from pre to fit tightly inside the wrapper
    pre.style.marginTop = '0'
    pre.style.marginBottom = '0'

    // Create the copy button
    const btn = document.createElement('button')
    btn.className = 'copy-btn absolute top-2 right-2 p-1.5 rounded text-gray-400 bg-gray-800/80 hover:text-white hover:bg-gray-700 opacity-0 group-hover:opacity-100 transition-all duration-200 cursor-pointer border border-gray-600/30'
    btn.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide-copy"><rect width="14" height="14" x="8" y="8" rx="2" ry="2"/><path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"/></svg>`

    wrapper.appendChild(btn)
  })
}, { immediate: true })

const handleCopy = async (e: MouseEvent) => {
  const target = e.target as HTMLElement
  const btn = target.closest('.copy-btn')
  if (btn) {
    const wrapper = btn.closest('.code-wrapper')
    const codeEl = wrapper?.querySelector('code')
    if (codeEl) {
      await navigator.clipboard.writeText(codeEl.innerText)
      const originalHTML = btn.innerHTML
      // Show checkmark icon
      btn.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide-check text-green-500"><path d="M20 6 9 17l-5-5"/></svg>`
      setTimeout(() => {
        btn.innerHTML = originalHTML
      }, 2000)
    }
  }
}
</script>

<template>
  <div
    ref="markdownContainer"
    @click="handleCopy"
    class="flat-design prose prose-sm sm:prose-base dark:prose-invert max-w-none
           rounded border border-[#094438]/20 bg-[#094438]/10 p-6 sm:p-8
           dark:border-[#094438]/40 dark:bg-[#094438]/30
           prose-a:text-primary-500 hover:prose-a:text-primary-600
           prose-headings:text-gray-900 dark:prose-headings:text-white
           prose-p:text-gray-700 dark:prose-p:text-gray-300
           prose-li:text-gray-700 dark:prose-li:text-gray-300"
    v-html="renderedMarkdown"
  ></div>
</template>

<style scoped>
.flat-design {
  box-shadow: none !important;
}

/* Ensure code blocks have proper background and spacing */
:deep(pre) {
  padding: 0.5rem 1rem !important;
  border-radius: 0.375rem;
  background-color: #f3f4f6 !important; /* light mode fallback */
}

@media (prefers-color-scheme: dark) {
  :deep(pre) {
    background-color: #121d2f !important;
  }
}

:deep(pre code.hljs) {
  background-color: transparent !important;
  color: inherit;
  font-size: 1rem !important; /* Increase code font size */
  line-height: 1.5;
}

:deep(:not(pre) > code.hljs) {
  padding: 0.2rem 0.4rem;
  border-radius: 0.25rem;
  background-color: #e5e7eb !important;
  color: #374151 !important;
}

@media (prefers-color-scheme: dark) {
  :deep(:not(pre) > code.hljs) {
    background-color: #333 !important;
    color: #d4d4d4 !important;
  }
}
</style>

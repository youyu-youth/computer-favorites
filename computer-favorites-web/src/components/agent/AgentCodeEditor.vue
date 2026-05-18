<template>
  <div class="cf-code-overlay fixed inset-0 z-[120] flex items-center justify-center px-4" @click.self="$emit('close')">
    <!-- 遮罩 -->
    <div class="cf-code-mask absolute inset-0" aria-hidden="true" />

    <!-- Dialog -->
    <div class="cf-code-dialog relative w-full max-w-xl rounded-2xl bg-white/95 dark:bg-stone-900/90 backdrop-blur-2xl ring-1 ring-stone-900/[0.06] dark:ring-white/[0.06] shadow-[inset_0_1px_0_rgba(255,255,255,0.6),0_28px_60px_-22px_rgba(15,23,42,0.45)] dark:shadow-[inset_0_1px_0_rgba(255,255,255,0.05),0_30px_64px_-22px_rgba(0,0,0,0.7)] flex flex-col max-h-[82dvh] overflow-hidden">
      <!-- Header -->
      <div class="flex items-center gap-2.5 px-5 pt-4 pb-3">
        <span class="grid h-7 w-7 place-items-center rounded-lg bg-primary-500/[0.08] text-primary-500 ring-1 ring-inset ring-primary-500/15 dark:bg-primary-500/[0.14] dark:ring-primary-500/25">
          <Code2 :size="14" :stroke-width="1.75" />
        </span>
        <div class="flex flex-1 items-baseline gap-2 min-w-0">
          <h3 class="text-[14px] font-semibold tracking-tight text-stone-900 dark:text-stone-100">插入代码</h3>
          <span class="text-[10px] font-medium tracking-[0.08em] uppercase text-stone-400 dark:text-stone-500">Insert</span>
        </div>
        <button
          type="button"
          class="grid h-8 w-8 place-items-center rounded-lg text-stone-400 hover:text-stone-700 dark:hover:text-stone-200 hover:bg-stone-900/[0.04] dark:hover:bg-white/[0.06] transition-colors cursor-pointer"
          aria-label="关闭"
          @click="$emit('close')"
        >
          <X :size="15" :stroke-width="1.75" />
        </button>
      </div>

      <!-- 语言 chips -->
      <div class="px-5 pb-3">
        <div class="cf-code-langs flex flex-wrap gap-1.5">
          <button
            v-for="lang in languages"
            :key="lang.value"
            type="button"
            class="cf-code-lang"
            :class="language === lang.value ? 'cf-code-lang--active' : ''"
            @click="language = lang.value"
          >
            {{ lang.name }}
          </button>
        </div>
      </div>

      <!-- 代码区域：暗色 surface + 行号 padding + mono -->
      <div class="px-5 pb-4 flex-1 min-h-0 flex flex-col">
        <div class="cf-code-surface relative flex-1 overflow-hidden rounded-xl bg-stone-950/95 ring-1 ring-stone-900/30 dark:ring-white/[0.06] shadow-[inset_0_1px_0_rgba(255,255,255,0.04)]">
          <!-- 装饰：左上角 macOS 三圆点 -->
          <div class="pointer-events-none absolute left-3 top-3 flex items-center gap-1 opacity-50" aria-hidden="true">
            <span class="h-2 w-2 rounded-full bg-rose-500/70" />
            <span class="h-2 w-2 rounded-full bg-amber-500/70" />
            <span class="h-2 w-2 rounded-full bg-emerald-500/70" />
          </div>
          <span class="pointer-events-none absolute right-3 top-2.5 text-[10px] font-mono uppercase tracking-[0.12em] text-stone-500" aria-hidden="true">
            {{ language || 'plain' }}
          </span>
          <textarea
            ref="codeInputRef"
            v-model="code"
            class="block w-full h-full min-h-[220px] font-mono text-[12.5px] leading-relaxed text-stone-100 bg-transparent resize-none outline-none px-4 pt-9 pb-3 placeholder-stone-500"
            placeholder="// 在此粘贴代码..."
            spellcheck="false"
          />
        </div>
      </div>

      <!-- Footer -->
      <div class="flex items-center justify-end gap-2 px-5 py-3 border-t border-stone-200/70 dark:border-stone-800/70">
        <button type="button" class="cf-code-btn cf-code-btn--ghost" @click="$emit('close')">取消</button>
        <button
          type="button"
          class="cf-code-btn cf-code-btn--primary disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:translate-y-0"
          :disabled="!code.trim()"
          @click="insert"
        >
          <Check :size="13" :stroke-width="2.25" />
          插入
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Code2, X, Check } from 'lucide-vue-next'

defineOptions({ name: 'AgentCodeEditor' })

const emit = defineEmits<{
  close: []
  insert: [code: string, language: string]
}>()

const language = ref('java')
const code = ref('')
const codeInputRef = ref<HTMLTextAreaElement | null>(null)

const languages = [
  { name: 'Java', value: 'java' },
  { name: 'Python', value: 'python' },
  { name: 'JavaScript', value: 'javascript' },
  { name: 'TypeScript', value: 'typescript' },
  { name: 'C', value: 'c' },
  { name: 'C++', value: 'cpp' },
  { name: 'Go', value: 'go' },
  { name: 'Rust', value: 'rust' },
  { name: 'SQL', value: 'sql' },
  { name: 'Bash', value: 'bash' },
  { name: '无高亮', value: '' },
]

function insert() {
  if (!code.value.trim()) return
  emit('insert', code.value, language.value)
}

onMounted(() => {
  codeInputRef.value?.focus()
})
</script>

<style scoped>
.cf-code-overlay {
  animation: cf-overlay-in 220ms cubic-bezier(0.16, 1, 0.3, 1) both;
}
@keyframes cf-overlay-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

.cf-code-mask {
  background-color: rgb(15 23 42 / 0.42);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.cf-code-dialog {
  animation: cf-dialog-in 320ms cubic-bezier(0.16, 1, 0.3, 1) both;
  transform-origin: center;
}
@keyframes cf-dialog-in {
  from { opacity: 0; transform: translateY(6px) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* 语言 chip 按钮 */
.cf-code-lang {
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
  color: rgb(120 113 108 / 1);
  background-color: transparent;
  box-shadow: inset 0 0 0 1px rgb(0 0 0 / 0.08);
  cursor: pointer;
  transition: all 160ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-code-lang:hover {
  color: rgb(63 63 70 / 1);
  background-color: rgb(0 0 0 / 0.04);
}
:global(html.dark) .cf-code-lang {
  color: rgb(168 162 158 / 1);
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 0.08);
}
:global(html.dark) .cf-code-lang:hover {
  color: rgb(231 229 228 / 1);
  background-color: rgb(255 255 255 / 0.05);
}
.cf-code-lang--active {
  color: white !important;
  background-color: rgb(var(--cf-color-primary-500-rgb) / 1);
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.22),
    0 4px 10px -4px rgb(var(--cf-color-primary-500-rgb) / 0.45);
}
.cf-code-lang--active:hover {
  background-color: rgb(var(--cf-color-primary-600-rgb) / 1);
}

/* Footer 按钮 */
.cf-code-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  height: 2rem;
  padding-inline: 0.875rem;
  border-radius: 0.625rem;
  font-size: 12.5px;
  font-weight: 500;
  cursor: pointer;
  transition: all 160ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-code-btn--ghost {
  color: rgb(82 82 91 / 1);
  background-color: transparent;
  box-shadow: inset 0 0 0 1px rgb(0 0 0 / 0.08);
}
.cf-code-btn--ghost:hover {
  background-color: rgb(0 0 0 / 0.04);
  color: rgb(24 24 27 / 1);
}
:global(html.dark) .cf-code-btn--ghost {
  color: rgb(168 162 158 / 1);
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 0.1);
}
:global(html.dark) .cf-code-btn--ghost:hover {
  background-color: rgb(255 255 255 / 0.06);
  color: rgb(231 229 228 / 1);
}
.cf-code-btn--primary {
  color: white;
  background-color: rgb(var(--cf-color-primary-500-rgb) / 1);
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.22),
    0 8px 18px -6px rgb(var(--cf-color-primary-500-rgb) / 0.45);
}
.cf-code-btn--primary:hover {
  background-color: rgb(var(--cf-color-primary-600-rgb) / 1);
  transform: translateY(-1px);
}
.cf-code-btn:active {
  transform: scale(0.97) translateY(1px);
}

@media (prefers-reduced-motion: reduce) {
  .cf-code-overlay,
  .cf-code-dialog {
    animation: none;
    opacity: 1;
    transform: none;
  }
}
</style>

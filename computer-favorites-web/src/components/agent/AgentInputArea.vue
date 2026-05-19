<template>
  <div class="cf-composer-wrap px-3 md:px-6 pt-2 pb-3 md:pb-4">
    <!-- 已附加文件 chips（行内列出在 surface 上方） -->
    <div v-if="files.length > 0" class="mb-2 flex flex-wrap gap-1.5">
      <div
        v-for="(file, idx) in files"
        :key="idx"
        class="cf-file-chip group/chip inline-flex items-center gap-1.5 rounded-full bg-stone-100 dark:bg-stone-800 ring-1 ring-inset ring-stone-200/80 dark:ring-stone-700/70 px-2 py-0.5 text-[11px] text-stone-600 dark:text-stone-300"
      >
        <Paperclip class="text-stone-400" :size="11" :stroke-width="1.75" />
        <span class="max-w-[140px] truncate">{{ file.name }}</span>
        <button
          type="button"
          class="grid h-4 w-4 place-items-center rounded-full text-stone-400 hover:text-rose-500 hover:bg-rose-500/[0.08] transition-colors cursor-pointer"
          aria-label="移除文件"
          @click="removeFile(idx)"
        >
          <X :size="10" :stroke-width="2" />
        </button>
      </div>
    </div>

    <!-- Stacked composer surface -->
    <div
      class="cf-composer relative rounded-2xl bg-white/85 dark:bg-stone-900/70 backdrop-blur-xl ring-1 ring-stone-200/80 dark:ring-stone-700/70 shadow-[inset_0_1px_0_rgba(255,255,255,0.7),0_22px_44px_-24px_rgba(15,23,42,0.18)] dark:shadow-[inset_0_1px_0_rgba(255,255,255,0.05),0_28px_48px_-22px_rgba(0,0,0,0.6)] transition-shadow duration-300 ease-[cubic-bezier(0.16,1,0.3,1)] focus-within:ring-2 focus-within:ring-primary-400/50"
      :class="{ 'cf-composer--disabled': disabled }"
    >
      <!-- 第一行：textarea，无边框，仅文本 -->
      <textarea
        ref="textareaRef"
        v-model="inputText"
        rows="1"
        class="block w-full min-h-[44px] max-h-[180px] resize-none px-4 pt-3 pb-1 text-[14px] leading-relaxed text-stone-800 dark:text-stone-100 bg-transparent placeholder-stone-400 dark:placeholder-stone-500 outline-none"
        :disabled="disabled"
        placeholder="问点什么 · Enter 发送 · Shift+Enter 换行"
        @keydown="handleKeydown"
        @input="autoResize"
      />

      <!-- 第二行：工具栏 -->
      <div class="flex items-center gap-1 px-2.5 pb-2 pt-1">
        <!-- 技能切换 button + chevron -->
        <div class="relative">
          <button
            type="button"
            class="cf-tool-chip inline-flex items-center gap-1 px-2 py-1 text-[11.5px] font-medium tracking-tight rounded-lg ring-1 ring-inset ring-stone-200/80 dark:ring-stone-700/70 text-stone-600 dark:text-stone-300 hover:text-stone-900 dark:hover:text-stone-100 hover:bg-stone-900/[0.04] dark:hover:bg-white/[0.05] transition-colors cursor-pointer"
            :disabled="disabled"
            @click="skillMenuOpen = !skillMenuOpen"
          >
            <Sparkles class="text-primary-500" :size="12" :stroke-width="2" />
            {{ currentSkillName }}
            <ChevronDown
              class="text-stone-400 transition-transform duration-200"
              :class="{ 'rotate-180': skillMenuOpen }"
              :size="11"
              :stroke-width="2"
            />
          </button>

          <Transition name="cf-skill-pop">
            <div
              v-if="skillMenuOpen"
              ref="skillMenuRef"
              class="absolute bottom-[calc(100%+6px)] left-0 z-30 min-w-[180px] rounded-xl bg-white/95 dark:bg-stone-900/95 backdrop-blur-xl ring-1 ring-stone-200/80 dark:ring-stone-700/70 shadow-[0_18px_36px_-12px_rgba(15,23,42,0.22)] dark:shadow-[0_24px_48px_-18px_rgba(0,0,0,0.7)] p-1.5"
            >
              <button
                type="button"
                class="cf-skill-option flex w-full items-center gap-2 rounded-lg px-2.5 py-1.5 text-left text-[12.5px] font-medium tracking-tight transition-colors cursor-pointer"
                :class="!store.currentSkillCode
                  ? 'bg-primary-500/[0.08] text-primary-600 dark:text-primary-400'
                  : 'text-stone-600 dark:text-stone-300 hover:bg-stone-900/[0.04] dark:hover:bg-white/[0.06]'"
                @click="selectSkill('')"
              >
                <span
                  class="h-1.5 w-1.5 rounded-full"
                  :class="!store.currentSkillCode ? 'bg-primary-500' : 'bg-stone-300 dark:bg-stone-600'"
                />
                通用助手
              </button>
              <button
                v-for="skill in store.skills"
                :key="skill.skillCode"
                type="button"
                class="cf-skill-option flex w-full items-center gap-2 rounded-lg px-2.5 py-1.5 text-left text-[12.5px] font-medium tracking-tight transition-colors cursor-pointer"
                :class="store.currentSkillCode === skill.skillCode
                  ? 'bg-primary-500/[0.08] text-primary-600 dark:text-primary-400'
                  : 'text-stone-600 dark:text-stone-300 hover:bg-stone-900/[0.04] dark:hover:bg-white/[0.06]'"
                @click="selectSkill(skill.skillCode)"
              >
                <span
                  class="h-1.5 w-1.5 rounded-full"
                  :class="store.currentSkillCode === skill.skillCode ? 'bg-primary-500' : 'bg-stone-300 dark:bg-stone-600'"
                />
                {{ skill.name }}
              </button>
            </div>
          </Transition>
        </div>

        <!-- 插入代码 -->
        <button
          type="button"
          class="cf-tool-icon"
          :disabled="disabled"
          title="插入代码"
          @click="showCodeEditor = true"
        >
          <Code2 :size="14" :stroke-width="1.75" />
        </button>
        <!-- 上传文件 -->
        <button
          type="button"
          class="cf-tool-icon"
          :disabled="disabled"
          title="上传文件"
          @click="triggerFileUpload"
        >
          <Paperclip :size="14" :stroke-width="1.75" />
        </button>
        <input
          ref="fileInputRef"
          type="file"
          accept=".java,.py,.js,.ts,.cpp,.c,.go,.rs,.txt,.md,.json,.xml,.yml,.yaml,image/*"
          class="hidden"
          multiple
          @change="handleFileChange"
        />

        <!-- 右侧：配额 mono + 发送 -->
        <div class="ml-auto flex items-center gap-2">
          <span v-if="store.quotaRemaining !== null" class="hidden sm:inline-flex items-center gap-1 text-[10.5px] font-medium tracking-[0.05em] text-stone-400 dark:text-stone-500">
            <span class="tabular-nums">{{ store.quotaRemaining }}</span>
            <span class="opacity-50">/</span>
            <span class="tabular-nums">{{ store.quota?.dailyMessageLimit ?? '—' }}</span>
          </span>

          <button
            type="button"
            class="cf-send-btn"
            :disabled="disabled || !inputText.trim()"
            @click="send"
          >
            <ArrowUp :size="14" :stroke-width="2.25" />
            <span class="hidden sm:inline">发送</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 代码编辑器（teleport 到 body） -->
    <Teleport to="body">
      <AgentCodeEditor
        v-if="showCodeEditor"
        @close="showCodeEditor = false"
        @insert="insertCodeBlock"
      />
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onBeforeUnmount, onMounted } from 'vue'
import { ArrowUp, ChevronDown, Code2, Paperclip, Sparkles, X } from 'lucide-vue-next'
import { useAgentChatStore } from '@/stores/agentChat'
import AgentCodeEditor from '@/components/agent/AgentCodeEditor.vue'

defineOptions({ name: 'AgentInputArea' })

const emit = defineEmits<{
  send: [message: string, files: File[]]
}>()

const store = useAgentChatStore()

const inputText = ref('')
const files = ref<File[]>([])
const fileInputRef = ref<HTMLInputElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const showCodeEditor = ref(false)

/** 技能选择下拉菜单显隐 */
const skillMenuOpen = ref(false)
const skillMenuRef = ref<HTMLElement | null>(null)

const disabled = computed(() => store.connectionState === 'streaming' || store.connectionState === 'connecting')

/** 当前选中技能名（用于按钮标签显示） */
const currentSkillName = computed(() => {
  const cur = store.skills.find((s) => s.skillCode === store.currentSkillCode)
  return cur?.name ?? '通用助手'
})

/** 切换技能并关闭菜单 */
function selectSkill(code: string) {
  store.currentSkillCode = code
  skillMenuOpen.value = false
}

/** 外部点击关闭技能菜单 */
function closeSkillMenuOnOutside(event: MouseEvent) {
  if (!skillMenuOpen.value) return
  const menu = skillMenuRef.value
  const target = event.target as Node | null
  if (menu && target && !menu.contains(target) && !(target instanceof HTMLElement && target.closest('.cf-tool-chip'))) {
    skillMenuOpen.value = false
  }
}

/** Esc 关闭菜单 */
function closeSkillMenuOnEsc(event: KeyboardEvent) {
  if (event.key === 'Escape' && skillMenuOpen.value) {
    skillMenuOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('click', closeSkillMenuOnOutside)
  window.addEventListener('keydown', closeSkillMenuOnEsc)
})

onBeforeUnmount(() => {
  window.removeEventListener('click', closeSkillMenuOnOutside)
  window.removeEventListener('keydown', closeSkillMenuOnEsc)
})

function autoResize() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

function send() {
  if (!inputText.value.trim() && files.value.length === 0) return
  emit('send', inputText.value, [...files.value])
  inputText.value = ''
  files.value = []
  nextTick(() => {
    const el = textareaRef.value
    if (el) el.style.height = 'auto'
  })
}

function triggerFileUpload() {
  fileInputRef.value?.click()
}

function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files) {
    files.value.push(...Array.from(target.files))
  }
  target.value = ''
}

function removeFile(idx: number) {
  files.value.splice(idx, 1)
}

function insertCodeBlock(code: string, language: string) {
  const langTag = language || ''
  inputText.value += `\n\`\`\`${langTag}\n${code}\n\`\`\`\n`
  showCodeEditor.value = false
  nextTick(() => textareaRef.value?.focus())
}
</script>

<style scoped>
/* 整体 surface disabled 视觉降饱和 */
.cf-composer--disabled {
  opacity: 0.7;
}

/* 工具栏图标按钮 */
.cf-tool-icon {
  display: inline-grid;
  place-items: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  color: rgb(120 113 108 / 1);
  cursor: pointer;
  transition:
    color 160ms cubic-bezier(0.16, 1, 0.3, 1),
    background-color 160ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 160ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-tool-icon:hover {
  color: rgb(63 63 70 / 1);
  background-color: rgb(0 0 0 / 0.04);
}
.cf-tool-icon:active {
  transform: scale(0.94);
}
.cf-tool-icon:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* 发送按钮：brand 渐变 + tinted shadow + 微 lift */
.cf-send-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  height: 30px;
  padding-inline: 0.875rem;
  border-radius: 0.625rem;
  font-size: 12.5px;
  font-weight: 500;
  letter-spacing: -0.01em;
  color: white;
  background-color: rgb(var(--cf-color-primary-500-rgb) / 1);
  cursor: pointer;
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.22),
    inset 0 -1px 0 0 rgba(0, 0, 0, 0.08),
    0 1px 2px 0 rgba(15, 23, 42, 0.08),
    0 8px 16px -6px rgb(var(--cf-color-primary-500-rgb) / 0.4);
  transition: all 180ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-send-btn:hover {
  background-color: rgb(var(--cf-color-primary-600-rgb) / 1);
  transform: translateY(-1px);
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.2),
    inset 0 -1px 0 0 rgba(0, 0, 0, 0.1),
    0 2px 4px 0 rgba(15, 23, 42, 0.1),
    0 12px 24px -8px rgb(var(--cf-color-primary-500-rgb) / 0.5);
}
.cf-send-btn:active {
  transform: scale(0.97) translateY(0);
}
.cf-send-btn:disabled {
  background-color: rgb(168 162 158 / 0.6);
  color: rgb(255 255 255 / 0.7);
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

/* 技能 chip：禁用样式 */
.cf-tool-chip:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 技能菜单 pop transition */
.cf-skill-pop-enter-active,
.cf-skill-pop-leave-active {
  transition:
    opacity 200ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 240ms cubic-bezier(0.16, 1, 0.3, 1);
  transform-origin: bottom left;
}
.cf-skill-pop-enter-from,
.cf-skill-pop-leave-to {
  opacity: 0;
  transform: translateY(4px) scale(0.96);
}

@media (prefers-reduced-motion: reduce) {
  .cf-send-btn,
  .cf-tool-icon {
    transition: none;
  }
}
</style>

<style>
html.dark .cf-tool-icon {
  color: rgb(168 162 158 / 1);
}
html.dark .cf-tool-icon:hover {
  color: rgb(231 229 228 / 1);
  background-color: rgb(255 255 255 / 0.05);
}
html.dark .cf-send-btn:disabled {
  background-color: rgb(82 82 91 / 0.5);
  color: rgb(255 255 255 / 0.5);
}
</style>

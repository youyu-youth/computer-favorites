<template>
  <div class="cf-composer-wrap px-4 pb-4 pt-3 sm:px-8 sm:pb-6">
    <!-- 已附加文件 chips（行内列出在 surface 上方） -->
    <div v-if="files.length > 0" class="mx-auto mb-2 flex max-w-4xl flex-wrap gap-1.5">
      <div
        v-for="(file, idx) in files"
        :key="idx"
        class="cf-file-chip group/chip inline-flex items-center gap-1.5 rounded-full border border-stone-200 bg-white px-2 py-0.5 text-[11px] text-stone-600 dark:border-white/10 dark:bg-[#0f0f11] dark:text-stone-400"
      >
        <Paperclip class="text-stone-500" :size="11" :stroke-width="1.75" />
        <span class="max-w-[140px] truncate">{{ file.name }}</span>
        <button
          type="button"
          class="grid h-4 w-4 cursor-pointer place-items-center rounded-full text-stone-500 transition-colors hover:bg-rose-500/[0.08] hover:text-rose-500"
          aria-label="移除文件"
          @click="removeFile(idx)"
        >
          <X :size="10" :stroke-width="2" />
        </button>
      </div>
    </div>

    <!-- Stacked composer surface -->
    <div class="mx-auto max-w-4xl">
      <div
        class="cf-composer relative overflow-hidden rounded-2xl border border-primary-500 bg-white shadow-[0_0_15px_rgb(var(--cf-color-primary-500-rgb)/0.12)] transition-colors duration-300 focus-within:border-primary-400 dark:bg-[#0f0f11] dark:shadow-[0_0_15px_rgb(var(--cf-color-primary-500-rgb)/0.16)]"
        :class="{ 'cf-composer--disabled': disabled }"
      >
        <!-- 第一行：textarea，无边框，仅文本 -->
        <textarea
          ref="textareaRef"
          v-model="inputText"
          rows="1"
          class="block min-h-[52px] max-h-[180px] w-full resize-none bg-transparent px-4 pb-1 pt-4 text-[14px] leading-relaxed text-stone-900 outline-none placeholder:text-stone-400 dark:text-stone-200 dark:placeholder:text-stone-600"
          :disabled="disabled"
          placeholder="输入消息给 Nova AI..."
          @keydown="handleKeydown"
          @input="autoResize"
        />

        <!-- 第二行：工具栏 -->
        <div class="flex items-center gap-1 px-2.5 pb-2 pt-1">
          <!-- 技能切换 button + chevron -->
          <div class="relative">
            <button
              type="button"
              class="cf-tool-chip inline-flex cursor-pointer items-center gap-1 rounded-lg border border-stone-200 px-2 py-1 text-[11.5px] font-medium tracking-tight text-stone-600 transition-colors hover:bg-stone-900/[0.05] hover:text-stone-900 dark:border-white/10 dark:text-stone-400 dark:hover:bg-white/[0.05] dark:hover:text-stone-100"
              :disabled="disabled"
              @click="skillMenuOpen = !skillMenuOpen"
            >
              <Sparkles class="text-primary-400" :size="12" :stroke-width="2" />
              {{ currentSkillName }}
              <ChevronDown
                class="text-stone-600 transition-transform duration-200"
                :class="{ 'rotate-180': skillMenuOpen }"
                :size="11"
                :stroke-width="2"
              />
            </button>

            <Transition name="cf-skill-pop">
              <div
                v-if="skillMenuOpen"
                ref="skillMenuRef"
                class="absolute bottom-[calc(100%+6px)] left-0 z-30 min-w-[180px] rounded-xl border border-stone-200 bg-white p-1.5 shadow-[0_24px_48px_-18px_rgba(15,23,42,0.28)] dark:border-white/10 dark:bg-[#0f0f11] dark:shadow-[0_24px_48px_-18px_rgba(0,0,0,0.8)]"
              >
                <button
                  v-for="skill in store.skills"
                  :key="skill.code"
                  type="button"
                  class="cf-skill-option flex w-full cursor-pointer items-center gap-2 rounded-lg px-2.5 py-1.5 text-left text-[12.5px] font-medium tracking-tight transition-colors"
                  :class="
                    store.currentSkillCode === skill.code
                      ? 'bg-primary-500/[0.08] text-primary-400'
                      : 'text-stone-600 hover:bg-stone-900/[0.05] hover:text-stone-900 dark:text-stone-400 dark:hover:bg-white/[0.06] dark:hover:text-stone-100'
                  "
                  @click="selectSkill(skill.code)"
                >
                  <span
                    class="h-1.5 w-1.5 rounded-full"
                    :class="
                      store.currentSkillCode === skill.code ? 'bg-primary-500' : 'bg-stone-600'
                    "
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
            <span
              v-if="store.quotaRemaining !== null"
              class="hidden items-center gap-1 text-[10.5px] font-medium tracking-[0.05em] text-stone-600 sm:inline-flex"
            >
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
      <div class="mt-3 text-center text-[11px] font-medium text-stone-500 dark:text-stone-700">
        Nova AI 可能会出错，请核对重要信息。
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

const disabled = computed(
  () => store.connectionState === 'streaming' || store.connectionState === 'connecting',
)

/** 当前选中技能名（用于按钮标签显示） */
const currentSkillName = computed(() => {
  const cur = store.skills.find((s) => s.code === store.currentSkillCode)
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
  if (
    menu &&
    target &&
    !menu.contains(target) &&
    !(target instanceof HTMLElement && target.closest('.cf-tool-chip'))
  ) {
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
  color: rgb(87 83 78 / 1);
  cursor: pointer;
  transition:
    color 160ms cubic-bezier(0.16, 1, 0.3, 1),
    background-color 160ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 160ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-tool-icon:hover {
  color: rgb(28 25 23 / 1);
  background-color: rgb(0 0 0 / 0.05);
}
:global(html.dark) .cf-tool-icon {
  color: rgb(168 162 158 / 1);
}
:global(html.dark) .cf-tool-icon:hover {
  color: rgb(231 229 228 / 1);
  background-color: rgb(255 255 255 / 0.05);
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
    0 8px 16px -6px rgb(var(--cf-color-primary-500-rgb) / 0.4);
  transition: all 180ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cf-send-btn:hover {
  background-color: rgb(var(--cf-color-primary-600-rgb) / 1);
  transform: translateY(-1px);
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.2),
    0 12px 24px -8px rgb(var(--cf-color-primary-500-rgb) / 0.5);
}
.cf-send-btn:active {
  transform: scale(0.97) translateY(0);
}
.cf-send-btn:disabled {
  background-color: rgb(214 211 209 / 0.8);
  color: rgb(120 113 108 / 0.7);
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}
:global(html.dark) .cf-send-btn:disabled {
  background-color: rgb(82 82 91 / 0.5);
  color: rgb(255 255 255 / 0.5);
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

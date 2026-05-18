<template>
  <div class="cf-session-panel relative flex h-full flex-col bg-stone-100 dark:bg-[#100f0d]">
    <!-- Top brand row -->
    <div class="px-4 pt-4 pb-2">
      <div class="flex items-center gap-2 mb-3">
        <span
          class="grid h-7 w-7 place-items-center rounded-lg bg-primary-500 text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.32),0_6px_14px_-6px_rgb(var(--cf-color-primary-500-rgb)/0.6)]"
        >
          <Sparkles :size="14" :stroke-width="2" />
        </span>
        <span class="text-[13px] font-semibold tracking-tight text-stone-900 dark:text-stone-100"
          >CS Copilot</span
        >
        <span class="ml-auto text-[10px] font-medium tracking-[0.08em] text-stone-500 uppercase dark:text-stone-400"
          >对话</span
        >
      </div>

      <!-- 新对话 CTA -->
      <button
        type="button"
        class="cf-new-chat-btn group relative flex w-full items-center justify-center gap-1.5 rounded-xl px-3.5 py-2.5 text-[13px] font-medium text-white transition-all duration-200 ease-[cubic-bezier(0.16,1,0.3,1)] active:scale-[0.985] active:translate-y-px"
        @click="$emit('newChat')"
      >
        <Plus
          :size="15"
          :stroke-width="2"
          class="transition-transform duration-300 group-hover:rotate-90"
        />
        新对话
      </button>
    </div>

    <!-- 搜索：嵌入式 inline icon -->
    <div class="px-4 pb-3 pt-1">
      <label class="cf-search-wrap relative block">
        <Search
          class="absolute left-2.5 top-1/2 -translate-y-1/2 text-stone-400"
          :size="13"
          :stroke-width="1.75"
        />
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索会话"
          class="block w-full rounded-lg bg-white pl-7 pr-2 py-1.5 text-xs text-stone-900 placeholder-stone-400 ring-1 ring-inset ring-stone-200/80 outline-none transition-all duration-200 ease-[cubic-bezier(0.16,1,0.3,1)] focus:bg-white focus:ring-2 focus:ring-primary-400 focus:ring-offset-0 dark:bg-white/[0.04] dark:text-stone-100 dark:placeholder-stone-500 dark:ring-white/10 dark:focus:bg-white/[0.06]"
          @input="onSearchInput"
        />
      </label>
    </div>

    <!-- 分组头：仅装饰 -->
    <div
      class="px-5 pb-1.5 text-[10px] font-medium tracking-[0.12em] uppercase text-stone-500 dark:text-stone-500"
    >
      最近会话
    </div>

    <!-- 会话列表 -->
    <div class="flex-1 overflow-y-auto cf-scroll pb-2">
      <!-- 骨架屏（加载中） -->
      <div v-if="store.sessionsLoading" class="space-y-2 px-2 py-1">
        <div
          v-for="n in 5"
          :key="n"
          class="cf-skeleton mx-2 h-9 rounded-xl"
          :style="{ '--cf-i': n }"
        />
      </div>

      <!-- 空状态 -->
      <div
        v-else-if="store.sessions.length === 0"
        class="mx-4 mt-4 flex flex-col items-start gap-2 rounded-2xl border border-dashed border-stone-300/80 bg-white/60 px-4 py-5 dark:border-white/10 dark:bg-white/[0.03]"
      >
        <span class="grid h-7 w-7 place-items-center rounded-lg bg-stone-200/80 text-stone-500 dark:bg-white/[0.06]">
          <MessageSquareDashed :size="14" :stroke-width="1.75" />
        </span>
        <p class="text-[13px] font-medium text-stone-700 leading-snug dark:text-stone-200">还没有对话</p>
        <p class="text-[11px] text-stone-400 dark:text-stone-500 leading-relaxed">
          点击上方「新对话」开始你的第一次提问
        </p>
      </div>

      <!-- 列表 -->
      <AgentSessionItem
        v-for="session in store.sessions"
        :key="session.id"
        :session="session"
        :is-active="session.id === store.currentSessionId"
        @select="store.switchToSession(session)"
        @toggle-pin="handleTogglePin"
        @delete="handleDelete"
        @rename="handleRename"
      />
    </div>

    <!-- 配额：mono 数字 + progress bar -->
    <div
      v-if="store.quotaRemaining !== null"
      class="px-4 py-3 border-t border-stone-200/80 bg-stone-100 dark:border-white/10 dark:bg-[#100f0d]"
    >
      <div class="flex items-baseline justify-between mb-1.5">
        <span
          class="text-[10px] font-medium tracking-[0.1em] uppercase text-stone-400 dark:text-stone-500"
          >今日配额</span
        >
        <span class="text-[11px] tabular-nums font-medium text-stone-700 dark:text-stone-200">
          <span class="text-primary-500">{{ store.quotaRemaining }}</span>
          <span class="text-stone-400 dark:text-stone-500">
            / {{ store.quota?.dailyMessageLimit ?? '—' }}</span
          >
        </span>
      </div>
      <div class="h-[3px] w-full overflow-hidden rounded-full bg-stone-200/80 dark:bg-white/[0.06]">
        <div
          class="h-full rounded-full bg-primary-500 transition-[width] duration-500 ease-[cubic-bezier(0.16,1,0.3,1)]"
          :style="{ width: `${quotaPercent}%` }"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-05-17
 *
 * Agent 会话列表组件 — 左侧边栏会话管理面板
 */
import { computed, ref } from 'vue'
import { Sparkles, Plus, Search, MessageSquareDashed } from 'lucide-vue-next'
import { useAgentChatStore } from '@/stores/agentChat'
import { togglePin, deleteSession, renameSession } from '@/api/agent'
import AgentSessionItem from '@/components/agent/AgentSessionItem.vue'

defineOptions({ name: 'AgentSessionList' })

defineEmits<{
  newChat: []
}>()

const store = useAgentChatStore()
const searchKeyword = ref('')

let searchTimer: ReturnType<typeof setTimeout> | null = null

/**
 * 当日配额剩余百分比（0-100），用于配额进度条
 * - 后端未返回 quota.dailyMessageLimit 时回退为 0%（避免 0/0 NaN）
 */
const quotaPercent = computed(() => {
  const limit = store.quota?.dailyMessageLimit
  const remaining = store.quotaRemaining
  if (!limit || remaining == null) return 0
  return Math.max(0, Math.min(100, Math.round((remaining / limit) * 100)))
})

/**
 * 搜索输入防抖处理，300ms 后触发会话列表加载
 */
function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    store.loadSessions(searchKeyword.value || undefined)
  }, 300)
}

/**
 * 切换会话置顶状态
 */
async function handleTogglePin(id: number, pinned: boolean) {
  try {
    await togglePin(id, pinned)
    store.updateSession(id, { pinned: pinned ? 1 : 0 })
  } catch {
    /* 忽略错误 */
  }
}

/**
 * 删除会话
 */
async function handleDelete(id: number) {
  try {
    await deleteSession(id)
    store.removeSession(id)
    if (store.currentSessionId === id) {
      store.startNewChat()
    }
  } catch {
    /* 忽略错误 */
  }
}

/**
 * 重命名会话
 */
async function handleRename(id: number, title: string) {
  try {
    await renameSession(id, title)
    store.updateSession(id, { title })
  } catch {
    /* 忽略错误 */
  }
}
</script>

<style scoped>
/* 主 CTA：内嵌高光 + 染色阴影，避免塑料色块 */
.cf-new-chat-btn {
  background-color: rgb(var(--cf-color-primary-500-rgb) / 1);
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.24),
    inset 0 -1px 0 0 rgba(0, 0, 0, 0.08),
    0 1px 2px 0 rgba(15, 23, 42, 0.08),
    0 10px 22px -8px rgb(var(--cf-color-primary-500-rgb) / 0.45);
}
.cf-new-chat-btn:hover {
  background-color: rgb(var(--cf-color-primary-600-rgb) / 1);
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.22),
    inset 0 -1px 0 0 rgba(0, 0, 0, 0.12),
    0 2px 4px 0 rgba(15, 23, 42, 0.1),
    0 14px 28px -10px rgb(var(--cf-color-primary-500-rgb) / 0.55);
}

/* 自定义滚动条，避免默认浏览器粗条带 */
.cf-scroll {
  scrollbar-width: thin;
  scrollbar-color: rgb(120 113 108 / 0.25) transparent;
}
.cf-scroll::-webkit-scrollbar {
  width: 4px;
}
.cf-scroll::-webkit-scrollbar-thumb {
  background-color: rgb(120 113 108 / 0.25);
  border-radius: 999px;
}
.cf-scroll::-webkit-scrollbar-thumb:hover {
  background-color: rgb(120 113 108 / 0.4);
}

/* 骨架屏：错落 stagger + shimmer 动画 */
.cf-skeleton {
  position: relative;
  overflow: hidden;
  background: linear-gradient(
    90deg,
    rgb(168 162 158 / 0.08) 0%,
    rgb(168 162 158 / 0.16) 50%,
    rgb(168 162 158 / 0.08) 100%
  );
  background-size: 200% 100%;
  animation:
    cf-shimmer 1.6s linear infinite,
    cf-skeleton-in 320ms cubic-bezier(0.16, 1, 0.3, 1) both;
  animation-delay: 0s, calc(var(--cf-i, 0) * 60ms);
}
:global(html.dark) .cf-skeleton {
  background: linear-gradient(
    90deg,
    rgb(255 255 255 / 0.04) 0%,
    rgb(255 255 255 / 0.08) 50%,
    rgb(255 255 255 / 0.04) 100%
  );
  background-size: 200% 100%;
}
@keyframes cf-shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
@keyframes cf-skeleton-in {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .cf-skeleton {
    animation: none;
  }
}
</style>

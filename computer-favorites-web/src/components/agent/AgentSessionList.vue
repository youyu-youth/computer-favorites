<template>
  <div
    class="cf-session-panel flex h-full flex-col bg-white text-stone-900 dark:bg-[#000000] dark:text-stone-200"
  >
    <div class="flex items-center justify-between px-5 py-5">
      <div class="flex items-center gap-2">
        <span
          class="grid h-9 w-9 place-items-center rounded-xl border border-primary-500/30 bg-primary-500/[0.12] text-primary-400"
        >
          <Sparkles :size="18" :stroke-width="2" />
        </span>
        <div class="min-w-0">
          <h2
            class="m-0 truncate text-[18px] font-bold tracking-tight text-stone-950 dark:text-white"
          >
            Nova AI
          </h2>
          <p class="m-0 truncate text-[11px] font-medium text-stone-500">计算机学习智能助手</p>
        </div>
      </div>
      <span
        class="rounded-md border border-stone-200 bg-stone-50 px-2 py-1 text-[10px] font-semibold text-stone-500 dark:border-white/10 dark:bg-white/[0.04]"
      >
        AI
      </span>
    </div>

    <div class="px-4 pb-4">
      <button
        type="button"
        class="cf-new-chat-btn flex w-full cursor-pointer items-center justify-between rounded-xl border-0 px-4 py-3 text-[15px] font-semibold text-white transition-colors duration-200"
        @click="$emit('newChat')"
      >
        <span class="inline-flex items-center gap-2">
          <Plus :size="18" :stroke-width="2" />
          新对话
        </span>
        <span
          class="rounded-md border border-white/20 bg-black/10 px-2 py-0.5 text-xs font-medium text-white/90"
        >
          Ctrl K
        </span>
      </button>
    </div>

    <div class="px-4 pb-3">
      <label class="relative block">
        <Search
          class="absolute left-3 top-1/2 -translate-y-1/2 text-stone-500"
          :size="15"
          :stroke-width="1.75"
        />
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索会话"
          class="block w-full rounded-xl border border-stone-200 bg-stone-50 py-2.5 pl-9 pr-3 text-[13px] text-stone-900 outline-none placeholder:text-stone-400 transition-colors duration-200 focus:border-primary-500/70 focus:bg-white dark:border-white/10 dark:bg-[#09090b] dark:text-stone-100 dark:placeholder:text-stone-600 dark:focus:bg-[#0f0f11]"
          @input="onSearchInput"
        />
      </label>
    </div>

    <div
      class="flex items-center justify-between px-5 pb-2 pt-1 text-xs font-medium text-stone-500"
    >
      <span>最近会话</span>
      <span class="tabular-nums">{{ store.sessions.length }}</span>
    </div>

    <div class="cf-scroll flex-1 overflow-y-auto px-2 pb-3">
      <div v-if="store.sessionsLoading" class="space-y-1.5 px-2 py-1">
        <div v-for="n in 6" :key="n" class="cf-skeleton h-10 rounded-xl" :style="{ '--cf-i': n }" />
      </div>

      <div
        v-else-if="store.sessions.length === 0"
        class="mx-2 mt-3 rounded-2xl border border-dashed border-stone-200 bg-stone-50 px-4 py-5 dark:border-white/10 dark:bg-white/[0.03]"
      >
        <span
          class="mb-3 grid h-9 w-9 place-items-center rounded-xl border border-stone-200 bg-white text-stone-500 dark:border-white/10 dark:bg-white/[0.04]"
        >
          <MessageSquareDashed :size="16" :stroke-width="1.75" />
        </span>
        <p class="m-0 text-[14px] font-semibold text-stone-800 dark:text-stone-200">暂无会话</p>
        <p class="m-0 mt-1 text-[12px] leading-relaxed text-stone-500">
          点击「新对话」开始向 Nova AI 提问。
        </p>
      </div>

      <div v-else class="space-y-1">
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
    </div>

    <div
      v-if="store.quotaRemaining !== null"
      class="border-t border-stone-200/80 bg-stone-50 px-4 py-4 dark:border-white/10 dark:bg-[#050505]"
    >
      <div class="mb-2 flex items-center justify-between">
        <span class="text-[12px] font-medium text-stone-500">今日对话配额</span>
        <span class="text-[12px] font-semibold tabular-nums text-stone-700 dark:text-stone-300">
          <span class="text-primary-400">{{ store.quotaRemaining }}</span>
          <span class="text-stone-600"> / {{ store.quota?.dailyMessageLimit ?? '—' }}</span>
        </span>
      </div>
      <div class="h-1 overflow-hidden rounded-full bg-stone-200 dark:bg-white/[0.06]">
        <div
          class="h-full rounded-full bg-primary-500 transition-[width] duration-500"
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
.cf-new-chat-btn {
  background-color: rgb(var(--cf-color-primary-500-rgb) / 1);
  box-shadow:
    inset 0 1px 0 0 rgba(255, 255, 255, 0.2),
    0 10px 24px -12px rgb(var(--cf-color-primary-500-rgb) / 0.8);
}
.cf-new-chat-btn:hover {
  background-color: rgb(var(--cf-color-primary-600-rgb) / 1);
}

.cf-scroll {
  scrollbar-width: thin;
  scrollbar-color: rgb(120 113 108 / 0.28) transparent;
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

.cf-skeleton {
  position: relative;
  overflow: hidden;
  background-color: rgb(120 113 108 / 0.12);
  animation: cf-skeleton-in 320ms cubic-bezier(0.16, 1, 0.3, 1) both;
  animation-delay: calc(var(--cf-i, 0) * 60ms);
}
:global(html.dark) .cf-skeleton {
  background-color: rgb(255 255 255 / 0.06);
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

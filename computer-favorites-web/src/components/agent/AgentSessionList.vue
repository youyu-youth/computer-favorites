<template>
  <div class="flex flex-col h-full bg-gray-50 dark:bg-gray-950">
    <!-- 新对话按钮 -->
    <div class="px-4 py-3">
      <button
        class="w-full px-4 py-2.5 text-sm rounded-lg bg-amber-500 text-white hover:bg-amber-600
               transition-colors cursor-pointer font-medium"
        @click="$emit('newChat')"
      >
        + 新对话
      </button>
    </div>

    <!-- 搜索 -->
    <div class="px-4 pb-2">
      <input
        v-model="searchKeyword"
        type="text"
        placeholder="搜索会话..."
        class="w-full text-xs px-3 py-1.5 rounded-md border border-gray-200 dark:border-gray-700
               bg-white dark:bg-gray-900 text-gray-800 dark:text-gray-200
               placeholder-gray-400 dark:placeholder-gray-500
               outline-none focus:border-amber-400 transition-colors"
        @input="onSearchInput"
      />
    </div>

    <!-- 会话列表 -->
    <div class="flex-1 overflow-y-auto px-1">
      <div v-if="store.sessionsLoading" class="px-3 py-8 text-center text-sm text-gray-400">
        加载中...
      </div>
      <div v-else-if="store.sessions.length === 0" class="px-3 py-8 text-center text-sm text-gray-400">
        暂无对话
      </div>
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

    <!-- 配额底部信息 -->
    <div v-if="store.quotaRemaining !== null" class="px-4 py-3 border-t border-gray-200 dark:border-gray-700">
      <p class="text-xs text-gray-500 dark:text-gray-400">
        今日配额:
        <span class="text-amber-500 font-semibold">{{ store.quotaRemaining }}</span>
        / {{ store.quota?.dailyMessageLimit ?? '-' }} 条消息
      </p>
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
import { ref } from 'vue'
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
  } catch { /* 忽略错误 */ }
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
  } catch { /* 忽略错误 */ }
}

/**
 * 重命名会话
 */
async function handleRename(id: number, title: string) {
  try {
    await renameSession(id, title)
    store.updateSession(id, { title })
  } catch { /* 忽略错误 */ }
}
</script>

<template>
  <div class="flex h-[calc(100vh-4rem)]">
    <!-- Desktop sidebar -->
    <aside class="hidden md:flex flex-col w-72 shrink-0 border-r border-gray-200 dark:border-gray-700">
      <AgentSessionList @new-chat="handleNewChat" />
    </aside>

    <!-- Mobile drawer -->
    <Teleport to="body">
      <div
        v-if="mobileSidebarOpen"
        class="fixed inset-0 z-50 md:hidden"
        @click.self="mobileSidebarOpen = false"
      >
        <div class="absolute inset-0 bg-black/40" />
        <div class="absolute left-0 top-0 bottom-0 w-80 bg-white dark:bg-gray-950 shadow-xl">
          <AgentSessionList @new-chat="handleNewChat" />
        </div>
      </div>
    </Teleport>

    <!-- Main chat area -->
    <main class="flex-1 min-w-0">
      <AgentChatView @toggle-sidebar="mobileSidebarOpen = !mobileSidebarOpen" />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAgentChatStore } from '@/stores/agentChat'
import AgentSessionList from '@/components/agent/AgentSessionList.vue'
import AgentChatView from '@/components/agent/AgentChatView.vue'

defineOptions({ name: 'AgentView' })

const store = useAgentChatStore()
const mobileSidebarOpen = ref(false)

onMounted(async () => {
  await Promise.all([store.loadSessions(), store.loadQuota()])
})

function handleNewChat() {
  store.startNewChat()
  mobileSidebarOpen.value = false
}
</script>

<template>
  <div
    class="flex h-screen overflow-hidden bg-stone-50 text-stone-900 dark:bg-black dark:text-stone-200"
  >
    <aside
      class="hidden w-[280px] shrink-0 flex-col border-r border-stone-200/80 bg-white md:flex dark:border-white/10 dark:bg-black"
    >
      <AgentSessionList @new-chat="handleNewChat" />
    </aside>

    <Teleport to="body">
      <div
        v-if="mobileSidebarOpen"
        class="fixed inset-0 z-50 md:hidden"
        @click.self="mobileSidebarOpen = false"
      >
        <div class="absolute inset-0 bg-stone-950/40 dark:bg-black/80" />
        <div
          class="absolute bottom-0 left-0 top-0 w-[280px] border-r border-stone-200/80 bg-white shadow-2xl dark:border-white/10 dark:bg-black"
        >
          <AgentSessionList @new-chat="handleNewChat" />
        </div>
      </div>
    </Teleport>

    <main class="min-w-0 flex-1">
      <AgentChatUserView @toggle-sidebar="mobileSidebarOpen = !mobileSidebarOpen" />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAgentChatStore } from '@/stores/agentChat'
import AgentSessionList from '@/components/agent/AgentSessionList.vue'
import AgentChatUserView from '@/views/user/AgentChatUserView.vue'

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

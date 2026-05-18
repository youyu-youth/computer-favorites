<template>
  <div class="flex h-screen overflow-hidden bg-black text-stone-200">
    <aside class="hidden w-[280px] shrink-0 flex-col border-r border-white/10 bg-black md:flex">
      <AgentSessionList @new-chat="handleNewChat" />
    </aside>

    <Teleport to="body">
      <div
        v-if="mobileSidebarOpen"
        class="fixed inset-0 z-50 md:hidden"
        @click.self="mobileSidebarOpen = false"
      >
        <div class="absolute inset-0 bg-black/80" />
        <div
          class="absolute bottom-0 left-0 top-0 w-[280px] border-r border-white/10 bg-black shadow-2xl"
        >
          <AgentSessionList @new-chat="handleNewChat" />
        </div>
      </div>
    </Teleport>

    <main class="min-w-0 flex-1">
      <AgentChatAdminView @toggle-sidebar="mobileSidebarOpen = !mobileSidebarOpen" />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAgentChatStore } from '@/stores/agentChat'
import AgentSessionList from '@/components/agent/AgentSessionList.vue'
import AgentChatAdminView from '@/views/admin/AgentChatAdminView.vue'

defineOptions({ name: 'AdminAgentView' })

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

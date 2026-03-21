<script setup lang="ts">
import { reactive, ref, shallowRef, provide } from 'vue'
// @ts-ignore
import SettingsSidebar from '@/components/user/settings/SettingsSidebar.vue'
// @ts-ignore
import BasicProfileSection from '@/components/user/settings/sections/BasicProfileSection.vue'
// @ts-ignore
import AccountSettingsSection from '@/components/user/settings/sections/AccountSettingsSection.vue'
// @ts-ignore
import PreferenceSettingsSection from '@/components/user/settings/sections/PreferenceSettingsSection.vue'
// @ts-ignore
import DataManagementSection from '@/components/user/settings/sections/DataManagementSection.vue'
// @ts-ignore
import MessageSettingsSection from '@/components/user/settings/sections/MessageSettingsSection.vue'
import { mockUserBasicInfo, mockUserDetailProfile, mockUserPreferenceSetting } from '@/components/user/settings/mock'
import { settingsStateKey } from '@/components/user/settings/context'
import { useToast } from '@/composables/useToast'

defineOptions({
  name: 'SettingsView',
})

const tabs = [
  { id: 'basic', label: '基础资料', icon: 'i-lucide-user', component: BasicProfileSection },
  { id: 'account', label: '账号设置', icon: 'i-lucide-shield', component: AccountSettingsSection },
  { id: 'preference', label: '偏好设置', icon: 'i-lucide-sliders', component: PreferenceSettingsSection },
  { id: 'message', label: '消息设置', icon: 'i-lucide-bell', component: MessageSettingsSection },
  { id: 'data', label: '数据管理', icon: 'i-lucide-database', component: DataManagementSection },
]

const settingsState = reactive({
  basicInfo: { ...mockUserBasicInfo },
  profile: { ...mockUserDetailProfile },
  setting: { ...mockUserPreferenceSetting },
})
const toast = useToast()

provide(settingsStateKey, settingsState)

const firstTab = tabs[0]
if (!firstTab) {
  throw new Error('Settings tabs are not configured')
}

const activeTabId = ref(firstTab.id)
const activeComponent = shallowRef(firstTab.component)

const handleTabChange = (id: string) => {
  activeTabId.value = id
  const tab = tabs.find((t) => t.id === id)
  if (tab) {
    activeComponent.value = tab.component
  }
}

const handleSaveAllChanges = () => {
  toast.add({
    title: '保存成功',
    description: '已统一保存当前五个设置模块的更改',
    type: 'success',
  })
}
</script>

<template>
  <div class="min-h-[calc(100vh-8rem)] bg-slate-50 transition-colors duration-300 dark:bg-black">
    <UContainer class="max-w-6xl py-6 md:py-10">
      <div class="flex justify-end pb-4">
        <UButton variant="solid" class="cursor-pointer !bg-[#f59e0b] !text-white hover:!bg-[#d97706] active:!bg-[#d97706] focus-visible:!outline-[#f59e0b]" @click="handleSaveAllChanges">
          保存全部更改
        </UButton>
      </div>
      <div class="flex flex-col gap-8 md:flex-row">
        <!-- Sidebar -->
        <div class="w-full shrink-0 md:w-64">
          <SettingsSidebar
            :tabs="tabs"
            :active-id="activeTabId"
            @change="handleTabChange"
          />
        </div>

        <!-- Content Area -->
        <main class="min-w-0 flex-1">
          <transition name="fade" mode="out-in">
            <component :is="activeComponent" />
          </transition>
        </main>
      </div>
    </UContainer>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.fade-enter-from {
  opacity: 0;
  transform: translateY(4px);
}
.fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>

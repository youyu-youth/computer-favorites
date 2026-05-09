<script setup lang="ts">
import { computed, onMounted, provide, ref, shallowRef } from 'vue'
// @ts-ignore
import SettingsSidebar from '@/components/user/settings/SettingsSidebar.vue'
// @ts-ignore
import BasicProfileSection from '@/components/user/settings/sections/BasicProfileSection.vue'
// @ts-ignore
import AccountSettingsSection from '@/components/user/settings/sections/AccountSettingsSection.vue'
// @ts-ignore
import PreferenceSettingsSection from '@/components/user/settings/sections/PreferenceSettingsSection.vue'
// @ts-ignore
import PrivacySettingsSection from '@/components/user/settings/sections/PrivacySettingsSection.vue'
// @ts-ignore
import DataManagementSection from '@/components/user/settings/sections/DataManagementSection.vue'
// @ts-ignore
import MessageSettingsSection from '@/components/user/settings/sections/MessageSettingsSection.vue'
import { settingsLockSignalKey } from '@/components/user/settings/context'
import { useToast } from '@/composables/useToast'
import { useSettingsStore } from '@/stores/settings'

defineOptions({
  name: 'SettingsView',
})

const tabs = [
  { id: 'basic', label: '基础资料', icon: 'i-lucide-user-round', component: BasicProfileSection, hint: 'Profile' },
  { id: 'account', label: '账号设置', icon: 'i-lucide-shield-check', component: AccountSettingsSection, hint: 'Security' },
  { id: 'preference', label: '偏好设置', icon: 'i-lucide-sliders-horizontal', component: PreferenceSettingsSection, hint: 'Appearance' },
  { id: 'privacy', label: '隐私设置', icon: 'i-lucide-lock-keyhole', component: PrivacySettingsSection, hint: 'Privacy' },
  { id: 'message', label: '消息设置', icon: 'i-lucide-bell', component: MessageSettingsSection, hint: 'Notifications' },
  { id: 'data', label: '数据管理', icon: 'i-lucide-database', component: DataManagementSection, hint: 'Data' },
]

const toast = useToast()
const settingsStore = useSettingsStore()

const cloneState = () =>
  JSON.parse(JSON.stringify({
    basicInfo: settingsStore.basicInfo,
    profile: settingsStore.profile,
    setting: settingsStore.setting,
    privacy: settingsStore.privacy,
  }))
const baselineSnapshot = ref(cloneState())
const lockSignal = ref(0)
const loadError = ref('')

provide(settingsLockSignalKey, lockSignal)

const firstTab = tabs[0]
if (!firstTab) {
  throw new Error('Settings tabs are not configured')
}

const activeTabId = ref(firstTab.id)
const activeComponent = shallowRef(firstTab.component)
const lastSavedAt = ref<Date | null>(null)
const saving = computed(() => settingsStore.saving)
const hasDirty = computed(
  () => JSON.stringify(cloneState()) !== JSON.stringify(baselineSnapshot.value),
)

const activeTabLabel = computed(() => tabs.find((t) => t.id === activeTabId.value)?.label ?? '')

const formatRelativeTime = (date: Date | null) => {
  if (!date) {
    return '尚未保存'
  }
  const diff = Math.floor((Date.now() - date.getTime()) / 1000)
  if (diff < 5) return '刚刚'
  if (diff < 60) return `${diff} 秒前`
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  return `${Math.floor(diff / 3600)} 小时前`
}

const lastSavedText = computed(() => formatRelativeTime(lastSavedAt.value))

const handleTabChange = (id: string) => {
  activeTabId.value = id
  const tab = tabs.find((t) => t.id === id)
  if (tab) {
    activeComponent.value = tab.component
  }
}

const loadSettings = async () => {
  loadError.value = ''
  try {
    await settingsStore.fetchProfile()
    baselineSnapshot.value = cloneState()
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '加载设置失败'
    toast.add({
      title: '加载失败',
      description: loadError.value,
      type: 'error',
    })
  }
}

const handleSaveAllChanges = async () => {
  if (saving.value || settingsStore.loading || !hasDirty.value) {
    return
  }
  try {
    await settingsStore.saveAll()
    baselineSnapshot.value = cloneState()
    lastSavedAt.value = new Date()
    lockSignal.value += 1
    toast.add({
      title: '保存成功',
      description: '已统一保存设置模块的更改',
      type: 'success',
    })
  } catch (error) {
    Object.assign(settingsStore.basicInfo, baselineSnapshot.value.basicInfo)
    Object.assign(settingsStore.profile, baselineSnapshot.value.profile)
    Object.assign(settingsStore.setting, baselineSnapshot.value.setting)
    Object.assign(settingsStore.privacy, baselineSnapshot.value.privacy)
    toast.add({
      title: '保存失败',
      description: error instanceof Error ? error.message : '设置保存失败，请稍后重试',
      type: 'error',
    })
  }
}

onMounted(loadSettings)
</script>

<template>
  <div class="cf-settings-shell relative min-h-[calc(100vh-8rem)] bg-slate-50 transition-colors duration-300 dark:bg-[#08080a]">
    <div class="cf-settings-glow pointer-events-none absolute inset-0 -z-10" aria-hidden="true"></div>
    <div class="cf-settings-grain pointer-events-none absolute inset-0 -z-10 opacity-60 dark:opacity-100" aria-hidden="true"></div>

    <UContainer class="relative z-0 max-w-7xl px-4 py-8 md:py-12 lg:px-6">
      <!-- Hero -->
      <header class="cf-settings-hero mb-8 flex flex-col gap-4 md:mb-10 md:flex-row md:items-end md:justify-between">
        <div class="space-y-3">
          <div class="flex items-center gap-2 text-xs font-medium uppercase tracking-[0.18em] text-amber-600 dark:text-amber-400">
            <span class="inline-block h-1.5 w-1.5 rounded-full bg-amber-500"></span>
            <span>Personal Workspace</span>
            <span class="text-slate-300 dark:text-white/20">/</span>
            <span class="text-slate-500 dark:text-slate-400 normal-case tracking-normal">{{ activeTabLabel }}</span>
          </div>
          <h1 class="text-[2rem] font-semibold leading-tight tracking-tight text-slate-900 dark:text-white md:text-[2.5rem]">
            设置<span class="ml-3 inline-block h-7 w-1 align-middle bg-amber-500 md:h-9"></span>
          </h1>
          <p class="max-w-xl text-sm text-slate-500 dark:text-slate-400">
            管理你的个人资料、账号安全、外观与通知偏好。所有更改在底部统一保存。
          </p>
        </div>

        <div class="flex flex-wrap items-center gap-3 text-xs text-slate-500 dark:text-slate-400">
          <div class="flex items-center gap-2 rounded-full border border-slate-200 bg-white/60 px-3 py-1.5 backdrop-blur dark:border-white/[0.10] dark:bg-[#16161d]">
            <UIcon name="i-lucide-clock" class="h-3.5 w-3.5" />
            <span>最近保存：{{ lastSavedText }}</span>
          </div>
          <div
            class="flex items-center gap-2 rounded-full px-3 py-1.5"
            :class="hasDirty
              ? 'border border-amber-300/60 bg-amber-50 text-amber-700 dark:border-amber-400/30 dark:bg-amber-500/10 dark:text-amber-300'
              : 'border border-emerald-300/60 bg-emerald-50 text-emerald-700 dark:border-emerald-400/30 dark:bg-emerald-500/10 dark:text-emerald-300'"
          >
            <span class="inline-block h-1.5 w-1.5 rounded-full" :class="hasDirty ? 'bg-amber-500 animate-pulse' : 'bg-emerald-500'"></span>
            <span>{{ hasDirty ? '有未保存改动' : '已是最新' }}</span>
          </div>
        </div>
      </header>

      <div class="flex flex-col gap-8 md:flex-row md:gap-10">
        <!-- Sidebar -->
        <aside class="w-full shrink-0 md:w-72">
          <SettingsSidebar
            :tabs="tabs"
            :active-id="activeTabId"
            @change="handleTabChange"
          />
        </aside>

        <!-- Content Area -->
        <main class="min-w-0 flex-1">
          <div class="cf-settings-card relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition-colors dark:border-white/[0.10] dark:bg-[#0f0f14] dark:shadow-none md:p-8">
            <div v-if="settingsStore.loading" class="space-y-4">
              <USkeleton class="h-8 w-40" />
              <USkeleton class="h-24 w-full" />
              <USkeleton class="h-24 w-full" />
              <USkeleton class="h-24 w-full" />
            </div>
            <div v-else-if="loadError" class="flex flex-col items-center gap-4 py-12 text-center">
              <UIcon name="i-lucide-circle-alert" class="h-10 w-10 text-rose-500" />
              <div class="space-y-1">
                <p class="text-sm font-semibold text-slate-900 dark:text-white">设置加载失败</p>
                <p class="text-xs text-slate-500 dark:text-slate-400">{{ loadError }}</p>
              </div>
              <button
                type="button"
                class="cf-btn-primary inline-flex h-10 cursor-pointer items-center justify-center gap-2 rounded-xl px-4 text-sm font-semibold transition-all duration-200"
                @click="loadSettings"
              >
                <UIcon name="i-lucide-refresh-cw" class="h-4 w-4" />
                <span>重新加载</span>
              </button>
            </div>
            <transition name="cf-fade" mode="out-in">
              <component v-if="!settingsStore.loading && !loadError" :is="activeComponent" :key="activeTabId" />
            </transition>
          </div>

          <div class="mt-6 flex flex-col-reverse items-stretch justify-between gap-3 sm:flex-row sm:items-center">
            <p class="text-xs text-slate-500 dark:text-slate-400">
              <UIcon name="i-lucide-info" class="mr-1 inline h-3.5 w-3.5 -mt-0.5" />
              所有更改通过<span class="text-amber-600 dark:text-amber-400">「保存全部更改」</span>统一提交，未保存的内容刷新页面将丢失。
            </p>
            <button
              type="button"
              class="cf-btn-primary group inline-flex h-11 cursor-pointer items-center justify-center gap-2 rounded-xl px-5 text-sm font-semibold transition-all duration-200 disabled:cursor-not-allowed"
              :disabled="!hasDirty || saving"
              @click="handleSaveAllChanges"
            >
              <UIcon
                v-if="!saving"
                :name="hasDirty ? 'i-lucide-save' : 'i-lucide-check-check'"
                class="h-4 w-4 transition-transform duration-200 group-hover:-rotate-6"
              />
              <UIcon v-else name="i-lucide-loader-2" class="h-4 w-4 animate-spin" />
              <span>{{ saving ? '保存中…' : hasDirty ? '保存全部更改' : '已是最新' }}</span>
            </button>
          </div>
        </main>
      </div>
    </UContainer>
  </div>
</template>

<style scoped>
/* 装饰层：amber 双 radial 光晕 —— 底色由模板上的 Tailwind dark: 控制 */
.cf-settings-glow {
  background-image:
    radial-gradient(1200px 600px at 80% -10%, rgb(245 158 11 / 0.06), transparent 60%),
    radial-gradient(900px 500px at -10% 30%, rgb(245 158 11 / 0.04), transparent 65%);
}

:where(html.dark) .cf-settings-glow {
  background-image:
    radial-gradient(1200px 600px at 80% -10%, rgb(245 158 11 / 0.10), transparent 60%),
    radial-gradient(900px 500px at -10% 30%, rgb(245 158 11 / 0.06), transparent 65%);
}

.cf-settings-grain {
  background-image:
    linear-gradient(to right, rgb(15 23 42 / 0.04) 1px, transparent 1px),
    linear-gradient(to bottom, rgb(15 23 42 / 0.04) 1px, transparent 1px);
  background-size: 56px 56px;
  mask-image: radial-gradient(ellipse 60% 50% at 50% 30%, #000 30%, transparent 80%);
}

:where(html.dark) .cf-settings-grain {
  background-image:
    linear-gradient(to right, rgb(255 255 255 / 0.03) 1px, transparent 1px),
    linear-gradient(to bottom, rgb(255 255 255 / 0.03) 1px, transparent 1px);
}

.cf-settings-card {
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.05) inset,
    0 20px 40px -30px rgb(245 158 11 / 0.18);
}

:where(html.dark) .cf-settings-card {
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.04) inset,
    0 24px 48px -24px rgb(0 0 0 / 0.6),
    0 0 0 1px rgb(255 255 255 / 0.02);
}

.cf-fade-enter-active,
.cf-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}
.cf-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.cf-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.cf-btn-primary {
  background: #f59e0b;
  color: #1f1300;
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.35) inset,
    0 8px 20px -10px rgb(245 158 11 / 0.55);
}
.cf-btn-primary:hover:not(:disabled) {
  background: #fbbf24;
  transform: translateY(-1px);
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.45) inset,
    0 12px 26px -10px rgb(245 158 11 / 0.65);
}
.cf-btn-primary:active:not(:disabled) {
  transform: translateY(0);
  background: #d97706;
  box-shadow:
    0 1px 0 rgb(255 255 255 / 0.25) inset,
    0 6px 14px -10px rgb(245 158 11 / 0.55);
}
.cf-btn-primary:disabled {
  background: rgb(120 113 108 / 0.5);
  color: rgb(214 211 209);
  box-shadow: none;
}
:global(html.dark) .cf-btn-primary:disabled {
  background: rgb(255 255 255 / 0.06);
  color: rgb(148 163 184);
}
</style>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import { getAdminProfile, updateAdminPassword, updateAdminProfile } from '@/api/admin-profile'
import { useToast } from '@/composables/useToast'
import { useAdminAuthStore } from '@/stores/adminAuth'
import type { AdminProfile, UpdateAdminPasswordRequest, UpdateAdminProfileRequest } from '@/types/admin'

const adminAuthStore = useAdminAuthStore()
const toast = useToast()
const router = useRouter()

const adminInfo = reactive<AdminProfile>({
  id: 0,
  username: '',
  email: '',
  avatar: '',
  nickname: '',
  role: '',
  status: 1,
  lastLoginTime: '',
  lastLoginIp: '',
  createTime: '',
  updateTime: ''
})

// UI State
const isEditing = ref(false)
const isSaving = ref(false)
const isLoading = ref(true)
const loadError = ref('')
const passwordDialogVisible = ref(false)
const isChangingPassword = ref(false)
const passwordError = ref('')

// Form state
const editForm = reactive({
  nickname: '',
  email: ''
})

const passwordForm = reactive<UpdateAdminPasswordRequest>({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const passwordStrengthLevel = computed(() => {
  const password = passwordForm.newPassword
  const hasMinLength = password.length >= 8
  const hasLetter = /[A-Za-z]/.test(password)
  const hasNumber = /\d/.test(password)
  const hasSpecial = /[^A-Za-z\d]/.test(password)
  const score = [hasMinLength, hasLetter, hasNumber, hasSpecial].filter(Boolean).length

  if (score <= 1) {
    return { text: 'WEAK', color: 'text-red-500 dark:text-red-400' }
  }
  if (score <= 3) {
    return { text: 'MEDIUM', color: 'text-amber-500 dark:text-amber-400' }
  }
  return { text: 'STRONG', color: 'text-emerald-500 dark:text-emerald-400' }
})

const canSubmitPassword = computed(() => {
  return Boolean(passwordForm.currentPassword && passwordForm.newPassword && passwordForm.confirmPassword)
})

const resolveErrorMessage = (error: unknown): string => {
  return error instanceof Error ? error.message : '请求失败，请稍后重试'
}

const applyAdminProfile = (profile: AdminProfile) => {
  adminInfo.id = profile.id ?? 0
  adminInfo.username = profile.username ?? ''
  adminInfo.email = profile.email ?? ''
  adminInfo.avatar = profile.avatar ?? ''
  adminInfo.nickname = profile.nickname ?? ''
  adminInfo.role = profile.role ?? ''
  adminInfo.status = profile.status ?? 1
  adminInfo.lastLoginTime = profile.lastLoginTime ?? ''
  adminInfo.lastLoginIp = profile.lastLoginIp ?? ''
  adminInfo.createTime = profile.createTime ?? ''
  adminInfo.updateTime = profile.updateTime ?? ''
}

const syncEditForm = () => {
  editForm.nickname = adminInfo.nickname
  editForm.email = adminInfo.email
}

const loadProfile = async (showErrorToast = true): Promise<boolean> => {
  isLoading.value = true
  loadError.value = ''
  try {
    const profile = await getAdminProfile()
    applyAdminProfile(profile)
    syncEditForm()
    return true
  } catch (error) {
    const message = resolveErrorMessage(error)
    loadError.value = message
    if (showErrorToast) {
      toast.add({
        title: '管理员资料加载失败',
        description: message,
        type: 'error',
      })
    }
    return false
  } finally {
    isLoading.value = false
  }
}

const reloadProfile = () => {
  void loadProfile()
}

const enableEdit = () => {
  syncEditForm()
  isEditing.value = true
}

const cancelEdit = () => {
  syncEditForm()
  isEditing.value = false
}

const resetPasswordForm = () => {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const openPasswordDialog = () => {
  passwordError.value = ''
  resetPasswordForm()
  passwordDialogVisible.value = true
}

const closePasswordDialog = (force = false) => {
  if (!force && isChangingPassword.value) {
    return
  }
  passwordDialogVisible.value = false
  passwordError.value = ''
  resetPasswordForm()
}

const onPasswordDialogVisibleChange = (visible: boolean) => {
  if (visible) {
    passwordDialogVisible.value = true
    return
  }
  closePasswordDialog()
}

const validatePasswordForm = (): string => {
  if (!passwordForm.currentPassword) {
    return '请输入当前密码'
  }
  if (!passwordForm.newPassword) {
    return '请输入新密码'
  }
  if (passwordForm.newPassword.length < 8 || passwordForm.newPassword.length > 64) {
    return '新密码长度需在8-64位之间'
  }
  if (!/[A-Za-z]/.test(passwordForm.newPassword)
    || !/\d/.test(passwordForm.newPassword)
    || !/[^A-Za-z\d]/.test(passwordForm.newPassword)) {
    return '新密码需包含字母、数字和特殊字符'
  }
  if (!passwordForm.confirmPassword) {
    return '请输入确认新密码'
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    return '两次输入的新密码不一致'
  }
  if (passwordForm.currentPassword === passwordForm.newPassword) {
    return '新密码不能与当前密码相同'
  }
  return ''
}

const changePassword = async () => {
  if (isChangingPassword.value) {
    return
  }

  const validationError = validatePasswordForm()
  if (validationError) {
    passwordError.value = validationError
    return
  }

  isChangingPassword.value = true
  passwordError.value = ''
  try {
    await updateAdminPassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword,
    })

    closePasswordDialog(true)
    toast.add({
      title: '管理员密码修改成功',
      description: '请使用新密码重新登录',
      type: 'success',
    })

    try {
      await adminAuthStore.logout()
    } catch {
      adminAuthStore.clear()
    }
    await router.replace({
      name: 'adminLogin',
      query: {
        logoutReset: Date.now().toString(),
      },
    })
  } catch (error) {
    passwordError.value = resolveErrorMessage(error)
  } finally {
    isChangingPassword.value = false
  }
}

const saveProfile = async () => {
  isSaving.value = true
  try {
    const payload: UpdateAdminProfileRequest = {}
    const nickname = editForm.nickname.trim()
    const email = editForm.email.trim()

    if (nickname !== adminInfo.nickname) {
      payload.nickname = nickname
    }
    if (email !== adminInfo.email) {
      payload.email = email
    }

    if (Object.keys(payload).length === 0) {
      isEditing.value = false
      toast.add({
        title: '没有需要保存的变更',
        type: 'info',
      })
      return
    }

    await updateAdminProfile(payload)
    const loaded = await loadProfile(false)
    if (!loaded) {
      if (payload.nickname !== undefined) {
        adminInfo.nickname = payload.nickname
      }
      if (payload.email !== undefined) {
        adminInfo.email = payload.email
      }
      syncEditForm()
    }

    adminAuthStore.setUserSnapshot({
      nickname: adminInfo.nickname,
    })

    isEditing.value = false
    toast.add({
      title: '管理员资料保存成功',
      type: 'success',
    })
  } catch (error) {
    toast.add({
      title: '管理员资料保存失败',
      description: resolveErrorMessage(error),
      type: 'error',
    })
  } finally {
    isSaving.value = false
  }
}

onMounted(() => {
  void loadProfile()
})

</script>

<template>
  <div class="admin-profile-page min-h-screen bg-[#f3f6f9] text-slate-700 dark:bg-[#0e1317] dark:text-slate-300 font-mono transition-colors duration-200">
    <main class="flex-grow max-w-6xl w-full mx-auto px-3 sm:px-6 lg:px-8 py-5 sm:py-8">

      <!-- Terminal Navigation -->
      <div class="mb-6 sm:mb-8 border-b border-[#cbd5e1] dark:border-[#273138] pb-3 sm:pb-4">
        <div class="text-sm text-slate-500 dark:text-gray-400 flex flex-wrap items-center gap-2 break-all">
          <span class="text-[#e95322]">{{ adminInfo.username || 'admin_root' }}@sys</span>:<span class="text-[#3b82f6]">~</span>$ cat /etc/admin/profile.conf
          <span class="cursor-blink"></span>
        </div>
      </div>

      <div v-if="isLoading" class="border border-[#cbd5e1] dark:border-[#273138] bg-white dark:bg-[#151b21] p-6 text-sm text-slate-600 dark:text-gray-300">
        <span class="text-[#e95322]">[SYS]</span> Loading admin profile...
      </div>

      <div v-else-if="loadError" class="border border-[#cbd5e1] dark:border-[#273138] bg-white dark:bg-[#151b21] p-6">
        <p class="text-sm text-red-600 dark:text-red-400">{{ loadError }}</p>
        <button
          type="button"
          @click="reloadProfile"
          class="mt-4 border border-[#e95322] text-[#e95322] px-4 py-2 text-xs uppercase tracking-wider hover:bg-[#e95322] hover:text-white transition-colors cursor-pointer"
        >
          Retry_Load
        </button>
      </div>

      <div v-else class="grid grid-cols-1 xl:grid-cols-[320px_minmax(0,1fr)] gap-5 lg:gap-8">

        <!-- Left Panel: Readonly System Info -->
        <aside class="w-full space-y-5 sm:space-y-6">

          <!-- Avatar & Identity -->
          <div class="border border-[#cbd5e1] dark:border-[#273138] bg-white dark:bg-[#151b21] p-5 sm:p-6 relative group hover:border-[#e95322] transition-colors">
            <div class="absolute top-0 left-0 w-2 h-2 border-t border-l border-[#e95322]"></div>
            <div class="absolute bottom-0 right-0 w-2 h-2 border-b border-r border-[#e95322]"></div>

            <div class="flex flex-col items-center">
              <div class="w-24 h-24 mb-4 border-2 border-[#cbd5e1] dark:border-[#273138] group-hover:border-[#e95322] overflow-hidden bg-[#f8fafc] dark:bg-[#0e1317] flex items-center justify-center p-1 transition-colors">
                <img v-if="adminInfo.avatar" :src="adminInfo.avatar" alt="Avatar" class="w-full h-full object-cover filter grayscale hover:grayscale-0 transition-all duration-300">
                <i v-else class="pi pi-user text-4xl text-slate-500 dark:text-gray-500 group-hover:text-[#e95322] transition-colors"></i>
              </div>

              <h2 class="text-xl text-slate-900 dark:text-white font-bold mb-1 uppercase tracking-wider break-all text-center">{{ adminInfo.username || 'UNKNOWN' }}</h2>
              <p class="text-sm text-slate-500 dark:text-gray-500 mb-4 text-center">"{{ adminInfo.nickname || 'NO_NICKNAME' }}"</p>

              <div class="flex gap-2 w-full flex-wrap sm:flex-nowrap">
                <div class="flex-1 text-center border border-[#3b82f6]/30 bg-[#3b82f6]/10 text-[#3b82f6] py-1 text-xs uppercase tracking-wider">
                  <i class="pi pi-shield mr-1"></i> {{ adminInfo.role }}
                </div>
                <div :class="adminInfo.status === 1 ? 'border-[#e95322]/30 bg-[#feece6] dark:bg-[#3e1a10] text-[#e95322]' : 'border-slate-400 dark:border-gray-600 bg-slate-200 dark:bg-gray-800 text-slate-500 dark:text-gray-400'"
                     class="flex-1 text-center border py-1 text-xs uppercase tracking-wider">
                  <span v-if="adminInfo.status === 1">[OK] NORMAL</span>
                  <span v-else>[ERR] DISABLED</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Sys Info Log -->
          <div class="border border-[#cbd5e1] dark:border-[#273138] bg-white dark:bg-[#0e1317] p-4 sm:p-5">
            <h3 class="text-slate-500 dark:text-gray-500 text-xs uppercase tracking-widest mb-3 border-b border-[#cbd5e1] dark:border-[#273138] pb-1">>> SYS_INFO_LOG</h3>
            <div class="space-y-3 text-xs">
              <div>
                <span class="text-slate-500 dark:text-gray-500 block mb-0.5">UID_REF:</span>
                <span class="text-[#e95322]">0x{{ String(adminInfo.id).padStart(6, '0') }}</span>
              </div>
              <div>
                <span class="text-slate-500 dark:text-gray-500 block mb-0.5">LAST_LOGIN_TIME:</span>
                <span class="text-slate-700 dark:text-gray-300">{{ adminInfo.lastLoginTime || 'NULL' }}</span>
              </div>
              <div>
                <span class="text-slate-500 dark:text-gray-500 block mb-0.5">LAST_LOGIN_IP:</span>
                <span class="text-[#3b82f6]">{{ adminInfo.lastLoginIp || '0.0.0.0' }}</span>
              </div>
              <div class="pt-2 border-t border-[#cbd5e1] dark:border-[#273138] border-dashed">
                <span class="text-slate-500 dark:text-gray-500 block mb-0.5">CREATED_AT:</span>
                <span class="text-slate-600 dark:text-gray-400">{{ adminInfo.createTime || 'NULL' }}</span>
              </div>
              <div>
                <span class="text-slate-500 dark:text-gray-500 block mb-0.5">UPDATED_AT:</span>
                <span class="text-slate-600 dark:text-gray-400">{{ adminInfo.updateTime || 'NULL' }}</span>
              </div>
            </div>
          </div>

        </aside>

        <!-- Right Panel: Editable Config -->
        <div class="min-w-0">
          <div class="border border-[#cbd5e1] dark:border-[#273138] bg-white dark:bg-[#0e1317] relative">

            <!-- Header -->
            <div class="bg-[#eef2f6] dark:bg-[#151b21] border-b border-[#cbd5e1] dark:border-[#273138] px-4 py-3 flex items-center justify-between gap-3">
              <h3 class="text-slate-600 dark:text-gray-400 text-sm uppercase tracking-widest min-w-0 truncate">
                <i class="pi pi-cog mr-2 text-[#e95322]"></i>>> USER_CONFIG
              </h3>
              <span class="text-xs text-[#e95322] animate-pulse" v-if="isEditing">--INSERT--</span>
              <span class="text-xs text-slate-500 dark:text-gray-600" v-else>--READ_ONLY--</span>
            </div>

            <!-- Form Content -->
            <form @submit.prevent="saveProfile" class="p-4 sm:p-6 space-y-6">

              <div class="grid grid-cols-1 md:grid-cols-2 gap-5 sm:gap-6">
                <!-- Username -->
                <div>
                  <label class="block text-xs text-slate-500 dark:text-gray-500 uppercase tracking-widest mb-2">USERNAME_ID</label>
                  <div class="relative flex items-center bg-[#f8fafc] dark:bg-[#151b21] border border-[#cbd5e1] dark:border-[#273138] opacity-80">
                    <span class="pl-3 text-slate-500 dark:text-gray-600 font-bold">#</span>
                    <input type="text" :value="adminInfo.username" disabled class="w-full bg-transparent text-slate-600 dark:text-gray-400 p-2 outline-none font-mono text-sm cursor-not-allowed">
                  </div>
                  <p class="mt-1 text-[10px] text-slate-500 dark:text-gray-600">Username is unique and cannot be modified.</p>
                </div>

                <!-- Role -->
                <div>
                  <label class="block text-xs text-slate-500 dark:text-gray-500 uppercase tracking-widest mb-2">ROLE_LEVEL</label>
                  <div class="relative flex items-center bg-[#f8fafc] dark:bg-[#151b21] border border-[#cbd5e1] dark:border-[#273138] opacity-80">
                    <span class="pl-3 text-slate-500 dark:text-gray-600 font-bold">$</span>
                    <input type="text" :value="adminInfo.role" disabled class="w-full bg-transparent text-slate-600 dark:text-gray-400 p-2 outline-none font-mono text-sm cursor-not-allowed">
                  </div>
                </div>

                <!-- Nickname -->
                <div>
                  <label class="block text-xs uppercase tracking-widest mb-2" :class="isEditing ? 'text-[#e95322]' : 'text-slate-500 dark:text-gray-500'">NICKNAME</label>
                  <div class="relative flex items-center border transition-colors bg-white dark:bg-black/80" :class="isEditing ? 'border-[#e95322]' : 'border-[#cbd5e1] dark:border-[#273138]'">
                    <span class="pl-3 font-bold" :class="isEditing ? 'text-[#e95322]' : 'text-slate-500 dark:text-gray-600'">></span>
                    <input type="text" v-model="editForm.nickname" :disabled="!isEditing" placeholder="Enter nickname..." class="w-full bg-transparent text-slate-800 dark:text-gray-200 p-2 outline-none font-mono text-sm disabled:text-slate-500 dark:disabled:text-gray-500">
                  </div>
                </div>

                <!-- Email -->
                <div>
                  <label class="block text-xs uppercase tracking-widest mb-2" :class="isEditing ? 'text-[#e95322]' : 'text-slate-500 dark:text-gray-500'">EMAIL_ADDRESS <span v-if="isEditing" class="text-red-500">*</span></label>
                  <div class="relative flex items-center border transition-colors bg-white dark:bg-black/80" :class="isEditing ? 'border-[#e95322]' : 'border-[#cbd5e1] dark:border-[#273138]'">
                    <span class="pl-3 font-bold" :class="isEditing ? 'text-[#e95322]' : 'text-slate-500 dark:text-gray-600'">></span>
                    <input type="email" v-model="editForm.email" :disabled="!isEditing" required placeholder="admin@example.com" class="w-full bg-transparent text-slate-800 dark:text-gray-200 p-2 outline-none font-mono text-sm disabled:text-slate-500 dark:disabled:text-gray-500">
                  </div>
                  <p class="mt-1 text-[10px] text-slate-500 dark:text-gray-600">Must be unique (uk_email).</p>
                </div>
              </div>

               <!-- Password Alert -->
               <div class="mt-8 border border-[#cbd5e1] dark:border-[#273138] border-dashed p-4 bg-yellow-500/10 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div class="min-w-0">
                  <h4 class="text-sm text-slate-700 dark:text-gray-300 mb-1">SECURITY_KEYS (PASSWORD)</h4>
                  <p class="text-xs text-slate-500 dark:text-gray-600">BCrypt hashed. Requires specific permissions to alter.</p>
                </div>
                <button type="button" @click="openPasswordDialog" class="w-full sm:w-auto border border-slate-400 dark:border-gray-600 text-slate-600 dark:text-gray-400 hover:text-slate-900 dark:hover:text-white hover:border-slate-700 dark:hover:border-white px-3 py-2 sm:py-1 text-xs uppercase transition-colors cursor-pointer">
                  --reset-passwd
                </button>
              </div>

              <!-- Action Buttons -->
              <div class="pt-6 border-t border-[#cbd5e1] dark:border-[#273138] flex flex-col-reverse sm:flex-row sm:justify-end gap-3 mt-6">
                <template v-if="!isEditing">
                  <button type="button" @click="enableEdit" class="w-full sm:w-auto bg-transparent dark:bg-[#0e1317] border border-[#e95322] text-[#e95322] px-6 py-2 text-sm font-bold hover:bg-[#e95322] hover:text-white transition-colors uppercase cursor-pointer">
                    <i class="pi pi-file-edit mr-2"></i> Enable_Edit
                  </button>
                </template>
                <template v-else>
                  <button type="button" @click="cancelEdit" class="w-full sm:w-auto bg-transparent border border-slate-400 dark:border-gray-600 text-slate-600 dark:text-gray-400 px-6 py-2 text-sm font-bold hover:border-slate-700 dark:hover:border-white hover:text-slate-900 dark:hover:text-white transition-colors uppercase cursor-pointer">
                    Abort
                  </button>
                  <button type="submit" :disabled="isSaving" class="w-full sm:w-auto bg-[#e95322] border border-[#e95322] text-white px-6 py-2 text-sm font-bold hover:opacity-90 disabled:opacity-50 transition-colors uppercase flex items-center justify-center cursor-pointer">
                    <template v-if="!isSaving">
                      <i class="pi pi-save mr-2"></i> Write_Config
                    </template>
                    <template v-else>
                      <i class="pi pi-spin pi-spinner mr-2"></i> Writing...
                    </template>
                  </button>
                </template>
              </div>

            </form>
          </div>
        </div>

      </div>
    </main>

    <Dialog
      :visible="passwordDialogVisible"
      modal
      :draggable="false"
      :closable="!isChangingPassword"
      :dismissableMask="!isChangingPassword"
      :pt="{
        mask: { class: 'bg-black/45 backdrop-blur-[1px] z-[120]' },
        root: {
          class:
            'w-[min(94vw,560px)] rounded-lg border border-[#cbd5e1] dark:border-[#273138] bg-white dark:bg-[#0e1317] shadow-[0_18px_40px_rgba(15,23,42,0.28)] dark:shadow-[0_24px_48px_rgba(2,6,23,0.68)] overflow-hidden'
        },
        header: {
          class:
            'border-b border-[#cbd5e1] dark:border-[#273138] bg-[#eef2f6] dark:bg-[#151b21] px-4 sm:px-5 py-3 flex items-center justify-between'
        },
        content: { class: 'px-4 sm:px-5 py-4 text-sm text-slate-700 dark:text-gray-300' },
        footer: {
          class:
            'px-4 sm:px-5 py-3 border-t border-[#cbd5e1] dark:border-[#273138] bg-[#f8fafc] dark:bg-[#151b21] flex flex-col-reverse sm:flex-row sm:justify-end gap-3'
        }
      }"
      @update:visible="onPasswordDialogVisibleChange"
    >
      <template #header>
        <div class="flex items-center gap-2 min-w-0">
          <i class="pi pi-lock text-[#e95322]"></i>
          <span class="text-sm sm:text-base font-semibold text-slate-800 dark:text-gray-100 uppercase tracking-wider truncate">Modify_Admin_Password</span>
        </div>
      </template>

      <form id="admin-password-form" class="space-y-4" @submit.prevent="changePassword">
        <div v-if="passwordError" class="rounded border border-red-300 dark:border-red-800 bg-red-50 dark:bg-red-950/40 px-3 py-2 text-xs text-red-600 dark:text-red-300">
          {{ passwordError }}
        </div>

        <div>
          <label class="block text-xs uppercase tracking-widest text-slate-500 dark:text-gray-500 mb-2">CURRENT_PASSWORD</label>
          <input
            v-model="passwordForm.currentPassword"
            type="password"
            autocomplete="current-password"
            class="w-full bg-transparent border border-[#cbd5e1] dark:border-[#273138] text-slate-800 dark:text-gray-200 px-3 py-2 outline-none text-sm focus:border-[#e95322] transition-colors"
            placeholder="Enter current password"
          >
        </div>

        <div>
          <label class="block text-xs uppercase tracking-widest text-slate-500 dark:text-gray-500 mb-2">NEW_PASSWORD</label>
          <input
            v-model="passwordForm.newPassword"
            type="password"
            autocomplete="new-password"
            class="w-full bg-transparent border border-[#cbd5e1] dark:border-[#273138] text-slate-800 dark:text-gray-200 px-3 py-2 outline-none text-sm focus:border-[#e95322] transition-colors"
            placeholder="8-64 chars with letter, number and symbol"
          >
          <div class="mt-2 flex items-center justify-between text-[11px] text-slate-500 dark:text-gray-500">
            <span>PASSWORD_STRENGTH</span>
            <span :class="passwordStrengthLevel.color">{{ passwordStrengthLevel.text }}</span>
          </div>
        </div>

        <div>
          <label class="block text-xs uppercase tracking-widest text-slate-500 dark:text-gray-500 mb-2">CONFIRM_NEW_PASSWORD</label>
          <input
            v-model="passwordForm.confirmPassword"
            type="password"
            autocomplete="new-password"
            class="w-full bg-transparent border border-[#cbd5e1] dark:border-[#273138] text-slate-800 dark:text-gray-200 px-3 py-2 outline-none text-sm focus:border-[#e95322] transition-colors"
            placeholder="Re-enter new password"
          >
        </div>
      </form>

      <template #footer>
        <Button
          type="button"
          :disabled="isChangingPassword"
          @click="closePasswordDialog()"
          class="w-full sm:w-auto cursor-pointer border border-slate-400 dark:border-gray-600 text-slate-600 dark:text-gray-300 px-4 py-2 text-sm font-semibold hover:border-slate-700 dark:hover:border-white hover:text-slate-900 dark:hover:text-white transition-colors"
        >
          取消
        </Button>
        <Button
          type="button"
          :disabled="!canSubmitPassword || isChangingPassword"
          @click="changePassword"
          class="w-full sm:w-auto cursor-pointer bg-[#e95322] border border-[#e95322] text-white px-4 py-2 text-sm font-semibold hover:opacity-90 disabled:opacity-50 transition-colors"
        >
          <span v-if="isChangingPassword">提交中...</span>
          <span v-else>确认修改</span>
        </Button>
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.admin-profile-page {
  --profile-scroll-track: #e2e8f0;
  --profile-scroll-thumb: #94a3b8;
  --profile-scroll-border: #f8fafc;
}

:global(.dark) .admin-profile-page {
  --profile-scroll-track: #0e1317;
  --profile-scroll-thumb: #273138;
  --profile-scroll-border: #0e1317;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
.cursor-blink {
  animation: blink 1s step-end infinite;
  display: inline-block;
  width: 8px;
  height: 1em;
  background-color: #e95322;
  vertical-align: middle;
  margin-left: 4px;
}

/* Custom Scrollbar for Terminal aesthetics */
:deep(::-webkit-scrollbar) {
  width: 10px;
  height: 10px;
}
:deep(::-webkit-scrollbar-track) {
  background: var(--profile-scroll-track);
  border-left: 1px solid var(--profile-scroll-thumb);
}
:deep(::-webkit-scrollbar-thumb) {
  background: var(--profile-scroll-thumb);
  border: 1px solid var(--profile-scroll-border);
}
:deep(::-webkit-scrollbar-thumb:hover) {
  background: #e95322;
}
/* Selection */
:deep(::selection) {
  background-color: #e95322;
  color: #ffffff;
}
</style>


<script setup lang="ts">
import { ref, reactive } from 'vue'

// Mock data based on t_admin
const adminInfo = reactive({
  id: 1,
  username: 'admin_root',
  email: 'root@glama.sys',
  avatar: '',
  nickname: 'System Administrator',
  role: 'admin',
  status: 1,
  last_login_time: '2026-03-31 20:15:42',
  last_login_ip: '192.168.1.254',
  create_time: '2025-01-01 00:00:00',
  update_time: '2026-03-25 14:20:11'
})

// UI State
const isEditing = ref(false)
const isSaving = ref(false)

// Form state
const editForm = reactive({
  nickname: '',
  email: '',
  avatar: ''
})

const enableEdit = () => {
  editForm.nickname = adminInfo.nickname
  editForm.email = adminInfo.email
  editForm.avatar = adminInfo.avatar
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
}

const saveProfile = async () => {
  isSaving.value = true

  // Simulate network request
  await new Promise(resolve => setTimeout(resolve, 1200))

  adminInfo.nickname = editForm.nickname
  adminInfo.email = editForm.email
  adminInfo.avatar = editForm.avatar

  const now = new Date()
  adminInfo.update_time = now.toISOString().replace('T', ' ').substring(0, 19)

  isSaving.value = false
  isEditing.value = false

  // Replace with toast later
  console.log(`[SYS] Config updated for UID: ${adminInfo.id}`)
}

</script>

<template>
  <div class="admin-profile-page min-h-screen bg-[#f3f6f9] text-slate-700 dark:bg-[#0e1317] dark:text-slate-300 font-mono transition-colors duration-200">
    <main class="flex-grow max-w-6xl w-full mx-auto px-3 sm:px-6 lg:px-8 py-5 sm:py-8">

      <!-- Terminal Navigation -->
      <div class="mb-6 sm:mb-8 border-b border-[#cbd5e1] dark:border-[#273138] pb-3 sm:pb-4">
        <div class="text-sm text-slate-500 dark:text-gray-400 flex flex-wrap items-center gap-2 break-all">
          <span class="text-[#e95322]">{{ adminInfo.username }}@sys</span>:<span class="text-[#3b82f6]">~</span>$ cat /etc/admin/profile.conf
          <span class="cursor-blink"></span>
        </div>
      </div>

      <div class="grid grid-cols-1 xl:grid-cols-[320px_minmax(0,1fr)] gap-5 lg:gap-8">

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

              <h2 class="text-xl text-slate-900 dark:text-white font-bold mb-1 uppercase tracking-wider break-all text-center">{{ adminInfo.username }}</h2>
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
                <span class="text-slate-700 dark:text-gray-300">{{ adminInfo.last_login_time || 'NULL' }}</span>
              </div>
              <div>
                <span class="text-slate-500 dark:text-gray-500 block mb-0.5">LAST_LOGIN_IP:</span>
                <span class="text-[#3b82f6]">{{ adminInfo.last_login_ip || '0.0.0.0' }}</span>
              </div>
              <div class="pt-2 border-t border-[#cbd5e1] dark:border-[#273138] border-dashed">
                <span class="text-slate-500 dark:text-gray-500 block mb-0.5">CREATED_AT:</span>
                <span class="text-slate-600 dark:text-gray-400">{{ adminInfo.create_time }}</span>
              </div>
              <div>
                <span class="text-slate-500 dark:text-gray-500 block mb-0.5">UPDATED_AT:</span>
                <span class="text-slate-600 dark:text-gray-400">{{ adminInfo.update_time }}</span>
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
               <div class="mt-8 border border-[#cbd5e1] dark:border-[#273138] border-dashed p-4 bg-[#f8fafc] dark:bg-[#151b21] flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div class="min-w-0">
                  <h4 class="text-sm text-slate-700 dark:text-gray-300 mb-1">SECURITY_KEYS (PASSWORD)</h4>
                  <p class="text-xs text-slate-500 dark:text-gray-600">BCrypt hashed. Requires specific permissions to alter.</p>
                </div>
                <button type="button" class="w-full sm:w-auto border border-slate-400 dark:border-gray-600 text-slate-600 dark:text-gray-400 hover:text-slate-900 dark:hover:text-white hover:border-slate-700 dark:hover:border-white px-3 py-2 sm:py-1 text-xs uppercase transition-colors cursor-pointer">
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


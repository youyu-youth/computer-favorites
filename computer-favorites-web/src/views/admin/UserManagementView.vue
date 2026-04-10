<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import Dialog from 'primevue/dialog'
import UsersBreadcrumbs from '@/components/admin/users/UsersBreadcrumbs.vue'
import UsersToolbar from '@/components/admin/users/UsersToolbar.vue'
import UsersTable from '@/components/admin/users/UsersTable.vue'
import AdminPagination from '@/components/admin/common/AdminPagination.vue'
import UserEditorModal from '@/components/admin/users/UserEditorModal.vue'
import UserDetailModal from '@/components/admin/users/UserDetailModal.vue'
import { useAdminNavStore } from '@/stores/adminNav'
import type { AdminUserItem, AdminUserFormModel, AdminUserStats } from '@/types/user'
import {
  getAdminUserPage,
  getAdminUserStats,
  updateAdminUserStatus,
  resetAdminUserPassword,
  kickAdminUserSessions,
} from '@/api/admin-user'

const adminNavStore = useAdminNavStore()

/* ---- 列表状态 ---- */
const pagedUsers = ref<AdminUserItem[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const selectedUserIds = ref<number[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)
const totalPagesCount = ref(1)

/* ---- 统计数据 ---- */
const statsData = ref<AdminUserStats>({ total: 0, normal: 0, disabled: 0, emailVerified: 0 })

const stats = computed(() => statsData.value)

/* ---- 编辑器状态 ---- */
const editorOpen = ref(false)
const editorMode = ref<'create' | 'edit'>('create')
const editorSubmitting = ref(false)
const editingUser = ref<AdminUserItem | null>(null)

const EMPTY_FORM: AdminUserFormModel = {
  username: '',
  email: '',
  phone: '',
  nickname: '',
  password: '',
  status: 1,
}

const formModel = reactive<AdminUserFormModel>({ ...EMPTY_FORM })
const formErrors = reactive<Record<string, string>>({})

/* ---- 详情弹窗 ---- */
const detailOpen = ref(false)
const detailUser = ref<AdminUserItem | null>(null)

/* ---- 踢下线确认 ---- */
const kickDialogOpen = ref(false)
const kickingUser = ref<AdminUserItem | null>(null)
const kickSubmitting = ref(false)

/* ---- 分页计算 ---- */
const totalPages = computed(() => totalPagesCount.value)

const visiblePages = computed<Array<number | string>>(() => {
  const total = totalPages.value
  const cur = currentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  const pages: Array<number | string> = [1]
  if (cur > 3) pages.push('...')
  for (let i = Math.max(2, cur - 1); i <= Math.min(total - 1, cur + 1); i++) pages.push(i)
  if (cur < total - 2) pages.push('...')
  pages.push(total)
  return pages
})

/* ---- 数据加载 ---- */
async function loadUsers() {
  loading.value = true
  try {
    const result = await getAdminUserPage({
      keyword: searchKeyword.value || undefined,
      status: statusFilter.value !== '' ? (Number(statusFilter.value) as 0 | 1) : undefined,
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    })
    pagedUsers.value = result.records
    totalItems.value = Number(result.total)
    totalPagesCount.value = Number(result.totalPages) || 1
  } catch (e) {
    console.error('加载用户列表失败', e)
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    statsData.value = await getAdminUserStats()
  } catch (e) {
    console.error('加载用户统计失败', e)
  }
}

/* ---- 搜索/筛选触发重新加载 ---- */
watch([searchKeyword, statusFilter], () => {
  currentPage.value = 1
  loadUsers()
})

watch(currentPage, () => {
  loadUsers()
})

/* ---- 分页操作 ---- */
const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--
}
const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++
}
const goToPage = (page: number | string) => {
  const p = Number(page)
  if (!Number.isNaN(p) && p >= 1 && p <= totalPages.value) {
    currentPage.value = p
  }
}

/* ---- 搜索/筛选 ---- */
const setSearchKeyword = (kw: string) => {
  searchKeyword.value = kw
}
const setStatusFilter = (val: string) => {
  statusFilter.value = val
}

/* ---- 刷新 ---- */
const refreshData = () => {
  loadUsers()
  loadStats()
}

/* ---- 选择 ---- */
const setSelectedIds = (ids: number[]) => {
  selectedUserIds.value = ids
}

/* ---- 查看详情 ---- */
const openDetail = (user: AdminUserItem) => {
  detailUser.value = user
  detailOpen.value = true
}

/* ---- 编辑器 ---- */
const openCreateEditor = () => {
  editorMode.value = 'create'
  editingUser.value = null
  Object.assign(formModel, { ...EMPTY_FORM })
  Object.keys(formErrors).forEach((k) => delete formErrors[k])
  editorOpen.value = true
}

const openEditEditor = (user: AdminUserItem) => {
  editorMode.value = 'edit'
  editingUser.value = user
  Object.assign(formModel, {
    username: user.username,
    email: user.email,
    phone: user.phone || '',
    nickname: user.nickname || '',
    password: '',
    status: user.status,
  })
  Object.keys(formErrors).forEach((k) => delete formErrors[k])
  editorOpen.value = true
  if (detailOpen.value) detailOpen.value = false
}

const closeEditor = () => {
  editorOpen.value = false
}

const validateForm = (): boolean => {
  Object.keys(formErrors).forEach((k) => delete formErrors[k])
  if (editorMode.value === 'create') {
    if (!formModel.username.trim()) formErrors.username = '用户名不能为空'
    if (!formModel.email.trim()) formErrors.email = '邮箱不能为空'
    if (!formModel.password.trim()) formErrors.password = '密码不能为空'
  } else {
    if (formModel.password.trim() && formModel.password.trim().length < 6) {
      formErrors.password = '密码长度不能少于6位'
    }
  }
  return Object.keys(formErrors).length === 0
}

const saveUser = async () => {
  if (!validateForm()) return
  editorSubmitting.value = true

  try {
    if (editorMode.value === 'create') {
      // 创建用户暂未开放
      alert('创建用户功能暂未开放，请联系系统管理员')
      editorOpen.value = false
      return
    }

    if (editorMode.value === 'edit' && editingUser.value) {
      const userId = editingUser.value.id

      // 若填写了新密码则重置密码
      if (formModel.password.trim()) {
        await resetAdminUserPassword(userId, formModel.password.trim())
      }

      // 若状态有变化则更新状态
      if (formModel.status !== editingUser.value.status) {
        await updateAdminUserStatus(userId, formModel.status)
      }

      editorOpen.value = false
      loadUsers()
      loadStats()
    }
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '操作失败'
    formErrors.password = msg
  } finally {
    editorSubmitting.value = false
  }
}

/* ---- 切换状态 ---- */
const toggleUserStatus = async (user: AdminUserItem) => {
  const newStatus: 0 | 1 = user.status === 1 ? 0 : 1
  try {
    await updateAdminUserStatus(user.id, newStatus)
    loadUsers()
    loadStats()
  } catch (e) {
    console.error('切换用户状态失败', e)
  }
}

/* ---- 踢下线（替代删除操作） ---- */
const requestDelete = (user: AdminUserItem) => {
  kickingUser.value = user
  kickDialogOpen.value = true
}

const closeKickDialog = () => {
  if (!kickSubmitting.value) {
    kickDialogOpen.value = false
    kickingUser.value = null
  }
}

const confirmKick = async () => {
  if (!kickingUser.value || kickSubmitting.value) return
  kickSubmitting.value = true
  try {
    await kickAdminUserSessions(kickingUser.value.id)
    kickDialogOpen.value = false
    kickingUser.value = null
    loadUsers()
  } catch (e) {
    console.error('踢出会话失败', e)
  } finally {
    kickSubmitting.value = false
  }
}

/* ---- 更新表单 ---- */
const updateFormModel = (nextValue: AdminUserFormModel) => {
  Object.assign(formModel, nextValue)
}

const kickDialogPt = {
  mask: { class: 'bg-black/45 backdrop-blur-[1px] z-[120]' },
  root: {
    class:
      'w-[min(92vw,440px)] rounded-lg border border-gray-200 dark:border-dark-border bg-white dark:bg-dark-card shadow-[0_16px_36px_rgba(15,23,42,0.28)] dark:shadow-[0_22px_44px_rgba(2,6,23,0.62)] overflow-hidden',
  },
  header: { class: 'border-b border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg px-5 py-4' },
  content: { class: 'px-5 py-4' },
  footer: {
    class: 'px-5 py-4 border-t border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-bg flex flex-col-reverse gap-2 sm:flex-row sm:justify-end',
  },
} as const

onMounted(() => {
  adminNavStore.setActiveMenu('users')
  loadUsers()
  loadStats()
})
</script>

<template>
  <main class="flex-grow w-full px-4 py-6 sm:px-6 lg:px-8">
    <div class="mx-auto min-w-0 max-w-[1320px]">
      <UsersBreadcrumbs />

      <!-- 统计卡片 -->
      <div class="mb-6 grid grid-cols-2 gap-4 sm:grid-cols-4">
        <div
          class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm dark:border-dark-border dark:bg-dark-card"
        >
          <div class="flex items-center gap-3">
            <div
              class="flex h-11 w-11 items-center justify-center rounded-full bg-blue-50 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400"
            >
              <i class="fas fa-users text-lg"></i>
            </div>
            <div>
              <p class="text-xs font-medium text-gray-500 dark:text-gray-400">总用户数</p>
              <h3 class="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
                {{ stats.total }}
              </h3>
            </div>
          </div>
        </div>

        <div
          class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm dark:border-dark-border dark:bg-dark-card"
        >
          <div class="flex items-center gap-3">
            <div
              class="flex h-11 w-11 items-center justify-center rounded-full bg-emerald-50 text-emerald-600 dark:bg-emerald-900/20 dark:text-emerald-400"
            >
              <i class="fas fa-user-check text-lg"></i>
            </div>
            <div>
              <p class="text-xs font-medium text-gray-500 dark:text-gray-400">正常账号</p>
              <h3 class="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
                {{ stats.normal }}
              </h3>
            </div>
          </div>
        </div>

        <div
          class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm dark:border-dark-border dark:bg-dark-card"
        >
          <div class="flex items-center gap-3">
            <div
              class="flex h-11 w-11 items-center justify-center rounded-full bg-red-50 text-red-600 dark:bg-red-900/20 dark:text-red-400"
            >
              <i class="fas fa-user-slash text-lg"></i>
            </div>
            <div>
              <p class="text-xs font-medium text-gray-500 dark:text-gray-400">已禁用</p>
              <h3 class="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
                {{ stats.disabled }}
              </h3>
            </div>
          </div>
        </div>

        <div
          class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm dark:border-dark-border dark:bg-dark-card"
        >
          <div class="flex items-center gap-3">
            <div
              class="flex h-11 w-11 items-center justify-center rounded-full bg-green-50 text-green-600 dark:bg-green-900/20 dark:text-green-400"
            >
              <i class="fas fa-envelope-circle-check text-lg"></i>
            </div>
            <div>
              <p class="text-xs font-medium text-gray-500 dark:text-gray-400">邮箱已验证</p>
              <h3 class="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
                {{ stats.emailVerified }}
              </h3>
            </div>
          </div>
        </div>
      </div>

      <!-- 主内容区 -->
      <section class="rounded-xl border border-gray-200 p-4 dark:border-dark-border sm:p-5">
        <UsersToolbar
          :searchKeyword="searchKeyword"
          :statusFilter="statusFilter"
          :selectedCount="selectedUserIds.length"
          @update:searchKeyword="setSearchKeyword"
          @update:statusFilter="setStatusFilter"
          @refresh="refreshData"
          @create="openCreateEditor"
        />

        <UsersTable
          :rows="pagedUsers"
          :selectedRowIds="selectedUserIds"
          :loading="loading"
          @selection-change="setSelectedIds"
          @view="openDetail"
          @edit="openEditEditor"
          @toggle-status="toggleUserStatus"
          @delete="requestDelete"
        />

        <AdminPagination
          v-if="totalItems > 0"
          :currentPage="currentPage"
          :totalPages="totalPages"
          :visiblePages="visiblePages"
          :total="totalItems"
          :pageSize="pageSize"
          @prev="prevPage"
          @next="nextPage"
          @goto="goToPage"
        />
      </section>
    </div>

    <!-- 创建/编辑弹窗 -->
    <UserEditorModal
      :open="editorOpen"
      :mode="editorMode"
      :modelValue="formModel"
      :errors="formErrors"
      :submitting="editorSubmitting"
      @update:open="(val) => { if (!val) closeEditor() }"
      @update:modelValue="updateFormModel"
      @submit="saveUser"
    />

    <!-- 详情弹窗 -->
    <UserDetailModal
      :open="detailOpen"
      :user="detailUser"
      @update:open="(val) => { detailOpen = val }"
      @edit="openEditEditor"
    />

    <!-- 踢下线确认 -->
    <Dialog
      :visible="kickDialogOpen"
      modal
      :dismissableMask="true"
      :draggable="false"
      :pt="kickDialogPt"
      @update:visible="(val) => { if (!val) closeKickDialog() }"
    >
      <template #header>
        <div class="space-y-1">
          <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">确认踢出会话</h3>
          <p class="mt-1 text-sm text-gray-600 dark:text-gray-300">
            此操作将强制该用户下线，用户需重新登录才能继续使用。
          </p>
        </div>
      </template>

      <div
        class="rounded-md border border-amber-200 bg-amber-50/70 px-3 py-2 text-sm text-amber-700 dark:border-amber-900/40 dark:bg-amber-900/15 dark:text-amber-200"
      >
        即将踢出用户：
        <span class="font-semibold">{{ kickingUser?.nickname || kickingUser?.username || '-' }}</span>
        <span class="ml-1 text-xs opacity-70">(@{{ kickingUser?.username }})</span>
      </div>

      <template #footer>
        <button
          type="button"
          class="inline-flex h-9 cursor-pointer items-center justify-center rounded-md border border-gray-300 bg-white px-4 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg"
          :disabled="kickSubmitting"
          @click="closeKickDialog"
        >
          取消
        </button>
        <button
          type="button"
          class="inline-flex h-9 cursor-pointer items-center justify-center rounded-md border border-transparent bg-amber-600 px-4 text-sm font-medium text-white shadow-sm hover:bg-amber-700 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:bg-amber-500 dark:hover:bg-amber-600 dark:focus:ring-offset-gray-900"
          :disabled="kickSubmitting"
          @click="confirmKick"
        >
          <i v-if="kickSubmitting" class="fas fa-spinner fa-spin mr-2"></i>
          <span>确认踢出</span>
        </button>
      </template>
    </Dialog>
  </main>
</template>

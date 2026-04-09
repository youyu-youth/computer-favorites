<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import Dialog from 'primevue/dialog'
import UsersBreadcrumbs from '@/components/admin/users/UsersBreadcrumbs.vue'
import UsersToolbar from '@/components/admin/users/UsersToolbar.vue'
import UsersTable from '@/components/admin/users/UsersTable.vue'
import AdminPagination from '@/components/admin/common/AdminPagination.vue'
import UserEditorModal from '@/components/admin/users/UserEditorModal.vue'
import UserDetailModal from '@/components/admin/users/UserDetailModal.vue'
import { useAdminNavStore } from '@/stores/adminNav'
import type { AdminUserItem, AdminUserFormModel } from '@/types/user'

const adminNavStore = useAdminNavStore()

/* ---- 假数据 ---- */
const MOCK_USERS: AdminUserItem[] = [
  {
    id: 1,
    username: 'admin_zhang',
    email: 'zhang@example.com',
    phone: '13800138001',
    nickname: '张三',
    avatar: null,
    status: 1,
    emailVerified: 1,
    phoneVerified: 1,
    lastLoginTime: '2025-04-08 09:32:10',
    lastLoginIp: '192.168.1.100',
    createTime: '2024-01-10 08:00:00',
    updateTime: '2025-04-08 09:32:10',
    deleted: 0,
  },
  {
    id: 2,
    username: 'li_si_dev',
    email: 'lisi@tech.io',
    phone: null,
    nickname: '李四',
    avatar: null,
    status: 1,
    emailVerified: 1,
    phoneVerified: 0,
    lastLoginTime: '2025-04-07 15:20:00',
    lastLoginIp: '10.0.0.55',
    createTime: '2024-02-14 12:30:00',
    updateTime: '2025-04-07 15:20:00',
    deleted: 0,
  },
  {
    id: 3,
    username: 'wang_wu',
    email: 'wangwu@mail.com',
    phone: '13900139003',
    nickname: '王五',
    avatar: null,
    status: 0,
    emailVerified: 0,
    phoneVerified: 0,
    lastLoginTime: '2025-03-01 11:00:00',
    lastLoginIp: '172.16.0.22',
    createTime: '2024-03-20 09:15:00',
    updateTime: '2025-03-01 11:00:00',
    deleted: 0,
  },
  {
    id: 4,
    username: 'zhao_liu',
    email: 'zhaoliu@cs.edu',
    phone: '13700137004',
    nickname: '赵六',
    avatar: null,
    status: 1,
    emailVerified: 1,
    phoneVerified: 1,
    lastLoginTime: '2025-04-09 08:50:00',
    lastLoginIp: '192.168.0.10',
    createTime: '2024-05-01 07:00:00',
    updateTime: '2025-04-09 08:50:00',
    deleted: 0,
  },
  {
    id: 5,
    username: 'sun_qi_cs',
    email: 'sunqi@code.cn',
    phone: null,
    nickname: '孙七',
    avatar: null,
    status: 1,
    emailVerified: 1,
    phoneVerified: 0,
    lastLoginTime: '2025-04-06 20:10:00',
    lastLoginIp: '10.10.1.88',
    createTime: '2024-06-15 16:00:00',
    updateTime: '2025-04-06 20:10:00',
    deleted: 0,
  },
  {
    id: 6,
    username: 'zhou_ba',
    email: 'zhouba@dev.org',
    phone: '15600156006',
    nickname: null,
    avatar: null,
    status: 0,
    emailVerified: 1,
    phoneVerified: 0,
    lastLoginTime: '2025-02-20 14:00:00',
    lastLoginIp: '203.0.113.45',
    createTime: '2024-07-22 11:45:00',
    updateTime: '2025-02-20 14:00:00',
    deleted: 0,
  },
  {
    id: 7,
    username: 'wu_jiu_pro',
    email: 'wujiu@pro.net',
    phone: '13500135007',
    nickname: '吴九',
    avatar: null,
    status: 1,
    emailVerified: 0,
    phoneVerified: 1,
    lastLoginTime: '2025-04-08 17:30:00',
    lastLoginIp: '192.0.2.100',
    createTime: '2024-08-10 09:00:00',
    updateTime: '2025-04-08 17:30:00',
    deleted: 0,
  },
  {
    id: 8,
    username: 'zheng_shi',
    email: 'zhengshi@web.cc',
    phone: '18900189008',
    nickname: '郑十',
    avatar: null,
    status: 1,
    emailVerified: 1,
    phoneVerified: 1,
    lastLoginTime: '2025-04-09 10:05:00',
    lastLoginIp: '10.0.2.15',
    createTime: '2024-09-03 14:20:00',
    updateTime: '2025-04-09 10:05:00',
    deleted: 0,
  },
  {
    id: 9,
    username: 'chen_yan',
    email: 'chenyan@algo.ai',
    phone: null,
    nickname: '陈燕',
    avatar: null,
    status: 1,
    emailVerified: 1,
    phoneVerified: 0,
    lastLoginTime: '2025-04-05 12:00:00',
    lastLoginIp: '172.31.0.50',
    createTime: '2024-10-18 10:10:00',
    updateTime: '2025-04-05 12:00:00',
    deleted: 0,
  },
  {
    id: 10,
    username: 'lin_kai',
    email: 'linkai@startup.io',
    phone: '17600176010',
    nickname: '林凯',
    avatar: null,
    status: 0,
    emailVerified: 0,
    phoneVerified: 0,
    lastLoginTime: null,
    lastLoginIp: null,
    createTime: '2024-11-25 08:30:00',
    updateTime: '2024-11-25 08:30:00',
    deleted: 0,
  },
  {
    id: 11,
    username: 'fang_lei',
    email: 'fanglei@sys.tech',
    phone: '13200132011',
    nickname: '方雷',
    avatar: null,
    status: 1,
    emailVerified: 1,
    phoneVerified: 1,
    lastLoginTime: '2025-04-08 23:45:00',
    lastLoginIp: '192.168.100.200',
    createTime: '2024-12-01 11:00:00',
    updateTime: '2025-04-08 23:45:00',
    deleted: 0,
  },
  {
    id: 12,
    username: 'xiao_ming_cs',
    email: 'xiaoming@campus.edu',
    phone: null,
    nickname: '小明',
    avatar: null,
    status: 1,
    emailVerified: 1,
    phoneVerified: 0,
    lastLoginTime: '2025-04-09 07:00:00',
    lastLoginIp: '10.20.30.40',
    createTime: '2025-01-05 09:00:00',
    updateTime: '2025-04-09 07:00:00',
    deleted: 0,
  },
]

/* ---- 状态 ---- */
const allUsers = ref<AdminUserItem[]>(JSON.parse(JSON.stringify(MOCK_USERS)))
const loading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const selectedUserIds = ref<number[]>([])
const currentPage = ref(1)
const pageSize = ref(10)

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

/* ---- 删除确认 ---- */
const deleteDialogOpen = ref(false)
const deletingUser = ref<AdminUserItem | null>(null)
const deleteSubmitting = ref(false)

/* ---- 计算属性 ---- */
const filteredUsers = computed(() => {
  let list = allUsers.value.filter((u) => u.deleted === 0)

  if (statusFilter.value !== '') {
    list = list.filter((u) => String(u.status) === statusFilter.value)
  }

  if (searchKeyword.value.trim()) {
    const q = searchKeyword.value.trim().toLowerCase()
    list = list.filter(
      (u) =>
        u.username.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q) ||
        (u.nickname && u.nickname.toLowerCase().includes(q)),
    )
  }

  return list
})

const totalItems = computed(() => filteredUsers.value.length)
const totalPages = computed(() => Math.max(1, Math.ceil(totalItems.value / pageSize.value)))

const pagedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredUsers.value.slice(start, start + pageSize.value)
})

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

const stats = computed(() => {
  const active = allUsers.value.filter((u) => u.deleted === 0)
  return {
    total: active.length,
    normal: active.filter((u) => u.status === 1).length,
    disabled: active.filter((u) => u.status === 0).length,
    emailVerified: active.filter((u) => u.emailVerified === 1).length,
  }
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

/* ---- 搜索 / 筛选重置分页 ---- */
const setSearchKeyword = (kw: string) => {
  searchKeyword.value = kw
  currentPage.value = 1
}
const setStatusFilter = (val: string) => {
  statusFilter.value = val
  currentPage.value = 1
}

/* ---- 刷新 ---- */
const refreshData = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 600)
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
  if (!formModel.username.trim()) formErrors.username = '用户名不能为空'
  if (!formModel.email.trim()) formErrors.email = '邮箱不能为空'
  if (editorMode.value === 'create' && !formModel.password.trim()) {
    formErrors.password = '密码不能为空'
  }
  return Object.keys(formErrors).length === 0
}

const saveUser = () => {
  if (!validateForm()) return
  editorSubmitting.value = true

  setTimeout(() => {
    if (editorMode.value === 'create') {
      const newId = Math.max(...allUsers.value.map((u) => u.id)) + 1
      allUsers.value.push({
        id: newId,
        username: formModel.username.trim(),
        email: formModel.email.trim(),
        phone: formModel.phone.trim() || null,
        nickname: formModel.nickname.trim() || null,
        avatar: null,
        status: formModel.status,
        emailVerified: 0,
        phoneVerified: 0,
        lastLoginTime: null,
        lastLoginIp: null,
        createTime: new Date().toISOString(),
        updateTime: new Date().toISOString(),
        deleted: 0,
      })
    } else if (editingUser.value) {
      const idx = allUsers.value.findIndex((u) => u.id === editingUser.value!.id)
      if (idx !== -1) {
        allUsers.value[idx] = {
          ...allUsers.value[idx],
          email: formModel.email.trim(),
          phone: formModel.phone.trim() || null,
          nickname: formModel.nickname.trim() || null,
          status: formModel.status,
          updateTime: new Date().toISOString(),
        }
      }
    }

    editorSubmitting.value = false
    editorOpen.value = false
  }, 500)
}

/* ---- 切换状态 ---- */
const toggleUserStatus = (user: AdminUserItem) => {
  const idx = allUsers.value.findIndex((u) => u.id === user.id)
  if (idx !== -1) {
    allUsers.value[idx] = {
      ...allUsers.value[idx],
      status: allUsers.value[idx].status === 1 ? 0 : 1,
      updateTime: new Date().toISOString(),
    }
  }
}

/* ---- 删除 ---- */
const requestDelete = (user: AdminUserItem) => {
  deletingUser.value = user
  deleteDialogOpen.value = true
}

const closeDeleteDialog = () => {
  if (!deleteSubmitting.value) {
    deleteDialogOpen.value = false
    deletingUser.value = null
  }
}

const confirmDelete = () => {
  if (!deletingUser.value || deleteSubmitting.value) return
  deleteSubmitting.value = true
  const targetId = deletingUser.value.id

  setTimeout(() => {
    const idx = allUsers.value.findIndex((u) => u.id === targetId)
    if (idx !== -1) {
      allUsers.value[idx] = { ...allUsers.value[idx], deleted: 1 }
    }
    deleteSubmitting.value = false
    deleteDialogOpen.value = false
    deletingUser.value = null
  }, 400)
}

/* ---- 更新表单 ---- */
const updateFormModel = (nextValue: AdminUserFormModel) => {
  Object.assign(formModel, nextValue)
}

const deleteDialogPt = {
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

    <!-- 删除确认 -->
    <Dialog
      :visible="deleteDialogOpen"
      modal
      :dismissableMask="true"
      :draggable="false"
      :pt="deleteDialogPt"
      @update:visible="(val) => { if (!val) closeDeleteDialog() }"
    >
      <template #header>
        <div class="space-y-1">
          <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">确认删除用户</h3>
          <p class="mt-1 text-sm text-gray-600 dark:text-gray-300">
            删除后数据将被软删除，不可恢复，请谨慎操作。
          </p>
        </div>
      </template>

      <div
        class="rounded-md border border-red-200 bg-red-50/70 px-3 py-2 text-sm text-red-700 dark:border-red-900/40 dark:bg-red-900/15 dark:text-red-200"
      >
        即将删除用户：
        <span class="font-semibold">{{ deletingUser?.nickname || deletingUser?.username || '-' }}</span>
        <span class="ml-1 text-xs opacity-70">(@{{ deletingUser?.username }})</span>
      </div>

      <template #footer>
        <button
          type="button"
          class="inline-flex h-9 cursor-pointer items-center justify-center rounded-md border border-gray-300 bg-white px-4 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:border-dark-border dark:bg-dark-card dark:text-gray-300 dark:hover:bg-dark-bg"
          :disabled="deleteSubmitting"
          @click="closeDeleteDialog"
        >
          取消
        </button>
        <button
          type="button"
          class="inline-flex h-9 cursor-pointer items-center justify-center rounded-md border border-transparent bg-red-600 px-4 text-sm font-medium text-white shadow-sm hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 dark:bg-red-500 dark:hover:bg-red-600 dark:focus:ring-offset-gray-900"
          :disabled="deleteSubmitting"
          @click="confirmDelete"
        >
          <i v-if="deleteSubmitting" class="fas fa-spinner fa-spin mr-2"></i>
          <span>确认删除</span>
        </button>
      </template>
    </Dialog>
  </main>
</template>

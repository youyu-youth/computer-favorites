<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { Bell, Inbox } from 'lucide-vue-next'
import AppPagination from '@/components/common/AppPagination.vue'
import MessageItem, { type MessageProps } from '@/components/user/message/MessageItem.vue'
import UserSelect from '@/components/user/UserSelect.vue'
import { batchReadUserMessages, getUserMessagePage, markUserMessageRead } from '@/api/user-notification'
import { useUserMessageUnread } from '@/composables/useUserMessageUnread'
import { useToast } from '@/composables/useToast'
import {
  USER_MESSAGE_TYPE_OPTIONS,
  resolveUserMessageViewType,
  type UserMessageFilterValue,
} from '@/constants/user-message'
import type { UserMessageItem, UserMessageQuery } from '@/types/notification'

type TabValue = 'all' | 'unread' | 'read'

const { add: showToast } = useToast()
const { setUnreadCount: setGlobalUnreadCount } = useUserMessageUnread()

const currentTab = ref<TabValue>('all')
const currentType = ref<UserMessageFilterValue>('all')
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const unreadCount = ref(0)
const loading = ref(false)
const loadError = ref('')
const messages = ref<MessageProps[]>([])
const selectedIds = ref<number[]>([])
const batchLoading = ref(false)
const allReadLoading = ref(false)
const singleLoadingIds = ref<number[]>([])

let latestRequestId = 0

const tabs: Array<{ label: string; value: TabValue }> = [
  { label: '全部消息', value: 'all' },
  { label: '未读', value: 'unread' },
  { label: '已读', value: 'read' },
]

const totalPages = computed(() => {
  if (pageSize.value <= 0) {
    return 1
  }
  return Math.max(1, Math.ceil(total.value / pageSize.value))
})

const queryParams = computed<UserMessageQuery>(() => {
  const query: UserMessageQuery = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
  }

  if (currentTab.value === 'read') {
    query.isRead = 1
  }
  if (currentTab.value === 'unread') {
    query.isRead = 0
  }
  if (currentType.value !== 'all') {
    query.type = currentType.value
  }

  return query
})

const selectableUnreadIds = computed(() => {
  return messages.value.filter((message) => !message.isRead).map((message) => message.id)
})

const selectedUnreadIds = computed(() => {
  const selectableSet = new Set(selectableUnreadIds.value)
  return selectedIds.value.filter((id) => selectableSet.has(id))
})

const hasSelectedUnread = computed(() => {
  return selectedUnreadIds.value.length > 0
})

const allUnreadSelected = computed(() => {
  if (selectableUnreadIds.value.length === 0) {
    return false
  }
  return selectableUnreadIds.value.every((id) => selectedIds.value.includes(id))
})

const allReadDisabled = computed(() => {
  return unreadCount.value === 0 || loading.value || batchLoading.value || allReadLoading.value
})

const resolveErrorMessage = (error: unknown, fallbackMessage: string): string => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallbackMessage
}

const formatMessageTime = (value: string): string => {
  if (!value) {
    return '-'
  }

  const parsedDate = new Date(value)
  if (Number.isNaN(parsedDate.getTime())) {
    return value.replace('T', ' ')
  }

  const now = Date.now()
  const diffMinutes = Math.floor((now - parsedDate.getTime()) / 60000)
  if (diffMinutes < 1) {
    return '刚刚'
  }
  if (diffMinutes < 60) {
    return `${diffMinutes}分钟前`
  }

  const diffHours = Math.floor(diffMinutes / 60)
  if (diffHours < 24) {
    return `${diffHours}小时前`
  }

  const diffDays = Math.floor(diffHours / 24)
  if (diffDays < 7) {
    return `${diffDays}天前`
  }

  const year = parsedDate.getFullYear()
  const month = `${parsedDate.getMonth() + 1}`.padStart(2, '0')
  const day = `${parsedDate.getDate()}`.padStart(2, '0')
  const hours = `${parsedDate.getHours()}`.padStart(2, '0')
  const minutes = `${parsedDate.getMinutes()}`.padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const toMessageView = (item: UserMessageItem): MessageProps => {
  return {
    id: Number(item.id),
    type: resolveUserMessageViewType(Number(item.type)),
    title: item.title || '系统通知',
    content: item.content || '',
    time: formatMessageTime(item.createTime),
    isRead: item.isRead === 1,
  }
}

const loadMessages = async () => {
  const requestId = ++latestRequestId
  loading.value = true
  loadError.value = ''

  try {
    const pageResult = await getUserMessagePage(queryParams.value)
    if (requestId !== latestRequestId) {
      return
    }

    const nextMessages = (pageResult.list || []).map(toMessageView)
    messages.value = nextMessages
    total.value = Number(pageResult.total || 0)
    unreadCount.value = Number(pageResult.unreadCount || 0)
    setGlobalUnreadCount(unreadCount.value)
    selectedIds.value = selectedIds.value.filter((id) => {
      return nextMessages.some((message) => message.id === id && !message.isRead)
    })

    if (pageNum.value > totalPages.value) {
      pageNum.value = totalPages.value
    }
  } catch (error) {
    if (requestId !== latestRequestId) {
      return
    }

    messages.value = []
    total.value = 0
    unreadCount.value = 0
    selectedIds.value = []
    const errorMessage = resolveErrorMessage(error, '消息列表加载失败，请稍后重试')
    loadError.value = errorMessage
    showToast({
      type: 'error',
      title: errorMessage,
    })
  } finally {
    if (requestId === latestRequestId) {
      loading.value = false
    }
  }
}

const retryLoad = () => {
  void loadMessages()
}

const isSingleLoading = (messageId: number): boolean => {
  return singleLoadingIds.value.includes(messageId)
}

const toggleSelect = (messageId: number) => {
  const target = messages.value.find((message) => message.id === messageId)
  if (!target || target.isRead) {
    return
  }

  if (selectedIds.value.includes(messageId)) {
    selectedIds.value = selectedIds.value.filter((id) => id !== messageId)
    return
  }
  selectedIds.value = [...selectedIds.value, messageId]
}

const toggleSelectAllUnread = () => {
  if (selectableUnreadIds.value.length === 0) {
    return
  }
  if (allUnreadSelected.value) {
    selectedIds.value = []
    return
  }
  selectedIds.value = [...selectableUnreadIds.value]
}

const markSingleAsRead = async (message: MessageProps) => {
  if (message.isRead || batchLoading.value || allReadLoading.value || isSingleLoading(message.id)) {
    return
  }

  singleLoadingIds.value = [...singleLoadingIds.value, message.id]
  try {
    await markUserMessageRead(message.id)
    selectedIds.value = selectedIds.value.filter((id) => id !== message.id)
    showToast({
      type: 'success',
      title: '消息已标记为已读',
    })
    await loadMessages()
  } catch (error) {
    showToast({
      type: 'error',
      title: resolveErrorMessage(error, '标记消息已读失败'),
    })
  } finally {
    singleLoadingIds.value = singleLoadingIds.value.filter((id) => id !== message.id)
  }
}

const markSelectedAsRead = async () => {
  if (batchLoading.value || allReadLoading.value) {
    return
  }

  const ids = [...selectedUnreadIds.value]
  if (ids.length === 0) {
    showToast({
      type: 'warning',
      title: '请先勾选未读消息',
    })
    return
  }

  batchLoading.value = true
  try {
    if (ids.length === 1) {
      const firstId = ids[0]
      if (firstId === undefined) {
        return
      }
      await markUserMessageRead(firstId)
      showToast({
        type: 'success',
        title: '已完成标记已读',
      })
    } else {
      const result = await batchReadUserMessages(ids)
      const successCount = Number(result.successCount || 0)
      const failedCount = Number(result.failedCount || 0)
      if (failedCount > 0) {
        showToast({
          type: 'warning',
          title: `已读 ${successCount} 条，失败 ${failedCount} 条`,
        })
      } else {
        showToast({
          type: 'success',
          title: `已成功标记 ${successCount} 条消息`,
        })
      }
    }

    selectedIds.value = []
    await loadMessages()
  } catch (error) {
    showToast({
      type: 'error',
      title: resolveErrorMessage(error, '批量标记已读失败'),
    })
  } finally {
    batchLoading.value = false
  }
}

const markAllAsRead = async () => {
  if (allReadDisabled.value) {
    return
  }

  allReadLoading.value = true
  selectedIds.value = []
  let totalSuccess = 0
  let loopCount = 0

  try {
    while (loopCount < 20) {
      loopCount += 1
      const unreadPage = await getUserMessagePage({
        pageNum: 1,
        pageSize: 100,
        isRead: 0,
      })

      const unreadIds = (unreadPage.list || [])
        .map((item) => Number(item.id))
        .filter((id) => Number.isFinite(id) && id > 0)

      if (unreadIds.length === 0) {
        break
      }

      if (unreadIds.length === 1) {
        const firstUnreadId = unreadIds[0]
        if (firstUnreadId === undefined) {
          break
        }
        await markUserMessageRead(firstUnreadId)
        totalSuccess += 1
      } else {
        const result = await batchReadUserMessages(unreadIds)
        const successCount = Number(result.successCount || 0)
        totalSuccess += successCount
        if (successCount === 0) {
          throw new Error('存在无法处理的未读消息，请稍后重试')
        }
      }

      if (unreadIds.length < 100) {
        break
      }
    }

    if (loopCount >= 20) {
      showToast({
        type: 'warning',
        title: '消息较多，已部分完成全部已读，请再次点击继续',
      })
    } else if (totalSuccess > 0) {
      showToast({
        type: 'success',
        title: `已标记 ${totalSuccess} 条消息为已读`,
      })
    }
    await loadMessages()
  } catch (error) {
    showToast({
      type: 'error',
      title: resolveErrorMessage(error, '全部已读失败，请稍后重试'),
    })
  } finally {
    allReadLoading.value = false
  }
}

watch([currentTab, currentType], () => {
  selectedIds.value = []
  if (pageNum.value !== 1) {
    pageNum.value = 1
    return
  }
  void loadMessages()
})

watch(pageNum, () => {
  selectedIds.value = []
  void loadMessages()
})

onMounted(() => {
  void loadMessages()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 text-gray-800 transition-colors duration-300 dark:bg-black dark:text-gray-100">
    <div class="mx-auto max-w-3xl p-4 sm:p-6 lg:p-8">
      <header class="mb-6 flex items-center justify-between sm:mb-8">
        <h1 class="flex items-center gap-2 text-2xl font-bold text-gray-900 dark:text-white sm:text-3xl">
          <Bell class="h-7 w-7 text-primary-500" stroke-width="2" />
          消息通知
        </h1>
      </header>

      <div class="mb-4 flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-center">
        <div
          class="no-scrollbar flex w-full space-x-1 overflow-x-auto rounded-lg bg-gray-200 p-1 dark:border dark:border-white/10 dark:bg-black sm:w-auto sm:space-x-2"
        >
          <button
            v-for="tab in tabs"
            :key="tab.value"
            @click="currentTab = tab.value"
            :class="[
              'flex-1 cursor-pointer whitespace-nowrap rounded-md px-4 py-2 text-sm font-medium transition-all duration-200 sm:flex-none',
              currentTab === tab.value
                ? 'bg-white text-primary-600 shadow-sm dark:bg-white/10 dark:text-primary-400'
                : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-200',
            ]"
          >
            {{ tab.label }}
            <span
              v-if="tab.value === 'unread' && unreadCount > 0"
              class="ml-1 inline-flex items-center justify-center rounded-full bg-red-500 px-2 py-0.5 text-xs font-bold leading-none text-red-100"
            >
              {{ unreadCount }}
            </span>
          </button>
        </div>

        <div class="flex w-full items-center gap-3 sm:w-auto">
          <UserSelect
            v-model="currentType"
            :options="USER_MESSAGE_TYPE_OPTIONS"
            optionLabel="name"
            optionValue="id"
            placeholder="消息类型"
            minWidth="160px"
          />

          <button
            @click="markAllAsRead"
            :disabled="allReadDisabled"
            class="shrink-0 whitespace-nowrap rounded px-2 py-1 text-sm text-gray-500 transition-colors hover:text-primary-500 disabled:cursor-not-allowed disabled:opacity-50 dark:text-gray-400 dark:hover:text-primary-400"
          >
            {{ allReadLoading ? '处理中...' : '全部已读' }}
          </button>
        </div>
      </div>

      <div
        class="mb-4 flex flex-wrap items-center justify-between gap-3 rounded-lg border border-gray-200 bg-white px-3 py-2 dark:border-white/10 dark:bg-black"
      >
        <div class="flex items-center gap-3">
          <button
            type="button"
            class="cursor-pointer rounded px-2 py-1 text-sm text-gray-600 transition-colors hover:text-primary-500 disabled:cursor-not-allowed disabled:opacity-50 dark:text-gray-300 dark:hover:text-primary-400"
            :disabled="selectableUnreadIds.length === 0 || loading || batchLoading || allReadLoading"
            @click="toggleSelectAllUnread"
          >
            {{ allUnreadSelected ? '取消全选未读' : '全选未读' }}
          </button>
          <span class="text-xs text-gray-500 dark:text-gray-400">已勾选 {{ selectedUnreadIds.length }} 条未读</span>
        </div>

        <button
          type="button"
          class="cursor-pointer rounded-md bg-primary-500 px-3 py-1.5 text-xs font-medium text-white transition-colors hover:bg-primary-600 disabled:cursor-not-allowed disabled:bg-primary-300 dark:disabled:bg-primary-800"
          :disabled="!hasSelectedUnread || batchLoading || loading || allReadLoading"
          @click="markSelectedAsRead"
        >
          {{ batchLoading ? '处理中...' : selectedUnreadIds.length > 1 ? '批量已读' : '标记已读' }}
        </button>
      </div>

      <div
        class="min-h-[400px] overflow-hidden rounded-xl border border-gray-100 bg-white shadow-sm dark:border-white/10 dark:bg-black"
      >
        <div
          v-if="loading"
          class="flex h-64 items-center justify-center text-sm text-gray-500 dark:text-gray-400"
        >
          正在加载消息...
        </div>

        <div
          v-else-if="loadError"
          class="flex h-64 flex-col items-center justify-center gap-3 text-sm text-red-500 dark:text-red-400"
        >
          <p>{{ loadError }}</p>
          <button
            type="button"
            class="cursor-pointer rounded border border-red-300 px-3 py-1 text-xs text-red-500 transition-colors hover:bg-red-50 dark:border-red-900 dark:hover:bg-red-900/20"
            @click="retryLoad"
          >
            重试
          </button>
        </div>

        <transition-group
          v-else-if="messages.length > 0"
          name="list"
          tag="ul"
          class="divide-y divide-gray-100 dark:divide-dark-border"
        >
          <MessageItem
            v-for="msg in messages"
            :key="msg.id"
            :message="msg"
            :checked="selectedIds.includes(msg.id)"
            :selectable="!msg.isRead"
            :selecting="batchLoading || allReadLoading || loading"
            :mark-read-loading="isSingleLoading(msg.id)"
            @toggle-select="toggleSelect(msg.id)"
            @mark-read="markSingleAsRead(msg)"
          />
        </transition-group>

        <div
          v-else
          class="flex h-64 flex-col items-center justify-center text-gray-400 dark:text-gray-500"
        >
          <Inbox stroke-width="1" class="mb-4 h-12 w-12 opacity-50" />
          <p>暂无相关消息</p>
        </div>
      </div>

      <AppPagination
        v-if="!loading && !loadError && totalPages > 1"
        :current-page="pageNum"
        :total-pages="totalPages"
        @update:current-page="pageNum = $event"
      />
    </div>
  </div>
</template>

<style scoped>
.list-enter-active,
.list-leave-active {
  transition: all var(--cf-motion-standard) var(--cf-ease-standard);
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(15px);
}

.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>

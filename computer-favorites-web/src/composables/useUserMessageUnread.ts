import { computed, ref } from 'vue'
import { getUserMessagePage } from '@/api/user-notification'

const unreadMessageCount = ref(0)
const unreadLoading = ref(false)
let latestRequestId = 0

const normalizeUnreadCount = (value: number): number => {
  if (!Number.isFinite(value) || value <= 0) {
    return 0
  }
  return Math.floor(value)
}

export function useUserMessageUnread() {
  const setUnreadCount = (value: number): void => {
    unreadMessageCount.value = normalizeUnreadCount(value)
  }

  const clearUnreadCount = (): void => {
    unreadMessageCount.value = 0
  }

  const refreshUnreadCount = async (): Promise<number> => {
    const requestId = ++latestRequestId
    unreadLoading.value = true

    try {
      const page = await getUserMessagePage({
        pageNum: 1,
        pageSize: 1,
      })

      if (requestId !== latestRequestId) {
        return unreadMessageCount.value
      }

      setUnreadCount(Number(page.unreadCount || 0))
    } catch {
      if (requestId !== latestRequestId) {
        return unreadMessageCount.value
      }
      // 未读数请求失败时保留当前值，避免红点异常闪烁。
    } finally {
      if (requestId === latestRequestId) {
        unreadLoading.value = false
      }
    }

    return unreadMessageCount.value
  }

  return {
    unreadMessageCount,
    hasUnreadMessage: computed(() => unreadMessageCount.value > 0),
    unreadLoading,
    setUnreadCount,
    clearUnreadCount,
    refreshUnreadCount,
  }
}

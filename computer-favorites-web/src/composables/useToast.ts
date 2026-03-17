import { ref } from 'vue'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface ToastOptions {
  title: string
  description?: string
  type?: ToastType
  timeout?: number
}

export interface Toast extends ToastOptions {
  id: string
}

// 维护全局 Toast 状态
const toasts = ref<Toast[]>([])

export function useToast() {
  /**
   * 添加一条新的 Toast 提示
   * @param options 提示配置项
   */
  const add = (options: ToastOptions) => {
    // 简易 ID 生成，满足前端渲染需求
    const id = Math.random().toString(36).substring(2, 9) + Date.now().toString(36)
    
    const toast: Toast = {
      id,
      title: options.title,
      description: options.description,
      type: options.type || 'info',
      timeout: options.timeout ?? 3000
    }
    
    toasts.value.push(toast)
    
    // 如果 timeout 不为 0，则自动移除
    if (toast.timeout !== 0) {
      setTimeout(() => {
        remove(id)
      }, toast.timeout)
    }
  }

  /**
   * 手动移除一条 Toast
   * @param id Toast 的唯一标识
   */
  const remove = (id: string) => {
    const index = toasts.value.findIndex(t => t.id === id)
    if (index > -1) {
      toasts.value.splice(index, 1)
    }
  }

  return {
    toasts,
    add,
    remove
  }
}

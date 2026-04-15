import { createApp, h } from 'vue'
import Toast from '@/components/common/Toast.vue'
import type { ToastProps } from '@/components/common/Toast.vue'

interface ToastOptions {
  message: string
  duration?: number
  type?: 'success' | 'error' | 'warning' | 'info'
}

class ToastManager {
  private container: HTMLDivElement | null = null
  private instances: Set<HTMLDivElement> = new Set()

  private ensureContainer() {
    if (!this.container) {
      this.container = document.createElement('div')
      this.container.className =
        'fixed top-4 left-1/2 -translate-x-1/2 z-[10000] flex flex-col gap-2 pointer-events-none w-full max-w-md px-4'
      document.body.appendChild(this.container)
    }
    return this.container
  }

  private show(options: ToastOptions) {
    const container = this.ensureContainer()
    const wrapper = document.createElement('div')
    this.instances.add(wrapper)

    const onClose = () => {
      app.unmount()
      if (wrapper.parentNode) {
        wrapper.parentNode.removeChild(wrapper)
      }
      this.instances.delete(wrapper)
    }

    const props: ToastProps = {
      type: options.type || 'info',
      message: options.message,
      duration: options.duration ?? 3000,
      onClose,
    }

    const app = createApp({
      render() {
        return h(Toast, props)
      },
    })

    container.appendChild(wrapper)
    app.mount(wrapper)
  }

  success(message: string, duration?: number) {
    this.show({ message, duration, type: 'success' })
  }

  error(message: string, duration?: number) {
    this.show({ message, duration, type: 'error' })
  }

  warning(message: string, duration?: number) {
    this.show({ message, duration, type: 'warning' })
  }

  info(message: string, duration?: number) {
    this.show({ message, duration, type: 'info' })
  }

  clear() {
    this.instances.forEach((wrapper) => {
      if (wrapper.parentNode) {
        wrapper.parentNode.removeChild(wrapper)
      }
    })
    this.instances.clear()
  }
}

const toastManager = new ToastManager()

export function useToast() {
  return {
    success: (message: string, duration?: number) => toastManager.success(message, duration),
    error: (message: string, duration?: number) => toastManager.error(message, duration),
    warning: (message: string, duration?: number) => toastManager.warning(message, duration),
    info: (message: string, duration?: number) => toastManager.info(message, duration),
    clear: () => toastManager.clear(),
  }
}

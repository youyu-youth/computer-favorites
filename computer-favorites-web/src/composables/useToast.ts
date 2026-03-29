import { useToast as usePrimeToast } from 'primevue/usetoast'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface ToastOptions {
  title: string
  description?: string
  type?: ToastType
  timeout?: number
}

const severityMap: Record<ToastType, 'success' | 'error' | 'warn' | 'info'> = {
  success: 'success',
  error: 'error',
  warning: 'warn',
  info: 'info',
}

export function useToast() {
  const toast = usePrimeToast()

  const add = (options: ToastOptions): void => {
    const type = options.type ?? 'info'
    toast.add({
      group: 'app-headless',
      severity: severityMap[type],
      summary: options.title,
      detail: options.description,
      life: options.timeout ?? 3000,
    })
  }

  const clear = (): void => {
    toast.removeGroup('app-headless')
  }

  return {
    add,
    clear,
  }
}

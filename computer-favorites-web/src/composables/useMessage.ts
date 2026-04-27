import { reactive, computed } from 'vue'

export type MessageType = 'success' | 'info' | 'error' | 'warning'
export type MessagePosition = 'top-center' | 'top-right'

export interface MessageItem {
  id: number
  type: MessageType
  title: string
  description?: string
  duration: number
  position: MessagePosition
}

export interface MessageOptions {
  title: string
  description?: string
  type?: MessageType
  duration?: number
  position?: MessagePosition
}

interface MessageState {
  messages: MessageItem[]
  offset: number
}

const state = reactive<MessageState>({
  messages: [],
  offset: 76,
})

let nextId = 0
const timers = new Map<number, ReturnType<typeof setTimeout>>()

function add(options: MessageOptions): void {
  const id = nextId++
  const item: MessageItem = {
    id,
    type: options.type ?? 'info',
    title: options.title,
    description: options.description,
    duration: options.duration ?? 3000,
    position: options.position ?? 'top-center',
  }
  state.messages.push(item)

  if (item.duration > 0) {
    timers.set(id, setTimeout(() => close(id), item.duration))
  }
}

function close(id: number): void {
  const timer = timers.get(id)
  if (timer) {
    clearTimeout(timer)
    timers.delete(id)
  }
  const index = state.messages.findIndex((m) => m.id === id)
  if (index > -1) state.messages.splice(index, 1)
}

function clear(): void {
  timers.forEach((timer) => clearTimeout(timer))
  timers.clear()
  state.messages.splice(0, state.messages.length)
}

function setOffset(px: number): void {
  state.offset = px
}

export function useMessage() {
  return {
    messages: computed(() => state.messages),
    offset: computed(() => state.offset),
    add,
    close,
    clear,
    setOffset,
  }
}

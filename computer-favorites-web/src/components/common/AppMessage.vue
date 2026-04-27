<script setup lang="ts">
import { computed } from 'vue'
import { useMessage } from '@/composables/useMessage'
import type { MessageItem, MessagePosition } from '@/composables/useMessage'

const { messages, offset, close } = useMessage()

const typeConfig: Record<string, { icon: string; color: string; darkColor: string }> = {
  success: {
    icon: 'fa-check',
    color: 'text-emerald-600 bg-emerald-50 border-emerald-200',
    darkColor: 'dark:text-emerald-400 dark:bg-emerald-950/60 dark:border-emerald-800/60',
  },
  info: {
    icon: 'fa-info',
    color: 'text-sky-600 bg-sky-50 border-sky-200',
    darkColor: 'dark:text-sky-400 dark:bg-sky-950/60 dark:border-sky-800/60',
  },
  error: {
    icon: 'fa-xmark',
    color: 'text-rose-600 bg-rose-50 border-rose-200',
    darkColor: 'dark:text-rose-400 dark:bg-rose-950/60 dark:border-rose-800/60',
  },
  warning: {
    icon: 'fa-exclamation',
    color: 'text-amber-600 bg-amber-50 border-amber-200',
    darkColor: 'dark:text-amber-400 dark:bg-amber-950/60 dark:border-amber-800/60',
  },
}

function getConfig(item: MessageItem) {
  return typeConfig[item.type] ?? typeConfig.info
}

const centerMessages = computed(() => messages.value.filter((m) => m.position === 'top-center'))
const rightMessages = computed(() => messages.value.filter((m) => m.position === 'top-right'))

const containerStyle = computed(() => ({
  top: `${offset.value}px`,
}))

const positionClasses: Record<MessagePosition, string> = {
  'top-center': 'inset-x-0 items-center',
  'top-right': 'right-0 items-end',
}

function onEnter(el: Element) {
  const htmlEl = el as HTMLElement
  htmlEl.style.height = '0'
  htmlEl.style.opacity = '0'
  htmlEl.style.transform = 'translateY(-12px) scale(0.97)'
  requestAnimationFrame(() => {
    htmlEl.style.transition = `height var(--cf-motion-standard) var(--cf-ease-standard), opacity var(--cf-motion-standard) var(--cf-ease-standard), transform var(--cf-motion-standard) var(--cf-ease-standard)`
    htmlEl.style.height = `${htmlEl.scrollHeight}px`
    htmlEl.style.opacity = '1'
    htmlEl.style.transform = 'translateY(0) scale(1)'
  })
}

function onAfterEnter(el: Element) {
  const htmlEl = el as HTMLElement
  htmlEl.style.height = 'auto'
  htmlEl.style.transition = ''
}

function onLeave(el: Element) {
  const htmlEl = el as HTMLElement
  const h = htmlEl.scrollHeight
  htmlEl.style.height = `${h}px`
  htmlEl.style.overflow = 'hidden'
  requestAnimationFrame(() => {
    htmlEl.style.transition = `height var(--cf-motion-fast) var(--cf-ease-standard), opacity var(--cf-motion-fast) var(--cf-ease-standard), transform var(--cf-motion-fast) var(--cf-ease-standard)`
    htmlEl.style.height = '0'
    htmlEl.style.opacity = '0'
    htmlEl.style.transform = 'translateY(-8px) scale(0.97)'
  })
}
</script>

<template>
  <Teleport to="body">
    <!-- top-center -->
    <div
      v-if="centerMessages.length > 0"
      class="pointer-events-none fixed z-[99999] flex flex-col"
      :class="positionClasses['top-center']"
      :style="containerStyle"
    >
      <TransitionGroup
        @enter="onEnter"
        @after-enter="onAfterEnter"
        @leave="onLeave"
      >
        <div
          v-for="item in centerMessages"
          :key="item.id"
          class="pointer-events-auto mx-4 w-full max-w-sm overflow-hidden"
          role="alert"
        >
          <div
            class="flex items-start gap-2.5 rounded-lg border px-3.5 py-2.5"
            :class="[getConfig(item).color, getConfig(item).darkColor]"
          >
            <span
              class="mt-px flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full text-[10px]"
              :class="[getConfig(item).color.split(' ')[0]]"
              style="background: currentColor"
            >
              <i
                :class="['fas', getConfig(item).icon]"
                class="text-white"
              />
            </span>
            <div class="min-w-0 flex-1">
              <p class="text-[13px] font-medium leading-snug">
                {{ item.title }}
              </p>
              <p
                v-if="item.description"
                class="mt-0.5 text-[12px] leading-relaxed opacity-70"
              >
                {{ item.description }}
              </p>
            </div>
            <button
              class="mt-px flex-shrink-0 rounded p-0.5 opacity-40 transition-opacity hover:opacity-70"
              @click="close(item.id)"
            >
              <i class="fas fa-xmark text-xs" />
            </button>
          </div>
        </div>
      </TransitionGroup>
    </div>

    <!-- top-right -->
    <div
      v-if="rightMessages.length > 0"
      class="pointer-events-none fixed right-0 z-[99999] flex flex-col items-end"
      :style="containerStyle"
    >
      <TransitionGroup
        @enter="onEnter"
        @after-enter="onAfterEnter"
        @leave="onLeave"
      >
        <div
          v-for="item in rightMessages"
          :key="item.id"
          class="pointer-events-auto mr-4 w-full max-w-sm overflow-hidden"
          role="alert"
        >
          <div
            class="flex items-start gap-2.5 rounded-lg border px-3.5 py-2.5"
            :class="[getConfig(item).color, getConfig(item).darkColor]"
          >
            <span
              class="mt-px flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full text-[10px]"
              :class="[getConfig(item).color.split(' ')[0]]"
              style="background: currentColor"
            >
              <i
                :class="['fas', getConfig(item).icon]"
                class="text-white"
              />
            </span>
            <div class="min-w-0 flex-1">
              <p class="text-[13px] font-medium leading-snug">
                {{ item.title }}
              </p>
              <p
                v-if="item.description"
                class="mt-0.5 text-[12px] leading-relaxed opacity-70"
              >
                {{ item.description }}
              </p>
            </div>
            <button
              class="mt-px flex-shrink-0 rounded p-0.5 opacity-40 transition-opacity hover:opacity-70"
              @click="close(item.id)"
            >
              <i class="fas fa-xmark text-xs" />
            </button>
          </div>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

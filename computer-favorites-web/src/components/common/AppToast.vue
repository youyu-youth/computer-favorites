<template>
  <!-- 移动端：顶部居中铺满留白；PC端：右上角悬浮 -->
  <!-- z-[100] 确保覆盖绝大部分弹出层 -->
  <div
    class="fixed top-4 right-0 left-0 sm:left-auto sm:right-6 z-[100] flex flex-col gap-3 px-4 sm:px-0 pointer-events-none sm:max-w-[360px] w-full"
  >
    <TransitionGroup name="toast" tag="div" class="flex flex-col gap-3 relative w-full">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="pointer-events-auto flex w-full items-start gap-3 rounded-xl border border-gray-100 bg-white/95 backdrop-blur-md p-4 shadow-[0_8px_30px_rgb(0,0,0,0.08)] dark:border-gray-800/80 dark:bg-gray-900/95 dark:shadow-[0_8px_30px_rgb(0,0,0,0.4)] overflow-hidden transition-all duration-300"
        role="alert"
      >
        <!-- Icon 区域 -->
        <div class="flex-shrink-0 mt-0.5">
          <!-- Success -->
          <svg
            v-if="toast.type === 'success'"
            class="h-5 w-5 text-emerald-500"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fill-rule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z"
              clip-rule="evenodd"
            />
          </svg>
          <!-- Error -->
          <svg
            v-else-if="toast.type === 'error'"
            class="h-5 w-5 text-rose-500"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fill-rule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z"
              clip-rule="evenodd"
            />
          </svg>
          <!-- Warning -->
          <svg
            v-else-if="toast.type === 'warning'"
            class="h-5 w-5 text-amber-500"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fill-rule="evenodd"
              d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a.75.75 0 00-.75.75v4.5a.75.75 0 001.5 0v-4.5A.75.75 0 0010 5z"
              clip-rule="evenodd"
            />
          </svg>
          <!-- Info (Default) -->
          <svg
            v-else
            class="h-5 w-5 text-blue-500"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fill-rule="evenodd"
              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a.75.75 0 000 1.5h.253a.25.25 0 01.244.304l-.459 2.066A1.75 1.75 0 0010.747 15H11a.75.75 0 000-1.5h-.253a.25.25 0 01-.244-.304l.459-2.066A1.75 1.75 0 009.253 9H9z"
              clip-rule="evenodd"
            />
          </svg>
        </div>

        <!-- Content -->
        <div class="flex-1 min-w-0 pt-[2px]">
          <p class="text-[14px] font-medium text-gray-900 dark:text-gray-100 leading-snug truncate">
            {{ toast.title }}
          </p>
          <p
            v-if="toast.description"
            class="mt-1 text-[13px] text-gray-500 dark:text-gray-400 leading-relaxed break-words"
          >
            {{ toast.description }}
          </p>
        </div>

        <!-- Close Button -->
        <button
          @click="remove(toast.id)"
          class="flex-shrink-0 ml-3 rounded-md p-1 -mr-1 text-gray-400 hover:text-gray-600 hover:bg-gray-100/50 dark:hover:text-gray-300 dark:hover:bg-gray-800/50 focus:outline-none focus:ring-2 focus:ring-gray-200 dark:focus:ring-gray-700 transition-colors"
          :aria-label="$t('common.close')"
        >
          <svg
            class="h-4 w-4"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z"
            />
          </svg>
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { useToast } from '@/composables/useToast'

const { toasts, remove } = useToast()
</script>

<style scoped>
/* 入场与退场动画（Spring 效果模拟） */
.toast-move,
.toast-enter-active,
.toast-leave-active {
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

/* 移动端默认从顶部滑入 */
.toast-enter-from {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}
@media (min-width: 640px) {
  /* PC 端从右侧滑入 */
  .toast-enter-from {
    transform: translateX(40px) scale(0.95);
  }
}

.toast-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}
@media (min-width: 640px) {
  .toast-leave-to {
    transform: translateX(40px) scale(0.95);
  }
}

/* 离开时设为 absolute 以确保持续动画排版平滑 */
.toast-leave-active {
  position: absolute;
  /* 使元素在离开状态下依然保持容器宽度，解决移动端 absolute 缩放问题 */
  width: calc(100% - 2rem);
  left: 1rem;
}
@media (min-width: 640px) {
  .toast-leave-active {
    width: 100%;
    left: 0;
  }
}
</style>

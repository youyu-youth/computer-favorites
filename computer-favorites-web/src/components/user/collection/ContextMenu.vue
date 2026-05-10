<script setup lang="ts">
/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 通用上下文菜单组件 — 支持桌面端右键定位与移动端底部滑出
 */
import { computed, onBeforeUnmount, onMounted, ref, watch, nextTick } from 'vue'
import {
  FolderPlus,
  Pencil,
  EyeOff,
  Trash2,
  Eye,
  Lock,
  Globe,
  type LucideIcon,
} from 'lucide-vue-next'

export interface MenuItem {
  label: string
  icon?: 'folder-plus' | 'pencil' | 'eye-off' | 'trash-2' | 'eye' | 'lock' | 'globe'
  danger?: boolean
  divider?: boolean
  onClick: () => void
}

interface Props {
  visible: boolean
  x: number
  y: number
  items: MenuItem[]
  isMobile: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const menuRef = ref<HTMLElement | null>(null)
const adjustedPos = ref({ x: 0, y: 0 })

const iconMap: Record<string, LucideIcon> = {
  'folder-plus': FolderPlus,
  pencil: Pencil,
  'eye-off': EyeOff,
  'trash-2': Trash2,
  eye: Eye,
  lock: Lock,
  globe: Globe,
}

const closeMenu = () => {
  emit('update:visible', false)
}

const handleItemClick = (item: MenuItem) => {
  if (item.divider) return
  item.onClick()
  closeMenu()
}

const handleBackdropClick = () => {
  closeMenu()
}

const calculatePosition = async () => {
  if (!menuRef.value || props.isMobile) return
  await nextTick()
  const menu = menuRef.value
  const rect = menu.getBoundingClientRect()
  const vw = window.innerWidth
  const vh = window.innerHeight
  let nx = props.x
  let ny = props.y
  if (nx + rect.width > vw - 8) {
    nx = vw - rect.width - 8
  }
  if (nx < 8) nx = 8
  if (ny + rect.height > vh - 8) {
    ny = vh - rect.height - 8
  }
  if (ny < 8) ny = 8
  adjustedPos.value = { x: nx, y: ny }
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && props.visible) {
    closeMenu()
  }
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      adjustedPos.value = { x: props.x, y: props.y }
      calculatePosition()
      document.addEventListener('keydown', handleKeydown)
    } else {
      document.removeEventListener('keydown', handleKeydown)
    }
  },
)

onMounted(() => {
  if (props.visible) document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <Teleport to="body">
    <!-- 桌面端浮动菜单 -->
    <template v-if="!isMobile">
      <Transition
        enter-active-class="transition-all duration-200 ease-out"
        enter-from-class="opacity-0 scale-95"
        enter-to-class="opacity-100 scale-100"
        leave-active-class="transition-all duration-150 ease-in"
        leave-from-class="opacity-100 scale-100"
        leave-to-class="opacity-0 scale-95"
      >
        <div v-if="visible" class="fixed inset-0 z-[200]" @click.self="handleBackdropClick">
          <div
            ref="menuRef"
            class="ctx-menu-panel absolute min-w-[180px] overflow-hidden rounded-xl py-1.5"
            :style="{ left: adjustedPos.x + 'px', top: adjustedPos.y + 'px' }"
            @click.stop
          >
            <template v-for="(item, idx) in items" :key="idx">
              <div v-if="item.divider" class="ctx-divider my-1 border-t" />
              <button
                v-else
                type="button"
                class="ctx-item w-full flex items-center gap-2.5 px-3.5 py-2 text-sm transition-all duration-150 cursor-pointer"
                :class="[item.danger ? 'ctx-item-danger' : 'ctx-item-default']"
                @click="handleItemClick(item)"
              >
                <component
                  :is="iconMap[item.icon || '']"
                  v-if="item.icon && iconMap[item.icon]"
                  class="h-4 w-4 shrink-0"
                />
                <span>{{ item.label }}</span>
              </button>
            </template>
          </div>
        </div>
      </Transition>
    </template>

    <!-- 移动端底部滑出 -->
    <template v-else>
      <Transition
        enter-active-class="transition-opacity duration-200"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition-opacity duration-150"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div
          v-if="visible"
          class="fixed inset-0 z-[200] bg-black/50 backdrop-blur-sm"
          @click.self="handleBackdropClick"
        />
      </Transition>
      <Transition
        enter-active-class="transition-transform duration-300 ease-out"
        enter-from-class="translate-y-full"
        enter-to-class="translate-y-0"
        leave-active-class="transition-transform duration-200 ease-in"
        leave-from-class="translate-y-0"
        leave-to-class="translate-y-full"
      >
        <div
          v-if="visible"
          class="ctx-sheet fixed bottom-0 left-0 right-0 z-[201] rounded-t-2xl px-2 pb-safe pt-3"
          @click.stop
        >
          <div class="ctx-sheet-handle mx-auto mb-3 h-1 w-10 rounded-full" />
          <template v-for="(item, idx) in items" :key="idx">
            <div v-if="item.divider" class="ctx-divider my-1 border-t" />
            <button
              v-else
              type="button"
              class="ctx-item w-full flex items-center gap-3 px-4 py-3.5 text-[15px] font-medium transition-all duration-150 cursor-pointer rounded-lg"
              :class="[item.danger ? 'ctx-item-danger' : 'ctx-item-default']"
              @click="handleItemClick(item)"
            >
              <component
                :is="iconMap[item.icon || '']"
                v-if="item.icon && iconMap[item.icon]"
                class="h-5 w-5 shrink-0"
              />
              <span>{{ item.label }}</span>
            </button>
          </template>
          <div class="mt-2 px-2 pb-3">
            <button
              type="button"
              class="ctx-sheet-cancel w-full py-3 text-sm font-medium rounded-xl transition-all duration-150 cursor-pointer"
              @click="closeMenu"
            >
              取消
            </button>
          </div>
        </div>
      </Transition>
    </template>
  </Teleport>
</template>

<style scoped>
/* 桌面端菜单面板 */
.ctx-menu-panel {
  border: 1px solid rgb(229 231 235 / 0.8);
  background-color: rgb(255 255 255);
  box-shadow:
    0 8px 30px rgb(0 0 0 / 0.08),
    0 2px 8px rgb(0 0 0 / 0.04),
    inset 0 1px 0 rgb(255 255 255 / 0.8);
}

:root.dark .ctx-menu-panel {
  border-color: rgb(255 255 255 / 0.08);
  background-color: rgb(14 14 16);
  box-shadow:
    0 16px 40px rgb(0 0 0 / 0.6),
    0 4px 12px rgb(0 0 0 / 0.4),
    inset 0 1px 0 rgb(255 255 255 / 0.06);
}

/* 分隔线 */
.ctx-divider {
  border-color: rgb(229 231 235);
}

:root.dark .ctx-divider {
  border-color: rgb(255 255 255 / 0.06);
}

/* 默认菜单项 */
.ctx-item-default {
  color: rgb(55 65 81);
}

.ctx-item-default:hover {
  background-color: rgb(59 130 246 / 0.08);
  color: rgb(37 99 235);
}

.ctx-item-default:active {
  background-color: rgb(59 130 246 / 0.14);
  transform: scale(0.98);
}

:root.dark .ctx-item-default {
  color: rgb(209 213 219);
}

:root.dark .ctx-item-default:hover {
  background-color: rgb(59 130 246 / 0.1);
  color: rgb(96 165 250);
}

:root.dark .ctx-item-default:active {
  background-color: rgb(59 130 246 / 0.18);
}

/* 危险菜单项 */
.ctx-item-danger {
  color: rgb(239 68 68);
}

.ctx-item-danger:hover {
  background-color: rgb(239 68 68 / 0.08);
  color: rgb(220 38 38);
}

.ctx-item-danger:active {
  background-color: rgb(239 68 68 / 0.14);
  transform: scale(0.98);
}

:root.dark .ctx-item-danger {
  color: rgb(248 113 113);
}

:root.dark .ctx-item-danger:hover {
  background-color: rgb(239 68 68 / 0.1);
  color: rgb(252 165 165);
}

:root.dark .ctx-item-danger:active {
  background-color: rgb(239 68 68 / 0.18);
}

/* 移动端底部面板 */
.ctx-sheet {
  border-top: 1px solid rgb(229 231 235 / 0.8);
  background-color: rgb(255 255 255);
  box-shadow: 0 -8px 30px rgb(0 0 0 / 0.1);
}

:root.dark .ctx-sheet {
  border-top-color: rgb(255 255 255 / 0.08);
  background-color: rgb(14 14 16);
  box-shadow:
    0 -8px 30px rgb(0 0 0 / 0.5),
    inset 0 1px 0 rgb(255 255 255 / 0.06);
}

/* 拖拽手柄 */
.ctx-sheet-handle {
  background-color: rgb(209 213 219);
}

:root.dark .ctx-sheet-handle {
  background-color: rgb(255 255 255 / 0.1);
}

/* 取消按钮 */
.ctx-sheet-cancel {
  color: rgb(55 65 81);
  background-color: rgb(243 244 246);
}

.ctx-sheet-cancel:hover {
  background-color: rgb(229 231 235);
}

.ctx-sheet-cancel:active {
  transform: scale(0.98);
}

:root.dark .ctx-sheet-cancel {
  color: rgb(209 213 219);
  background-color: rgb(255 255 255 / 0.06);
}

:root.dark .ctx-sheet-cancel:hover {
  background-color: rgb(255 255 255 / 0.1);
}

:root.dark .ctx-sheet-cancel:active {
  background-color: rgb(255 255 255 / 0.14);
}
</style>

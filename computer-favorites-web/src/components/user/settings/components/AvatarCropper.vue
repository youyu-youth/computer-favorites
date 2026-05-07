<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { VueCropper } from 'vue-cropper'
import 'vue-cropper/dist/index.css'
import {
  Maximize2,
  RotateCcw,
  RotateCw,
  RefreshCw,
  ZoomIn,
  ZoomOut,
} from 'lucide-vue-next'

type AspectRatio = '1:1' | '4:3' | '16:9' | 'free'
type OutputType = 'jpeg' | 'png' | 'webp'

interface Props {
  src: string
  outputType?: OutputType
  outputQuality?: number
  ratios?: AspectRatio[]
  defaultRatio?: AspectRatio
  outputMaxSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  outputType: 'jpeg',
  outputQuality: 0.92,
  ratios: () => ['1:1', '4:3', '16:9', 'free'],
  defaultRatio: '1:1',
  outputMaxSize: 512,
})

const emit = defineEmits<{
  (e: 'ready'): void
  (e: 'error', message: string): void
}>()

defineOptions({
  name: 'AvatarCropper',
})

// VueCropper 实例引用，类型不严格（库未提供完整 d.ts）
const cropperRef = ref<{
  getCropBlob: (cb: (blob: Blob) => void) => void
  refresh: () => void
  rotateLeft: () => void
  rotateRight: () => void
  changeScale: (n: number) => void
} | null>(null)

const currentRatio = ref<AspectRatio>(props.defaultRatio)
const zoomValue = ref(0)
const isReady = ref(false)

// 比例胶囊配置
const ratioPresets: Record<AspectRatio, [number, number] | null> = {
  '1:1': [1, 1],
  '4:3': [4, 3],
  '16:9': [16, 9],
  free: null,
}

const ratioLabel: Record<AspectRatio, string> = {
  '1:1': '1:1',
  '4:3': '4:3',
  '16:9': '16:9',
  free: '自由',
}

const isFixedRatio = computed(() => currentRatio.value !== 'free')
const fixedNumber = computed(() => ratioPresets[currentRatio.value] ?? [1, 1])

// 切换比例时通过修改 key 强制 vue-cropper 重新挂载，确保比例真正生效
const cropperKey = ref(0)

const handleRatioChange = (ratio: AspectRatio) => {
  if (ratio === currentRatio.value) {
    return
  }
  currentRatio.value = ratio
  cropperKey.value += 1
  zoomValue.value = 0
}

// 缩放：vue-cropper changeScale(num) 中 num 为相对偏移
const applyZoom = (next: number) => {
  const clamped = Math.max(-30, Math.min(60, next))
  const delta = clamped - zoomValue.value
  if (delta === 0 || !cropperRef.value) {
    return
  }
  cropperRef.value.changeScale(delta)
  zoomValue.value = clamped
}

const handleZoomSlider = (event: Event) => {
  const target = event.target as HTMLInputElement
  applyZoom(Number(target.value))
}

const zoomIn = () => applyZoom(zoomValue.value + 5)
const zoomOut = () => applyZoom(zoomValue.value - 5)

const rotateLeft = () => cropperRef.value?.rotateLeft()
const rotateRight = () => cropperRef.value?.rotateRight()
const reset = () => {
  cropperRef.value?.refresh()
  zoomValue.value = 0
}

const onImgLoad = (status: 'success' | 'error') => {
  if (status === 'success') {
    isReady.value = true
    emit('ready')
  } else {
    emit('error', '图片加载失败，请更换图片重试')
  }
}

// 暴露给父组件：拿到裁剪后的 File
const getCroppedFile = (filename = 'avatar'): Promise<File> =>
  new Promise((resolve, reject) => {
    const cropper = cropperRef.value
    if (!cropper) {
      reject(new Error('裁剪器尚未初始化'))
      return
    }
    try {
      cropper.getCropBlob((blob: Blob) => {
        if (!blob) {
          reject(new Error('裁剪结果为空，请重试'))
          return
        }
        const ext = props.outputType === 'jpeg' ? 'jpg' : props.outputType
        const file = new File([blob], `${filename}.${ext}`, {
          type: `image/${props.outputType}`,
          lastModified: Date.now(),
        })
        resolve(file)
      })
    } catch (err) {
      reject(err instanceof Error ? err : new Error('裁剪失败'))
    }
  })

defineExpose({
  getCroppedFile,
  reset,
})

// 切换图片源时重置内部状态
watch(
  () => props.src,
  () => {
    isReady.value = false
    zoomValue.value = 0
  },
)

// 桌面端键盘快捷键（仅在组件挂载期间）
const handleKeydown = (event: KeyboardEvent) => {
  if (!isReady.value) {
    return
  }
  // 避免与输入框冲突
  const target = event.target as HTMLElement | null
  if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA')) {
    return
  }
  if (event.key === '+' || event.key === '=') {
    event.preventDefault()
    zoomIn()
  } else if (event.key === '-' || event.key === '_') {
    event.preventDefault()
    zoomOut()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <div class="avatar-cropper flex flex-col gap-3">
    <!-- 裁剪区 -->
    <div class="cropper-stage relative w-full overflow-hidden rounded-2xl border border-black/10 bg-black dark:border-white/10">
      <VueCropper
        :key="cropperKey"
        ref="cropperRef"
        :img="src"
        :auto-crop="true"
        :fixed="isFixedRatio"
        :fixed-number="fixedNumber"
        :fixed-box="false"
        :can-move="true"
        :can-move-box="true"
        :can-scale="true"
        :center-box="true"
        :info="true"
        :info-true="true"
        :output-type="outputType"
        :output-size="outputQuality"
        :enlarge="1"
        :auto-crop-width="240"
        :auto-crop-height="240"
        mode="contain"
        @img-load="onImgLoad"
      />
    </div>

    <!-- 比例切换 -->
    <div class="flex flex-wrap items-center gap-1.5">
      <span class="text-[11px] font-medium tracking-wider text-black/45 uppercase dark:text-white/45">
        比例
      </span>
      <div class="flex flex-wrap gap-1">
        <button
          v-for="r in ratios"
          :key="r"
          type="button"
          class="ratio-chip cursor-pointer"
          :class="currentRatio === r ? 'is-active' : ''"
          @click="handleRatioChange(r)"
        >
          {{ ratioLabel[r] }}
        </button>
      </div>
    </div>

    <!-- 缩放滑块 + 操作按钮 -->
    <div class="flex flex-col gap-2.5 sm:flex-row sm:items-center sm:gap-3">
      <!-- 缩放 -->
      <div class="zoom-row flex flex-1 items-center gap-2 rounded-xl border border-black/10 bg-white px-2.5 py-1.5 dark:border-white/10 dark:bg-black">
        <button
          type="button"
          class="icon-btn cursor-pointer"
          aria-label="缩小"
          @click="zoomOut"
        >
          <ZoomOut :size="14" :stroke-width="1.75" />
        </button>
        <input
          :value="zoomValue"
          type="range"
          min="-30"
          max="60"
          step="1"
          class="zoom-slider flex-1"
          aria-label="缩放滑块"
          @input="handleZoomSlider"
        />
        <button
          type="button"
          class="icon-btn cursor-pointer"
          aria-label="放大"
          @click="zoomIn"
        >
          <ZoomIn :size="14" :stroke-width="1.75" />
        </button>
      </div>

      <!-- 旋转 + 重置 -->
      <div class="flex items-center gap-1.5">
        <button
          type="button"
          class="action-btn cursor-pointer"
          aria-label="向左旋转 90°"
          @click="rotateLeft"
        >
          <RotateCcw :size="14" :stroke-width="1.75" />
          <span class="hidden sm:inline">左转</span>
        </button>
        <button
          type="button"
          class="action-btn cursor-pointer"
          aria-label="向右旋转 90°"
          @click="rotateRight"
        >
          <RotateCw :size="14" :stroke-width="1.75" />
          <span class="hidden sm:inline">右转</span>
        </button>
        <button
          type="button"
          class="action-btn cursor-pointer"
          aria-label="重置裁剪"
          @click="reset"
        >
          <RefreshCw :size="14" :stroke-width="1.75" />
          <span class="hidden sm:inline">重置</span>
        </button>
      </div>
    </div>

    <!-- 提示语 -->
    <p class="flex items-center gap-1.5 text-[11px] text-black/50 dark:text-white/50">
      <Maximize2 :size="11" :stroke-width="1.75" />
      <span>拖动调整位置，滚轮或滑块缩放，框角拖拽改变裁剪范围。</span>
    </p>
  </div>
</template>

<style scoped>
/* 裁剪区高度自适应 */
.cropper-stage {
  height: 280px;
}
@media (min-width: 640px) {
  .cropper-stage {
    height: 360px;
  }
}
@media (min-width: 1024px) {
  .cropper-stage {
    height: 400px;
  }
}

/* 比例胶囊 */
.ratio-chip {
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid rgb(0 0 0 / 0.12);
  background: white;
  color: rgb(0 0 0 / 0.7);
  transition:
    background-color 160ms ease,
    color 160ms ease,
    border-color 160ms ease;
}
.ratio-chip:hover {
  border-color: rgb(0 0 0 / 0.4);
  color: black;
}
.ratio-chip.is-active {
  background: black;
  color: white;
  border-color: black;
}
:where(html.dark) .ratio-chip {
  background: black;
  border-color: rgb(255 255 255 / 0.12);
  color: rgb(255 255 255 / 0.7);
}
:where(html.dark) .ratio-chip:hover {
  border-color: rgb(255 255 255 / 0.4);
  color: white;
}
:where(html.dark) .ratio-chip.is-active {
  background: white;
  color: black;
  border-color: white;
}

/* 操作按钮 */
.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 28px;
  width: 28px;
  border-radius: 8px;
  color: rgb(0 0 0 / 0.7);
  transition:
    background-color 140ms ease,
    color 140ms ease;
}
.icon-btn:hover {
  background: rgb(0 0 0 / 0.06);
  color: black;
}
:where(html.dark) .icon-btn {
  color: rgb(255 255 255 / 0.7);
}
:where(html.dark) .icon-btn:hover {
  background: rgb(255 255 255 / 0.08);
  color: white;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 32px;
  padding: 0 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid rgb(0 0 0 / 0.12);
  background: white;
  color: rgb(0 0 0 / 0.75);
  transition:
    background-color 140ms ease,
    color 140ms ease,
    border-color 140ms ease;
}
.action-btn:hover {
  background: rgb(0 0 0 / 0.04);
  border-color: rgb(0 0 0 / 0.4);
  color: black;
}
:where(html.dark) .action-btn {
  background: black;
  border-color: rgb(255 255 255 / 0.12);
  color: rgb(255 255 255 / 0.75);
}
:where(html.dark) .action-btn:hover {
  background: rgb(255 255 255 / 0.06);
  border-color: rgb(255 255 255 / 0.4);
  color: white;
}

/* 缩放滑块（无品牌色，纯黑/白） */
.zoom-slider {
  appearance: none;
  height: 4px;
  border-radius: 999px;
  background: rgb(0 0 0 / 0.12);
  outline: none;
}
:where(html.dark) .zoom-slider {
  background: rgb(255 255 255 / 0.12);
}
.zoom-slider::-webkit-slider-thumb {
  appearance: none;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: black;
  border: 2px solid white;
  cursor: pointer;
  box-shadow: 0 1px 2px rgb(0 0 0 / 0.3);
}
:where(html.dark) .zoom-slider::-webkit-slider-thumb {
  background: white;
  border-color: black;
}
.zoom-slider::-moz-range-thumb {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: black;
  border: 2px solid white;
  cursor: pointer;
}
:where(html.dark) .zoom-slider::-moz-range-thumb {
  background: white;
  border-color: black;
}

/* vue-cropper 内部样式覆写：去除蓝色品牌色，统一为白色描边 */
.cropper-stage :deep(.vue-cropper) {
  background-color: black;
}
.cropper-stage :deep(.cropper-view-box) {
  outline: 1px solid rgb(255 255 255 / 0.85);
  outline-color: rgb(255 255 255 / 0.85);
}
.cropper-stage :deep(.crop-point) {
  background-color: white;
  opacity: 0.9;
}
.cropper-stage :deep(.cropper-modal) {
  background: rgba(0, 0, 0, 0.65);
}
.cropper-stage :deep(.crop-info) {
  background-color: rgb(0 0 0 / 0.75);
  font-variant-numeric: tabular-nums;
}
</style>

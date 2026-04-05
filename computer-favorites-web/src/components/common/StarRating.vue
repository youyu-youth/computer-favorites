<script setup lang="ts">
import { computed } from 'vue'
import { Star, StarHalf } from 'lucide-vue-next'

const props = defineProps({
  score: {
    type: Number,
    required: true,
    default: 0
  },
  maxScore: {
    type: Number,
    default: 5
  },
  sizeClass: {
    type: String,
    default: 'w-4 h-4'
  },
  activeColorClass: {
    type: String,
    default: 'text-amber-400'
  },
  inactiveColorClass: {
    type: String,
    default: 'text-gray-300 dark:text-gray-600'
  }
})

// Calculate the number of full, half, and empty stars
const stars = computed(() => {
  const result = []
  const currentScore = Math.max(0, Math.min(props.score, props.maxScore))

  for (let i = 1; i <= props.maxScore; i++) {
    if (currentScore >= i) {
      result.push('full')
    } else if (currentScore >= i - 0.5) {
      result.push('half')
    } else {
      result.push('empty')
    }
  }
  return result
})
</script>

<template>
  <div class="flex items-center gap-1" :title="`${score} / ${maxScore}`">
    <template v-for="(type, index) in stars" :key="index">
      <Star
        v-if="type === 'full'"
        :class="[sizeClass, activeColorClass]"
        :fill="activeColorClass.includes('text-') ? 'currentColor' : ''"
      />
      <div v-else-if="type === 'half'" class="relative" :class="sizeClass">
        <Star :class="[sizeClass, inactiveColorClass]" />
        <div class="absolute inset-0 overflow-hidden w-1/2">
          <Star :class="[sizeClass, activeColorClass]" fill="currentColor" />
        </div>
      </div>
      <Star
        v-else
        :class="[sizeClass, inactiveColorClass]"
      />
    </template>
  </div>
</template>

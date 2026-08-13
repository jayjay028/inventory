<template>
  <div v-if="overlay" class="loading-overlay">
    <div class="spinner-border" :class="spinnerClass" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
  <div v-else class="d-flex justify-content-center align-items-center py-4">
    <div class="spinner-border" :class="spinnerClass" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  overlay: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'md',
    validator: (val) => ['sm', 'md', 'lg'].includes(val)
  }
})

const spinnerClass = computed(() => {
  const classes = ['text-primary']
  if (props.size === 'sm') classes.push('spinner-border-sm')
  if (props.size === 'lg') classes.push('spinner-lg')
  return classes
})
</script>

<style scoped>
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1080;
}

.spinner-lg {
  width: 3rem;
  height: 3rem;
}
</style>

<template>
  <Teleport to="body">
    <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 1090;">
      <transition name="toast-slide">
        <div
          v-if="appStore.toast.show"
          class="toast show align-items-center border-0"
          :class="toastClass"
          role="alert"
          aria-live="assertive"
          aria-atomic="true"
        >
          <div class="d-flex">
            <div class="toast-body d-flex align-items-center gap-2">
              <i :class="['bi', toastIcon]"></i>
              <span>{{ appStore.toast.message }}</span>
            </div>
            <button
              type="button"
              class="btn-close btn-close-white me-2 m-auto"
              aria-label="Close"
              @click="appStore.hideToast()"
            ></button>
          </div>
        </div>
      </transition>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const toastClass = computed(() => {
  const typeMap = {
    success: 'text-bg-success',
    error: 'text-bg-danger',
    warning: 'text-bg-warning',
    info: 'text-bg-info'
  }
  return typeMap[appStore.toast.type] || 'text-bg-success'
})

const toastIcon = computed(() => {
  const iconMap = {
    success: 'bi-check-circle-fill',
    error: 'bi-exclamation-triangle-fill',
    warning: 'bi-exclamation-circle-fill',
    info: 'bi-info-circle-fill'
  }
  return iconMap[appStore.toast.type] || 'bi-check-circle-fill'
})
</script>

<style scoped>
.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.toast-slide-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.toast-slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>

<template>
  <Teleport to="body">
    <div v-if="show" class="modal-backdrop fade show" @click="handleCancel"></div>
    <div
      v-if="show"
      class="modal fade show d-block"
      tabindex="-1"
      role="dialog"
      aria-modal="true"
      @click.self="handleCancel"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ title }}</h5>
            <button
              type="button"
              class="btn-close"
              aria-label="Close"
              @click="handleCancel"
            ></button>
          </div>
          <div class="modal-body">
            <p class="mb-0">{{ message }}</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="handleCancel">
              {{ cancelText }}
            </button>
            <button type="button" :class="['btn', `btn-${variant}`]" @click="handleConfirm">
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({
  show: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: 'Confirm Action'
  },
  message: {
    type: String,
    default: 'Are you sure you want to proceed?'
  },
  confirmText: {
    type: String,
    default: 'Confirm'
  },
  cancelText: {
    type: String,
    default: 'Cancel'
  },
  variant: {
    type: String,
    default: 'danger'
  }
})

const emit = defineEmits(['confirm', 'cancel'])

function handleConfirm() {
  emit('confirm')
}

function handleCancel() {
  emit('cancel')
}
</script>

<style scoped>
.modal-backdrop {
  z-index: 1050;
}

.modal {
  z-index: 1055;
}
</style>

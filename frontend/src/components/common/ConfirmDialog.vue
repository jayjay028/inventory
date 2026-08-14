<template>
  <Teleport to="body">
    <div v-if="show" class="cd-backdrop" @click="handleCancel"></div>
    <div
      v-if="show"
      class="cd-overlay"
      tabindex="-1"
      role="dialog"
      aria-modal="true"
      @click.self="handleCancel"
    >
      <div class="cd-modal">
        <!-- Icon -->
        <div class="cd-icon-wrap" :class="`cd-icon-${variant}`">
          <i v-if="variant === 'danger'" class="bi bi-exclamation-triangle"></i>
          <i v-else-if="variant === 'warning'" class="bi bi-exclamation-circle"></i>
          <i v-else class="bi bi-question-circle"></i>
        </div>

        <!-- Content -->
        <h3 class="cd-title">{{ title }}</h3>
        <p class="cd-message">{{ message }}</p>

        <!-- Actions -->
        <div class="cd-actions">
          <button type="button" class="cd-btn cd-btn-cancel" @click="handleCancel">
            {{ cancelText }}
          </button>
          <button type="button" class="cd-btn" :class="`cd-btn-${variant}`" @click="handleConfirm">
            {{ confirmText }}
          </button>
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
.cd-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  z-index: 1050;
  animation: cd-fade-in 0.15s ease;
}

.cd-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1055;
  padding: 1rem;
  animation: cd-fade-in 0.15s ease;
}

.cd-modal {
  background: #fff;
  border-radius: 12px;
  padding: 2rem;
  max-width: 400px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.12), 0 4px 16px rgba(0, 0, 0, 0.08);
  animation: cd-slide-up 0.2s ease;
}

/* Icon */
.cd-icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-bottom: 1rem;
  font-size: 1.25rem;
}

.cd-icon-danger {
  background: #fef2f2;
  color: #dc2626;
}

.cd-icon-warning {
  background: #fffbeb;
  color: #d97706;
}

.cd-icon-primary {
  background: #eff6ff;
  color: #1e40af;
}

/* Content */
.cd-title {
  font-size: 1.0625rem;
  font-weight: 600;
  color: #111827;
  margin: 0 0 0.5rem;
}

.cd-message {
  font-size: 0.8125rem;
  color: #6b7280;
  margin: 0 0 1.5rem;
  line-height: 1.5;
}

/* Actions */
.cd-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
}

.cd-btn {
  padding: 0.5rem 1.25rem;
  font-size: 0.8125rem;
  font-weight: 500;
  border-radius: 6px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.15s ease;
  font-family: inherit;
  line-height: 1.4;
}

.cd-btn-cancel {
  background: #fff;
  color: #374151;
  border-color: #d1d5db;
}

.cd-btn-cancel:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.cd-btn-danger {
  background: #dc2626;
  color: #fff;
}

.cd-btn-danger:hover {
  background: #b91c1c;
}

.cd-btn-warning {
  background: #d97706;
  color: #fff;
}

.cd-btn-warning:hover {
  background: #b45309;
}

.cd-btn-primary {
  background: #1e40af;
  color: #fff;
}

.cd-btn-primary:hover {
  background: #1e3a8a;
}

/* Animations */
@keyframes cd-fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes cd-slide-up {
  from {
    opacity: 0;
    transform: translateY(8px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>

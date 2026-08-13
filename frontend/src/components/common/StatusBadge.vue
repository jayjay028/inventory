<template>
  <span class="badge" :class="badgeClass">{{ displayStatus }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    default: ''
  }
})

const badgeClass = computed(() => {
  const normalized = (props.status || '').toUpperCase().replace(/\s+/g, '_')

  const successStatuses = ['ACTIVE', 'APPROVED', 'PAID', 'CLOSED', 'COMPLETED']
  const warningStatuses = ['OPEN', 'CREATED', 'PENDING', 'LOW']
  const dangerStatuses = ['INACTIVE', 'CANCELLED', 'VOIDED', 'OUT_OF_STOCK']

  if (successStatuses.includes(normalized)) return 'text-bg-success'
  if (warningStatuses.includes(normalized)) return 'text-bg-warning'
  if (dangerStatuses.includes(normalized)) return 'text-bg-danger'
  if (normalized === 'NORMAL') return 'text-bg-primary'

  return 'text-bg-secondary'
})

const displayStatus = computed(() => {
  if (!props.status) return ''
  return props.status.replace(/_/g, ' ')
})
</script>

<style scoped>
.badge {
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: capitalize;
}
</style>

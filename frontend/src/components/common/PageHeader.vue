<template>
  <div class="page-header mb-4">
    <!-- Breadcrumbs -->
    <nav v-if="breadcrumbs && breadcrumbs.length" aria-label="breadcrumb">
      <ol class="breadcrumb mb-2">
        <li
          v-for="(crumb, index) in breadcrumbs"
          :key="index"
          class="breadcrumb-item"
          :class="{ active: index === breadcrumbs.length - 1 }"
        >
          <router-link v-if="crumb.route && index < breadcrumbs.length - 1" :to="crumb.route">
            {{ crumb.label }}
          </router-link>
          <span v-else>{{ crumb.label }}</span>
        </li>
      </ol>
    </nav>

    <!-- Title row -->
    <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
      <div>
        <h3 class="mb-1">{{ title }}</h3>
        <p v-if="subtitle" class="text-muted mb-0">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.actions" class="d-flex gap-2 flex-wrap">
        <slot name="actions"></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  breadcrumbs: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.page-header {
  padding-bottom: 1rem;
  border-bottom: 1px solid #e9ecef;
}
</style>

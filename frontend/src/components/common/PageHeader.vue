<template>
  <div class="ph-wrapper">
    <!-- Breadcrumbs -->
    <nav v-if="breadcrumbs && breadcrumbs.length" aria-label="breadcrumb" class="ph-breadcrumbs">
      <ol>
        <li
          v-for="(crumb, index) in breadcrumbs"
          :key="index"
          :class="{ 'ph-crumb-active': index === breadcrumbs.length - 1 }"
        >
          <router-link v-if="crumb.route && index < breadcrumbs.length - 1" :to="crumb.route">
            {{ crumb.label }}
          </router-link>
          <span v-else>{{ crumb.label }}</span>
        </li>
      </ol>
    </nav>

    <!-- Title row -->
    <div class="ph-title-row">
      <div class="ph-title-block">
        <h1 class="ph-title">{{ title }}</h1>
        <p v-if="subtitle" class="ph-subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.actions" class="ph-actions">
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
.ph-wrapper {
  margin-bottom: 1.5rem;
}

/* Breadcrumbs */
.ph-breadcrumbs ol {
  display: flex;
  align-items: center;
  list-style: none;
  padding: 0;
  margin: 0 0 0.5rem;
  gap: 0;
}

.ph-breadcrumbs li {
  display: inline-flex;
  align-items: center;
  font-size: 0.75rem;
  color: #6b7280;
}

.ph-breadcrumbs li + li::before {
  content: '/';
  margin: 0 0.5rem;
  color: #d1d5db;
  font-size: 0.6875rem;
}

.ph-breadcrumbs li a {
  color: #6b7280;
  text-decoration: none;
  transition: color 0.15s ease;
}

.ph-breadcrumbs li a:hover {
  color: #1e40af;
}

.ph-crumb-active {
  color: #374151;
  font-weight: 500;
}

/* Title row */
.ph-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.ph-title-block {
  min-width: 0;
}

.ph-title {
  font-size: 1.375rem;
  font-weight: 600;
  color: #111827;
  margin: 0;
  line-height: 1.3;
}

.ph-subtitle {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0.25rem 0 0;
  line-height: 1.4;
}

.ph-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
  flex-shrink: 0;
}
</style>

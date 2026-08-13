<template>
  <nav class="navbar navbar-dark bg-primary sticky-top px-3">
    <div class="d-flex align-items-center">
      <button
        class="btn btn-link text-white me-3 p-0 border-0"
        type="button"
        aria-label="Toggle sidebar"
        @click="appStore.toggleSidebar()"
      >
        <i class="bi bi-list fs-4"></i>
      </button>
      <span class="navbar-brand mb-0 fw-semibold">Inventory + POS</span>
    </div>

    <div class="dropdown">
      <button
        class="btn btn-link text-white text-decoration-none dropdown-toggle d-flex align-items-center gap-2"
        type="button"
        data-bs-toggle="dropdown"
        aria-expanded="false"
      >
        <i class="bi bi-person-circle fs-5"></i>
        <span class="d-none d-sm-inline">{{ authStore.userName }}</span>
      </button>
      <ul class="dropdown-menu dropdown-menu-end">
        <li>
          <span class="dropdown-item-text text-muted small">
            {{ authStore.userRole }}
          </span>
        </li>
        <li><hr class="dropdown-divider" /></li>
        <li>
          <button class="dropdown-item text-danger" type="button" @click="handleLogout">
            <i class="bi bi-box-arrow-right me-2"></i>Logout
          </button>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'

const authStore = useAuthStore()
const appStore = useAppStore()

function handleLogout() {
  authStore.logout()
}
</script>

<style scoped>
.navbar {
  z-index: 1020;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>

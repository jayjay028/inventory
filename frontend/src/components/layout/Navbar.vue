<template>
  <header class="app-navbar">
    <div class="navbar-left">
      <button
        class="navbar-toggle"
        type="button"
        aria-label="Toggle sidebar"
        @click="appStore.toggleSidebar()"
      >
        <i class="bi bi-list"></i>
      </button>
      <div class="navbar-context">
        <span class="navbar-app-name">Inventory + POS</span>
      </div>
    </div>

    <div class="navbar-right">
      <div class="navbar-user" ref="dropdownRef">
        <button
          class="user-trigger"
          type="button"
          @click="toggleDropdown"
          :aria-expanded="dropdownOpen"
        >
          <span class="user-avatar">
            {{ userInitials }}
          </span>
          <span class="user-name d-none d-sm-inline">{{ authStore.userName }}</span>
          <i class="bi bi-chevron-down user-chevron"></i>
        </button>

        <transition name="dropdown-fade">
          <div v-if="dropdownOpen" class="user-dropdown">
            <div class="dropdown-user-info">
              <span class="dropdown-user-name">{{ authStore.userName }}</span>
              <span class="dropdown-user-role">{{ authStore.userRole }}</span>
            </div>
            <div class="dropdown-divider"></div>
            <button class="dropdown-action logout-action" type="button" @click="handleLogout">
              <i class="bi bi-box-arrow-right"></i>
              <span>Sign out</span>
            </button>
          </div>
        </transition>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'

const authStore = useAuthStore()
const appStore = useAppStore()

const dropdownOpen = ref(false)
const dropdownRef = ref(null)

const userInitials = computed(() => {
  const name = authStore.userName || ''
  const parts = name.trim().split(/\s+/)
  if (parts.length >= 2) {
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
  }
  return name.substring(0, 2).toUpperCase()
})

function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value
}

function handleClickOutside(event) {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    dropdownOpen.value = false
  }
}

function handleLogout() {
  dropdownOpen.value = false
  authStore.logout()
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.app-navbar {
  position: sticky;
  top: 0;
  z-index: 1020;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 1.25rem;
  background-color: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

/* Left section */
.navbar-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.navbar-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border: none;
  background: none;
  border-radius: 4px;
  color: #374151;
  font-size: 1.25rem;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.navbar-toggle:hover {
  background-color: #f3f4f6;
}

.navbar-context {
  display: flex;
  align-items: center;
}

.navbar-app-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #111827;
}

/* Right section */
.navbar-right {
  display: flex;
  align-items: center;
}

.navbar-user {
  position: relative;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.5rem 0.25rem 0.25rem;
  border: none;
  background: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.user-trigger:hover {
  background-color: #f3f4f6;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  background-color: #1e40af;
  color: #ffffff;
  font-size: 0.6875rem;
  font-weight: 600;
  letter-spacing: 0.025em;
}

.user-name {
  font-size: 0.8125rem;
  font-weight: 500;
  color: #374151;
}

.user-chevron {
  font-size: 0.625rem;
  color: #6b7280;
  transition: transform 0.15s ease;
}

.user-trigger[aria-expanded="true"] .user-chevron {
  transform: rotate(180deg);
}

/* Dropdown */
.user-dropdown {
  position: absolute;
  top: calc(100% + 0.5rem);
  right: 0;
  width: 200px;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1), 0 1px 3px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  z-index: 1050;
}

.dropdown-user-info {
  padding: 0.75rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}

.dropdown-user-name {
  font-size: 0.8125rem;
  font-weight: 600;
  color: #111827;
}

.dropdown-user-role {
  font-size: 0.6875rem;
  color: #6b7280;
  text-transform: capitalize;
}

.dropdown-divider {
  height: 1px;
  background-color: #f3f4f6;
  margin: 0;
}

.dropdown-action {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.625rem 1rem;
  border: none;
  background: none;
  font-size: 0.8125rem;
  color: #374151;
  cursor: pointer;
  transition: background-color 0.15s ease;
  text-align: left;
}

.dropdown-action:hover {
  background-color: #f9fafb;
}

.logout-action {
  color: #dc2626;
}

.logout-action:hover {
  background-color: #fef2f2;
}

.logout-action i {
  font-size: 0.875rem;
}

/* Dropdown animation */
.dropdown-fade-enter-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.dropdown-fade-leave-active {
  transition: opacity 0.1s ease, transform 0.1s ease;
}

.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* Responsive */
@media (max-width: 575.98px) {
  .navbar-app-name {
    font-size: 0.8125rem;
  }
}
</style>

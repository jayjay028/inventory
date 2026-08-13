<template>
  <aside class="sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
    <div class="sidebar-brand d-flex align-items-center px-3 py-3">
      <i class="bi bi-box-seam-fill fs-4 text-primary me-2"></i>
      <span class="brand-text fw-bold">Inventory</span>
    </div>

    <nav class="sidebar-nav">
      <ul class="nav flex-column">
        <template v-for="item in filteredNavigation" :key="item.label">
          <!-- Single link (no children) -->
          <li v-if="!item.children" class="nav-item">
            <router-link
              :to="item.route"
              class="nav-link d-flex align-items-center gap-2"
              active-class="active"
            >
              <i :class="['bi', item.icon]"></i>
              <span>{{ item.label }}</span>
            </router-link>
          </li>

          <!-- Group with children -->
          <li v-else class="nav-item nav-group">
            <button
              class="nav-link nav-group-toggle d-flex align-items-center gap-2 w-100 border-0 bg-transparent"
              type="button"
              :class="{ open: expandedGroups[item.label] }"
              @click="toggleGroup(item.label)"
            >
              <i :class="['bi', item.icon]"></i>
              <span class="flex-grow-1 text-start">{{ item.label }}</span>
              <i class="bi bi-chevron-down chevron-icon"></i>
            </button>
            <transition name="slide">
              <ul v-show="expandedGroups[item.label]" class="nav flex-column nav-children">
                <li v-for="child in item.children" :key="child.label" class="nav-item">
                  <router-link
                    :to="child.route"
                    class="nav-link d-flex align-items-center gap-2 ps-4"
                    active-class="active"
                  >
                    <i class="bi bi-dot"></i>
                    <span>{{ child.label }}</span>
                  </router-link>
                </li>
              </ul>
            </transition>
          </li>
        </template>
      </ul>
    </nav>
  </aside>

  <!-- Backdrop for mobile -->
  <div
    v-if="!appStore.sidebarCollapsed"
    class="sidebar-backdrop d-lg-none"
    @click="appStore.toggleSidebar()"
  ></div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'

const authStore = useAuthStore()
const appStore = useAppStore()

const expandedGroups = ref({})

const navigation = [
  { label: 'Dashboard', icon: 'bi-speedometer2', route: '/dashboard', permission: 0 },
  { label: 'POS Terminal', icon: 'bi-cart-check', route: '/pos', permission: 14 },
  {
    label: 'Inventory',
    icon: 'bi-box-seam',
    children: [
      { label: 'Items', route: '/items', permission: 1 },
      { label: 'Categories', route: '/categories', permission: 3 },
      { label: 'Stock Overview', route: '/stock', permission: 9 },
      { label: 'Stock In', route: '/stock/in', permission: 10 },
      { label: 'Stock Out', route: '/stock/out', permission: 11 },
      { label: 'Stock Adjustment', route: '/stock/adjust', permission: 12 },
      { label: 'Transactions', route: '/transactions', permission: 13 }
    ]
  },
  {
    label: 'Sales',
    icon: 'bi-receipt',
    children: [
      { label: 'Sales History', route: '/sales', permission: 14 },
      { label: 'Shifts', route: '/shifts', permission: 16 }
    ]
  },
  {
    label: 'Contacts',
    icon: 'bi-people',
    children: [
      { label: 'Customers', route: '/customers', permission: 5 },
      { label: 'Suppliers', route: '/suppliers', permission: 7 }
    ]
  },
  { label: 'Reports', icon: 'bi-file-earmark-bar-graph', route: '/reports', permission: 17 },
  {
    label: 'Administration',
    icon: 'bi-gear',
    children: [
      { label: 'Users', route: '/users', permission: 19 },
      { label: 'Add-ons', route: '/addons', permission: 21 },
      { label: 'Settings', route: '/settings', permission: 20 },
      { label: 'Audit Trail', route: '/audit', permission: 18 }
    ]
  }
]

const filteredNavigation = computed(() => {
  return navigation
    .map((item) => {
      if (item.children) {
        const filteredChildren = item.children.filter((child) =>
          authStore.hasPermission(child.permission)
        )
        if (filteredChildren.length === 0) return null
        return { ...item, children: filteredChildren }
      }
      if (!authStore.hasPermission(item.permission)) return null
      return item
    })
    .filter(Boolean)
})

function toggleGroup(label) {
  expandedGroups.value[label] = !expandedGroups.value[label]
}
</script>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 250px;
  background-color: var(--sidebar-bg, #1e293b);
  color: #cbd5e1;
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 1030;
  transition: transform 0.3s ease;
}

.sidebar.collapsed {
  transform: translateX(-100%);
}

.sidebar-brand {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.brand-text {
  color: #f8fafc;
  font-size: 1.1rem;
}

.sidebar-nav {
  padding: 0.5rem 0;
}

.sidebar-nav .nav-link {
  color: #94a3b8;
  padding: 0.6rem 1rem;
  font-size: 0.875rem;
  border-radius: 0;
  transition: background-color 0.15s, color 0.15s;
}

.sidebar-nav .nav-link:hover {
  color: #f1f5f9;
  background-color: rgba(255, 255, 255, 0.05);
}

.sidebar-nav .nav-link.active {
  color: #fff;
  background-color: var(--bs-primary, #0d6efd);
}

.nav-group-toggle {
  cursor: pointer;
}

.nav-group-toggle .chevron-icon {
  font-size: 0.7rem;
  transition: transform 0.2s ease;
}

.nav-group-toggle.open .chevron-icon {
  transform: rotate(180deg);
}

.nav-children {
  background-color: rgba(0, 0, 0, 0.15);
}

.nav-children .nav-link {
  padding-left: 2.5rem;
  font-size: 0.8125rem;
}

.slide-enter-active,
.slide-leave-active {
  transition: max-height 0.25s ease, opacity 0.2s ease;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
}

.slide-enter-to,
.slide-leave-from {
  max-height: 500px;
  opacity: 1;
}

.sidebar-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1025;
}

@media (max-width: 991.98px) {
  .sidebar {
    transform: translateX(-100%);
  }

  .sidebar:not(.collapsed) {
    transform: translateX(0);
  }
}
</style>

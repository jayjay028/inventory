<template>
  <aside class="sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
    <!-- Brand -->
    <div class="sidebar-brand">
      <img src="/maracoder-logo.png" alt="MaraCoder" class="brand-logo" />
      <span class="brand-text">MaraCoder</span>
    </div>

    <!-- Navigation -->
    <nav class="sidebar-nav">
      <ul class="nav-list">
        <template v-for="item in filteredNavigation" :key="item.label">
          <!-- Single link (no children) -->
          <li v-if="!item.children" class="nav-item">
            <router-link
              :to="item.route"
              class="nav-link"
              active-class="active"
            >
              <i :class="['bi', item.icon, 'nav-icon']"></i>
              <span class="nav-label">{{ item.label }}</span>
            </router-link>
          </li>

          <!-- Group with children -->
          <li v-else class="nav-item nav-group">
            <div class="nav-group-header">{{ item.label }}</div>
            <button
              class="nav-group-toggle"
              type="button"
              :class="{ open: expandedGroups[item.label] }"
              @click="toggleGroup(item.label)"
            >
              <i :class="['bi', item.icon, 'nav-icon']"></i>
              <span class="nav-label">{{ item.label }}</span>
              <i class="bi bi-chevron-right chevron-icon"></i>
            </button>
            <transition name="slide">
              <ul v-show="expandedGroups[item.label]" class="nav-children">
                <li v-for="child in item.children" :key="child.label" class="nav-item">
                  <router-link
                    :to="child.route"
                    class="nav-link child-link"
                    active-class="active"
                  >
                    <span class="nav-label">{{ child.label }}</span>
                  </router-link>
                </li>
              </ul>
            </transition>
          </li>
        </template>
      </ul>
    </nav>

    <!-- Recent Transactions -->
    <div v-if="authStore.hasPermission(13)" class="sidebar-transactions">
      <div class="transactions-header">
        <span class="transactions-title">Recent Transactions</span>
        <router-link to="/transactions" class="transactions-link">
          <i class="bi bi-arrow-right-short"></i>
        </router-link>
      </div>
      <div v-if="loadingTransactions" class="transactions-loading">
        <span class="spinner-border spinner-border-sm"></span>
      </div>
      <ul v-else class="transactions-list">
        <li
          v-for="tx in recentTransactions"
          :key="tx.id"
          class="transaction-item"
        >
          <div class="tx-row">
            <span class="tx-status-dot" :class="'dot-' + (tx.status || 'created').toLowerCase()"></span>
            <span class="tx-doc-number">{{ tx.documentNumber || tx.id }}</span>
            <span class="tx-time">{{ formatDate(tx.createdAt || tx.transactionDate) }}</span>
          </div>
        </li>
        <li v-if="recentTransactions.length === 0" class="transaction-empty">
          No recent transactions
        </li>
      </ul>
    </div>

    <!-- Register / Shift widget (static, md+ only) -->
    <div class="register-widget d-none d-md-block">
      <div class="register-widget-header">
        <i class="bi bi-hdd-network register-widget-icon"></i>
        <span class="register-widget-title">Register #01</span>
      </div>
      <div class="register-widget-status">Shift active</div>
      <div class="register-widget-progress">
        <div class="register-widget-progress-fill"></div>
      </div>
    </div>
  </aside>

  <!-- Backdrop for mobile -->
  <div
    v-if="!appStore.sidebarCollapsed"
    class="sidebar-backdrop d-lg-none"
    @click="appStore.toggleSidebar()"
  ></div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import transactionsApi from '@/api/transactions'

const authStore = useAuthStore()
const appStore = useAppStore()
const route = useRoute()

const expandedGroups = ref({})
const recentTransactions = ref([])
const loadingTransactions = ref(false)

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
      { label: 'Roles', route: '/roles', permission: 19 },
      { label: 'Stores', route: '/stores', permission: 20 },
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

/**
 * Auto-expand the navigation group that contains the currently active route,
 * so the active child is always visible (real-system behavior).
 */
function expandActiveGroup(path) {
  navigation.forEach((item) => {
    if (item.children && item.children.some((c) => path.startsWith(c.route))) {
      expandedGroups.value[item.label] = true
    }
  })
}

// Expand the active group on load and whenever the route changes.
watch(
  () => route.path,
  (path) => {
    expandActiveGroup(path)
    // On small screens, collapse the sidebar after navigating (drawer behavior).
    if (window.innerWidth < 992 && !appStore.sidebarCollapsed) {
      appStore.toggleSidebar()
    }
  },
  { immediate: true }
)

function getTransactionIcon(type) {
  if (!type) return 'bi-arrow-left-right'
  const t = type.toUpperCase()
  if (t.includes('IN') || t === 'STOCK_IN') return 'bi-box-arrow-in-down'
  if (t.includes('OUT') || t === 'STOCK_OUT') return 'bi-box-arrow-up'
  if (t.includes('ADJ') || t === 'ADJUSTMENT') return 'bi-sliders'
  return 'bi-arrow-left-right'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)

  if (diffMins < 1) return 'Just now'
  if (diffMins < 60) return `${diffMins}m ago`

  const diffHours = Math.floor(diffMins / 60)
  if (diffHours < 24) return `${diffHours}h ago`

  const diffDays = Math.floor(diffHours / 24)
  if (diffDays < 7) return `${diffDays}d ago`

  return date.toLocaleDateString('en-PH', { month: 'short', day: 'numeric' })
}

async function loadRecentTransactions() {
  if (!authStore.hasPermission(13)) return
  loadingTransactions.value = true
  try {
    const { data } = await transactionsApi.getAll({ page: 0, size: 8, sort: 'createdAt,desc' })
    const payload = data.data || data
    recentTransactions.value = payload.content || payload || []
  } catch (error) {
    // Silently fail - sidebar shouldn't break if API fails
    recentTransactions.value = []
  } finally {
    loadingTransactions.value = false
  }
}

let refreshInterval = null

onMounted(() => {
  loadRecentTransactions()
  // Refresh every 60 seconds
  refreshInterval = setInterval(loadRecentTransactions, 60000)
})

onBeforeUnmount(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
})
</script>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 250px;
  background-color: var(--color-sidebar-bg);
  color: var(--color-sidebar-text);
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 1030;
  transition: transform 0.2s ease;
  display: flex;
  flex-direction: column;
  scrollbar-width: thin;
  scrollbar-color: #1e293b #0f172a;
}

.sidebar::-webkit-scrollbar {
  width: 5px;
}

.sidebar::-webkit-scrollbar-track {
  background: var(--color-sidebar-bg);
}

.sidebar::-webkit-scrollbar-thumb {
  background: #1e293b;
  border-radius: 3px;
}

.sidebar.collapsed {
  transform: translateX(-100%);
}

/* Brand */
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  padding: 1.125rem 1.25rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.brand-icon {
  font-size: 1.25rem;
  color: #22d3ee;
}

.brand-logo {
  width: 26px;
  height: 26px;
  border-radius: 7px;
  object-fit: cover;
  background: #fff;
  padding: 1px;
}

.brand-text {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #f1f5f9;
  letter-spacing: -0.01em;
}

/* Navigation */
.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 0.75rem 0;
  scrollbar-width: thin;
  scrollbar-color: #1e293b #0f172a;
}

.sidebar-nav::-webkit-scrollbar {
  width: 5px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: var(--color-sidebar-bg);
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: #1e293b;
  border-radius: 3px;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin: 1px 0;
}

.nav-group {
  margin-top: 0.5rem;
}

.nav-group-header {
  display: none;
}

/* Nav links */
.nav-link {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  margin: 2px 10px;
  padding: 0.55rem 0.85rem;
  border-radius: 10px;
  color: var(--color-sidebar-text);
  text-decoration: none;
  font-size: 0.8125rem;
  font-weight: 400;
  border-left: 2px solid transparent;
  transition: color 0.15s ease, background-color 0.15s ease;
  cursor: pointer;
}

.nav-link:hover {
  color: #e2e8f0;
  background-color: rgba(255, 255, 255, 0.05);
}

.nav-link.active {
  color: #7dd3fc;
  font-weight: 500;
  border-left-color: transparent;
  background-color: rgba(37, 99, 235, 0.18);
}

.nav-icon {
  font-size: 0.9375rem;
  width: 1.25rem;
  text-align: center;
  flex-shrink: 0;
  opacity: 0.7;
}

.nav-link.active .nav-icon {
  opacity: 1;
  color: #7dd3fc;
}

.nav-label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Group toggle */
.nav-group-toggle {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  width: calc(100% - 20px);
  margin: 2px 10px;
  padding: 0.55rem 0.85rem;
  border-radius: 10px;
  color: var(--color-sidebar-text);
  font-size: 0.8125rem;
  font-weight: 400;
  background: none;
  border: none;
  border-left: 2px solid transparent;
  cursor: pointer;
  text-align: left;
  transition: color 0.15s ease, background-color 0.15s ease;
}

.nav-group-toggle:hover {
  color: #e2e8f0;
  background-color: rgba(255, 255, 255, 0.05);
}

.chevron-icon {
  font-size: 0.625rem;
  margin-left: auto;
  transition: transform 0.2s ease;
  opacity: 0.5;
}

.nav-group-toggle.open .chevron-icon {
  transform: rotate(90deg);
}

/* Children */
.nav-children {
  list-style: none;
  padding: 0.125rem 0 0.375rem 0;
  margin: 0;
}

.child-link {
  padding: 0.4rem 0.85rem 0.4rem 2.25rem;
  font-size: 0.8125rem;
}

.child-link.active {
  color: #7dd3fc;
  font-weight: 500;
  border-left-color: transparent;
  background-color: rgba(37, 99, 235, 0.18);
}

/* Transitions */
.slide-enter-active,
.slide-leave-active {
  transition: max-height 0.2s ease, opacity 0.15s ease;
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

/* Recent Transactions */
.sidebar-transactions {
  flex-shrink: 0;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  max-height: 240px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #1e293b #0f172a;
}

.sidebar-transactions::-webkit-scrollbar {
  width: 5px;
}

.sidebar-transactions::-webkit-scrollbar-track {
  background: var(--color-sidebar-bg);
}

.sidebar-transactions::-webkit-scrollbar-thumb {
  background: #1e293b;
  border-radius: 3px;
}

.transactions-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1.25rem 0.5rem;
  position: sticky;
  top: 0;
  background-color: var(--color-sidebar-bg);
  z-index: 1;
}

.transactions-title {
  font-size: 0.6875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-sidebar-text);
}

.transactions-link {
  color: var(--color-sidebar-text);
  text-decoration: none;
  font-size: 1rem;
  line-height: 1;
  transition: color 0.15s ease;
}

.transactions-link:hover {
  color: var(--color-sidebar-text);
}

.transactions-loading {
  text-align: center;
  padding: 1rem 0;
}

.transactions-loading .spinner-border {
  width: 0.875rem;
  height: 0.875rem;
  border-width: 2px;
  color: #475569;
}

.transactions-list {
  list-style: none;
  padding: 0 0 0.5rem;
  margin: 0;
}

.transaction-item {
  padding: 0.375rem 1.25rem;
  transition: background-color 0.15s ease;
}

.transaction-item:hover {
  background-color: rgba(255, 255, 255, 0.02);
}

.tx-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tx-status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot-created,
.dot-pending {
  background-color: #f59e0b;
}

.dot-approved,
.dot-completed {
  background-color: #10b981;
}

.dot-cancelled,
.dot-void {
  background-color: #ef4444;
}

.tx-doc-number {
  font-family: 'JetBrains Mono', 'SF Mono', 'Fira Code', monospace;
  font-size: 0.6875rem;
  color: #cbd5e1;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tx-time {
  font-size: 0.625rem;
  color: #475569;
  flex-shrink: 0;
  white-space: nowrap;
}

.transaction-empty {
  padding: 0.75rem 1.25rem;
  font-size: 0.6875rem;
  color: #475569;
  text-align: center;
}

/* Register / Shift widget */
.register-widget {
  flex-shrink: 0;
  margin: 0.75rem;
  padding: 0.75rem;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.28), #1e293b);
  border: 1px solid rgba(6, 182, 212, 0.25);
  border-radius: 12px;
}

.register-widget-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.register-widget-icon {
  font-size: 0.9375rem;
  color: #22d3ee;
}

.register-widget-title {
  font-size: 0.8125rem;
  font-weight: 600;
  color: #7dd3fc;
}

.register-widget-status {
  margin-top: 0.25rem;
  font-size: 0.6875rem;
  color: var(--color-sidebar-text);
}

.register-widget-progress {
  margin-top: 0.5rem;
  height: 4px;
  border-radius: 999px;
  background-color: rgba(148, 163, 184, 0.2);
  overflow: hidden;
}

.register-widget-progress-fill {
  height: 100%;
  width: 60%;
  border-radius: 999px;
  background: linear-gradient(90deg, #2563eb, #06b6d4);
}

/* Backdrop */
.sidebar-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  z-index: 1025;
}

/* Responsive */
@media (max-width: 991.98px) {
  .sidebar {
    transform: translateX(-100%);
  }

  .sidebar:not(.collapsed) {
    transform: translateX(0);
  }
}
</style>

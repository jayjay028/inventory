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

    <!-- Recent Transactions -->
    <div v-if="authStore.hasPermission(13)" class="sidebar-transactions">
      <div class="sidebar-section-header d-flex align-items-center justify-content-between px-3 py-2">
        <span class="small fw-bold text-uppercase">Recent Transactions</span>
        <router-link to="/transactions" class="text-decoration-none">
          <i class="bi bi-arrow-right-circle small"></i>
        </router-link>
      </div>
      <div v-if="loadingTransactions" class="text-center py-2">
        <span class="spinner-border spinner-border-sm text-secondary"></span>
      </div>
      <ul v-else class="transaction-list">
        <li
          v-for="tx in recentTransactions"
          :key="tx.id"
          class="transaction-item px-3 py-2"
        >
          <div class="d-flex align-items-center gap-2">
            <i :class="['bi', getTransactionIcon(tx.type)]" class="tx-icon"></i>
            <div class="flex-grow-1 min-width-0">
              <div class="tx-code text-truncate">{{ tx.documentNumber || tx.id }}</div>
              <div class="tx-meta d-flex align-items-center gap-1">
                <span :class="['tx-badge', 'tx-badge-' + (tx.status || 'created').toLowerCase()]">
                  {{ tx.status || 'CREATED' }}
                </span>
                <span class="tx-date">{{ formatDate(tx.createdAt || tx.transactionDate) }}</span>
              </div>
            </div>
          </div>
        </li>
        <li v-if="recentTransactions.length === 0" class="px-3 py-2 text-center">
          <small class="text-muted">No recent transactions</small>
        </li>
      </ul>
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
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import transactionsApi from '@/api/transactions'

const authStore = useAuthStore()
const appStore = useAppStore()

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

onMounted(() => {
  loadRecentTransactions()
  // Refresh every 60 seconds
  setInterval(loadRecentTransactions, 60000)
})
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
  display: flex;
  flex-direction: column;
}

.sidebar.collapsed {
  transform: translateX(-100%);
}

.sidebar-brand {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
}

.brand-text {
  color: #f8fafc;
  font-size: 1.1rem;
}

.sidebar-nav {
  padding: 0.5rem 0;
  flex: 1;
  overflow-y: auto;
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

/* Recent Transactions Section */
.sidebar-transactions {
  flex-shrink: 0;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  max-height: 280px;
  overflow-y: auto;
}

.sidebar-section-header {
  color: #64748b;
  position: sticky;
  top: 0;
  background-color: var(--sidebar-bg, #1e293b);
  z-index: 1;
}

.sidebar-section-header a {
  color: #64748b;
}

.sidebar-section-header a:hover {
  color: #94a3b8;
}

.transaction-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.transaction-item {
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: background-color 0.15s;
}

.transaction-item:hover {
  background-color: rgba(255, 255, 255, 0.03);
}

.transaction-item:last-child {
  border-bottom: none;
}

.tx-icon {
  font-size: 0.85rem;
  color: #64748b;
  flex-shrink: 0;
}

.tx-code {
  font-size: 0.75rem;
  color: #e2e8f0;
  font-family: monospace;
}

.tx-meta {
  font-size: 0.65rem;
  color: #64748b;
}

.tx-badge {
  display: inline-block;
  padding: 0.05rem 0.35rem;
  border-radius: 3px;
  font-size: 0.6rem;
  font-weight: 600;
  text-transform: uppercase;
}

.tx-badge-created { background: #854d0e; color: #fef3c7; }
.tx-badge-approved { background: #166534; color: #dcfce7; }
.tx-badge-cancelled { background: #991b1b; color: #fee2e2; }
.tx-badge-pending { background: #854d0e; color: #fef3c7; }

.tx-date {
  color: #475569;
}

.min-width-0 {
  min-width: 0;
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

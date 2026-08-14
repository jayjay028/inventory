<template>
  <aside class="right-sidebar" :class="{ open: isOpen }">
    <!-- Toggle Button (always visible on right edge) -->
    <button class="right-sidebar-toggle" @click="isOpen = !isOpen" :title="isOpen ? 'Close panel' : 'Open panel'">
      <i class="bi" :class="isOpen ? 'bi-chevron-right' : 'bi-chevron-left'"></i>
    </button>

    <div class="right-sidebar-content">
      <!-- Header -->
      <div class="rs-header">
        <span class="rs-header-title">Quick Access</span>
      </div>

      <!-- Navigation Sections -->
      <nav class="rs-nav">
        <!-- Transactions -->
        <div v-if="authStore.hasPermission(PERMISSIONS.VIEW_TRANSACTIONS)" class="rs-section">
          <button
            class="rs-section-btn"
            :class="{ active: activeSection === 'transactions' }"
            @click="toggleSection('transactions')"
          >
            <i class="bi bi-arrow-left-right rs-icon"></i>
            <span>Transactions</span>
            <i class="bi bi-chevron-down rs-chevron" :class="{ rotated: activeSection === 'transactions' }"></i>
          </button>
          <transition name="rs-expand">
            <ul v-show="activeSection === 'transactions'" class="rs-submenu">
              <li><router-link to="/transactions" @click="handleNav">All Transactions</router-link></li>
              <li v-if="authStore.hasPermission(PERMISSIONS.MANAGE_STOCK_IN)">
                <router-link to="/stock/in" @click="handleNav">Stock In</router-link>
              </li>
              <li v-if="authStore.hasPermission(PERMISSIONS.MANAGE_STOCK_OUT)">
                <router-link to="/stock/out" @click="handleNav">Stock Out</router-link>
              </li>
              <li v-if="authStore.hasPermission(PERMISSIONS.MANAGE_STOCK_ADJ)">
                <router-link to="/stock/adjust" @click="handleNav">Stock Adjustment</router-link>
              </li>
              <li v-if="authStore.hasPermission(PERMISSIONS.VIEW_STOCK)">
                <router-link to="/stock" @click="handleNav">Stock Overview</router-link>
              </li>
              <li v-if="authStore.hasPermission(PERMISSIONS.USE_POS)">
                <router-link to="/sales" @click="handleNav">Sales History</router-link>
              </li>
              <li v-if="authStore.hasPermission(PERMISSIONS.MANAGE_SHIFTS)">
                <router-link to="/shifts" @click="handleNav">Shifts</router-link>
              </li>
            </ul>
          </transition>
        </div>

        <!-- Reports -->
        <div v-if="authStore.hasPermission(PERMISSIONS.VIEW_REPORTS)" class="rs-section">
          <button
            class="rs-section-btn"
            :class="{ active: activeSection === 'reports' }"
            @click="toggleSection('reports')"
          >
            <i class="bi bi-file-earmark-bar-graph rs-icon"></i>
            <span>Reports</span>
            <i class="bi bi-chevron-down rs-chevron" :class="{ rotated: activeSection === 'reports' }"></i>
          </button>
          <transition name="rs-expand">
            <ul v-show="activeSection === 'reports'" class="rs-submenu">
              <li class="rs-submenu-group">Inventory</li>
              <li><router-link to="/reports?type=stock-level" @click="handleNav">Stock Level</router-link></li>
              <li><router-link to="/reports?type=stock-movement" @click="handleNav">Stock Movement</router-link></li>
              <li><router-link to="/reports?type=low-stock" @click="handleNav">Low Stock Alert</router-link></li>
              <li><router-link to="/reports?type=stock-valuation" @click="handleNav">Stock Valuation</router-link></li>
              <li class="rs-submenu-group">Financial</li>
              <li><router-link to="/reports?type=sales-summary" @click="handleNav">Sales Summary</router-link></li>
              <li><router-link to="/reports?type=gross-profit" @click="handleNav">Gross Profit</router-link></li>
              <li><router-link to="/reports?type=vat-summary" @click="handleNav">VAT Summary</router-link></li>
              <li class="rs-submenu-group">POS</li>
              <li><router-link to="/reports?type=daily-sales" @click="handleNav">Daily Sales (Z-Reading)</router-link></li>
              <li><router-link to="/reports?type=shift-report" @click="handleNav">Shift Report (X-Reading)</router-link></li>
              <li><router-link to="/reports?type=cashier-sales" @click="handleNav">Sales by Cashier</router-link></li>
            </ul>
          </transition>
        </div>

        <!-- Inventory -->
        <div v-if="authStore.hasPermission(PERMISSIONS.VIEW_ITEMS)" class="rs-section">
          <button
            class="rs-section-btn"
            :class="{ active: activeSection === 'inventory' }"
            @click="toggleSection('inventory')"
          >
            <i class="bi bi-box-seam rs-icon"></i>
            <span>Inventory</span>
            <i class="bi bi-chevron-down rs-chevron" :class="{ rotated: activeSection === 'inventory' }"></i>
          </button>
          <transition name="rs-expand">
            <ul v-show="activeSection === 'inventory'" class="rs-submenu">
              <li><router-link to="/items" @click="handleNav">Items</router-link></li>
              <li v-if="authStore.hasPermission(PERMISSIONS.VIEW_CATEGORIES)">
                <router-link to="/categories" @click="handleNav">Categories</router-link>
              </li>
              <li v-if="authStore.hasPermission(PERMISSIONS.VIEW_SUPPLIERS)">
                <router-link to="/suppliers" @click="handleNav">Suppliers</router-link>
              </li>
              <li v-if="authStore.hasPermission(PERMISSIONS.VIEW_CUSTOMERS)">
                <router-link to="/customers" @click="handleNav">Customers</router-link>
              </li>
            </ul>
          </transition>
        </div>

        <!-- Settings -->
        <div v-if="authStore.hasPermission(PERMISSIONS.MANAGE_SETTINGS)" class="rs-section">
          <button
            class="rs-section-btn"
            :class="{ active: activeSection === 'settings' }"
            @click="toggleSection('settings')"
          >
            <i class="bi bi-gear rs-icon"></i>
            <span>Settings</span>
            <i class="bi bi-chevron-down rs-chevron" :class="{ rotated: activeSection === 'settings' }"></i>
          </button>
          <transition name="rs-expand">
            <ul v-show="activeSection === 'settings'" class="rs-submenu">
              <li><router-link to="/settings" @click="handleNav">Company Info</router-link></li>
              <li><router-link to="/settings?tab=tax" @click="handleNav">Tax Settings</router-link></li>
              <li><router-link to="/settings?tab=numbering" @click="handleNav">Document Numbering</router-link></li>
              <li><router-link to="/settings?tab=pos" @click="handleNav">POS Settings</router-link></li>
              <li><router-link to="/settings?tab=discount" @click="handleNav">Discount Settings</router-link></li>
              <li><router-link to="/settings?tab=access" @click="handleNav">Access Rights</router-link></li>
            </ul>
          </transition>
        </div>

        <!-- Administration -->
        <div v-if="authStore.hasPermission(PERMISSIONS.MANAGE_USERS) || authStore.hasPermission(PERMISSIONS.VIEW_AUDIT_TRAIL)" class="rs-section">
          <button
            class="rs-section-btn"
            :class="{ active: activeSection === 'admin' }"
            @click="toggleSection('admin')"
          >
            <i class="bi bi-shield-lock rs-icon"></i>
            <span>Administration</span>
            <i class="bi bi-chevron-down rs-chevron" :class="{ rotated: activeSection === 'admin' }"></i>
          </button>
          <transition name="rs-expand">
            <ul v-show="activeSection === 'admin'" class="rs-submenu">
              <li v-if="authStore.hasPermission(PERMISSIONS.MANAGE_USERS)">
                <router-link to="/users" @click="handleNav">Users</router-link>
              </li>
              <li v-if="authStore.hasPermission(PERMISSIONS.MANAGE_ADDONS)">
                <router-link to="/addons" @click="handleNav">Add-ons</router-link>
              </li>
              <li v-if="authStore.hasPermission(PERMISSIONS.VIEW_AUDIT_TRAIL)">
                <router-link to="/audit" @click="handleNav">Audit Trail</router-link>
              </li>
            </ul>
          </transition>
        </div>
      </nav>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore, PERMISSIONS } from '@/stores/auth'

const authStore = useAuthStore()

const isOpen = ref(false)
const activeSection = ref('')

function toggleSection(section) {
  activeSection.value = activeSection.value === section ? '' : section
}

function handleNav() {
  // On mobile, close sidebar after navigation
  if (window.innerWidth < 992) {
    isOpen.value = false
  }
}
</script>

<style scoped>
.right-sidebar {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 260px;
  background-color: #ffffff;
  border-left: 1px solid #e5e7eb;
  z-index: 1025;
  transform: translateX(100%);
  transition: transform 0.25s ease;
  display: flex;
  flex-direction: column;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.04);
}

.right-sidebar.open {
  transform: translateX(0);
}

/* Toggle Button */
.right-sidebar-toggle {
  position: absolute;
  top: 50%;
  left: -32px;
  transform: translateY(-50%);
  width: 32px;
  height: 64px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-right: none;
  border-radius: 6px 0 0 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #6b7280;
  font-size: 0.8rem;
  transition: background 0.2s, color 0.2s;
  box-shadow: -2px 0 4px rgba(0, 0, 0, 0.04);
}

.right-sidebar-toggle:hover {
  background: #f3f4f6;
  color: #1e40af;
}

/* Content */
.right-sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* Header */
.rs-header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #f1f5f9;
}

.rs-header-title {
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #9ca3af;
}

/* Navigation */
.rs-nav {
  padding: 0.5rem 0;
}

.rs-section {
  border-bottom: 1px solid #f3f4f6;
}

.rs-section:last-child {
  border-bottom: none;
}

.rs-section-btn {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  width: 100%;
  padding: 0.75rem 1.25rem;
  background: none;
  border: none;
  font-size: 0.8125rem;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  text-align: left;
}

.rs-section-btn:hover {
  background: #f8fafc;
  color: #1e40af;
}

.rs-section-btn.active {
  color: #1e40af;
  background: #eff6ff;
}

.rs-icon {
  font-size: 1rem;
  width: 1.25rem;
  text-align: center;
  color: #6b7280;
  flex-shrink: 0;
}

.rs-section-btn.active .rs-icon {
  color: #1e40af;
}

.rs-chevron {
  margin-left: auto;
  font-size: 0.65rem;
  color: #9ca3af;
  transition: transform 0.2s ease;
}

.rs-chevron.rotated {
  transform: rotate(180deg);
}

/* Submenu */
.rs-submenu {
  list-style: none;
  padding: 0 0 0.5rem 0;
  margin: 0;
  background: #f9fafb;
}

.rs-submenu li a {
  display: block;
  padding: 0.4rem 1.25rem 0.4rem 3rem;
  font-size: 0.8rem;
  color: #4b5563;
  text-decoration: none;
  transition: color 0.15s, background 0.15s;
}

.rs-submenu li a:hover {
  color: #1e40af;
  background: #eff6ff;
}

.rs-submenu li a.router-link-active {
  color: #1e40af;
  font-weight: 500;
  background: #eff6ff;
}

.rs-submenu-group {
  padding: 0.5rem 1.25rem 0.2rem 2rem !important;
  font-size: 0.65rem !important;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #9ca3af !important;
  pointer-events: none;
}

/* Transition */
.rs-expand-enter-active,
.rs-expand-leave-active {
  transition: max-height 0.25s ease, opacity 0.2s ease;
  overflow: hidden;
}

.rs-expand-enter-from,
.rs-expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.rs-expand-enter-to,
.rs-expand-leave-from {
  max-height: 600px;
  opacity: 1;
}

/* Scrollbar */
.right-sidebar-content::-webkit-scrollbar {
  width: 4px;
}

.right-sidebar-content::-webkit-scrollbar-track {
  background: transparent;
}

.right-sidebar-content::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 4px;
}

@media (max-width: 991.98px) {
  .right-sidebar {
    width: 240px;
  }
}
</style>

<template>
  <div>
    <PageHeader
      title="Settings"
      subtitle="System configuration"
      :breadcrumbs="[{ label: 'Dashboard', route: '/dashboard' }, { label: 'Settings' }]"
    />

    <div v-if="loading" class="text-center py-5">
      <span class="spinner-border"></span>
    </div>

    <template v-else>
      <!-- Tabs -->
      <ul class="nav nav-tabs mb-4">
        <li v-for="tab in tabs" :key="tab.key" class="nav-item">
          <button
            class="nav-link"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            <i :class="tab.icon" class="me-1"></i>{{ tab.label }}
          </button>
        </li>
      </ul>

      <div class="card border-0 shadow-sm">
        <div class="card-body" :style="activeTab === 'access' ? '' : 'max-width: 700px;'">
          <!-- Company Info -->
          <div v-show="activeTab === 'company'">
            <FormInput v-model="settings.companyName" label="Company Name" :error="errors.companyName" />
            <FormInput v-model="settings.companyAddress" label="Address" type="textarea" />
            <div class="row">
              <div class="col-md-6">
                <FormInput v-model="settings.companyTin" label="TIN" />
              </div>
              <div class="col-md-6">
                <FormInput v-model="settings.companyPhone" label="Phone" />
              </div>
            </div>
            <FormInput v-model="settings.companyEmail" label="Email" type="email" />
          </div>

          <!-- Tax Settings -->
          <div v-show="activeTab === 'tax'">
            <div class="form-check mb-3">
              <input id="taxEnabled" v-model="settings.taxEnabled" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="taxEnabled">Enable Tax Computation</label>
            </div>
            <div class="row">
              <div class="col-md-6">
                <FormInput v-model="settings.taxRate" label="Tax Rate (%)" type="number" />
              </div>
              <div class="col-md-6">
                <FormInput v-model="settings.taxLabel" label="Tax Label" placeholder="e.g. VAT" />
              </div>
            </div>
            <div class="form-check mb-3">
              <input id="taxInclusive" v-model="settings.taxInclusive" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="taxInclusive">Tax Inclusive Pricing</label>
            </div>
          </div>

          <!-- Document Numbering -->
          <div v-show="activeTab === 'numbering'">
            <FormInput v-model="settings.salePrefix" label="Sale Number Prefix" placeholder="e.g. SL-" />
            <FormInput v-model="settings.saleNextNumber" label="Next Sale Number" type="number" />
            <FormInput v-model="settings.stockInPrefix" label="Stock In Prefix" placeholder="e.g. SI-" />
            <FormInput v-model="settings.stockInNextNumber" label="Next Stock In Number" type="number" />
            <FormInput v-model="settings.stockOutPrefix" label="Stock Out Prefix" placeholder="e.g. SO-" />
            <FormInput v-model="settings.stockOutNextNumber" label="Next Stock Out Number" type="number" />
          </div>

          <!-- POS Settings -->
          <div v-show="activeTab === 'pos'">
            <FormInput v-model="settings.receiptHeader" label="Receipt Header" type="textarea" />
            <FormInput v-model="settings.receiptFooter" label="Receipt Footer" type="textarea" />
            <div class="form-check mb-3">
              <input id="autoPrint" v-model="settings.autoPrintReceipt" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="autoPrint">Auto-print receipt after payment</label>
            </div>
            <div class="form-check mb-3">
              <input id="requireShift" v-model="settings.requireShift" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="requireShift">Require open shift to create sales</label>
            </div>
          </div>

          <!-- Discount Settings -->
          <div v-show="activeTab === 'discount'">
            <div class="form-check mb-3">
              <input id="discountEnabled" v-model="settings.discountEnabled" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="discountEnabled">Enable Discounts</label>
            </div>
            <FormInput v-model="settings.maxDiscountPercent" label="Max Discount Percentage (%)" type="number" />
            <FormInput v-model="settings.seniorDiscountRate" label="Senior/PWD Discount Rate (%)" type="number" />
          </div>

          <!-- User Access Rights -->
          <div v-show="activeTab === 'access'">
            <div class="mb-3">
              <p class="text-muted">Configure default access rights for each role. Changes here apply to newly created users. To update existing users, edit them individually in User Management.</p>
            </div>

            <!-- Role Presets Table -->
            <div class="table-responsive">
              <table class="table table-bordered table-sm align-middle">
                <thead class="table-light">
                  <tr>
                    <th style="min-width: 200px;">Permission</th>
                    <th v-for="role in roles" :key="role.value" class="text-center" style="min-width: 100px;">
                      <small class="fw-bold">{{ role.label }}</small>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="group in permissionGroups" :key="group.label" class="table-secondary">
                    <td colspan="100%" class="fw-bold small text-uppercase py-1 px-2">{{ group.label }}</td>
                  </tr>
                  <template v-for="group in permissionGroups" :key="'g-' + group.label">
                    <tr v-for="perm in group.permissions" :key="perm.bit">
                      <td class="small">{{ perm.label }}</td>
                      <td v-for="role in roles" :key="role.value" class="text-center">
                        <input
                          class="form-check-input"
                          type="checkbox"
                          :checked="hasRolePermission(role.value, perm.bit)"
                          @change="toggleRolePermission(role.value, perm.bit)"
                        />
                      </td>
                    </tr>
                  </template>
                </tbody>
              </table>
            </div>

            <!-- Quick Actions -->
            <div class="d-flex gap-2 flex-wrap mt-3">
              <button
                v-for="role in roles"
                :key="role.value"
                class="btn btn-outline-secondary btn-sm"
                @click="selectAllForRole(role.value)"
              >
                Select All: {{ role.label }}
              </button>
              <button class="btn btn-outline-danger btn-sm" @click="clearAllRolePermissions">
                Clear All
              </button>
            </div>
          </div>

          <!-- Save Button -->
          <div class="mt-4 pt-3 border-top">
            <button class="btn btn-primary" :disabled="saving" @click="saveSettings">
              <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
              Save Settings
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import settingsApi from '@/api/settings'
import PageHeader from '@/components/common/PageHeader.vue'
import FormInput from '@/components/common/FormInput.vue'
import { useAppStore } from '@/stores/app'
import { PERMISSIONS } from '@/stores/auth'

const appStore = useAppStore()

const loading = ref(true)
const saving = ref(false)
const activeTab = ref('company')

const tabs = [
  { key: 'company', label: 'Company Info', icon: 'bi bi-building' },
  { key: 'tax', label: 'Tax Settings', icon: 'bi bi-percent' },
  { key: 'numbering', label: 'Document Numbering', icon: 'bi bi-hash' },
  { key: 'pos', label: 'POS Settings', icon: 'bi bi-cart3' },
  { key: 'discount', label: 'Discount Settings', icon: 'bi bi-tag' },
  { key: 'access', label: 'User Access Rights', icon: 'bi bi-shield-lock' }
]

const roles = [
  { value: 'ADMIN', label: 'Admin' },
  { value: 'MANAGER', label: 'Manager' },
  { value: 'CASHIER', label: 'Cashier' },
  { value: 'INVENTORY_CLERK', label: 'Inventory Clerk' },
  { value: 'VIEWER', label: 'Viewer' }
]

const permissionGroups = [
  {
    label: 'Dashboard',
    permissions: [
      { bit: PERMISSIONS.VIEW_DASHBOARD, label: 'View Dashboard' }
    ]
  },
  {
    label: 'Inventory',
    permissions: [
      { bit: PERMISSIONS.VIEW_ITEMS, label: 'View Items' },
      { bit: PERMISSIONS.MANAGE_ITEMS, label: 'Manage Items (Create/Edit/Delete)' },
      { bit: PERMISSIONS.VIEW_CATEGORIES, label: 'View Categories' },
      { bit: PERMISSIONS.MANAGE_CATEGORIES, label: 'Manage Categories' },
      { bit: PERMISSIONS.VIEW_STOCK, label: 'View Stock Levels' },
      { bit: PERMISSIONS.MANAGE_STOCK_IN, label: 'Stock In' },
      { bit: PERMISSIONS.MANAGE_STOCK_OUT, label: 'Stock Out' },
      { bit: PERMISSIONS.MANAGE_STOCK_ADJ, label: 'Stock Adjustment' },
      { bit: PERMISSIONS.VIEW_TRANSACTIONS, label: 'View Transactions' },
      { bit: PERMISSIONS.APPROVE_TRANSACTIONS, label: 'Approve Transactions' },
      { bit: PERMISSIONS.CANCEL_TRANSACTIONS, label: 'Cancel Transactions' }
    ]
  },
  {
    label: 'Point of Sale',
    permissions: [
      { bit: PERMISSIONS.USE_POS, label: 'Use POS Terminal' },
      { bit: PERMISSIONS.VOID_SALES, label: 'Void Sales' },
      { bit: PERMISSIONS.MANAGE_SHIFTS, label: 'Manage Shifts' },
      { bit: PERMISSIONS.REPRINT, label: 'Reprint Receipts' }
    ]
  },
  {
    label: 'Contacts',
    permissions: [
      { bit: PERMISSIONS.VIEW_CUSTOMERS, label: 'View Customers' },
      { bit: PERMISSIONS.MANAGE_CUSTOMERS, label: 'Manage Customers' },
      { bit: PERMISSIONS.VIEW_SUPPLIERS, label: 'View Suppliers' },
      { bit: PERMISSIONS.MANAGE_SUPPLIERS, label: 'Manage Suppliers' }
    ]
  },
  {
    label: 'Administration',
    permissions: [
      { bit: PERMISSIONS.VIEW_REPORTS, label: 'View Reports' },
      { bit: PERMISSIONS.VIEW_AUDIT_TRAIL, label: 'View Audit Trail' },
      { bit: PERMISSIONS.MANAGE_USERS, label: 'Manage Users' },
      { bit: PERMISSIONS.MANAGE_SETTINGS, label: 'Manage Settings' },
      { bit: PERMISSIONS.MANAGE_ADDONS, label: 'Manage Add-ons' }
    ]
  }
]

// Default role presets (bitwise)
const rolePresets = reactive({
  ADMIN: 0xFFFFFFFF, // All permissions
  MANAGER: 0,
  CASHIER: 0,
  INVENTORY_CLERK: 0,
  VIEWER: 0
})

// Set default presets
function initDefaultPresets() {
  // Manager - everything except manage users/settings
  rolePresets.MANAGER = buildPermissionMask([
    PERMISSIONS.VIEW_DASHBOARD, PERMISSIONS.VIEW_ITEMS, PERMISSIONS.MANAGE_ITEMS,
    PERMISSIONS.VIEW_CATEGORIES, PERMISSIONS.MANAGE_CATEGORIES, PERMISSIONS.VIEW_STOCK,
    PERMISSIONS.MANAGE_STOCK_IN, PERMISSIONS.MANAGE_STOCK_OUT, PERMISSIONS.MANAGE_STOCK_ADJ,
    PERMISSIONS.VIEW_TRANSACTIONS, PERMISSIONS.APPROVE_TRANSACTIONS, PERMISSIONS.CANCEL_TRANSACTIONS,
    PERMISSIONS.USE_POS, PERMISSIONS.VOID_SALES, PERMISSIONS.MANAGE_SHIFTS, PERMISSIONS.REPRINT,
    PERMISSIONS.VIEW_CUSTOMERS, PERMISSIONS.MANAGE_CUSTOMERS, PERMISSIONS.VIEW_SUPPLIERS,
    PERMISSIONS.MANAGE_SUPPLIERS, PERMISSIONS.VIEW_REPORTS, PERMISSIONS.VIEW_AUDIT_TRAIL
  ])

  // Cashier - POS + view items
  rolePresets.CASHIER = buildPermissionMask([
    PERMISSIONS.VIEW_DASHBOARD, PERMISSIONS.VIEW_ITEMS, PERMISSIONS.VIEW_CATEGORIES,
    PERMISSIONS.VIEW_STOCK, PERMISSIONS.USE_POS, PERMISSIONS.MANAGE_SHIFTS, PERMISSIONS.REPRINT,
    PERMISSIONS.VIEW_CUSTOMERS
  ])

  // Inventory Clerk - Stock + items
  rolePresets.INVENTORY_CLERK = buildPermissionMask([
    PERMISSIONS.VIEW_DASHBOARD, PERMISSIONS.VIEW_ITEMS, PERMISSIONS.MANAGE_ITEMS,
    PERMISSIONS.VIEW_CATEGORIES, PERMISSIONS.MANAGE_CATEGORIES, PERMISSIONS.VIEW_STOCK,
    PERMISSIONS.MANAGE_STOCK_IN, PERMISSIONS.MANAGE_STOCK_OUT, PERMISSIONS.MANAGE_STOCK_ADJ,
    PERMISSIONS.VIEW_TRANSACTIONS, PERMISSIONS.VIEW_SUPPLIERS, PERMISSIONS.MANAGE_SUPPLIERS
  ])

  // Viewer - view only
  rolePresets.VIEWER = buildPermissionMask([
    PERMISSIONS.VIEW_DASHBOARD, PERMISSIONS.VIEW_ITEMS, PERMISSIONS.VIEW_CATEGORIES,
    PERMISSIONS.VIEW_STOCK, PERMISSIONS.VIEW_TRANSACTIONS, PERMISSIONS.VIEW_CUSTOMERS,
    PERMISSIONS.VIEW_SUPPLIERS, PERMISSIONS.VIEW_REPORTS
  ])
}

function buildPermissionMask(bits) {
  return bits.reduce((mask, bit) => mask | (1 << bit), 0)
}

function hasRolePermission(role, bit) {
  return (rolePresets[role] & (1 << bit)) !== 0
}

function toggleRolePermission(role, bit) {
  rolePresets[role] ^= (1 << bit)
}

function selectAllForRole(role) {
  rolePresets[role] = 0xFFFFFFFF
}

function clearAllRolePermissions() {
  roles.forEach(r => { rolePresets[r.value] = 0 })
  // Keep admin full access
  rolePresets.ADMIN = 0xFFFFFFFF
}

const settings = reactive({
  companyName: '',
  companyAddress: '',
  companyTin: '',
  companyPhone: '',
  companyEmail: '',
  taxEnabled: true,
  taxRate: 12,
  taxLabel: 'VAT',
  taxInclusive: false,
  salePrefix: 'SL-',
  saleNextNumber: 1,
  stockInPrefix: 'SI-',
  stockInNextNumber: 1,
  stockOutPrefix: 'SO-',
  stockOutNextNumber: 1,
  receiptHeader: '',
  receiptFooter: '',
  autoPrintReceipt: false,
  requireShift: false,
  discountEnabled: true,
  maxDiscountPercent: 50,
  seniorDiscountRate: 20,
  // Role presets stored as JSON
  rolePresets: ''
})

const errors = reactive({
  companyName: ''
})

onMounted(async () => {
  initDefaultPresets()
  try {
    const { data } = await settingsApi.getAll()
    const payload = data.data || data
    Object.keys(settings).forEach(key => {
      if (payload[key] !== undefined) {
        settings[key] = payload[key]
      }
    })
    // Load saved role presets if they exist
    if (payload.rolePresets) {
      try {
        const saved = JSON.parse(payload.rolePresets)
        Object.keys(saved).forEach(role => {
          if (rolePresets[role] !== undefined) {
            rolePresets[role] = saved[role]
          }
        })
      } catch (e) {
        // Use defaults if parse fails
      }
    }
  } catch (error) {
    appStore.showToast('Failed to load settings', 'error')
  } finally {
    loading.value = false
  }
})

async function saveSettings() {
  saving.value = true
  try {
    const payload = { ...settings }
    // Serialize role presets
    payload.rolePresets = JSON.stringify({ ...rolePresets })
    await settingsApi.update(payload)
    appStore.showToast('Settings saved successfully')
  } catch (error) {
    const data = error.response?.data
    appStore.showToast(data?.message || 'Failed to save settings', 'error')
  } finally {
    saving.value = false
  }
}
</script>

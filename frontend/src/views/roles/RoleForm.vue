<template>
  <div>
    <PageHeader
      :title="isEdit ? 'Edit Role' : 'New Role'"
      :breadcrumbs="[
        { label: 'Dashboard', route: '/dashboard' },
        { label: 'Roles', route: '/roles' },
        { label: isEdit ? 'Edit' : 'New' }
      ]"
    />

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <div v-if="pageLoading" class="text-center py-5">
          <span class="spinner-border"></span>
        </div>

        <form v-else @submit.prevent="handleSubmit">
          <div v-if="isAdminRole" class="alert alert-info">
            <i class="bi bi-shield-lock me-1"></i>
            The Admin role is reserved and cannot be modified.
          </div>

          <div class="row" style="max-width: 700px;">
            <div class="col-md-6">
              <FormInput
                v-model="form.name"
                label="Role Name"
                placeholder="e.g. Cashier"
                required
                :disabled="isAdminRole"
                :error="errors.name"
              />
            </div>
            <div class="col-md-6">
              <FormInput
                v-model="form.description"
                label="Description"
                placeholder="Short description"
                :disabled="isAdminRole"
              />
            </div>
          </div>

          <!-- Permission Matrix -->
          <div class="mb-3">
            <label class="form-label fw-medium">Permissions</label>
            <div v-for="group in permissionGroups" :key="group.label" class="mb-2">
              <div class="fw-medium small text-uppercase text-muted mt-2">{{ group.label }}</div>
              <div class="row g-2">
                <div v-for="perm in group.permissions" :key="perm.bit" class="col-md-6 col-lg-4">
                  <div class="form-check">
                    <input
                      :id="`perm-${perm.bit}`"
                      class="form-check-input"
                      type="checkbox"
                      :checked="hasPermission(perm.bit)"
                      :disabled="isAdminRole"
                      @change="togglePermission(perm.bit)"
                    />
                    <label :for="`perm-${perm.bit}`" class="form-check-label small">
                      {{ perm.label }}
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-primary" :disabled="submitting || isAdminRole">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              {{ isEdit ? 'Update' : 'Create' }}
            </button>
            <router-link to="/roles" class="btn btn-secondary">Cancel</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import rolesApi from '@/api/roles'
import PageHeader from '@/components/common/PageHeader.vue'
import FormInput from '@/components/common/FormInput.vue'
import { useAppStore } from '@/stores/app'
import { PERMISSIONS } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const isEdit = computed(() => !!route.params.id)
const isAdminRole = computed(() => isEdit.value && Number(route.params.id) === 1)
const pageLoading = ref(false)
const submitting = ref(false)

const permissionGroups = [
  {
    label: 'Dashboard',
    permissions: [{ bit: PERMISSIONS.VIEW_DASHBOARD, label: 'View Dashboard' }]
  },
  {
    label: 'Inventory',
    permissions: [
      { bit: PERMISSIONS.VIEW_ITEMS, label: 'View Items' },
      { bit: PERMISSIONS.MANAGE_ITEMS, label: 'Manage Items' },
      { bit: PERMISSIONS.VIEW_CATEGORIES, label: 'View Categories' },
      { bit: PERMISSIONS.MANAGE_CATEGORIES, label: 'Manage Categories' },
      { bit: PERMISSIONS.VIEW_STOCK, label: 'View Stock' },
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
      { bit: PERMISSIONS.USE_POS, label: 'Use POS' },
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

const form = reactive({
  name: '',
  description: '',
  accessRights: 0
})

const errors = reactive({
  name: ''
})

onMounted(async () => {
  if (isEdit.value) {
    pageLoading.value = true
    try {
      const { data } = await rolesApi.getById(route.params.id)
      const payload = data.data || data
      form.name = payload.name || ''
      form.description = payload.description || ''
      form.accessRights = payload.accessRights || 0
    } catch (error) {
      appStore.showToast('Failed to load role', 'error')
      router.push('/roles')
    } finally {
      pageLoading.value = false
    }
  }
})

function hasPermission(bit) {
  return (form.accessRights & (1 << bit)) !== 0
}

function togglePermission(bit) {
  form.accessRights ^= (1 << bit)
}

function validate() {
  errors.name = ''
  if (!form.name.trim()) {
    errors.name = 'Role name is required'
    return false
  }
  return true
}

async function handleSubmit() {
  if (isAdminRole.value) return
  if (!validate()) return

  submitting.value = true
  try {
    const payload = {
      name: form.name,
      description: form.description,
      accessRights: form.accessRights
    }
    if (isEdit.value) {
      await rolesApi.update(route.params.id, payload)
      appStore.showToast('Role updated successfully')
    } else {
      await rolesApi.create(payload)
      appStore.showToast('Role created successfully')
    }
    router.push('/roles')
  } catch (error) {
    const data = error.response?.data
    if (data?.errors) {
      Object.assign(errors, data.errors)
    } else {
      appStore.showToast(data?.message || 'Failed to save role', 'error')
    }
  } finally {
    submitting.value = false
  }
}
</script>

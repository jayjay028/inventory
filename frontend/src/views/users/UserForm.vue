<template>
  <div>
    <PageHeader
      :title="isEdit ? 'Edit User' : 'New User'"
      :breadcrumbs="[
        { label: 'Dashboard', route: '/dashboard' },
        { label: 'Users', route: '/users' },
        { label: isEdit ? 'Edit' : 'New' }
      ]"
    />

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <div v-if="pageLoading" class="text-center py-5">
          <span class="spinner-border"></span>
        </div>

        <form v-else @submit.prevent="handleSubmit" style="max-width: 700px;">
          <div class="row">
            <div class="col-md-6">
              <FormInput
                v-model="form.username"
                label="Username"
                placeholder="Enter username"
                required
                :disabled="isEdit"
                :error="errors.username"
              />
            </div>
            <div class="col-md-6">
              <FormInput
                v-if="!isEdit"
                v-model="form.password"
                label="Password"
                type="password"
                placeholder="Enter password"
                required
                :error="errors.password"
              />
            </div>
          </div>

          <div class="row">
            <div class="col-md-6">
              <FormInput
                v-model="form.fullName"
                label="Full Name"
                placeholder="Enter full name"
                required
                :error="errors.fullName"
              />
            </div>
            <div class="col-md-6">
              <FormInput
                v-model="form.email"
                label="Email"
                type="email"
                placeholder="Enter email"
                :error="errors.email"
              />
            </div>
          </div>

          <FormInput
            v-model="form.role"
            label="Role"
            type="select"
            required
            :options="roleOptions"
            :error="errors.role"
          />

          <!-- Permissions -->
          <div class="mb-3">
            <label class="form-label fw-medium">Permissions</label>
            <div class="row g-2">
              <div v-for="perm in permissionList" :key="perm.bit" class="col-md-6 col-lg-4">
                <div class="form-check">
                  <input
                    :id="`perm-${perm.bit}`"
                    class="form-check-input"
                    type="checkbox"
                    :checked="hasPermission(perm.bit)"
                    @change="togglePermission(perm.bit)"
                  />
                  <label :for="`perm-${perm.bit}`" class="form-check-label small">
                    {{ perm.label }}
                  </label>
                </div>
              </div>
            </div>
          </div>

          <!-- Active toggle -->
          <div class="form-check mb-3" v-if="isEdit">
            <input id="activeToggle" v-model="form.active" class="form-check-input" type="checkbox" />
            <label class="form-check-label" for="activeToggle">Active</label>
          </div>

          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              {{ isEdit ? 'Update' : 'Create' }}
            </button>
            <router-link to="/users" class="btn btn-secondary">Cancel</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import usersApi from '@/api/users'
import PageHeader from '@/components/common/PageHeader.vue'
import FormInput from '@/components/common/FormInput.vue'
import { useAppStore } from '@/stores/app'
import { PERMISSIONS } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const isEdit = computed(() => !!route.params.id)
const pageLoading = ref(false)
const submitting = ref(false)

const roleOptions = [
  { value: 'ADMIN', label: 'Administrator' },
  { value: 'MANAGER', label: 'Manager' },
  { value: 'CASHIER', label: 'Cashier' },
  { value: 'INVENTORY_CLERK', label: 'Inventory Clerk' },
  { value: 'VIEWER', label: 'Viewer' }
]

const permissionList = [
  { bit: PERMISSIONS.VIEW_DASHBOARD, label: 'View Dashboard' },
  { bit: PERMISSIONS.VIEW_ITEMS, label: 'View Items' },
  { bit: PERMISSIONS.MANAGE_ITEMS, label: 'Manage Items' },
  { bit: PERMISSIONS.VIEW_CATEGORIES, label: 'View Categories' },
  { bit: PERMISSIONS.MANAGE_CATEGORIES, label: 'Manage Categories' },
  { bit: PERMISSIONS.VIEW_CUSTOMERS, label: 'View Customers' },
  { bit: PERMISSIONS.MANAGE_CUSTOMERS, label: 'Manage Customers' },
  { bit: PERMISSIONS.VIEW_SUPPLIERS, label: 'View Suppliers' },
  { bit: PERMISSIONS.MANAGE_SUPPLIERS, label: 'Manage Suppliers' },
  { bit: PERMISSIONS.VIEW_STOCK, label: 'View Stock' },
  { bit: PERMISSIONS.MANAGE_STOCK_IN, label: 'Stock In' },
  { bit: PERMISSIONS.MANAGE_STOCK_OUT, label: 'Stock Out' },
  { bit: PERMISSIONS.MANAGE_STOCK_ADJ, label: 'Stock Adjustment' },
  { bit: PERMISSIONS.VIEW_TRANSACTIONS, label: 'View Transactions' },
  { bit: PERMISSIONS.APPROVE_TRANSACTIONS, label: 'Approve Transactions' },
  { bit: PERMISSIONS.CANCEL_TRANSACTIONS, label: 'Cancel Transactions' },
  { bit: PERMISSIONS.USE_POS, label: 'Use POS' },
  { bit: PERMISSIONS.VOID_SALES, label: 'Void Sales' },
  { bit: PERMISSIONS.MANAGE_SHIFTS, label: 'Manage Shifts' },
  { bit: PERMISSIONS.VIEW_REPORTS, label: 'View Reports' },
  { bit: PERMISSIONS.VIEW_AUDIT_TRAIL, label: 'View Audit Trail' },
  { bit: PERMISSIONS.MANAGE_USERS, label: 'Manage Users' },
  { bit: PERMISSIONS.MANAGE_SETTINGS, label: 'Manage Settings' },
  { bit: PERMISSIONS.MANAGE_ADDONS, label: 'Manage Add-ons' },
  { bit: PERMISSIONS.REPRINT, label: 'Reprint' }
]

const form = reactive({
  username: '',
  password: '',
  fullName: '',
  email: '',
  role: '',
  accessRights: 0,
  active: true
})

const errors = reactive({
  username: '',
  password: '',
  fullName: '',
  email: '',
  role: ''
})

onMounted(async () => {
  if (isEdit.value) {
    pageLoading.value = true
    try {
      const { data } = await usersApi.getById(route.params.id)
      form.username = data.username || ''
      form.fullName = data.fullName || ''
      form.email = data.email || ''
      form.role = data.role || ''
      form.accessRights = data.accessRights || 0
      form.active = data.active !== false
    } catch (error) {
      appStore.showToast('Failed to load user', 'error')
      router.push('/users')
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
  let valid = true
  Object.keys(errors).forEach(k => (errors[k] = ''))

  if (!form.username.trim()) { errors.username = 'Username is required'; valid = false }
  if (!isEdit.value && !form.password) { errors.password = 'Password is required'; valid = false }
  if (!form.fullName.trim()) { errors.fullName = 'Full name is required'; valid = false }
  if (!form.role) { errors.role = 'Role is required'; valid = false }

  if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = 'Invalid email format'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validate()) return

  submitting.value = true
  try {
    const payload = {
      username: form.username,
      fullName: form.fullName,
      email: form.email || null,
      role: form.role,
      accessRights: form.accessRights,
      active: form.active
    }

    if (!isEdit.value) {
      payload.password = form.password
      await usersApi.create(payload)
      appStore.showToast('User created successfully')
    } else {
      await usersApi.update(route.params.id, payload)
      appStore.showToast('User updated successfully')
    }
    router.push('/users')
  } catch (error) {
    const data = error.response?.data
    if (data?.errors) {
      Object.assign(errors, data.errors)
    } else {
      appStore.showToast(data?.message || 'Failed to save user', 'error')
    }
  } finally {
    submitting.value = false
  }
}
</script>

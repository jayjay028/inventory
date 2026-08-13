<template>
  <div>
    <PageHeader
      title="Users"
      subtitle="Manage system users"
      :breadcrumbs="[{ label: 'Dashboard', route: '/dashboard' }, { label: 'Users' }]"
    >
      <template #actions>
        <router-link to="/users/new" class="btn btn-primary">
          <i class="bi bi-plus-lg me-1"></i>New User
        </router-link>
      </template>
    </PageHeader>

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <DataTable
          :columns="columns"
          :data="users"
          :loading="loading"
          :total-pages="totalPages"
          :current-page="currentPage"
          searchable
          search-placeholder="Search users..."
          @search="handleSearch"
          @page-change="handlePageChange"
        >
          <template #cell-status="{ row }">
            <StatusBadge :status="row.active ? 'ACTIVE' : 'INACTIVE'" />
          </template>
          <template #actions="{ row }">
            <div class="d-flex gap-1">
              <router-link :to="`/users/${row.id}/edit`" class="btn btn-sm btn-outline-primary">
                <i class="bi bi-pencil"></i>
              </router-link>
              <button
                class="btn btn-sm"
                :class="row.active ? 'btn-outline-danger' : 'btn-outline-success'"
                @click="toggleStatus(row)"
              >
                <i class="bi" :class="row.active ? 'bi-x-circle' : 'bi-check-circle'"></i>
              </button>
            </div>
          </template>
        </DataTable>
      </div>
    </div>

    <ConfirmDialog
      :show="confirmDialog.show"
      :title="confirmDialog.title"
      :message="confirmDialog.message"
      :variant="confirmDialog.variant"
      @confirm="confirmAction"
      @cancel="confirmDialog.show = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import usersApi from '@/api/users'
import PageHeader from '@/components/common/PageHeader.vue'
import DataTable from '@/components/common/DataTable.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const columns = [
  { key: 'username', label: 'Username', sortable: true },
  { key: 'fullName', label: 'Full Name', sortable: true },
  { key: 'role', label: 'Role', width: '120px' },
  { key: 'status', label: 'Status', width: '90px' }
]

const users = ref([])
const loading = ref(true)
const currentPage = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')

const confirmDialog = reactive({
  show: false,
  title: '',
  message: '',
  variant: 'danger',
  row: null
})

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: 10,
      search: searchQuery.value || undefined
    }
    const { data } = await usersApi.getAll(params)
    users.value = data.content || data
    totalPages.value = data.totalPages || 1
  } catch (error) {
    appStore.showToast('Failed to load users', 'error')
  } finally {
    loading.value = false
  }
}

function handleSearch(query) {
  searchQuery.value = query
  currentPage.value = 1
  loadData()
}

function handlePageChange(page) {
  currentPage.value = page
  loadData()
}

function toggleStatus(row) {
  confirmDialog.row = row
  confirmDialog.title = row.active ? 'Deactivate User' : 'Activate User'
  confirmDialog.message = `Are you sure you want to ${row.active ? 'deactivate' : 'activate'} user "${row.username}"?`
  confirmDialog.variant = row.active ? 'danger' : 'success'
  confirmDialog.show = true
}

async function confirmAction() {
  const row = confirmDialog.row
  confirmDialog.show = false
  try {
    await usersApi.updateStatus(row.id, !row.active)
    appStore.showToast(`User ${row.active ? 'deactivated' : 'activated'} successfully`)
    loadData()
  } catch (error) {
    appStore.showToast('Failed to update user status', 'error')
  }
}
</script>

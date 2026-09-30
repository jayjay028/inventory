<template>
  <div>
    <PageHeader
      title="Roles"
      subtitle="Define roles and their permissions"
      :breadcrumbs="[{ label: 'Dashboard', route: '/dashboard' }, { label: 'Roles' }]"
    >
      <template #actions>
        <router-link to="/roles/new" class="btn btn-primary">
          <i class="bi bi-plus-lg me-1"></i>New Role
        </router-link>
      </template>
    </PageHeader>

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <DataTable
          :columns="columns"
          :data="roles"
          :loading="loading"
          :total-pages="totalPages"
          :current-page="currentPage"
          @page-change="handlePageChange"
        >
          <template #cell-name="{ row }">
            {{ row.name }}
            <span v-if="row.isSystem" class="badge bg-secondary ms-1">System</span>
          </template>
          <template #cell-status="{ row }">
            <StatusBadge :status="row.active ? 'ACTIVE' : 'INACTIVE'" />
          </template>
          <template #actions="{ row }">
            <div class="d-flex gap-1">
              <router-link
                :to="`/roles/${row.id}/edit`"
                class="btn btn-sm btn-outline-primary"
                :class="{ disabled: row.id === 1 }"
              >
                <i class="bi bi-pencil"></i>
              </router-link>
              <button
                class="btn btn-sm"
                :class="row.active ? 'btn-outline-danger' : 'btn-outline-success'"
                :disabled="row.id === 1"
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
import rolesApi from '@/api/roles'
import PageHeader from '@/components/common/PageHeader.vue'
import DataTable from '@/components/common/DataTable.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const columns = [
  { key: 'name', label: 'Name' },
  { key: 'description', label: 'Description' },
  { key: 'status', label: 'Status', width: '90px' }
]

const roles = ref([])
const loading = ref(true)
const currentPage = ref(1)
const totalPages = ref(1)

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
    const { data } = await rolesApi.getAll({ page: currentPage.value, size: 10 })
    const payload = data.data || data
    roles.value = payload.content || payload
    totalPages.value = payload.totalPages || 1
  } catch (error) {
    appStore.showToast('Failed to load roles', 'error')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  loadData()
}

function toggleStatus(row) {
  confirmDialog.row = row
  confirmDialog.title = row.active ? 'Deactivate Role' : 'Activate Role'
  confirmDialog.message = `Are you sure you want to ${row.active ? 'deactivate' : 'activate'} "${row.name}"?`
  confirmDialog.variant = row.active ? 'danger' : 'success'
  confirmDialog.show = true
}

async function confirmAction() {
  const row = confirmDialog.row
  confirmDialog.show = false
  try {
    await rolesApi.updateStatus(row.id, !row.active)
    appStore.showToast(`Role ${row.active ? 'deactivated' : 'activated'} successfully`)
    loadData()
  } catch (error) {
    appStore.showToast(error.response?.data?.message || 'Failed to update role status', 'error')
  }
}
</script>

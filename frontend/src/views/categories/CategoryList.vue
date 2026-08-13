<template>
  <div>
    <PageHeader
      title="Categories"
      :breadcrumbs="[{ label: 'Dashboard', route: '/dashboard' }, { label: 'Categories' }]"
    >
      <template #actions>
        <router-link to="/categories/new" class="btn btn-primary">
          <i class="bi bi-plus-lg me-1"></i>New Category
        </router-link>
      </template>
    </PageHeader>

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <DataTable
          :columns="columns"
          :data="categories"
          :loading="loading"
          :total-pages="totalPages"
          :current-page="currentPage"
          searchable
          search-placeholder="Search categories..."
          @search="handleSearch"
          @page-change="handlePageChange"
          @sort-change="handleSortChange"
        >
          <template #cell-status="{ row }">
            <StatusBadge :status="row.active ? 'ACTIVE' : 'INACTIVE'" />
          </template>
          <template #actions="{ row }">
            <div class="d-flex gap-1">
              <router-link :to="`/categories/${row.id}/edit`" class="btn btn-sm btn-outline-primary">
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
import categoriesApi from '@/api/categories'
import PageHeader from '@/components/common/PageHeader.vue'
import DataTable from '@/components/common/DataTable.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const columns = [
  { key: 'name', label: 'Name', sortable: true },
  { key: 'description', label: 'Description' },
  { key: 'status', label: 'Status', width: '100px' }
]

const categories = ref([])
const loading = ref(true)
const currentPage = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')
const sortKey = ref('')
const sortOrder = ref('')

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
      search: searchQuery.value || undefined,
      sortBy: sortKey.value || undefined,
      sortDir: sortOrder.value || undefined
    }
    const { data } = await categoriesApi.getAll(params)
    categories.value = data.content || data
    totalPages.value = data.totalPages || 1
  } catch (error) {
    appStore.showToast('Failed to load categories', 'error')
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

function handleSortChange({ key, order }) {
  sortKey.value = key
  sortOrder.value = order
  loadData()
}

function toggleStatus(row) {
  confirmDialog.row = row
  confirmDialog.title = row.active ? 'Deactivate Category' : 'Activate Category'
  confirmDialog.message = `Are you sure you want to ${row.active ? 'deactivate' : 'activate'} "${row.name}"?`
  confirmDialog.variant = row.active ? 'danger' : 'success'
  confirmDialog.show = true
}

async function confirmAction() {
  const row = confirmDialog.row
  confirmDialog.show = false

  try {
    await categoriesApi.updateStatus(row.id, !row.active)
    appStore.showToast(`Category ${row.active ? 'deactivated' : 'activated'} successfully`)
    loadData()
  } catch (error) {
    appStore.showToast('Failed to update category status', 'error')
  }
}
</script>

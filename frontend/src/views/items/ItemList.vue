<template>
  <div>
    <PageHeader
      title="Items"
      :breadcrumbs="[{ label: 'Dashboard', route: '/dashboard' }, { label: 'Items' }]"
    >
      <template #actions>
        <router-link to="/items/new" class="btn btn-primary">
          <i class="bi bi-plus-lg me-1"></i>New Item
        </router-link>
      </template>
    </PageHeader>

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <DataTable
          :columns="columns"
          :data="items"
          :loading="loading"
          :total-pages="totalPages"
          :current-page="currentPage"
          searchable
          search-placeholder="Search items..."
          @search="handleSearch"
          @page-change="handlePageChange"
          @sort-change="handleSortChange"
        >
          <template #toolbar>
            <select v-model="categoryFilter" class="form-select form-select-sm" style="width: auto;" @change="handleFilterChange">
              <option value="">All Categories</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </template>
          <template #cell-price="{ value }">
            {{ formatCurrency(value) }}
          </template>
          <template #cell-costPrice="{ value }">
            {{ formatCurrency(value) }}
          </template>
          <template #cell-status="{ row }">
            <StatusBadge :status="row.active ? 'ACTIVE' : 'INACTIVE'" />
          </template>
          <template #actions="{ row }">
            <div class="d-flex gap-1">
              <router-link :to="`/items/${row.id}/edit`" class="btn btn-sm btn-outline-primary">
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
import itemsApi from '@/api/items'
import categoriesApi from '@/api/categories'
import PageHeader from '@/components/common/PageHeader.vue'
import DataTable from '@/components/common/DataTable.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const columns = [
  { key: 'itemCode', label: 'Item Code', sortable: true, width: '110px' },
  { key: 'name', label: 'Name', sortable: true },
  { key: 'categoryName', label: 'Category', sortable: true },
  { key: 'unit', label: 'Unit', width: '80px' },
  { key: 'price', label: 'Price', sortable: true, width: '100px' },
  { key: 'costPrice', label: 'Cost', width: '100px' },
  { key: 'qtyOnHand', label: 'Stock', sortable: true, width: '80px' },
  { key: 'status', label: 'Status', width: '90px' }
]

const items = ref([])
const categories = ref([])
const loading = ref(true)
const currentPage = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')
const categoryFilter = ref('')
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
  loadCategories()
  loadData()
})

async function loadCategories() {
  try {
    const { data } = await categoriesApi.getAll({ size: 100 })
    categories.value = data.content || data
  } catch (error) {
    // Non-critical, filter just won't work
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: 10,
      search: searchQuery.value || undefined,
      categoryId: categoryFilter.value || undefined,
      sortBy: sortKey.value || undefined,
      sortDir: sortOrder.value || undefined
    }
    const { data } = await itemsApi.getAll(params)
    items.value = data.content || data
    totalPages.value = data.totalPages || 1
  } catch (error) {
    appStore.showToast('Failed to load items', 'error')
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

function handleFilterChange() {
  currentPage.value = 1
  loadData()
}

function toggleStatus(row) {
  confirmDialog.row = row
  confirmDialog.title = row.active ? 'Deactivate Item' : 'Activate Item'
  confirmDialog.message = `Are you sure you want to ${row.active ? 'deactivate' : 'activate'} "${row.name}"?`
  confirmDialog.variant = row.active ? 'danger' : 'success'
  confirmDialog.show = true
}

async function confirmAction() {
  const row = confirmDialog.row
  confirmDialog.show = false

  try {
    await itemsApi.updateStatus(row.id, !row.active)
    appStore.showToast(`Item ${row.active ? 'deactivated' : 'activated'} successfully`)
    loadData()
  } catch (error) {
    appStore.showToast('Failed to update item status', 'error')
  }
}

function formatCurrency(value) {
  if (value == null) return '—'
  return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(value)
}
</script>

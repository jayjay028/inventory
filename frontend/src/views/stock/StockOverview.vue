<template>
  <div>
    <PageHeader
      title="Stock Overview"
      subtitle="Current inventory levels"
      :breadcrumbs="[{ label: 'Dashboard', route: '/dashboard' }, { label: 'Stock Overview' }]"
    >
      <template #actions>
        <router-link to="/stock/in" class="btn btn-success me-1">
          <i class="bi bi-box-arrow-in-down me-1"></i>Stock In
        </router-link>
        <router-link to="/stock/out" class="btn btn-info">
          <i class="bi bi-box-arrow-up me-1"></i>Stock Out
        </router-link>
      </template>
    </PageHeader>

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <DataTable
          :columns="columns"
          :data="stockItems"
          :loading="loading"
          :total-pages="totalPages"
          :current-page="currentPage"
          searchable
          search-placeholder="Search stock..."
          @search="handleSearch"
          @page-change="handlePageChange"
          @sort-change="handleSortChange"
        >
          <template #cell-qtyOnHand="{ row }">
            <span class="fw-bold" :class="stockTextClass(row)">{{ row.qtyOnHand }}</span>
          </template>
          <template #cell-status="{ row }">
            <span class="badge" :class="stockBadgeClass(row)">
              {{ stockStatus(row) }}
            </span>
          </template>
        </DataTable>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import stockApi from '@/api/stock'
import PageHeader from '@/components/common/PageHeader.vue'
import DataTable from '@/components/common/DataTable.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const columns = [
  { key: 'itemCode', label: 'Item Code', sortable: true, width: '120px' },
  { key: 'itemName', label: 'Item Name', sortable: true },
  { key: 'categoryName', label: 'Category', sortable: true },
  { key: 'qtyOnHand', label: 'Qty On Hand', sortable: true, width: '120px' },
  { key: 'reorderLevel', label: 'Reorder Level', width: '120px' },
  { key: 'status', label: 'Status', width: '120px' }
]

const stockItems = ref([])
const loading = ref(true)
const currentPage = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')
const sortKey = ref('')
const sortOrder = ref('')

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
    const { data } = await stockApi.getAll(params)
    stockItems.value = data.content || data
    totalPages.value = data.totalPages || 1
  } catch (error) {
    appStore.showToast('Failed to load stock data', 'error')
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

function stockStatus(row) {
  if (row.qtyOnHand <= 0) return 'OUT OF STOCK'
  if (row.qtyOnHand <= row.reorderLevel) return 'LOW'
  return 'NORMAL'
}

function stockBadgeClass(row) {
  if (row.qtyOnHand <= 0) return 'bg-danger'
  if (row.qtyOnHand <= row.reorderLevel) return 'bg-warning text-dark'
  return 'bg-success'
}

function stockTextClass(row) {
  if (row.qtyOnHand <= 0) return 'text-danger'
  if (row.qtyOnHand <= row.reorderLevel) return 'text-warning'
  return 'text-success'
}
</script>

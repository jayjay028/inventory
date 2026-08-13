<template>
  <div>
    <PageHeader
      title="Shifts"
      subtitle="POS shift history"
      :breadcrumbs="[{ label: 'Dashboard', route: '/dashboard' }, { label: 'Shifts' }]"
    />

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <DataTable
          :columns="columns"
          :data="shifts"
          :loading="loading"
          :total-pages="totalPages"
          :current-page="currentPage"
          searchable
          search-placeholder="Search shifts..."
          @search="handleSearch"
          @page-change="handlePageChange"
        >
          <template #cell-openedAt="{ value }">
            {{ formatDateTime(value) }}
          </template>
          <template #cell-closedAt="{ value }">
            {{ value ? formatDateTime(value) : '—' }}
          </template>
          <template #cell-totalSales="{ value }">
            {{ formatCurrency(value) }}
          </template>
          <template #cell-status="{ row }">
            <span class="badge" :class="row.closedAt ? 'bg-secondary' : 'bg-success'">
              {{ row.closedAt ? 'CLOSED' : 'OPEN' }}
            </span>
          </template>
        </DataTable>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import posApi from '@/api/pos'
import PageHeader from '@/components/common/PageHeader.vue'
import DataTable from '@/components/common/DataTable.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const columns = [
  { key: 'userName', label: 'User', sortable: true },
  { key: 'openedAt', label: 'Opened', sortable: true },
  { key: 'closedAt', label: 'Closed', sortable: true },
  { key: 'totalSales', label: 'Total Sales', width: '120px' },
  { key: 'transactionCount', label: 'Transactions', width: '110px' },
  { key: 'status', label: 'Status', width: '90px' }
]

const shifts = ref([])
const loading = ref(true)
const currentPage = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')

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
    const { data } = await posApi.getShifts(params)
    shifts.value = data.content || data
    totalPages.value = data.totalPages || 1
  } catch (error) {
    appStore.showToast('Failed to load shifts', 'error')
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

function formatDateTime(datetime) {
  if (!datetime) return ''
  return new Date(datetime).toLocaleString('en-PH', {
    month: 'short', day: 'numeric', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

function formatCurrency(value) {
  if (value == null) return '—'
  return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(value)
}
</script>

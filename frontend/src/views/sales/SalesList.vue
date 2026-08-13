<template>
  <div>
    <PageHeader
      title="Sales"
      subtitle="Sales transaction history"
      :breadcrumbs="[{ label: 'Dashboard', route: '/dashboard' }, { label: 'Sales' }]"
    />

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <!-- Filters -->
        <div class="row g-2 mb-3">
          <div class="col-md-2">
            <select v-model="filters.status" class="form-select form-select-sm" @change="loadData">
              <option value="">All Status</option>
              <option value="OPEN">Open</option>
              <option value="PAID">Paid</option>
              <option value="CLOSED">Closed</option>
              <option value="VOIDED">Voided</option>
            </select>
          </div>
          <div class="col-md-2">
            <input v-model="filters.dateFrom" type="date" class="form-control form-control-sm" @change="loadData" />
          </div>
          <div class="col-md-2">
            <input v-model="filters.dateTo" type="date" class="form-control form-control-sm" @change="loadData" />
          </div>
        </div>

        <DataTable
          :columns="columns"
          :data="sales"
          :loading="loading"
          :total-pages="totalPages"
          :current-page="currentPage"
          searchable
          search-placeholder="Search sales..."
          @search="handleSearch"
          @page-change="handlePageChange"
          @sort-change="handleSortChange"
        >
          <template #cell-saleDate="{ value }">
            {{ formatDate(value) }}
          </template>
          <template #cell-totalAmount="{ value }">
            {{ formatCurrency(value) }}
          </template>
          <template #cell-status="{ value }">
            <StatusBadge :status="value" />
          </template>
          <template #actions="{ row }">
            <div class="d-flex gap-1">
              <router-link :to="`/sales/${row.id}`" class="btn btn-sm btn-outline-primary" title="View">
                <i class="bi bi-eye"></i>
              </router-link>
              <button
                v-if="row.status === 'PAID' || row.status === 'CLOSED'"
                class="btn btn-sm btn-outline-secondary"
                title="Receipt"
                @click="printReceipt(row)"
              >
                <i class="bi bi-receipt"></i>
              </button>
              <button
                v-if="canVoid && (row.status === 'PAID' || row.status === 'CLOSED')"
                class="btn btn-sm btn-outline-danger"
                title="Void"
                @click="voidSale(row)"
              >
                <i class="bi bi-x-circle"></i>
              </button>
            </div>
          </template>
        </DataTable>
      </div>
    </div>

    <ConfirmDialog
      :show="confirmDialog.show"
      title="Void Sale"
      :message="confirmDialog.message"
      variant="danger"
      confirm-text="Void Sale"
      @confirm="confirmVoid"
      @cancel="confirmDialog.show = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import posApi from '@/api/pos'
import PageHeader from '@/components/common/PageHeader.vue'
import DataTable from '@/components/common/DataTable.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore, PERMISSIONS } from '@/stores/auth'

const appStore = useAppStore()
const authStore = useAuthStore()

const canVoid = computed(() => authStore.hasPermission(PERMISSIONS.VOID_SALES))

const columns = [
  { key: 'saleNumber', label: 'Sale #', sortable: true, width: '110px' },
  { key: 'saleDate', label: 'Date', sortable: true, width: '110px' },
  { key: 'customerName', label: 'Customer' },
  { key: 'itemCount', label: 'Items', width: '70px' },
  { key: 'totalAmount', label: 'Total', sortable: true, width: '110px' },
  { key: 'paymentMethod', label: 'Payment', width: '100px' },
  { key: 'status', label: 'Status', width: '100px' }
]

const sales = ref([])
const loading = ref(true)
const currentPage = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')
const sortKey = ref('')
const sortOrder = ref('')

const filters = reactive({
  status: '',
  dateFrom: '',
  dateTo: ''
})

const confirmDialog = reactive({
  show: false,
  message: '',
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
      status: filters.status || undefined,
      dateFrom: filters.dateFrom || undefined,
      dateTo: filters.dateTo || undefined,
      sortBy: sortKey.value || undefined,
      sortDir: sortOrder.value || undefined
    }
    const { data } = await posApi.getSales(params)
    sales.value = data.content || data
    totalPages.value = data.totalPages || 1
  } catch (error) {
    appStore.showToast('Failed to load sales', 'error')
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

async function printReceipt(row) {
  try {
    const { data } = await posApi.getReceipt(row.id)
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    window.open(url, '_blank')
  } catch (error) {
    appStore.showToast('Failed to load receipt', 'error')
  }
}

function voidSale(row) {
  confirmDialog.row = row
  confirmDialog.message = `Are you sure you want to void Sale #${row.saleNumber}? This action cannot be undone.`
  confirmDialog.show = true
}

async function confirmVoid() {
  const row = confirmDialog.row
  confirmDialog.show = false
  try {
    await posApi.voidSale(row.id, { reason: 'Voided by user' })
    appStore.showToast('Sale voided successfully')
    loadData()
  } catch (error) {
    appStore.showToast(error.response?.data?.message || 'Failed to void sale', 'error')
  }
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('en-PH', { month: 'short', day: 'numeric', year: 'numeric' })
}

function formatCurrency(value) {
  if (value == null) return '—'
  return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(value)
}
</script>

<template>
  <div>
    <PageHeader
      title="Transactions"
      subtitle="All stock movement transactions"
      :breadcrumbs="[{ label: 'Dashboard', route: '/dashboard' }, { label: 'Transactions' }]"
    />

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <!-- Filters -->
        <div class="row g-2 mb-3">
          <div class="col-md-3">
            <select v-model="filters.status" class="form-select form-select-sm" @change="loadData">
              <option value="">All Statuses</option>
              <option value="CREATED">Created</option>
              <option value="APPROVED">Approved</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>
          <div class="col-md-3">
            <select v-model="filters.type" class="form-select form-select-sm" @change="loadData">
              <option value="">All Types</option>
              <option value="IN">Stock In</option>
              <option value="OUT">Stock Out</option>
              <option value="ADJUSTMENT">Adjustment</option>
            </select>
          </div>
        </div>

        <DataTable
          :columns="columns"
          :data="transactions"
          :loading="loading"
          :total-pages="totalPages"
          :current-page="currentPage"
          searchable
          search-placeholder="Search transactions..."
          @search="handleSearch"
          @page-change="handlePageChange"
          @sort-change="handleSortChange"
        >
          <template #cell-transactionDate="{ value }">
            {{ formatDate(value) }}
          </template>
          <template #cell-type="{ value }">
            <span class="badge" :class="typeBadgeClass(value)">{{ value }}</span>
          </template>
          <template #cell-status="{ value }">
            <StatusBadge :status="value" />
          </template>
          <template #cell-amount="{ value }">
            {{ formatCurrency(value) }}
          </template>
          <template #actions="{ row }">
            <div class="d-flex gap-1">
              <button
                v-if="row.status === 'CREATED' && canApprove"
                class="btn btn-sm btn-outline-success"
                title="Approve"
                @click="approveTransaction(row)"
              >
                <i class="bi bi-check-lg"></i>
              </button>
              <button
                v-if="row.status === 'CREATED' && canCancel"
                class="btn btn-sm btn-outline-danger"
                title="Cancel"
                @click="cancelTransaction(row)"
              >
                <i class="bi bi-x-lg"></i>
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
import { ref, reactive, computed, onMounted } from 'vue'
import transactionsApi from '@/api/transactions'
import stockApi from '@/api/stock'
import PageHeader from '@/components/common/PageHeader.vue'
import DataTable from '@/components/common/DataTable.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore, PERMISSIONS } from '@/stores/auth'

const appStore = useAppStore()
const authStore = useAuthStore()

const canApprove = computed(() => authStore.hasPermission(PERMISSIONS.APPROVE_TRANSACTIONS))
const canCancel = computed(() => authStore.hasPermission(PERMISSIONS.CANCEL_TRANSACTIONS))

const columns = [
  { key: 'transactionDate', label: 'Date', sortable: true, width: '110px' },
  { key: 'itemName', label: 'Item', sortable: true },
  { key: 'type', label: 'Type', width: '100px' },
  { key: 'quantity', label: 'Qty', width: '70px' },
  { key: 'status', label: 'Status', width: '110px' },
  { key: 'documentType', label: 'Document' },
  { key: 'amount', label: 'Amount', width: '110px' },
  { key: 'userName', label: 'User', width: '100px' }
]

const transactions = ref([])
const loading = ref(true)
const currentPage = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')
const sortKey = ref('')
const sortOrder = ref('')

const filters = reactive({
  status: '',
  type: ''
})

const confirmDialog = reactive({
  show: false,
  title: '',
  message: '',
  variant: 'success',
  action: null,
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
      type: filters.type || undefined,
      sortBy: sortKey.value || undefined,
      sortDir: sortOrder.value || undefined
    }
    const { data } = await transactionsApi.getAll(params)
    transactions.value = data.content || data
    totalPages.value = data.totalPages || 1
  } catch (error) {
    appStore.showToast('Failed to load transactions', 'error')
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

function approveTransaction(row) {
  confirmDialog.row = row
  confirmDialog.action = 'approve'
  confirmDialog.title = 'Approve Transaction'
  confirmDialog.message = `Approve this ${row.type} transaction for "${row.itemName}" (Qty: ${row.quantity})?`
  confirmDialog.variant = 'success'
  confirmDialog.show = true
}

function cancelTransaction(row) {
  confirmDialog.row = row
  confirmDialog.action = 'cancel'
  confirmDialog.title = 'Cancel Transaction'
  confirmDialog.message = `Cancel this ${row.type} transaction for "${row.itemName}" (Qty: ${row.quantity})? This cannot be undone.`
  confirmDialog.variant = 'danger'
  confirmDialog.show = true
}

async function confirmAction() {
  const row = confirmDialog.row
  const action = confirmDialog.action
  confirmDialog.show = false

  try {
    if (action === 'approve') {
      await stockApi.approve(row.id)
      appStore.showToast('Transaction approved successfully')
    } else {
      await stockApi.cancel(row.id)
      appStore.showToast('Transaction cancelled')
    }
    loadData()
  } catch (error) {
    appStore.showToast(`Failed to ${action} transaction`, 'error')
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

function typeBadgeClass(type) {
  const map = { IN: 'bg-success', OUT: 'bg-info', ADJUSTMENT: 'bg-secondary' }
  return map[type] || 'bg-secondary'
}
</script>

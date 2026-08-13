<template>
  <div>
    <PageHeader title="Dashboard" subtitle="Overview of your business metrics" />

    <!-- Summary Cards -->
    <div class="row g-3 mb-4">
      <div class="col-xl col-md-4 col-sm-6">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-shrink-0">
                <div class="bg-primary bg-opacity-10 rounded-3 p-3">
                  <i class="bi bi-box-seam fs-4 text-primary"></i>
                </div>
              </div>
              <div class="flex-grow-1 ms-3">
                <h6 class="text-muted mb-1">Total Items</h6>
                <h4 class="mb-0">{{ dashboard.totalItems ?? '—' }}</h4>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-xl col-md-4 col-sm-6">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-shrink-0">
                <div class="bg-success bg-opacity-10 rounded-3 p-3">
                  <i class="bi bi-currency-dollar fs-4 text-success"></i>
                </div>
              </div>
              <div class="flex-grow-1 ms-3">
                <h6 class="text-muted mb-1">Stock Value</h6>
                <h4 class="mb-0">{{ formatCurrency(dashboard.stockValue) }}</h4>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-xl col-md-4 col-sm-6">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-shrink-0">
                <div class="bg-warning bg-opacity-10 rounded-3 p-3">
                  <i class="bi bi-exclamation-triangle fs-4 text-warning"></i>
                </div>
              </div>
              <div class="flex-grow-1 ms-3">
                <h6 class="text-muted mb-1">Low Stock</h6>
                <h4 class="mb-0">{{ dashboard.lowStockCount ?? '—' }}</h4>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-xl col-md-4 col-sm-6">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-shrink-0">
                <div class="bg-info bg-opacity-10 rounded-3 p-3">
                  <i class="bi bi-cart-check fs-4 text-info"></i>
                </div>
              </div>
              <div class="flex-grow-1 ms-3">
                <h6 class="text-muted mb-1">Today's Sales</h6>
                <h4 class="mb-0">{{ formatCurrency(dashboard.todaySales) }}</h4>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-xl col-md-4 col-sm-6">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-shrink-0">
                <div class="bg-danger bg-opacity-10 rounded-3 p-3">
                  <i class="bi bi-clock-history fs-4 text-danger"></i>
                </div>
              </div>
              <div class="flex-grow-1 ms-3">
                <h6 class="text-muted mb-1">Pending Approvals</h6>
                <h4 class="mb-0">{{ dashboard.pendingApprovals ?? '—' }}</h4>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-4">
      <!-- Recent Transactions -->
      <div class="col-lg-8">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white d-flex justify-content-between align-items-center">
            <h6 class="mb-0">Recent Transactions</h6>
            <router-link to="/transactions" class="btn btn-sm btn-outline-primary">View All</router-link>
          </div>
          <div class="card-body p-0">
            <div class="table-responsive">
              <table class="table table-hover mb-0">
                <thead class="table-light">
                  <tr>
                    <th>Date</th>
                    <th>Item</th>
                    <th>Type</th>
                    <th class="text-end">Qty</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="loading">
                    <td colspan="5" class="text-center py-4">
                      <span class="spinner-border spinner-border-sm me-2"></span>Loading...
                    </td>
                  </tr>
                  <tr v-else-if="!dashboard.recentTransactions?.length">
                    <td colspan="5" class="text-center py-4 text-muted">No recent transactions</td>
                  </tr>
                  <tr v-for="txn in dashboard.recentTransactions" :key="txn.id">
                    <td>{{ formatDate(txn.transactionDate) }}</td>
                    <td>{{ txn.itemName }}</td>
                    <td>
                      <span class="badge" :class="typeBadgeClass(txn.type)">{{ txn.type }}</span>
                    </td>
                    <td class="text-end">{{ txn.quantity }}</td>
                    <td>
                      <StatusBadge :status="txn.status" />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- Low Stock Alerts -->
      <div class="col-lg-4">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-white d-flex justify-content-between align-items-center">
            <h6 class="mb-0">Low Stock Alerts</h6>
            <router-link to="/stock" class="btn btn-sm btn-outline-warning">View All</router-link>
          </div>
          <div class="card-body p-0">
            <div class="list-group list-group-flush">
              <div v-if="loading" class="text-center py-4">
                <span class="spinner-border spinner-border-sm me-2"></span>Loading...
              </div>
              <div v-else-if="!dashboard.lowStockItems?.length" class="text-center py-4 text-muted">
                All items are well-stocked
              </div>
              <div
                v-for="item in dashboard.lowStockItems"
                :key="item.id"
                class="list-group-item d-flex justify-content-between align-items-center"
              >
                <div>
                  <div class="fw-medium">{{ item.name }}</div>
                  <small class="text-muted">Reorder at: {{ item.reorderLevel }}</small>
                </div>
                <span class="badge bg-danger rounded-pill">{{ item.qtyOnHand }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Top Selling Items -->
        <div class="card border-0 shadow-sm mt-4">
          <div class="card-header bg-white">
            <h6 class="mb-0">Top Selling Items (This Month)</h6>
          </div>
          <div class="card-body p-0">
            <div class="list-group list-group-flush">
              <div v-if="loading" class="text-center py-4">
                <span class="spinner-border spinner-border-sm me-2"></span>Loading...
              </div>
              <div v-else-if="!dashboard.topSellingItems?.length" class="text-center py-4 text-muted">
                No sales data yet
              </div>
              <div
                v-for="(item, index) in dashboard.topSellingItems"
                :key="item.id"
                class="list-group-item d-flex justify-content-between align-items-center"
              >
                <div class="d-flex align-items-center">
                  <span class="badge bg-primary rounded-circle me-2">{{ index + 1 }}</span>
                  <span>{{ item.name }}</span>
                </div>
                <span class="text-muted">{{ item.totalQtySold }} sold</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import dashboardApi from '@/api/dashboard'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const loading = ref(true)
const dashboard = reactive({
  totalItems: 0,
  stockValue: 0,
  lowStockCount: 0,
  todaySales: 0,
  pendingApprovals: 0,
  recentTransactions: [],
  lowStockItems: [],
  topSellingItems: []
})

onMounted(async () => {
  try {
    const { data } = await dashboardApi.get()
    Object.assign(dashboard, data)
  } catch (error) {
    appStore.showToast('Failed to load dashboard data', 'error')
  } finally {
    loading.value = false
  }
})

function formatCurrency(value) {
  if (value == null) return '—'
  return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(value)
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('en-PH', { month: 'short', day: 'numeric', year: 'numeric' })
}

function typeBadgeClass(type) {
  const map = { IN: 'bg-success', OUT: 'bg-info', ADJUSTMENT: 'bg-secondary' }
  return map[type] || 'bg-secondary'
}
</script>

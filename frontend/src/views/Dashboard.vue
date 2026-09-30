<template>
  <div class="dashboard">
    <PageHeader title="Dashboard" subtitle="Business performance overview" />

    <!-- Summary Cards -->
    <div class="row g-3 mb-4">
      <div class="col-xl col-md-4 col-sm-6">
        <div class="summary-card summary-card--indigo">
          <div class="summary-card__icon summary-card__icon--indigo">
            <i class="bi bi-box-seam"></i>
          </div>
          <div class="summary-card__content">
            <div class="summary-card__value">{{ dashboard.totalItems ?? '—' }}</div>
            <div class="summary-card__label">Total Items</div>
          </div>
        </div>
      </div>
      <div class="col-xl col-md-4 col-sm-6">
        <div class="summary-card summary-card--emerald">
          <div class="summary-card__icon summary-card__icon--emerald">
            <i class="bi bi-wallet2"></i>
          </div>
          <div class="summary-card__content">
            <div class="summary-card__value">{{ formatCurrency(dashboard.stockValue) }}</div>
            <div class="summary-card__label">Stock Value</div>
          </div>
        </div>
      </div>
      <div class="col-xl col-md-4 col-sm-6">
        <div class="summary-card summary-card--amber">
          <div class="summary-card__icon summary-card__icon--amber">
            <i class="bi bi-exclamation-triangle"></i>
          </div>
          <div class="summary-card__content">
            <div class="summary-card__value">{{ dashboard.lowStockCount ?? '—' }}</div>
            <div class="summary-card__label">Low Stock</div>
          </div>
        </div>
      </div>
      <div class="col-xl col-md-4 col-sm-6">
        <div class="summary-card summary-card--teal">
          <div class="summary-card__icon summary-card__icon--teal">
            <i class="bi bi-cart-check"></i>
          </div>
          <div class="summary-card__content">
            <div class="summary-card__value">{{ formatCurrency(dashboard.todaySales) }}</div>
            <div class="summary-card__label">Today's Sales</div>
          </div>
        </div>
      </div>
      <div class="col-xl col-md-4 col-sm-6">
        <div class="summary-card summary-card--rose">
          <div class="summary-card__icon summary-card__icon--rose">
            <i class="bi bi-clock-history"></i>
          </div>
          <div class="summary-card__content">
            <div class="summary-card__value">{{ dashboard.pendingApprovals ?? '—' }}</div>
            <div class="summary-card__label">Pending Approvals</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Two Column Layout -->
    <div class="row g-4">
      <!-- Recent Transactions -->
      <div class="col-lg-8">
        <div class="panel">
          <div class="panel__header">
            <h6 class="panel__title">Recent Transactions</h6>
            <router-link to="/transactions" class="btn-outline-action">
              View All <i class="bi bi-arrow-right ms-1"></i>
            </router-link>
          </div>
          <div class="panel__body panel__body--flush">
            <div class="table-responsive">
              <table class="txn-table">
                <thead>
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
                    <td colspan="5" class="txn-table__empty">
                      <span class="spinner-border spinner-border-sm me-2"></span>Loading...
                    </td>
                  </tr>
                  <tr v-else-if="!dashboard.recentTransactions?.length">
                    <td colspan="5" class="txn-table__empty">No recent transactions</td>
                  </tr>
                  <tr v-for="txn in dashboard.recentTransactions" :key="txn.id">
                    <td class="txn-table__date">{{ formatDate(txn.transactionDate) }}</td>
                    <td class="txn-table__item">{{ txn.itemName }}</td>
                    <td>
                      <span class="type-badge" :class="typeBadgeClass(txn.type)">{{ txn.type }}</span>
                    </td>
                    <td class="text-end fw-medium">{{ txn.quantity }}</td>
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
        <div class="panel">
          <div class="panel__header">
            <h6 class="panel__title">Low Stock Alerts</h6>
            <router-link to="/stock" class="btn-outline-action btn-outline-action--warning">
              View All
            </router-link>
          </div>
          <div class="panel__body panel__body--flush">
            <div v-if="loading" class="stock-list__empty">
              <span class="spinner-border spinner-border-sm me-2"></span>Loading...
            </div>
            <div v-else-if="!dashboard.lowStockItems?.length" class="stock-list__empty">
              <i class="bi bi-check-circle text-success me-2"></i>All items are well-stocked
            </div>
            <div v-else class="stock-list">
              <div
                v-for="item in dashboard.lowStockItems"
                :key="item.id"
                class="stock-list__item"
              >
                <div class="stock-list__info">
                  <div class="stock-list__name">{{ item.name }}</div>
                  <div class="stock-list__reorder">Reorder level: {{ item.reorderLevel }}</div>
                </div>
                <div class="stock-list__qty">
                  {{ item.qtyOnHand }}
                </div>
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
    const result = data.data || data
    Object.assign(dashboard, result)
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
  const map = { IN: 'type-badge--in', OUT: 'type-badge--out', ADJUSTMENT: 'type-badge--adjust' }
  return map[type] || 'type-badge--adjust'
}
</script>

<style scoped>
/* Summary Cards */
.summary-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem;
  background: var(--color-card-bg);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  border-left: 3px solid transparent;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  height: 100%;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.summary-card:hover {
  border-color: var(--color-border-light);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.35);
}

.summary-card--indigo { border-left-color: rgba(37, 99, 235, 0.6); }
.summary-card--emerald { border-left-color: rgba(16, 185, 129, 0.6); }
.summary-card--amber { border-left-color: rgba(245, 158, 11, 0.6); }
.summary-card--teal { border-left-color: rgba(13, 148, 136, 0.6); }
.summary-card--rose { border-left-color: rgba(244, 63, 94, 0.6); }

.summary-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  font-size: 1.15rem;
  flex-shrink: 0;
}

.summary-card__icon--indigo { background: rgba(37, 99, 235, 0.15); color: #60a5fa; }
.summary-card__icon--emerald { background: rgba(16, 185, 129, 0.12); color: #34d399; }
.summary-card__icon--amber { background: rgba(245, 158, 11, 0.12); color: #fbbf24; }
.summary-card__icon--teal { background: rgba(13, 148, 136, 0.12); color: #2dd4bf; }
.summary-card__icon--rose { background: rgba(244, 63, 94, 0.12); color: #fb7185; }

.summary-card__content {
  min-width: 0;
}

.summary-card__value {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.summary-card__label {
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.03em;
  margin-top: 0.125rem;
}

/* Panel (Card) */
.panel {
  background: var(--color-card-bg);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

.panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--color-border);
}

.panel__title {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}

.panel__body {
  padding: 1.25rem;
}

.panel__body--flush {
  padding: 0;
}

/* Action Button */
.btn-outline-action {
  display: inline-flex;
  align-items: center;
  font-size: 0.75rem;
  font-weight: 500;
  color: #7dd3fc;
  text-decoration: none;
  padding: 0.3rem 0.7rem;
  border: 1px solid rgba(37, 99, 235, 0.35);
  border-radius: 6px;
  transition: all 0.15s ease;
  background: transparent;
}

.btn-outline-action:hover {
  background: rgba(37, 99, 235, 0.15);
  border-color: rgba(37, 99, 235, 0.5);
  color: #bae6fd;
}

.btn-outline-action--warning {
  color: #fbbf24;
  border-color: rgba(245, 158, 11, 0.3);
}

.btn-outline-action--warning:hover {
  background: rgba(245, 158, 11, 0.12);
  border-color: rgba(245, 158, 11, 0.5);
  color: #fcd34d;
}

/* Transaction Table */
.txn-table {
  width: 100%;
  font-size: 0.8125rem;
  border-collapse: collapse;
}

.txn-table thead th {
  padding: 0.7rem 1.25rem;
  font-size: 0.6875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-text-muted);
  background: var(--color-surface-2);
  border-bottom: 1px solid var(--color-border);
  white-space: nowrap;
}

.txn-table tbody td {
  padding: 0.7rem 1.25rem;
  vertical-align: middle;
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text);
}

.txn-table tbody tr:last-child td {
  border-bottom: none;
}

.txn-table tbody tr:hover td {
  background: var(--color-card-hover);
}

.txn-table__empty {
  text-align: center;
  padding: 2.5rem 1rem !important;
  color: var(--color-text-muted);
}

.txn-table__date {
  color: var(--color-text-muted);
  white-space: nowrap;
}

.txn-table__item {
  font-weight: 500;
  color: var(--color-heading);
}

/* Type Badge */
.type-badge {
  display: inline-block;
  font-size: 0.6875rem;
  font-weight: 500;
  padding: 0.2rem 0.55rem;
  border-radius: 4px;
  text-transform: uppercase;
  letter-spacing: 0.02em;
}

.type-badge--in {
  background: rgba(16, 185, 129, 0.15);
  color: #6ee7b7;
}

.type-badge--out {
  background: rgba(37, 99, 235, 0.18);
  color: #7dd3fc;
}

.type-badge--adjust {
  background: rgba(148, 163, 184, 0.15);
  color: #cbd5e1;
}

/* Stock List */
.stock-list__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2.5rem 1rem;
  color: var(--color-text-muted);
  font-size: 0.8125rem;
}

.stock-list__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1.25rem;
  border-bottom: 1px solid var(--color-border);
  transition: background 0.15s ease;
}

.stock-list__item:last-child {
  border-bottom: none;
}

.stock-list__item:hover {
  background: var(--color-card-hover);
}

.stock-list__name {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--color-heading);
}

.stock-list__reorder {
  font-size: 0.7rem;
  color: var(--color-text-muted);
  margin-top: 0.125rem;
}

.stock-list__qty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 24px;
  padding: 0 0.5rem;
  background: rgba(239, 68, 68, 0.15);
  color: #fca5a5;
  font-size: 0.75rem;
  font-weight: 600;
  border-radius: 4px;
}

/* Responsive */
@media (max-width: 767.98px) {
  .summary-card__value {
    font-size: 1.25rem;
  }

  .summary-card {
    padding: 1rem;
  }
}
</style>

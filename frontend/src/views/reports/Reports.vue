<template>
  <div>
    <PageHeader
      title="Reports"
      subtitle="Generate and download reports"
      :breadcrumbs="[{ label: 'Dashboard', route: '/dashboard' }, { label: 'Reports' }]"
    />

    <!-- Report Categories -->
    <div v-if="!selectedReport">
      <!-- Inventory Reports -->
      <h6 class="text-muted text-uppercase small fw-bold mb-3">Inventory Reports</h6>
      <div class="row g-3 mb-4">
        <div v-for="report in inventoryReports" :key="report.key" class="col-md-4">
          <div class="card border-0 shadow-sm h-100 cursor-pointer report-card" @click="selectReport(report)">
            <div class="card-body">
              <div class="d-flex align-items-center">
                <div class="bg-primary bg-opacity-10 rounded-3 p-3 me-3">
                  <i :class="report.icon" class="fs-4 text-primary"></i>
                </div>
                <div>
                  <h6 class="mb-1">{{ report.title }}</h6>
                  <small class="text-muted">{{ report.description }}</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Financial Reports -->
      <h6 class="text-muted text-uppercase small fw-bold mb-3">Financial Reports</h6>
      <div class="row g-3 mb-4">
        <div v-for="report in financialReports" :key="report.key" class="col-md-4">
          <div class="card border-0 shadow-sm h-100 cursor-pointer report-card" @click="selectReport(report)">
            <div class="card-body">
              <div class="d-flex align-items-center">
                <div class="bg-success bg-opacity-10 rounded-3 p-3 me-3">
                  <i :class="report.icon" class="fs-4 text-success"></i>
                </div>
                <div>
                  <h6 class="mb-1">{{ report.title }}</h6>
                  <small class="text-muted">{{ report.description }}</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- POS Reports -->
      <h6 class="text-muted text-uppercase small fw-bold mb-3">POS Reports</h6>
      <div class="row g-3 mb-4">
        <div v-for="report in posReports" :key="report.key" class="col-md-4">
          <div class="card border-0 shadow-sm h-100 cursor-pointer report-card" @click="selectReport(report)">
            <div class="card-body">
              <div class="d-flex align-items-center">
                <div class="bg-info bg-opacity-10 rounded-3 p-3 me-3">
                  <i :class="report.icon" class="fs-4 text-info"></i>
                </div>
                <div>
                  <h6 class="mb-1">{{ report.title }}</h6>
                  <small class="text-muted">{{ report.description }}</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Report Parameter Form -->
    <div v-else class="card border-0 shadow-sm">
      <div class="card-header bg-white d-flex justify-content-between align-items-center">
        <h6 class="mb-0">{{ selectedReport.title }}</h6>
        <button class="btn btn-sm btn-outline-secondary" @click="selectedReport = null">
          <i class="bi bi-arrow-left me-1"></i>Back to Reports
        </button>
      </div>
      <div class="card-body" style="max-width: 500px;">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label">Date From</label>
            <input v-model="params.dateFrom" type="date" class="form-control" />
          </div>
          <div class="col-md-6">
            <label class="form-label">Date To</label>
            <input v-model="params.dateTo" type="date" class="form-control" />
          </div>
        </div>

        <!-- Customer/Supplier select for statements -->
        <div v-if="selectedReport.key === 'customerStatement'" class="mt-3">
          <FormInput
            v-model="params.customerId"
            label="Customer"
            type="select"
            required
            :options="customerOptions"
          />
        </div>
        <div v-if="selectedReport.key === 'supplierStatement'" class="mt-3">
          <FormInput
            v-model="params.supplierId"
            label="Supplier"
            type="select"
            required
            :options="supplierOptions"
          />
        </div>

        <button class="btn btn-primary mt-4" :disabled="generating" @click="generateReport">
          <span v-if="generating" class="spinner-border spinner-border-sm me-1"></span>
          <i v-else class="bi bi-file-earmark-pdf me-1"></i>
          Generate Report
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import reportsApi from '@/api/reports'
import customersApi from '@/api/customers'
import suppliersApi from '@/api/suppliers'
import PageHeader from '@/components/common/PageHeader.vue'
import FormInput from '@/components/common/FormInput.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const selectedReport = ref(null)
const generating = ref(false)
const customers = ref([])
const suppliers = ref([])

const params = reactive({
  dateFrom: '',
  dateTo: '',
  customerId: '',
  supplierId: ''
})

const inventoryReports = [
  { key: 'inventory', title: 'Inventory Report', description: 'Current stock levels', icon: 'bi bi-box-seam' },
  { key: 'stockMovement', title: 'Stock Movement', description: 'In/Out/Adjust history', icon: 'bi bi-arrow-left-right' },
  { key: 'lowStock', title: 'Low Stock Report', description: 'Items below reorder level', icon: 'bi bi-exclamation-triangle' }
]

const financialReports = [
  { key: 'purchaseHistory', title: 'Purchase History', description: 'Stock in transactions', icon: 'bi bi-receipt' },
  { key: 'customerStatement', title: 'Customer Statement', description: 'Customer account activity', icon: 'bi bi-person-lines-fill' },
  { key: 'supplierStatement', title: 'Supplier Statement', description: 'Supplier account activity', icon: 'bi bi-truck' },
  { key: 'auditTrail', title: 'Audit Trail Report', description: 'System activity log', icon: 'bi bi-shield-check' }
]

const posReports = [
  { key: 'sales', title: 'Sales Report', description: 'Detailed sales data', icon: 'bi bi-cart-check' },
  { key: 'salesSummary', title: 'Sales Summary', description: 'Summary by period', icon: 'bi bi-graph-up' }
]

const customerOptions = computed(() =>
  customers.value.map(c => ({ value: c.id, label: c.name }))
)

const supplierOptions = computed(() =>
  suppliers.value.map(s => ({ value: s.id, label: s.name }))
)

onMounted(async () => {
  try {
    const [custRes, suppRes] = await Promise.all([
      customersApi.getAll({ size: 100 }),
      suppliersApi.getAll({ size: 100 })
    ])
    customers.value = custRes.data.content || custRes.data
    suppliers.value = suppRes.data.content || suppRes.data
  } catch (error) {
    // Non-critical for page load
  }
})

function selectReport(report) {
  selectedReport.value = report
  // Default to current month
  const now = new Date()
  const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
  params.dateFrom = firstDay.toISOString().split('T')[0]
  params.dateTo = now.toISOString().split('T')[0]
  params.customerId = ''
  params.supplierId = ''
}

async function generateReport() {
  const report = selectedReport.value
  generating.value = true

  try {
    let response
    const reportParams = {
      dateFrom: params.dateFrom || undefined,
      dateTo: params.dateTo || undefined
    }

    switch (report.key) {
      case 'inventory': response = await reportsApi.inventory(reportParams); break
      case 'stockMovement': response = await reportsApi.stockMovement(reportParams); break
      case 'lowStock': response = await reportsApi.lowStock(reportParams); break
      case 'sales': response = await reportsApi.sales(reportParams); break
      case 'salesSummary': response = await reportsApi.salesSummary(reportParams); break
      case 'purchaseHistory': response = await reportsApi.purchaseHistory(reportParams); break
      case 'customerStatement':
        if (!params.customerId) { appStore.showToast('Please select a customer', 'warning'); generating.value = false; return }
        response = await reportsApi.customerStatement(params.customerId, reportParams)
        break
      case 'supplierStatement':
        if (!params.supplierId) { appStore.showToast('Please select a supplier', 'warning'); generating.value = false; return }
        response = await reportsApi.supplierStatement(params.supplierId, reportParams)
        break
      case 'auditTrail': response = await reportsApi.auditTrail(reportParams); break
      default: return
    }

    // Open PDF in new tab
    const blob = new Blob([response.data], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    window.open(url, '_blank')
  } catch (error) {
    appStore.showToast('Failed to generate report', 'error')
  } finally {
    generating.value = false
  }
}
</script>

<style scoped>
.report-card {
  transition: transform 0.15s, box-shadow 0.15s;
}
.report-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.cursor-pointer {
  cursor: pointer;
}
</style>

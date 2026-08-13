<template>
  <div>
    <PageHeader
      :title="`Sale #${sale.saleNumber || ''}`"
      :breadcrumbs="[
        { label: 'Dashboard', route: '/dashboard' },
        { label: 'Sales', route: '/sales' },
        { label: `Sale #${sale.saleNumber || ''}` }
      ]"
    >
      <template #actions>
        <button
          v-if="canVoid && (sale.status === 'PAID' || sale.status === 'CLOSED')"
          class="btn btn-danger"
          @click="showVoidDialog = true"
        >
          <i class="bi bi-x-circle me-1"></i>Void Sale
        </button>
        <button class="btn btn-outline-primary" @click="printReceipt">
          <i class="bi bi-printer me-1"></i>Print Receipt
        </button>
      </template>
    </PageHeader>

    <div v-if="loading" class="text-center py-5">
      <span class="spinner-border"></span>
    </div>

    <template v-else>
      <!-- Header Info -->
      <div class="card border-0 shadow-sm mb-4">
        <div class="card-body">
          <div class="row">
            <div class="col-md-3">
              <small class="text-muted d-block">Status</small>
              <StatusBadge :status="sale.status" />
            </div>
            <div class="col-md-3">
              <small class="text-muted d-block">Date</small>
              <strong>{{ formatDate(sale.saleDate) }}</strong>
            </div>
            <div class="col-md-3">
              <small class="text-muted d-block">Customer</small>
              <strong>{{ sale.customerName || 'Walk-in' }}</strong>
            </div>
            <div class="col-md-3">
              <small class="text-muted d-block">Payment Method</small>
              <strong>{{ sale.paymentMethod || '—' }}</strong>
            </div>
          </div>
        </div>
      </div>

      <!-- Items Table -->
      <div class="card border-0 shadow-sm mb-4">
        <div class="card-header bg-white">
          <h6 class="mb-0">Items</h6>
        </div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover mb-0">
              <thead class="table-light">
                <tr>
                  <th>Item</th>
                  <th class="text-end">Qty</th>
                  <th class="text-end">Unit Price</th>
                  <th class="text-end">Discount</th>
                  <th class="text-end">Line Total</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in sale.items" :key="item.id">
                  <td>{{ item.itemName }}</td>
                  <td class="text-end">{{ item.quantity }}</td>
                  <td class="text-end">{{ formatCurrency(item.unitPrice) }}</td>
                  <td class="text-end">{{ formatCurrency(item.discount || 0) }}</td>
                  <td class="text-end fw-bold">{{ formatCurrency(item.lineTotal) }}</td>
                </tr>
              </tbody>
              <tfoot class="table-light">
                <tr>
                  <td colspan="4" class="text-end">Subtotal:</td>
                  <td class="text-end fw-bold">{{ formatCurrency(sale.subtotal) }}</td>
                </tr>
                <tr v-if="sale.discountAmount">
                  <td colspan="4" class="text-end">Discount:</td>
                  <td class="text-end text-danger">-{{ formatCurrency(sale.discountAmount) }}</td>
                </tr>
                <tr>
                  <td colspan="4" class="text-end">Tax:</td>
                  <td class="text-end">{{ formatCurrency(sale.taxAmount) }}</td>
                </tr>
                <tr>
                  <td colspan="4" class="text-end fs-5 fw-bold">Total:</td>
                  <td class="text-end fs-5 fw-bold text-primary">{{ formatCurrency(sale.totalAmount) }}</td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>
      </div>

      <!-- Payments -->
      <div v-if="sale.payments?.length" class="card border-0 shadow-sm mb-4">
        <div class="card-header bg-white">
          <h6 class="mb-0">Payments</h6>
        </div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table mb-0">
              <thead class="table-light">
                <tr>
                  <th>Method</th>
                  <th>Reference</th>
                  <th class="text-end">Amount</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="payment in sale.payments" :key="payment.id">
                  <td>{{ payment.method }}</td>
                  <td>{{ payment.reference || '—' }}</td>
                  <td class="text-end">{{ formatCurrency(payment.amount) }}</td>
                  <td>{{ formatDate(payment.paymentDate) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Add-ons -->
      <div v-if="sale.addons?.length" class="card border-0 shadow-sm mb-4">
        <div class="card-header bg-white">
          <h6 class="mb-0">Add-ons</h6>
        </div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table mb-0">
              <thead class="table-light">
                <tr>
                  <th>Name</th>
                  <th class="text-end">Amount</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="addon in sale.addons" :key="addon.id">
                  <td>{{ addon.name }}</td>
                  <td class="text-end">{{ formatCurrency(addon.amount) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </template>

    <ConfirmDialog
      :show="showVoidDialog"
      title="Void Sale"
      :message="`Are you sure you want to void Sale #${sale.saleNumber}? This cannot be undone.`"
      variant="danger"
      confirm-text="Void Sale"
      @confirm="handleVoid"
      @cancel="showVoidDialog = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import posApi from '@/api/pos'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore, PERMISSIONS } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

const canVoid = computed(() => authStore.hasPermission(PERMISSIONS.VOID_SALES))
const loading = ref(true)
const showVoidDialog = ref(false)

const sale = reactive({
  id: null,
  saleNumber: '',
  saleDate: '',
  status: '',
  customerName: '',
  paymentMethod: '',
  subtotal: 0,
  discountAmount: 0,
  taxAmount: 0,
  totalAmount: 0,
  items: [],
  payments: [],
  addons: []
})

onMounted(async () => {
  try {
    const { data } = await posApi.getSaleById(route.params.id)
    Object.assign(sale, data)
  } catch (error) {
    appStore.showToast('Failed to load sale details', 'error')
    router.push('/sales')
  } finally {
    loading.value = false
  }
})

async function handleVoid() {
  showVoidDialog.value = false
  try {
    await posApi.voidSale(sale.id, { reason: 'Voided by user' })
    sale.status = 'VOIDED'
    appStore.showToast('Sale voided successfully')
  } catch (error) {
    appStore.showToast(error.response?.data?.message || 'Failed to void sale', 'error')
  }
}

async function printReceipt() {
  try {
    const { data } = await posApi.getReceipt(sale.id)
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    window.open(url, '_blank')
  } catch (error) {
    appStore.showToast('Failed to load receipt', 'error')
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

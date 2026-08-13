<template>
  <div>
    <PageHeader
      title="Stock Out"
      subtitle="Issue inventory"
      :breadcrumbs="[
        { label: 'Dashboard', route: '/dashboard' },
        { label: 'Stock Overview', route: '/stock' },
        { label: 'Stock Out' }
      ]"
    />

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <form @submit.prevent="handleSubmit" style="max-width: 800px;">
          <!-- Item search -->
          <div class="mb-3">
            <label class="form-label">Item <span class="text-danger">*</span></label>
            <div class="position-relative">
              <input
                v-model="itemSearch"
                type="text"
                class="form-control"
                :class="{ 'is-invalid': errors.itemId }"
                placeholder="Search item by name or code..."
                autocomplete="off"
                @input="searchItems"
                @focus="showItemDropdown = true"
              />
              <div v-if="errors.itemId" class="invalid-feedback d-block">{{ errors.itemId }}</div>
              <div
                v-if="showItemDropdown && itemResults.length"
                class="dropdown-menu show w-100 mt-1"
                style="max-height: 200px; overflow-y: auto;"
              >
                <button
                  v-for="item in itemResults"
                  :key="item.id"
                  type="button"
                  class="dropdown-item"
                  @click="selectItem(item)"
                >
                  <span class="fw-medium">{{ item.itemCode }}</span> — {{ item.name }}
                  <small class="text-muted ms-2">(Stock: {{ item.qtyOnHand }})</small>
                </button>
              </div>
            </div>
            <div v-if="selectedItem" class="mt-2 p-2 bg-light rounded small">
              <strong>Selected:</strong> {{ selectedItem.itemCode }} — {{ selectedItem.name }}
              <span class="ms-2 badge bg-info">Available Stock: {{ selectedItem.qtyOnHand }}</span>
            </div>
          </div>

          <div class="row">
            <div class="col-md-4">
              <FormInput
                v-model="form.quantity"
                label="Quantity"
                type="number"
                placeholder="0"
                required
                :error="errors.quantity"
              />
            </div>
            <div class="col-md-4">
              <FormInput
                v-model="form.unitPrice"
                label="Unit Price"
                type="number"
                placeholder="0.00"
                required
                :error="errors.unitPrice"
              />
            </div>
            <div class="col-md-4">
              <FormInput
                v-model="form.customerId"
                label="Customer"
                type="select"
                :options="customerOptions"
                :error="errors.customerId"
              />
            </div>
          </div>

          <div class="row">
            <div class="col-md-4">
              <FormInput
                v-model="form.documentType"
                label="Document Type"
                type="select"
                :options="docTypeOptions"
                :error="errors.documentType"
              />
            </div>
            <div class="col-md-4">
              <FormInput
                v-model="form.referenceNo"
                label="Reference No"
                placeholder="Enter reference"
                :error="errors.referenceNo"
              />
            </div>
            <div class="col-md-4">
              <FormInput
                v-model="form.transactionDate"
                label="Transaction Date"
                type="date"
                required
                :error="errors.transactionDate"
              />
            </div>
          </div>

          <!-- Discount -->
          <div class="row">
            <div class="col-md-4">
              <FormInput
                v-model="form.discountType"
                label="Discount Type"
                type="select"
                :options="discountTypeOptions"
              />
            </div>
            <div class="col-md-4">
              <FormInput
                v-model="form.discountValue"
                label="Discount Value"
                type="number"
                placeholder="0"
              />
            </div>
            <div class="col-md-4">
              <div class="mb-3">
                <label class="form-label">Computed Discount</label>
                <div class="form-control bg-light">{{ formatCurrency(computedDiscount) }}</div>
              </div>
            </div>
          </div>

          <div class="form-check mb-3">
            <input id="taxToggle" v-model="form.taxable" class="form-check-input" type="checkbox" />
            <label class="form-check-label" for="taxToggle">Include Tax</label>
          </div>

          <FormInput
            v-model="form.remarks"
            label="Remarks"
            type="textarea"
            placeholder="Enter remarks (optional)"
          />

          <!-- Add-ons section -->
          <div class="mb-3">
            <label class="form-label">Add-ons</label>
            <div
              v-for="(addon, index) in form.addons"
              :key="index"
              class="row g-2 mb-2 align-items-end"
            >
              <div class="col-md-5">
                <input
                  v-model="addon.name"
                  type="text"
                  class="form-control form-control-sm"
                  placeholder="Add-on name"
                />
              </div>
              <div class="col-md-4">
                <input
                  v-model="addon.amount"
                  type="number"
                  class="form-control form-control-sm"
                  placeholder="Amount"
                />
              </div>
              <div class="col-md-3">
                <button type="button" class="btn btn-sm btn-outline-danger" @click="removeAddon(index)">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </div>
            <button type="button" class="btn btn-sm btn-outline-secondary" @click="addAddon">
              <i class="bi bi-plus me-1"></i>Add Add-on
            </button>
          </div>

          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-info text-white" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              Submit Stock Out
            </button>
            <router-link to="/stock" class="btn btn-secondary">Cancel</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import stockApi from '@/api/stock'
import itemsApi from '@/api/items'
import customersApi from '@/api/customers'
import PageHeader from '@/components/common/PageHeader.vue'
import FormInput from '@/components/common/FormInput.vue'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const appStore = useAppStore()

const submitting = ref(false)
const customers = ref([])
const itemSearch = ref('')
const itemResults = ref([])
const showItemDropdown = ref(false)
const selectedItem = ref(null)

let searchTimeout = null

const docTypeOptions = [
  { value: '', label: 'Select...' },
  { value: 'SALES_INVOICE', label: 'Sales Invoice' },
  { value: 'DELIVERY_RECEIPT', label: 'Delivery Receipt' },
  { value: 'TRANSFER', label: 'Transfer' },
  { value: 'CONSUMPTION', label: 'Consumption' },
  { value: 'OTHER', label: 'Other' }
]

const discountTypeOptions = [
  { value: '', label: 'None' },
  { value: 'PERCENTAGE', label: 'Percentage (%)' },
  { value: 'FIXED', label: 'Fixed Amount' }
]

const form = reactive({
  itemId: '',
  quantity: '',
  unitPrice: '',
  customerId: '',
  documentType: '',
  referenceNo: '',
  transactionDate: new Date().toISOString().split('T')[0],
  discountType: '',
  discountValue: '',
  taxable: false,
  remarks: '',
  addons: []
})

const errors = reactive({
  itemId: '',
  quantity: '',
  unitPrice: '',
  customerId: '',
  documentType: '',
  referenceNo: '',
  transactionDate: ''
})

const customerOptions = computed(() =>
  customers.value.map(c => ({ value: c.id, label: c.name }))
)

const computedDiscount = computed(() => {
  const qty = Number(form.quantity) || 0
  const price = Number(form.unitPrice) || 0
  const value = Number(form.discountValue) || 0
  const subtotal = qty * price

  if (form.discountType === 'PERCENTAGE') {
    return subtotal * (value / 100)
  } else if (form.discountType === 'FIXED') {
    return value
  }
  return 0
})

onMounted(async () => {
  try {
    const { data } = await customersApi.getAll({ size: 100, active: true })
    customers.value = data.content || data
  } catch (error) {
    // Non-critical
  }

  document.addEventListener('click', handleClickOutside)
})

function handleClickOutside(e) {
  if (!e.target.closest('.position-relative')) {
    showItemDropdown.value = false
  }
}

function searchItems() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(async () => {
    if (itemSearch.value.length < 2) {
      itemResults.value = []
      return
    }
    try {
      const { data } = await itemsApi.search(itemSearch.value, { size: 10 })
      itemResults.value = data.content || data
      showItemDropdown.value = true
    } catch (error) {
      itemResults.value = []
    }
  }, 300)
}

function selectItem(item) {
  selectedItem.value = item
  form.itemId = item.id
  form.unitPrice = item.price || ''
  itemSearch.value = `${item.itemCode} — ${item.name}`
  showItemDropdown.value = false
  itemResults.value = []
}

function addAddon() {
  form.addons.push({ name: '', amount: '' })
}

function removeAddon(index) {
  form.addons.splice(index, 1)
}

function validate() {
  let valid = true
  Object.keys(errors).forEach(k => (errors[k] = ''))

  if (!form.itemId) { errors.itemId = 'Please select an item'; valid = false }
  if (!form.quantity || Number(form.quantity) <= 0) { errors.quantity = 'Quantity must be greater than 0'; valid = false }
  if (!form.unitPrice || Number(form.unitPrice) < 0) { errors.unitPrice = 'Valid unit price is required'; valid = false }
  if (!form.transactionDate) { errors.transactionDate = 'Transaction date is required'; valid = false }

  // Stock validation
  if (selectedItem.value && Number(form.quantity) > selectedItem.value.qtyOnHand) {
    errors.quantity = `Insufficient stock. Available: ${selectedItem.value.qtyOnHand}`
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validate()) return

  submitting.value = true
  try {
    const payload = {
      itemId: form.itemId,
      quantity: Number(form.quantity),
      unitPrice: Number(form.unitPrice),
      customerId: form.customerId || null,
      documentType: form.documentType || null,
      referenceNo: form.referenceNo || null,
      transactionDate: form.transactionDate,
      discountType: form.discountType || null,
      discountValue: Number(form.discountValue) || 0,
      taxable: form.taxable,
      remarks: form.remarks || null,
      addons: form.addons.filter(a => a.name && a.amount).map(a => ({
        name: a.name,
        amount: Number(a.amount)
      }))
    }

    await stockApi.stockOut(payload)
    appStore.showToast('Stock Out transaction created successfully')
    router.push('/stock')
  } catch (error) {
    const data = error.response?.data
    appStore.showToast(data?.message || 'Failed to create stock out transaction', 'error')
  } finally {
    submitting.value = false
  }
}

function formatCurrency(value) {
  if (!value) return '₱0.00'
  return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(value)
}
</script>

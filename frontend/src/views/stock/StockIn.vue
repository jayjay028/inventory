<template>
  <div>
    <PageHeader
      title="Stock In"
      subtitle="Receive inventory"
      :breadcrumbs="[
        { label: 'Dashboard', route: '/dashboard' },
        { label: 'Stock Overview', route: '/stock' },
        { label: 'Stock In' }
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
              <span class="ms-2 badge bg-info">Current Stock: {{ selectedItem.qtyOnHand }}</span>
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
                v-model="form.unitCost"
                label="Unit Cost"
                type="number"
                placeholder="0.00"
                required
                :error="errors.unitCost"
              />
            </div>
            <div class="col-md-4">
              <FormInput
                v-model="form.supplierId"
                label="Supplier"
                type="select"
                :options="supplierOptions"
                :error="errors.supplierId"
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
            <button type="submit" class="btn btn-success" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              Submit Stock In
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
import suppliersApi from '@/api/suppliers'
import PageHeader from '@/components/common/PageHeader.vue'
import FormInput from '@/components/common/FormInput.vue'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const appStore = useAppStore()

const submitting = ref(false)
const suppliers = ref([])
const itemSearch = ref('')
const itemResults = ref([])
const showItemDropdown = ref(false)
const selectedItem = ref(null)

let searchTimeout = null

const docTypeOptions = [
  { value: '', label: 'Select...' },
  { value: 'PURCHASE_ORDER', label: 'Purchase Order' },
  { value: 'DELIVERY_RECEIPT', label: 'Delivery Receipt' },
  { value: 'RETURN', label: 'Return' },
  { value: 'TRANSFER', label: 'Transfer' },
  { value: 'OTHER', label: 'Other' }
]

const form = reactive({
  itemId: '',
  quantity: '',
  unitCost: '',
  supplierId: '',
  documentType: '',
  referenceNo: '',
  transactionDate: new Date().toISOString().split('T')[0],
  taxable: false,
  remarks: '',
  addons: []
})

const errors = reactive({
  itemId: '',
  quantity: '',
  unitCost: '',
  supplierId: '',
  documentType: '',
  referenceNo: '',
  transactionDate: ''
})

const supplierOptions = computed(() =>
  suppliers.value.map(s => ({ value: s.id, label: s.name }))
)

onMounted(async () => {
  try {
    const { data } = await suppliersApi.getAll({ size: 100, active: true })
    suppliers.value = data.content || data
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
  if (!form.unitCost || Number(form.unitCost) < 0) { errors.unitCost = 'Valid unit cost is required'; valid = false }
  if (!form.transactionDate) { errors.transactionDate = 'Transaction date is required'; valid = false }

  return valid
}

async function handleSubmit() {
  if (!validate()) return

  submitting.value = true
  try {
    const payload = {
      itemId: form.itemId,
      quantity: Number(form.quantity),
      unitCost: Number(form.unitCost),
      supplierId: form.supplierId || null,
      documentType: form.documentType || null,
      referenceNo: form.referenceNo || null,
      transactionDate: form.transactionDate,
      taxable: form.taxable,
      remarks: form.remarks || null,
      addons: form.addons.filter(a => a.name && a.amount).map(a => ({
        name: a.name,
        amount: Number(a.amount)
      }))
    }

    await stockApi.stockIn(payload)
    appStore.showToast('Stock In transaction created successfully')
    router.push('/stock')
  } catch (error) {
    const data = error.response?.data
    appStore.showToast(data?.message || 'Failed to create stock in transaction', 'error')
  } finally {
    submitting.value = false
  }
}
</script>

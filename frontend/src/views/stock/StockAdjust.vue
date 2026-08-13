<template>
  <div>
    <PageHeader
      title="Stock Adjustment"
      subtitle="Adjust inventory quantities"
      :breadcrumbs="[
        { label: 'Dashboard', route: '/dashboard' },
        { label: 'Stock Overview', route: '/stock' },
        { label: 'Adjustment' }
      ]"
    />

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <form @submit.prevent="handleSubmit" style="max-width: 600px;">
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
            <div class="col-md-6">
              <FormInput
                v-model="form.quantity"
                label="Adjustment Quantity"
                type="number"
                placeholder="Enter positive or negative value"
                required
                help-text="Use positive to add, negative to reduce"
                :error="errors.quantity"
              />
            </div>
            <div class="col-md-6">
              <FormInput
                v-model="form.transactionDate"
                label="Transaction Date"
                type="date"
                required
                :error="errors.transactionDate"
              />
            </div>
          </div>

          <FormInput
            v-model="form.remarks"
            label="Remarks"
            type="textarea"
            placeholder="Reason for adjustment (required)"
            required
            :error="errors.remarks"
          />

          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-warning" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              Submit Adjustment
            </button>
            <router-link to="/stock" class="btn btn-secondary">Cancel</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import stockApi from '@/api/stock'
import itemsApi from '@/api/items'
import PageHeader from '@/components/common/PageHeader.vue'
import FormInput from '@/components/common/FormInput.vue'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const appStore = useAppStore()

const submitting = ref(false)
const itemSearch = ref('')
const itemResults = ref([])
const showItemDropdown = ref(false)
const selectedItem = ref(null)

let searchTimeout = null

const form = reactive({
  itemId: '',
  quantity: '',
  transactionDate: new Date().toISOString().split('T')[0],
  remarks: ''
})

const errors = reactive({
  itemId: '',
  quantity: '',
  transactionDate: '',
  remarks: ''
})

onMounted(() => {
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

function validate() {
  let valid = true
  Object.keys(errors).forEach(k => (errors[k] = ''))

  if (!form.itemId) { errors.itemId = 'Please select an item'; valid = false }
  if (!form.quantity || Number(form.quantity) === 0) { errors.quantity = 'Quantity cannot be zero'; valid = false }
  if (!form.transactionDate) { errors.transactionDate = 'Transaction date is required'; valid = false }
  if (!form.remarks.trim()) { errors.remarks = 'Remarks are required for adjustments'; valid = false }

  return valid
}

async function handleSubmit() {
  if (!validate()) return

  submitting.value = true
  try {
    const payload = {
      itemId: form.itemId,
      quantity: Number(form.quantity),
      transactionDate: form.transactionDate,
      remarks: form.remarks
    }

    await stockApi.stockAdjust(payload)
    appStore.showToast('Stock adjustment created successfully')
    router.push('/stock')
  } catch (error) {
    const data = error.response?.data
    appStore.showToast(data?.message || 'Failed to create adjustment', 'error')
  } finally {
    submitting.value = false
  }
}
</script>

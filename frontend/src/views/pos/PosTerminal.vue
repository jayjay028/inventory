<template>
  <div class="pos-terminal d-flex flex-column vh-100">
    <!-- Top bar -->
    <div class="bg-dark text-white px-3 py-2 d-flex justify-content-between align-items-center">
      <h5 class="mb-0"><i class="bi bi-cart3 me-2"></i>Point of Sale</h5>
      <button class="btn btn-outline-light btn-sm" @click="exitPos">
        <i class="bi bi-x-lg me-1"></i>Exit POS
      </button>
    </div>

    <div class="flex-grow-1 d-flex overflow-hidden">
      <!-- Left panel: Item search & grid -->
      <div class="d-flex flex-column flex-grow-1 border-end" style="flex-basis: 60%;">
        <div class="p-3 border-bottom">
          <div class="input-group">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input
              v-model="searchQuery"
              type="text"
              class="form-control"
              placeholder="Search items by name or code..."
              @input="handleSearch"
            />
          </div>
        </div>

        <div class="flex-grow-1 overflow-auto p-3">
          <div v-if="searchLoading" class="text-center py-5">
            <span class="spinner-border"></span>
          </div>
          <div v-else-if="!searchResults.length" class="text-center py-5 text-muted">
            <i class="bi bi-search fs-1 d-block mb-2"></i>
            <p>Search for items to add to cart</p>
          </div>
          <div v-else class="row g-2">
            <div v-for="item in searchResults" :key="item.id" class="col-6 col-md-4 col-lg-3">
              <div
                class="card h-100 border cursor-pointer item-card"
                :class="{ 'opacity-50': item.qtyOnHand <= 0 }"
                @click="addToCart(item)"
              >
                <div class="card-body p-2 text-center">
                  <div class="fw-bold small text-truncate">{{ item.name }}</div>
                  <div class="text-primary fw-bold mt-1">{{ formatCurrency(item.price) }}</div>
                  <small class="text-muted">Stock: {{ item.qtyOnHand }}</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right panel: Cart -->
      <div class="d-flex flex-column bg-light" style="flex-basis: 40%; min-width: 350px;">
        <div class="p-3 border-bottom bg-white">
          <h6 class="mb-0"><i class="bi bi-bag me-1"></i>Current Sale ({{ cart.length }} items)</h6>
        </div>

        <div class="flex-grow-1 overflow-auto">
          <div v-if="!cart.length" class="text-center py-5 text-muted">
            <i class="bi bi-cart fs-1 d-block mb-2"></i>
            <p>Cart is empty</p>
          </div>
          <div v-else class="list-group list-group-flush">
            <div v-for="(cartItem, index) in cart" :key="index" class="list-group-item">
              <div class="d-flex justify-content-between align-items-start">
                <div class="flex-grow-1">
                  <div class="fw-medium small">{{ cartItem.name }}</div>
                  <small class="text-muted">{{ formatCurrency(cartItem.price) }} each</small>
                </div>
                <button class="btn btn-sm btn-link text-danger p-0" @click="removeFromCart(index)">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
              <div class="d-flex align-items-center justify-content-between mt-2">
                <div class="input-group input-group-sm" style="width: 120px;">
                  <button class="btn btn-outline-secondary" type="button" @click="updateQty(index, -1)">
                    <i class="bi bi-dash"></i>
                  </button>
                  <input
                    :value="cartItem.quantity"
                    type="number"
                    class="form-control text-center"
                    min="1"
                    @change="setQty(index, $event.target.value)"
                  />
                  <button class="btn btn-outline-secondary" type="button" @click="updateQty(index, 1)">
                    <i class="bi bi-plus"></i>
                  </button>
                </div>
                <span class="fw-bold">{{ formatCurrency(cartItem.price * cartItem.quantity) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Bottom: Totals & Pay -->
        <div class="bg-white border-top p-3">
          <!-- Discount -->
          <div class="row g-2 mb-2">
            <div class="col-6">
              <select v-model="discount.type" class="form-select form-select-sm">
                <option value="">No Discount</option>
                <option value="PERCENTAGE">% Discount</option>
                <option value="FIXED">Fixed Discount</option>
              </select>
            </div>
            <div class="col-6">
              <input
                v-if="discount.type"
                v-model="discount.value"
                type="number"
                class="form-control form-control-sm"
                placeholder="Value"
                min="0"
              />
            </div>
          </div>

          <div class="d-flex justify-content-between mb-1">
            <span class="text-muted">Subtotal:</span>
            <span>{{ formatCurrency(subtotal) }}</span>
          </div>
          <div v-if="discountAmount > 0" class="d-flex justify-content-between mb-1">
            <span class="text-muted">Discount:</span>
            <span class="text-danger">-{{ formatCurrency(discountAmount) }}</span>
          </div>
          <div class="d-flex justify-content-between mb-1">
            <span class="text-muted">Tax (12%):</span>
            <span>{{ formatCurrency(taxAmount) }}</span>
          </div>
          <div class="d-flex justify-content-between fw-bold fs-5 border-top pt-2 mt-2">
            <span>Total:</span>
            <span class="text-primary">{{ formatCurrency(total) }}</span>
          </div>

          <button
            class="btn btn-primary btn-lg w-100 mt-3"
            :disabled="!cart.length || processing"
            @click="processSale"
          >
            <span v-if="processing" class="spinner-border spinner-border-sm me-1"></span>
            <i v-else class="bi bi-credit-card me-1"></i>
            Pay {{ formatCurrency(total) }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import posApi from '@/api/pos'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const appStore = useAppStore()

const searchQuery = ref('')
const searchResults = ref([])
const searchLoading = ref(false)
const cart = ref([])
const processing = ref(false)

const discount = reactive({
  type: '',
  value: 0
})

let searchTimeout = null

const subtotal = computed(() =>
  cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
)

const discountAmount = computed(() => {
  if (discount.type === 'PERCENTAGE') {
    return subtotal.value * (Number(discount.value) / 100)
  } else if (discount.type === 'FIXED') {
    return Number(discount.value) || 0
  }
  return 0
})

const taxAmount = computed(() => (subtotal.value - discountAmount.value) * 0.12)

const total = computed(() => subtotal.value - discountAmount.value + taxAmount.value)

function handleSearch() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(async () => {
    if (searchQuery.value.length < 1) {
      searchResults.value = []
      return
    }
    searchLoading.value = true
    try {
      const { data } = await posApi.searchItems(searchQuery.value, { size: 20 })
      searchResults.value = data.content || data
    } catch (error) {
      searchResults.value = []
    } finally {
      searchLoading.value = false
    }
  }, 300)
}

function addToCart(item) {
  if (item.qtyOnHand <= 0) {
    appStore.showToast('Item is out of stock', 'warning')
    return
  }

  const existing = cart.value.find(c => c.itemId === item.id)
  if (existing) {
    if (existing.quantity >= item.qtyOnHand) {
      appStore.showToast('Cannot exceed available stock', 'warning')
      return
    }
    existing.quantity++
  } else {
    cart.value.push({
      itemId: item.id,
      name: item.name,
      price: item.price,
      quantity: 1,
      maxQty: item.qtyOnHand
    })
  }
}

function removeFromCart(index) {
  cart.value.splice(index, 1)
}

function updateQty(index, delta) {
  const item = cart.value[index]
  const newQty = item.quantity + delta
  if (newQty <= 0) {
    removeFromCart(index)
  } else if (newQty <= item.maxQty) {
    item.quantity = newQty
  } else {
    appStore.showToast('Cannot exceed available stock', 'warning')
  }
}

function setQty(index, value) {
  const qty = parseInt(value) || 1
  const item = cart.value[index]
  if (qty > item.maxQty) {
    appStore.showToast('Cannot exceed available stock', 'warning')
    item.quantity = item.maxQty
  } else if (qty <= 0) {
    removeFromCart(index)
  } else {
    item.quantity = qty
  }
}

async function processSale() {
  processing.value = true
  try {
    const payload = {
      items: cart.value.map(item => ({
        itemId: item.itemId,
        quantity: item.quantity,
        unitPrice: item.price
      })),
      discountType: discount.type || null,
      discountValue: Number(discount.value) || 0,
      paymentMethod: 'CASH'
    }

    await posApi.createSale(payload)
    appStore.showToast('Sale completed successfully!')
    cart.value = []
    discount.type = ''
    discount.value = 0
    searchResults.value = []
    searchQuery.value = ''
  } catch (error) {
    const msg = error.response?.data?.message || 'Failed to process sale'
    appStore.showToast(msg, 'error')
  } finally {
    processing.value = false
  }
}

function exitPos() {
  router.push('/dashboard')
}

function formatCurrency(value) {
  if (value == null) return '₱0.00'
  return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(value)
}
</script>

<style scoped>
.pos-terminal {
  background: #f8f9fa;
}

.item-card {
  transition: transform 0.15s, box-shadow 0.15s;
}

.item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.cursor-pointer {
  cursor: pointer;
}
</style>

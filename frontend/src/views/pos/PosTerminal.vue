<template>
  <div class="pos-terminal">
    <!-- Header Bar -->
    <header class="pos-header">
      <div class="pos-header__left">
        <i class="bi bi-upc-scan pos-header__icon"></i>
        <h1 class="pos-header__title">Point of Sale</h1>
      </div>
      <div class="pos-header__center">
        <span class="pos-header__shift">
          <i class="bi bi-clock me-1"></i>Active Session
        </span>
      </div>
      <div class="pos-header__right">
        <button class="pos-exit-btn" @click="exitPos">
          <i class="bi bi-box-arrow-left me-1"></i>Exit
        </button>
      </div>
    </header>

    <!-- Main Content -->
    <div class="pos-body">
      <!-- Left Panel: Search & Products -->
      <div class="pos-products">
        <!-- Search Bar -->
        <div class="pos-search">
          <div class="pos-search__wrapper">
            <i class="bi bi-upc-scan pos-search__icon"></i>
            <input
              v-model="searchQuery"
              type="text"
              class="pos-search__input"
              placeholder="Scan barcode or search items..."
              @input="handleSearch"
            />
            <span v-if="searchQuery" class="pos-search__clear" @click="searchQuery = ''; searchResults = []">
              <i class="bi bi-x-lg"></i>
            </span>
          </div>
        </div>

        <!-- Product Grid -->
        <div class="pos-products__grid">
          <!-- Loading State -->
          <div v-if="searchLoading" class="pos-products__empty">
            <div class="pos-spinner"></div>
            <p>Searching items...</p>
          </div>

          <!-- Empty/Initial State -->
          <div v-else-if="!searchResults.length" class="pos-products__empty">
            <i class="bi bi-search pos-products__empty-icon"></i>
            <p class="pos-products__empty-title">Search for items</p>
            <p class="pos-products__empty-sub">Type a product name or scan a barcode to get started</p>
          </div>

          <!-- Results Grid -->
          <div v-else class="pos-grid">
            <div
              v-for="item in searchResults"
              :key="item.id"
              class="pos-item-card"
              :class="{ 'pos-item-card--disabled': item.qtyOnHand <= 0 }"
              @click="addToCart(item)"
            >
              <div class="pos-item-card__name">{{ item.name }}</div>
              <div class="pos-item-card__price">{{ formatCurrency(item.price) }}</div>
              <div class="pos-item-card__stock" :class="{ 'pos-item-card__stock--out': item.qtyOnHand <= 0 }">
                <i class="bi bi-box-seam"></i>
                {{ item.qtyOnHand > 0 ? item.qtyOnHand + ' in stock' : 'Out of stock' }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Panel: Cart -->
      <div class="pos-cart">
        <!-- Cart Header -->
        <div class="pos-cart__header">
          <h2 class="pos-cart__title">
            <i class="bi bi-bag me-2"></i>Current Sale
          </h2>
          <span class="pos-cart__count">{{ cart.length }} {{ cart.length === 1 ? 'item' : 'items' }}</span>
        </div>

        <!-- Cart Items -->
        <div class="pos-cart__items">
          <!-- Empty Cart -->
          <div v-if="!cart.length" class="pos-cart__empty">
            <i class="bi bi-cart3 pos-cart__empty-icon"></i>
            <p class="pos-cart__empty-text">No items in cart</p>
            <p class="pos-cart__empty-hint">Search and click items to add them here</p>
          </div>

          <!-- Cart Item List -->
          <div v-else class="pos-cart__list">
            <div v-for="(cartItem, index) in cart" :key="index" class="pos-cart-item">
              <div class="pos-cart-item__top">
                <div class="pos-cart-item__info">
                  <span class="pos-cart-item__name">{{ cartItem.name }}</span>
                  <span class="pos-cart-item__unit-price">{{ formatCurrency(cartItem.price) }} each</span>
                </div>
                <button class="pos-cart-item__remove" @click="removeFromCart(index)" title="Remove item">
                  <i class="bi bi-trash3"></i>
                </button>
              </div>
              <div class="pos-cart-item__bottom">
                <div class="pos-qty-control">
                  <button class="pos-qty-control__btn" @click="updateQty(index, -1)">
                    <i class="bi bi-dash"></i>
                  </button>
                  <input
                    :value="cartItem.quantity"
                    type="number"
                    class="pos-qty-control__input"
                    min="1"
                    @change="setQty(index, $event.target.value)"
                  />
                  <button class="pos-qty-control__btn" @click="updateQty(index, 1)">
                    <i class="bi bi-plus"></i>
                  </button>
                </div>
                <span class="pos-cart-item__line-total">{{ formatCurrency(cartItem.price * cartItem.quantity) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Cart Footer: Totals & Actions -->
        <div class="pos-cart__footer">
          <!-- Discount Controls -->
          <div class="pos-discount">
            <div class="pos-discount__options">
              <label class="pos-discount__radio">
                <input type="radio" v-model="discount.type" value="" name="discountType" />
                <span>No Discount</span>
              </label>
              <label class="pos-discount__radio">
                <input type="radio" v-model="discount.type" value="PERCENTAGE" name="discountType" />
                <span>%</span>
              </label>
              <label class="pos-discount__radio">
                <input type="radio" v-model="discount.type" value="FIXED" name="discountType" />
                <span>Fixed</span>
              </label>
            </div>
            <input
              v-if="discount.type"
              v-model="discount.value"
              type="number"
              class="pos-discount__input"
              placeholder="Value"
              min="0"
            />
          </div>

          <!-- Totals -->
          <div class="pos-totals">
            <div class="pos-totals__row">
              <span class="pos-totals__label">Subtotal</span>
              <span class="pos-totals__value">{{ formatCurrency(subtotal) }}</span>
            </div>
            <div v-if="discountAmount > 0" class="pos-totals__row pos-totals__row--discount">
              <span class="pos-totals__label">Discount</span>
              <span class="pos-totals__value">−{{ formatCurrency(discountAmount) }}</span>
            </div>
            <div class="pos-totals__row">
              <span class="pos-totals__label">Tax (12%)</span>
              <span class="pos-totals__value">{{ formatCurrency(taxAmount) }}</span>
            </div>
            <div class="pos-totals__row pos-totals__row--grand">
              <span class="pos-totals__label">Total</span>
              <span class="pos-totals__value">{{ formatCurrency(total) }}</span>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="pos-actions">
            <button
              class="pos-actions__pay"
              :disabled="!cart.length || processing"
              @click="processSale"
            >
              <span v-if="processing" class="pos-spinner pos-spinner--sm"></span>
              <i v-else class="bi bi-credit-card me-2"></i>
              Pay {{ formatCurrency(total) }}
            </button>
            <button
              class="pos-actions__clear"
              :disabled="!cart.length"
              @click="cart = []; discount.type = ''; discount.value = 0"
            >
              <i class="bi bi-x-circle me-1"></i>Clear
            </button>
          </div>
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
/* ===== POS Terminal Layout ===== */
.pos-terminal {
  position: fixed;
  inset: 0;
  display: grid;
  grid-template-rows: 56px 1fr;
  grid-template-columns: 1fr 380px;
  background-color: #f9fafb;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  overflow: hidden;
  z-index: 1050;
}

/* ===== Header ===== */
.pos-header {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.25rem;
  background-color: #0f172a;
  color: #ffffff;
}

.pos-header__left {
  display: flex;
  align-items: center;
  gap: 0.625rem;
}

.pos-header__icon {
  font-size: 1.25rem;
  opacity: 0.8;
}

.pos-header__title {
  font-size: 1.0625rem;
  font-weight: 600;
  margin: 0;
  letter-spacing: -0.01em;
}

.pos-header__center {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.pos-header__shift {
  font-size: 0.75rem;
  color: #94a3b8;
  font-weight: 400;
}

.pos-header__right {
  display: flex;
  align-items: center;
}

.pos-exit-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.375rem 0.875rem;
  font-size: 0.8125rem;
  font-weight: 500;
  color: #e2e8f0;
  background: transparent;
  border: 1px solid #334155;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.pos-exit-btn:hover {
  background-color: #1e293b;
  border-color: #475569;
  color: #ffffff;
}

/* ===== Products Panel (Left) ===== */
.pos-products {
  grid-column: 1;
  grid-row: 2;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Search Bar */
.pos-search {
  padding: 1rem 1.25rem;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.pos-search__wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.pos-search__icon {
  position: absolute;
  left: 1rem;
  font-size: 1.125rem;
  color: #94a3b8;
  pointer-events: none;
}

.pos-search__input {
  width: 100%;
  padding: 0.75rem 2.5rem 0.75rem 2.75rem;
  font-size: 0.9375rem;
  font-weight: 400;
  color: #1e293b;
  background: #f8fafc;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  outline: none;
  transition: all 0.15s ease;
}

.pos-search__input::placeholder {
  color: #94a3b8;
}

.pos-search__input:focus {
  background: #ffffff;
  border-color: #1e40af;
  box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.08);
}

.pos-search__clear {
  position: absolute;
  right: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.5rem;
  height: 1.5rem;
  font-size: 0.75rem;
  color: #64748b;
  cursor: pointer;
  border-radius: 50%;
  transition: background 0.15s ease;
}

.pos-search__clear:hover {
  background: #e2e8f0;
  color: #1e293b;
}

/* Product Grid Area */
.pos-products__grid {
  flex: 1;
  overflow-y: auto;
  padding: 1.25rem;
}

.pos-products__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 300px;
  text-align: center;
  color: #94a3b8;
}

.pos-products__empty-icon {
  font-size: 2.5rem;
  margin-bottom: 0.75rem;
  opacity: 0.5;
}

.pos-products__empty-title {
  font-size: 1rem;
  font-weight: 500;
  color: #64748b;
  margin: 0 0 0.25rem;
}

.pos-products__empty-sub {
  font-size: 0.8125rem;
  color: #94a3b8;
  margin: 0;
}

/* Product Grid */
.pos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 0.75rem;
}

/* Product Card */
.pos-item-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1rem 0.75rem;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
  text-align: center;
  min-height: 100px;
}

.pos-item-card:hover {
  border-color: #1e40af;
  box-shadow: 0 2px 8px rgba(30, 64, 175, 0.08);
  transform: translateY(-1px);
}

.pos-item-card:active {
  transform: translateY(0) scale(0.98);
}

.pos-item-card--disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.pos-item-card--disabled:hover {
  border-color: #e5e7eb;
  box-shadow: none;
  transform: none;
}

.pos-item-card__name {
  font-size: 0.8125rem;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.3;
  margin-bottom: 0.375rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.pos-item-card__price {
  font-size: 0.9375rem;
  font-weight: 700;
  color: #1e40af;
  margin-bottom: 0.25rem;
}

.pos-item-card__stock {
  font-size: 0.6875rem;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.pos-item-card__stock--out {
  color: #dc2626;
  font-weight: 500;
}

/* ===== Cart Panel (Right) ===== */
.pos-cart {
  grid-column: 2;
  grid-row: 2;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-left: 1px solid #e5e7eb;
  overflow: hidden;
}

.pos-cart__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.875rem 1.25rem;
  border-bottom: 1px solid #f1f5f9;
}

.pos-cart__title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.pos-cart__count {
  font-size: 0.75rem;
  font-weight: 500;
  color: #64748b;
  background: #f1f5f9;
  padding: 0.25rem 0.625rem;
  border-radius: 100px;
}

/* Cart Items */
.pos-cart__items {
  flex: 1;
  overflow-y: auto;
}

.pos-cart__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 2rem;
  text-align: center;
}

.pos-cart__empty-icon {
  font-size: 2.25rem;
  color: #cbd5e1;
  margin-bottom: 0.75rem;
}

.pos-cart__empty-text {
  font-size: 0.875rem;
  font-weight: 500;
  color: #94a3b8;
  margin: 0 0 0.25rem;
}

.pos-cart__empty-hint {
  font-size: 0.75rem;
  color: #cbd5e1;
  margin: 0;
}

.pos-cart__list {
  padding: 0;
}

/* Cart Item Row */
.pos-cart-item {
  padding: 0.75rem 1.25rem;
  border-bottom: 1px solid #f8fafc;
  transition: background 0.1s ease;
}

.pos-cart-item:hover {
  background: #fafbfc;
}

.pos-cart-item__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.pos-cart-item__info {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
  min-width: 0;
}

.pos-cart-item__name {
  font-size: 0.8125rem;
  font-weight: 500;
  color: #1e293b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pos-cart-item__unit-price {
  font-size: 0.6875rem;
  color: #94a3b8;
}

.pos-cart-item__remove {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.5rem;
  height: 1.5rem;
  font-size: 0.75rem;
  color: #94a3b8;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s ease;
  flex-shrink: 0;
}

.pos-cart-item__remove:hover {
  color: #dc2626;
  background: #fef2f2;
}

.pos-cart-item__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pos-cart-item__line-total {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1e293b;
}

/* Quantity Control */
.pos-qty-control {
  display: inline-flex;
  align-items: center;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}

.pos-qty-control__btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.75rem;
  height: 1.75rem;
  font-size: 0.875rem;
  color: #475569;
  background: #f8fafc;
  border: none;
  cursor: pointer;
  transition: background 0.1s ease;
}

.pos-qty-control__btn:hover {
  background: #e2e8f0;
  color: #1e293b;
}

.pos-qty-control__btn:active {
  background: #cbd5e1;
}

.pos-qty-control__input {
  width: 2.25rem;
  height: 1.75rem;
  text-align: center;
  font-size: 0.8125rem;
  font-weight: 600;
  color: #1e293b;
  border: none;
  border-left: 1px solid #e5e7eb;
  border-right: 1px solid #e5e7eb;
  outline: none;
  background: #ffffff;
  -moz-appearance: textfield;
}

.pos-qty-control__input::-webkit-outer-spin-button,
.pos-qty-control__input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* ===== Cart Footer ===== */
.pos-cart__footer {
  border-top: 2px solid #e5e7eb;
  padding: 1rem 1.25rem;
  background: #f9fafb;
}

/* Discount Controls */
.pos-discount {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.875rem;
  padding-bottom: 0.875rem;
  border-bottom: 1px solid #f1f5f9;
}

.pos-discount__options {
  display: flex;
  gap: 0.125rem;
  background: #e5e7eb;
  border-radius: 6px;
  padding: 2px;
}

.pos-discount__radio {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin: 0;
}

.pos-discount__radio input[type="radio"] {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.pos-discount__radio span {
  padding: 0.3rem 0.625rem;
  font-size: 0.6875rem;
  font-weight: 500;
  color: #64748b;
  border-radius: 4px;
  transition: all 0.15s ease;
  white-space: nowrap;
}

.pos-discount__radio input[type="radio"]:checked + span {
  background: #ffffff;
  color: #1e293b;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.pos-discount__input {
  width: 72px;
  padding: 0.35rem 0.5rem;
  font-size: 0.8125rem;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  outline: none;
  text-align: center;
  transition: border-color 0.15s ease;
  -moz-appearance: textfield;
}

.pos-discount__input:focus {
  border-color: #1e40af;
}

.pos-discount__input::-webkit-outer-spin-button,
.pos-discount__input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* Totals */
.pos-totals {
  margin-bottom: 1rem;
}

.pos-totals__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.25rem 0;
}

.pos-totals__label {
  font-size: 0.8125rem;
  color: #64748b;
}

.pos-totals__value {
  font-size: 0.8125rem;
  font-weight: 500;
  color: #1e293b;
}

.pos-totals__row--discount .pos-totals__value {
  color: #dc2626;
}

.pos-totals__row--grand {
  margin-top: 0.5rem;
  padding-top: 0.625rem;
  border-top: 2px solid #1e293b;
}

.pos-totals__row--grand .pos-totals__label {
  font-size: 1rem;
  font-weight: 700;
  color: #0f172a;
}

.pos-totals__row--grand .pos-totals__value {
  font-size: 1.375rem;
  font-weight: 700;
  color: #0f172a;
}

/* Action Buttons */
.pos-actions {
  display: flex;
  gap: 0.5rem;
}

.pos-actions__pay {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.75rem 1rem;
  font-size: 0.9375rem;
  font-weight: 600;
  color: #ffffff;
  background: #0f172a;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.pos-actions__pay:hover:not(:disabled) {
  background: #1e293b;
}

.pos-actions__pay:active:not(:disabled) {
  transform: scale(0.98);
}

.pos-actions__pay:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pos-actions__clear {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.75rem 1rem;
  font-size: 0.8125rem;
  font-weight: 500;
  color: #64748b;
  background: transparent;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.pos-actions__clear:hover:not(:disabled) {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #1e293b;
}

.pos-actions__clear:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ===== Spinner ===== */
.pos-spinner {
  width: 2rem;
  height: 2rem;
  border: 3px solid #e5e7eb;
  border-top-color: #1e40af;
  border-radius: 50%;
  animation: pos-spin 0.7s linear infinite;
  margin-bottom: 0.75rem;
}

.pos-spinner--sm {
  width: 1rem;
  height: 1rem;
  border-width: 2px;
  margin: 0 0.375rem 0 0;
}

@keyframes pos-spin {
  to { transform: rotate(360deg); }
}

/* ===== Responsive ===== */
@media (max-width: 991.98px) {
  .pos-terminal {
    grid-template-columns: 1fr;
    grid-template-rows: 56px 1fr auto;
  }

  .pos-cart {
    grid-column: 1;
    grid-row: 3;
    border-left: none;
    border-top: 1px solid #e5e7eb;
    max-height: 45vh;
  }

  .pos-header__center {
    display: none;
  }
}

@media (max-width: 575.98px) {
  .pos-grid {
    grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  }

  .pos-search__input {
    font-size: 0.875rem;
    padding: 0.625rem 2.25rem 0.625rem 2.5rem;
  }

  .pos-cart__footer {
    padding: 0.875rem 1rem;
  }

  .pos-totals__row--grand .pos-totals__value {
    font-size: 1.125rem;
  }
}
</style>

<template>
  <div>
    <PageHeader
      title="Settings"
      subtitle="System configuration"
      :breadcrumbs="[{ label: 'Dashboard', route: '/dashboard' }, { label: 'Settings' }]"
    />

    <div v-if="loading" class="text-center py-5">
      <span class="spinner-border"></span>
    </div>

    <template v-else>
      <!-- Tabs -->
      <ul class="nav nav-tabs mb-4">
        <li v-for="tab in tabs" :key="tab.key" class="nav-item">
          <button
            class="nav-link"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            <i :class="tab.icon" class="me-1"></i>{{ tab.label }}
          </button>
        </li>
      </ul>

      <div class="card border-0 shadow-sm">
        <div class="card-body" style="max-width: 700px;">
          <!-- Company Info -->
          <div v-show="activeTab === 'company'">
            <FormInput v-model="settings.companyName" label="Company Name" :error="errors.companyName" />
            <FormInput v-model="settings.companyAddress" label="Address" type="textarea" />
            <div class="row">
              <div class="col-md-6">
                <FormInput v-model="settings.companyTin" label="TIN" />
              </div>
              <div class="col-md-6">
                <FormInput v-model="settings.companyPhone" label="Phone" />
              </div>
            </div>
            <FormInput v-model="settings.companyEmail" label="Email" type="email" />
          </div>

          <!-- Tax Settings -->
          <div v-show="activeTab === 'tax'">
            <div class="form-check mb-3">
              <input id="taxEnabled" v-model="settings.taxEnabled" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="taxEnabled">Enable Tax Computation</label>
            </div>
            <div class="row">
              <div class="col-md-6">
                <FormInput v-model="settings.taxRate" label="Tax Rate (%)" type="number" />
              </div>
              <div class="col-md-6">
                <FormInput v-model="settings.taxLabel" label="Tax Label" placeholder="e.g. VAT" />
              </div>
            </div>
            <div class="form-check mb-3">
              <input id="taxInclusive" v-model="settings.taxInclusive" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="taxInclusive">Tax Inclusive Pricing</label>
            </div>
          </div>

          <!-- Document Numbering -->
          <div v-show="activeTab === 'numbering'">
            <FormInput v-model="settings.salePrefix" label="Sale Number Prefix" placeholder="e.g. SL-" />
            <FormInput v-model="settings.saleNextNumber" label="Next Sale Number" type="number" />
            <FormInput v-model="settings.stockInPrefix" label="Stock In Prefix" placeholder="e.g. SI-" />
            <FormInput v-model="settings.stockInNextNumber" label="Next Stock In Number" type="number" />
            <FormInput v-model="settings.stockOutPrefix" label="Stock Out Prefix" placeholder="e.g. SO-" />
            <FormInput v-model="settings.stockOutNextNumber" label="Next Stock Out Number" type="number" />
          </div>

          <!-- POS Settings -->
          <div v-show="activeTab === 'pos'">
            <FormInput v-model="settings.receiptHeader" label="Receipt Header" type="textarea" />
            <FormInput v-model="settings.receiptFooter" label="Receipt Footer" type="textarea" />
            <div class="form-check mb-3">
              <input id="autoPrint" v-model="settings.autoPrintReceipt" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="autoPrint">Auto-print receipt after payment</label>
            </div>
            <div class="form-check mb-3">
              <input id="requireShift" v-model="settings.requireShift" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="requireShift">Require open shift to create sales</label>
            </div>
          </div>

          <!-- Discount Settings -->
          <div v-show="activeTab === 'discount'">
            <div class="form-check mb-3">
              <input id="discountEnabled" v-model="settings.discountEnabled" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="discountEnabled">Enable Discounts</label>
            </div>
            <FormInput v-model="settings.maxDiscountPercent" label="Max Discount Percentage (%)" type="number" />
            <FormInput v-model="settings.seniorDiscountRate" label="Senior/PWD Discount Rate (%)" type="number" />
          </div>

          <!-- Save Button -->
          <div class="mt-4 pt-3 border-top">
            <button class="btn btn-primary" :disabled="saving" @click="saveSettings">
              <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
              Save Settings
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import settingsApi from '@/api/settings'
import PageHeader from '@/components/common/PageHeader.vue'
import FormInput from '@/components/common/FormInput.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const loading = ref(true)
const saving = ref(false)
const activeTab = ref('company')

const tabs = [
  { key: 'company', label: 'Company Info', icon: 'bi bi-building' },
  { key: 'tax', label: 'Tax Settings', icon: 'bi bi-percent' },
  { key: 'numbering', label: 'Document Numbering', icon: 'bi bi-hash' },
  { key: 'pos', label: 'POS Settings', icon: 'bi bi-cart3' },
  { key: 'discount', label: 'Discount Settings', icon: 'bi bi-tag' }
]

const settings = reactive({
  companyName: '',
  companyAddress: '',
  companyTin: '',
  companyPhone: '',
  companyEmail: '',
  taxEnabled: true,
  taxRate: 12,
  taxLabel: 'VAT',
  taxInclusive: false,
  salePrefix: 'SL-',
  saleNextNumber: 1,
  stockInPrefix: 'SI-',
  stockInNextNumber: 1,
  stockOutPrefix: 'SO-',
  stockOutNextNumber: 1,
  receiptHeader: '',
  receiptFooter: '',
  autoPrintReceipt: false,
  requireShift: false,
  discountEnabled: true,
  maxDiscountPercent: 50,
  seniorDiscountRate: 20
})

const errors = reactive({
  companyName: ''
})

onMounted(async () => {
  try {
    const { data } = await settingsApi.getAll()
    Object.keys(settings).forEach(key => {
      if (data[key] !== undefined) {
        settings[key] = data[key]
      }
    })
  } catch (error) {
    appStore.showToast('Failed to load settings', 'error')
  } finally {
    loading.value = false
  }
})

async function saveSettings() {
  saving.value = true
  try {
    await settingsApi.update({ ...settings })
    appStore.showToast('Settings saved successfully')
  } catch (error) {
    const data = error.response?.data
    appStore.showToast(data?.message || 'Failed to save settings', 'error')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <PageHeader
      :title="isEdit ? 'Edit Item' : 'New Item'"
      :breadcrumbs="[
        { label: 'Dashboard', route: '/dashboard' },
        { label: 'Items', route: '/items' },
        { label: isEdit ? 'Edit' : 'New' }
      ]"
    />

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <div v-if="pageLoading" class="text-center py-5">
          <span class="spinner-border"></span>
        </div>

        <form v-else @submit.prevent="handleSubmit" style="max-width: 700px;">
          <div class="row">
            <div class="col-md-6">
              <FormInput
                v-model="form.itemCode"
                label="Item Code"
                placeholder="Enter item code"
                required
                :error="errors.itemCode"
              />
            </div>
            <div class="col-md-6">
              <FormInput
                v-model="form.name"
                label="Name"
                placeholder="Enter item name"
                required
                :error="errors.name"
              />
            </div>
          </div>

          <FormInput
            v-model="form.description"
            label="Description"
            type="textarea"
            placeholder="Enter description"
            :error="errors.description"
          />

          <div class="row">
            <div class="col-md-6">
              <FormInput
                v-model="form.categoryId"
                label="Category"
                type="select"
                required
                :options="categoryOptions"
                :error="errors.categoryId"
              />
            </div>
            <div class="col-md-6">
              <FormInput
                v-model="form.unit"
                label="Unit"
                type="select"
                required
                :options="unitOptions"
                :error="errors.unit"
              />
            </div>
          </div>

          <div class="row">
            <div class="col-md-4">
              <FormInput
                v-model="form.price"
                label="Selling Price"
                type="number"
                placeholder="0.00"
                required
                :error="errors.price"
              />
            </div>
            <div class="col-md-4">
              <FormInput
                v-model="form.costPrice"
                label="Cost Price"
                type="number"
                placeholder="0.00"
                required
                :error="errors.costPrice"
              />
            </div>
            <div class="col-md-4">
              <FormInput
                v-model="form.reorderLevel"
                label="Reorder Level"
                type="number"
                placeholder="0"
                :error="errors.reorderLevel"
              />
            </div>
          </div>

          <div class="form-check mb-3">
            <input
              id="taxable"
              v-model="form.taxable"
              class="form-check-input"
              type="checkbox"
            />
            <label class="form-check-label" for="taxable">Taxable</label>
          </div>

          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              {{ isEdit ? 'Update' : 'Create' }}
            </button>
            <router-link to="/items" class="btn btn-secondary">Cancel</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import itemsApi from '@/api/items'
import categoriesApi from '@/api/categories'
import PageHeader from '@/components/common/PageHeader.vue'
import FormInput from '@/components/common/FormInput.vue'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const isEdit = computed(() => !!route.params.id)
const pageLoading = ref(false)
const submitting = ref(false)
const categories = ref([])

const unitOptions = [
  { value: 'PCS', label: 'Pieces' },
  { value: 'BOX', label: 'Box' },
  { value: 'KG', label: 'Kilogram' },
  { value: 'LTR', label: 'Liter' },
  { value: 'MTR', label: 'Meter' },
  { value: 'SET', label: 'Set' },
  { value: 'PACK', label: 'Pack' },
  { value: 'ROLL', label: 'Roll' },
  { value: 'BAG', label: 'Bag' },
  { value: 'BOTTLE', label: 'Bottle' }
]

const form = reactive({
  itemCode: '',
  name: '',
  description: '',
  categoryId: '',
  unit: '',
  price: '',
  costPrice: '',
  reorderLevel: '',
  taxable: false
})

const errors = reactive({
  itemCode: '',
  name: '',
  description: '',
  categoryId: '',
  unit: '',
  price: '',
  costPrice: '',
  reorderLevel: ''
})

const categoryOptions = computed(() =>
  categories.value.map(c => ({ value: c.id, label: c.name }))
)

onMounted(async () => {
  pageLoading.value = true
  try {
    const { data } = await categoriesApi.getAll({ size: 100, active: true })
    categories.value = data.content || data

    if (isEdit.value) {
      const { data: item } = await itemsApi.getById(route.params.id)
      form.itemCode = item.itemCode || ''
      form.name = item.name || ''
      form.description = item.description || ''
      form.categoryId = item.categoryId || ''
      form.unit = item.unit || ''
      form.price = item.price ?? ''
      form.costPrice = item.costPrice ?? ''
      form.reorderLevel = item.reorderLevel ?? ''
      form.taxable = item.taxable || false
    }
  } catch (error) {
    appStore.showToast('Failed to load data', 'error')
    router.push('/items')
  } finally {
    pageLoading.value = false
  }
})

function validate() {
  let valid = true
  Object.keys(errors).forEach(k => (errors[k] = ''))

  if (!form.itemCode.trim()) { errors.itemCode = 'Item code is required'; valid = false }
  if (!form.name.trim()) { errors.name = 'Name is required'; valid = false }
  if (!form.categoryId) { errors.categoryId = 'Category is required'; valid = false }
  if (!form.unit) { errors.unit = 'Unit is required'; valid = false }
  if (!form.price || Number(form.price) < 0) { errors.price = 'Valid price is required'; valid = false }
  if (!form.costPrice || Number(form.costPrice) < 0) { errors.costPrice = 'Valid cost price is required'; valid = false }

  return valid
}

async function handleSubmit() {
  if (!validate()) return

  submitting.value = true
  try {
    const payload = {
      ...form,
      price: Number(form.price),
      costPrice: Number(form.costPrice),
      reorderLevel: Number(form.reorderLevel) || 0
    }

    if (isEdit.value) {
      await itemsApi.update(route.params.id, payload)
      appStore.showToast('Item updated successfully')
    } else {
      await itemsApi.create(payload)
      appStore.showToast('Item created successfully')
    }
    router.push('/items')
  } catch (error) {
    const data = error.response?.data
    if (data?.errors) {
      Object.assign(errors, data.errors)
    } else {
      appStore.showToast(data?.message || 'Failed to save item', 'error')
    }
  } finally {
    submitting.value = false
  }
}
</script>

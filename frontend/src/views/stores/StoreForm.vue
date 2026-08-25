<template>
  <div>
    <PageHeader
      :title="isEdit ? 'Edit Store' : 'New Store'"
      :breadcrumbs="[
        { label: 'Dashboard', route: '/dashboard' },
        { label: 'Stores', route: '/stores' },
        { label: isEdit ? 'Edit' : 'New' }
      ]"
    />

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <div v-if="pageLoading" class="text-center py-5">
          <span class="spinner-border"></span>
        </div>

        <form v-else @submit.prevent="handleSubmit" style="max-width: 600px;">
          <div class="row">
            <div class="col-md-4">
              <FormInput
                v-model="form.code"
                label="Store Code"
                placeholder="e.g. MAIN"
                required
                :disabled="isEdit"
                :error="errors.code"
              />
            </div>
            <div class="col-md-8">
              <FormInput
                v-model="form.name"
                label="Store Name"
                placeholder="Enter store name"
                required
                :error="errors.name"
              />
            </div>
          </div>

          <FormInput
            v-model="form.address"
            label="Address"
            type="textarea"
            placeholder="Enter store address"
            :error="errors.address"
          />

          <div class="row">
            <div class="col-md-6">
              <FormInput
                v-model="form.tin"
                label="TIN"
                placeholder="Enter TIN"
                :error="errors.tin"
              />
            </div>
            <div class="col-md-6">
              <FormInput
                v-model="form.phone"
                label="Phone"
                placeholder="Enter phone number"
                :error="errors.phone"
              />
            </div>
          </div>

          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              {{ isEdit ? 'Update' : 'Create' }}
            </button>
            <router-link to="/stores" class="btn btn-secondary">Cancel</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import storesApi from '@/api/stores'
import PageHeader from '@/components/common/PageHeader.vue'
import FormInput from '@/components/common/FormInput.vue'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const isEdit = computed(() => !!route.params.id)
const pageLoading = ref(false)
const submitting = ref(false)

const form = reactive({
  code: '',
  name: '',
  address: '',
  tin: '',
  phone: ''
})

const errors = reactive({
  code: '',
  name: '',
  address: '',
  tin: '',
  phone: ''
})

onMounted(async () => {
  if (isEdit.value) {
    pageLoading.value = true
    try {
      const { data } = await storesApi.getById(route.params.id)
      const payload = data.data || data
      Object.keys(form).forEach(key => {
        form[key] = payload[key] || ''
      })
    } catch (error) {
      appStore.showToast('Failed to load store', 'error')
      router.push('/stores')
    } finally {
      pageLoading.value = false
    }
  }
})

function validate() {
  let valid = true
  Object.keys(errors).forEach(k => (errors[k] = ''))

  if (!form.code.trim()) { errors.code = 'Store code is required'; valid = false }
  if (!form.name.trim()) { errors.name = 'Store name is required'; valid = false }

  return valid
}

async function handleSubmit() {
  if (!validate()) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await storesApi.update(route.params.id, form)
      appStore.showToast('Store updated successfully')
    } else {
      await storesApi.create(form)
      appStore.showToast('Store created successfully')
    }
    router.push('/stores')
  } catch (error) {
    const data = error.response?.data
    if (data?.errors) {
      Object.assign(errors, data.errors)
    } else {
      appStore.showToast(data?.message || 'Failed to save store', 'error')
    }
  } finally {
    submitting.value = false
  }
}
</script>

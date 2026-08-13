<template>
  <div>
    <PageHeader
      :title="isEdit ? 'Edit Supplier' : 'New Supplier'"
      :breadcrumbs="[
        { label: 'Dashboard', route: '/dashboard' },
        { label: 'Suppliers', route: '/suppliers' },
        { label: isEdit ? 'Edit' : 'New' }
      ]"
    />

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <div v-if="pageLoading" class="text-center py-5">
          <span class="spinner-border"></span>
        </div>

        <form v-else @submit.prevent="handleSubmit" style="max-width: 600px;">
          <FormInput
            v-model="form.name"
            label="Name"
            placeholder="Enter supplier name"
            required
            :error="errors.name"
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
                v-model="form.contactPerson"
                label="Contact Person"
                placeholder="Enter contact person"
                :error="errors.contactPerson"
              />
            </div>
          </div>

          <FormInput
            v-model="form.address"
            label="Address"
            type="textarea"
            placeholder="Enter address"
            :error="errors.address"
          />

          <div class="row">
            <div class="col-md-6">
              <FormInput
                v-model="form.contactNumber"
                label="Contact Number"
                placeholder="Enter contact number"
                :error="errors.contactNumber"
              />
            </div>
            <div class="col-md-6">
              <FormInput
                v-model="form.email"
                label="Email"
                type="email"
                placeholder="Enter email"
                :error="errors.email"
              />
            </div>
          </div>

          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              {{ isEdit ? 'Update' : 'Create' }}
            </button>
            <router-link to="/suppliers" class="btn btn-secondary">Cancel</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import suppliersApi from '@/api/suppliers'
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
  name: '',
  tin: '',
  address: '',
  contactPerson: '',
  contactNumber: '',
  email: ''
})

const errors = reactive({
  name: '',
  tin: '',
  address: '',
  contactPerson: '',
  contactNumber: '',
  email: ''
})

onMounted(async () => {
  if (isEdit.value) {
    pageLoading.value = true
    try {
      const { data } = await suppliersApi.getById(route.params.id)
      Object.keys(form).forEach(key => {
        form[key] = data[key] || ''
      })
    } catch (error) {
      appStore.showToast('Failed to load supplier', 'error')
      router.push('/suppliers')
    } finally {
      pageLoading.value = false
    }
  }
})

function validate() {
  let valid = true
  Object.keys(errors).forEach(k => (errors[k] = ''))

  if (!form.name.trim()) { errors.name = 'Name is required'; valid = false }

  if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = 'Invalid email format'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validate()) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await suppliersApi.update(route.params.id, form)
      appStore.showToast('Supplier updated successfully')
    } else {
      await suppliersApi.create(form)
      appStore.showToast('Supplier created successfully')
    }
    router.push('/suppliers')
  } catch (error) {
    const data = error.response?.data
    if (data?.errors) {
      Object.assign(errors, data.errors)
    } else {
      appStore.showToast(data?.message || 'Failed to save supplier', 'error')
    }
  } finally {
    submitting.value = false
  }
}
</script>

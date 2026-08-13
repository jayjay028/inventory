<template>
  <div>
    <PageHeader
      :title="isEdit ? 'Edit Category' : 'New Category'"
      :breadcrumbs="[
        { label: 'Dashboard', route: '/dashboard' },
        { label: 'Categories', route: '/categories' },
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
            placeholder="Enter category name"
            required
            :error="errors.name"
          />

          <FormInput
            v-model="form.description"
            label="Description"
            type="textarea"
            placeholder="Enter description"
            :error="errors.description"
          />

          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              {{ isEdit ? 'Update' : 'Create' }}
            </button>
            <router-link to="/categories" class="btn btn-secondary">Cancel</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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

const form = reactive({
  name: '',
  description: ''
})

const errors = reactive({
  name: '',
  description: ''
})

onMounted(async () => {
  if (isEdit.value) {
    pageLoading.value = true
    try {
      const { data } = await categoriesApi.getById(route.params.id)
      form.name = data.name || ''
      form.description = data.description || ''
    } catch (error) {
      appStore.showToast('Failed to load category', 'error')
      router.push('/categories')
    } finally {
      pageLoading.value = false
    }
  }
})

function validate() {
  let valid = true
  errors.name = ''
  errors.description = ''

  if (!form.name.trim()) {
    errors.name = 'Name is required'
    valid = false
  }
  return valid
}

async function handleSubmit() {
  if (!validate()) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await categoriesApi.update(route.params.id, form)
      appStore.showToast('Category updated successfully')
    } else {
      await categoriesApi.create(form)
      appStore.showToast('Category created successfully')
    }
    router.push('/categories')
  } catch (error) {
    const data = error.response?.data
    if (data?.errors) {
      Object.assign(errors, data.errors)
    } else {
      appStore.showToast(data?.message || 'Failed to save category', 'error')
    }
  } finally {
    submitting.value = false
  }
}
</script>

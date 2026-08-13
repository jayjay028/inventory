<template>
  <div>
    <PageHeader
      title="Add-ons"
      subtitle="Manage add-on master data"
      :breadcrumbs="[{ label: 'Dashboard', route: '/dashboard' }, { label: 'Add-ons' }]"
    >
      <template #actions>
        <button class="btn btn-primary" @click="openForm()">
          <i class="bi bi-plus-lg me-1"></i>New Add-on
        </button>
      </template>
    </PageHeader>

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <DataTable
          :columns="columns"
          :data="addons"
          :loading="loading"
          :total-pages="totalPages"
          :current-page="currentPage"
          searchable
          search-placeholder="Search add-ons..."
          @search="handleSearch"
          @page-change="handlePageChange"
        >
          <template #cell-amount="{ value }">
            {{ formatCurrency(value) }}
          </template>
          <template #cell-status="{ row }">
            <StatusBadge :status="row.active ? 'ACTIVE' : 'INACTIVE'" />
          </template>
          <template #actions="{ row }">
            <div class="d-flex gap-1">
              <button class="btn btn-sm btn-outline-primary" @click="openForm(row)">
                <i class="bi bi-pencil"></i>
              </button>
              <button
                class="btn btn-sm"
                :class="row.active ? 'btn-outline-danger' : 'btn-outline-success'"
                @click="toggleStatus(row)"
              >
                <i class="bi" :class="row.active ? 'bi-x-circle' : 'bi-check-circle'"></i>
              </button>
            </div>
          </template>
        </DataTable>
      </div>
    </div>

    <!-- Inline Form Modal -->
    <Teleport to="body">
      <div v-if="showForm" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,0.5);">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">{{ editingAddon ? 'Edit Add-on' : 'New Add-on' }}</h5>
              <button type="button" class="btn-close" @click="showForm = false"></button>
            </div>
            <div class="modal-body">
              <form @submit.prevent="handleSave">
                <FormInput
                  v-model="form.name"
                  label="Name"
                  placeholder="Enter add-on name"
                  required
                  :error="formErrors.name"
                />
                <FormInput
                  v-model="form.description"
                  label="Description"
                  type="textarea"
                  placeholder="Enter description"
                />
                <FormInput
                  v-model="form.amount"
                  label="Default Amount"
                  type="number"
                  placeholder="0.00"
                  :error="formErrors.amount"
                />

                <div class="d-flex gap-2 mt-3">
                  <button type="submit" class="btn btn-primary" :disabled="saving">
                    <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
                    {{ editingAddon ? 'Update' : 'Create' }}
                  </button>
                  <button type="button" class="btn btn-secondary" @click="showForm = false">Cancel</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <ConfirmDialog
      :show="confirmDialog.show"
      :title="confirmDialog.title"
      :message="confirmDialog.message"
      :variant="confirmDialog.variant"
      @confirm="confirmAction"
      @cancel="confirmDialog.show = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import addonsApi from '@/api/addons'
import PageHeader from '@/components/common/PageHeader.vue'
import DataTable from '@/components/common/DataTable.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import FormInput from '@/components/common/FormInput.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const columns = [
  { key: 'name', label: 'Name', sortable: true },
  { key: 'description', label: 'Description' },
  { key: 'amount', label: 'Amount', width: '120px' },
  { key: 'status', label: 'Status', width: '90px' }
]

const addons = ref([])
const loading = ref(true)
const currentPage = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')

const showForm = ref(false)
const editingAddon = ref(null)
const saving = ref(false)

const form = reactive({
  name: '',
  description: '',
  amount: ''
})

const formErrors = reactive({
  name: '',
  amount: ''
})

const confirmDialog = reactive({
  show: false,
  title: '',
  message: '',
  variant: 'danger',
  row: null
})

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: 10,
      search: searchQuery.value || undefined
    }
    const { data } = await addonsApi.getAll(params)
    addons.value = data.content || data
    totalPages.value = data.totalPages || 1
  } catch (error) {
    appStore.showToast('Failed to load add-ons', 'error')
  } finally {
    loading.value = false
  }
}

function handleSearch(query) {
  searchQuery.value = query
  currentPage.value = 1
  loadData()
}

function handlePageChange(page) {
  currentPage.value = page
  loadData()
}

function openForm(addon = null) {
  editingAddon.value = addon
  form.name = addon?.name || ''
  form.description = addon?.description || ''
  form.amount = addon?.amount ?? ''
  formErrors.name = ''
  formErrors.amount = ''
  showForm.value = true
}

async function handleSave() {
  formErrors.name = ''
  formErrors.amount = ''

  if (!form.name.trim()) {
    formErrors.name = 'Name is required'
    return
  }

  saving.value = true
  try {
    const payload = {
      name: form.name,
      description: form.description || null,
      amount: Number(form.amount) || 0
    }

    if (editingAddon.value) {
      await addonsApi.update(editingAddon.value.id, payload)
      appStore.showToast('Add-on updated successfully')
    } else {
      await addonsApi.create(payload)
      appStore.showToast('Add-on created successfully')
    }
    showForm.value = false
    loadData()
  } catch (error) {
    const data = error.response?.data
    appStore.showToast(data?.message || 'Failed to save add-on', 'error')
  } finally {
    saving.value = false
  }
}

function toggleStatus(row) {
  confirmDialog.row = row
  confirmDialog.title = row.active ? 'Deactivate Add-on' : 'Activate Add-on'
  confirmDialog.message = `Are you sure you want to ${row.active ? 'deactivate' : 'activate'} "${row.name}"?`
  confirmDialog.variant = row.active ? 'danger' : 'success'
  confirmDialog.show = true
}

async function confirmAction() {
  const row = confirmDialog.row
  confirmDialog.show = false
  try {
    await addonsApi.updateStatus(row.id, !row.active)
    appStore.showToast(`Add-on ${row.active ? 'deactivated' : 'activated'} successfully`)
    loadData()
  } catch (error) {
    appStore.showToast('Failed to update add-on status', 'error')
  }
}

function formatCurrency(value) {
  if (value == null) return '—'
  return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(value)
}
</script>

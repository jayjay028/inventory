<template>
  <div>
    <PageHeader
      title="Audit Trail"
      subtitle="System activity log"
      :breadcrumbs="[{ label: 'Dashboard', route: '/dashboard' }, { label: 'Audit Trail' }]"
    />

    <div class="card border-0 shadow-sm">
      <div class="card-body">
        <!-- Filters -->
        <div class="row g-2 mb-3">
          <div class="col-md-2">
            <select v-model="filters.entityType" class="form-select form-select-sm" @change="loadData">
              <option value="">All Entities</option>
              <option value="ITEM">Item</option>
              <option value="CATEGORY">Category</option>
              <option value="CUSTOMER">Customer</option>
              <option value="SUPPLIER">Supplier</option>
              <option value="TRANSACTION">Transaction</option>
              <option value="SALE">Sale</option>
              <option value="USER">User</option>
              <option value="SETTINGS">Settings</option>
            </select>
          </div>
          <div class="col-md-2">
            <select v-model="filters.action" class="form-select form-select-sm" @change="loadData">
              <option value="">All Actions</option>
              <option value="CREATE">Create</option>
              <option value="UPDATE">Update</option>
              <option value="DELETE">Delete</option>
              <option value="APPROVE">Approve</option>
              <option value="CANCEL">Cancel</option>
              <option value="VOID">Void</option>
              <option value="LOGIN">Login</option>
            </select>
          </div>
          <div class="col-md-2">
            <input v-model="filters.user" type="text" class="form-control form-control-sm" placeholder="User" @change="loadData" />
          </div>
          <div class="col-md-2">
            <input v-model="filters.dateFrom" type="date" class="form-control form-control-sm" @change="loadData" />
          </div>
          <div class="col-md-2">
            <input v-model="filters.dateTo" type="date" class="form-control form-control-sm" @change="loadData" />
          </div>
        </div>

        <!-- Table -->
        <div class="table-responsive">
          <table class="table table-hover align-middle">
            <thead class="table-light">
              <tr>
                <th style="width: 160px;">Timestamp</th>
                <th style="width: 120px;">User</th>
                <th style="width: 120px;">Entity</th>
                <th style="width: 100px;">Action</th>
                <th>Details</th>
                <th style="width: 40px;"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="6" class="text-center py-4">
                  <span class="spinner-border spinner-border-sm me-2"></span>Loading...
                </td>
              </tr>
              <tr v-else-if="!auditLogs.length">
                <td colspan="6" class="text-center py-4 text-muted">No audit logs found</td>
              </tr>
              <template v-for="(log, index) in auditLogs" :key="log.id">
                <tr>
                  <td><small>{{ formatDateTime(log.timestamp) }}</small></td>
                  <td>{{ log.userName }}</td>
                  <td><span class="badge bg-secondary">{{ log.entityType }}</span></td>
                  <td>
                    <span class="badge" :class="actionBadgeClass(log.action)">{{ log.action }}</span>
                  </td>
                  <td><small class="text-muted">{{ log.description }}</small></td>
                  <td>
                    <button
                      v-if="log.changes?.length"
                      class="btn btn-sm btn-link p-0"
                      @click="toggleExpand(index)"
                    >
                      <i class="bi" :class="expandedRows.includes(index) ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
                    </button>
                  </td>
                </tr>
                <!-- Expanded row -->
                <tr v-if="expandedRows.includes(index) && log.changes?.length">
                  <td colspan="6" class="bg-light p-0">
                    <div class="p-3">
                      <table class="table table-sm table-bordered mb-0 small">
                        <thead>
                          <tr>
                            <th>Field</th>
                            <th>Old Value</th>
                            <th>New Value</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="change in log.changes" :key="change.field">
                            <td class="fw-medium">{{ change.field }}</td>
                            <td class="text-danger">{{ change.oldValue ?? '—' }}</td>
                            <td class="text-success">{{ change.newValue ?? '—' }}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="d-flex justify-content-between align-items-center mt-3">
          <small class="text-muted">Page {{ currentPage }} of {{ totalPages }}</small>
          <nav>
            <ul class="pagination pagination-sm mb-0">
              <li class="page-item" :class="{ disabled: currentPage <= 1 }">
                <button class="page-link" @click="goToPage(currentPage - 1)"><i class="bi bi-chevron-left"></i></button>
              </li>
              <li
                v-for="page in visiblePages"
                :key="page"
                class="page-item"
                :class="{ active: page === currentPage }"
              >
                <button class="page-link" @click="goToPage(page)">{{ page }}</button>
              </li>
              <li class="page-item" :class="{ disabled: currentPage >= totalPages }">
                <button class="page-link" @click="goToPage(currentPage + 1)"><i class="bi bi-chevron-right"></i></button>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import auditApi from '@/api/audit'
import PageHeader from '@/components/common/PageHeader.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const auditLogs = ref([])
const loading = ref(true)
const currentPage = ref(1)
const totalPages = ref(1)
const expandedRows = ref([])

const filters = reactive({
  entityType: '',
  action: '',
  user: '',
  dateFrom: '',
  dateTo: ''
})

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)
  if (end - start + 1 < maxVisible) start = Math.max(1, end - maxVisible + 1)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: 15,
      entityType: filters.entityType || undefined,
      action: filters.action || undefined,
      user: filters.user || undefined,
      dateFrom: filters.dateFrom || undefined,
      dateTo: filters.dateTo || undefined
    }
    const { data } = await auditApi.getAll(params)
    auditLogs.value = data.content || data
    totalPages.value = data.totalPages || 1
  } catch (error) {
    appStore.showToast('Failed to load audit trail', 'error')
  } finally {
    loading.value = false
  }
}

function goToPage(page) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    expandedRows.value = []
    loadData()
  }
}

function toggleExpand(index) {
  const idx = expandedRows.value.indexOf(index)
  if (idx >= 0) {
    expandedRows.value.splice(idx, 1)
  } else {
    expandedRows.value.push(index)
  }
}

function formatDateTime(datetime) {
  if (!datetime) return ''
  return new Date(datetime).toLocaleString('en-PH', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function actionBadgeClass(action) {
  const map = {
    CREATE: 'bg-success',
    UPDATE: 'bg-primary',
    DELETE: 'bg-danger',
    APPROVE: 'bg-success',
    CANCEL: 'bg-danger',
    VOID: 'bg-danger',
    LOGIN: 'bg-info'
  }
  return map[action] || 'bg-secondary'
}
</script>
